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

import static org.torqlang.util.ListTools.last;

public class TorqCompiler implements TorqCompilerReady, TorqCompilerParsed {

    private final Map<String, ParsedModule> parsedModules = new HashMap<>();
    private State state = State.READY;
    private FileBroker fileBroker;
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

    public final FileBroker fileBroker() {
        return fileBroker;
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
        List<FileName> files = fileBroker.list();
        for (FileName name : files) {
            parse(List.of(name));
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

    private void parse(List<FileName> absolutePath) throws Exception {
        FileName fileName = last(absolutePath);
        if (fileName.type() == FileType.DIRECTORY) {
            List<FileName> files = fileBroker.list(absolutePath);
            for (FileName name : files) {
                List<FileName> filePath = FileBroker.append(absolutePath, name);
                parse(filePath);
            }
        } else if (fileName.type() == FileType.TORQ) {
            if (isTorqSourceFile(fileName.value())) {
                String source = fileBroker.source(absolutePath);
                String absolutePathFormatted = formatFilePath(absolutePath);
                notifyMessageListener("Parsing source: " + absolutePathFormatted);
                Parser parser = new Parser(source);
                List<FileName> qualifiedTorqName = fileBroker.trimRoot(absolutePath);
                if (qualifiedTorqName.size() < 2) {
                    throw new IllegalArgumentException("Missing package");
                }
                List<FileName> torqPackage = qualifiedTorqName.subList(0, qualifiedTorqName.size() - 1);
                ModuleStmt moduleStmt = parser.parseModule();
                if (!equalsPackageStmt(torqPackage, moduleStmt.packageStmt)) {
                    throw new IllegalArgumentException("Package directory does not match package statement");
                }
                parsedModules.put(absolutePathFormatted, new ParsedModule(absolutePath, qualifiedTorqName, torqPackage, fileName, moduleStmt));
            } else {
                notifyMessageListener("Skipping unknown file type: " + fileName.value());
            }
        }
    }

    @Override
    public final TorqCompilerReady setFileBroker(FileBroker fileBroker) {
        if (state != State.READY) {
            throw new IllegalStateException("Cannot setFileBroker at state: " + state);
        }
        this.fileBroker = fileBroker;
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
