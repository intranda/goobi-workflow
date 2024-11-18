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

import java.util.Arrays;
import java.util.UUID;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ScheduledMessage;

import com.google.gson.Gson;

import de.sub.goobi.config.ConfigurationHelper;
import jakarta.jms.Connection;
import jakarta.jms.DeliveryMode;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;

public class TicketGenerator {
    private static Gson gson = new Gson();

    public static TaskTicket generateSimpleTicket(String ticketType) {
        return new TaskTicket(ticketType);
    }

    /**
     * Submits a ticket to the fast or slow queue
     * 
     * @deprecated This method is replaced by other methods with different parameters
     * 
     * @param ticket the ticket to put in the queue
     * @param slowQueue if true, the ticket goes to the StartQueueBrokerListener.SLOW_QUEUE
     * @return id of the generated message
     * @throws JMSException
     */
    @Deprecated(since = "23.05", forRemoval = true)
    public static String submitTicket(TaskTicket ticket, boolean slowQueue, String ticketType, Integer processid) throws JMSException {
        QueueType queueName = slowQueue ? QueueType.SLOW_QUEUE : QueueType.FAST_QUEUE;
        return submitInternalTicket(ticket, queueName, ticketType, processid);
    }

    /**
     * submits an Object to an internal queue. Be sure that someone really consumes the queue, the argument "queueType" is not checked for sanity
     * 
     * @param ticket
     * @param queueType
     * @return id of the generated message
     * @throws JMSException
     */
    public static String submitInternalTicket(Object ticket, QueueType queueType, String ticketType, Integer processid) throws JMSException {
        return submitInternalTicketWithDelay(ticket, queueType, ticketType, processid, 0);
    }

    /**
     * submits an Object to an internal queue. Be sure that someone really consumes the queue, the argument "queueType" is not checked for sanity. The
     * delay is given in seconds
     * 
     * @param ticket
     * @param queueType
     * @return id of the generated message
     * @throws JMSException
     */
    public static String submitInternalTicketWithDelay(Object ticket, QueueType queueType, String ticketType, Integer processid, Integer delay)
            throws JMSException {
        ConfigurationHelper config = ConfigurationHelper.getInstance();

        ActiveMQConnectionFactory connFactory = new ActiveMQConnectionFactory();
        connFactory.setTrustedPackages(Arrays.asList("org.goobi.managedbeans", "org.goobi.api.mq", "org.goobi.api.mq.ticket"));

        Connection conn = connFactory.createConnection(config.getMessageBrokerUsername(), config.getMessageBrokerPassword());
        String messageId = submitTicket(ticket, config.getQueueName(queueType), conn, ticketType, processid, delay);
        conn.close();
        return messageId;
    }

    /**
     * submits an Object to an external queue. Be sure that someone really consumes the queue, the argument "queueType" is not checked for sanity. The
     * delay is given in seconds
     * 
     * @param ticket
     * @param queueType
     * @return id of the generated message
     * @throws JMSException
     */
    public static String submitExternalTicket(Object ticket, QueueType queueType, String ticketType, Integer processid) throws JMSException {
        return submitExternalTicketWithDelay(ticket, queueType, ticketType, processid, 0);
    }

    /**
     * submits an Object to an external queue. Be sure that someone really consumes the queue, the argument "queueType" is not checked for sanity
     * 
     * @param ticket
     * @param queueType
     * @return id of the generated message
     * @throws JMSException
     */
    public static String submitExternalTicketWithDelay(Object ticket, QueueType queueType, String ticketType, Integer processid, Integer delay)
            throws JMSException {
        ConfigurationHelper config = ConfigurationHelper.getInstance();

        Connection conn = ExternalConnectionFactory.createConnection(config.getMessageBrokerUsername(), config.getMessageBrokerPassword());
        String messageId = submitTicket(ticket, config.getQueueName(queueType), conn, ticketType, processid, delay);
        conn.close();
        return messageId;
    }

    private static String submitTicket(Object ticket, String queueName, Connection conn, String ticketType, Integer processid, Integer delay)
            throws JMSException {
        TextMessage message = null;
        try (Session sess = conn.createSession(false, Session.AUTO_ACKNOWLEDGE)) {
            final Destination dest = sess.createQueue(queueName);
            try (MessageProducer producer = sess.createProducer(dest)) {
                producer.setDeliveryMode(DeliveryMode.PERSISTENT);
                message = sess.createTextMessage();
                // we set a random UUID here, because otherwise tickets will not be processed in parallel in an SQS fifo queue.
                // we still need a fifo queue for message deduplication, though.
                // See: https://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/sqs-additional-fifo-queue-recommendations.html
                message.setStringProperty("JMSXGroupID", UUID.randomUUID().toString());
                message.setText(gson.toJson(ticket));
                message.setStringProperty("JMSType", ticketType);
                message.setIntProperty("processid", processid);

                // add a delay, given in seconds
                if (delay > 0) {
                    long seconds = delay * 1000l;
                    message.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, seconds);
                }

                producer.send(message);
            }
        }
        return message.getJMSMessageID();
    }

}
