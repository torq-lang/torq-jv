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
import org.torqlang.lang.*;
import org.torqlang.local.ValueTools;

final class DefaultApiDesc implements ApiDesc {

    private final Type inputType;
    private final Type outputType;
    private final TupleType pathType;
    private final RecType queryType;
    private final ContextProvider contextProvider;

    DefaultApiDesc(TupleType pathType,
                   RecType queryType,
                   Type inputType,
                   Type outputDesc,
                   ContextProvider contextProvider)
    {
        this.pathType = pathType;
        this.queryType = queryType;
        this.inputType = inputType;
        this.outputType = outputDesc;
        this.contextProvider = contextProvider;
    }

    @Override
    public final Type inputType() {
        return inputType;
    }

    @Override
    public final Type outputType() {
        return outputType;
    }

    @Override
    public final TupleType pathType() {
        return pathType;
    }

    @Override
    public final RecType queryType() {
        return queryType;
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
        if (pathType == null) {
            throw new IllegalArgumentException("Path description is null");
        }
        TupleTypeExpr pathTypeExpr = null;
        if (pathType instanceof TupleTypeExpr pathTypeExprFound) {
            pathTypeExpr = pathTypeExprFound;
            if (path.segs.size() != pathTypeExpr.values.size()) {
                throw new IllegalArgumentException("Path size does not match API description");
            }
        }
        CompleteTupleBuilder tupleBuilder = Rec.completeTupleBuilder();
        for (int i = 0; i < path.segs.size(); i++) {
            String seg = path.segs.get(i);
            Type segType = pathTypeExpr != null ? pathTypeExpr.values.get(i) : null;
            tupleBuilder.addValue(
                ValueTools.toKernelValue(seg, segType)
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
            String queryValue = f.getValue();
            if (queryValue.charAt(0) == '"') {
                queryValue = queryValue.substring(1, queryValue.length() - 1);
                queryValue = queryValue.replace("\\\"", "\"");
            }
            Complete value;
            if (queryType instanceof RecTypeExpr queryTypeExpr) {
                // TODO: Support multiple values per query field. The queryType will map to an ArrayType when multiple
                //       values are allowed.
                FeatureAsType featureAsType = FeatureAsType.create(feature);
                Type queryValueType = queryTypeExpr.findValue(featureAsType);
                if (queryValueType != null) {
                    value = ValueTools.toKernelValue(queryValue, queryValueType);
                } else {
                    value = Str.of(queryValue);
                }
            } else {
                value = Str.of(queryValue);
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
                bodyValue = ValueTools.toKernelValue(new JsonParser(requestText).parse(), inputType);
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
        return JsonFormatter.DEFAULT.format(nativeResponseValue);
    }

}
