/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.local;

import org.torqlang.util.SourceFileBroker;

import java.util.List;
import java.util.function.Consumer;

public interface TorqCompilerReady {
    TorqCompilerReady setMessageListener(Consumer<String> messageListener);

    TorqCompilerReady setWorkspace(List<SourceFileBroker> fileBrokers);

    TorqCompilerParsed parse() throws Exception;
}
