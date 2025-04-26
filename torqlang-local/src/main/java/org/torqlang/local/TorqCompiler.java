/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import org.torqlang.lang.IdentAsExpr;
import org.torqlang.lang.ModuleStmt;
import org.torqlang.lang.PackageStmt;
import org.torqlang.lang.Parser;
import org.torqlang.util.FileBroker;
import org.torqlang.util.FileName;
import org.torqlang.util.FileType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.torqlang.util.ListTools.nullSafeCopyOf;

public class TorqCompiler implements TorqCompilerReady, TorqCompilerParsed {

    private final Map<String, ParsedModule> parsedModules = new HashMap<>();
    private State state = State.READY;
    private List<FileBroker> fileBrokers;
    private Consumer<String> messageListener;

    private TorqCompiler() {
    }

    public static TorqCompilerReady create() {
        return new TorqCompiler();
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

    public final List<FileBroker> fileBrokers() {
        return fileBrokers;
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
        for (FileBroker fileBroker : fileBrokers) {
            for (List<FileName> root : fileBroker.roots()) {
                List<FileName> files = fileBroker.list(root);
                for (FileName file : files) {
                    parse(fileBroker, root, file);
                }
            }
        }
        this.state = State.PARSED;
        return this;
    }

    private String formatFilePath(List<FileName> path) {
        return path.stream().map(FileName::value).collect(Collectors.joining("."));
    }

    private boolean isTorqSourceFile(String name) {
        return name.endsWith(".torq");
    }

    private void parse(FileBroker fileBroker, List<FileName> directory, FileName file) throws Exception {
        if (file.type() == FileType.DIRECTORY) {
            List<FileName> children = fileBroker.list(FileBroker.append(directory, file));
            for (FileName child : children) {
                parse(fileBroker, FileBroker.append(directory, file), child);
            }
        } else if (file.type() == FileType.TORQ) {
            if (isTorqSourceFile(file.value())) {
                List<FileName> modulePath = FileBroker.append(directory, file);
                String source = fileBroker.source(modulePath);
                String absolutePathFormatted = formatFilePath(modulePath);
                notifyMessageListener("Parsing source: " + absolutePathFormatted);
                Parser parser = new Parser(source);
                List<FileName> qualifiedTorqName = fileBroker.trimRoot(modulePath);
                if (qualifiedTorqName.size() < 2) {
                    throw new IllegalArgumentException("Missing package");
                }
                List<FileName> torqPackage = qualifiedTorqName.subList(0, qualifiedTorqName.size() - 1);
                ModuleStmt moduleStmt = parser.parseModule();
                if (!equalsPackageStmt(torqPackage, moduleStmt.packageStmt)) {
                    throw new IllegalArgumentException("Package directory does not match package statement");
                }
                parsedModules.put(absolutePathFormatted, new ParsedModule(modulePath, qualifiedTorqName, torqPackage, file, moduleStmt));
            } else {
                notifyMessageListener("Skipping unknown file type: " + file.value());
            }
        }
    }

    @Override
    public final TorqCompilerReady setFileBrokers(List<FileBroker> fileBrokers) {
        if (state != State.READY) {
            throw new IllegalStateException("Cannot setFileBroker at state: " + state);
        }
        this.fileBrokers = nullSafeCopyOf(fileBrokers);
        return this;
    }

    @Override
    public final TorqCompilerReady setMessageListener(Consumer<String> messageListener) {
        if (state != State.READY) {
            throw new IllegalStateException("Cannot setMessageListener at state: " + state);
        }
        this.messageListener = messageListener;
        return this;
    }

    private enum State {
        INIT,
        READY,
        PARSED,
    }

    private record ParsedModule(List<FileName> absolutePath,
                                List<FileName> qualifiedTorqName,
                                List<FileName> torqPackage,
                                FileName simpleTorqName,
                                ModuleStmt moduleStmt)
    {
    }

}
