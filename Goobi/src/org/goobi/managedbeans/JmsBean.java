package org.goobi.managedbeans;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.jms.JMSException;
import javax.jms.Message;
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
import org.goobi.api.mq.TaskTicket;
import org.goobi.api.mq.TicketGenerator;

import com.google.gson.Gson;

import de.sub.goobi.config.ConfigurationHelper;
import lombok.extern.log4j.Log4j;

@ManagedBean
@SessionScoped
@Log4j
public class JmsBean {

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

    public void deleteSlowQueryTicket(TaskTicket ticket) {
        try {
            ObjectName queue = new ObjectName(
                    "org.apache.activemq:type=Broker,brokerName=localhost,destinationType=Queue,destinationName=goobi_slow");
            deleteMessage(ticket.getMessageId(), queue);
        } catch (MalformedObjectNameException e1) {
            log.error(e1);
        }
    }

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

    public List<TaskTicket> getActiveSlowQueryMesssages() {
        List<TaskTicket> answer = new ArrayList<>();
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

    //    @PostConstruct
    public void testConnection() throws JMSException {

        connection.start();
        Queue queue = queueSession.createQueue("goobi_slow");
        QueueBrowser browser = queueSession.createBrowser(queue);
        Enumeration<?> messagesInQueue = browser.getEnumeration();

        while (messagesInQueue.hasMoreElements()) {
            Message queueMessage = (Message) messagesInQueue.nextElement();

            System.out.println(queueMessage.getJMSMessageID());
            System.out.println(queueMessage);
            //            MessageConsumer consumer = queueSession.createConsumer(queue, "JMSMessageID='" + queueMessage.getJMSMessageID() + "'");
            //            consumer.receive();
            //            consumer.close();

            ActiveMQTextMessage activeMQTextMessage = (ActiveMQTextMessage) queueMessage;
            System.out.println(activeMQTextMessage);
        }
        connection.stop();

    }

    public void submitSlowQueryDummyTicket() {
        TaskTicket unzipTticket = TicketGenerator.generateSimpleTicket("submitSlowQueryDummyTicket");
        unzipTticket.setProcessId(1234);
        unzipTticket.setProcessName("processname");
        unzipTticket.setStepId(666);
        unzipTticket.setStepName("step");
        unzipTticket.getProperties().put("filename", "/tmp/");
        unzipTticket.getProperties().put("closeStep", "true");
        try {
            TicketGenerator.submitTicket(unzipTticket, true);

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void submitDummyTicket() {
        TaskTicket unzipTticket = TicketGenerator.generateSimpleTicket("submitDummyTicket");
        unzipTticket.setProcessId(1234);
        unzipTticket.setProcessName("processname");
        unzipTticket.setStepId(666);
        unzipTticket.setStepName("step");
        unzipTticket.getProperties().put("filename", "/tmp/");
        unzipTticket.getProperties().put("closeStep", "true");
        try {
            TicketGenerator.submitTicket(unzipTticket, false);

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void deleteFastQueryTicket(TaskTicket ticket) {
        try {
            ObjectName queue = new ObjectName(
                    "org.apache.activemq:type=Broker,brokerName=localhost,destinationType=Queue,destinationName=goobi_fast");
            deleteMessage(ticket.getMessageId(), queue);
        } catch (MalformedObjectNameException e1) {
            log.error(e1);
        }
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

    public List<TaskTicket> getActiveFastQueryMesssages() {
        List<TaskTicket> answer = new ArrayList<>();
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

}
