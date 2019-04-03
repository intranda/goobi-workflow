package org.goobi.api.mq;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class TaskTicket {

    private Integer processId;
    private String processName;

    private Integer stepId;
    private String stepName;

    private String taskType;
    private String queueName = "goobi-default-queue";
    private int retryCount = 0;

    private Map<String, String> properties = new HashMap<>();

    public TaskTicket(String taskType) {
        this.taskType = taskType;
    }

}
