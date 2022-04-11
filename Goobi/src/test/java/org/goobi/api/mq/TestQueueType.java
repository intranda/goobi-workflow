package org.goobi.api.mq;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.sub.goobi.AbstractTest;

public class TestQueueType extends AbstractTest {

    @Test
    public void TestQueueInstantiation() {
        QueueType slowQueue = QueueType.getByName("goobi_slow");
        assertTrue("queue should be identical to QueueType.GOOBI_SLOW", slowQueue == QueueType.SLOW_QUEUE);

        QueueType shouldBeNone = QueueType.getByName("myOwnCoolQueue");
        assertTrue("queue should be identical to QueueType.NONE", shouldBeNone == QueueType.NONE);
    }
}
