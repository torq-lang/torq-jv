/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.server;

import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.util.Fields;
import org.torqlang.klvm.Complete;
import org.torqlang.klvm.CompleteRec;
import org.torqlang.klvm.CompleteTuple;
import org.torqlang.local.RecDesc;
import org.torqlang.local.TupleDesc;
import org.torqlang.local.ValueDesc;

/*
 * An API description bundles several value descriptions with behavior for transforming HTTP text to and from kernel
 * values.
 */
public interface ApiDesc {

    static ApiDescBuilder builder() {
        return new ApiDescBuilder();
    }

    ValueDesc inputDesc();

    ValueDesc outputDesc();

    TupleDesc pathDesc();

    RecDesc queryDesc();

    CompleteRec toContextRec(Request request);

    CompleteRec toHeadersRec(HttpFields headerFields);

    CompleteTuple toPathTuple(ApiPath path);

    CompleteRec toQueryRec(Fields queryFields);

    CompleteRec toRequestRec(String method, CompleteTuple pathTuple, CompleteRec headersRec, CompleteRec queryRec,
                             CompleteRec contextRec, String requestText);

    String toResponseBodyText(Complete responseRec);

}
