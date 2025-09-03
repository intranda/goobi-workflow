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

import org.goobi.beans.JournalEntry;
import org.goobi.beans.JournalEntry.EntryType;
import org.goobi.beans.Step;
import org.goobi.production.enums.LogType;

import com.google.gson.Gson;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.JournalManager;
import de.sub.goobi.persistence.managers.StepManager;
import jakarta.jms.BytesMessage;
import jakarta.jms.Connection;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import lombok.extern.log4j.Log4j2;

/**
 * This class listens to the dead letter queue of the queue for external script tickets. Every ticket that appears in that queue is an
 * ExternalScriptTicket. When a ticket appears in the DLQ, the correpsonding step will be set to an error state.
 * 
 * @author Oliver Paetzel
 *
 */
@Log4j2
public class GoobiExternalJobQueueDLQListener {
    private Gson gson = new Gson();
    private Connection conn;
    private Thread thread;
    private Session sess;
    private MessageConsumer cons;

    public void register(String username, String password) throws JMSException {
        conn = ExternalConnectionFactory.createConnection(username, password);
        ConfigurationHelper config = ConfigurationHelper.getInstance();
        sess = conn.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        final Destination dest = sess.createQueue(config.getQueueName(QueueType.EXTERNAL_DL_QUEUE));

        cons = sess.createConsumer(dest);

        Runnable run = () -> {
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
                    ExternalScriptTicket t = gson.fromJson(strMessage, ExternalScriptTicket.class);
                    Step step = StepManager.getStepById(t.getStepId());
                    step.setBearbeitungsstatusEnum(StepStatus.ERROR);
                    step.setBearbeitungsende(new Date());
                    StepManager.saveStep(step);

                    JournalEntry logEntry = new JournalEntry(step.getProcessId(), new Date(), "Goobi DLQ listener", LogType.ERROR,
                            "Script ticket failed after retries", EntryType.PROCESS);
                    JournalManager.saveJournalEntry(logEntry);

                    message.acknowledge();
                } catch (JMSException | DAOException e) {
                    log.error(e);
                }
            }
        };
        thread = new Thread(run);
        thread.setDaemon(true);

        conn.start();
        thread.start();

    }

    public void close() throws JMSException {
        cons.close();
        sess.close();
        this.conn.close();
        try {
            thread.join(1000);
        } catch (InterruptedException e) {
            log.error(e);
            Thread.currentThread().interrupt();
        }
    }
}
