package org.goobi.api.mq;

import java.util.Date;

import org.goobi.beans.DatabaseObject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
public class MqStatusMessage implements DatabaseObject {

    public enum MessageStatus {
        DONE("DONE"),
        ERROR("ERROR"),
        ERROR_DLQ("ERROR_DLQ");

        @Getter
        private String name;

        private MessageStatus(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
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
