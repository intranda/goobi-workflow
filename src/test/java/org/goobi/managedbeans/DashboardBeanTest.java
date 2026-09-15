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
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.view.ViewScoped;

class DashboardBeanTest {

    /**
     * The bean is referenced from the main menu, so it is used on every page. Its @PostConstruct looks up the dashboard plugin, which builds a new
     * plugin manager and scans the plugin folder. Request scope would repeat that work for every single request.
     */
    @Test
    void testDashboardBeanOutlivesTheSingleRequest() {
        assertFalse(DashboardBean.class.isAnnotationPresent(RequestScoped.class), "dashboard plugin lookup would run on every request");
        assertTrue(DashboardBean.class.isAnnotationPresent(ViewScoped.class), "the plugin has to be looked up once per rendered page");
    }

    /**
     * The plugin instance carries the dashboard data, and the plugins cache it in their constructor or in lazy getters. A scope that survives a page
     * reload would therefore keep showing the numbers of the first page load.
     */
    @Test
    void testDashboardBeanDoesNotOutliveTheView() {
        assertFalse(DashboardBean.class.isAnnotationPresent(SessionScoped.class), "cached dashboard data would never be refreshed");
        assertFalse(DashboardBean.class.isAnnotationPresent(WindowScoped.class), "cached dashboard data would never be refreshed");
    }

    private User userWithDashboardPlugin(String pluginName) {
        Institution institution = Mockito.mock(Institution.class);
        Mockito.when(institution.isDashboardPluginAvailable(pluginName)).thenReturn(true);
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
     * The bean is created for every page, so it may well be created before anybody is logged in.
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
     * A user who picks a different dashboard plugin must not have to reload the page for it.
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

    /**
     * The main menu and the breadcrumb of every page only need to know whether a dashboard exists. Answering that through getPlugin() meant scanning
     * the plugin folder and running the plugin constructor - for the extended dashboard several database queries - on every page that shows a
     * breadcrumb.
     */
    @Test
    void testDashboardAvailabilityIsAnsweredWithoutBuildingThePlugin() {
        try (MockedStatic<Helper> helper = Mockito.mockStatic(Helper.class);
                MockedStatic<PluginLoader> pluginLoader = Mockito.mockStatic(PluginLoader.class)) {
            User user = userWithDashboardPlugin("extended");
            helper.when(Helper::getCurrentUser).thenReturn(user);

            assertTrue(new DashboardBean().isDashboardAvailable());

            pluginLoader.verifyNoInteractions();
        }
    }

    @Test
    void testNoDashboardIsAvailableWithoutAUserOrAConfiguredPlugin() {
        try (MockedStatic<Helper> helper = Mockito.mockStatic(Helper.class)) {
            DashboardBean bean = new DashboardBean();

            helper.when(Helper::getCurrentUser).thenReturn(null);
            assertFalse(bean.isDashboardAvailable());

            User userWithoutDashboard = userWithDashboardPlugin("");
            helper.when(Helper::getCurrentUser).thenReturn(userWithoutDashboard);
            assertFalse(bean.isDashboardAvailable());
        }
    }

    /**
     * A plugin the institution does not offer must not be built, and the page must fall back to the plain start page.
     */
    @Test
    void testPluginIsNotLoadedWhenTheInstitutionDoesNotOfferIt() {
        try (MockedStatic<Helper> helper = Mockito.mockStatic(Helper.class);
                MockedStatic<PluginLoader> pluginLoader = Mockito.mockStatic(PluginLoader.class)) {
            registerPlugin(pluginLoader, "extended");
            Institution institution = Mockito.mock(Institution.class);
            Mockito.when(institution.isDashboardPluginAvailable("extended")).thenReturn(false);
            User user = Mockito.mock(User.class);
            Mockito.when(user.getDashboardPlugin()).thenReturn("extended");
            Mockito.when(user.getInstitution()).thenReturn(institution);
            helper.when(Helper::getCurrentUser).thenReturn(user);

            DashboardBean bean = new DashboardBean();

            assertFalse(bean.isDashboardAvailable());
            assertNull(bean.getPlugin());
            pluginLoader.verifyNoInteractions();
        }
    }
}
