package de.sub.goobi.helper;

import static org.junit.Assert.*;

import org.junit.Test;

public class BatchTest {

    @Test
    public void testConstructor() {
        Batch batch = new Batch(1, "1");
        assertNotNull(batch);
    }

    @Test
    public void testBatchId() {
        Batch batch = new Batch(1, "1");
        batch.setBatchId("2");
        assertEquals("2", batch.getBatchId());
    }
    
    @Test
    public void testBatchLabel() {
        Batch batch = new Batch(1, "1");
        batch.setBatchLabel("2");
        assertEquals("2", batch.getBatchLabel());
    }
    
    
}
