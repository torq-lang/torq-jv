/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import org.torqlang.util.FileName;
import org.torqlang.util.FileType;
import org.torqlang.util.ResourceFileBroker;

import java.lang.invoke.MethodHandles;
import java.util.List;

import static org.torqlang.util.ResourceFileBroker.Entry;

public final class SystemSourceBroker {

    public static final List<Entry> CONTENT = List.of(
        new Entry(new FileName(FileType.FOLDER, "org"), List.of(
            new Entry(new FileName(FileType.FOLDER, "torqlang"), List.of(
                new Entry(new FileName(FileType.FOLDER, "local"), List.of(
                    new Entry(new FileName(FileType.FOLDER, "torqsrc"), List.of(
                        new Entry(new FileName(FileType.FOLDER, "system"), List.of(
                            new Entry(new FileName(FileType.FOLDER, "lang"), List.of(
                                new Entry(new FileName(FileType.SOURCE, "Rec.torq"), null),
                                new Entry(new FileName(FileType.SOURCE, "Str.torq"), null)
                            )),
                            new Entry(new FileName(FileType.FOLDER, "util"), List.of(
                                new Entry(new FileName(FileType.SOURCE, "ArrayList.torq"), null),
                                new Entry(new FileName(FileType.SOURCE, "HashMap.torq"), null),
                                new Entry(new FileName(FileType.SOURCE, "Message.torq"), null)
                            ))
                        ))
                    ))
                ))
            ))
        ))
    );

    public static final FileName ORG = new FileName(FileType.FOLDER, "org");
    public static final FileName TORQLANG = new FileName(FileType.FOLDER, "torqlang");
    public static final FileName LOCAL = new FileName(FileType.FOLDER, "local");
    public static final FileName TORQSRC = new FileName(FileType.FOLDER, "torqsrc");

    public static final List<FileName> EXAMPLES_ROOT = List.of(ORG, TORQLANG, LOCAL, TORQSRC);

    public static ResourceFileBroker create() {
        return new ResourceFileBroker(MethodHandles.lookup().lookupClass(), List.of(EXAMPLES_ROOT), CONTENT);
    }

}
