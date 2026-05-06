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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.junit.jupiter.api.Test;

import de.sub.goobi.AbstractTest;

public class HttpUtilsTest extends AbstractTest {

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
