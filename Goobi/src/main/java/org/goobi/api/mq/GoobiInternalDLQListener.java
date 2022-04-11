package org.goobi.api.mq;

import java.util.Date;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQPrefetchPolicy;
import org.apache.activemq.RedeliveryPolicy;
import org.goobi.api.mq.MqStatusMessage.MessageStatus;

import de.sub.goobi.persistence.managers.MQResultManager;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GoobiInternalDLQListener {
    private ActiveMQConnection conn;

    public void register(String username, String password, QueueType queue) throws JMSException {
        ActiveMQConnectionFactory connFactory = new ActiveMQConnectionFactory("vm://localhost");
        conn = (ActiveMQConnection) connFactory.createConnection(username, password);
        ActiveMQPrefetchPolicy prefetchPolicy = new ActiveMQPrefetchPolicy();
        prefetchPolicy.setAll(0);
        conn.setPrefetchPolicy(prefetchPolicy);
        RedeliveryPolicy policy = conn.getRedeliveryPolicy();
        policy.setMaximumRedeliveries(0);
        final Session sess = conn.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        final Destination dest = sess.createQueue(queue.toString());

        final MessageConsumer cons = sess.createConsumer(dest);

        Runnable run = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Message message = cons.receive();
                        // write to DB that there was a poison ACK for this JMS message ID
                        String id = message.getJMSMessageID();
                        String origMessage = null;
                        if (message instanceof TextMessage) {
                            TextMessage tm = (TextMessage) message;
                            origMessage = tm.getText();
                        }
                        if (message instanceof BytesMessage) {
                            BytesMessage bm = (BytesMessage) message;
                            byte[] bytes = new byte[(int) bm.getBodyLength()];
                            bm.readBytes(bytes);
                            origMessage = new String(bytes);
                        }
                        MqStatusMessage statusMessage =
                                new MqStatusMessage(id, new Date(), MessageStatus.ERROR_DLQ, "Message failed after retries.", origMessage);
                        MQResultManager.insertResult(statusMessage);
                    } catch (JMSException e) {
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
