package org.goobi.api.mq;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.StringUtils;
import org.goobi.production.enums.PluginReturnValue;
import org.reflections.Reflections;

import com.google.gson.Gson;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import lombok.extern.log4j.Log4j;

@Log4j
public class GoobiDefaultQueueListener implements MessageListener {

    private Channel channel;

    @Override
    public String getQueueName() {
        return "goobi-default-queue";
    }

    private static Map<String, TicketHandler<PluginReturnValue>> instances = new HashMap<>();

    @Override
    public void register(String url, Integer port, String username, String password) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(url);
        factory.setPort(port);
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
            factory.setUsername(username);
            factory.setPassword(password);
        }

        try {
            Connection connection = factory.newConnection();
            channel = connection.createChannel();
            channel.queueDeclare(getQueueName(), true, false, false, null);

            Consumer consumer = receiveMessage(channel);

            channel.basicConsume(getQueueName(), false, consumer);

        } catch (IOException | TimeoutException e) {
            log.error(e);
        }
    }

    @Override
    public Consumer receiveMessage(Channel channel) {
        Gson gson = new Gson();
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                TaskTicket ticket = gson.fromJson(message, TaskTicket.class);

                log.info("Received ticket for job " + ticket.getTaskType());

                if (instances.isEmpty()) {
                    getInstalledTicketHandler();
                }

                TicketHandler<PluginReturnValue> runnable = null;

                if (instances.containsKey(ticket.getTaskType())) {
                    runnable = instances.get(ticket.getTaskType());
                } else {
                    getInstalledTicketHandler();
                    if (instances.containsKey(ticket.getTaskType())) {
                        runnable = instances.get(ticket.getTaskType());
                    }
                }

                if (runnable != null) {

                    PluginReturnValue returnValue = runnable.call(ticket);

                    if (returnValue == PluginReturnValue.ERROR) {
                        // TODO general error handling, maybe try to answer the sender
                    }
                } else {
                    log.error("Ticket type unknown: " + ticket.getTaskType());
                }

                channel.basicAck(envelope.getDeliveryTag(), false);
            }
        };
        return consumer;
    }

    @Override
    public void close() {
        try {
            this.channel.close();
        } catch (IOException | TimeoutException e) {
            // TODO Auto-generated catch block
            log.error(e);
        }
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
