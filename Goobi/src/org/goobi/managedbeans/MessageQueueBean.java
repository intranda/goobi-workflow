package org.goobi.managedbeans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.jmx.QueueViewMBean;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.lang.StringUtils;
import org.goobi.api.mq.MessageStatus;
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
@SessionScoped
@Log4j2
public class MessageQueueBean extends BasicBean implements Serializable {

    private static final long serialVersionUID = 9201515793444130154L;

    private Gson gson = new Gson();

    private ActiveMQConnection connection;
    private QueueSession queueSession;

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

            try {
                connection = (ActiveMQConnection) connectionFactory.createConnection(ConfigurationHelper.getInstance().getMessageBrokerUsername(),
                        ConfigurationHelper.getInstance().getMessageBrokerPassword());
                queueSession = connection.createQueueSession(false, Session.CLIENT_ACKNOWLEDGE);
            } catch (JMSException e) {
                log.error(e);
                e.printStackTrace();
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
                QueueBrowser browser = queueSession.createBrowser(queue);
                Enumeration<?> messagesInQueue = browser.getEnumeration();
                while (messagesInQueue.hasMoreElements()) {
                    ActiveMQTextMessage queueMessage = (ActiveMQTextMessage) messagesInQueue.nextElement();

                    String type = queueMessage.getStringProperty("ticketType");
                    if (fastQueueContent.containsKey(type)) {
                        fastQueueContent.put(type, fastQueueContent.get(type) + 1);
                    } else {
                        fastQueueContent.put(type, 1);
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
                QueueBrowser browser = queueSession.createBrowser(queue);
                Enumeration<?> messagesInQueue = browser.getEnumeration();
                while (messagesInQueue.hasMoreElements()) {
                    ActiveMQTextMessage queueMessage = (ActiveMQTextMessage) messagesInQueue.nextElement();

                    String type = queueMessage.getStringProperty("ticketType");
                    if (fastQueueContent.containsKey(type)) {
                        fastQueueContent.put(type, fastQueueContent.get(type) + 1);
                    } else {
                        fastQueueContent.put(type, 1);
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

    public void deleteSlowQueryTicket(TaskTicket ticket) {
        //        try {
        //            ObjectName queue =
        //                    new ObjectName("org.apache.activemq:type=Broker,brokerName=localhost,destinationType=Queue,destinationName=goobi_slow");
        //            deleteMessage(ticket.getMessageId(), queue);
        //        } catch (MalformedObjectNameException e1) {
        //            log.error(e1);
        //        }
    }

    /**
     * Remove all active messages from the goobi_slow queue
     * 
     */

    public void clearSlowQueryQueue() {
        ObjectName queueName;
        try {
            queueName = new ObjectName("org.apache.activemq:type=Broker,brokerName=localhost,destinationType=Queue,destinationName=goobi_slow");
            JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi");
            JMXConnector connector = JMXConnectorFactory.connect(url, null);
            connector.connect();
            MBeanServerConnection connection = connector.getMBeanServerConnection();
            QueueViewMBean bean = MBeanServerInvocationHandler.newProxyInstance(connection, queueName, QueueViewMBean.class, true);
            bean.purge();
            connector.close();
        } catch (Exception e) {
            log.error(e);
        }
    }

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
                QueueBrowser browser = queueSession.createBrowser(queue, "ticketType = '" + messageType + "'");
                Enumeration<?> messagesInQueue = browser.getEnumeration();
                while (messagesInQueue.hasMoreElements()) {
                    ActiveMQTextMessage queueMessage = (ActiveMQTextMessage) messagesInQueue.nextElement();

                    TaskTicket ticket = gson.fromJson(queueMessage.getText(), TaskTicket.class);
                    ticket.setMessageId(queueMessage.getJMSMessageID());

                    answer.add(ticket);
                }
                connection.stop();
            } catch (JMSException e) {
                log.error(e);
            }
        }
        return answer;
    }

    /**
     * Delete a single message from the goobi_fast queue
     * 
     * @param ticket to delete
     */
    public void deleteFastQueryTicket(TaskTicket ticket) {
        //        try {
        //            ObjectName queue =
        //                    new ObjectName("org.apache.activemq:type=Broker,brokerName=localhost,destinationType=Queue,destinationName=goobi_fast");
        //            deleteMessage(ticket.getMessageId(), queue);
        //        } catch (MalformedObjectNameException e1) {
        //            log.error(e1);
        //        }
    }

    /**
     * Remove all active messages from the goobi_fast queue
     * 
     */
    public void clearFastQueryQueue() {
        ObjectName queueName;
        try {
            queueName = new ObjectName("org.apache.activemq:type=Broker,brokerName=localhost,destinationType=Queue,destinationName=goobi_fast");
            JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi");
            JMXConnector connector = JMXConnectorFactory.connect(url, null);
            connector.connect();
            MBeanServerConnection connection = connector.getMBeanServerConnection();
            QueueViewMBean bean = MBeanServerInvocationHandler.newProxyInstance(connection, queueName, QueueViewMBean.class, true);
            bean.purge();
            connector.close();
        } catch (Exception e) {
            log.error(e);
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
                QueueBrowser browser = queueSession.createBrowser(queue, "ticketType = '" + messageType + "'");
                Enumeration<?> messagesInQueue = browser.getEnumeration();
                while (messagesInQueue.hasMoreElements()) {
                    ActiveMQTextMessage queueMessage = (ActiveMQTextMessage) messagesInQueue.nextElement();

                    TaskTicket ticket = gson.fromJson(queueMessage.getText(), TaskTicket.class);
                    ticket.setMessageId(queueMessage.getJMSMessageID());

                    answer.add(ticket);
                }
                connection.stop();
            } catch (JMSException e) {
                log.error(e);
            }
        }

        return answer;
    }

    public void deleteMessage(TaskTicket ticket) {
        MessageStatus.cancelMessage(ticket.getMessageId(), queueType, messageType, ticket.getProcessId());

        //        try {
        //
        //            Queue queue = queueSession.createQueue(queueType);
        //            MessageConsumer consumer =
        //                    queueSession.createConsumer(queue, "JMSMessageID=" + ticket.getMessageId().replace("ID:", "").replace(":1:1:1:1", ""));
        //            //            MessageConsumer consumer = queueSession.createConsumer(dest, "ticketType='" + messageType + "' AND processid=" +ticket.getProcessId());
        //            connection.start();
        //            Message message = consumer.receiveNoWait();
        //            if (message != null) {
        //                message.acknowledge();
        //            }
        //            connection.stop();
        //        } catch (JMSException e) {
        //            log.error(e);
        //        }
    }

}
