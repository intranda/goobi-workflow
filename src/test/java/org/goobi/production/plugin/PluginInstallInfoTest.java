package org.goobi.production.plugin;

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
 */
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

public class PluginInstallInfoTest {

    @Test
    public void testNoArgsConstructor() {
        PluginInstallInfo info = new PluginInstallInfo();
        assertNotNull(info);
        assertEquals(0, info.getId());
        assertNull(info.getName());
        assertNull(info.getType());
        assertNull(info.getSourcecodeUri());
        assertNull(info.getDocumentationUri());
        assertNull(info.getVersions());
    }

    @Test
    public void testAllArgsConstructor() {
        List<PluginVersion> versions = Arrays.asList(new PluginVersion("abc", "sha256", "24.05", "24.05", "1.0"));
        PluginInstallInfo info = new PluginInstallInfo(42, "plugin_intranda_step_test", "step",
                "https://github.com/intranda/plugin", "https://docs.intranda.com", versions);

        assertEquals(42, info.getId());
        assertEquals("plugin_intranda_step_test", info.getName());
        assertEquals("step", info.getType());
        assertEquals("https://github.com/intranda/plugin", info.getSourcecodeUri());
        assertEquals("https://docs.intranda.com", info.getDocumentationUri());
        assertEquals(1, info.getVersions().size());
    }

    @Test
    public void testSetters() {
        PluginInstallInfo info = new PluginInstallInfo();
        info.setId(7);
        info.setName("plugin_intranda_import_csv");
        info.setType("import");
        info.setSourcecodeUri("https://github.com/intranda/plugin2");
        info.setDocumentationUri("https://docs.intranda.com/plugin2");

        assertEquals(7, info.getId());
        assertEquals("plugin_intranda_import_csv", info.getName());
        assertEquals("import", info.getType());
    }

    @Test
    public void testToString() {
        PluginInstallInfo info = new PluginInstallInfo(1, "test", "step", null, null, null);
        assertNotNull(info.toString());
    }
}
