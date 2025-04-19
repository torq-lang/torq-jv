/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public interface FileBroker {

    default List<Name> append(List<Name> directory, Name file) {
        ArrayList<Name> answer = new ArrayList<>(directory);
        answer.add(file);
        return List.of(answer.toArray(new Name[0]));
    }

    List<Name> list();

    List<Name> list(List<Name> path);

    String source(List<Name> path) throws IOException;

    enum NameType {
        FILE,
        DIRECTORY,
    }

    record Name(NameType type, String value) {
    }
}
