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
package de.sub.goobi.helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import javax.jms.JMSException;
import javax.naming.ConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.goobi.api.mq.ExternalScriptTicket;
import org.goobi.api.mq.GenericAutomaticStepHandler;
import org.goobi.api.mq.QueueType;
import org.goobi.api.mq.TaskTicket;
import org.goobi.api.mq.TicketGenerator;
import org.goobi.beans.JournalEntry;
import org.goobi.beans.JournalEntry.EntryType;
import org.goobi.beans.Step;
import org.goobi.managedbeans.JobTypesCache;
import org.goobi.production.enums.LogType;
import org.goobi.production.enums.PluginReturnValue;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IDelayPlugin;
import org.goobi.production.plugin.interfaces.IStepPlugin;
import org.goobi.production.plugin.interfaces.IStepPluginVersion2;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.managers.JournalManager;
import de.sub.goobi.persistence.managers.StepManager;
import lombok.extern.log4j.Log4j2;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.WriteException;

@Log4j2
public class ScriptThreadWithoutHibernate extends Thread {
    private JobTypesCache jobTypesCache;
    HelperSchritte hs = new HelperSchritte();
    private Step step;
    public String rueckgabe = "";
    public boolean cancel = false;

    public ScriptThreadWithoutHibernate(Step step) {
        this.step = step;
        this.jobTypesCache = Helper.getBeanByClass(JobTypesCache.class);
        setDaemon(true);
    }

    public void startOrPutToQueue() {
        //first check if this step might be paused
        if (jobTypesCache.isStepPaused(this.step.getTitel())) {
            try {
                StepManager.setStepPaused(this.step.getId(), true);
                return;
            } catch (DAOException e) {
                log.error(e);
            }
        }
        if (!step.getProzess().isPauseAutomaticExecution()) {

            QueueType queueType = this.step.getMessageQueue();
            if (queueType == QueueType.EXTERNAL_QUEUE && !this.step.getAllScriptPaths().isEmpty() && StringUtils.isBlank(this.step.getStepPlugin())) {
                // check if this is a script-step and has no additional plugin set
                // put this to the external queue and continue
                addStepScriptsToExternalQueue(this.step);
                return;
            }
            if (queueType == QueueType.SLOW_QUEUE || queueType == QueueType.FAST_QUEUE) {
                if (!ConfigurationHelper.getInstance().isStartInternalMessageBroker()) {
                    this.step.setBearbeitungsstatusEnum(StepStatus.ERROR);
                    String message = "Step '" + this.step.getTitel() + "' should be executed in a message queue but message queues are switched off.";
                    Helper.addMessageToProcessJournal(this.step.getProzess().getId(), LogType.ERROR, message);
                    log.error(message);
                    try {
                        StepManager.saveStep(this.step);
                    } catch (DAOException daoe) {
                        message = "An exception occurred while saving the error status for an automatic step for process with ID ";
                        log.error(message + this.step.getProzess().getId(), daoe);
                    }
                    return;
                }
                TaskTicket t = new TaskTicket(GenericAutomaticStepHandler.HANDLERNAME);
                t.setStepId(this.step.getId());
                t.setProcessId(this.step.getProzess().getId());
                t.setStepName(this.step.getTitel());
                try {
                    TicketGenerator.submitInternalTicket(t, queueType, step.getTitel(), step.getProzess().getId());
                    step.setBearbeitungsstatusEnum(StepStatus.INFLIGHT);
                    step.setBearbeitungsbeginn(new Date());
                    StepManager.saveStep(step);
                } catch (JMSException | DAOException e) {
                    this.step.setBearbeitungsstatusEnum(StepStatus.ERROR);
                    try {
                        StepManager.saveStep(this.step);
                    } catch (DAOException e1) {
                        log.error(e1);
                    }
                    log.error("Error adding TaskTicket to queue: ", e);

                    String message = "Error reading metadata for step" + this.step.getTitel();
                    JournalEntry errorEntry =
                            new JournalEntry(step.getProcessId(), new Date(), "automatic", LogType.ERROR, message, EntryType.PROCESS);

                    JournalManager.saveJournalEntry(errorEntry);
                }
            } else {
                this.start();
            }
        }
    }

    @Override
    public void run() {

        boolean automatic = this.step.isTypAutomatisch();
        List<String> scriptPaths = step.getAllScriptPaths();
        if (log.isDebugEnabled()) {
            log.debug("step is automatic: " + automatic);
            log.debug("found " + scriptPaths.size() + " scripts");
        }
        if (step.isTypScriptStep() && !scriptPaths.isEmpty()) {
            this.hs.executeAllScriptsForStep(this.step, automatic);
        } else if (this.step.isTypExportDMS()) {
            this.hs.executeDmsExport(this.step, automatic);
        } else {
            if (step.getProzess().isPauseAutomaticExecution()) {
                return;
            }
            if (this.step.isDelayStep() && this.step.getStepPlugin() != null && !this.step.getStepPlugin().isEmpty()) {
                IDelayPlugin idp = (IDelayPlugin) PluginLoader.getPluginByTitle(PluginType.Step, step.getStepPlugin());
                idp.initialize(step, "");
                if (idp.execute()) {
                    hs.CloseStepObjectAutomatic(step);
                }
            } else if (this.step.getStepPlugin() != null && !this.step.getStepPlugin().isEmpty()) {
                IStepPlugin isp = (IStepPlugin) PluginLoader.getPluginByTitle(PluginType.Step, step.getStepPlugin());
                isp.initialize(step, "");

                if (isp instanceof IStepPluginVersion2) {
                    IStepPluginVersion2 plugin = (IStepPluginVersion2) isp;
                    PluginReturnValue val = plugin.run();
                    if (val == PluginReturnValue.FINISH) {
                        hs.CloseStepObjectAutomatic(step);
                    } else if (val == PluginReturnValue.ERROR) {
                        hs.errorStep(step);
                    } else if (val == PluginReturnValue.WAIT) {
                        // stay in status inwork
                    }

                } else {
                    if (isp.execute()) {
                        hs.CloseStepObjectAutomatic(step);
                    } else {
                        hs.errorStep(step);
                    }
                }
            } else if (this.step.isHttpStep()) {
                this.hs.runHttpStep(this.step);
            }
        }
    }

    public void stopThread() {
        this.rueckgabe = "Import wurde wegen Zeitüberschreitung abgebrochen";
        this.cancel = true;
    }

    public void addStepScriptsToExternalQueue(Step automaticStep) {
        ExternalScriptTicket t = new ExternalScriptTicket();
        t.setStepId(automaticStep.getId());
        t.setProcessId(automaticStep.getProzess().getId());
        t.setStepName(automaticStep.getTitel());
        // put all scriptPaths to properties (with replaced Goobi-variables!)
        List<List<String>> listOfScripts = new ArrayList<>();
        List<String> scriptNames = new ArrayList<>();
        int counter = 0;
        for (Entry<String, String> entry : automaticStep.getAllScripts().entrySet()) {
            counter++;
            String script = entry.getValue();
            try {
                String scriptName = entry.getKey();
                if (scriptName == null) {
                    scriptNames.add("Script " + counter);
                } else {
                    scriptNames.add(entry.getKey());
                }
                List<String> params = HelperSchritte.createShellParamsForBashScript(automaticStep, script);
                listOfScripts.add(params);
            } catch (PreferencesException | ReadException | WriteException | IOException | SwapException | DAOException e) {
                log.error("error trying to put script-step to external queue: ", e);
                return;
            }
        }
        try {
            t.setJwt(JwtHelper.createChangeStepToken(automaticStep));
            t.setRestJwt(JwtHelper.createApiToken("/stepspaused/" + automaticStep.getTitel(), new String[] { "GET" }));
        } catch (ConfigurationException e) {
            log.error(e);
        }
        t.setScripts(listOfScripts);
        t.setScriptNames(scriptNames);
        try {

            TicketGenerator.submitExternalTicket(t, QueueType.EXTERNAL_QUEUE, step.getTitel(), step.getProzess().getId());
            automaticStep.setBearbeitungsbeginn(new Date());
            automaticStep.setBearbeitungsstatusEnum(StepStatus.INFLIGHT);
            StepManager.saveStep(automaticStep);
        } catch (JMSException | DAOException e) {
            automaticStep.setBearbeitungsstatusEnum(StepStatus.ERROR);
            try {
                StepManager.saveStep(automaticStep);
            } catch (DAOException e1) {
                log.error(e1);
            }
            log.error("Error adding TaskTicket to queue: ", e);
            JournalEntry errorEntry = new JournalEntry(step.getProcessId(), new Date(), "automatic", LogType.ERROR,
                    "Error trying to put script-step to external queue: " + this.step.getTitel(), EntryType.PROCESS);
            JournalManager.saveJournalEntry(errorEntry);
        }
    }

}