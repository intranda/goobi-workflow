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

import java.sql.SQLException;

import javax.faces.convert.ConverterException;

import org.easymock.EasyMock;
import org.goobi.beans.Docket;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.DocketManager;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DocketManager.class)
public class DocketConverterTest {

    @Test
    public void testGetAsObject() throws DAOException, SQLException {
        Docket docket = new Docket();
        PowerMock.mockStatic(DocketManager.class);
        EasyMock.expect(DocketManager.getDocketById(1)).andReturn(docket);

        EasyMock.expect(DocketManager.getDocketById(2)).andThrow(new DAOException("test"));
        EasyMock.expectLastCall();
        PowerMock.replayAll();

        DocketConverter conv = new DocketConverter();
        Object fixture = conv.getAsObject(null, null, "1");
        assertNotNull(fixture);
        assertNull(conv.getAsObject(null, null, null));
        String zero = (String) conv.getAsObject(null, null, "NAN");
        assertEquals("0", zero);

        assertNotNull(conv.getAsObject(null, null, "2"));

    }

    @Test
    public void testGetAsString() {
        Docket docket = new Docket();
        docket.setFile("file");
        docket.setId(42);
        DocketConverter conv = new DocketConverter();
        String value = conv.getAsString(null, null, docket);
        assertEquals("42", value);

        value = conv.getAsString(null, null, "test");
        assertEquals("test", value);

        String nullValue = (String) conv.getAsString(null, null, null);
        assertNull(nullValue);
    }

    @Test(expected = ConverterException.class)
    public void testConverterException() {
        DocketConverter conv = new DocketConverter();
        conv.getAsString(null, null, 1);
    }

}
