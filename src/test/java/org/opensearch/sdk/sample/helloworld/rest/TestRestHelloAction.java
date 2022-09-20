/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */
package org.opensearch.sdk.sample.helloworld.rest;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensearch.rest.RestHandler.Route;
import org.opensearch.rest.RestRequest.Method;
import org.opensearch.common.bytes.BytesReference;
import org.opensearch.rest.BytesRestResponse;
import org.opensearch.rest.RestResponse;
import org.opensearch.rest.RestStatus;
import org.opensearch.sdk.ExtensionRestHandler;
import org.opensearch.test.OpenSearchTestCase;

public class TestRestHelloAction extends OpenSearchTestCase {

    private ExtensionRestHandler restHelloAction;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        restHelloAction = new RestHelloAction();
    }

    @Test
    public void testRoutes() {
        List<Route> routes = restHelloAction.routes();
        assertEquals(2, routes.size());
        assertEquals(Method.GET, routes.get(0).getMethod());
        assertEquals("/hello", routes.get(0).getPath());
        assertEquals(Method.PUT, routes.get(1).getMethod());
        assertEquals("/hello/{name}", routes.get(1).getPath());
    }

    @Test
    public void testHandleRequest() {
        RestResponse response = restHelloAction.handleRequest(Method.GET, "/hello");
        assertEquals(RestStatus.OK, response.status());
        assertEquals(BytesRestResponse.TEXT_CONTENT_TYPE, response.contentType());
        String responseStr = new String(BytesReference.toBytes(response.content()), StandardCharsets.UTF_8);
        assertEquals("Hello, World!", responseStr);

        response = restHelloAction.handleRequest(Method.PUT, "/hello");
        assertEquals(RestStatus.NOT_FOUND, response.status());
        assertEquals(BytesRestResponse.TEXT_CONTENT_TYPE, response.contentType());
        responseStr = new String(BytesReference.toBytes(response.content()), StandardCharsets.UTF_8);
        assertTrue(responseStr.contains("PUT"));

        response = restHelloAction.handleRequest(Method.PUT, "/hello/Passing+Test");
        assertEquals(RestStatus.OK, response.status());
        assertEquals(BytesRestResponse.TEXT_CONTENT_TYPE, response.contentType());
        responseStr = new String(BytesReference.toBytes(response.content()), StandardCharsets.UTF_8);
        assertEquals("Updated the world's name to Passing Test", responseStr);

        response = restHelloAction.handleRequest(Method.GET, "/hello");
        assertEquals(RestStatus.OK, response.status());
        assertEquals(BytesRestResponse.TEXT_CONTENT_TYPE, response.contentType());
        responseStr = new String(BytesReference.toBytes(response.content()), StandardCharsets.UTF_8);
        assertEquals("Hello, Passing Test!", responseStr);

        response = restHelloAction.handleRequest(Method.PUT, "/hello/Bad%Request");
        assertEquals(RestStatus.BAD_REQUEST, response.status());
        assertEquals(BytesRestResponse.TEXT_CONTENT_TYPE, response.contentType());
        responseStr = new String(BytesReference.toBytes(response.content()), StandardCharsets.UTF_8);
        assertTrue(responseStr.contains("Illegal hex characters in escape (%) pattern"));

        response = restHelloAction.handleRequest(Method.GET, "/goodbye");
        assertEquals(RestStatus.NOT_FOUND, response.status());
        assertEquals(BytesRestResponse.TEXT_CONTENT_TYPE, response.contentType());
        responseStr = new String(BytesReference.toBytes(response.content()), StandardCharsets.UTF_8);
        assertTrue(responseStr.contains("/goodbye"));
    }

}