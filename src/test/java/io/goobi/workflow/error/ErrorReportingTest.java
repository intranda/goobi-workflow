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
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
package io.goobi.workflow.error;

import static org.mockito.Mockito.doThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.goobi.beans.User;
import org.junit.jupiter.api.Test;

import jakarta.faces.application.ViewExpiredException;
import jakarta.servlet.http.HttpServletRequest;

class ErrorReportingTest {

    private static HttpServletRequest request(Map<String, Object> attributes) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getAttribute(anyString())).thenAnswer(i -> attributes.get(i.getArgument(0, String.class)));
        doAnswer(i -> attributes.put(i.getArgument(0, String.class), i.getArgument(1))).when(request).setAttribute(anyString(), org.mockito.ArgumentMatchers.any());
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/goobi/uii/x.xhtml");
        return request;
    }

    @Test
    void reportIsCreatedOncePerRequest() {
        HttpServletRequest request = request(new HashMap<>());
        ErrorReport first = ErrorReporting.report(request, new IllegalStateException("a"), 500);
        ErrorReport second = ErrorReporting.report(request, new IllegalArgumentException("b"), 500);
        assertSame(first, second);
        assertEquals(first.incidentId(), second.incidentId());
    }

    @Test
    void noIncidentForNotFound() {
        ErrorReport report = ErrorReporting.report(request(new HashMap<>()), null, 404);
        assertNull(report.incidentId());
        assertSame(ErrorCatalogue.NOT_FOUND, report.error().entry());
    }

    @Test
    void expiredViewIdIsKept() {
        ErrorReport report = ErrorReporting.report(request(new HashMap<>()), new ViewExpiredException("x", "/uii/process_all.xhtml"), 500);
        assertEquals("/uii/process_all.xhtml", report.expiredViewId());
        assertNull(report.incidentId());
    }

    @Test
    void unexpectedGetsIncident() {
        ErrorReport report = ErrorReporting.report(request(new HashMap<>()), new NullPointerException(), 500);
        assertNotNull(report.incidentId());
        assertTrue(report.incidentId().matches("^GW-[2-9A-HJ-NP-Z]{5}$"));
    }

    @Test
    void ajaxHeaderIsDetected() {
        HttpServletRequest request = request(new HashMap<>());
        when(request.getHeader("Faces-Request")).thenReturn("partial/ajax");
        User user = mock(User.class);
        when(user.getLogin()).thenReturn("jschmidt");
        when(user.getId()).thenReturn(12);
        ErrorReport report = ErrorReporting.report(request, new NullPointerException(), 500, user);
        IncidentLogger.RequestInfo info = ErrorReporting.requestInfo(request, user);
        String text = IncidentLogger.format(report, info);
        assertTrue(text.contains("ajax: yes"), text);
        assertTrue(text.contains("jschmidt (id 12)"), text);
    }

    @Test
    void throwingRequestStillReports() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getAttribute(anyString())).thenThrow(new IllegalStateException("gone"));
        doThrow(new IllegalStateException("gone")).when(request).setAttribute(anyString(), org.mockito.ArgumentMatchers.any());
        ErrorReport report = ErrorReporting.report(request, new NullPointerException(), 500);
        assertNotNull(report);
        assertNotNull(report.incidentId());
    }

    @Test
    void nullRequestStillReports() {
        ErrorReport report = ErrorReporting.report(null, new NullPointerException(), 500);
        assertNotNull(report);
        assertNotNull(report.incidentId());
    }

    @Test
    void errorRequestUriIsPreferred() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("jakarta.servlet.error.request_uri", "/goobi/original.xhtml");
        IncidentLogger.RequestInfo info = ErrorReporting.requestInfo(request(attributes), null);
        assertEquals("/goobi/original.xhtml", info.uri());
    }
}
