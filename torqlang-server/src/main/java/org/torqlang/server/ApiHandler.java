/*
 * Copyright (c) 2024 Torqware LLC. All rights reserved.
 *
 * You should have received a copy of the Torq Lang License v1.0 along with this program.
 * If not, see <http://torq-lang.github.io/licensing/torq-lang-license-v1_0>.
 */

package org.torqlang.server;

import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.io.Content;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;
import org.torqlang.klvm.*;
import org.torqlang.local.*;
import org.torqlang.server.ApiReceiver.ApiReceiverImage;
import org.torqlang.server.ApiReceiver.ApiReceiverRef;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public final class ApiHandler extends Handler.Abstract.NonBlocking {

    private static final String APPLICATION_JSON_CHARSET_UTF_8 = "application/json; charset=utf-8";
    private static final String TEXT_PLAIN_CHARSET_UTF_8 = "text/plain; charset=utf-8";

    private static final String RESPONSE_ADDRESS_PREFIX = "ApiHandler.ResponseAddress";

    private final ActorSystem system;
    private final ApiRouter router;

    public ApiHandler(ActorSystem system, ApiRouter router) {
        this.system = system;
        this.router = router;
    }

    public static ApiHandlerBuilder builder() {
        return new ApiHandlerBuilder();
    }

    @Override
    public final boolean handle(final Request request, final Response response, final Callback callback) {
        // This method simply sends a request message. However, if a text body can be present, we must first
        // retrieve the text asynchronously.
        final String method = request.getMethod();
        final String pathInContext = URLDecoder.decode(Request.getPathInContext(request), StandardCharsets.UTF_8);
        final ApiPath path = new ApiPath(pathInContext);
        final ApiRoute route = router.findRoute(path);
        if (route == null) {
            Response.writeError(request, response, callback, HttpStatus.NOT_FOUND_404);
            return true;
        }
        final CompleteTuple pathTuple = route.desc.toPathTuple(path);
        final CompleteRec headersRec = route.desc.toHeadersRec(request.getHeaders());
        final CompleteRec queryRec = route.desc.toQueryRec(Request.extractQueryParameters(request));
        final CompleteRec contextRec = route.desc.toContextRec(request);
        if (method.equals(HttpMethod.GET.name())) {
            sendRequestMessage(request, response, callback, route, headersRec, method, pathTuple, queryRec,
                contextRec, null);
        } else {
            Content.Source.asStringAsync(request, StandardCharsets.UTF_8)
                .thenAccept((requestText -> sendRequestMessage(request, response, callback, route, headersRec, method, pathTuple,
                    queryRec, contextRec, requestText)));
        }
        return true;
    }

    public final ApiRouter router() {
        return router;
    }

    private void sendRequestMessage(Request request, Response response, Callback callback, ApiRoute route,
                                    CompleteRec headersRec, String method, CompleteTuple pathTuple,
                                    CompleteRec queryRec, CompleteRec contextRec, String requestText)
    {
        try {
            ActorRef actorRef;
            if (route.receiver instanceof ApiReceiverImage targetActorImage) {
                actorRef = Actor.spawn(Address.create("api-handler"), targetActorImage.value());
            } else {
                actorRef = ((ApiReceiverRef) route.receiver).actorRef;
            }
            CompleteRec requestRec = route.desc.toRequestRec(method, pathTuple, headersRec, queryRec,
                contextRec, requestText);
            ActorRef responseAdapter = new ResponseAdapter(request, response, callback, route);
            actorRef.send(Envelope.createRequest(requestRec, responseAdapter, Null.SINGLETON));
        } catch (Exception exc) {
            Response.writeError(request, response, callback, exc);
        }
    }

    public final ActorSystem system() {
        return system;
    }

    private static class ResponseAdapter implements ActorRef {
        private final Address address;
        private final Request request;
        private final Response response;
        private final Callback callback;
        private final ApiRoute route;

        private ResponseAdapter(Request request, Response response, Callback callback, ApiRoute route) {
            address = Address.create(RESPONSE_ADDRESS_PREFIX + "." + request.getId());
            this.request = request;
            this.response = response;
            this.callback = callback;
            this.route = route;
        }

        @Override
        public Address address() {
            return address;
        }

        @Override
        public void send(Envelope envelope) {
            try {
                Complete message = (Complete) envelope.message();
                if (!envelope.isResponse()) {
                    response.setStatus(500);
                    response.getHeaders().put(HttpHeader.CONTENT_TYPE, TEXT_PLAIN_CHARSET_UTF_8);
                    Content.Sink.write(response, true, "Not a response: " + envelope, callback);
                } else if (message instanceof FailedValue failedValue) {
                    response.setStatus(500);
                    response.getHeaders().put(HttpHeader.CONTENT_TYPE, TEXT_PLAIN_CHARSET_UTF_8);
                    Content.Sink.write(response, true, failedValue.toDetailsString(), callback);
                } else {
                    String bodyText = route.desc.toResponseBodyText(message);
                    response.setStatus(200);
                    response.getHeaders().put(HttpHeader.CONTENT_TYPE, APPLICATION_JSON_CHARSET_UTF_8);
                    Content.Sink.write(response, true, bodyText, callback);
                }
            } catch (Exception exc) {
                Response.writeError(request, response, callback, exc);
            }
        }
    }

}
