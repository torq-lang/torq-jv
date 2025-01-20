/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

// TODO: Rename to DebuggerSetting

public final class DebuggerSetting {

    private static Debugger debugger;

    public static Debugger debugger() {
        return debugger;
    }

    public static void setDebugger(Debugger debugger) {
        if (DebuggerSetting.debugger != null) {
            throw new IllegalStateException("debugger already set");
        }
        String warningBanner = """
            Debugger is active
            ============== WARNING ==============
            This server is in debug mode.
            Do NOT use this server in production.
            =====================================""";
        ConsoleLogger.SINGLETON.warn(warningBanner);
        DebuggerSetting.debugger = debugger;
    }

}
