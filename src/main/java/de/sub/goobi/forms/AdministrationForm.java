package de.sub.goobi.forms;

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
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.goobi.production.enums.PluginGuiType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.flow.jobs.HistoryAnalyserJob;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IAdministrationPlugin;
import org.goobi.production.plugin.interfaces.IPushPlugin;
import org.omnifaces.cdi.Push;
import org.omnifaces.cdi.PushContext;

import de.sub.goobi.helper.Helper;
import lombok.Getter;
import lombok.Setter;

@Named("AdministrationForm")
@WindowScoped
public class AdministrationForm implements Serializable {
    private static final long serialVersionUID = 5648439270064158243L;
    public static final String DIRECTORY_SUFFIX = "_tif";

    @Getter
    @Setter
    private List<String> possibleAdministrationPluginNames;

    @Getter
    @Setter
    private String currentAdministrationPluginName;

    @Getter
    @Setter
    private IAdministrationPlugin administrationPlugin;

    @Inject // NOSONAR needs to be a field injection, as the been constructor does not allow arguments
    @Push
    PushContext adminPluginPush;

    public AdministrationForm() {
        possibleAdministrationPluginNames = PluginLoader.getListOfPlugins(PluginType.Administration);
        Collections.sort(possibleAdministrationPluginNames);
    }

    public void startStorageCalculationForAllProcessesNow() {
        HistoryAnalyserJob job = new HistoryAnalyserJob();
        if (!job.isRunning()) {
            job.execute();
            Helper.setMeldung("scheduler calculation executed");
        } else {
            Helper.setMeldung("Job is already running, try again in a few minutes");
        }
    }

    public String setPlugin(String pluginName) {
        currentAdministrationPluginName = pluginName;
        administrationPlugin = (IAdministrationPlugin) PluginLoader.getPluginByTitle(PluginType.Administration, currentAdministrationPluginName);
        if (administrationPlugin instanceof IPushPlugin) {
            ((IPushPlugin) administrationPlugin).setPushContext(adminPluginPush);
        }
        if (PluginGuiType.FULL == administrationPlugin.getPluginGuiType()) {
            return administrationPlugin.getGui();
        }

        return "administration";
    }

}
