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
import org.torqlang.lang.RecType;
import org.torqlang.lang.TupleType;
import org.torqlang.lang.Type;

/*
 * An API description bundles together types and methods for transforming HTTP text to and from kernel values.
 */
public interface ApiDesc {

    static ApiDescBuilder builder() {
        return new ApiDescBuilder();
    }

    Type inputType();

    Type outputType();

    TupleType pathType();

    RecType queryType();

    CompleteRec toContextRec(Request request);

    CompleteRec toHeadersRec(HttpFields headerFields);

    CompleteTuple toPathTuple(ApiPath path);

    CompleteRec toQueryRec(Fields queryFields);

    CompleteRec toRequestRec(String method, CompleteTuple pathTuple, CompleteRec headersRec, CompleteRec queryRec,
                             CompleteRec contextRec, String requestText);

    String toResponseBodyText(Complete responseRec);

}
