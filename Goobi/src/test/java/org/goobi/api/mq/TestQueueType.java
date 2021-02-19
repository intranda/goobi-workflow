package org.goobi.api.mq;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TestQueueType {

    @Test
    public void TestQueueInstantiation() {
        QueueType slowQueue = QueueType.getByName("goobi_slow");
        assertTrue("queue should be identical to QueueType.GOOBI_SLOW", slowQueue == QueueType.SLOW_QUEUE);

        QueueType shouldBeNone = QueueType.getByName("myOwnCoolQueue");
        assertTrue("queue should be identical to QueueType.NONE", shouldBeNone == QueueType.NONE);
    }
}
