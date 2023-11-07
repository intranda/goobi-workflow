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
package org.goobi.managedbeans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.inject.Named;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.lang3.StringUtils;
import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.goobi.api.mq.TaskTicket;

import com.google.gson.Gson;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.persistence.managers.MQResultManager;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 * 
 * This bean can be used to display the current state of the goobi_fast and goobi_slow queues. The bean provides methods to show all active tickets
 * and remove a ticket or clear the queue.
 *
 */

@Named
@WindowScoped
@Log4j2
public class MessageQueueBean extends BasicBean implements Serializable {

    private static final long serialVersionUID = 9201515793444130154L;

    private transient Gson gson = new Gson();

    private transient ActiveMQConnection connection;
    private transient QueueSession queueSession;

    @Getter
    @Setter
    private String mode = "waiting";

    @Getter
    private boolean messageBrokerStart;

    @Getter
    @Setter
    private String messageType;

    @Getter
    @Setter
    private String queueType;

    public MessageQueueBean() {
        this.initMessageBrokerStart();

        if (this.messageBrokerStart) {

            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
            connectionFactory.setTrustedPackages(Arrays.asList("org.goobi.managedbeans", "org.goobi.api.mq", "org.goobi.api.mq.ticket"));

            try {
                connection = (ActiveMQConnection) connectionFactory.createConnection(ConfigurationHelper.getInstance().getMessageBrokerUsername(),
                        ConfigurationHelper.getInstance().getMessageBrokerPassword());
                queueSession = connection.createQueueSession(false, Session.CLIENT_ACKNOWLEDGE);
            } catch (JMSException e) {
                log.error(e);
            }
            paginator = new DatabasePaginator(null, null, new MQResultManager(), "queue.xhtml");
        }
    }

    public Map<String, Integer> getFastQueueContent() {
        Map<String, Integer> fastQueueContent = new TreeMap<>();
        if (ConfigurationHelper.getInstance().isStartInternalMessageBroker()) {
            try {
                connection.start();
                Queue queue = queueSession.createQueue("goobi_fast");
                try (QueueBrowser browser = queueSession.createBrowser(queue)) {
                    Enumeration<?> messagesInQueue = browser.getEnumeration();
                    while (messagesInQueue.hasMoreElements()) {
                        ActiveMQTextMessage queueMessage = (ActiveMQTextMessage) messagesInQueue.nextElement();

                        String type = queueMessage.getStringProperty("JMSType");
                        if (fastQueueContent.containsKey(type)) {
                            fastQueueContent.put(type, fastQueueContent.get(type) + 1);
                        } else {
                            fastQueueContent.put(type, 1);
                        }
                    }
                }
                connection.stop();
            } catch (JMSException e) {
                log.error(e);
            }
        }
        return fastQueueContent;
    }

    public Map<String, Integer> getSlowQueueContent() {
        Map<String, Integer> fastQueueContent = new TreeMap<>();
        if (ConfigurationHelper.getInstance().isStartInternalMessageBroker()) {
            try {
                connection.start();
                Queue queue = queueSession.createQueue("goobi_slow");
                try (QueueBrowser browser = queueSession.createBrowser(queue)) {
                    Enumeration<?> messagesInQueue = browser.getEnumeration();
                    while (messagesInQueue.hasMoreElements()) {
                        ActiveMQTextMessage queueMessage = (ActiveMQTextMessage) messagesInQueue.nextElement();

                        String type = queueMessage.getStringProperty("JMSType");
                        if (fastQueueContent.containsKey(type)) {
                            fastQueueContent.put(type, fastQueueContent.get(type) + 1);
                        } else {
                            fastQueueContent.put(type, 1);
                        }
                    }
                }
                connection.stop();
            } catch (JMSException e) {
                log.error(e);
            }
        }
        return fastQueueContent;
    }

    public void initMessageBrokerStart() {
        this.messageBrokerStart = ConfigurationHelper.getInstance().isStartInternalMessageBroker();
    }

    /**
     * Delete a single message from the goobi_slow queue
     * 
     * @param ticket to delete
     */

    /**
     * Get a list of all active messages in the goobi_slow queue
     */

    public List<TaskTicket> getActiveSlowQueryMesssages() {

        List<TaskTicket> answer = new ArrayList<>();
        if (!ConfigurationHelper.getInstance().isStartInternalMessageBroker()) {
            return answer;
        }
        if (StringUtils.isNotBlank(messageType)) {
            try {
                connection.start();
                Queue queue = queueSession.createQueue("goobi_slow");
                try (QueueBrowser browser = queueSession.createBrowser(queue, "JMSType = '" + messageType + "'")) {
                    Enumeration<?> messagesInQueue = browser.getEnumeration();
                    while (messagesInQueue.hasMoreElements() && answer.size() < 100) {
                        ActiveMQTextMessage queueMessage = (ActiveMQTextMessage) messagesInQueue.nextElement();
                        TaskTicket ticket = gson.fromJson(queueMessage.getText(), TaskTicket.class);
                        ticket.setMessageId(queueMessage.getJMSMessageID());

                        answer.add(ticket);
                    }
                }
                connection.stop();
            } catch (JMSException e) {
                log.error(e);
            }
        }
        return answer;
    }

    /**
     * Remove all active messages of a given type from the queue
     * 
     */
    public void clearQueue() {
        if (StringUtils.isNotBlank(messageType)) {
            try {
                Queue queue = queueSession.createQueue(queueType);
                try (MessageConsumer consumer = queueSession.createConsumer(queue, "JMSType='" + messageType + "'")) {
                    connection.start();
                    Message message = consumer.receiveNoWait();
                    while (message != null) {
                        message.acknowledge();
                        message = consumer.receiveNoWait();
                        // TODO get step, set step status to open
                        // TODO write cancel message to process log

                    }
                }
                connection.stop();
            } catch (JMSException e) {
                log.error(e);
            }
        }

    }

    /**
     * Get a list of all active messages in the goobi_fast queue
     */

    public List<TaskTicket> getActiveFastQueryMesssages() {
        List<TaskTicket> answer = new ArrayList<>();
        if (!ConfigurationHelper.getInstance().isStartInternalMessageBroker()) {
            return answer;
        }
        if (StringUtils.isNotBlank(messageType)) {
            try {
                connection.start();
                Queue queue = queueSession.createQueue("goobi_fast");

                try (QueueBrowser browser = queueSession.createBrowser(queue, "JMSType = '" + messageType + "'")) {
                    Enumeration<?> messagesInQueue = browser.getEnumeration();
                    // get up to 100 messages
                    while (messagesInQueue.hasMoreElements() && answer.size() < 100) {
                        ActiveMQTextMessage queueMessage = (ActiveMQTextMessage) messagesInQueue.nextElement();
                        TaskTicket ticket = gson.fromJson(queueMessage.getText(), TaskTicket.class);
                        ticket.setMessageId(queueMessage.getJMSMessageID());

                        answer.add(ticket);
                    }
                }
                connection.stop();
            } catch (JMSException e) {
                log.error(e);
            }
        }

        return answer;
    }

    /**
     * Delete a single message from the queue
     * 
     * @param ticket
     */

    public void deleteMessage(TaskTicket ticket) {
        try {
            connection.start();
            Queue queue = queueSession.createQueue(queueType);
            try (QueueReceiver receiver = queueSession.createReceiver(queue, "JMSMessageID='" + ticket.getMessageId() + "'")) {
                Message message = receiver.receiveNoWait();

                if (message != null) {
                    message.acknowledge();
                }
            }
            connection.stop();

            // TODO get step, set step status to open
            // TODO write cancel message to process log
        } catch (JMSException e) {
            log.error(e);
        }
    }

}
