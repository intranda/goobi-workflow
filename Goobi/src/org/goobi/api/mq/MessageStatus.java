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
import org.goobi.beans.Step;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.StepManager;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class MessageStatus {

    public static void cancelMessage(Step step) {

        ConfigurationHelper config = ConfigurationHelper.getInstance();

        String messageId = step.getMessageId();
        if (StringUtils.isBlank(messageId)) {
            return;
        }

        Connection conn = null;

        try {
            switch (step.getMessageQueue()) {
                case NONE:
                    return;

                case EXTERNAL_QUEUE:
                    conn = ExternalConnectionFactory.createConnection(config.getMessageBrokerUsername(), config.getMessageBrokerPassword());
                    break;
                case FAST_QUEUE:
                case SLOW_QUEUE:
                    ConnectionFactory connFactory = new ActiveMQConnectionFactory();
                    conn = connFactory.createConnection(config.getMessageBrokerUsername(), config.getMessageBrokerPassword());
                default:
                    break;
            }
            Session session = conn.createSession(false, Session.CLIENT_ACKNOWLEDGE);
            final Destination dest = session.createQueue(config.getQueueName(step.getMessageQueue()));

            //            MessageConsumer consumer = session.createConsumer(dest, "JMSMessageID=" + messageId.replace("ID:", "").replace(":1:1:1:1", "") );

            MessageConsumer consumer = session.createConsumer(dest, "ticketType='" + step.getTitel() + "' AND processid=" +step.getProzess().getId());
            conn.start();
            Message message = consumer.receiveNoWait();
            if (message != null) {
                message.acknowledge();
            }
            conn.close();
        } catch (JMSException e) {
            log.error(e);
        }
        try {
            step.setMessageId(null);
            StepManager.saveStep(step);
        } catch (DAOException e) {
            log.error(e);
        }

    }

}
