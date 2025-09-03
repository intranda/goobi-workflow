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
package org.goobi.production.plugin;

import java.io.Serializable;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.interfaces.IPlugin;

import de.sub.goobi.config.ConfigurationHelper;
import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.impl.PluginManagerFactory;
import net.xeoh.plugins.base.util.PluginManagerUtil;

public class PluginLoader implements Serializable {

    public PluginLoader() {
        // default constructor is needed, class gets extended
    }

    private static final long serialVersionUID = -5922522891886118875L;

    public static List<IPlugin> getPluginList(PluginType inType) {
        PluginManagerUtil pmu = initialize(inType);
        Collection<IPlugin> plugins = pmu.getPlugins(IPlugin.class);
        return new ArrayList<>(plugins);
    }

    public static IPlugin getPluginByTitle(PluginType inType, String inTitle) {

        PluginManagerUtil pmu = initialize(inType);
        Collection<IPlugin> plugins = pmu.getPlugins(inType.getInterfaz());
        for (IPlugin p : plugins) {
            if (p.getTitle().equals(inTitle)) {
                return p;
            }
        }
        return null;
    }

    private static PluginManagerUtil initialize(PluginType inType) {
        PluginManager pm = PluginManagerFactory.createPluginManager();
        String path = ConfigurationHelper.getInstance().getPluginFolder() + inType.getName() + "/";
        pm.addPluginsFrom(Paths.get(path).toUri());
        return new PluginManagerUtil(pm);
    }

    public static List<String> getListOfPlugins(PluginType inType) {
        List<String> pluginList = new ArrayList<>();
        PluginManagerUtil pmu = initialize(inType);
        Collection<IPlugin> plugins = pmu.getPlugins(inType.getInterfaz());
        for (IPlugin p : plugins) {
            pluginList.add(p.getTitle());
        }

        return pluginList;
    }
}
