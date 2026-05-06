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
package org.goobi.production.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.goobi.production.plugin.interfaces.IImportPlugin;
import org.goobi.production.plugin.interfaces.IStepPlugin;
import org.junit.jupiter.api.Test;

public class PluginTypeTest {

    @Test
    public void testValuesCount() {
        assertEquals(15, PluginType.values().length);
    }

    @Test
    public void testImportId() {
        assertEquals(1, PluginType.Import.getId());
    }

    @Test
    public void testImportName() {
        assertEquals("import", PluginType.Import.getName());
    }

    @Test
    public void testImportInterface() {
        assertEquals(IImportPlugin.class, PluginType.Import.getInterfaz());
    }

    @Test
    public void testStepId() {
        assertEquals(2, PluginType.Step.getId());
    }

    @Test
    public void testStepName() {
        assertEquals("step", PluginType.Step.getName());
    }

    @Test
    public void testStepInterface() {
        assertEquals(IStepPlugin.class, PluginType.Step.getInterfaz());
    }

    @Test
    public void testGetTypeFromValueImport() {
        assertEquals(PluginType.Import, PluginType.getTypeFromValue("import"));
    }

    @Test
    public void testGetTypeFromValueStep() {
        assertEquals(PluginType.Step, PluginType.getTypeFromValue("step"));
    }

    @Test
    public void testGetTypeFromValueDashboard() {
        assertEquals(PluginType.Dashboard, PluginType.getTypeFromValue("dashboard"));
    }

    @Test
    public void testGetTypeFromValueAdministration() {
        assertEquals(PluginType.Administration, PluginType.getTypeFromValue("administration"));
    }

    @Test
    public void testGetTypeFromValueUnknownReturnsNull() {
        assertNull(PluginType.getTypeFromValue("unknown"));
    }

    @Test
    public void testGetTypeFromValueNullReturnsNull() {
        assertNull(PluginType.getTypeFromValue(null));
    }

    @Test
    public void testGetTypesFromIdImport() {
        assertEquals(PluginType.Import, PluginType.getTypesFromId(1));
    }

    @Test
    public void testGetTypesFromIdStep() {
        assertEquals(PluginType.Step, PluginType.getTypesFromId(2));
    }

    @Test
    public void testGetTypesFromIdWorkflow() {
        assertEquals(PluginType.Workflow, PluginType.getTypesFromId(13));
    }

    @Test
    public void testGetTypesFromIdUnknownReturnsNull() {
        assertNull(PluginType.getTypesFromId(999));
    }

    @Test
    public void testAllTypesHaveNonNullInterface() {
        for (PluginType type : PluginType.values()) {
            assertNotNull(type.getInterfaz(), "Interface null for " + type.getName());
        }
    }
}
