/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import org.torqlang.lang.*;
import org.torqlang.util.FileName;
import org.torqlang.util.FileType;
import org.torqlang.util.SourceFileBroker;
import org.torqlang.util.SourceString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.torqlang.util.ListTools.nullSafeCopyOf;

/*
 * Torq Module Terminology
 *
 *     Workspace -- a set of root folders where each root is a tree arranged to reflect its packages.
 *         Folder -- a root folder where its tree is arranged to reflect packages.
 *             Package -- a path within a folder that contains packages.
 *                 Module -- a file within a folder that contains members.
 *                     Member -- is an Actor, Func, Type, etc.
 *
 * Torq Packages and Bundling
 *
 *     The Torq compiler can ultimately collect and bundle artifacts into physical packages that reflect the logical
 *     packaging declared in the workspace. A physical package is a Torq record where each feature is a formatted
 *     package name and each value is the compiled and exported member.
 *
 *     NOTE: Because "package" is a reserved word in Java, we will often use the word "bundle" instead.
 */
public class TorqCompiler implements TorqCompilerReady, TorqCompilerParsed, TorqCompilerCollected, TorqCompilerCompiled, TorqCompilerBundled {

    private final Map<String, Module> modules = new HashMap<>();
    private final Map<String, List<Module>> imports = new HashMap<>();
    private final Map<String, List<String>> exports = new HashMap<>();
    private State state = State.READY;
    private List<SourceFileBroker> workspace;
    private Consumer<String> messageListener;

    private TorqCompiler() {
    }

    public static TorqCompilerReady create() {
        return new TorqCompiler();
    }

    @Override
    public final TorqCompilerBundled bundle() throws Exception {
        if (state != State.COMPILED) {
            throw new IllegalStateException("Cannot parse at state: " + state);
        }
        notifyMessageListener("Bundling packages");
        // TODO -- bundle exports into importable bundles
        notifyMessageListener("Done bundling packages");
        this.state = State.BUNDLED;
        return this;
    }

    private ExportDetails checkForExport(Lang lang) {
        if (lang.metaStruct() == null) {
            return null;
        }
        MetaStruct metaStruct = lang.metaStruct();
        if (metaStruct instanceof MetaTuple metaTuple) {
            for (MetaValue v : metaTuple.values()) {
                if (isExportValue(v)) {
                    return new ExportDetails(null);
                }
            }
        } else if (metaStruct instanceof MetaRec metaRec) {
            for (MetaField metaField : metaRec.fields()) {
                // Handle the case where an export is a simple tag: meta#{'export'}
                if (metaField.feature instanceof Int64AsExpr && isExportValue(metaField.value)) {
                    return new ExportDetails(null);
                } else if (metaField.feature instanceof MetaValue metaValue && isExportValue(metaValue)) {
                    // Handle the case where an export is a feature-value pair: meta#{'export': true} or  meta#{'export': 'stereotype'}
                    if (metaField.value instanceof StrAsExpr stereotype) {
                        return new ExportDetails(stereotype.str.value);
                    } else if (metaField.value instanceof BoolAsExpr boolAsExpr) {
                        if (boolAsExpr.bool.value) {
                            return new ExportDetails(null);
                        } else {
                            return null;
                        }
                    } else {
                        throw new IllegalArgumentException("Invalid export request: " + metaField);
                    }
                }
            }
        }
        return null;
    }

    private boolean isExportValue(MetaValue metaValue) {
        return (metaValue instanceof StrAsExpr strAsExpr) && (strAsExpr.str.value.equals("export"));
    }

    @Override
    public final TorqCompilerCollected collect() throws Exception {
        if (state != State.PARSED) {
            throw new IllegalStateException("Cannot parse at state: " + state);
        }
        notifyMessageListener("Collecting imports and exports from each parsed module");
        for (Module module : modules.values()) {
            String qualifier = formatFileNamesAsPath(module.absolutePath);
            notifyMessageListener("Collecting from " + qualifier);
            Consumer<Lang> consumer = lang -> {
                if (lang instanceof ImportStmt importStmt) {
                    collectImports(importStmt, module);
                }
                ExportDetails details = checkForExport(lang);
                if (details != null) {
                    collectExport(lang, module, details);
                }
            };
            LangConsumer langMapper = new LangConsumer();
            module.moduleStmt.accept(langMapper, consumer);
        }
        notifyMessageListener("Done collecting imports and exports from each parsed module");
        notifyMessageListener("TODO: Validate imports and exports");
        notifyMessageListener("    TODO: A module must have at least one export");
        notifyMessageListener("    TODO: You cannot export and import same name in same module");
        this.state = State.COLLECTED;
        return this;
    }

    private void collectImports(ImportStmt importStmt, Module module) {
        for (ImportName in : importStmt.names) {
            String qualifiedName = formatIdentsAsQualifier(importStmt.qualifier) + "." + in.name.ident.name;
            notifyMessageListener("    Collecting import: " + qualifiedName);
            List<Module> existing = imports.computeIfAbsent(qualifiedName, k -> new ArrayList<>());
            if (existing.contains(module)) {
                throw new IllegalArgumentException("Duplicate import: " + qualifiedName);
            }
            existing.add(module);
        }
    }

    private void collectExport(Lang lang, Module module, ExportDetails details) {
        String qualifier = formatFileNamesAsQualifier(module.torqPackage);
        if (lang instanceof ActorStmt actorStmt) {
            String label;
            if (details != null) {
                label = "(Actor as " + details.stereotype + ")";
            } else {
                label = "(Actor)";
            }
            notifyMessageListener("    Collecting export: " + label + " " + qualifier + "." + actorStmt.name);
        } else if (lang instanceof TypeStmt typeStmt) {
            notifyMessageListener("    Collecting export: (Type) " + qualifier + "." + typeStmt.name);
        } else if (lang instanceof ProtocolStmt protocolStmt) {
            notifyMessageListener("    Collecting export: (Protocol) " + qualifier + "." + protocolStmt.name);
        } else {
            notifyMessageListener("    Collecting export: TODO " + lang.getClass().getSimpleName());
        }
    }

    public final TorqCompilerCompiled compile() throws Exception {
        if (state != State.COLLECTED) {
            throw new IllegalStateException("Cannot compile at state: " + state);
        }
        notifyMessageListener("Compiling modules");
        // TODO -- bundle exports into importable bundles
        notifyMessageListener("Done compiling modules");
        this.state = State.COMPILED;
        return this;
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

    private String formatFileNamesAsPath(List<FileName> path) {
        return path.stream().map(FileName::value).collect(Collectors.joining("/"));
    }

    private String formatFileNamesAsQualifier(List<FileName> path) {
        return path.stream().map(FileName::value).collect(Collectors.joining("."));
    }

    private String formatIdentsAsQualifier(List<IdentAsExpr> path) {
        return path.stream().map(id -> id.ident.name).collect(Collectors.joining("."));
    }

    private boolean isTorqSourceFile(String name) {
        return name.endsWith(".torq");
    }

    private void notifyMessageListener(String message) {
        if (messageListener != null) {
            messageListener.accept(message);
        }
    }

    @Override
    public final TorqCompilerParsed parse() throws Exception {
        if (state != State.READY) {
            throw new IllegalStateException("Cannot parse at state: " + state);
        }
        notifyMessageListener("Parsing source modules");
        for (SourceFileBroker fileBroker : workspace) {
            for (List<FileName> root : fileBroker.roots()) {
                List<FileName> files = fileBroker.list(root);
                for (FileName file : files) {
                    parse(fileBroker, root, file);
                }
            }
        }
        notifyMessageListener("Done parsing source modules");
        this.state = State.PARSED;
        return this;
    }

    private void parse(SourceFileBroker fileBroker, List<FileName> folder, FileName file) throws Exception {
        if (file.type() == FileType.FOLDER) {
            List<FileName> children = fileBroker.list(SourceFileBroker.append(folder, file));
            for (FileName child : children) {
                parse(fileBroker, SourceFileBroker.append(folder, file), child);
            }
        } else if (file.type() == FileType.SOURCE) {
            if (isTorqSourceFile(file.value())) {
                List<FileName> modulePath = SourceFileBroker.append(folder, file);
                SourceString source = fileBroker.source(modulePath);
                String absolutePathFormatted = formatFileNamesAsPath(modulePath);
                notifyMessageListener("Parsing source module: " + absolutePathFormatted);
                Parser parser = new Parser(source);
                List<FileName> qualifiedTorqName = fileBroker.trimRoot(modulePath);
                if (qualifiedTorqName.size() < 2) {
                    throw new IllegalArgumentException("Missing package");
                }
                List<FileName> torqPackage = qualifiedTorqName.subList(0, qualifiedTorqName.size() - 1);
                ModuleStmt moduleStmt = parser.parseModule();
                if (!equalsPackageStmt(torqPackage, moduleStmt.packageStmt)) {
                    throw new IllegalArgumentException("Package folder does not match package statement");
                }
                modules.put(absolutePathFormatted, new Module(modulePath, qualifiedTorqName, torqPackage, file, moduleStmt));
            } else {
                notifyMessageListener("Skipping unknown file type: " + file.value());
            }
        }
    }

    @Override
    public final TorqCompilerReady setMessageListener(Consumer<String> messageListener) {
        if (state != State.READY) {
            throw new IllegalStateException("Cannot setMessageListener at state: " + state);
        }
        this.messageListener = messageListener;
        return this;
    }

    @Override
    public final TorqCompilerReady setWorkspace(List<SourceFileBroker> fileBrokers) {
        if (state != State.READY) {
            throw new IllegalStateException("Cannot setFileBroker at state: " + state);
        }
        this.workspace = nullSafeCopyOf(fileBrokers);
        return this;
    }

    public final List<SourceFileBroker> workspace() {
        return workspace;
    }

    private enum State {
        READY,
        PARSED,
        COLLECTED,
        COMPILED,
        BUNDLED,
    }

    private record ExportDetails(String stereotype) {
    }

    private record Module(List<FileName> absolutePath,
                          List<FileName> qualifiedTorqName,
                          List<FileName> torqPackage,
                          FileName simpleTorqName,
                          ModuleStmt moduleStmt)
    {
    }

}
