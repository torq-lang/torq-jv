/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.klvm;

public interface UnificationPriority {
    int JAVA_OBJECT = 500;
    int COMPLETE_TUPLE = 400;
    int COMPLETE_REC = 300;
    int PARTIAL_TUPLE = 200;
    int PARTIAL_REC = 100;
}
