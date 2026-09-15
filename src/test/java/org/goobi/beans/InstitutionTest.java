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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.ArrayList;
import java.util.List;

import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.PluginLoader;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import de.sub.goobi.persistence.managers.InstitutionManager;

public class InstitutionTest {

    @Test
    public void testEquals() {
        Institution inst1 = new Institution();
        inst1.setId(1);
        Institution inst2 = new Institution();
        inst2.setId(1);
        Institution inst3 = new Institution();
        inst3.setId(2);

        assertEquals(inst1, inst1);
        assertEquals(inst1, inst2);
        assertNotEquals(inst1, inst3);
        assertNotEquals(inst1, null);
        assertNotEquals(inst1, new Object());

        // Null id must not throw NullPointerException:
        Institution nullIdInst = new Institution();
        assertNotEquals(inst1, nullIdInst);
        assertNotEquals(nullIdInst, inst1);
        assertEquals(nullIdInst, nullIdInst);
    }

    @Test
    public void testHashCode() {
        Institution inst1 = new Institution();
        inst1.setId(1);
        inst1.setShortName("ABC");
        inst1.setLongName("Alpha Beta Corp");
        assertEquals(inst1.hashCode(), inst1.hashCode());

        // Null fields must not throw NullPointerException:
        Institution nullFieldsInst = new Institution();
        assertEquals(nullFieldsInst.hashCode(), nullFieldsInst.hashCode());
    }

    /**
     * The configured plugins used to be cached with an isEmpty() check, so an empty result was looked up again on every single call - and the menu
     * asks for it once per plugin entry and request.
     */
    @Test
    public void testEmptyPluginConfigurationIsLookedUpOnlyOnce() {
        try (MockedStatic<PluginLoader> pluginLoader = Mockito.mockStatic(PluginLoader.class);
                MockedStatic<InstitutionManager> manager = Mockito.mockStatic(InstitutionManager.class)) {
            pluginLoader.when(() -> PluginLoader.getListOfPlugins(Mockito.any())).thenReturn(new ArrayList<>(List.of("plugin")));
            manager.when(() -> InstitutionManager.getConfiguredDashboardPlugins(Mockito.any(), Mockito.any())).thenReturn(new ArrayList<>());
            manager.when(() -> InstitutionManager.getConfiguredWorkflowPlugins(Mockito.any(), Mockito.any())).thenReturn(new ArrayList<>());
            manager.when(() -> InstitutionManager.getConfiguredAdministrationPlugins(Mockito.any(), Mockito.any())).thenReturn(new ArrayList<>());
            manager.when(() -> InstitutionManager.getConfiguredStatisticsPlugins(Mockito.any(), Mockito.any())).thenReturn(new ArrayList<>());

            Institution institution = new Institution();
            institution.setId(1);

            institution.getAllowedDashboardPlugins();
            institution.getAllowedDashboardPlugins();
            institution.getAllowedWorkflowPlugins();
            institution.getAllowedWorkflowPlugins();
            institution.getAllowedAdministrationPlugins();
            institution.getAllowedAdministrationPlugins();
            institution.getAllowedStatisticsPlugins();
            institution.getAllowedStatisticsPlugins();

            manager.verify(() -> InstitutionManager.getConfiguredDashboardPlugins(Mockito.any(), Mockito.any()), Mockito.times(1));
            manager.verify(() -> InstitutionManager.getConfiguredWorkflowPlugins(Mockito.any(), Mockito.any()), Mockito.times(1));
            manager.verify(() -> InstitutionManager.getConfiguredAdministrationPlugins(Mockito.any(), Mockito.any()), Mockito.times(1));
            manager.verify(() -> InstitutionManager.getConfiguredStatisticsPlugins(Mockito.any(), Mockito.any()), Mockito.times(1));
        }
    }

    /**
     * Without any installed plugins there is nothing to configure, so the plugin folder must not be scanned again on every call.
     */
    @Test
    public void testPluginFolderIsNotScannedAgainWhenNoPluginsAreInstalled() {
        try (MockedStatic<PluginLoader> pluginLoader = Mockito.mockStatic(PluginLoader.class)) {
            pluginLoader.when(() -> PluginLoader.getListOfPlugins(PluginType.Dashboard)).thenReturn(new ArrayList<>());

            Institution institution = new Institution();
            institution.setId(1);

            institution.getAllowedDashboardPlugins();
            institution.getAllowedDashboardPlugins();

            pluginLoader.verify(() -> PluginLoader.getListOfPlugins(PluginType.Dashboard), Mockito.times(1));
        }
    }
}
