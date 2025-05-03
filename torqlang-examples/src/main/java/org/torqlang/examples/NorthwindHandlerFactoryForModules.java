/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.examples;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.torqlang.klvm.CompleteRec;
import org.torqlang.klvm.Rec;
import org.torqlang.local.*;
import org.torqlang.util.NeedsImpl;

import java.util.List;

public final class NorthwindHandlerFactoryForModules {

    private static CompleteRec emptyContextProvider(Request request) {
        return Rec.completeRecBuilder().build();
    }

    public static Handler createApiHandler() throws Exception {

        TorqCompilerBundled bundled = TorqCompiler.create()
            .setMessageListener(ConsoleLogger.SINGLETON::info)
            .setWorkspace(List.of(SystemSourceBroker.create(), ExamplesSourceBroker.createResourcesBrokerForModules()))
            .parse()
            .collect()
            .compile()
            .bundle();

        throw new NeedsImpl("Needs API Router");
    }

}
