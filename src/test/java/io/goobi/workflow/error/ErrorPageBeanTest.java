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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.Test;

class ErrorPageBeanTest {

    private static final Instant TIME = Instant.parse("2026-11-13T10:42:17Z");

    private static ErrorPageBean bean(ErrorEntry entry, List<String> params, Throwable cause, String expiredViewId, boolean admin,
            boolean loggedIn) {
        ErrorReport report = new ErrorReport(new ResolvedError(entry, params, Map.of(), cause), entry.incident() ? "GW-7K3F9" : null, TIME,
                expiredViewId);
        return new ErrorPageBean(report, admin, loggedIn, "/goobi", Locale.ENGLISH, ZoneId.of("UTC"), (k, p) -> "T:" + k + (p.length == 0 ? "" : " " + String.join("|", p)));
    }

    private static ErrorPageBean bean(ErrorEntry entry, boolean admin, boolean loggedIn) {
        return bean(entry, List.of(), null, null, admin, loggedIn);
    }

    @Test
    void adminSeesAdminStepsForMetadataEntry() {
        ErrorPageBean bean = bean(ErrorCatalogue.METADATA_UNREADABLE, true, true);
        assertEquals(List.of("T:error_metadataUnreadable_admin_1", "T:error_metadataUnreadable_admin_2", "T:error_metadataUnreadable_admin_3"),
                bean.getAdminSteps());
        assertTrue(bean.isShowAdmin());
    }

    @Test
    void nonAdminSeesNoAdminSteps() {
        ErrorPageBean bean = bean(ErrorCatalogue.METADATA_UNREADABLE, false, true);
        assertTrue(bean.getAdminSteps().isEmpty());
        assertFalse(bean.isShowAdmin());
    }

    @Test
    void loggedOutSeesNoAdminSteps() {
        ErrorPageBean bean = bean(ErrorCatalogue.METADATA_UNREADABLE, true, false);
        assertTrue(bean.getAdminSteps().isEmpty());
        assertFalse(bean.isShowAdmin());
    }

    @Test
    void incidentShownOnlyWhenPresent() {
        assertNull(bean(ErrorCatalogue.NOT_FOUND, false, true).getIncidentId());
        assertEquals("GW-7K3F9", bean(ErrorCatalogue.UNEXPECTED, false, true).getIncidentId());
    }

    @Test
    void viewExpiredOffersOpenAgain() {
        ErrorPageBean bean = bean(ErrorCatalogue.VIEW_EXPIRED, List.of(), null, "/uii/process_all.xhtml", false, true);
        assertEquals("/goobi/uii/process_all.xhtml", bean.getOpenAgainUrl());
        assertFalse(bean.isShowBack());
    }

    @Test
    void loggedOutUsesBlankTemplateAndLoginButton() {
        ErrorPageBean bean = bean(ErrorCatalogue.UNEXPECTED, false, false);
        assertEquals("/uii/template/template_blank.html", bean.getTemplate());
        assertEquals("T:error_goToLogin", bean.getStartLabel());
        assertFalse(bean.isShowBack());
        assertEquals(List.of("T:error_unexpected_anonHow_1", "T:error_unexpected_anonHow_2"), bean.getHowSteps());
        assertEquals("T:error_unexpected_anonWhat", bean.getWhat());
    }

    @Test
    void loggedOutFallsBackToNormalTexts() {
        ErrorReport report = new ErrorReport(new ResolvedError(ErrorCatalogue.FORBIDDEN, List.of(), Map.of(), null), null, TIME, null);
        // missing keys are returned unchanged, like Helper does
        ErrorPageBean bean = new ErrorPageBean(report, false, false, "/goobi", Locale.ENGLISH, ZoneId.of("UTC"),
                (k, p) -> k.contains("anon") ? k : "T:" + k);
        assertEquals("T:error_forbidden_what", bean.getWhat());
        assertEquals(List.of("T:error_forbidden_how_1"), bean.getHowSteps());
    }

    @Test
    void loggedInUsesFullTemplate() {
        ErrorPageBean bean = bean(ErrorCatalogue.UNEXPECTED, false, true);
        assertEquals("/uii/template/template.html", bean.getTemplate());
        assertEquals("T:error_startPage", bean.getStartLabel());
        assertTrue(bean.isShowBack());
        assertEquals("T:error_unexpected_what", bean.getWhat());
        assertEquals("/goobi/uii/index.xhtml", bean.getStartUrl());
    }

    @Test
    void illustrationUrlForMetadataEntry() {
        assertEquals("/goobi/uii/template/img/errors/metadata-unreadable.svg",
                bean(ErrorCatalogue.METADATA_UNREADABLE, false, true).getIllustrationUrl());
        ErrorPageBean unexpected = bean(ErrorCatalogue.UNEXPECTED, false, true);
        assertNull(unexpected.getIllustrationUrl());
        assertEquals("alert-octagon", unexpected.getIcon());
        assertEquals("danger", unexpected.getSeverityCss());
    }

    @Test
    void contextNamesTheProcessOnlyForMetadataEntry() {
        ErrorPageBean metadata = bean(ErrorCatalogue.METADATA_UNREADABLE, List.of("book_1", "4711"), null, null, false, true);
        assertEquals("T:error_context_process book_1|4711", metadata.getContext());
        assertEquals(List.of("T:error_context_process ", "book_1", "|", "4711"),
                metadata.getContextSegments().stream().map(ErrorPageBean.ContextSegment::text).toList());
        assertEquals(List.of(false, true, false, false), metadata.getContextSegments().stream().map(ErrorPageBean.ContextSegment::strong).toList());
        assertNull(bean(ErrorCatalogue.UNEXPECTED, false, true).getContext());
    }

    @Test
    void copyTextHasNoTechnicalData() {
        ErrorPageBean bean = bean(ErrorCatalogue.UNEXPECTED, List.of(), new IllegalStateException("/opt/secret/path"), null, true, true);
        assertFalse(bean.getCopyText().contains("/opt/secret/path"));
        assertEquals("13 Nov 2026, 10:42:17", bean.getIncidentTime());
        assertEquals("Incident GW-7K3F9, 13 Nov 2026, 10:42:17, T:error_unexpected_title", bean.getCopyText());
    }
}
