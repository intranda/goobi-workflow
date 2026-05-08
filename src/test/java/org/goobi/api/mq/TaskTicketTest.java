package org.goobi.api.mq;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TaskTicketTest {
    private TaskTicket original;
    private TaskTicket clone;

    @BeforeEach
    public void setup() {
        original = new TaskTicket("testType");
        original.setProcessId(1);
        original.setProcessName("test-process");
        original.setStepId(2);
        original.setStepName("test-step");
        original.setQueueName("test-queue");
        original.setRetryCount(7);
        original.setNumberOfObjects(12);
        original.setMessageId("test-message-id");
        original.getProperties().put("test-property", "test-value");
        clone = new TaskTicket(original);
    }

    @Test
    public void testCopyConstructor() {
        assertEquals(original.getProcessId(), clone.getProcessId());
        assertEquals(original.getProcessName(), clone.getProcessName());
        assertEquals(original.getStepId(), clone.getStepId());
        assertEquals(original.getStepName(), clone.getStepName());
        assertEquals(original.getQueueName(), clone.getQueueName());
        assertEquals(original.getRetryCount(), clone.getRetryCount());
        assertEquals(original.getNumberOfObjects(), clone.getNumberOfObjects());
        assertEquals(original.getMessageId(), clone.getMessageId());
        assertEquals(original.getTaskType(), clone.getTaskType());
        assertEquals(original.getProperties(), clone.getProperties());
    }
}
