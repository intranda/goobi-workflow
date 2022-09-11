package org.goobi.production.flow.jobs;

import java.util.Date;
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
 */
import java.util.List;

import org.goobi.beans.LogEntry;
import org.goobi.beans.Step;
import org.goobi.production.enums.LogType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IDelayPlugin;
import org.goobi.production.plugin.interfaces.IStepPlugin;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.HelperSchritte;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.StepManager;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class DelayJob extends AbstractGoobiJob {

    private List<Step> getListOfStepsWithDelay() {
        String filter = " delayStep = true AND stepPlugin is not NULL AND Bearbeitungsstatus = 2";
        return StepManager.getSteps(null, filter);
    }

    @Override
    public String getJobName() {
        return "DelayJob";
    }

    @Override
    public void execute() {
        if (log.isDebugEnabled()) {
            log.debug("execute delay job");
        }
        List<Step> stepsWithDelay = getListOfStepsWithDelay();
        if (log.isDebugEnabled()) {
            log.debug(stepsWithDelay.size() + " steps are waiting");
        }
        for (Step step : stepsWithDelay) {
            IStepPlugin plugin = (IStepPlugin) PluginLoader.getPluginByTitle(PluginType.Step, step.getStepPlugin());

            if (plugin instanceof IDelayPlugin) {
                IDelayPlugin delay = (IDelayPlugin) plugin;
                delay.initialize(step, "");
                if (delay.delayIsExhausted()) {
                    LogEntry logEntry = new LogEntry();
                    logEntry.setContent(Helper.getTranslation("blockingDelayIsExhausted"));
                    logEntry.setCreationDate(new Date());
                    logEntry.setProcessId(step.getProzess().getId());
                    logEntry.setType(LogType.DEBUG);
                    logEntry.setUserName("-delay-");

                    ProcessManager.saveLogEntry(logEntry);
                    new HelperSchritte().CloseStepObjectAutomatic(step);
                } else {
                    if (log.isTraceEnabled()) {
                        log.trace(step.getProzess().getTitel() + ": remaining delay is " + delay.getRemainingDelay());
                    }
                }
            }

        }

    }

    public static void main(String[] args) {
        DelayJob dj = new DelayJob();
        dj.execute();
    }

}
