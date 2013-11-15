package de.sub.goobi.converter;

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
        EasyMock.expectLastCall();
        PowerMock.replayAll();

        DocketConverter conv = new DocketConverter();
        Object fixture = conv.getAsObject(null, null, "1");
        assertNotNull(fixture);
        assertNull(conv.getAsObject(null, null, null));
        String zero = (String) conv.getAsObject(null, null, "NAN");
        assertEquals("0", zero);
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
    }

    @Test(expected = ConverterException.class)
    public void testConverterException() {
        DocketConverter conv = new DocketConverter();
        conv.getAsString(null, null, 1);
    }

}
