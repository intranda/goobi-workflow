package org.goobi.beans;

import java.util.Date;

import org.goobi.production.enums.LogType;

import lombok.Data;

@Data
public class LogEntry {

    private Date creationDate;
    private String userName;
    private LogType type;
    private String content;
    private String secondContent;
    private String thirdContent;

}
