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
package org.goobi.production.cli;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.Test;

import de.sub.goobi.AbstractTest;

public class WebInterfaceTest extends AbstractTest {

    @Test
    public void testBuildHelpMessageEscapesXssPayloadInForCommand() {
        String result = WebInterface.buildHelpMessage(Collections.emptyList(), "<script>alert(1)</script>");
        assertFalse("XSS payload must not appear unescaped in help output", result.contains("<script>"));
    }

    @Test
    public void testBuildHelpMessageContainsEscapedForCommandWhenNoPluginMatches() {
        String result = WebInterface.buildHelpMessage(Collections.emptyList(), "<script>alert(1)</script>");
        assertTrue("Escaped forCommand must appear in output", result.contains("&lt;script&gt;"));
    }

    @Test
    public void testBuildHelpMessageWithNullForCommandContainsUsageHint() {
        String result = WebInterface.buildHelpMessage(Collections.emptyList(), null);
        assertTrue("Help message with null forCommand must contain usage hint", result.contains("for="));
    }

    @Test
    public void testBuildAnswerHtmlEscapesTitleXssPayload() {
        CommandResponse cr = new CommandResponse(200, "<script>alert(1)</script>", "message");
        String html = WebInterface.buildAnswerHtml(cr);
        assertFalse("XSS in title must not appear unescaped", html.contains("<script>alert(1)</script>"));
        assertTrue("Escaped title must appear in HTML", html.contains("&lt;script&gt;alert(1)&lt;/script&gt;"));
    }

    @Test
    public void testBuildAnswerHtmlDoesNotEscapeMessageHtml() {
        CommandResponse cr = new CommandResponse(200, "title", "<h4>Help</h4>");
        String html = WebInterface.buildAnswerHtml(cr);
        assertTrue("Message HTML must be preserved unescaped", html.contains("<h4>Help</h4>"));
    }
}
