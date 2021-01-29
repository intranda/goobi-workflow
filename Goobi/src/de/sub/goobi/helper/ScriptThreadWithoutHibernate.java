package de.sub.goobi.helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- https://goobi.io
 * 			- https://www.intranda.com
 * 			- https://github.com/intranda/goobi-workflow
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
import java.util.List;
import java.util.Map.Entry;

import javax.jms.JMSException;
import javax.naming.ConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.goobi.api.mq.ExternalScriptTicket;
import org.goobi.api.mq.GenericAutomaticStepHandler;
import org.goobi.api.mq.QueueType;
import org.goobi.api.mq.TaskTicket;
import org.goobi.api.mq.TicketGenerator;
import org.goobi.beans.LogEntry;
import org.goobi.beans.Step;
import org.goobi.production.enums.LogType;
import org.goobi.production.enums.PluginReturnValue;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IDelayPlugin;
import org.goobi.production.plugin.interfaces.IStepPlugin;
import org.goobi.production.plugin.interfaces.IStepPluginVersion2;

import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.managers.ProcessManager;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.WriteException;

public class ScriptThreadWithoutHibernate extends Thread {
    HelperSchritte hs = new HelperSchritte();
    private Step step;
    public String rueckgabe = "";
    public boolean stop = false;
    private static final Logger logger = LogManager.getLogger(ScriptThreadWithoutHibernate.class);

    public ScriptThreadWithoutHibernate(Step step) {
        this.step = step;
        setDaemon(true);
    }

    public void startOrPutToQueue() {
        if (this.step.getMessageQueue() == QueueType.EXTERNAL_QUEUE) {
            // check if this is a script-step and has no additional plugin set
            if (!this.step.getAllScriptPaths().isEmpty() && StringUtils.isBlank(this.step.getStepPlugin())) {
                // put this to the external queue and continue
                addStepScriptsToExternalQueue(this.step);
                return;
            }
        }
        if (this.step.getMessageQueue() == QueueType.SLOW_QUEUE || this.step.getMessageQueue() == QueueType.FAST_QUEUE) {
            TaskTicket t = new TaskTicket(GenericAutomaticStepHandler.HANDLERNAME);
            t.setStepId(this.step.getId());
            t.setProcessId(this.step.getProzess().getId());
            t.setStepName(this.step.getTitel());
            try {
                TicketGenerator.submitInternalTicket(t, this.step.getMessageQueue(), step.getTitel(), step.getProzess().getId());
            } catch (JMSException e) {
                this.step.setBearbeitungsstatusEnum(StepStatus.ERROR);
                logger.error("Error adding TaskTicket to queue: ", e);
                LogEntry errorEntry = LogEntry.build(this.step.getProcessId())
                        .withType(LogType.ERROR)
                        .withContent("Error reading metadata for step" + this.step.getTitel())
                        .withCreationDate(new Date())
                        .withUsername("automatic");
                ProcessManager.saveLogEntry(errorEntry);
            }
        } else {
            this.start();
        }
    }

    @Override
    public void run() {

        boolean automatic = this.step.isTypAutomatisch();
        List<String> scriptPaths = step.getAllScriptPaths();
        if (logger.isDebugEnabled()) {
            logger.debug("step is automatic: " + automatic);
            logger.debug("found " + scriptPaths.size() + " scripts");
        }
        if (step.getTypScriptStep() && !scriptPaths.isEmpty()) {
            this.hs.executeAllScriptsForStep(this.step, automatic);
        } else if (this.step.isTypExportDMS()) {
            this.hs.executeDmsExport(this.step, automatic);
        } else if (this.step.isDelayStep() && this.step.getStepPlugin() != null && !this.step.getStepPlugin().isEmpty()) {
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

    public void stopThread() {
        this.rueckgabe = "Import wurde wegen Zeitüberschreitung abgebrochen";
        this.stop = true;
    }

    public void addStepScriptsToExternalQueue(Step automaticStep) {
        ExternalScriptTicket t = new ExternalScriptTicket();
        t.setStepId(automaticStep.getId());
        t.setProcessId(automaticStep.getProzess().getId());
        t.setStepName(automaticStep.getTitel());
        // put all scriptPaths to properties (with replaced Goobi-variables!)
        List<List<String>> listOfScripts = new ArrayList<List<String>>();
        List<String> scriptNames = new ArrayList<String>();
        for (Entry<String, String> entry : automaticStep.getAllScripts().entrySet()) {
            String script = entry.getValue();
            try {
                scriptNames.add(entry.getKey());
                List<String> params = HelperSchritte.createShellParamsForBashScript(automaticStep, script);
                listOfScripts.add(params);
            } catch (PreferencesException | ReadException | WriteException | IOException | InterruptedException | SwapException
                    | DAOException e) {
                logger.error("error trying to put script-step to external queue: ", e);
            }
        }
        try {
            t.setJwt(JwtHelper.createChangeStepToken(automaticStep));
        } catch (ConfigurationException e) {
            logger.error(e);
        }
        t.setScripts(listOfScripts);
        t.setScriptNames(scriptNames);
        try {
            TicketGenerator.submitExternalTicket(t, QueueType.EXTERNAL_QUEUE, step.getTitel(), step.getProzess().getId());
        } catch (JMSException e) {
            automaticStep.setBearbeitungsstatusEnum(StepStatus.ERROR);
            logger.error("Error adding TaskTicket to queue: ", e);
            LogEntry errorEntry = LogEntry.build(this.step.getProcessId())
                    .withType(LogType.ERROR)
                    .withContent("Error reading metadata for step" + this.step.getTitel())
                    .withCreationDate(new Date())
                    .withUsername("automatic");
            ProcessManager.saveLogEntry(errorEntry);
        }
    }

}