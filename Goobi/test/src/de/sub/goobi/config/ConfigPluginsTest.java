package de.sub.goobi.config;

import static org.junit.Assert.*;

import java.io.File;

import org.apache.commons.configuration.XMLConfiguration;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IImportPlugin;
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
