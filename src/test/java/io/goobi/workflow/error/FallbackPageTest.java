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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.Test;

class FallbackPageTest {

    private static ErrorReport report(String incident) {
        return new ErrorReport(new ResolvedError(ErrorCatalogue.DATABASE_UNAVAILABLE, List.of(), Map.of(), null), incident, Instant.now(), null);
    }

    @Test
    void rendersIncidentAndEscapes() {
        String html = FallbackPage.render(report("GW-ABCDE"), Locale.ENGLISH);
        assertTrue(html.contains("GW-ABCDE"));
        assertTrue(html.contains("lang=\"en\""));
        assertFalse(html.contains("${"));
        assertTrue(html.contains("class=\"login"));

        String escaped = FallbackPage.render(report("<b>&\"x"), Locale.ENGLISH);
        assertFalse(escaped.contains("<b>&"));
        assertTrue(escaped.contains("&lt;b&gt;&amp;&quot;x"));

        String german = FallbackPage.render(report("GW-ABCDE"), Locale.GERMAN);
        assertTrue(german.contains("lang=\"de\""));
        assertTrue(german.contains("Goobi workflow ist momentan nicht verfügbar"));
        assertFalse(german.contains("${"));
    }

    @Test
    void noIncidentChipWithoutIncident() {
        String html = FallbackPage.render(report(null), Locale.ENGLISH);
        assertFalse(html.contains("GW-"));
        assertFalse(html.contains("class=\"chip\""));
        assertFalse(html.contains("${"));
    }

    @Test
    void unknownLocaleFallsBackToEnglish() {
        String html = FallbackPage.render(report("GW-ABCDE"), Locale.FRENCH);
        assertTrue(html.contains("lang=\"en\""));
    }

    @Test
    void partialWrapsPageInViewRootUpdate() {
        String partial = FallbackPage.renderPartial(report("GW-ABCDE"), Locale.ENGLISH);
        assertTrue(partial.startsWith("<?xml"));
        assertTrue(partial.contains("<update id=\"jakarta.faces.ViewRoot\"><![CDATA["));
        assertTrue(partial.contains("GW-ABCDE"));
        assertTrue(partial.endsWith("]]></update></changes></partial-response>"));
    }
}
