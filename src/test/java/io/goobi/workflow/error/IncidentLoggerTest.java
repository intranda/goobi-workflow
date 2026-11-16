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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class IncidentLoggerTest {

    @Test
    void formatContainsAllContext() {
        Map<String, String> context = new LinkedHashMap<>();
        context.put("file", "/opt/x/meta.xml");
        context.put("backups", "meta.xml.2026-11-12-174102311");
        ResolvedError error = new ResolvedError(ErrorCatalogue.METADATA_UNREADABLE, java.util.List.of(), context, null);
        ErrorReport report = new ErrorReport(error, IncidentIds.next(), Instant.now(), null);
        IncidentLogger.RequestInfo info = new IncidentLogger.RequestInfo("jschmidt", 12, "GET", "/goobi/uii/metseditor.xhtml",
                "/uii/process_edit.xhtml", false, "3f9c…");

        String text = IncidentLogger.format(report, info);

        assertTrue(text.contains("Incident GW-"), text);
        assertTrue(text.contains("[metadataUnreadable]"), text);
        assertTrue(text.contains("jschmidt (id 12)"), text);
        assertTrue(text.contains("GET /goobi/uii/metseditor.xhtml"), text);
        assertTrue(text.contains("ajax: no"), text);
        assertTrue(text.contains("file: /opt/x/meta.xml"), text);
        assertTrue(text.contains("backups: meta.xml.2026-11-12-174102311"), text);
    }

    @Test
    void missingValuesAreDashes() {
        ResolvedError error = new ResolvedError(ErrorCatalogue.UNEXPECTED, java.util.List.of(), Map.of(), null);
        ErrorReport report = new ErrorReport(error, "GW-AAAAA", Instant.now(), null);
        IncidentLogger.RequestInfo info = new IncidentLogger.RequestInfo(null, null, null, null, null, false, null);

        String text = IncidentLogger.format(report, info);

        assertTrue(text.contains("user:     -"), text);
    }
}
