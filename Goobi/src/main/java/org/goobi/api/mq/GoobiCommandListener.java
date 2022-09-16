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
package org.goobi.api.mq;


import java.util.Date;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.ConfigurationException;

import org.goobi.beans.JournalEntry;
import org.goobi.beans.Step;
import org.goobi.production.enums.LogType;

import com.google.gson.Gson;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.HelperSchritte;
import de.sub.goobi.helper.JwtHelper;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.ExternalMQManager;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.StepManager;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GoobiCommandListener {
    Gson gson = new Gson();

    private Connection conn;

    public void register(String username, String password) throws JMSException {
        this.conn = ExternalConnectionFactory.createConnection(username, password);
        ConfigurationHelper config = ConfigurationHelper.getInstance();

        final Session sess = conn.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        final Destination dest = sess.createQueue(config.getQueueName(QueueType.COMMAND_QUEUE));

        final MessageConsumer cons = sess.createConsumer(dest);

        Runnable run = new Runnable() {
            @Override
            public void run() {
                while (true) { //NOSONAR, no abort condition is needed
                    try {
                        Message message = cons.receive();
                        // check command and if the token allows this.
                        String strMessage = null;
                        if (message instanceof TextMessage) {
                            TextMessage tm = (TextMessage) message;
                            strMessage = tm.getText();
                        }
                        if (message instanceof BytesMessage) {
                            BytesMessage bm = (BytesMessage) message;
                            byte[] bytes = new byte[(int) bm.getBodyLength()];
                            bm.readBytes(bytes);
                            strMessage = new String(bytes);
                        }
                        CommandTicket t = gson.fromJson(strMessage, CommandTicket.class);

                        handleCommandTicket(t);
                        message.acknowledge();
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        log.error(e);
                    }
                }
            }
        };
        Thread t = new Thread(run);
        t.setDaemon(true);

        conn.start();
        t.start();
    }

    private void handleCommandTicket(CommandTicket t) {
        String command = t.getCommand();
        String token = t.getJwt();
        Integer stepId = t.getStepId();
        Integer processId = t.getProcessId();
        Step step = StepManager.getStepById(stepId);
        if (step == null) {
            log.error("GoobiCommandListener: Could not find step with ID " + stepId);
            return;
        }
        switch (command) {
            case "changeStep":
                try {
                    if (JwtHelper.verifyChangeStepToken(token, stepId)) {
                        // change step
                        String newStatus = t.getNewStatus();
                        switch (newStatus) {
                            case "error":
                                step.setBearbeitungsstatusEnum(StepStatus.ERROR);
                                step.setBearbeitungszeitpunkt(new Date());
                                step.setBearbeitungsende(step.getBearbeitungszeitpunkt());
                                StepManager.saveStep(step);
                                break;
                            case "done":
                                // Write to DB with date.
                                for (String scriptName : t.getScriptNames()) {
                                    ExternalMQManager.insertResult(new ExternalCommandResult(t.getProcessId(), t.getStepId(), scriptName));
                                }
                                new HelperSchritte().CloseStepObjectAutomatic(step);
                                break;
                            case "paused":
                                // Step was paused when the workernode tried to run it. Persist this to schritte table
                                StepManager.setStepPaused(stepId, true);
                                break;
                        }
                    }
                } catch (ConfigurationException | DAOException e) {
                    log.error(e);
                }
                break;
            case "addToProcessLog":
            case "addToProcessJournal":
                try {
                    if (JwtHelper.verifyChangeStepToken(token, stepId)) {
                        // add to process log
                        JournalEntry entry = JournalEntry.build(processId)
                                .withCreationDate(new Date())
                                .withType(LogType.getByTitle(t.getLogType()))
                                .withUsername(t.getIssuer())
                                .withContent(t.getContent());
                        ProcessManager.saveLogEntry(entry);
                    }
                } catch (ConfigurationException e) {
                    log.error(e);
                }
                break;
            default:
                break;
        }
    }
}
