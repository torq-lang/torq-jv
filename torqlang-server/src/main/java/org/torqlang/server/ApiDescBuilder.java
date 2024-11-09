/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.server;

import org.torqlang.local.RecDesc;
import org.torqlang.local.TupleDesc;
import org.torqlang.local.ValueDesc;

public final class ApiDescBuilder {

    private TupleDesc pathDesc;
    private RecDesc queryDesc;
    private ValueDesc inputDesc;
    private ValueDesc outputDesc;
    private ContextProvider contextProvider;

    public final ApiDescBuilder setContextProvider(ContextProvider contextProvider) {
        this.contextProvider = contextProvider;
        return this;
    }

    public final ApiDescBuilder setInputDesc(ValueDesc inputDesc) {
        this.inputDesc = inputDesc;
        return this;
    }

    public final ApiDescBuilder setOutputDesc(ValueDesc outputDesc) {
        this.outputDesc = outputDesc;
        return this;
    }

    public final ApiDescBuilder setPathDesc(TupleDesc pathDesc) {
        this.pathDesc = pathDesc;
        return this;
    }

    public final ApiDescBuilder setQueryDesc(RecDesc queryDesc) {
        this.queryDesc = queryDesc;
        return this;
    }

    public ApiDesc build() {
        return new DefaultApiDesc(pathDesc, queryDesc, inputDesc, outputDesc, contextProvider);
    }

}
