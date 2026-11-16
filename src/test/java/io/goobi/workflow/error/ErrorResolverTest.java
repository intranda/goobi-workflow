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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.sql.SQLTransientConnectionException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import jakarta.el.ELException;
import jakarta.faces.FacesException;
import jakarta.faces.application.ViewExpiredException;

class ErrorResolverTest {

    private final ErrorResolver resolver = new ErrorResolver(List.of(new ViewExpiredMapper(), new DatabaseUnavailableMapper()));

    private static class Looping extends Throwable {
        private static final long serialVersionUID = 1L;
        private transient Throwable other;

        @Override
        public synchronized Throwable getCause() {
            return other;
        }
    }

    @Test
    void userFacingExceptionWrappedInFacesExceptionWins() {
        Throwable t = new FacesException(new ELException(new GoobiUserFacingException(ErrorCatalogue.METADATA_UNREADABLE, "m", null,
                List.of("t", "4711"), Map.of("file", "/x/meta.xml"))));
        ResolvedError r = resolver.resolve(t, null);
        assertEquals(ErrorCatalogue.METADATA_UNREADABLE, r.entry());
        assertEquals(List.of("t", "4711"), r.params());
        assertTrue(r.logContext().containsKey("file"));
    }

    @Test
    void outermostUserFacingExceptionWins() {
        GoobiUserFacingException inner = new GoobiUserFacingException(ErrorCatalogue.METADATA_UNREADABLE, "i", null, null, null);
        GoobiUserFacingException outer = new GoobiUserFacingException(ErrorCatalogue.UNEXPECTED, "o", inner, null, null);
        assertEquals(ErrorCatalogue.UNEXPECTED, resolver.resolve(outer, null).entry());
    }

    @Test
    void viewExpiredIsMapped() {
        Throwable t = new FacesException(new ViewExpiredException("x", "/uii/process_all.xhtml"));
        assertEquals(ErrorCatalogue.VIEW_EXPIRED, resolver.resolve(t, null).entry());
    }

    @Test
    void sqlStateConnectionFailureIsMapped() {
        Throwable t = new RuntimeException(new SQLException("x", "08S01"));
        assertEquals(ErrorCatalogue.DATABASE_UNAVAILABLE, resolver.resolve(t, null).entry());
    }

    @Test
    void otherSqlExceptionIsUnexpected() {
        Throwable t = new RuntimeException(new SQLException("x", "42000"));
        assertEquals(ErrorCatalogue.UNEXPECTED, resolver.resolve(t, null).entry());
    }

    @Test
    void nullThrowableUsesStatus() {
        assertEquals(ErrorCatalogue.NOT_FOUND, resolver.resolve(null, 404).entry());
        assertEquals(ErrorCatalogue.FORBIDDEN, resolver.resolve(null, 403).entry());
        assertEquals(ErrorCatalogue.UNEXPECTED, resolver.resolve(null, 500).entry());
        assertEquals(ErrorCatalogue.UNEXPECTED, resolver.resolve(null, 503).entry());
        assertEquals(ErrorCatalogue.UNEXPECTED, resolver.resolve(null, null).entry());
    }

    @Test
    void unknownExceptionIsUnexpected() {
        NullPointerException npe = new NullPointerException();
        ResolvedError r = resolver.resolve(npe, null);
        assertEquals(ErrorCatalogue.UNEXPECTED, r.entry());
        assertSame(npe, r.cause());
    }

    @Test
    void causeCycleTerminates() {
        Looping a = new Looping();
        Looping b = new Looping();
        a.other = b;
        b.other = a;
        assertEquals(ErrorCatalogue.UNEXPECTED, resolver.resolve(a, null).entry());
    }

    @Test
    void serviceLoaderFindsCoreMappers() {
        assertEquals(ErrorCatalogue.VIEW_EXPIRED, ErrorResolver.fromServiceLoader().resolve(new ViewExpiredException(), null).entry());
    }

    @Test
    void serviceLoaderFindsDatabaseMapper() {
        assertEquals(ErrorCatalogue.DATABASE_UNAVAILABLE,
                ErrorResolver.fromServiceLoader().resolve(new SQLException("x", "08001"), null).entry());
    }

    @Test
    void nestedMapperHitWinsWhenOuterUnknown() {
        Throwable t = new IllegalStateException(new RuntimeException(new SQLTransientConnectionException("x")));
        assertEquals(ErrorCatalogue.DATABASE_UNAVAILABLE, resolver.resolve(t, null).entry());
    }

    @Test
    void nullSqlStateIsUnexpected() {
        assertEquals(ErrorCatalogue.UNEXPECTED, resolver.resolve(new SQLException("x"), null).entry());
    }

    @Test
    void nullParamsBecomeEmptyStrings() {
        GoobiUserFacingException e = new GoobiUserFacingException(ErrorCatalogue.UNEXPECTED, "m", null, Arrays.asList("a", null), null);
        assertEquals(List.of("a", ""), e.getParams());
    }

    @Test
    void nullLogContextValuesBecomeDash() {
        Map<String, String> ctx = new java.util.HashMap<>();
        ctx.put("k", null);
        GoobiUserFacingException e = new GoobiUserFacingException(ErrorCatalogue.UNEXPECTED, "m", null, null, ctx);
        assertEquals("-", e.getLogContext().get("k"));
    }

    @Test
    void messageIsKeptVerbatim() {
        assertEquals("msg", new GoobiUserFacingException(ErrorCatalogue.UNEXPECTED, "msg", null, null, null).getMessage());
    }

    @Test
    void entryIsRequired() {
        assertThrows(NullPointerException.class, () -> new GoobiUserFacingException(null, "m", null, null, null));
    }

    @Test
    void entryIsSerializable() {
        assertTrue(java.io.Serializable.class.isAssignableFrom(ErrorEntry.class));
    }
}
