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
import org.torqlang.local.SystemSourceBroker;
import org.torqlang.local.TorqCompiler;
import org.torqlang.local.TorqCompilerBundled;
import org.torqlang.local.TorqCompilerError;
import org.torqlang.util.Message;
import org.torqlang.util.MessageType;
import org.torqlang.util.NeedsImpl;

import java.util.List;

/*
 * TODO:
 *     -- add meta tags that define or refer to rate limiters
 *     -- create a rate limiter pool by name, attach rate limiters to api routes
 */
public final class NorthwindHandlerFactory {

    private static CompleteRec emptyContextProvider(Request request) {
        return Rec.completeRecBuilder().build();
    }

    public static Handler createApiHandler() throws Exception {
        TorqCompilerBundled bundled = compileWorkspace();
        for (Message m : bundled.messages()) {
            System.out.println(m.message());
        }
        throw new NeedsImpl("Needs API Router");
    }

    private static TorqCompilerBundled compileWorkspace() throws Exception {
        try {
            return TorqCompiler.create()
                .setLoggingLevel(MessageType.TRACE)
                .setWorkspace(List.of(SystemSourceBroker.create(), ExamplesSourceBroker.createResourcesBrokerForModules()))
                .parse()
                .collect()
                .generate()
                .bundle();
        } catch (TorqCompilerError error) {
            for (Message m : error.details()) {
                System.out.println(m.message());
            }
            throw error;
        }
    }

}
