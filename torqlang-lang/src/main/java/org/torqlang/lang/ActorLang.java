/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.lang;

import org.torqlang.util.SourceSpan;

import java.util.ArrayList;
import java.util.List;

import static org.torqlang.util.ListTools.nullSafeCopyOf;

public abstract class ActorLang extends AbstractLang implements StmtOrExpr {

    public final List<Pat> formalArgs;
    public final List<StmtOrExpr> body;

    private List<StmtOrExpr> initializer;
    private List<AskStmt> askHandlers;
    private List<TellStmt> tellHandlers;

    public ActorLang(List<Pat> formalArgs, List<StmtOrExpr> body, SourceSpan sourceSpan) {
        super(sourceSpan);
        this.formalArgs = nullSafeCopyOf(formalArgs);
        this.body = nullSafeCopyOf(body);
    }

    public final List<? extends AskStmt> askHandlers() {
        lazyLoad();
        return askHandlers;
    }

    public final List<? extends StmtOrExpr> initializer() {
        lazyLoad();
        return initializer;
    }

    private void lazyLoad() {
        if (initializer != null) {
            return;
        }
        initializer = new ArrayList<>(body.size());
        askHandlers = new ArrayList<>(body.size());
        tellHandlers = new ArrayList<>(body.size());
        for (StmtOrExpr sox : body) {
            if (sox instanceof AskStmt askHandler) {
                askHandlers.add(askHandler);
            } else if (sox instanceof TellStmt tellHandler) {
                tellHandlers.add(tellHandler);
            } else {
                initializer.add(sox);
            }
        }
    }

    public final List<TellStmt> tellHandlers() {
        lazyLoad();
        return tellHandlers;
    }

}
