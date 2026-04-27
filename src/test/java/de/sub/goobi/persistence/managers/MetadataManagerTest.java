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
package de.sub.goobi.persistence.managers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMock;
import org.goobi.production.cli.helper.StringPair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.AbstractTest;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ MetadataMysqlHelper.class })
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*" })
public class MetadataManagerTest extends AbstractTest {

    private Map<String, List<String>> sampleMetadata;

    @Before
    public void setUp() throws Exception {
        sampleMetadata = new HashMap<>();
        sampleMetadata.put("TitleDocMain", Arrays.asList("Test Title"));
        sampleMetadata.put("Author", Arrays.asList("Test Author"));

        PowerMock.mockStatic(MetadataMysqlHelper.class);

        MetadataMysqlHelper.removeMetadata(EasyMock.anyInt());
        EasyMock.expectLastCall().anyTimes();
        MetadataMysqlHelper.removeJSONMetadata(EasyMock.anyInt());
        EasyMock.expectLastCall().anyTimes();
        MetadataMysqlHelper.insertMetadata(EasyMock.anyInt(), EasyMock.anyObject());
        EasyMock.expectLastCall().anyTimes();
        MetadataMysqlHelper.insertJSONMetadata(EasyMock.anyInt(), EasyMock.anyObject());
        EasyMock.expectLastCall().anyTimes();

        List<String> names = Arrays.asList("TitleDocMain", "Author");
        EasyMock.expect(MetadataMysqlHelper.getDistinctMetadataNames()).andReturn(names).anyTimes();

        List<StringPair> metadataList = new ArrayList<>();
        metadataList.add(new StringPair("TitleDocMain", "Test Title"));
        EasyMock.expect(MetadataMysqlHelper.getMetadata(EasyMock.anyInt())).andReturn(metadataList).anyTimes();

        EasyMock.expect(MetadataMysqlHelper.getMetadataValue(EasyMock.anyInt(), EasyMock.anyString())).andReturn("Test Title").anyTimes();
        EasyMock.expect(MetadataMysqlHelper.getAllValuesForMetadata(EasyMock.anyInt(), EasyMock.anyString())).andReturn("Test Title").anyTimes();
        EasyMock.expect(MetadataMysqlHelper.getAllMetadataValues(EasyMock.anyInt(), EasyMock.anyString()))
                .andReturn(Arrays.asList("Test Title"))
                .anyTimes();
        EasyMock.expect(MetadataMysqlHelper.getAllProcessesWithMetadata(EasyMock.anyString(), EasyMock.anyString()))
                .andReturn(Arrays.asList(1, 2, 3))
                .anyTimes();
        PowerMock.replay(MetadataMysqlHelper.class);
    }

    @Test
    public void testDeleteMetadata() {
        MetadataManager.deleteMetadata(1);
    }

    @Test
    public void testInsertJSONMetadata() {
        MetadataManager.insertJSONMetadata(1, sampleMetadata);
    }

    @Test
    public void testInsertMetadata() {
        MetadataManager.insertMetadata(1, sampleMetadata);
    }

    @Test
    public void testUpdateJSONMetadata() {
        MetadataManager.updateJSONMetadata(1, sampleMetadata);
    }

    @Test
    public void testUpdateMetadata() {
        MetadataManager.updateMetadata(1, sampleMetadata);
    }

    @Test
    public void testGetDistinctMetadataNames() {
        List<String> result = MetadataManager.getDistinctMetadataNames();
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    public void testGetMetadata() {
        List<StringPair> result = MetadataManager.getMetadata(1);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void testGetMetadataValue() {
        String result = MetadataManager.getMetadataValue(1, "TitleDocMain");
        assertEquals("Test Title", result);
    }

    @Test
    public void testGetAllValuesForMetadata() {
        String result = MetadataManager.getAllValuesForMetadata(1, "TitleDocMain");
        assertEquals("Test Title", result);
    }

    @Test
    public void testGetAllMetadataValues() {
        List<String> result = MetadataManager.getAllMetadataValues(1, "TitleDocMain");
        assertNotNull(result);
        assertTrue(result.contains("Test Title"));
    }

    @Test
    public void testGetProcessesWithMetadata() {
        List<Integer> result = MetadataManager.getProcessesWithMetadata("TitleDocMain", "Test Title");
        assertNotNull(result);
        assertEquals(3, result.size());
    }
}
