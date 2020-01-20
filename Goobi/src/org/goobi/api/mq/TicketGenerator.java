package org.goobi.api.mq;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

import com.google.gson.Gson;

import de.sub.goobi.config.ConfigurationHelper;
import lombok.extern.log4j.Log4j;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class TicketGenerator {
    private static Gson gson = new Gson();

    public static TaskTicket generateSimpleTicket(String ticketType) {
        TaskTicket tt = new TaskTicket(ticketType);
        return tt;

    }

    public static void submitTicket(TaskTicket ticket, boolean slow) throws JMSException {
        ConfigurationHelper config = ConfigurationHelper.getInstance();

        ConnectionFactory connFactory = new ActiveMQConnectionFactory();
        Connection conn = connFactory.createConnection(config.getMessageBrokerUsername(), config.getMessageBrokerPassword());
        Session sess = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
        final Destination dest = sess.createQueue(slow ? StartQueueBrokerListener.SLOW_QUEUE : StartQueueBrokerListener.FAST_QUEUE);
        MessageProducer producer = sess.createProducer(dest);
        producer.setDeliveryMode(DeliveryMode.PERSISTENT);
        TextMessage message = sess.createTextMessage();
        message.setText(gson.toJson(ticket));
        producer.send(message);

        conn.close();

    }

}
