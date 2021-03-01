package org.goobi.api.mq;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.lang.StringUtils;

import de.sub.goobi.config.ConfigurationHelper;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class MessageStatus {

    public static void cancelMessage(String messageId, String queueType, String stepName, int processid) {

        ConfigurationHelper config = ConfigurationHelper.getInstance();

        if (StringUtils.isBlank(messageId)) {
            return;
        }

        Connection conn = null;

        try {

            ConnectionFactory connFactory = new ActiveMQConnectionFactory();
            conn = connFactory.createConnection(config.getMessageBrokerUsername(), config.getMessageBrokerPassword());


            Session session = conn.createSession(false, Session.CLIENT_ACKNOWLEDGE);
            final Destination dest = session.createQueue(config.getQueueName(QueueType.getByName(queueType)));

            //            MessageConsumer consumer = session.createConsumer(dest, "JMSMessageID=" + messageId.replace("ID:", "").replace(":1:1:1:1", "") );

            MessageConsumer consumer = session.createConsumer(dest, "ticketType='" + stepName + "' AND processid=" +processid);
            conn.start();
            Message message = consumer.receiveNoWait();
            if (message != null) {
                message.acknowledge();
            }
            conn.close();
        } catch (JMSException e) {
            log.error(e);
        }
    }

}
