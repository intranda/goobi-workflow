package org.goobi.api.mq;

import java.util.UUID;

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
    public static void submitTicket(TaskTicket ticket, boolean slowQueue, String ticketType, Integer processid) throws JMSException {
        QueueType queueName = slowQueue ? QueueType.SLOW_QUEUE : QueueType.FAST_QUEUE;
        submitInternalTicket(ticket, queueName, ticketType, processid);
    }

    /**
     * submits an Object to an internal queue. Be sure that someone really consumes the queue, the argument "queueType" is not checked for sanity
     * 
     * @param ticket
     * @param queueType
     * @throws JMSException
     */
    public static void submitInternalTicket(Object ticket, QueueType queueType, String ticketType, Integer processid) throws JMSException {
        ConfigurationHelper config = ConfigurationHelper.getInstance();

        ConnectionFactory connFactory = new ActiveMQConnectionFactory();
        Connection conn = connFactory.createConnection(config.getMessageBrokerUsername(), config.getMessageBrokerPassword());
        submitTicket(ticket, config.getQueueName(queueType), conn, ticketType, processid);

        conn.close();
    }

    /**
     * submits an Object to an external queue. Be sure that someone really consumes the queue, the argument "queueType" is not checked for sanity
     * 
     * @param ticket
     * @param queueType
     * @throws JMSException
     */
    public static void submitExternalTicket(Object ticket, QueueType queueType, String ticketType, Integer processid) throws JMSException {
        ConfigurationHelper config = ConfigurationHelper.getInstance();

        Connection conn = ExternalConnectionFactory.createConnection(config.getMessageBrokerUsername(), config.getMessageBrokerPassword());
        submitTicket(ticket, config.getQueueName(queueType), conn, ticketType, processid);

        conn.close();
    }

    private static void submitTicket(Object ticket, String queueName, Connection conn, String ticketType, Integer processid) throws JMSException {
        Session sess = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
        final Destination dest = sess.createQueue(queueName);
        MessageProducer producer = sess.createProducer(dest);
        producer.setDeliveryMode(DeliveryMode.PERSISTENT);
        TextMessage message = sess.createTextMessage();
        // we set a random UUID here, because otherwise tickets will not be processed in parallel in an SQS fifo queue.
        // we still need a fifo queue for message deduplication, though.
        // See: https://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/sqs-additional-fifo-queue-recommendations.html
        message.setStringProperty("JMSXGroupID", UUID.randomUUID().toString());
        message.setText(gson.toJson(ticket));
        message.setStringProperty("ticketType", ticketType);
        message.setIntProperty("processid", processid);
        producer.send(message);
    }

}
