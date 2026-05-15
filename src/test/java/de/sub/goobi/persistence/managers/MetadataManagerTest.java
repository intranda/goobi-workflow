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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.goobi.production.cli.helper.StringPair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.metadaten.search.DatabaseMetadataField;

@ExtendWith(MockitoExtension.class)
public class MetadataManagerTest extends AbstractTest {

    private Map<String, List<DatabaseMetadataField>> sampleMetadata;

    private List<StringPair> metadataList;
    private List<String> names;

    @BeforeEach
    public void setUp() throws Exception {
        sampleMetadata = new HashMap<>();
        sampleMetadata.put("TitleDocMain", Arrays.asList(new DatabaseMetadataField("TitleDocMain", "Test Title", "", "", "")));
        sampleMetadata.put("Author", Arrays.asList(new DatabaseMetadataField("Author", "Test Author", "", "", "")));

        names = Arrays.asList("TitleDocMain", "Author");

        metadataList = new ArrayList<>();
        metadataList.add(new StringPair("TitleDocMain", "Test Title"));

    }

    @Test
    public void testDeleteMetadata() {
        try (MockedStatic<MetadataMysqlHelper> mockedMetadataMysqlHelper = Mockito.mockStatic(MetadataMysqlHelper.class)) {
            mockedMetadataMysqlHelper.when(() -> MetadataMysqlHelper.getDistinctMetadataNames()).thenReturn(names);
            mockedMetadataMysqlHelper.when(() -> MetadataMysqlHelper.getMetadata(Mockito.anyInt())).thenReturn(metadataList);
            mockedMetadataMysqlHelper.when(() -> MetadataMysqlHelper.getMetadataValue(Mockito.anyInt(), Mockito.anyString()))
                    .thenReturn("Test Title");
            mockedMetadataMysqlHelper.when(() -> MetadataMysqlHelper.getAllValuesForMetadata(Mockito.anyInt(), Mockito.anyString()))
                    .thenReturn("Test Title");
            mockedMetadataMysqlHelper.when(() -> MetadataMysqlHelper.getAllMetadataValues(Mockito.anyInt(), Mockito.anyString()))
                    .thenReturn(Arrays.asList("Test Title"));
            mockedMetadataMysqlHelper.when(() -> MetadataMysqlHelper.getAllProcessesWithMetadata(Mockito.anyString(), Mockito.anyString()))
                    .thenReturn(Arrays.asList(1, 2, 3));

            MetadataManager.deleteMetadata(1);

        }
    }

    @Test
    public void testInsertMetadata() {
        try (MockedStatic<MetadataMysqlHelper> mockedMetadataMysqlHelper = Mockito.mockStatic(MetadataMysqlHelper.class)) {
            mockedMetadataMysqlHelper.when(() -> MetadataMysqlHelper.getDistinctMetadataNames()).thenReturn(names);
            mockedMetadataMysqlHelper.when(() -> MetadataMysqlHelper.getMetadata(Mockito.anyInt())).thenReturn(metadataList);
            mockedMetadataMysqlHelper.when(() -> MetadataMysqlHelper.getMetadataValue(Mockito.anyInt(), Mockito.anyString()))
                    .thenReturn("Test Title");
            mockedMetadataMysqlHelper.when(() -> MetadataMysqlHelper.getAllValuesForMetadata(Mockito.anyInt(), Mockito.anyString()))
                    .thenReturn("Test Title");
            mockedMetadataMysqlHelper.when(() -> MetadataMysqlHelper.getAllMetadataValues(Mockito.anyInt(), Mockito.anyString()))
                    .thenReturn(Arrays.asList("Test Title"));
            mockedMetadataMysqlHelper.when(() -> MetadataMysqlHelper.getAllProcessesWithMetadata(Mockito.anyString(), Mockito.anyString()))
                    .thenReturn(Arrays.asList(1, 2, 3));

            MetadataManager.insertMetadata(1, sampleMetadata);

        }
    }

    @Test
    public void testUpdateMetadata() {
        try (MockedStatic<MetadataMysqlHelper> mockedMetadataMysqlHelper = Mockito.mockStatic(MetadataMysqlHelper.class)) {
            mockedMetadataMysqlHelper.when(() -> MetadataMysqlHelper.getDistinctMetadataNames()).thenReturn(names);
            mockedMetadataMysqlHelper.when(() -> MetadataMysqlHelper.getMetadata(Mockito.anyInt())).thenReturn(metadataList);
            mockedMetadataMysqlHelper.when(() -> MetadataMysqlHelper.getMetadataValue(Mockito.anyInt(), Mockito.anyString()))
                    .thenReturn("Test Title");
            mockedMetadataMysqlHelper.when(() -> MetadataMysqlHelper.getAllValuesForMetadata(Mockito.anyInt(), Mockito.anyString()))
                    .thenReturn("Test Title");
            mockedMetadataMysqlHelper.when(() -> MetadataMysqlHelper.getAllMetadataValues(Mockito.anyInt(), Mockito.anyString()))
                    .thenReturn(Arrays.asList("Test Title"));
            mockedMetadataMysqlHelper.when(() -> MetadataMysqlHelper.getAllProcessesWithMetadata(Mockito.anyString(), Mockito.anyString()))
                    .thenReturn(Arrays.asList(1, 2, 3));

            MetadataManager.updateMetadata(1, sampleMetadata);

        }
    }

    @Test
    public void testGetDistinctMetadataNames() {
        try (MockedStatic<MetadataMysqlHelper> mockedMetadataMysqlHelper = Mockito.mockStatic(MetadataMysqlHelper.class)) {
            mockedMetadataMysqlHelper.when(() -> MetadataMysqlHelper.getDistinctMetadataNames()).thenReturn(names);
            mockedMetadataMysqlHelper.when(() -> MetadataMysqlHelper.getMetadata(Mockito.anyInt())).thenReturn(metadataList);
            mockedMetadataMysqlHelper.when(() -> MetadataMysqlHelper.getMetadataValue(Mockito.anyInt(), Mockito.anyString()))
                    .thenReturn("Test Title");
            mockedMetadataMysqlHelper.when(() -> MetadataMysqlHelper.getAllValuesForMetadata(Mockito.anyInt(), Mockito.anyString()))
                    .thenReturn("Test Title");
            mockedMetadataMysqlHelper.when(() -> MetadataMysqlHelper.getAllMetadataValues(Mockito.anyInt(), Mockito.anyString()))
                    .thenReturn(Arrays.asList("Test Title"));
            mockedMetadataMysqlHelper.when(() -> MetadataMysqlHelper.getAllProcessesWithMetadata(Mockito.anyString(), Mockito.anyString()))
                    .thenReturn(Arrays.asList(1, 2, 3));

            List<String> result = MetadataManager.getDistinctMetadataNames();
            assertNotNull(result);
            assertEquals(2, result.size());

        }
    }

    @Test
    public void testGetMetadata() {
        try (MockedStatic<MetadataMysqlHelper> mockedMetadataMysqlHelper = Mockito.mockStatic(MetadataMysqlHelper.class)) {
            mockedMetadataMysqlHelper.when(() -> MetadataMysqlHelper.getDistinctMetadataNames()).thenReturn(names);
            mockedMetadataMysqlHelper.when(() -> MetadataMysqlHelper.getMetadata(Mockito.anyInt())).thenReturn(metadataList);
            mockedMetadataMysqlHelper.when(() -> MetadataMysqlHelper.getMetadataValue(Mockito.anyInt(), Mockito.anyString()))
                    .thenReturn("Test Title");
            mockedMetadataMysqlHelper.when(() -> MetadataMysqlHelper.getAllValuesForMetadata(Mockito.anyInt(), Mockito.anyString()))
                    .thenReturn("Test Title");
            mockedMetadataMysqlHelper.when(() -> MetadataMysqlHelper.getAllMetadataValues(Mockito.anyInt(), Mockito.anyString()))
                    .thenReturn(Arrays.asList("Test Title"));
            mockedMetadataMysqlHelper.when(() -> MetadataMysqlHelper.getAllProcessesWithMetadata(Mockito.anyString(), Mockito.anyString()))
                    .thenReturn(Arrays.asList(1, 2, 3));

            List<StringPair> result = MetadataManager.getMetadata(1);
            assertNotNull(result);
            assertEquals(1, result.size());

        }
    }

    @Test
    public void testGetMetadataValue() {
        try (MockedStatic<MetadataMysqlHelper> mockedMetadataMysqlHelper = Mockito.mockStatic(MetadataMysqlHelper.class)) {
            mockedMetadataMysqlHelper.when(() -> MetadataMysqlHelper.getDistinctMetadataNames()).thenReturn(names);
            mockedMetadataMysqlHelper.when(() -> MetadataMysqlHelper.getMetadata(Mockito.anyInt())).thenReturn(metadataList);
            mockedMetadataMysqlHelper.when(() -> MetadataMysqlHelper.getMetadataValue(Mockito.anyInt(), Mockito.anyString()))
                    .thenReturn("Test Title");
            mockedMetadataMysqlHelper.when(() -> MetadataMysqlHelper.getAllValuesForMetadata(Mockito.anyInt(), Mockito.anyString()))
                    .thenReturn("Test Title");
            mockedMetadataMysqlHelper.when(() -> MetadataMysqlHelper.getAllMetadataValues(Mockito.anyInt(), Mockito.anyString()))
                    .thenReturn(Arrays.asList("Test Title"));
            mockedMetadataMysqlHelper.when(() -> MetadataMysqlHelper.getAllProcessesWithMetadata(Mockito.anyString(), Mockito.anyString()))
                    .thenReturn(Arrays.asList(1, 2, 3));

            String result = MetadataManager.getMetadataValue(1, "TitleDocMain");
            assertEquals("Test Title", result);

        }
    }

    @Test
    public void testGetAllValuesForMetadata() {
        try (MockedStatic<MetadataMysqlHelper> mockedMetadataMysqlHelper = Mockito.mockStatic(MetadataMysqlHelper.class)) {
            mockedMetadataMysqlHelper.when(() -> MetadataMysqlHelper.getDistinctMetadataNames()).thenReturn(names);
            mockedMetadataMysqlHelper.when(() -> MetadataMysqlHelper.getMetadata(Mockito.anyInt())).thenReturn(metadataList);
            mockedMetadataMysqlHelper.when(() -> MetadataMysqlHelper.getMetadataValue(Mockito.anyInt(), Mockito.anyString()))
                    .thenReturn("Test Title");
            mockedMetadataMysqlHelper.when(() -> MetadataMysqlHelper.getAllValuesForMetadata(Mockito.anyInt(), Mockito.anyString()))
                    .thenReturn("Test Title");
            mockedMetadataMysqlHelper.when(() -> MetadataMysqlHelper.getAllMetadataValues(Mockito.anyInt(), Mockito.anyString()))
                    .thenReturn(Arrays.asList("Test Title"));
            mockedMetadataMysqlHelper.when(() -> MetadataMysqlHelper.getAllProcessesWithMetadata(Mockito.anyString(), Mockito.anyString()))
                    .thenReturn(Arrays.asList(1, 2, 3));

            String result = MetadataManager.getAllValuesForMetadata(1, "TitleDocMain");
            assertEquals("Test Title", result);

        }
    }

    @Test
    public void testGetAllMetadataValues() {
        try (MockedStatic<MetadataMysqlHelper> mockedMetadataMysqlHelper = Mockito.mockStatic(MetadataMysqlHelper.class)) {
            mockedMetadataMysqlHelper.when(() -> MetadataMysqlHelper.getDistinctMetadataNames()).thenReturn(names);
            mockedMetadataMysqlHelper.when(() -> MetadataMysqlHelper.getMetadata(Mockito.anyInt())).thenReturn(metadataList);
            mockedMetadataMysqlHelper.when(() -> MetadataMysqlHelper.getMetadataValue(Mockito.anyInt(), Mockito.anyString()))
                    .thenReturn("Test Title");
            mockedMetadataMysqlHelper.when(() -> MetadataMysqlHelper.getAllValuesForMetadata(Mockito.anyInt(), Mockito.anyString()))
                    .thenReturn("Test Title");
            mockedMetadataMysqlHelper.when(() -> MetadataMysqlHelper.getAllMetadataValues(Mockito.anyInt(), Mockito.anyString()))
                    .thenReturn(Arrays.asList("Test Title"));
            mockedMetadataMysqlHelper.when(() -> MetadataMysqlHelper.getAllProcessesWithMetadata(Mockito.anyString(), Mockito.anyString()))
                    .thenReturn(Arrays.asList(1, 2, 3));

            List<String> result = MetadataManager.getAllMetadataValues(1, "TitleDocMain");
            assertNotNull(result);
            assertTrue(result.contains("Test Title"));

        }
    }

    @Test
    public void testGetProcessesWithMetadata() {
        try (MockedStatic<MetadataMysqlHelper> mockedMetadataMysqlHelper = Mockito.mockStatic(MetadataMysqlHelper.class)) {
            mockedMetadataMysqlHelper.when(() -> MetadataMysqlHelper.getDistinctMetadataNames()).thenReturn(names);
            mockedMetadataMysqlHelper.when(() -> MetadataMysqlHelper.getMetadata(Mockito.anyInt())).thenReturn(metadataList);
            mockedMetadataMysqlHelper.when(() -> MetadataMysqlHelper.getMetadataValue(Mockito.anyInt(), Mockito.anyString()))
                    .thenReturn("Test Title");
            mockedMetadataMysqlHelper.when(() -> MetadataMysqlHelper.getAllValuesForMetadata(Mockito.anyInt(), Mockito.anyString()))
                    .thenReturn("Test Title");
            mockedMetadataMysqlHelper.when(() -> MetadataMysqlHelper.getAllMetadataValues(Mockito.anyInt(), Mockito.anyString()))
                    .thenReturn(Arrays.asList("Test Title"));
            mockedMetadataMysqlHelper.when(() -> MetadataMysqlHelper.getAllProcessesWithMetadata(Mockito.anyString(), Mockito.anyString()))
                    .thenReturn(Arrays.asList(1, 2, 3));

            List<Integer> result = MetadataManager.getProcessesWithMetadata("TitleDocMain", "Test Title");
            assertNotNull(result);
            assertEquals(3, result.size());

        }
    }

}
