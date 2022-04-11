package org.goobi.api.mq;

import java.util.List;

import lombok.Data;

@Data
public class CommandTicket {
    private Integer processId;
    private String processName;

    private Integer stepId;
    private String stepName;
    
    private String command;
    private String jwt; 
    private String newStatus;
    
    private List<String> scriptNames;
    
    private String logType;
    private String issuer;
    private String content;
}
