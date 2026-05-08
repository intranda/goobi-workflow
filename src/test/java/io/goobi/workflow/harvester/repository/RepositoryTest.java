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
package io.goobi.workflow.harvester.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.config.ConfigProjectsTest;
import de.sub.goobi.persistence.managers.HarvesterRepositoryManager;
import io.goobi.workflow.api.connection.HttpUtils;

@ExtendWith(MockitoExtension.class)
public class RepositoryTest extends AbstractTest {

    private String dublinCoreRecord;

    @BeforeEach
    public void setUp() throws Exception {
        Path template = Paths.get(ConfigProjectsTest.class.getClassLoader().getResource(".").getFile());
        // for junit tests in eclipse
        Path samplefiles = Paths.get(template.getParent().getParent().toString() + "/src/test/resources/samplefiles/");
        if (!Files.exists(samplefiles)) {
            samplefiles = Paths.get("target/test-classes/samplefiles/"); // to run mvn test from cli or in jenkins
        }

        dublinCoreRecord = Files.readString(Paths.get(samplefiles.toString(), "oai_dc.xml"));
    }

    @Test
    public void testConstructor() {
        try (MockedStatic<HarvesterRepositoryManager> mockedHarvesterRepositoryManager = Mockito.mockStatic(HarvesterRepositoryManager.class);
                MockedStatic<HttpUtils> mockedHttpUtils = Mockito.mockStatic(HttpUtils.class)) {
            mockedHttpUtils.when(() -> HttpUtils.getStringFromUrl(Mockito.anyString())).thenReturn(dublinCoreRecord);
            mockedHarvesterRepositoryManager.when(() -> HarvesterRepositoryManager.addRecords(Mockito.any(), Mockito.anyBoolean())).thenReturn(1);

            Repository fixture = new Repository();
            assertNotNull(fixture);

        }
    }

    @Test
    public void testAllArgsConstructor() {
        try (MockedStatic<HarvesterRepositoryManager> mockedHarvesterRepositoryManager = Mockito.mockStatic(HarvesterRepositoryManager.class);
                MockedStatic<HttpUtils> mockedHttpUtils = Mockito.mockStatic(HttpUtils.class)) {
            mockedHttpUtils.when(() -> HttpUtils.getStringFromUrl(Mockito.anyString())).thenReturn(dublinCoreRecord);
            mockedHarvesterRepositoryManager.when(() -> HarvesterRepositoryManager.addRecords(Mockito.any(), Mockito.anyBoolean())).thenReturn(1);

            Repository fixture = new Repository(1, "name", "url", "exportFolderPath", "scriptPath", null, 24, 0, false, false, null, null, null);
            assertNotNull(fixture);

        }
    }

    @Test
    public void testId() {
        try (MockedStatic<HarvesterRepositoryManager> mockedHarvesterRepositoryManager = Mockito.mockStatic(HarvesterRepositoryManager.class);
                MockedStatic<HttpUtils> mockedHttpUtils = Mockito.mockStatic(HttpUtils.class)) {
            mockedHttpUtils.when(() -> HttpUtils.getStringFromUrl(Mockito.anyString())).thenReturn(dublinCoreRecord);
            mockedHarvesterRepositoryManager.when(() -> HarvesterRepositoryManager.addRecords(Mockito.any(), Mockito.anyBoolean())).thenReturn(1);

            Repository fixture = new Repository();
            assertNotNull(fixture);
            assertNull(fixture.getId());
            fixture.setId(1);
            assertEquals(1, fixture.getId().intValue());

        }
    }

    @Test
    public void testName() {
        try (MockedStatic<HarvesterRepositoryManager> mockedHarvesterRepositoryManager = Mockito.mockStatic(HarvesterRepositoryManager.class);
                MockedStatic<HttpUtils> mockedHttpUtils = Mockito.mockStatic(HttpUtils.class)) {
            mockedHttpUtils.when(() -> HttpUtils.getStringFromUrl(Mockito.anyString())).thenReturn(dublinCoreRecord);
            mockedHarvesterRepositoryManager.when(() -> HarvesterRepositoryManager.addRecords(Mockito.any(), Mockito.anyBoolean())).thenReturn(1);

            Repository fixture = new Repository();
            assertNotNull(fixture);
            assertNull(fixture.getName());
            fixture.setName("fixture");
            assertEquals("fixture", fixture.getName());

        }
    }

    @Test
    public void testUrl() {
        try (MockedStatic<HarvesterRepositoryManager> mockedHarvesterRepositoryManager = Mockito.mockStatic(HarvesterRepositoryManager.class);
                MockedStatic<HttpUtils> mockedHttpUtils = Mockito.mockStatic(HttpUtils.class)) {
            mockedHttpUtils.when(() -> HttpUtils.getStringFromUrl(Mockito.anyString())).thenReturn(dublinCoreRecord);
            mockedHarvesterRepositoryManager.when(() -> HarvesterRepositoryManager.addRecords(Mockito.any(), Mockito.anyBoolean())).thenReturn(1);

            Repository fixture = new Repository();
            assertNotNull(fixture);
            assertNull(fixture.getUrl());
            fixture.setUrl("fixture");
            assertEquals("fixture", fixture.getUrl());

        }
    }

    @Test
    public void testParameter() {
        try (MockedStatic<HarvesterRepositoryManager> mockedHarvesterRepositoryManager = Mockito.mockStatic(HarvesterRepositoryManager.class);
                MockedStatic<HttpUtils> mockedHttpUtils = Mockito.mockStatic(HttpUtils.class)) {
            mockedHttpUtils.when(() -> HttpUtils.getStringFromUrl(Mockito.anyString())).thenReturn(dublinCoreRecord);
            mockedHarvesterRepositoryManager.when(() -> HarvesterRepositoryManager.addRecords(Mockito.any(), Mockito.anyBoolean())).thenReturn(1);

            Repository fixture = new Repository();
            assertNotNull(fixture);
            assertTrue(fixture.getParameter().isEmpty());
            fixture.getParameter().put("key", "value");
            assertFalse(fixture.getParameter().isEmpty());
            assertEquals("value", fixture.getParameter().get("key"));

        }
    }

    @Test
    public void testLastHarvest() {
        try (MockedStatic<HarvesterRepositoryManager> mockedHarvesterRepositoryManager = Mockito.mockStatic(HarvesterRepositoryManager.class);
                MockedStatic<HttpUtils> mockedHttpUtils = Mockito.mockStatic(HttpUtils.class)) {
            mockedHttpUtils.when(() -> HttpUtils.getStringFromUrl(Mockito.anyString())).thenReturn(dublinCoreRecord);
            mockedHarvesterRepositoryManager.when(() -> HarvesterRepositoryManager.addRecords(Mockito.any(), Mockito.anyBoolean())).thenReturn(1);

            Repository fixture = new Repository();
            assertNotNull(fixture);
            assertNull(fixture.getLastHarvest());
            fixture.setLastHarvest(new Timestamp(1L));
            assertEquals(1L, fixture.getLastHarvest().getTime());

        }
    }

    @Test
    public void testFrequency() {
        try (MockedStatic<HarvesterRepositoryManager> mockedHarvesterRepositoryManager = Mockito.mockStatic(HarvesterRepositoryManager.class);
                MockedStatic<HttpUtils> mockedHttpUtils = Mockito.mockStatic(HttpUtils.class)) {
            mockedHttpUtils.when(() -> HttpUtils.getStringFromUrl(Mockito.anyString())).thenReturn(dublinCoreRecord);
            mockedHarvesterRepositoryManager.when(() -> HarvesterRepositoryManager.addRecords(Mockito.any(), Mockito.anyBoolean())).thenReturn(1);

            Repository fixture = new Repository();
            assertNotNull(fixture);
            assertEquals(24, fixture.getFrequency());
            fixture.setFrequency(1);
            assertEquals(1, fixture.getFrequency());

        }
    }

    @Test
    public void testDelay() {
        try (MockedStatic<HarvesterRepositoryManager> mockedHarvesterRepositoryManager = Mockito.mockStatic(HarvesterRepositoryManager.class);
                MockedStatic<HttpUtils> mockedHttpUtils = Mockito.mockStatic(HttpUtils.class)) {
            mockedHttpUtils.when(() -> HttpUtils.getStringFromUrl(Mockito.anyString())).thenReturn(dublinCoreRecord);
            mockedHarvesterRepositoryManager.when(() -> HarvesterRepositoryManager.addRecords(Mockito.any(), Mockito.anyBoolean())).thenReturn(1);

            Repository fixture = new Repository();
            assertNotNull(fixture);
            assertEquals(0, fixture.getDelay());
            fixture.setDelay(1);
            assertEquals(1, fixture.getDelay());

        }
    }

    @Test
    public void testEnabled() {
        try (MockedStatic<HarvesterRepositoryManager> mockedHarvesterRepositoryManager = Mockito.mockStatic(HarvesterRepositoryManager.class);
                MockedStatic<HttpUtils> mockedHttpUtils = Mockito.mockStatic(HttpUtils.class)) {
            mockedHttpUtils.when(() -> HttpUtils.getStringFromUrl(Mockito.anyString())).thenReturn(dublinCoreRecord);
            mockedHarvesterRepositoryManager.when(() -> HarvesterRepositoryManager.addRecords(Mockito.any(), Mockito.anyBoolean())).thenReturn(1);

            Repository fixture = new Repository();
            assertNotNull(fixture);
            assertTrue(fixture.isEnabled());
            fixture.setEnabled(false);
            assertFalse(fixture.isEnabled());

        }
    }

    @Test
    public void testExportFolderPath() {
        try (MockedStatic<HarvesterRepositoryManager> mockedHarvesterRepositoryManager = Mockito.mockStatic(HarvesterRepositoryManager.class);
                MockedStatic<HttpUtils> mockedHttpUtils = Mockito.mockStatic(HttpUtils.class)) {
            mockedHttpUtils.when(() -> HttpUtils.getStringFromUrl(Mockito.anyString())).thenReturn(dublinCoreRecord);
            mockedHarvesterRepositoryManager.when(() -> HarvesterRepositoryManager.addRecords(Mockito.any(), Mockito.anyBoolean())).thenReturn(1);

            Repository fixture = new Repository();
            assertNotNull(fixture);
            assertNull(fixture.getExportFolderPath());
            fixture.setExportFolderPath("fixture");
            assertEquals("fixture", fixture.getExportFolderPath());

        }
    }

    @Test
    public void testScriptPath() {
        try (MockedStatic<HarvesterRepositoryManager> mockedHarvesterRepositoryManager = Mockito.mockStatic(HarvesterRepositoryManager.class);
                MockedStatic<HttpUtils> mockedHttpUtils = Mockito.mockStatic(HttpUtils.class)) {
            mockedHttpUtils.when(() -> HttpUtils.getStringFromUrl(Mockito.anyString())).thenReturn(dublinCoreRecord);
            mockedHarvesterRepositoryManager.when(() -> HarvesterRepositoryManager.addRecords(Mockito.any(), Mockito.anyBoolean())).thenReturn(1);

            Repository fixture = new Repository();
            assertNotNull(fixture);
            assertNull(fixture.getScriptPath());
            fixture.setScriptPath("fixture");
            assertEquals("fixture", fixture.getScriptPath());

        }
    }

    @Test
    public void testAllowUpdates() {
        try (MockedStatic<HarvesterRepositoryManager> mockedHarvesterRepositoryManager = Mockito.mockStatic(HarvesterRepositoryManager.class);
                MockedStatic<HttpUtils> mockedHttpUtils = Mockito.mockStatic(HttpUtils.class)) {
            mockedHttpUtils.when(() -> HttpUtils.getStringFromUrl(Mockito.anyString())).thenReturn(dublinCoreRecord);
            mockedHarvesterRepositoryManager.when(() -> HarvesterRepositoryManager.addRecords(Mockito.any(), Mockito.anyBoolean())).thenReturn(1);

            Repository fixture = new Repository();
            assertNotNull(fixture);
            assertTrue(fixture.isAllowUpdates());
            fixture.setAllowUpdates(false);
            assertFalse(fixture.isAllowUpdates());

        }
    }

    @Test
    public void testAutoExport() {
        try (MockedStatic<HarvesterRepositoryManager> mockedHarvesterRepositoryManager = Mockito.mockStatic(HarvesterRepositoryManager.class);
                MockedStatic<HttpUtils> mockedHttpUtils = Mockito.mockStatic(HttpUtils.class)) {
            mockedHttpUtils.when(() -> HttpUtils.getStringFromUrl(Mockito.anyString())).thenReturn(dublinCoreRecord);
            mockedHarvesterRepositoryManager.when(() -> HarvesterRepositoryManager.addRecords(Mockito.any(), Mockito.anyBoolean())).thenReturn(1);

            Repository fixture = new Repository();
            assertNotNull(fixture);
            assertTrue(fixture.isAutoExport());
            fixture.setAutoExport(false);
            assertFalse(fixture.isAutoExport());

        }
    }

    @Test
    public void testRepositoryType() {
        try (MockedStatic<HarvesterRepositoryManager> mockedHarvesterRepositoryManager = Mockito.mockStatic(HarvesterRepositoryManager.class);
                MockedStatic<HttpUtils> mockedHttpUtils = Mockito.mockStatic(HttpUtils.class)) {
            mockedHttpUtils.when(() -> HttpUtils.getStringFromUrl(Mockito.anyString())).thenReturn(dublinCoreRecord);
            mockedHarvesterRepositoryManager.when(() -> HarvesterRepositoryManager.addRecords(Mockito.any(), Mockito.anyBoolean())).thenReturn(1);

            Repository fixture = new Repository();
            assertNotNull(fixture);
            assertNull(fixture.getRepositoryType());
            fixture.setRepositoryType("fixture");
            assertEquals("fixture", fixture.getRepositoryType());

        }
    }

    @Test
    public void testGoobiImport() {
        try (MockedStatic<HarvesterRepositoryManager> mockedHarvesterRepositoryManager = Mockito.mockStatic(HarvesterRepositoryManager.class);
                MockedStatic<HttpUtils> mockedHttpUtils = Mockito.mockStatic(HttpUtils.class)) {
            mockedHttpUtils.when(() -> HttpUtils.getStringFromUrl(Mockito.anyString())).thenReturn(dublinCoreRecord);
            mockedHarvesterRepositoryManager.when(() -> HarvesterRepositoryManager.addRecords(Mockito.any(), Mockito.anyBoolean())).thenReturn(1);

            Repository fixture = new Repository();
            assertNotNull(fixture);
            assertFalse(fixture.isGoobiImport());
            fixture.setGoobiImport(true);
            assertTrue(fixture.isGoobiImport());

        }
    }

    @Test
    public void testImportProjectName() {
        try (MockedStatic<HarvesterRepositoryManager> mockedHarvesterRepositoryManager = Mockito.mockStatic(HarvesterRepositoryManager.class);
                MockedStatic<HttpUtils> mockedHttpUtils = Mockito.mockStatic(HttpUtils.class)) {
            mockedHttpUtils.when(() -> HttpUtils.getStringFromUrl(Mockito.anyString())).thenReturn(dublinCoreRecord);
            mockedHarvesterRepositoryManager.when(() -> HarvesterRepositoryManager.addRecords(Mockito.any(), Mockito.anyBoolean())).thenReturn(1);

            Repository fixture = new Repository();
            assertNotNull(fixture);
            assertNull(fixture.getImportProjectName());
            fixture.setImportProjectName("fixture");
            assertEquals("fixture", fixture.getImportProjectName());

        }
    }

    @Test
    public void testProcessTemplateName() {
        try (MockedStatic<HarvesterRepositoryManager> mockedHarvesterRepositoryManager = Mockito.mockStatic(HarvesterRepositoryManager.class);
                MockedStatic<HttpUtils> mockedHttpUtils = Mockito.mockStatic(HttpUtils.class)) {
            mockedHttpUtils.when(() -> HttpUtils.getStringFromUrl(Mockito.anyString())).thenReturn(dublinCoreRecord);
            mockedHarvesterRepositoryManager.when(() -> HarvesterRepositoryManager.addRecords(Mockito.any(), Mockito.anyBoolean())).thenReturn(1);

            Repository fixture = new Repository();
            assertNotNull(fixture);
            assertNull(fixture.getProcessTemplateName());
            fixture.setProcessTemplateName("fixture");
            assertEquals("fixture", fixture.getProcessTemplateName());

        }
    }

    @Test
    public void testFileformat() {
        try (MockedStatic<HarvesterRepositoryManager> mockedHarvesterRepositoryManager = Mockito.mockStatic(HarvesterRepositoryManager.class);
                MockedStatic<HttpUtils> mockedHttpUtils = Mockito.mockStatic(HttpUtils.class)) {
            mockedHttpUtils.when(() -> HttpUtils.getStringFromUrl(Mockito.anyString())).thenReturn(dublinCoreRecord);
            mockedHarvesterRepositoryManager.when(() -> HarvesterRepositoryManager.addRecords(Mockito.any(), Mockito.anyBoolean())).thenReturn(1);

            Repository fixture = new Repository();
            assertNotNull(fixture);
            assertNull(fixture.getFileformat());
            fixture.setFileformat("fixture");
            assertEquals("fixture", fixture.getFileformat());

        }
    }

    @Test
    public void testStatus() {
        try (MockedStatic<HarvesterRepositoryManager> mockedHarvesterRepositoryManager = Mockito.mockStatic(HarvesterRepositoryManager.class);
                MockedStatic<HttpUtils> mockedHttpUtils = Mockito.mockStatic(HttpUtils.class)) {
            mockedHttpUtils.when(() -> HttpUtils.getStringFromUrl(Mockito.anyString())).thenReturn(dublinCoreRecord);
            mockedHarvesterRepositoryManager.when(() -> HarvesterRepositoryManager.addRecords(Mockito.any(), Mockito.anyBoolean())).thenReturn(1);

            Repository fixture = new Repository();
            assertNotNull(fixture);
            assertTrue(fixture.isEnabled());
            assertEquals("deactivate", fixture.getStatus());
            fixture.changeStatus();
            assertFalse(fixture.isEnabled());
            assertEquals("activate", fixture.getStatus());

        }
    }

    @Test
    public void testHarvest() throws Exception {
        try (MockedStatic<HarvesterRepositoryManager> mockedHarvesterRepositoryManager = Mockito.mockStatic(HarvesterRepositoryManager.class);
                MockedStatic<HttpUtils> mockedHttpUtils = Mockito.mockStatic(HttpUtils.class)) {
            mockedHttpUtils.when(() -> HttpUtils.getStringFromUrl(Mockito.anyString())).thenReturn(dublinCoreRecord);
            mockedHarvesterRepositoryManager.when(() -> HarvesterRepositoryManager.addRecords(Mockito.any(), Mockito.anyBoolean())).thenReturn(1);

            Repository fixture = new Repository();
            assertNotNull(fixture);
            fixture.setRepositoryType("type");
            int val = fixture.harvest(1);
            assertEquals(0, val);

        }
    }

    @Test
    public void testHarvestOai() throws Exception {
        try (MockedStatic<HarvesterRepositoryManager> mockedHarvesterRepositoryManager = Mockito.mockStatic(HarvesterRepositoryManager.class);
                MockedStatic<HttpUtils> mockedHttpUtils = Mockito.mockStatic(HttpUtils.class)) {
            mockedHttpUtils.when(() -> HttpUtils.getStringFromUrl(Mockito.anyString())).thenReturn(dublinCoreRecord);
            mockedHarvesterRepositoryManager.when(() -> HarvesterRepositoryManager.addRecords(Mockito.any(), Mockito.anyBoolean())).thenReturn(1);

            Repository fixture = new Repository();
            assertNotNull(fixture);
            fixture.setRepositoryType("oai");
            int val = fixture.harvest(1);
            assertEquals(1, val);

        }
    }

    @Test
    public void testHarvestIa() throws Exception {
        try (MockedStatic<HarvesterRepositoryManager> mockedHarvesterRepositoryManager = Mockito.mockStatic(HarvesterRepositoryManager.class);
                MockedStatic<HttpUtils> mockedHttpUtils = Mockito.mockStatic(HttpUtils.class)) {
            mockedHttpUtils.when(() -> HttpUtils.getStringFromUrl(Mockito.anyString())).thenReturn(dublinCoreRecord);
            mockedHarvesterRepositoryManager.when(() -> HarvesterRepositoryManager.addRecords(Mockito.any(), Mockito.anyBoolean())).thenReturn(1);

            Repository fixture = new Repository();
            assertNotNull(fixture);
            fixture.setUrl("url");
            fixture.setRepositoryType("ia");
            int val = fixture.harvest(1);
            assertEquals(0, val);

        }
    }

    // @Test
    public void testHarvestIaCli() throws Exception {
        try (MockedStatic<HarvesterRepositoryManager> mockedHarvesterRepositoryManager = Mockito.mockStatic(HarvesterRepositoryManager.class);
                MockedStatic<HttpUtils> mockedHttpUtils = Mockito.mockStatic(HttpUtils.class)) {
            mockedHttpUtils.when(() -> HttpUtils.getStringFromUrl(Mockito.anyString())).thenReturn(dublinCoreRecord);
            mockedHarvesterRepositoryManager.when(() -> HarvesterRepositoryManager.addRecords(Mockito.any(), Mockito.anyBoolean())).thenReturn(1);

            Repository fixture = new Repository();
            assertNotNull(fixture);
            fixture.setUrl("url");
            fixture.setRepositoryType("ia cli");
            int val = fixture.harvest(1);
            assertEquals(0, val);

        }
    }

    @Test
    public void testTestmode() throws Exception {
        try (MockedStatic<HarvesterRepositoryManager> mockedHarvesterRepositoryManager = Mockito.mockStatic(HarvesterRepositoryManager.class);
                MockedStatic<HttpUtils> mockedHttpUtils = Mockito.mockStatic(HttpUtils.class)) {
            mockedHttpUtils.when(() -> HttpUtils.getStringFromUrl(Mockito.anyString())).thenReturn(dublinCoreRecord);
            mockedHarvesterRepositoryManager.when(() -> HarvesterRepositoryManager.addRecords(Mockito.any(), Mockito.anyBoolean())).thenReturn(1);

            Repository fixture = new Repository();
            assertNotNull(fixture);
            assertFalse(fixture.isTestMode());
            fixture.setTestMode(true);
            assertTrue(fixture.isTestMode());

        }
    }

    @Test
    public void testStartDate() {
        try (MockedStatic<HarvesterRepositoryManager> mockedHarvesterRepositoryManager = Mockito.mockStatic(HarvesterRepositoryManager.class);
                MockedStatic<HttpUtils> mockedHttpUtils = Mockito.mockStatic(HttpUtils.class)) {
            mockedHttpUtils.when(() -> HttpUtils.getStringFromUrl(Mockito.anyString())).thenReturn(dublinCoreRecord);
            mockedHarvesterRepositoryManager.when(() -> HarvesterRepositoryManager.addRecords(Mockito.any(), Mockito.anyBoolean())).thenReturn(1);

            Repository fixture = new Repository();
            assertNotNull(fixture);
            assertNull(fixture.getStartDate());
            fixture.setStartDate("fixture");
            assertEquals("fixture", fixture.getStartDate());

        }
    }

    @Test
    public void testEndDate() {
        try (MockedStatic<HarvesterRepositoryManager> mockedHarvesterRepositoryManager = Mockito.mockStatic(HarvesterRepositoryManager.class);
                MockedStatic<HttpUtils> mockedHttpUtils = Mockito.mockStatic(HttpUtils.class)) {
            mockedHttpUtils.when(() -> HttpUtils.getStringFromUrl(Mockito.anyString())).thenReturn(dublinCoreRecord);
            mockedHarvesterRepositoryManager.when(() -> HarvesterRepositoryManager.addRecords(Mockito.any(), Mockito.anyBoolean())).thenReturn(1);

            Repository fixture = new Repository();
            assertNotNull(fixture);
            assertNull(fixture.getEndDate());
            fixture.setEndDate("fixture");
            assertEquals("fixture", fixture.getEndDate());

        }
    }

    @Test
    public void testHarvestWithFromUntil() throws Exception {
        try (MockedStatic<HarvesterRepositoryManager> mockedHarvesterRepositoryManager = Mockito.mockStatic(HarvesterRepositoryManager.class);
                MockedStatic<HttpUtils> mockedHttpUtils = Mockito.mockStatic(HttpUtils.class)) {
            mockedHttpUtils.when(() -> HttpUtils.getStringFromUrl(Mockito.anyString())).thenReturn(dublinCoreRecord);
            mockedHarvesterRepositoryManager.when(() -> HarvesterRepositoryManager.addRecords(Mockito.any(), Mockito.anyBoolean())).thenReturn(1);

            Repository fixture = new Repository();
            assertNotNull(fixture);
            fixture.setRepositoryType("oai");
            fixture.setStartDate("2020-01-01T00:00:00Z");
            fixture.setEndDate("2020-12-31T23:59:59Z");
            int val = fixture.harvest(1);
            assertEquals(1, val);

        }
    }

    @Test
    public void testHarvestTestmode() throws Exception {
        try (MockedStatic<HarvesterRepositoryManager> mockedHarvesterRepositoryManager = Mockito.mockStatic(HarvesterRepositoryManager.class);
                MockedStatic<HttpUtils> mockedHttpUtils = Mockito.mockStatic(HttpUtils.class)) {
            mockedHttpUtils.when(() -> HttpUtils.getStringFromUrl(Mockito.anyString())).thenReturn(dublinCoreRecord);
            mockedHarvesterRepositoryManager.when(() -> HarvesterRepositoryManager.addRecords(Mockito.any(), Mockito.anyBoolean())).thenReturn(1);

            Repository fixture = new Repository();
            assertNotNull(fixture);
            fixture.setRepositoryType("oai");
            fixture.setTestMode(true);
            int val = fixture.harvest(1);
            assertEquals(1, val);

        }
    }
}
