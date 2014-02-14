package de.sub.goobi.converter;

import static org.junit.Assert.*;

import java.sql.SQLException;

import org.goobi.api.display.enums.NormDatabase;
import org.junit.Test;

import de.sub.goobi.helper.exceptions.DAOException;

public class NormDatabaseConverterTest {

    @Test
    public void testGetAsObject() throws DAOException, SQLException {
        NormDatabaseConverter converter = new NormDatabaseConverter();
        assertEquals(NormDatabase.GND, converter.getAsObject(null, null, "gnd"));
        assertEquals(NormDatabase.REFGEO, converter.getAsObject(null, null, "intranda Geo Datenbank"));
        assertEquals(NormDatabase.REFBIO, converter.getAsObject(null, null, "intranda PND"));
        assertEquals(NormDatabase.GND, converter.getAsObject(null, null, null));
        assertEquals(NormDatabase.GND, converter.getAsObject(null, null, ""));
    }

    @Test
    public void testGetAsString() {
        NormDatabaseConverter converter = new NormDatabaseConverter();
        assertEquals(NormDatabase.GND.getAbbreviation(), converter.getAsString(null, null, null));
        assertEquals(NormDatabase.GND.getAbbreviation(), converter.getAsString(null, null, ""));
        assertEquals(NormDatabase.GND.getAbbreviation(), converter.getAsString(null, null, NormDatabase.GND));
        assertEquals(NormDatabase.REFGEO.getAbbreviation(), converter.getAsString(null, null, NormDatabase.REFGEO));
        assertEquals(NormDatabase.REFBIO.getAbbreviation(), converter.getAsString(null, null, NormDatabase.REFBIO));
        
    }

}
