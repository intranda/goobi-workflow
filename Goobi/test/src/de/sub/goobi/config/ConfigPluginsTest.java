package de.sub.goobi.config;

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
import static org.junit.Assert.assertNotNull;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.configuration.XMLConfiguration;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IImportPlugin;
import org.junit.BeforeClass;
import org.junit.Test;

import de.sub.goobi.AbstractTest;

public class ConfigPluginsTest extends AbstractTest {

    @BeforeClass
    public static void setUp() throws URISyntaxException {
        Path template = Paths.get(ConfigProjectsTest.class.getClassLoader().getResource(".").getFile());
        String goobiFolder = template.getParent().getParent().getParent().toString() + "/test/resources/";
        ConfigurationHelper.CONFIG_FILE_NAME = goobiFolder + "config/goobi_config.properties";
        ConfigurationHelper.resetConfigurationFile();
        ConfigurationHelper.getInstance().setParameter("goobiFolder", goobiFolder);
    }

    @Test
    public void testConstructor() {
        ConfigPlugins cp = new ConfigPlugins();
        assertNotNull(cp);
    }

    @Test
    public void testConfigPluginsWithoutFile() {

        IImportPlugin plugin = (IImportPlugin) PluginLoader.getPluginByTitle(PluginType.Import, "JunitImportPlugin");

        @SuppressWarnings("deprecation")
        XMLConfiguration config = ConfigPlugins.getPluginConfig(plugin);
        assertNotNull(config);
    }

    @Test
    public void testConfigPluginsWithConfiguration() {

        IImportPlugin plugin = (IImportPlugin) PluginLoader.getPluginByTitle(PluginType.Import, "JunitImportPluginError");

        @SuppressWarnings("deprecation")
        XMLConfiguration config = ConfigPlugins.getPluginConfig(plugin);
        assertNotNull(config);
    }

}
