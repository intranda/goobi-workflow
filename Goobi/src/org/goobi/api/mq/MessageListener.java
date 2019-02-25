package org.goobi.api.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;

public interface MessageListener {

    /**
     * get the name of the queue on which the listener is listen
     * @return name of the queue
     */

    public String getQueueName();

    /**
     * register a processor to the queue, if more than one ticket can be handled at the same time, another instance can be registered
     */

    public void register(String url, Integer port, String username, String password);


    /**
     * Method to call when a message is received
     */

    public Consumer receiveMessage(Channel channel);



}
