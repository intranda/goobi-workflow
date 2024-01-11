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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.Collections;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.config.ConfigProjectsTest;
import de.sub.goobi.helper.ShellScript;
import de.sub.goobi.persistence.managers.HarvesterRepositoryManager;
import io.goobi.workflow.api.connection.HttpUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ HttpUtils.class, HarvesterRepositoryManager.class, ShellScript.class })
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*" })
public class RepositoryTest extends AbstractTest {

    @Before
    public void setUp() throws Exception {
        Path template = Paths.get(ConfigProjectsTest.class.getClassLoader().getResource(".").getFile());
        // for junit tests in eclipse
        Path samplefiles = Paths.get(template.getParent().getParent().toString() + "/src/test/resources/samplefiles/");
        if (!Files.exists(samplefiles)) {
            samplefiles = Paths.get("target/test-classes/samplefiles/"); // to run mvn test from cli or in jenkins
        }

        PowerMock.mockStatic(HarvesterRepositoryManager.class);
        PowerMock.mockStatic(HttpUtils.class);

        String dublinCoreRecord = Files.readString(Paths.get(samplefiles.toString(), "oai_dc.xml"));
        EasyMock.expect(HttpUtils.getStringFromUrl(EasyMock.anyString())).andReturn(dublinCoreRecord).anyTimes();
        HarvesterRepositoryManager.updateLastHarvestingTime(EasyMock.anyInt(), EasyMock.anyObject());
        EasyMock.expect(HarvesterRepositoryManager.addRecords(EasyMock.anyObject(), EasyMock.anyBoolean())).andReturn(1).anyTimes();

        HarvesterRepositoryManager.changeStatusOfRepository(EasyMock.anyObject());

        ShellScript script = PowerMock.createMockAndExpectNew(ShellScript.class, EasyMock.anyObject(Path.class));
        EasyMock.expect(script.run(EasyMock.anyObject())).andReturn(0).anyTimes();
        EasyMock.expect(script.getStdOut()).andReturn(Collections.emptyList()).anyTimes();

        PowerMock.replay(script);
        PowerMock.replay(ShellScript.class);

        PowerMock.replay(HarvesterRepositoryManager.class);
        PowerMock.replay(HttpUtils.class);
    }

    @Test
    public void testConstructor() {
        Repository fixture = new Repository();
        assertNotNull(fixture);
    }

    @Test
    public void testAllArgsConstructor() {
        Repository fixture = new Repository(1, "name", "url", "exportFolderPath", "scriptPath", null, 24, 0, false, false, null, null, null);
        assertNotNull(fixture);
    }

    @Test
    public void testId() {
        Repository fixture = new Repository();
        assertNotNull(fixture);
        assertNull(fixture.getId());
        fixture.setId(1);
        assertEquals(1, fixture.getId().intValue());
    }

    @Test
    public void testName() {
        Repository fixture = new Repository();
        assertNotNull(fixture);
        assertNull(fixture.getName());
        fixture.setName("fixture");
        assertEquals("fixture", fixture.getName());
    }

    @Test
    public void testUrl() {
        Repository fixture = new Repository();
        assertNotNull(fixture);
        assertNull(fixture.getUrl());
        fixture.setUrl("fixture");
        assertEquals("fixture", fixture.getUrl());
    }

    @Test
    public void testParameter() {
        Repository fixture = new Repository();
        assertNotNull(fixture);
        assertTrue(fixture.getParameter().isEmpty());
        fixture.getParameter().put("key", "value");
        assertFalse(fixture.getParameter().isEmpty());
        assertEquals("value", fixture.getParameter().get("key"));
    }

    @Test
    public void testLastHarvest() {
        Repository fixture = new Repository();
        assertNotNull(fixture);
        assertNull(fixture.getLastHarvest());
        fixture.setLastHarvest(new Timestamp(1l));
        assertEquals(1l, fixture.getLastHarvest().getTime());
    }

    @Test
    public void testFrequency() {
        Repository fixture = new Repository();
        assertNotNull(fixture);
        assertEquals(24, fixture.getFrequency());
        fixture.setFrequency(1);
        assertEquals(1, fixture.getFrequency());
    }

    @Test
    public void testDelay() {
        Repository fixture = new Repository();
        assertNotNull(fixture);
        assertEquals(0, fixture.getDelay());
        fixture.setDelay(1);
        assertEquals(1, fixture.getDelay());
    }

    @Test
    public void testEnabled() {
        Repository fixture = new Repository();
        assertNotNull(fixture);
        assertTrue(fixture.isEnabled());
        fixture.setEnabled(false);
        assertFalse(fixture.isEnabled());
    }

    @Test
    public void testExportFolderPath() {
        Repository fixture = new Repository();
        assertNotNull(fixture);
        assertNull(fixture.getExportFolderPath());
        fixture.setExportFolderPath("fixture");
        assertEquals("fixture", fixture.getExportFolderPath());
    }

    @Test
    public void testScriptPath() {
        Repository fixture = new Repository();
        assertNotNull(fixture);
        assertNull(fixture.getScriptPath());
        fixture.setScriptPath("fixture");
        assertEquals("fixture", fixture.getScriptPath());
    }

    @Test
    public void testAllowUpdates() {
        Repository fixture = new Repository();
        assertNotNull(fixture);
        assertTrue(fixture.isAllowUpdates());
        fixture.setAllowUpdates(false);
        assertFalse(fixture.isAllowUpdates());
    }

    @Test
    public void testAutoExport() {
        Repository fixture = new Repository();
        assertNotNull(fixture);
        assertTrue(fixture.isAutoExport());
        fixture.setAutoExport(false);
        assertFalse(fixture.isAutoExport());
    }

    @Test
    public void testRepositoryType() {
        Repository fixture = new Repository();
        assertNotNull(fixture);
        assertNull(fixture.getRepositoryType());
        fixture.setRepositoryType("fixture");
        assertEquals("fixture", fixture.getRepositoryType());
    }

    @Test
    public void testGoobiImport() {
        Repository fixture = new Repository();
        assertNotNull(fixture);
        assertFalse(fixture.isGoobiImport());
        fixture.setGoobiImport(true);
        assertTrue(fixture.isGoobiImport());
    }

    @Test
    public void testImportProjectName() {
        Repository fixture = new Repository();
        assertNotNull(fixture);
        assertNull(fixture.getImportProjectName());
        fixture.setImportProjectName("fixture");
        assertEquals("fixture", fixture.getImportProjectName());
    }

    @Test
    public void testProcessTemplateName() {
        Repository fixture = new Repository();
        assertNotNull(fixture);
        assertNull(fixture.getProcessTemplateName());
        fixture.setProcessTemplateName("fixture");
        assertEquals("fixture", fixture.getProcessTemplateName());
    }

    @Test
    public void testFileformat() {
        Repository fixture = new Repository();
        assertNotNull(fixture);
        assertNull(fixture.getFileformat());
        fixture.setFileformat("fixture");
        assertEquals("fixture", fixture.getFileformat());
    }

    @Test
    public void testStatus() {
        Repository fixture = new Repository();
        assertNotNull(fixture);
        assertTrue(fixture.isEnabled());
        assertEquals("deactivate", fixture.getStatus());
        fixture.changeStatus();
        assertFalse(fixture.isEnabled());
        assertEquals("activate", fixture.getStatus());
    }

    @Test
    public void testHarvest() throws Exception {
        Repository fixture = new Repository();
        assertNotNull(fixture);
        fixture.setRepositoryType("type");
        int val = fixture.harvest(1);
        assertEquals(0, val);
    }

    @Test
    public void testHarvestOai() throws Exception {
        Repository fixture = new Repository();
        assertNotNull(fixture);
        fixture.setRepositoryType("oai");
        int val = fixture.harvest(1);
        assertEquals(1, val);
    }

    @Test
    public void testHarvestIa() throws Exception {
        Repository fixture = new Repository();
        assertNotNull(fixture);
        fixture.setUrl("url");
        fixture.setRepositoryType("ia");
        int val = fixture.harvest(1);
        assertEquals(0, val);
    }

    // @Test
    public void testHarvestIaCli() throws Exception {
        Repository fixture = new Repository();
        assertNotNull(fixture);
        fixture.setUrl("url");
        fixture.setRepositoryType("ia cli");
        int val = fixture.harvest(1);
        assertEquals(0, val);
    }

}
