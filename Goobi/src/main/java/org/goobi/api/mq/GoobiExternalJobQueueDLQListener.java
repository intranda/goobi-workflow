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

import org.goobi.beans.LogEntry;
import org.goobi.beans.Step;
import org.goobi.production.enums.LogType;

import com.google.gson.Gson;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.StepManager;
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
    Gson gson = new Gson();

    private Connection conn;

    public void register(String username, String password) throws JMSException {
        this.conn = ExternalConnectionFactory.createConnection(username, password);
        ConfigurationHelper config = ConfigurationHelper.getInstance();

        final Session sess = conn.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        final Destination dest = sess.createQueue(config.getQueueName(QueueType.EXTERNAL_DL_QUEUE));

        final MessageConsumer cons = sess.createConsumer(dest);

        Runnable run = new Runnable() {
            @Override
            public void run() {
                while (true) { //NOSONAR
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

                        LogEntry logEntry = LogEntry.build(step.getProcessId())
                                .withContent("Script ticket failed after retries")
                                .withCreationDate(new Date())
                                .withType(LogType.ERROR)
                                .withUsername("Goobi DLQ listener");
                        ProcessManager.saveLogEntry(logEntry);

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

}
