package de.sub.goobi.converter;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *          - https://goobi.io
 *          - https://www.intranda.com 
 *          - https://github.com/intranda/goobi
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 */
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
