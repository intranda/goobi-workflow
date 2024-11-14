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
package org.goobi.goobiScript;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.LogType;
import org.goobi.production.enums.PluginGuiType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IStepPlugin;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.StepManager;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GoobiScriptRunPlugin extends AbstractIGoobiScript implements IGoobiScript {

    private static final String STEPTITLE = "steptitle";

    @Override
    public String getAction() {
        return "runPlugin";
    }

    @Override
    public String getSampleCall() {
        StringBuilder sb = new StringBuilder();
        addNewActionToSampleCall(sb, "This GoobiScript allow to run a plugin that is assigned to a workflow step.");
        addParameterToSampleCall(sb, STEPTITLE, "Catalogue update", "Title of the workflow step which has a plugin assigned that shall be executed");
        return sb.toString();
    }

    @Override
    public List<GoobiScriptResult> prepare(List<Integer> processes, String command, Map<String, String> parameters) {
        super.prepare(processes, command, parameters);

        String missingParameter = "Missing parameter: ";
        String steptitle = parameters.get(STEPTITLE);
        if (steptitle == null || "".equals(steptitle)) {
            Helper.setFehlerMeldung(missingParameter, STEPTITLE);
            return new ArrayList<>();
        }

        // add all valid commands to list
        List<GoobiScriptResult> newList = new ArrayList<>();
        for (Integer i : processes) {
            GoobiScriptResult gsr = new GoobiScriptResult(i, command, parameters, username, starttime);
            newList.add(gsr);
        }
        return newList;
    }

    @Override
    public void execute(GoobiScriptResult gsr) {
        Map<String, String> parameters = gsr.getParameters();
        String steptitle = parameters.get(STEPTITLE);

        String errorPrefix = "Plugin for step '" + steptitle + "'";
        Process p = ProcessManager.getProcessById(gsr.getProcessId());
        gsr.setProcessTitle(p.getTitel());
        gsr.setResultType(GoobiScriptResultType.RUNNING);
        gsr.updateTimestamp();
        for (Step step : p.getSchritteList()) {
            if (step.getTitel().equalsIgnoreCase(steptitle)) {
                Step so = StepManager.getStepById(step.getId());

                if (so.getStepPlugin() != null && !so.getStepPlugin().isEmpty()) {
                    IStepPlugin myPlugin = (IStepPlugin) PluginLoader.getPluginByTitle(PluginType.Step, so.getStepPlugin());

                    if (myPlugin != null) {
                        myPlugin.initialize(so, "");

                        if (myPlugin.getPluginGuiType() == PluginGuiType.NONE) {
                            myPlugin.execute();
                            myPlugin.finish();

                            Helper.addMessageToProcessJournal(p.getId(), LogType.DEBUG, errorPrefix + " executed using GoobiScript.", username);
                            log.info(errorPrefix + " executed using GoobiScript for process with ID " + p.getId());
                            gsr.setResultMessage(errorPrefix + " executed successfully.");
                            gsr.setResultType(GoobiScriptResultType.OK);
                        } else {
                            gsr.setResultMessage(errorPrefix + " cannot be executed as it has a GUI.");
                            gsr.setResultType(GoobiScriptResultType.ERROR);
                        }
                    }
                } else {
                    gsr.setResultMessage(errorPrefix + " cannot be executed as it is not Plugin workflow step.");
                    gsr.setResultType(GoobiScriptResultType.ERROR);
                }
            }
        }
        if (GoobiScriptResultType.RUNNING.equals(gsr.getResultType())) {
            gsr.setResultType(GoobiScriptResultType.OK);
            gsr.setResultMessage("Step not found: " + steptitle);
        }
        gsr.updateTimestamp();
    }
}
