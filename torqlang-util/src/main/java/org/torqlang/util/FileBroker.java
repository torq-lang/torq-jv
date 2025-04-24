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

    /*
     * Append name to the end of directory.
     */
    static List<FileName> append(List<FileName> directory, FileName file) {
        if (!directory.isEmpty() && !ListTools.last(directory).type().equals(FileType.DIRECTORY)) {
            throw new IllegalArgumentException("Not a directory");
        }
        ArrayList<FileName> answer = new ArrayList<>(directory);
        answer.add(file);
        return List.of(answer.toArray(new FileName[0]));
    }

    /*
     * Return true if path begins with root.
     */
    static boolean hasRoot(List<FileName> path, List<FileName> root) {
        if (root.size() > path.size()) {
            return false;
        }
        for (int i = 0; i < root.size(); i++) {
            if (!root.get(i).equals(path.get(i))) {
                return false;
            }
        }
        return true;
    }

    /*
     * List all names from all roots.
     */
    List<FileName> list();

    /*
     * List all names in the given absolute path.
     */
    List<FileName> list(List<FileName> absolutePath);

    /*
     * A file broker can serve source from multiple roots. In other words, a file broker can serve a collection
     * of absolute paths with different beginnings that contain the same Torq packages. Consider these two absolute
     * paths that contain `mypackage.Foo.torq` and `mypackage.Bar.torq` served from two different root paths,
     * `/Users/USER/foo_source` and `/Users/USER/bar_source`, respectively.
     *     1. /Users/USER/foo_source/mypackage/Foo.torq
     *     2. /Users/USER/bar_source/mypackage/Bar.torq
     */
    List<List<FileName>> roots();

    String source(List<FileName> path) throws IOException;

    /*
     * Trim the root from the given absolute path.
     */
    default List<FileName> trimRoot(List<FileName> absolutePath) {
        for (List<FileName> root : roots()) {
            if (hasRoot(absolutePath, root)) {
                return absolutePath.subList(root.size(), absolutePath.size());
            }
        }
        throw new IllegalArgumentException("Path does not contain a root");
    }

}
