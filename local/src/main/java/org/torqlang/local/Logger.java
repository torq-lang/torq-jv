/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

public interface Logger {

    static Logger createDefault() {
        return ConsoleLogger.SINGLETON;
    }

    void info(String message);

    void info(String caller, String message);

    void error(String message);

    void error(String caller, String message);

    void warn(String message);

    void warn(String caller, String message);

}
