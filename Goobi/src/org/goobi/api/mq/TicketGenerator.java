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
import lombok.extern.log4j.Log4j2;

@Log4j2
public class TicketGenerator {
    private static Gson gson = new Gson();

    public static TaskTicket generateSimpleTicket(String ticketType) {
        TaskTicket tt = new TaskTicket(ticketType);
        return tt;

    }

    /**
     * Submits a ticket to the fast or slow queue
     * 
     * @param ticket the ticket to put in the queue
     * @param slowQueue if true, the ticket goes to the StartQueueBrokerListener.SLOW_QUEUE
     * @throws JMSException
     */
    @Deprecated
    public static void submitTicket(TaskTicket ticket, boolean slowQueue) throws JMSException {
        QueueType queueName = slowQueue ? QueueType.SLOW_QUEUE : QueueType.FAST_QUEUE;
        submitTicket(ticket, queueName);
    }

    /**
     * submits an Object to a queue. Be sure that someone really consumes the queue, the argument "queueName" is not checked for sanity
     * 
     * @param ticket
     * @param queueName
     * @throws JMSException
     */
    public static void submitTicket(Object ticket, QueueType queueName) throws JMSException {
        ConfigurationHelper config = ConfigurationHelper.getInstance();

        ConnectionFactory connFactory = new ActiveMQConnectionFactory();
        Connection conn = connFactory.createConnection(config.getMessageBrokerUsername(), config.getMessageBrokerPassword());
        Session sess = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
        final Destination dest = sess.createQueue(queueName.toString());
        MessageProducer producer = sess.createProducer(dest);
        producer.setDeliveryMode(DeliveryMode.PERSISTENT);
        TextMessage message = sess.createTextMessage();
        message.setText(gson.toJson(ticket));
        producer.send(message);

        conn.close();
    }

}
