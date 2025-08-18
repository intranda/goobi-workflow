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
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.goobi.beans.JournalEntry;
import org.goobi.beans.JournalEntry.EntryType;
import org.goobi.beans.Process;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.LogType;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.JournalManager;
import de.sub.goobi.persistence.managers.ProcessManager;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GoobiScriptAddToJournal extends AbstractIGoobiScript implements IGoobiScript {

    private static final String MESSAGE = "message";
    private static final String TYPE = "type";

    @Override
    public String getAction() {
        return "addToJournal";
    }

    @Override
    public String getSampleCall() {
        StringBuilder sb = new StringBuilder();
        addNewActionToSampleCall(sb, "This GoobiScript allows to add messages to the Goobi process journal.");
        addParameterToSampleCall(sb, TYPE, "info",
                "Define the type for the message here. Possible values are: `error` `warn` `info` `debug` and `user`");
        addParameterToSampleCall(sb, MESSAGE, "\"This is my message\"",
                "This parameter allows to define the message itself that shall be added to the process log. "
                        + "To write special characters like # put it into quotes.");
        return sb.toString();
    }

    @Override
    public List<GoobiScriptResult> prepare(List<Integer> processes, String command, Map<String, String> parameters) {
        super.prepare(processes, command, parameters);

        String missingParameter = "Missing parameter: ";
        if (parameters.get(MESSAGE) == null || "".equals(parameters.get(MESSAGE))) {
            Helper.setFehlerMeldung(missingParameter, MESSAGE);
            return new ArrayList<>();
        }

        String type = parameters.get(TYPE);
        if (type == null || "".equals(type)) {
            Helper.setFehlerMeldung(missingParameter, TYPE);
            return new ArrayList<>();
        }
        if (!"error".equals(type) && !"warn".equals(type) && !"info".equals(type) && !"debug".equals(type) && !"user".equals(type)) {
            Helper.setFehlerMeldung("Wrong parameter for type. Allowed values are: error, warn, info, debug and user");
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
        gsr.setProcessTitle(p.getTitel());
        gsr.setResultType(GoobiScriptResultType.RUNNING);
        gsr.updateTimestamp();

        JournalEntry logEntry = new JournalEntry(p.getId(), new Date(), username, LogType.getByTitle(parameters.get(TYPE)), parameters.get(MESSAGE),
                EntryType.PROCESS);
        JournalManager.saveJournalEntry(logEntry);
        log.info("Process log updated for process with ID " + p.getId());

        gsr.setResultMessage("Process log updated.");
        gsr.setResultType(GoobiScriptResultType.OK);
        gsr.updateTimestamp();
    }

}
