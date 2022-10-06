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

import org.apache.commons.lang.StringUtils;
import org.goobi.beans.Process;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.LogType;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.ProcessManager;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GoobiScriptProcessRename extends AbstractIGoobiScript implements IGoobiScript {
    // action:renameProcess search:415121809 replace:1659235871 type:contains|full

    @Override
    public String getAction() {
        return "renameProcess";
    }

    @Override
    public String getSampleCall() {
        StringBuilder sb = new StringBuilder();
        addNewActionToSampleCall(sb, "This GoobiScript allows to rename a process by using search and replace.");
        addParameterToSampleCall(sb, "search", "Monograph", "Define a term that shall be searched to get replaced.");
        addParameterToSampleCall(sb, "replace", "Book", "Define the term that replaces the searched term.");
        addParameterToSampleCall(sb, "type", "contains",
                "Define here if the search term shall be contained in the current name (`contains`) or if the full name shall be the same (`full`)");
        return sb.toString();
    }

    @Override
    public List<GoobiScriptResult> prepare(List<Integer> processes, String command, Map<String, String> parameters) {
        super.prepare(processes, command, parameters);

        if (StringUtils.isBlank(parameters.get("search"))) {
            Helper.setFehlerMeldungUntranslated("goobiScriptfield", "Missing parameter: ", "search");
            return new ArrayList<>();
        }
        if (StringUtils.isBlank(parameters.get("replace"))) {
            Helper.setFehlerMeldungUntranslated("goobiScriptfield", "Missing parameter: ", "replace");
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
        Process p = ProcessManager.getProcessById(gsr.getProcessId());
        String processTitle = p.getTitel();
        gsr.setProcessTitle(processTitle);
        gsr.setResultType(GoobiScriptResultType.RUNNING);
        gsr.updateTimestamp();
        String type = parameters.get("type");
        if (StringUtils.isBlank(type)) {
            type = "contains";
        }
        String search = parameters.get("search");
        String replace = parameters.get("replace");
        boolean replacedTitle = false;
        if ("contains".equals(type)) {
            if (processTitle.contains(search)) {
                processTitle = processTitle.replace(search, replace);
                replacedTitle = true;
            }
        } else {
            if (processTitle.equalsIgnoreCase(search)) {
                processTitle = replace;
                replacedTitle = true;
            }
        }

        if (replacedTitle) {
            log.info("Proces title changed using GoobiScript for process with ID " + p.getId());
            Helper.addMessageToProcessJournal(p.getId(), LogType.DEBUG,
                    "Process title changed from '" + p.getTitel() + " to  '" + processTitle + "' using GoobiScript.", username);
            p.changeProcessTitle(processTitle);
            gsr.setProcessTitle(processTitle);
            ProcessManager.saveProcessInformation(p);
            gsr.setResultMessage("Process title changed successfully.");
        } else {
            gsr.setResultMessage("Process title did not match.");
        }

        gsr.setResultType(GoobiScriptResultType.OK);

        gsr.updateTimestamp();
    }

}
