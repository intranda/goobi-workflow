package de.sub.goobi.config;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *     		- https://goobi.io
 * 			- https://www.intranda.com
 * 			- https://github.com/intranda/goobi
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
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.goobi.production.plugin.interfaces.IPlugin;

import de.sub.goobi.helper.Helper;
import lombok.extern.log4j.Log4j;

@Log4j
public class ConfigPlugins {

    /**
     * pass back the right configuration file by giving the plugin class
     *
     * @deprecated use getPluginConfig(String pluginname) for this instead
     */
    @Deprecated
    public static XMLConfiguration getPluginConfig(IPlugin inPlugin) {
        return getPluginConfig(inPlugin.getClass().getSimpleName());
    }

    /**
     * pass back the right configuration file by giving the internal plugin name
     */
    public static XMLConfiguration getPluginConfig(String pluginname) {
        String file = "plugin_" + pluginname + ".xml";
        XMLConfiguration config;
        try {
            config = new XMLConfiguration(new Helper().getGoobiConfigDirectory() + file);
        } catch (ConfigurationException e) {
            log.error("Error while reading the configuration file " + file, e);
            config = new XMLConfiguration();
        }
        config.setListDelimiter('&');
        config.setReloadingStrategy(new FileChangedReloadingStrategy());
        return config;
    }
}
