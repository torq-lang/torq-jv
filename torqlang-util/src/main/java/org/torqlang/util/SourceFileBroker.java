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

public interface SourceFileBroker {

    /*
     * Append fileName to the end of path.
     */
    static List<FileName> append(List<FileName> path, FileName fileName) {
        if (!path.isEmpty() && !ListTools.last(path).type().equals(FileType.FOLDER)) {
            throw new IllegalArgumentException("Not a path");
        }
        ArrayList<FileName> answer = new ArrayList<>(path);
        answer.add(fileName);
        return List.of(answer.toArray(new FileName[0]));
    }

    /*
     * Throw an IllegalArgumentException if the list of roots contains duplicates.
     */
    static List<List<FileName>> checkForDuplicates(List<List<FileName>> roots) {
        for (int i = 0; i < roots.size(); i++) {
            List<FileName> left = roots.get(i);
            for (int j = i+1; j < roots.size(); j++) {
                List<FileName> right = roots.get(j);
                if (left.size() == right.size()) {
                    boolean same = true;
                    for (int k = 0; k < left.size(); k++) {
                        if (!left.get(k).value().equals(right.get(k).value())) {
                            same = false;
                            break;
                        }
                    }
                    if (same) {
                        throw new IllegalArgumentException("Duplicate root path");
                    }
                }
            }
        }
        return roots;
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
     * List all names in the given absolute path. Return null if the absolute path is not found.
     */
    List<FileName> list(List<FileName> absolutePath);

    /*
     * A file broker serves source from one or more root folders where Torq packages begin. Given the root directory
     * `/Users/USER/project/torqsrc` and the absolute path `/Users/USER/project/torqsrc/my/package/Bar.torq`, we get
     * the qualified Torq name `my.package.Bar.torq` having the package `my.package` and simple name `Bar.torq`.
     */
    List<List<FileName>> roots();

    SourceFile source(List<FileName> path) throws IOException;

    /*
     * Trim the root from the given absolute path. Return null if the absolute path does not contain a root.
     */
    default List<FileName> trimRoot(List<FileName> absolutePath) {
        for (List<FileName> root : roots()) {
            if (hasRoot(absolutePath, root)) {
                return absolutePath.subList(root.size(), absolutePath.size());
            }
        }
        return null;
    }

}
