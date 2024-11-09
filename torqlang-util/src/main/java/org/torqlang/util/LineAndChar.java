/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.util;

@SuppressWarnings("ClassCanBeRecord")
public final class LineAndChar {

    public final int lineNr;
    public final int charNr;

    public LineAndChar(int lineNr, int charNr) {
        this.lineNr = lineNr;
        this.charNr = charNr;
    }

    public final String toString() {
        return "[" + lineNr + ":" + charNr + "]";
    }

}
