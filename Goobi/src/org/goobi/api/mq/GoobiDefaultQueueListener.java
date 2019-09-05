package org.goobi.api.mq;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQPrefetchPolicy;
import org.apache.activemq.RedeliveryPolicy;
import org.goobi.production.enums.PluginReturnValue;
import org.reflections.Reflections;

import com.google.gson.Gson;

import lombok.extern.log4j.Log4j;

@Log4j
public class GoobiDefaultQueueListener {

    private Gson gson = new Gson();
    private ActiveMQConnection conn;

    private static Map<String, TicketHandler<PluginReturnValue>> instances = new HashMap<>();

    public void register(String username, String password, String queue) throws JMSException {
        ActiveMQConnectionFactory connFactory = new ActiveMQConnectionFactory();
        conn = (ActiveMQConnection) connFactory.createConnection(username, password);
        ActiveMQPrefetchPolicy prefetchPolicy = new ActiveMQPrefetchPolicy();
        prefetchPolicy.setAll(0);
        conn.setPrefetchPolicy(prefetchPolicy);
        RedeliveryPolicy policy = conn.getRedeliveryPolicy();
        policy.setMaximumRedeliveries(0);
        final Session sess = conn.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        final Destination dest = sess.createQueue(queue);

        final MessageConsumer cons = sess.createConsumer(dest);

        Runnable run = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Message message = cons.receive();
                        Optional<TaskTicket> optTicket = Optional.empty();
                        if (message instanceof TextMessage) {
                            TextMessage tm = (TextMessage) message;
                            optTicket = Optional.of(gson.fromJson(tm.getText(), TaskTicket.class));
                        }
                        if (message instanceof BytesMessage) {
                            BytesMessage bm = (BytesMessage) message;
                            byte[] bytes = new byte[(int) bm.getBodyLength()];
                            bm.readBytes(bytes);
                        }
                        if (optTicket.isPresent()) {
                            PluginReturnValue result = handleTicket(optTicket.get());
                            if (result == PluginReturnValue.FINISH) {
                                //acknowledge message, it is done
                                message.acknowledge();
                            } else {
                                //error or wait => put back to queue and retry by redeliveryPolicy
                                sess.recover();
                            }
                        }
                    } catch (JMSException e) {
                        // TODO Auto-generated catch block
                        log.error(e);
                    }
                }

            }
        };
        Thread t = new Thread(run);
        t.setDaemon(true);
        /*final MessageListener myListener = new MessageListener() {
            @Override
            public void onMessage(Message message) {
                try {
                    Optional<TaskTicket> optTicket = Optional.empty();
                    if (message instanceof TextMessage) {
                        TextMessage tm = (TextMessage) message;
                        optTicket = Optional.of(gson.fromJson(tm.getText(), TaskTicket.class));
                    }
                    if (message instanceof BytesMessage) {
                        BytesMessage bm = (BytesMessage) message;
                        byte[] bytes = new byte[(int) bm.getBodyLength()];
                        bm.readBytes(bytes);
                    }
                    if (optTicket.isPresent()) {
                        PluginReturnValue result = handleTicket(optTicket.get());
                        if (result == PluginReturnValue.FINISH) {
                            //acknowledge message, it is done
                            message.acknowledge();
                        } else {
                            //error or wait => put back to queue and retry by redeliveryPolicy
                            sess.recover();
                        }
                    }
                } catch (JMSException e) {
                    // TODO Auto-generated catch block
                    log.error(e);
                }
            }
        };
        
        cons.setMessageListener(myListener);*/

        conn.start();
        t.start();
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
        conn.close();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void getInstalledTicketHandler() {
        instances = new HashMap<>();
        Set<Class<? extends TicketHandler>> ticketHandlers = new Reflections("org.goobi.api.mq.*").getSubTypesOf(TicketHandler.class);
        for (Class<? extends TicketHandler> clazz : ticketHandlers) {
            try {
                TicketHandler<PluginReturnValue> handler = clazz.newInstance();
                instances.put(handler.getTicketHandlerName(), handler);
            } catch (InstantiationException | IllegalAccessException e) {
                log.error(e);
            }
        }
    }
}
