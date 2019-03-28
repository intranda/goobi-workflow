package org.goobi.api.mq;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
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
        RedeliveryPolicy policy = conn.getRedeliveryPolicy();
        policy.setMaximumRedeliveries(0);
        final Session sess = conn.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        final Destination dest = sess.createQueue(queue);

        final MessageConsumer cons = sess.createConsumer(dest);
        final MessageListener myListener = new MessageListener() {
            @Override
            public void onMessage(Message message) {
                System.out.println(message);
                try {
                    System.out.println(message.getJMSType());
                    if (message instanceof TextMessage) {
                        TextMessage tm = (TextMessage) message;
                        System.out.println(tm.getText());
                    }
                    if (message instanceof BytesMessage) {
                        System.out.println("bytesmessage");
                        BytesMessage bm = (BytesMessage) message;
                        byte[] bytes = new byte[(int) bm.getBodyLength()];
                        bm.readBytes(bytes);
                        System.out.println(new String(bytes));
                        //                        message.acknowledge();
                        sess.recover();
                    }
                } catch (JMSException e) {
                    // TODO Auto-generated catch block
                    log.error(e);
                }
            }
        };

        cons.setMessageListener(myListener);

        conn.start();

    }

    public void createConsumer() {
        //        DefaultConsumer consumer = new DefaultConsumer(channel) {
        //            @Override
        //            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        //                String message = new String(body, "UTF-8");
        //                TaskTicket ticket = gson.fromJson(message, TaskTicket.class);
        //
        //                log.info("Received ticket for job " + ticket.getTaskType());
        //
        //                if (instances.isEmpty()) {
        //                    getInstalledTicketHandler();
        //                }
        //
        //                if (ticket.getTaskType().equals("test-retry")) {
        //                    System.out.println("\n\ngot retry ticket. put into retry-queue\n\n");
        //                    ticket.setRetryCount(ticket.getRetryCount() + 1);
        //                    BasicProperties props = new AMQP.BasicProperties.Builder()
        //                            //                            .expiration(Integer.toString((5 * 60 * 1000)))
        //                            .build();
        //                    channel.basicPublish(QpidBrokerListener.RETRY_EXCHANGE, "", props, gson.toJson(ticket).getBytes(Charset.forName("UTF-8")));
        //                    return;
        //                }
        //
        //                TicketHandler<PluginReturnValue> runnable = null;
        //
        //                if (instances.containsKey(ticket.getTaskType())) {
        //                    runnable = instances.get(ticket.getTaskType());
        //                } else {
        //                    getInstalledTicketHandler();
        //                    if (instances.containsKey(ticket.getTaskType())) {
        //                        runnable = instances.get(ticket.getTaskType());
        //                    }
        //                }
        //
        //                if (runnable != null) {
        //
        //                    PluginReturnValue returnValue = runnable.call(ticket);
        //
        //                    if (returnValue == PluginReturnValue.ERROR) {
        //                        // put this one to the retry queue
        //                        ticket.setRetryCount(ticket.getRetryCount() + 1);
        //                        channel.basicPublish(QpidBrokerListener.RETRY_EXCHANGE, "", null, gson.toJson(ticket).getBytes(Charset.forName("UTF-8")));
        //                    }
        //                } else {
        //                    log.error("Ticket type unknown: " + ticket.getTaskType());
        //                }
        //
        //            }
        //        };
        //        return consumer;
    }

    public void close() throws JMSException {
        conn.close();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void getInstalledTicketHandler() {
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
