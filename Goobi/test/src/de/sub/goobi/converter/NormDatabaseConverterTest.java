package de.sub.goobi.converter;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *          - http://www.intranda.com
 *          - http://digiverso.com 
 *          - http://www.goobi.org
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
