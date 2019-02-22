package org.goobi.api.mq;

public interface TicketHandler<V>  {

    /**
     * call this method to execute the ticket
     * 
     * @param ticket
     * @return
     */


    public V call(TaskTicket ticket);

}
