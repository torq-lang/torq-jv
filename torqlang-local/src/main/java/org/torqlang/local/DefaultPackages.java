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

public final class DefaultPackages {

    private final Map<String, CompleteRec> packagesByQualifier;

    private DefaultPackages() {
        Map<String, CompleteRec> workingMap = new HashMap<>();
        workingMap.put(SystemLangPackage.singleton().qualifier(), SystemLangPackage.singleton().members());
        workingMap.put(SystemUtilPackage.singleton().qualifier(), SystemUtilPackage.singleton().members());
        this.packagesByQualifier = Map.copyOf(workingMap);
    }

    public static DefaultPackages singleton() {
        return LazySingleton.SINGLETON;
    }

    public final Map<String, CompleteRec> packagesByQualifier() {
        return packagesByQualifier;
    }

    private static final class LazySingleton {
        private static final DefaultPackages SINGLETON = new DefaultPackages();
    }

}
