/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.server;

import org.torqlang.lang.RecType;
import org.torqlang.lang.TupleType;
import org.torqlang.lang.Type;

public final class ApiDescBuilder {

    private Type inputType;
    private Type outputType;
    private TupleType pathType;
    private RecType queryType;

    private ContextProvider contextProvider;

    public final ApiDescBuilder setContextProvider(ContextProvider contextProvider) {
        this.contextProvider = contextProvider;
        return this;
    }

    public final ApiDescBuilder setInputType(Type inputType) {
        this.inputType = inputType;
        return this;
    }

    public final ApiDescBuilder setOutputType(Type outputType) {
        this.outputType = outputType;
        return this;
    }

    public final ApiDescBuilder setPathType(TupleType pathType) {
        this.pathType = pathType;
        return this;
    }

    public final ApiDescBuilder setQueryType(RecType queryType) {
        this.queryType = queryType;
        return this;
    }

    public ApiDesc build() {
        return new DefaultApiDesc(pathType, queryType, inputType, outputType, contextProvider);
    }

}
