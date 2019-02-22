package org.goobi.api.mq;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.StringUtils;
import org.goobi.production.enums.PluginReturnValue;

import com.google.gson.Gson;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class GoobiDefaultQueueListener implements MessageListener {

    @Override
    public String getQueueName() {
        return "goobi-default-queue";
    }

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
            Channel channel = connection.createChannel();
            channel.queueDeclare(getQueueName(), false, false, false, null);

            Consumer consumer = receiveMessage(channel);

            channel.basicConsume(getQueueName(), false, consumer);

        } catch (IOException | TimeoutException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // TODO Auto-generated method stub

    }

    @Override
    public Consumer receiveMessage(Channel channel) {
        Gson gson = new Gson();
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                TaskTicket ticket = gson.fromJson(message, TaskTicket.class);

                System.out.println(ticket.toString());
                TicketHandler<PluginReturnValue> runnable = null;
                switch (ticket.getTaskType()) {
                    case "unzip":
                        runnable = new UnzipFileRunnable();
                        break;

                    default:
                        System.out.println("Ticket type unknown: " + ticket.getTaskType());
                        break;
                }
                if (runnable != null) {

                    PluginReturnValue returnValue = runnable.call(ticket);

                    if (returnValue == PluginReturnValue.FINISH) {
                    }
                }

                channel.basicAck(envelope.getDeliveryTag(), false);
            }
        };
        return consumer;

    }

    @Override
    public void sendMessage() {
        // TODO Auto-generated method stub

    }

}
