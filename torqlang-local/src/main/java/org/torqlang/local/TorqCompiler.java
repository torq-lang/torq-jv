/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import org.torqlang.klvm.*;
import org.torqlang.lang.*;
import org.torqlang.util.*;

import java.util.*;
import java.util.stream.Collectors;

import static org.torqlang.util.ListTools.nullSafeCopyOf;

/*
 * Torq Module Terminology
 *
 *     Workspace -- a set of root folders where each root is a tree arranged to reflect its packages.
 *         Folder -- a filesystem folder with a tree structure arranged to reflect packages.
 *             Package -- a branch within a folder tree that contains files.
 *                 Module -- a file within a folder branch that contains members.
 *                     Member -- is an Actor, Func, Type, etc.
 *
 *     Bundle - another name for package sometimes used to avoid conflicts with the reserved word "package".
 *
 * Composites and Actors are formulated as records
 *     $new - contains the instance constructor
 *     $type - is present if the constructor creates a Composite
 *     $protocol - is present if the constructor creates an Actor
 *
 * Torq Packages and Bundling
 *
 *     The Torq compiler can ultimately collect and bundle artifacts into physical packages that reflect the logical
 *     packaging declared in the workspace. A physical package is a Torq record where each feature is an identifier
 *     name and each value is the compiled and exported member. A physical package is registered in a map where the
 *     key is a package name (qualifier) and the value is a record containing exported values.
 *
 * Clients of a TorqCompiler ultimately need:
 *
 *     A runnable system:
 *
 *     1) Package records for an ActorSystem
 *     2) An ActorSystem primed with packages
 *
 *     Maybe API handlers:
 *
 *     1) API handlers are a kind of actor that take no parameters
 *     2) API handlers are complete closures with no unbound free variables
 *     3) Each API handler is created many times from a single actor image to service API calls
 *
 *     Maybe API routes:
 *
 *     1) An ActorImage bound to the ActorSystem created above for each API handler
 *     2) An ApiDesc for each API handler
 *     3) An ApiRoute for each API handler
 *
 * Stages:
 *
 *     Parse: Visit each node in the workspace
 *         Parse each torq file into a Module structure
 *             After parsing, a module contains:
 *                 qualifiedName, absolutePath, qualifiedTorqFile, torqPackage, torqFile, moduleStmt
 *
 *     Collect: Collect imports and exports for each parsed module
 *         The objective is to build a global members map by qualified name. At each qualified name, we know
 *         which module exported the name and which modules import it.
 *             QUALIFIED-NAME -> (EXPORT, IMPORTS)
 *             Where QUALIFIED-NAME is a qualified member name, such as 'system.util.HashMap',
 *                   EXPORT is the module that exports the qualified name paired with the exported value
 *                   IMPORTS is a list of modules that import the qualified name
 *         Visit each module:
 *             Run the LangConsumer looking for imports and exports
 *                 For each import or export found, update the global members map
 *         Search for conflicts and incomplete information
 *             1. A module has no exports -- ERROR
 *             2. A member is imported but there is no export -- ERROR
 *             3. A member is exported but there is no import -- WARNING
 *             4. A native module does not match its Torq type -- ERROR
 *
 *     Generate: Generate kernel instructions for each parsed module
 *         Create a global type environment to be used by the generation phase
 *             This is a kind of "bundle" that bundles just types
 *         Visit each module with the global type environment and generate its kernel instructions
 *             Set kernel instructions on each module using Module.setModuleInstr() method
 *
 *     Bundle: Bundle exported members by qualifier
 *         Gather exported members for each qualifier
 *         Gather exported API handlers for each qualifier
 *         Build a package record of exported members for each qualifier
 *
 */
public class TorqCompiler implements TorqCompilerReady, TorqCompilerParsed, TorqCompilerGenerated, TorqCompilerCollected, TorqCompilerBundled {

    private static final String DOT_TORQ = ".torq";

    private final Map<String, Module> modulesByAbsolutePath = new HashMap<>();
    private final Map<String, Member> membersByQualifiedName = new HashMap<>();
    private final Map<String, Package> membersByQualifier = new HashMap<>();
    private final List<Message> messages = new ArrayList<>();

    private MessageType loggingLevel = MessageType.INFO;
    private State state = State.READY;
    private List<SourceFileBroker> workspace;

    private TorqCompiler() {
    }

    public static TorqCompilerReady create() {
        return new TorqCompiler();
    }

    /*
     * Return the name of a qualified name. For example return "HashMap" from "system.util.HashMap".
     * Throw an IllegalArgumentException if the name is not qualified.
     */
    static String nameFrom(String qualifiedName) {
        int lastDot = qualifiedName.lastIndexOf('.');
        if (lastDot != -1) {
            return qualifiedName.substring(lastDot + 1);
        } else {
            throw new IllegalArgumentException("No qualifier");
        }
    }

    /*
     * Return the qualifier of a qualified name. For example return "system.util" from "system.util.HashMap".
     * Throw an IllegalArgumentException if the name is not qualified.
     */
    static String qualifierFrom(String qualifiedName) {
        int lastDot = qualifiedName.lastIndexOf('.');
        if (lastDot != -1) {
            return qualifiedName.substring(0, lastDot);
        } else {
            throw new IllegalArgumentException("No qualifier");
        }
    }

    private void addInfoMessage(String message) {
        if (loggingLevel.ordinal() >= MessageType.INFO.ordinal()) {
            messages.add(Message.create("TorqCompilerTrace", MessageType.INFO, "[INFO ] " + message));
        }
    }

    private void addTraceMessage(String message) {
        if (loggingLevel.ordinal() >= MessageType.TRACE.ordinal()) {
            messages.add(Message.create("TorqCompilerTrace", MessageType.TRACE, "[TRACE] " + message));
        }
    }

    private void addWarnMessage(String message) {
        if (loggingLevel.ordinal() >= MessageType.WARN.ordinal()) {
            messages.add(Message.create("TorqCompilerWarn", MessageType.WARN, "[WARN ] " + message));
        }
    }

    @Override
    public final TorqCompilerBundled bundle() {
        if (state != State.GENERATED) {
            throw new TorqCompilerError("Cannot bundle at state: " + state, messages);
        }
        addInfoMessage("Bundling exported members by qualifier");
        for (Member member : membersByQualifiedName.values()) {
            Package bundle = membersByQualifier.get(member.qualifier);
            if (bundle == null) {
                bundle = new Package(member.qualifier);
                membersByQualifier.put(member.qualifier, bundle);
            }
            if (isApiHandler(member.export)) {
                addTraceMessage("Adding exported API handler: " + member.qualifiedName);
                bundle.addHandler(member);
            } else {
                addTraceMessage("Adding package member: " + member.qualifiedName);
                bundle.addMember(member);
            }
        }
        addInfoMessage("Done bundling exported members by qualifier");
        addInfoMessage("Building package records for each qualifier");
        for (Package bundle : membersByQualifier.values()) {
            CompleteRecBuilder builder = Rec.completeRecBuilder();
            for (Member member : bundle.members) {
                Complete value;
                if (member.export instanceof ActorExport actorExport) {
                    //throw new NeedsImpl();
                    continue;
                } else if (member.export instanceof TypeExport typeExport) {
                    value = OpaqueType.create(typeExport.typeStmt);
                } else if (member.export instanceof ProtocolExport protocolExport) {
                    value = OpaqueProtocol.create(protocolExport.protocolStmt);
                } else {
                    throw new NeedsImpl();
                }
                builder.addField(Str.of(member.name), value);
            }
            bundle.packageRec = builder.build();
        }
        addInfoMessage("Done building package records for each qualifier");
        addInfoMessage("Creating actor images for API handlers");
        addTraceMessage("---TODO---");
        addInfoMessage("Done creating actor images for API handlers");
        this.state = State.BUNDLED;
        return this;
    }

    @Override
    public final TorqCompilerCollected collect() throws Exception {
        if (state != State.PARSED) {
            throw new TorqCompilerError("Cannot collect at state: " + state, messages);
        }
        addInfoMessage("Collecting imports and exports from each parsed module");
        for (Module module : modulesByAbsolutePath.values()) {
            String qualifier = formatFileNamesAsPath(module.absolutePath);
            addTraceMessage("Collecting from " + qualifier);
            LangConsumer.consume(module.moduleStmt(), lang -> {
                if (lang instanceof ImportStmt importStmt) {
                    collectImports(importStmt, module);
                }
                FindExportResult langExport = findExport(lang);
                if (langExport != null) {
                    collectExport(langExport, module);
                }
            });
        }
        addInfoMessage("Done collecting imports and exports from each parsed module");
        addInfoMessage("Validating import and export references");
        addTraceMessage("Validating each module has an export");
        /*
         * The moving parts in this scenario are not obvious. Multiple file brokers can contribute to a module and
         * multiple modules can comprise a package. Therefore, we must iterate all modules by absolute path verifying
         * that an export exists somewhere for the package.
         */
        for (Module module : modulesByAbsolutePath.values()) {
            boolean found = false;
            for (Member member : membersByQualifiedName.values()) {
                if (member.qualifier.equals(module.qualifier) && member.export != null) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new TorqCompilerError("Module has no exports: " + module.qualifiedName, messages);
            }
        }
        /*
         * An import must have an export
         */
        addTraceMessage("Validating imports have an export");
        for (Member member : membersByQualifiedName.values()) {
            if (!member.imports.isEmpty() && member.export == null) {
                throw new TorqCompilerError("Import does not have an export: " + member.qualifiedName, messages);
            }
        }
        /*
         * TODO: Validate native module references
         *       A Torq type should have all its members present in the native module
         */
        addTraceMessage("---TODO--- Validate Torq types against their native module references");
        addInfoMessage("Done validating import and export references");
        this.state = State.COLLECTED;
        return this;
    }

    /*
     * We call this method when an export was found in a parsed module, and we want to save that export for further
     * processing.
     */
    private void collectExport(FindExportResult exportResult, Module module) {
        String formattedQualifier = formatFileNamesAsQualifier(module.torqPackage);
        Export langExport;
        if (exportResult.lang instanceof ActorStmt actorStmt) {
            langExport = new ActorExport(actorStmt, exportResult.stereotype, module);
        } else if (exportResult.lang instanceof TypeStmt typeStmt) {
            langExport = new TypeExport(typeStmt, module);
        } else if (exportResult.lang instanceof ProtocolStmt protocolStmt) {
            langExport = new ProtocolExport(protocolStmt, module);
        } else {
            if (!(exportResult.lang instanceof HandleStmt)) {
                throw new TorqCompilerError("Invalid export: " + exportResult.lang, messages);
            }
            // At this time, we only process actor exports and not their handlers. Later, we will process handlers when
            // we make a pass to create API routers.
            langExport = null;
        }
        if (langExport != null) {
            String qualifiedName = formattedQualifier + "." + langExport.simpleName();
            addTraceMessage("Collecting export: " + qualifiedName);
            Member member = membersByQualifiedName.get(qualifiedName);
            if (member == null) {
                member = new Member(qualifiedName);
                membersByQualifiedName.put(qualifiedName, member);
            } else {
                if (member.export != null) {
                    throw new TorqCompilerError("Duplicate export name: " + qualifiedName, messages);
                }
            }
            member.export = langExport;
        }
    }

    /*
     * Collect the given import that was found in the given module.
     */
    private void collectImports(ImportStmt importStmt, Module module) {
        String formattedQualifier = formatIdentsAsQualifier(importStmt.qualifier);
        for (ImportName in : importStmt.names) {
            String qualifiedName = formattedQualifier + "." + in.name.ident.name;
            addTraceMessage("Collecting import: " + qualifiedName);
            Member member = membersByQualifiedName.computeIfAbsent(qualifiedName, Member::new);
            if (member.imports.contains(module)) {
                throw new TorqCompilerError("Duplicate import name: " + qualifiedName, messages);
            }
            member.imports.add(module);
        }
    }

    private boolean equalsPackageStmt(List<FileName> torqPackage, PackageStmt packageStmt) {
        if (torqPackage.size() != packageStmt.path.size()) {
            return false;
        }
        for (int i = 0; i < torqPackage.size(); i++) {
            FileName left = torqPackage.get(i);
            IdentAsExpr right = packageStmt.path.get(i);
            if (!left.value().equals(right.ident.name)) {
                return false;
            }
        }
        return true;
    }

    private FindExportResult findExport(Lang lang) {
        if (lang.metaStruct() == null) {
            return null;
        }
        MetaStruct metaStruct = lang.metaStruct();
        if (metaStruct instanceof MetaTuple metaTuple) {
            for (MetaValue v : metaTuple.values()) {
                if (isExportValue(v)) {
                    return new FindExportResult(lang, null);
                }
            }
        } else if (metaStruct instanceof MetaRec metaRec) {
            for (MetaField metaField : metaRec.fields()) {
                // Handle the case where an export is a simple tag: meta#{'export'}
                if (metaField.feature instanceof Int64AsExpr && isExportValue(metaField.value)) {
                    return new FindExportResult(lang, null);
                } else if (metaField.feature instanceof MetaValue metaValue && isExportValue(metaValue)) {
                    // Handle the case where an export is a feature-value pair: meta#{'export': true} or  meta#{'export': 'stereotype'}
                    if (metaField.value instanceof StrAsExpr stereotype) {
                        return new FindExportResult(lang, stereotype.str.value);
                    } else if (metaField.value instanceof BoolAsExpr boolAsExpr) {
                        if (boolAsExpr.bool.value) {
                            return new FindExportResult(lang, null);
                        } else {
                            return null;
                        }
                    } else {
                        throw new TorqCompilerError("Invalid export request: " + metaField, messages);
                    }
                }
            }
        }
        return null;
    }

    private String formatFileNamesAsPath(List<FileName> path) {
        return path.stream().map(FileName::value).collect(Collectors.joining("/"));
    }

    private String formatFileNamesAsQualifier(List<FileName> path) {
        return path.stream().map(FileName::value).collect(Collectors.joining("."));
    }

    private String formatIdentsAsQualifier(List<IdentAsExpr> path) {
        return path.stream().map(id -> id.ident.name).collect(Collectors.joining("."));
    }

    @Override
    public final TorqCompilerGenerated generate() throws Exception {
        if (state != State.COLLECTED) {
            throw new TorqCompilerError("Cannot generate at state: " + state, messages);
        }
        addInfoMessage("Generating module instructions");
        for (Map.Entry<String, Module> module : modulesByAbsolutePath.entrySet()) {
            addTraceMessage("Generating " + module.getKey());
            Generator g = new Generator();
            Instr instr = g.acceptStmt(module.getValue().moduleStmt);
            module.getValue().setModuleInstr(instr);
        }
        addInfoMessage("Done generating module instructions");
        this.state = State.GENERATED;
        return this;
    }

    private boolean isApiHandler(Export export) {
        if (!(export instanceof ActorExport actorExport)) {
            return false;
        }
        return actorExport.stereotype != null && actorExport.stereotype.equals("api-handler");
    }

    private boolean isExportValue(MetaValue metaValue) {
        return (metaValue instanceof StrAsExpr strAsExpr) && (strAsExpr.str.value.equals("export"));
    }

    private boolean isTorqSourceFile(String name) {
        return name.endsWith(DOT_TORQ);
    }

    @Override
    public final List<Message> messages() {
        return Collections.unmodifiableList(messages);
    }

    @Override
    public final TorqCompilerParsed parse() throws Exception {
        if (state != State.READY) {
            throw new TorqCompilerError("Cannot parse at state: " + state, messages);
        }
        addInfoMessage("Parsing source modules");
        for (SourceFileBroker fileBroker : workspace) {
            for (List<FileName> root : fileBroker.roots()) {
                List<FileName> files = fileBroker.list(root);
                for (FileName file : files) {
                    parse(fileBroker, root, file);
                }
            }
        }
        addInfoMessage("Done parsing source modules");
        this.state = State.PARSED;
        return this;
    }

    private void parse(SourceFileBroker fileBroker, List<FileName> folderPath, FileName fileName) throws Exception {
        if (fileName.type() == FileType.FOLDER) {
            parseFolder(fileBroker, folderPath, fileName);
        } else if (fileName.type() == FileType.SOURCE && isTorqSourceFile(fileName.value())) {
            parseTorqSource(fileBroker, folderPath, fileName);
        } else {
            addWarnMessage("Skipping unknown file type: " + fileName.value());
        }
    }

    private void parseFolder(SourceFileBroker fileBroker, List<FileName> folderPath, FileName fileName) throws Exception {
        List<FileName> children = fileBroker.list(SourceFileBroker.append(folderPath, fileName));
        for (FileName child : children) {
            parse(fileBroker, SourceFileBroker.append(folderPath, fileName), child);
        }
    }

    private void parseTorqSource(SourceFileBroker fileBroker, List<FileName> folderPath, FileName fileName) throws Exception {
        List<FileName> absolutePath = SourceFileBroker.append(folderPath, fileName);
        SourceString source = fileBroker.source(absolutePath);
        String absolutePathFormatted = formatFileNamesAsPath(absolutePath);
        addTraceMessage("Parsing source module: " + absolutePathFormatted);
        Parser parser = new Parser(source);
        List<FileName> qualifiedTorqFile = fileBroker.trimRoot(absolutePath);
        if (qualifiedTorqFile.size() < 2) {
            throw new TorqCompilerError("Missing package", messages);
        }
        List<FileName> torqPackage = qualifiedTorqFile.subList(0, qualifiedTorqFile.size() - 1);
        ModuleStmt moduleStmt = parser.parseModule();
        if (!equalsPackageStmt(torqPackage, moduleStmt.packageStmt)) {
            throw new TorqCompilerError("Package folder does not match package statement", messages);
        }
        String torqFileName = ListTools.last(qualifiedTorqFile).value();
        String simpleTorqFileName = torqFileName.substring(0, torqFileName.lastIndexOf(DOT_TORQ));
        String qualifiedName;
        if (qualifiedTorqFile.size() == 1) {
            qualifiedName = simpleTorqFileName;
        } else {
            qualifiedName = formatFileNamesAsQualifier(qualifiedTorqFile.subList(0, qualifiedTorqFile.size() - 1)) +
                "." + simpleTorqFileName;
        }
        modulesByAbsolutePath.put(absolutePathFormatted,
            Module.createAfterParse(qualifiedName, absolutePath, qualifiedTorqFile, torqPackage, fileName, moduleStmt));
    }

    @Override
    public final TorqCompilerReady setLoggingLevel(MessageType loggingLevel) {
        this.loggingLevel = loggingLevel;
        return this;
    }

    @Override
    public final TorqCompilerReady setWorkspace(List<SourceFileBroker> workspace) {
        if (state != State.READY) {
            throw new TorqCompilerError("Cannot setWorkspace at state: " + state, messages);
        }
        this.workspace = nullSafeCopyOf(workspace);
        return this;
    }

    public final List<SourceFileBroker> workspace() {
        return workspace;
    }

    private enum State {
        READY,
        PARSED,
        GENERATED,
        COLLECTED,
        BUNDLED,
    }

    private interface Export {
        String simpleName();

        Module module();
    }

    private record FindExportResult(Lang lang, String stereotype) {
    }

    private record ActorExport(ActorStmt actorStmt, String stereotype, Module module) implements Export {
        @Override
        public final String simpleName() {
            return actorStmt.name.ident.name;
        }
    }

    private record ProtocolExport(ProtocolStmt protocolStmt, Module module) implements Export {
        @Override
        public final String simpleName() {
            return protocolStmt.name.ident.name;
        }
    }

    private record TypeExport(TypeStmt typeStmt, Module module) implements Export {
        @Override
        public final String simpleName() {
            return typeStmt.name().ident().name;
        }
    }

    private static final class Package {
        private final String qualifier;
        private final List<Member> members = new ArrayList<>();
        private final List<Member> handlers = new ArrayList<>();
        private CompleteRec packageRec;

        private Package(String qualifier) {
            this.qualifier = qualifier;
        }

        final void addMember(Member member) {
            this.members.add(member);
        }

        final void addHandler(Member handler) {
            this.handlers.add(handler);
        }

        @Override
        public final boolean equals(Object other) {
            if (other == this) return true;
            if (other == null || other.getClass() != this.getClass()) {
                return false;
            }
            var that = (Package) other;
            return Objects.equals(this.qualifier, that.qualifier);
        }

        @Override
        public final int hashCode() {
            return Objects.hash(qualifier);
        }

        @Override
        public final String toString() {
            return qualifier;
        }
    }

    private static final class Member {
        private final String qualifiedName;
        private final String name;
        private final String qualifier;
        private final List<Module> imports;
        private Export export;

        private Member(String qualifiedName) {
            this.qualifiedName = qualifiedName;
            this.name = nameFrom(qualifiedName);
            this.qualifier = qualifierFrom(qualifiedName);
            this.imports = new ArrayList<>();
        }

        @Override
        public final boolean equals(Object other) {
            if (other == this) return true;
            if (other == null || other.getClass() != this.getClass()) {
                return false;
            }
            var that = (Member) other;
            return Objects.equals(this.qualifiedName, that.qualifiedName);
        }

        @Override
        public final int hashCode() {
            return Objects.hash(qualifiedName);
        }

        @Override
        public final String toString() {
            return qualifiedName;
        }
    }

    private static final class Module {
        private final String qualifiedName;
        private final String qualifier;
        private final List<FileName> absolutePath;
        private final List<FileName> qualifiedTorqFile;
        private final List<FileName> torqPackage;
        private final FileName torqFile;
        private final ModuleStmt moduleStmt;
        private Instr moduleInstr;

        private Module(String qualifiedName,
                       List<FileName> absolutePath,
                       List<FileName> qualifiedTorqFile,
                       List<FileName> torqPackage,
                       FileName torqFile,
                       ModuleStmt moduleStmt,
                       Instr moduleInstr)
        {
            this.qualifiedName = qualifiedName;
            this.qualifier = qualifierFrom(qualifiedName);
            this.absolutePath = absolutePath;
            this.qualifiedTorqFile = qualifiedTorqFile;
            this.torqPackage = torqPackage;
            this.torqFile = torqFile;
            this.moduleStmt = moduleStmt;
            this.moduleInstr = moduleInstr;
        }

        static Module createAfterParse(String qualifiedName,
                                       List<FileName> absolutePath,
                                       List<FileName> qualifiedTorqFile,
                                       List<FileName> torqPackage,
                                       FileName torqFile,
                                       ModuleStmt moduleStmt)
        {
            return new Module(qualifiedName, absolutePath, qualifiedTorqFile, torqPackage, torqFile, moduleStmt, null);
        }

        @Override
        public final boolean equals(Object other) {
            if (other == this) return true;
            if (other == null || other.getClass() != this.getClass()) {
                return false;
            }
            var that = (Module) other;
            return Objects.equals(this.qualifiedName, that.qualifiedName);
        }

        @Override
        public final int hashCode() {
            return Objects.hash(qualifiedName);
        }

        @Override
        public final String toString() {
            return qualifiedName;
        }

        final ModuleStmt moduleStmt() {
            return moduleStmt;
        }

        final void setModuleInstr(Instr moduleInstr) {
            this.moduleInstr = moduleInstr;
        }

    }

}
