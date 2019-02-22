package org.goobi.api.mq;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import lombok.extern.log4j.Log4j;

@Log4j
public class TicketGenerator {

    public static TaskTicket generateSimpleTicket(String ticketType, Integer processId, String processName, Integer stepId, String stepName) {

        TaskTicket tt = new TaskTicket(ticketType);
        tt.setProcessId(processId);
        tt.setProcessName(processName);

        tt.setStepId(stepId);
        tt.setStepName(stepName);

        return tt;

    }

    public static void registerTicket(TaskTicket ticket) {
        // TODO get from configuration/environment

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(61616);

        Connection connection = null;
        Channel channel = null;
        try {
            connection = factory.newConnection();
            channel = connection.createChannel();
            channel.queueDeclare(ticket.getQueueName(), false, false, false, null);
            Gson gson = new Gson();

            String jsonString = gson.toJson(ticket);

            channel.basicPublish("", ticket.getQueueName(), null, jsonString.getBytes());

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

    public static void main(String[] args) {
        TaskTicket ticket = generateSimpleTicket("unzip", 666, "Number of the Beast", 99999, "unzip uploaded files");
        ticket.getProperties().put("filename", "/opt/digiverso/example.zip");
        ticket.getProperties().put("destination", "/opt/digiverso/goobi/metadata/19552/images/master_bla_media/");
        registerTicket(ticket);
    }
}
