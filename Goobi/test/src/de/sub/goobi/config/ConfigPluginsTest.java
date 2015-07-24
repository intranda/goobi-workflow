package de.sub.goobi.config;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *          - http://www.intranda.com
 *          - http://digiverso.com 
 *          - http://www.goobi.org
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
import static org.junit.Assert.*;

import org.apache.commons.configuration.XMLConfiguration;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IImportPlugin;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ConfigPluginsTest {

    @Before
    public void setUp() {

       String datafolder = System.getenv("junitdata");
        if (datafolder == null) {
            datafolder = "/opt/digiverso/junit/data/";
        }
        ConfigurationHelper.CONFIG_FILE_NAME = datafolder + "goobi_config.properties";
        ConfigurationHelper.getInstance().setParameter("KonfigurationVerzeichnis", datafolder);
        ConfigurationHelper.getInstance().setParameter("pluginFolder", datafolder);
    }
    
    @After
    public void tearDown() {
        ConfigurationHelper.getInstance().setParameter("KonfigurationVerzeichnis", "/opt/digiverso/goobi/config/");
        ConfigurationHelper.getInstance().setParameter("pluginFolder", "/opt/digiverso/goobi/plugins/");
    }
    
    @Test
    public void testConstructor() {
        ConfigPlugins cp = new ConfigPlugins();
        assertNotNull(cp);
    }
    
    @Test
    public void testConfigPluginsWithoutFile(){
    
        IImportPlugin plugin =(IImportPlugin) PluginLoader.getPluginByTitle(PluginType.Import, "JunitImportPlugin");
        
        XMLConfiguration config = ConfigPlugins.getPluginConfig(plugin);
        assertNotNull(config);
    }

    
    @Test
    public void testConfigPluginsWithConfiguration(){
    
        IImportPlugin plugin =(IImportPlugin) PluginLoader.getPluginByTitle(PluginType.Import, "JunitImportPluginError");
        
        XMLConfiguration config = ConfigPlugins.getPluginConfig(plugin);
        assertNotNull(config);
    }
    
  
}
