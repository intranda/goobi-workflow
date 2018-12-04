package org.goobi.api.display.enums;

import org.goobi.production.plugin.interfaces.IMetadataPlugin;

import de.intranda.goobi.plugins.DantePlugin;
import de.intranda.goobi.plugins.GeonamesPlugin;
import de.intranda.goobi.plugins.GndPlugin;
import de.intranda.goobi.plugins.HtmlInputPlugin;
import de.intranda.goobi.plugins.InputPlugin;
import de.intranda.goobi.plugins.PersonPlugin;
import de.intranda.goobi.plugins.ProcessPlugin;
import de.intranda.goobi.plugins.ReadonlyPlugin;
import de.intranda.goobi.plugins.Select1Plugin;
import de.intranda.goobi.plugins.SelectPlugin;
import de.intranda.goobi.plugins.TextareaPlugin;
import lombok.extern.log4j.Log4j;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *             - https://goobi.io
 *             - https://www.intranda.com
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
@Log4j
public enum DisplayType {

    input(InputPlugin.class),
    select(SelectPlugin.class),
    select1(Select1Plugin.class),
    textarea(TextareaPlugin.class),
    readonly(ReadonlyPlugin.class),
    gnd(GndPlugin.class),
    person(PersonPlugin.class),
    geonames(GeonamesPlugin.class),

    dante(DantePlugin.class),
    process(ProcessPlugin.class),
    htmlInput(HtmlInputPlugin.class);

    private Class<? extends IMetadataPlugin> plugin;

    //    public IMetadataPlugin getPlugin() {
    //        IMetadataPlugin plugin = null;
    //        String pluginName = name().substring(0, 1).toUpperCase() + name().substring(1) + "Plugin";
    //        try {
    //            plugin = (IMetadataPlugin) Class.forName("de.intranda.goobi.plugins." + pluginName).newInstance();
    //        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
    //            log.error("Metadata plugin for " + pluginName + " could not be loaded");
    //        }
    //        return plugin;
    //    }

    private DisplayType( Class<? extends IMetadataPlugin> plugin) {
        this.plugin = plugin;
    }

    public Class<? extends IMetadataPlugin> getPlugin() {
        return plugin;
    }

    public IMetadataPlugin getPluginInstance() {
        try {
            return plugin.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            log.error(e);
        }
        return null;
    }

    public static DisplayType getByTitle(String inName) {
        if (inName != null) {
            for (DisplayType type : DisplayType.values()) {
                if (type.name().equals(inName)) {
                    return type;
                }
            }
        }
        return input; // input is default
    }
}
