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

import org.apache.commons.lang3.StringUtils;
import org.goobi.beans.Process;
import org.goobi.production.enums.GoobiScriptResultType;

import de.sub.goobi.helper.BeanHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.ProcessManager;

public class GoobiScriptChangeProcessTemplate extends AbstractIGoobiScript implements IGoobiScript {

    private static final String TEMPLATE_NAME = "templateName";

    private Process processTemplate;

    private BeanHelper helper;

    @Override
    public String getAction() {
        return "changeProcessTemplate";
    }

    @Override
    public String getSampleCall() {
        StringBuilder sb = new StringBuilder();
        addNewActionToSampleCall(sb, "This GoobiScript allow to adapt the workflow for a process by switching to another process template.");
        addParameterToSampleCall(sb, TEMPLATE_NAME, "Manuscript_workflow", "Use the name of the process template to use for the Goobi processes.");
        return sb.toString();
    }

    @Override
    public List<GoobiScriptResult> prepare(List<Integer> processes, String command, Map<String, String> parameters) {
        super.prepare(processes, command, parameters);
        helper = new BeanHelper();
        String templateName = parameters.get(TEMPLATE_NAME);
        if (StringUtils.isBlank(templateName)) {
            Helper.setFehlerMeldung("Missing parameter: ", TEMPLATE_NAME);
            return new ArrayList<>();
        }

        // check if template exists
        Process template = ProcessManager.getProcessByExactTitle(templateName);
        if (template == null) {
            Helper.setFehlerMeldung("Unknown process template: ", templateName);
            return new ArrayList<>();
        }
        processTemplate = template;

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
        Process p = ProcessManager.getProcessById(gsr.getProcessId());
        gsr.setProcessTitle(p.getTitel());
        gsr.setResultType(GoobiScriptResultType.RUNNING);
        gsr.updateTimestamp();

        if (helper.changeProcessTemplate(p, processTemplate)) {
            gsr.setResultMessage("Changed template for process " + p.getTitel());
            gsr.setResultType(GoobiScriptResultType.OK);
        } else {
            Helper.setFehlerMeldung("goobiScriptfield", "Error while saving - " + p.getTitel());
            gsr.setResultMessage("Problem while changing template for process " + p.getTitel());
            gsr.setResultType(GoobiScriptResultType.ERROR);
        }

        gsr.updateTimestamp();
    }
}
