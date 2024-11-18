package org.goobi.api.mq;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TaskTicketTest {
    private TaskTicket original;
    private TaskTicket clone;

    @Before
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
