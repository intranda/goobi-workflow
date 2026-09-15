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
package org.goobi.managedbeans;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.goobi.beans.Institution;
import org.goobi.beans.User;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IDashboardPlugin;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import de.sub.goobi.helper.Helper;
import jakarta.enterprise.context.RequestScoped;

class DashboardBeanTest {

    /**
     * The bean is referenced from the main menu, so it is used on every page. Its @PostConstruct looks up the dashboard plugin, which builds a new
     * plugin manager and scans the plugin folder. Request scope would repeat that work for every single request, so the bean has to live as long as
     * the other menu beans do.
     */
    @Test
    void testDashboardBeanOutlivesTheSingleRequest() {
        assertFalse(DashboardBean.class.isAnnotationPresent(RequestScoped.class), "dashboard plugin lookup would run on every request");
        assertTrue(DashboardBean.class.isAnnotationPresent(WindowScoped.class), "menu beans are window scoped in this application");
    }

    private User userWithDashboardPlugin(String pluginName) {
        Institution institution = Mockito.mock(Institution.class);
        Mockito.when(institution.isAllowAllPlugins()).thenReturn(true);
        User user = Mockito.mock(User.class);
        Mockito.when(user.getDashboardPlugin()).thenReturn(pluginName);
        Mockito.when(user.getInstitution()).thenReturn(institution);
        return user;
    }

    private IDashboardPlugin registerPlugin(MockedStatic<PluginLoader> pluginLoader, String title) {
        IDashboardPlugin plugin = Mockito.mock(IDashboardPlugin.class);
        pluginLoader.when(() -> PluginLoader.getPluginByTitle(PluginType.Dashboard, title)).thenReturn(plugin);
        return plugin;
    }

    /**
     * The bean now lives as long as the browser window, so it may well be created before anybody is logged in.
     */
    @Test
    void testPluginIsPickedUpAfterTheUserLoggedIn() {
        try (MockedStatic<Helper> helper = Mockito.mockStatic(Helper.class);
                MockedStatic<PluginLoader> pluginLoader = Mockito.mockStatic(PluginLoader.class)) {
            helper.when(Helper::getCurrentUser).thenReturn(null);
            DashboardBean bean = new DashboardBean();
            assertNull(bean.getPlugin());

            IDashboardPlugin plugin = registerPlugin(pluginLoader, "extended");
            User user = userWithDashboardPlugin("extended");
            helper.when(Helper::getCurrentUser).thenReturn(user);

            assertEquals(plugin, bean.getPlugin());
        }
    }

    /**
     * A user who picks a different dashboard plugin must not have to open a new window for it.
     */
    @Test
    void testPluginFollowsAChangedUserSetting() {
        try (MockedStatic<Helper> helper = Mockito.mockStatic(Helper.class);
                MockedStatic<PluginLoader> pluginLoader = Mockito.mockStatic(PluginLoader.class)) {
            IDashboardPlugin first = registerPlugin(pluginLoader, "extended");
            IDashboardPlugin second = registerPlugin(pluginLoader, "barcode");

            User userWithExtended = userWithDashboardPlugin("extended");
            User userWithBarcode = userWithDashboardPlugin("barcode");

            helper.when(Helper::getCurrentUser).thenReturn(userWithExtended);
            DashboardBean bean = new DashboardBean();
            assertEquals(first, bean.getPlugin());

            helper.when(Helper::getCurrentUser).thenReturn(userWithBarcode);
            assertEquals(second, bean.getPlugin());
        }
    }

    /**
     * Looking the plugin up means building a plugin manager and scanning the plugin folder, so it must not happen again while nothing changed.
     */
    @Test
    void testPluginIsLookedUpOnlyOnceWhileTheSettingStaysTheSame() {
        try (MockedStatic<Helper> helper = Mockito.mockStatic(Helper.class);
                MockedStatic<PluginLoader> pluginLoader = Mockito.mockStatic(PluginLoader.class)) {
            registerPlugin(pluginLoader, "extended");
            User user = userWithDashboardPlugin("extended");
            helper.when(Helper::getCurrentUser).thenReturn(user);

            DashboardBean bean = new DashboardBean();
            bean.getPlugin();
            bean.getPlugin();
            bean.getPlugin();

            pluginLoader.verify(() -> PluginLoader.getPluginByTitle(PluginType.Dashboard, "extended"), Mockito.times(1));
        }
    }
}
