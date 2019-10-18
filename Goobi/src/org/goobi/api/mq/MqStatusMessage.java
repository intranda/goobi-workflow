package org.goobi.api.mq;

import java.util.Date;

import org.goobi.beans.DatabaseObject;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MqStatusMessage implements DatabaseObject {
    public enum MessageStatus {
        DONE,
        ERROR,
        ERROR_DLQ
    }

    private String ticketId;
    private Date time;
    private MessageStatus status;
    private String message;
    private String originalMessage;

    @Override
    public void lazyLoad() {
        // TODO Auto-generated method stub
    }
}
