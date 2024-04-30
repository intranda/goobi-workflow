package org.goobi.api.mq;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Date;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *          - https://goobi.io
 *          - https://www.intranda.com
 *          - https://github.com/intranda/goobi-workflow
 *          - http://digiverso.com
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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQPrefetchPolicy;
import org.apache.activemq.RedeliveryPolicy;
import org.goobi.api.mq.MqStatusMessage.MessageStatus;
import org.goobi.production.enums.PluginReturnValue;
import org.reflections.Reflections;

import com.google.gson.Gson;

import de.sub.goobi.persistence.managers.MQResultManager;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GoobiDefaultQueueListener {

    private Gson gson = new Gson();
    private Thread thread;
    private ActiveMQConnection conn;
    private volatile boolean shouldStop = false;

    private static Map<String, TicketHandler<PluginReturnValue>> instances = new HashMap<>();

    public void register(String username, String password, QueueType queue) throws JMSException {
        ActiveMQConnectionFactory connFactory = new ActiveMQConnectionFactory("vm://localhost");
        connFactory.setTrustedPackages(Arrays.asList("org.goobi.managedbeans", "org.goobi.api.mq", "org.goobi.api.mq.ticket"));
        conn = (ActiveMQConnection) connFactory.createConnection(username, password);
        ActiveMQPrefetchPolicy prefetchPolicy = new ActiveMQPrefetchPolicy();
        prefetchPolicy.setAll(0);
        conn.setPrefetchPolicy(prefetchPolicy);
        RedeliveryPolicy policy = conn.getRedeliveryPolicy();
        policy.setMaximumRedeliveries(0);
        thread = new Thread(() -> startMessageLoop(queue.toString(), conn));
        thread.setDaemon(true);
        thread.start();
    }

    void startMessageLoop(String queueType, ActiveMQConnection conn) {
        try {
            conn.start();
            startListener(queueType, conn);
            log.info("Exiting listener thread for message queue {}", queueType);
        } catch (JMSException e) {
            log.error("Error starting listener for queue {}. Aborting listerner startup", e.toString(), e);
        }
    }

    void startListener(String queueType, ActiveMQConnection conn) throws JMSException {
        try (Session sess = conn.createSession(false, Session.CLIENT_ACKNOWLEDGE);
                MessageConsumer consumer = sess.createConsumer(sess.createQueue(queueType));) {
            while (!shouldStop) {
                waitForMessage(sess, consumer);
                if (Thread.interrupted()) {
                    log.info("Queue listener for queue {} interrupted: exiting listener", queueType);
                    return;
                }
            }
        }
    }

    void waitForMessage(Session sess, MessageConsumer consumer) {

        try {
            String origMessage = null;
            Message message = consumer.receive();
            Optional<TaskTicket> optTicket = Optional.empty();
            if (message instanceof TextMessage) {
                TextMessage tm = (TextMessage) message;
                optTicket = Optional.of(gson.fromJson(tm.getText(), TaskTicket.class));
                origMessage = tm.getText();
            }
            if (message instanceof BytesMessage) {
                BytesMessage bm = (BytesMessage) message;
                byte[] bytes = new byte[(int) bm.getBodyLength()];
                bm.readBytes(bytes);
                optTicket = Optional.of(gson.fromJson(new String(bytes), TaskTicket.class));
                origMessage = new String(bytes);
            }
            if (optTicket.isPresent()) {
                log.debug("Handling ticket {}", optTicket.get());
                try {
                    PluginReturnValue result = handleTicket(optTicket.get());
                    if (result == PluginReturnValue.FINISH) {
                        //acknowledge message, it is done
                        message.acknowledge();
                        storeResult(origMessage, message, optTicket, MessageStatus.DONE);
                    } else {
                        //error or wait => put back to queue and retry by redeliveryPolicy
                        sess.recover();
                    }
                } catch (Throwable t) {
                    log.error("Error handling ticket " + message.getJMSMessageID() + ": ", t);
                    sess.recover();
                    storeResult(origMessage, message, optTicket, MessageStatus.ERROR);
                }
            }
        } catch (JMSException e) {
            if (!shouldStop) {
                // back off a little bit, maybe we have a problem with the connection or we are shutting down
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e1) {
                    Thread.currentThread().interrupt();
                }
                if (!shouldStop) {
                    log.error(e);
                }
            }
        }
    }

    private void storeResult(String origMessage, Message message, Optional<TaskTicket> optTicket, MessageStatus status) {
        if (optTicket.isPresent()) {
            try {
                MqStatusMessage statusMessage = new MqStatusMessage(message.getJMSMessageID(), new Date(), status, "",
                        origMessage, optTicket.get().getNumberOfObjects(), optTicket.get().getTaskType(), optTicket.get().getProcessId(),
                        optTicket.get().getStepId(), optTicket.get().getStepName());
                MQResultManager.insertResult(statusMessage);
            } catch (Exception e) {
                // error when result is saved
            }
        }
    }

    private PluginReturnValue handleTicket(TaskTicket ticket) {
        if (!instances.containsKey(ticket.getTaskType())) {
            getInstalledTicketHandler();
        }
        TicketHandler<PluginReturnValue> handler = instances.get(ticket.getTaskType());
        if (handler == null) {
            return PluginReturnValue.ERROR;
        }
        return handler.call(ticket);
    }

    public void close() throws JMSException {
        this.shouldStop = true;
        this.conn.close();
        try {
            this.thread.join(1000);
        } catch (InterruptedException e) {
            log.error(e);
            Thread.currentThread().interrupt();
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void getInstalledTicketHandler() {
        instances = new HashMap<>();
        Set<Class<? extends TicketHandler>> ticketHandlers = new Reflections("org.goobi.api.mq.*").getSubTypesOf(TicketHandler.class);
        for (Class<? extends TicketHandler> clazz : ticketHandlers) {
            try {
                TicketHandler<PluginReturnValue> handler = clazz.getDeclaredConstructor().newInstance();
                instances.put(handler.getTicketHandlerName(), handler);
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | IllegalArgumentException | InvocationTargetException
                    | SecurityException e) {
                log.error(e);
            }
        }
    }
}
