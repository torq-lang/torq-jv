/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import org.torqlang.util.FileName;
import org.torqlang.util.FileType;
import org.torqlang.util.ResourcesFileBroker;

import java.lang.invoke.MethodHandles;
import java.util.List;

import static org.torqlang.util.ResourcesFileBroker.Entry;

public final class SystemSourceBroker {

    public static final List<Entry> CONTENT = List.of(
        new Entry(new FileName(FileType.DIRECTORY, "org"), List.of(
            new Entry(new FileName(FileType.DIRECTORY, "torqlang"), List.of(
                new Entry(new FileName(FileType.DIRECTORY, "local"), List.of(
                    new Entry(new FileName(FileType.DIRECTORY, "torqsrc"), List.of(
                        new Entry(new FileName(FileType.DIRECTORY, "system"), List.of(
                            new Entry(new FileName(FileType.DIRECTORY, "lang"), List.of(
                                new Entry(new FileName(FileType.TORQ, "Rec.torq"), null),
                                new Entry(new FileName(FileType.TORQ, "Str.torq"), null)
                            )),
                            new Entry(new FileName(FileType.DIRECTORY, "util"), List.of(
                                new Entry(new FileName(FileType.TORQ, "ArrayList.torq"), null),
                                new Entry(new FileName(FileType.TORQ, "HashMap.torq"), null)
                            ))
                        ))
                    ))
                ))
            ))
        ))
    );

    public static final FileName ORG = new FileName(FileType.DIRECTORY, "org");
    public static final FileName TORQLANG = new FileName(FileType.DIRECTORY, "torqlang");
    public static final FileName LOCAL = new FileName(FileType.DIRECTORY, "local");
    public static final FileName TORQSRC = new FileName(FileType.DIRECTORY, "torqsrc");

    public static final List<FileName> EXAMPLES_ROOT = List.of(ORG, TORQLANG, LOCAL, TORQSRC);

    public static ResourcesFileBroker create() {
        return new ResourcesFileBroker(MethodHandles.lookup().lookupClass(), List.of(EXAMPLES_ROOT), CONTENT);
    }

}
