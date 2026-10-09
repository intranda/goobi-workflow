/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 *
 * Visit the websites for more information.
 *          - https://goobi.io
 *          - https://www.intranda.com
 *          - https://github.com/intranda/goobi-workflow
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package io.goobi.workflow.api.connection;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpResponseException;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import de.sub.goobi.AbstractTest;

public class HttpUtilsTest extends AbstractTest {

    private static HttpServer server;
    private static String baseUrl;

    @BeforeAll
    public static void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/content", exchange -> respond(exchange, HttpStatus.SC_OK, "content"));
        server.createContext("/redirect", exchange -> {
            exchange.getResponseHeaders().add("Location", "/content");
            exchange.sendResponseHeaders(HttpStatus.SC_MOVED_TEMPORARILY, -1);
            exchange.close();
        });
        server.createContext("/missing", exchange -> respond(exchange, HttpStatus.SC_NOT_FOUND, "missing"));
        server.createContext("/large", exchange -> {
            exchange.sendResponseHeaders(HttpStatus.SC_OK, 0);
            try (OutputStream out = exchange.getResponseBody()) {
                byte[] chunk = new byte[8192];
                for (int i = 0; i < 10000; i++) {
                    out.write(chunk);
                }
            } catch (IOException e) {
                // the client aborts the download
            }
        });
        server.createContext("/error-with-large-body", exchange -> {
            exchange.sendResponseHeaders(HttpStatus.SC_INTERNAL_SERVER_ERROR, 0);
            try (OutputStream out = exchange.getResponseBody()) {
                byte[] chunk = new byte[8192];
                for (int i = 0; i < 1000; i++) {
                    out.write(chunk);
                    out.flush();
                    Thread.sleep(10);
                }
            } catch (IOException e) {
                // the client aborts the download
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        server.createContext("/slow", exchange -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            respond(exchange, HttpStatus.SC_OK, "slow");
        });
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
    }

    @AfterAll
    public static void stopServer() {
        server.stop(0);
    }

    private static void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }

    @Test
    public void testGetBytesFromUrl() throws IOException {
        assertArrayEquals("content".getBytes(StandardCharsets.UTF_8), HttpUtils.getBytesFromUrl(baseUrl + "/content", 5, 1024));
    }

    @Test
    public void testGetBytesFromUrlFollowsRedirect() throws IOException {
        assertArrayEquals("content".getBytes(StandardCharsets.UTF_8), HttpUtils.getBytesFromUrl(baseUrl + "/redirect", 5, 1024));
    }

    @Test
    public void testGetBytesFromUrlWrongStatus() {
        HttpResponseException e = assertThrows(HttpResponseException.class, () -> HttpUtils.getBytesFromUrl(baseUrl + "/missing", 5, 1024));
        assertEquals(HttpStatus.SC_NOT_FOUND, e.getStatusCode());
    }

    @Test
    public void testGetBytesFromUrlTooLarge() {
        long start = System.currentTimeMillis();
        IOException e = assertThrows(IOException.class, () -> HttpUtils.getBytesFromUrl(baseUrl + "/large", 5, 1024));
        assertTrue(e.getMessage().contains("1024"), e.getMessage());
        assertTrue(System.currentTimeMillis() - start < 2000, "download must be aborted, not read completely");
    }

    @Test
    public void testGetBytesFromUrlWrongStatusDoesNotReadBody() {
        long start = System.currentTimeMillis();
        HttpResponseException e =
                assertThrows(HttpResponseException.class, () -> HttpUtils.getBytesFromUrl(baseUrl + "/error-with-large-body", 5, 1024));
        assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getStatusCode());
        assertTrue(System.currentTimeMillis() - start < 2000, "the body of an error response must not be read");
    }

    @Test
    public void testGetBytesFromUrlTimeout() {
        long start = System.currentTimeMillis();
        assertThrows(IOException.class, () -> HttpUtils.getBytesFromUrl(baseUrl + "/slow", 1, 1024));
        assertTrue(System.currentTimeMillis() - start < 2500, "request should be aborted after about one second");
    }

    @Test
    public void testGetBytesFromUrlInvalidUrl() {
        assertThrows(IOException.class, () -> HttpUtils.getBytesFromUrl("http://exa mple.org/", 1, 1024));
    }

    private static BasicHttpResponse okResponse(String body) {
        BasicHttpResponse response = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK"));
        if (body != null) {
            BasicHttpEntity entity = new BasicHttpEntity();
            entity.setContent(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
            response.setEntity(entity);
        }
        return response;
    }

    private static BasicHttpResponse errorResponse(int statusCode) {
        return new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, statusCode, "Error"));
    }

    @Test
    public void testByteArrayHandlerOkWithBody() throws IOException {
        byte[] result = HttpUtils.byteArrayResponseHandler.handleResponse(okResponse("hello"));
        assertNotNull(result);
        assertArrayEquals("hello".getBytes(StandardCharsets.UTF_8), result);
    }

    @Test
    public void testByteArrayHandlerNonOkStatus() throws IOException {
        byte[] result = HttpUtils.byteArrayResponseHandler.handleResponse(errorResponse(HttpStatus.SC_NOT_FOUND));
        assertNull(result);
    }

    @Test
    public void testByteArrayHandlerNullEntity() throws IOException {
        byte[] result = HttpUtils.byteArrayResponseHandler.handleResponse(okResponse(null));
        assertNull(result);
    }

    @Test
    public void testStringHandlerOkWithBody() throws IOException {
        String result = HttpUtils.stringResponseHandler.handleResponse(okResponse("world"));
        assertEquals("world", result);
    }

    @Test
    public void testStringHandlerNonOkStatus() throws IOException {
        String result = HttpUtils.stringResponseHandler.handleResponse(errorResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR));
        assertNull(result);
    }

    @Test
    public void testStringHandlerNullEntity() throws IOException {
        String result = HttpUtils.stringResponseHandler.handleResponse(okResponse(null));
        assertNull(result);
    }

    @Test
    public void testStreamHandlerOkWithBody() throws IOException {
        InputStream result = HttpUtils.streamResponseHandler.handleResponse(okResponse("stream"));
        assertNotNull(result);
        assertEquals("stream", new String(result.readAllBytes(), StandardCharsets.UTF_8));
    }

    @Test
    public void testStreamHandlerNonOkStatus() throws IOException {
        InputStream result = HttpUtils.streamResponseHandler.handleResponse(errorResponse(HttpStatus.SC_FORBIDDEN));
        assertNull(result);
    }

    @Test
    public void testStreamHandlerNullEntity() throws IOException {
        InputStream result = HttpUtils.streamResponseHandler.handleResponse(okResponse(null));
        assertNull(result);
    }

    @Test
    public void testGetStringFromUrlNullReturnsEmpty() {
        String result = HttpUtils.getStringFromUrl((String[]) null);
        assertEquals("", result);
    }

}
