package de.sub.goobi.forms;

import static org.junit.Assert.*;

import org.junit.Test;

public class AdministrationFormTest {

    @Test
    public void testConstructor() {
       AdministrationForm form = new AdministrationForm();
       assertNotNull(form);
    }
    
    @Test
    public void testLogin() {
        AdministrationForm form = new AdministrationForm();
        form.setPasswort("wrong password");
        form.Weiter();
        
        form.setPasswort("test");
        assertEquals("test", form.getPasswort());
        form.Weiter();
        assertEquals("098f6bcd4621d373cade4e832627b4f6", form.getPasswort());
        
    }

}
