/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.examples;

import org.torqlang.local.ActorSystem;
import org.torqlang.local.ConsoleLogger;
import org.torqlang.server.EchoHandler;
import org.torqlang.server.LocalServer;
import org.torqlang.server.ServerProps;
import org.torqlang.util.ErrorWithSourceSpan;
import org.torqlang.util.GetArg;
import org.torqlang.util.GetStackTrace;

import java.util.Arrays;
import java.util.List;

import static org.torqlang.server.ServerProps.RESOURCES_PROP;

/*
 * Example data:
 *     Northwind example data must be copied from the project directory
 *     `src/main/resources/org/torqlang/examples/data/northwind/` to the local home directory
 *     `/home/USER/.torq/resources/org/torqlang/examples/data/northwind/`.
 *
 * Run with all hardware threads:
 *     java -XX:+UseZGC -p ~/.torq/lib -m org.torqlang.examples/org.torqlang.examples.NorthwindServer
 *
 * Run with 8 hardware threads:
 *     taskset -c 0-7 <<nest-previous-java-expression-here>>
 *
 * Run in debug mode by adding the following "-agentlib" option to the previous java expression:
 *     -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005
 */
public final class NorthwindServer {

    public static void main(String[] args) throws Exception {
        try {
            start(args);
        } catch (Exception e) {
            String message;
            if (e instanceof ErrorWithSourceSpan errorWithSourceSpan) {
                message = "Failed to parse source\n" + errorWithSourceSpan.formatWithSource(5, 50, 50);
            } else {
                message = "Failed to start server\n" + GetStackTrace.apply(e, true);
            }
            System.err.println(message);
            System.exit(-1);
        }
    }

    private static void start(String[] args) throws Exception {

//        DebuggerSetting.set(new DefaultDebugger());
//        DebuggerSetting.get().addRecognizer((actorRef -> actorRef.address().path().equals("customers")));

        ConsoleLogger.SINGLETON.info("Process ID: " + ProcessHandle.current().pid());
        ConsoleLogger.SINGLETON.info("System executor: " + ActorSystem.defaultExecutor());
        ConsoleLogger.SINGLETON.info("NorthwindDb executor: " + NorthwindDbMod.NORTHWIND_DB_EXECUTOR);

        List<String> argsList = Arrays.asList(args);

        String resourcesRoot = GetArg.getSingleFromEither(ServerProps.RESOURCES_SHORT_OPTION,
            ServerProps.RESOURCES_LONG_OPTION, argsList);
        if (resourcesRoot == null) {
            resourcesRoot = System.getProperty("user.home") + "/.torq/resources";
        }
        ServerProps.put(RESOURCES_PROP, resourcesRoot);
        ConsoleLogger.SINGLETON.info("Resources root " + RESOURCES_PROP + ": " + resourcesRoot);

        LocalServer server = LocalServer.builder()
            .setPort(8080)
            .addContextHandler(new EchoHandler(), "/echo")
            .addContextHandler(NorthwindHandlerFactoryForModules.createApiHandler(), "/api")
            .build();
        server.start();
        server.join();
    }

}
