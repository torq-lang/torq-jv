/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.util;

public abstract class ErrorWithSourceSpan extends RuntimeException implements SourceSpanProvider {

    public ErrorWithSourceSpan() {
        super();
    }

    public ErrorWithSourceSpan(String message) {
        super(message);
    }

    public ErrorWithSourceSpan(String message, Throwable cause) {
        super(message, cause);
    }

    public ErrorWithSourceSpan(Throwable cause) {
        super(cause);
    }

    public final String formatWithSource(int lineNrWidth, int showBefore, int showAfter) {
        return sourceSpan() != null ?
            sourceSpan().formatSource(getMessage(), lineNrWidth, showBefore, showAfter) :
            this.toString();
    }

    public final void printError() {
        System.err.println("==== BEGIN ====");
        System.err.println(formatWithSource(4, 5, 5));
        System.err.println("==== END ====");
    }

}
