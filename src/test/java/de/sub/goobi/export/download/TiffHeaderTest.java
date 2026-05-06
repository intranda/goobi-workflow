/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 *
 * Visit the websites for more information.
 *          - https://goobi.io
 *          - https://www.intranda.com
 *          - https://github.com/intranda/goobi-workflow
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package de.sub.goobi.export.download;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.goobi.beans.GoobiProperty;
import org.goobi.beans.GoobiProperty.PropertyOwnerType;
import org.goobi.beans.Process;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.sub.goobi.AbstractTest;

public class TiffHeaderTest extends AbstractTest {

    private Process mockProcess;

    @BeforeEach
    public void setUp() {
        List<GoobiProperty> properties = new ArrayList<>();

        GoobiProperty docName = new GoobiProperty(PropertyOwnerType.PROCESS);
        docName.setPropertyName("TifHeaderDocumentname");
        docName.setPropertyValue("MyDocument");
        properties.add(docName);

        GoobiProperty imageDesc = new GoobiProperty(PropertyOwnerType.PROCESS);
        imageDesc.setPropertyName("TifHeaderImagedescription");
        imageDesc.setPropertyValue("MyDescription");
        properties.add(imageDesc);

        GoobiProperty artist = new GoobiProperty(PropertyOwnerType.PROCESS);
        artist.setPropertyName("Artist");
        artist.setPropertyValue("MyArtist");
        properties.add(artist);

        mockProcess = new Process();
        mockProcess.setEigenschaften(properties);
    }

    @Test
    public void testConstructor() {
        TiffHeader header = new TiffHeader(mockProcess);
        assertNotNull(header);
    }

    @Test
    public void testGetImageDescription() {
        TiffHeader header = new TiffHeader(mockProcess);
        assertEquals("MyDescription", header.getImageDescription());
    }

    @Test
    public void testGetTiffAllesContainsArtist() {
        TiffHeader header = new TiffHeader(mockProcess);
        String result = header.getTiffAlles();
        assertTrue(result.contains("Artist=MyArtist"));
    }

    @Test
    public void testGetTiffAllesContainsDocumentname() {
        TiffHeader header = new TiffHeader(mockProcess);
        String result = header.getTiffAlles();
        assertTrue(result.contains("Documentname=MyDocument"));
    }

    @Test
    public void testGetTiffAllesContainsImageDescription() {
        TiffHeader header = new TiffHeader(mockProcess);
        String result = header.getTiffAlles();
        assertTrue(result.contains("ImageDescription=MyDescription"));
    }

    @Test
    public void testGetTiffAllesContainsHeader() {
        TiffHeader header = new TiffHeader(mockProcess);
        String result = header.getTiffAlles();
        assertTrue(result.contains("# Configuration file for TIFFWRITER.pl"));
    }

    @Test
    public void testConstructorWithEmptyProperties() {
        Process emptyProcess = new Process();
        emptyProcess.setEigenschaften(new ArrayList<>());

        TiffHeader header = new TiffHeader(emptyProcess);
        assertEquals("", header.getImageDescription());
        assertTrue(header.getTiffAlles().contains("Artist="));
    }

    @Test
    public void testGetTiffAllesUnknownPropertyIgnored() {
        List<GoobiProperty> props = new ArrayList<>();
        GoobiProperty unknown = new GoobiProperty(PropertyOwnerType.PROCESS);
        unknown.setPropertyName("UnknownKey");
        unknown.setPropertyValue("UnknownValue");
        props.add(unknown);

        Process proc = new Process();
        proc.setEigenschaften(props);

        TiffHeader header = new TiffHeader(proc);
        assertEquals("", header.getImageDescription());
    }
}
