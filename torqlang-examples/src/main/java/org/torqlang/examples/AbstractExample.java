/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.examples;

import org.torqlang.klvm.FailedValue;

import java.util.Objects;

import static org.torqlang.util.ErrorWithSourceSpan.printWithSourceAndRethrow;

public abstract class AbstractExample {

    /*
     * Argument order (expected, response) is the same order used in JUnit assert methods.
     */
    public static void checkExpectedResponse(Object expected, Object response) {
        if (!Objects.equals(response, expected)) {
            String error = "Invalid response: " + response;
            if (response instanceof FailedValue failedValue) {
                error += "\n" + failedValue.toDetailsString();
            }
            throw new IllegalStateException(error);
        }
    }

    public static void checkNotFailedValue(Object response) {
        if (response instanceof FailedValue failedValue) {
            String error = "Invalid response: " + response + "\n" + failedValue.toDetailsString();
            throw new IllegalStateException(error);
        }
    }

    public abstract void perform() throws Exception;

    public final void performWithErrorCheck() throws Exception {
        try {
            perform();
        } catch (Exception exc) {
            printWithSourceAndRethrow(exc, 5, 5, 5);
        }
    }

}
