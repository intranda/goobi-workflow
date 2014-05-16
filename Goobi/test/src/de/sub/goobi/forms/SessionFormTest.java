package de.sub.goobi.forms;

import static org.junit.Assert.*;

import org.junit.Test;

public class SessionFormTest {

    @Test
    public void testConstructor() {
        SessionForm form = new SessionForm();
        assertNotNull(form);
    }

    
    @Test
    public void testGetAktiveSessions() {
        SessionForm form = new SessionForm();
        assertNotNull(form);
        assertEquals(0, form.getAktiveSessions());
        
    }
    
}
