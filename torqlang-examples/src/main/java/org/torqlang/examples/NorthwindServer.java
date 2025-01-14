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
import org.torqlang.util.GetArg;

import java.util.Arrays;
import java.util.List;

import static org.torqlang.server.ServerProps.RESOURCES_PROP;

/*
 * Example data:
 *     Example data must be copied from the project directory `resources/northwind/` to the local home
 *     directory `/home/USER/.torq_lang/resources/northwind`.
 * Run with all hardware threads:
 *     java -XX:+UseZGC -p ~/workspace/torq_jv_runtime -m org.torqlang.examples/org.torqlang.examples.NorthwindServer
 * Run with 8 hardware threads:
 *     taskset -c 0-7 java -XX:+UseZGC -p ~/workspace/torq_jv_runtime -m org.torqlang.examples/org.torqlang.examples.NorthwindServer
 */
public final class NorthwindServer {

    public static void main(String[] args) throws Exception {

        ConsoleLogger.SINGLETON.info("Process ID: " + ProcessHandle.current().pid());
        ConsoleLogger.SINGLETON.info("System executor: " + ActorSystem.defaultExecutor());
        ConsoleLogger.SINGLETON.info("NorthwindDb executor: " + NorthwindDbPack.NORTHWIND_DB_EXECUTOR);

        List<String> argsList = Arrays.asList(args);

        String resourcesRoot = GetArg.getSingleFromEither(ServerProps.RESOURCES_SHORT_OPTION,
            ServerProps.RESOURCES_LONG_OPTION, argsList);
        if (resourcesRoot == null) {
            resourcesRoot = System.getProperty("user.home") + "/.torq_lang/resources";
        }
        ServerProps.put(RESOURCES_PROP, resourcesRoot);
        ConsoleLogger.SINGLETON.info("Resources root " + RESOURCES_PROP + ": " + resourcesRoot);

        LocalServer server = LocalServer.builder()
            .setPort(8080)
            .addContextHandler(new EchoHandler(), "/echo")
            .addContextHandler(NorthwindHandlerFactory.createHandler(), "/api")
            .build();
        server.start();
        server.join();
    }

}
