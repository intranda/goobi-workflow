package de.sub.goobi.converter;

import static org.junit.Assert.*;

import javax.faces.convert.ConverterException;

import org.easymock.EasyMock;
import org.goobi.beans.Process;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.persistence.managers.ProcessManager;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ProcessManager.class)
public class ProcessConverterTest {

    @Test
    public void testGetAsObject() {
        Process process = new Process();
        process.setId(new Integer(1));
        PowerMock.mockStatic(ProcessManager.class);
        EasyMock.expect(ProcessManager.getProcessById(1)).andReturn(process);
        EasyMock.expectLastCall();
        PowerMock.replayAll();
            
        ProcessConverter conv = new ProcessConverter();
        Object fixture = conv.getAsObject(null, null, "1");
        assertNotNull(fixture);
        String zero = (String) conv.getAsObject(null, null, "NAN");
        assertEquals("0", zero);      
                
        String nullValue = (String) conv.getAsObject(null, null, null);
        assertNull(nullValue);
    }

    @Test
    public void testGetAsString() {
        Process process = new Process();
        process.setId(42);
        
        ProcessConverter conv = new ProcessConverter();
        String value = conv.getAsString(null, null, process);
        assertEquals("42", value);
        value = conv.getAsString(null, null, "test");
        assertEquals("test", value);
        
        String nullValue = (String) conv.getAsString(null, null, null);
        assertNull(nullValue);
    }

    
    @Test(expected = ConverterException.class)
    public void testConverterException() {
        ProcessConverter conv = new ProcessConverter();
        conv.getAsString(null, null, 1);
    }
}
