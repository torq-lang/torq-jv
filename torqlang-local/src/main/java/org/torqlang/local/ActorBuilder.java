/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import org.torqlang.klvm.*;
import org.torqlang.lang.ActorExpr;
import org.torqlang.lang.ActorStmt;
import org.torqlang.lang.Generator;
import org.torqlang.lang.Parser;
import org.torqlang.util.ListTools;
import org.torqlang.util.SourceSpan;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/*
 * Note that as we progress in the builder process, we gain access to properties while loosing access to methods.
 *
 * State transitions
 * =================
 *             (begin)          INIT
 * INIT        setSource        READY
 * INIT        setActorStmt     PARSED
 * READY       parse            PARSED
 * PARSED      rewrite          REWRITTEN      rewrite   --> actorExpr
 * REWRITTEN   generate         GENERATED      generate  --> createActorRecInstr
 * GENERATED   construct        CONSTRUCTED    construct --> actorRec
 * CONSTRUCTED configure        CONFIGURED     configure --> actorCfg
 * CONFIGURED  spawn            SPAWNED
 * SPAWNED     (end)
 *
 * Properties and Methods
 * ======================
 * INIT
 *   properties: (none)
 *   methods:    setAddress, setArgs, setSystem, setSource, setActorStmt, setActorCfg
 * READY
 *   properties: source
 *   methods:    parse, rewrite, generate, construct, configure, spawn
 * PARSED
 *   properties: source, actorStmt
 *   methods:    rewrite, generate, construct, configure, spawn
 * REWRITTEN
 *   properties: source, actorStmt, actorIdent, actorExpr
 *   methods:    generate, construct, configure, spawn
 * GENERATED
 *   properties: source, actorStmt, actorIdent, actorExpr, createActorRecInstr
 *   methods:    construct, configure, spawn
 * CONSTRUCTED
 *   properties: source, actorStmt, actorIdent, actorExpr, createActorRecInstr, actorRec
 *   methods:    configure, spawn
 * CONFIGURED
 *   properties: source, actorStmt, actorIdent, actorExpr, createActorRecInstr, actorRec, actorCfg
 *   methods:    spawn
 * SPAWNED
 *   properties: source, actorStmt, actorIdent, actorExpr, createActorRecInstr, actorRec, actorCfg, actorRef
 *   methods:    (none)
 *
 * Not shown above are the properties system, address, and args, which are available after INIT.
 */
public final class ActorBuilder implements ActorBuilderInit, ActorBuilderReady, ActorBuilderParsed,
    ActorBuilderRewritten, ActorBuilderGenerated, ActorBuilderConstructed, ActorBuilderConfigured, ActorBuilderSpawned
{
    private static final int TIME_SLICE_10_000 = 10_000;

    private State state;

    private Address address;
    private ActorImage actorImage;
    private ActorSystem system;
    private String source;
    private ActorExpr actorExpr;
    private ActorStmt actorStmt;
    private Ident actorIdent;
    private Instr createActorRecInstr;
    private Rec actorRec;
    private List<? extends CompleteOrIdent> args = List.of();
    private ActorCfg actorCfg;
    private LocalActor localActor;

    ActorBuilder() {
        state = State.INIT;
    }

    @Override
    public final ActorCfg actorCfg() {
        return actorCfg;
    }

    @Override
    public final ActorExpr actorExpr() {
        return actorExpr;
    }

    @Override
    public final Ident actorIdent() {
        return actorIdent;
    }

    @Override
    public final ActorImage actorImage() {
        if (actorImage == null) {
            checkAddress();
            Object response;
            try {
                response = RequestClient.builder()
                    .setAddress(address)
                    .send(actorRef(), CaptureImage.SINGLETON)
                    .awaitResponse(10, TimeUnit.MILLISECONDS);
                if (response instanceof FailedValue failedValue) {
                    throw new IllegalStateException(failedValue.toString());
                }
            } catch (Exception exc) {
                throw new IllegalStateException(exc);
            }
            actorImage = (ActorImage) response;
        }
        return actorImage;
    }

    @Override
    public final ActorImage actorImage(String source) throws Exception {
        setSource(source);
        spawn();
        return actorImage();
    }

    @Override
    public final Rec actorRec() {
        return actorRec;
    }

    @Override
    public final ActorRef actorRef() {
        return localActor;
    }

    @Override
    public final ActorStmt actorStmt() {
        return actorStmt;
    }

    @Override
    public final Address address() {
        return address;
    }

    @Override
    public final List<? extends CompleteOrIdent> args() {
        return args;
    }

    private void checkAddress() {
        if (address == null) {
            address = Address.UNDEFINED;
        }
    }

    private void checkSystem() {
        if (system == null) {
            system = ActorSystem.defaultSystem();
        }
    }

    private void computeInstr(Instr instr, Env env) {
        Stack stack;
        if (DebuggerSetting.get() != null) {
            DebugInstr debugInstr = new DebugInstr(DebuggerSetting.get(), instr, env, instr);
            stack = new Stack(debugInstr, Env.emptyEnv(), null);
        } else {
            stack = new Stack(instr, env, null);
        }
        Machine.compute(this, stack, TIME_SLICE_10_000);
    }

    @Override
    public final ActorCfg config() {
        return actorCfg;
    }

    @Override
    public final ActorBuilderConfigured configure() throws Exception {
        parseRewriteGenerateConstruct();
        if (state != State.CONSTRUCTED) {
            throw new IllegalStateException("Cannot spawn at state: " + state);
        }
        // The actor record will contain values (not vars). Therefore, we can access the ActorCfgtr directly.
        ActorCfgtr actorCfgtr = (ActorCfgtr) actorRec.findValue(Actor.NEW);
        Env env = Env.create(LocalActor.rootEnv(),
            List.of(
                new EnvEntry(Ident.$ACTOR_CFGTR, new Var(actorCfgtr)),
                new EnvEntry(Ident.$R, new Var())
            )
        );
        List<CompleteOrIdent> argsWithTarget = ListTools.append(CompleteOrIdent.class, args, Ident.$R);
        List<Instr> localInstrs = new ArrayList<>();
        localInstrs.add(new ApplyInstr(Ident.$ACTOR_CFGTR, argsWithTarget, SourceSpan.emptySourceSpan()));
        SeqInstr seqInstr = new SeqInstr(localInstrs, SourceSpan.emptySourceSpan());
        computeInstr(seqInstr, env);
        try {
            actorCfg = (ActorCfg) env.get(Ident.$R).resolveValue();
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
        state = State.CONFIGURED;
        return this;
    }

    @Override
    public final ActorBuilderConfigured configure(List<? extends CompleteOrIdent> args) throws Exception {
        setArgs(args);
        return configure();
    }

    @Override
    public final ActorBuilderConfigured configure(String source) throws Exception {
        setSource(source);
        return configure();
    }

    @Override
    public final ActorBuilderConfigured configure(String source, List<? extends CompleteOrIdent> args) throws Exception {
        setSource(source);
        setArgs(args);
        return configure();
    }

    @Override
    public final ActorBuilderConstructed construct() throws Exception {
        parseRewriteGenerate();
        if (state != State.GENERATED) {
            throw new IllegalStateException("Cannot createActorRec at state: " + state);
        }
        Env env = Env.create(LocalActor.rootEnv(), new EnvEntry(actorIdent, new Var()));
        computeInstr(createActorRecInstr, env);
        try {
            actorRec = (Rec) env.get(actorIdent).resolveValue();
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
        state = State.CONSTRUCTED;
        return this;
    }

    @Override
    public final ActorBuilderConstructed construct(String source) throws Exception {
        setSource(source);
        return construct();
    }

    @Override
    public final Instr createActorRecInstr() {
        return createActorRecInstr;
    }

    @Override
    public final ActorBuilderGenerated generate() throws Exception {
        parseRewrite();
        if (state != State.REWRITTEN) {
            throw new IllegalStateException("Cannot generate at state: " + state);
        }
        Generator g = new Generator();
        createActorRecInstr = g.acceptExpr(actorExpr, actorIdent);
        state = State.GENERATED;
        return this;
    }

    @Override
    public final ActorBuilderParsed parse() {
        if (state != State.READY) {
            throw new IllegalStateException("Cannot parse at state: " + state);
        }
        Parser p = new Parser(source);
        actorStmt = (ActorStmt) p.parse();
        state = State.PARSED;
        return this;
    }

    private void parseRewrite() {
        if (state == State.READY) {
            parse();
        }
        if (state == State.PARSED) {
            rewrite();
        }
    }

    private void parseRewriteGenerate() throws Exception {
        parseRewrite();
        if (state == State.REWRITTEN) {
            generate();
        }
    }

    private void parseRewriteGenerateConstruct() throws Exception {
        parseRewriteGenerate();
        if (state == State.GENERATED) {
            construct();
        }
    }

    private void parseRewriteGenerateConstructConfigure() throws Exception {
        parseRewriteGenerateConstruct();
        if (state == State.CONSTRUCTED) {
            configure();
        }
    }

    @Override
    public final ActorBuilderRewritten rewrite() {
        /*
            Rewrite:
                actor HelloWorld () in
                    ask 'hello' in
                        'Hello, World!'
                    end
                end
            As:
                HelloWorld = actor () in
                    ask 'hello' in
                        'Hello, World!'
                    end
                end
            Also, capture the actor name as an identifier.
         */
        if (state == State.READY) {
            parse();
        }
        if (state != State.PARSED) {
            throw new IllegalStateException("Cannot rewrite at state: " + state);
        }
        actorIdent = actorStmt.name;
        actorExpr = new ActorExpr(actorStmt.formalArgs, actorStmt.protocol, actorStmt.body, actorStmt);
        state = State.REWRITTEN;
        return this;
    }

    @Override
    public final ActorBuilderConfigured setActorCfg(ActorCfg actorCfg) {
        if (state != State.INIT) {
            throw new IllegalStateException("Cannot setActorCfg at state: " + state);
        }
        this.actorCfg = actorCfg;
        state = State.CONFIGURED;
        return this;
    }

    @Override
    public final ActorBuilderConstructed setActorRec(Rec actorRec) {
        if (state != State.INIT) {
            throw new IllegalStateException("Cannot setActorRec at state: " + state);
        }
        this.actorRec = actorRec;
        state = State.CONSTRUCTED;
        return this;
    }

    @Override
    public final ActorBuilderParsed setActorStmt(ActorStmt actorStmt) {
        if (state != State.INIT) {
            throw new IllegalStateException("Cannot setActorStmt at state: " + state);
        }
        this.actorStmt = actorStmt;
        state = State.PARSED;
        return this;
    }

    @Override
    public final ActorBuilderInit setAddress(Address address) {
        if (state != State.INIT) {
            throw new IllegalStateException("Cannot setAddress at state: " + state);
        }
        this.address = address;
        return this;
    }

    @Override
    public final ActorBuilderConstructed setArgs(List<? extends CompleteOrIdent> args) {
        if (state.ordinal() > State.CONSTRUCTED.ordinal()) {
            throw new IllegalStateException("Cannot setArgs at after CONSTRUCTED: " + state);
        }
        if (args == null) {
            throw new NullPointerException("args");
        }
        this.args = List.copyOf(args);
        return this;
    }

    @Override
    public final ActorBuilderReady setSource(String source) {
        if (state != State.INIT) {
            throw new IllegalStateException("Cannot setSource at state: " + state);
        }
        this.source = source;
        state = State.READY;
        return this;
    }

    @Override
    public final ActorBuilderInit setSystem(ActorSystem system) {
        if (state != State.INIT) {
            throw new IllegalStateException("Cannot setSystem at state: " + state);
        }
        this.system = system;
        return this;
    }

    @Override
    public final String source() {
        return source;
    }

    @Override
    public final ActorBuilderSpawned spawn(ActorCfg actorCfg) throws Exception {
        setActorCfg(actorCfg);
        return spawn();
    }

    @Override
    public final ActorBuilderSpawned spawn(Rec actorRec) throws Exception {
        setActorRec(actorRec);
        return spawn();
    }

    @Override
    public final ActorBuilderSpawned spawn(String source) throws Exception {
        setSource(source);
        return spawn();
    }

    @Override
    public final ActorBuilderSpawned spawn(String source, List<? extends CompleteOrIdent> args) throws Exception {
        setSource(source);
        setArgs(args);
        return spawn();
    }

    @Override
    public final ActorBuilderSpawned spawn() throws Exception {
        parseRewriteGenerateConstructConfigure();
        if (state != State.CONFIGURED) {
            throw new IllegalStateException("Cannot spawn at state: " + state);
        }
        checkAddress();
        checkSystem();
        localActor = new LocalActor(address, system);
        localActor.configure(actorCfg);
        state = State.SPAWNED;
        return this;
    }

    public final ActorSystem system() {
        return system;
    }

    private enum State {
        INIT,
        READY,
        PARSED,
        REWRITTEN,
        GENERATED,
        CONSTRUCTED,
        CONFIGURED,
        SPAWNED
    }

}
