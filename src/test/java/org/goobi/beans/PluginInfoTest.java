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
package org.goobi.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

public class PluginInfoTest {

    private PluginInfo info;

    @Before
    public void setUp() {
        info = new PluginInfo();
    }

    @Test
    public void testDefaultGoobiVersion() {
        assertEquals("N/A", info.getGoobiVersion());
    }

    @Test
    public void testDefaultContainedPluginsIsEmpty() {
        assertNotNull(info.getContainedPlugins());
        assertTrue(info.getContainedPlugins().isEmpty());
    }

    @Test
    public void testDefaultFilenameIsNull() {
        assertNull(info.getFilename());
    }

    @Test
    public void testSetFilename() {
        info.setFilename("plugin.jar");
        assertEquals("plugin.jar", info.getFilename());
    }

    @Test
    public void testSetBuildDate() {
        Date d = new Date();
        info.setBuildDate(d);
        assertEquals(d, info.getBuildDate());
    }

    @Test
    public void testSetGitHash() {
        info.setGitHash("abc123def");
        assertEquals("abc123def", info.getGitHash());
    }

    @Test
    public void testAddContainedPlugin() {
        info.addContainedPlugin("PluginA");
        info.addContainedPlugin("PluginB");
        assertEquals(2, info.getContainedPlugins().size());
        assertTrue(info.getContainedPlugins().contains("PluginA"));
        assertTrue(info.getContainedPlugins().contains("PluginB"));
    }
}
