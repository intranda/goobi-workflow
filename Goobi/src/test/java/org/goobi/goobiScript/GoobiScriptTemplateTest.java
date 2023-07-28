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

package org.goobi.goobiScript;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import de.sub.goobi.AbstractTest;

public class GoobiScriptTemplateTest extends AbstractTest {

    @Test
    public void testConstructor() {
        GoobiScriptTemplate fixture = new GoobiScriptTemplate();
        assertNotNull(fixture);
    }

    @Test
    public void testId() {
        GoobiScriptTemplate fixture = new GoobiScriptTemplate();
        assertNull(fixture.getId());
        fixture.setId(1);
        assertEquals(1, fixture.getId().intValue());
    }

    @Test
    public void testTitle() {
        GoobiScriptTemplate fixture = new GoobiScriptTemplate();
        assertNull(fixture.getTitle());

        fixture.setTitle("");
        assertEquals("", fixture.getTitle());

        fixture.setTitle("title");
        assertEquals("title", fixture.getTitle());
    }

    @Test
    public void testDescription() {
        GoobiScriptTemplate fixture = new GoobiScriptTemplate();
        assertNull(fixture.getDescription());

        fixture.setDescription("");
        assertEquals("", fixture.getDescription());

        fixture.setDescription("description");
        assertEquals("description", fixture.getDescription());
    }

    @Test
    public void testGoobiScripts() {
        GoobiScriptTemplate fixture = new GoobiScriptTemplate();
        assertNull(fixture.getGoobiScripts());

        fixture.setGoobiScripts("");
        assertEquals("", fixture.getGoobiScripts());

        fixture.setGoobiScripts("script");
        assertEquals("script", fixture.getGoobiScripts());
    }

    @Test
    public void testGetEscapedScripts() {
        GoobiScriptTemplate fixture = new GoobiScriptTemplate();
        assertEquals("", fixture.getEscapedScripts());

        fixture.setGoobiScripts("");
        assertEquals("", fixture.getEscapedScripts());

        fixture.setGoobiScripts("script");
        assertEquals("script", fixture.getEscapedScripts());

        fixture.setGoobiScripts("script\n\rwith newline");
        assertEquals("script\\nwith newline", fixture.getEscapedScripts());
    }

}
