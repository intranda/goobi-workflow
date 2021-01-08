package io.goobi.workflow.locking;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LockingBeanTest {

    @Test
    public void testConstructor() {
        LockingBean bean = new LockingBean();
        assertNotNull(bean);
    }

    @Test
    public void testLockObject() {
        LockingBean bean = new LockingBean();
        assertNotNull(bean);
        // lock object
        assertTrue(bean.lockObject("object", "user"));
        // update lock
        assertTrue(bean.lockObject("object", "user"));

        // try to lock it as a different user
        assertFalse(bean.lockObject("object", "other"));
    }



    @Test
    public void  testFreeObject() {
        LockingBean bean = new LockingBean();
        assertNotNull(bean);
        // lock object
        assertTrue(bean.lockObject("object", "user"));
        assertTrue(bean.isLocked("object"));

        // free object
        bean.freeObject("object");
        assertFalse(bean.isLocked("object"));
    }


    @Test
    public void  testResetAllLocks() {
        LockingBean bean = new LockingBean();
        assertNotNull(bean);
        // lock object
        assertTrue(bean.lockObject("object", "user"));
        assertTrue(bean.isLocked("object"));

        // free object
        bean.resetAllLocks();
        assertFalse(bean.isLocked("object"));
    }


}
