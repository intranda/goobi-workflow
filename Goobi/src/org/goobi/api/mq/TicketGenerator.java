package org.goobi.api.mq;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.StringUtils;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

import de.sub.goobi.config.ConfigurationHelper;
import lombok.extern.log4j.Log4j;

@Log4j
public class TicketGenerator {

    public static TaskTicket generateSimpleTicket(String ticketType) {
        TaskTicket tt = new TaskTicket(ticketType);
        return tt;

    }

    public static void registerTicket(TaskTicket ticket) {

        ConnectionFactory factory = new ConnectionFactory();

        factory.setHost(ConfigurationHelper.getInstance().getMessageBrokerUrl());
        factory.setPort(ConfigurationHelper.getInstance().getMessageBrokerPort());

        if (StringUtils.isNotBlank(ConfigurationHelper.getInstance().getMessageBrokerUsername())) {
            factory.setUsername(ConfigurationHelper.getInstance().getMessageBrokerUsername());
            factory.setPassword(ConfigurationHelper.getInstance().getMessageBrokerPassword());
        }

        Connection connection = null;
        Channel channel = null;
        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            channel.queueDeclare(ticket.getQueueName(), true, false, false, null);
            Gson gson = new Gson();

            String jsonString = gson.toJson(ticket);

            channel.basicPublish("", ticket.getQueueName(), MessageProperties.MINIMAL_PERSISTENT_BASIC, jsonString.getBytes());

        } catch (IOException | TimeoutException e) {
            log.error(e);
        } finally {
            try {
                channel.close();
                connection.close();
            } catch (IOException | TimeoutException e) {
                log.error(e);
            }
        }

    }

}
