/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

import java.util.Comparator;

/*
 * The sort order by type is: Str, Int, Bool, Eof, Null, Token. Strings are sorted in lexicographic order, integers are
 * in ascending order, booleans are in false, true order, and tokens are in ID order.
 */
public final class FeatureComparator implements Comparator<Feature> {

    public static final FeatureComparator SINGLETON = new FeatureComparator();

    private FeatureComparator() {
    }

    @Override
    public final int compare(Feature f1, Feature f2) {
        if (f1 instanceof Str f1s) {
            // f1 ? f2 when f2 is Str
            // f1 < f2 when f2 is not Str
            return f2 instanceof Str f2s ? f1s.value.compareTo(f2s.value) : -1;
        }
        if (f2 instanceof Str) {
            // f1 > f2 when f1 is not Str
            return 1;
        }
        // We know the remaining combinations exclude Str
        if (f1 instanceof Int64 f1i) {
            // f1 ? f2 when f2 is Int
            // f1 < f2 when f2 is not Str or Int
            return f2 instanceof Int64 f2i ? Long.compare(f1i.longValue(), f2i.longValue()) : -1;
        }
        if (f2 instanceof Int64) {
            // f1 > f2 when f1 is not Str or Int
            return 1;
        }
        // We know the remaining combinations exclude Str and Int
        if (f1 instanceof Bool f1b) {
            // f1 ? f2 when f2 is Bool
            // f1 < f2 when f2 is not Str, Int, or Bool
            return f2 instanceof Bool f2b ? Boolean.compare(f1b.value, f2b.value) : -1;
        }
        if (f2 instanceof Bool) {
            // f1 > f2 when f1 is not Str, Int, or Bool
            return 1;
        }
        // We know the remaining combinations exclude Str, Int, and Bool
        if (f1 == Eof.SINGLETON) {
            // f1 = f2 when f2 is Eof
            // f1 < f2 when f2 is not Str, Int, Bool, or Eof
            return f2 == Eof.SINGLETON ? 0 : -1;
        }
        if (f2 instanceof Eof) {
            // f1 > f2 when f1 is not Str, Int, Bool, or Eof
            return 1;
        }
        // We know the remaining combinations exclude Str, Int, Bool, and Eof
        if (f1 == Null.SINGLETON) {
            // f1 = f2 when f2 is Null
            // f1 < f2 when f2 is not Str, Int, Bool, Eof, and Null
            return f2 == Null.SINGLETON ? 0 : -1;
        }
        if (f2 == Null.SINGLETON) {
            // f1 > f2 when f1 is not Str, Int, Bool, Eof, and Null
            return 1;
        }
        // Compare tokens
        Token t1 = (Token) f1;
        Token t2 = (Token) f2;
        return Long.compare(t1.id, t2.id);
    }

}
