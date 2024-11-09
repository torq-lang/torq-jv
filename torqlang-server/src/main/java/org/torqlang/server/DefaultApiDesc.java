/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.server;

import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.util.Fields;
import org.torqlang.klvm.*;
import org.torqlang.lang.JsonFormatter;
import org.torqlang.lang.JsonParser;
import org.torqlang.local.RecDesc;
import org.torqlang.local.TupleDesc;
import org.torqlang.local.ValueDesc;
import org.torqlang.local.ValueTools;

final class DefaultApiDesc implements ApiDesc {

    private final ValueDesc inputDesc;
    private final ValueDesc outputDesc;
    private final TupleDesc pathDesc;
    private final RecDesc queryDesc;
    private final ContextProvider contextProvider;

    DefaultApiDesc(TupleDesc pathDesc,
                   RecDesc queryDesc,
                   ValueDesc inputDesc,
                   ValueDesc outputDesc,
                   ContextProvider contextProvider)
    {
        this.pathDesc = pathDesc;
        this.queryDesc = queryDesc;
        this.inputDesc = inputDesc;
        this.outputDesc = outputDesc;
        this.contextProvider = contextProvider;
    }

    @Override
    public final ValueDesc inputDesc() {
        return inputDesc;
    }

    @Override
    public final ValueDesc outputDesc() {
        return outputDesc;
    }

    @Override
    public final TupleDesc pathDesc() {
        return pathDesc;
    }

    @Override
    public final RecDesc queryDesc() {
        return queryDesc;
    }

    @Override
    public final CompleteRec toContextRec(Request request) {
        return contextProvider.apply(request);
    }

    @Override
    public final CompleteRec toHeadersRec(HttpFields headerFields) {
        CompleteRecBuilder headersRecBuilder = Rec.completeRecBuilder();
        for (HttpField f : headerFields) {
            headersRecBuilder.addField(Str.of(f.getName()), Str.of(f.getValue()));
        }
        return headersRecBuilder.build();
    }

    @Override
    public final CompleteTuple toPathTuple(ApiPath path) {
        if (pathDesc == null) {
            throw new IllegalArgumentException("Path description is null");
        }
        if (path.segs.size() != pathDesc.descs().size()) {
            throw new IllegalArgumentException("Path size does not equal path description size");
        }
        CompleteTupleBuilder tupleBuilder = Rec.completeTupleBuilder();
        for (int i = 0; i < path.segs.size(); i++) {
            String seg = path.segs.get(i);
            tupleBuilder.addValue(
                ValueTools.toKernelValue(seg, pathDesc.descs().get(i))
            );
        }
        return tupleBuilder.build();
    }

    @Override
    public final CompleteRec toQueryRec(Fields queryFields) {
        // Jetty will decode query parameters to UTF-8
        CompleteRecBuilder queryRecBuilder = Rec.completeRecBuilder();
        for (Fields.Field f : queryFields) {
            Feature feature = Str.of(f.getName());
            String unquotedValue = f.getValue();
            if (unquotedValue.charAt(0) == '"') {
                unquotedValue = unquotedValue.substring(1, unquotedValue.length() - 1);
                unquotedValue = unquotedValue.replace("\\\"", "\"");
            }
            Complete value;
            if (queryDesc != null) {
                // TODO: Support multiple values per query field.
                //       The queryDesc would map to an ArrayDesc if multiple values are supported.
                ValueDesc inputValueDesc = queryDesc.map.get(feature);
                if (inputValueDesc != null) {
                    value = ValueTools.toKernelValue(unquotedValue, inputValueDesc);
                } else {
                    value = Str.of(unquotedValue);
                }
            } else {
                value = Str.of(unquotedValue);
            }
            queryRecBuilder.addField(feature, value);
        }
        return queryRecBuilder.build();
    }

    @Override
    public final CompleteRec toRequestRec(String method, CompleteTuple pathTuple, CompleteRec headersRec,
                                          CompleteRec queryRec, CompleteRec contextRec, String requestText)
    {
        CompleteRecBuilder requestRecBuilder = Rec.completeRecBuilder()
            .setLabel(Str.of(method))
            .addField(Str.of("headers"), headersRec)
            .addField(Str.of("path"), pathTuple)
            .addField(Str.of("query"), queryRec);
        if (requestText != null) {
            Complete bodyValue;
            if (requestText.isBlank()) {
                bodyValue = Null.SINGLETON;
            } else {
                // TODO: Optimize with a text-to-kernel instead of text-to-native-to-kernel
                bodyValue = ValueTools.toKernelValue(new JsonParser(requestText).parse(), inputDesc);
            }
            requestRecBuilder.addField(Str.of("body"), bodyValue);
        }
        requestRecBuilder.addField(Str.of("context"), contextRec);
        return requestRecBuilder.build();
    }

    @Override
    public String toResponseBodyText(Complete response) {
        // TODO: Optimize with a kernel-to-text instead of kernel-to-native-to-text
        Object nativeResponseValue = ValueTools.toNativeValue(response);
        return JsonFormatter.SINGLETON.format(nativeResponseValue);
    }

}
