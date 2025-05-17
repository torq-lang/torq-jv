/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import org.torqlang.klvm.CompleteRec;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

public final class ActorSystemBuilder {

    private final Map<Address, ActorRefObj> actorsByAddress = new HashMap<>();
    private final Map<String, CompleteRec> packagesByPath = new HashMap<>();
    private String name;
    private Executor executor;

    public ActorSystemBuilder addActor(String path, ActorRefObj actorRefObj) {
        LocalAddress address = LocalAddress.create(path);
        if (actorsByAddress.containsKey(address)) {
            throw new IllegalArgumentException("Actor already exists:" + path);
        }
        actorsByAddress.put(address, actorRefObj);
        return this;
    }

    public final ActorSystemBuilder addDefaultPackages() {
        packagesByPath.putAll(DefaultPackages.singleton().packagesByPath());
        return this;
    }

    public ActorSystemBuilder addPackage(String path, CompleteRec packageRec) {
        if (packagesByPath.containsKey(path)) {
            throw new IllegalArgumentException("Package already exists:" + path);
        }
        packagesByPath.put(path, packageRec);
        return this;
    }

    public final ActorSystem build() {
        Map<String, CompleteRec> effectivePackagesByPath;
        if (packagesByPath.isEmpty()) {
            effectivePackagesByPath = DefaultPackages.singleton().packagesByPath();
        } else {
            effectivePackagesByPath = packagesByPath;
        }
        return new BasicActorSystem(name, executor, actorsByAddress, effectivePackagesByPath);
    }

    public final Executor executor() {
        return executor;
    }

    public final String name() {
        return name;
    }

    public final Map<String, CompleteRec> packagesByPath() {
        return Map.copyOf(packagesByPath);
    }

    public final ActorSystemBuilder setExecutor(Executor executor) {
        this.executor = executor;
        return this;
    }

    public final ActorSystemBuilder setName(String name) {
        this.name = name;
        return this;
    }

}
