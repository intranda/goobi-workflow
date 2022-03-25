package org.goobi.managedbeans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.inject.Named;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.jmx.QueueViewMBean;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.goobi.api.mq.TaskTicket;

import com.google.gson.Gson;

import de.sub.goobi.config.ConfigurationHelper;
import lombok.extern.log4j.Log4j;

/**
 * 
 * This bean can be used to display the current state of the goobi_fast and goobi_slow queues. The bean provides methods to show all active tickets
 * and remove a ticket or clear the queue.
 *
 */

@Named
@WindowScoped
@Log4j
public class JmsBean implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -4284897028678041373L;

    private Gson gson = new Gson();

    private ActiveMQConnection connection;
    private QueueSession queueSession;

    public JmsBean() {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();

        try {
            connection = (ActiveMQConnection) connectionFactory.createConnection(ConfigurationHelper.getInstance().getMessageBrokerUsername(),
                    ConfigurationHelper.getInstance().getMessageBrokerPassword());
            queueSession = connection.createQueueSession(false, Session.CLIENT_ACKNOWLEDGE);
        } catch (JMSException e) {
            log.error(e);
        }
    }

    /**
     * Delete a single message from the goobi_slow queue
     * 
     * @param ticket to delete
     */

    public void deleteSlowQueryTicket(TaskTicket ticket) {
        try {
            ObjectName queue =
                    new ObjectName("org.apache.activemq:type=Broker,brokerName=localhost,destinationType=Queue,destinationName=goobi_slow");
            deleteMessage(ticket.getMessageId(), queue);
        } catch (MalformedObjectNameException e1) {
            log.error(e1);
        }
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
        try {
            connection.start();
            Queue queue = queueSession.createQueue("goobi_slow");
            QueueBrowser browser = queueSession.createBrowser(queue);
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

        return answer;
    }

    /**
     * Delete a single message from the goobi_fast queue
     * 
     * @param ticket to delete
     */
    public void deleteFastQueryTicket(TaskTicket ticket) {
        try {
            ObjectName queue =
                    new ObjectName("org.apache.activemq:type=Broker,brokerName=localhost,destinationType=Queue,destinationName=goobi_fast");
            deleteMessage(ticket.getMessageId(), queue);
        } catch (MalformedObjectNameException e1) {
            log.error(e1);
        }
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
        try {
            connection.start();
            Queue queue = queueSession.createQueue("goobi_fast");
            QueueBrowser browser = queueSession.createBrowser(queue);
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

        return answer;
    }

    private void deleteMessage(String messageId, ObjectName queueName) {
        try {
            JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi");
            JMXConnector connector = JMXConnectorFactory.connect(url, null);
            connector.connect();
            MBeanServerConnection connection = connector.getMBeanServerConnection();
            QueueViewMBean bean = MBeanServerInvocationHandler.newProxyInstance(connection, queueName, QueueViewMBean.class, true);
            bean.removeMessage(messageId);
        } catch (Exception e) {
            log.error(e);
        }
    }
}
