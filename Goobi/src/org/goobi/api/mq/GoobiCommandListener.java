package org.goobi.api.mq;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.ConfigurationException;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQPrefetchPolicy;
import org.apache.activemq.RedeliveryPolicy;

import com.google.gson.Gson;

import de.sub.goobi.helper.JwtHelper;
import lombok.extern.log4j.Log4j;

@Log4j
public class GoobiCommandListener {
    Gson gson = new Gson();

    private ActiveMQConnection conn;

    public void register(String username, String password) throws JMSException {
        ActiveMQConnectionFactory connFactory = new ActiveMQConnectionFactory();
        conn = (ActiveMQConnection) connFactory.createConnection(username, password);
        ActiveMQPrefetchPolicy prefetchPolicy = new ActiveMQPrefetchPolicy();
        prefetchPolicy.setAll(0);
        conn.setPrefetchPolicy(prefetchPolicy);
        RedeliveryPolicy policy = conn.getRedeliveryPolicy();
        policy.setMaximumRedeliveries(0);
        final Session sess = conn.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        final Destination dest = sess.createQueue(StartQueueBrokerListener.COMMAND_QUEUE);

        final MessageConsumer cons = sess.createConsumer(dest);

        Runnable run = new Runnable() {
            @Override
            public void run() {
                while (true) {
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
                        TaskTicket t = gson.fromJson(strMessage, TaskTicket.class);

                        handleCommandTicket(t);
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

    private void handleCommandTicket(TaskTicket t) {
        String command = t.getProperties().get("command");
        switch (command) {
            case "changeStep":
            case "addToProcessLog":
                String token = t.getProperties().get("JWT");
                Integer stepId = Integer.parseInt(t.getProperties().get("stepId"));
                try {
                    if (JwtHelper.verifyChangeStepToken(token, stepId)) {
                        //TODO: change step or add to process log
                    }
                } catch (ConfigurationException e) {
                    log.error(e);
                }
        }
    }
}
