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
    private final Map<String, CompleteRec> packagesByQualifier = new HashMap<>();
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
        packagesByQualifier.putAll(DefaultPackages.singleton().packagesByQualifier());
        return this;
    }

    public ActorSystemBuilder addPackage(String path, CompleteRec packageRec) {
        if (packagesByQualifier.containsKey(path)) {
            throw new IllegalArgumentException("Package already exists:" + path);
        }
        packagesByQualifier.put(path, packageRec);
        return this;
    }

    public final ActorSystem build() {
        Map<String, CompleteRec> effectivePackagesByQualifier;
        if (packagesByQualifier.isEmpty()) {
            effectivePackagesByQualifier = DefaultPackages.singleton().packagesByQualifier();
        } else {
            effectivePackagesByQualifier = packagesByQualifier;
        }
        return new BasicActorSystem(name, executor, actorsByAddress, effectivePackagesByQualifier);
    }

    public final Executor executor() {
        return executor;
    }

    public final String name() {
        return name;
    }

    public final Map<String, CompleteRec> packagesByQualifier() {
        return Map.copyOf(packagesByQualifier);
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
