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
package org.goobi.managedbeans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.easymock.EasyMock;
import org.goobi.beans.Batch;
import org.goobi.beans.GoobiProperty.PropertyOwnerType;
import org.goobi.beans.Process;
import org.goobi.beans.Project;
import org.goobi.production.flow.statistics.hibernate.FilterHelper;
import org.goobi.production.properties.PropertyParser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.JournalManager;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.PropertyManager;
import jakarta.faces.model.SelectItem;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ProcessManager.class, JournalManager.class, Helper.class, FilterHelper.class, ConfigurationHelper.class,
        PropertyParser.class, PropertyManager.class })
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*" })
public class BatchBeanTest extends AbstractTest {

    private Batch batch1;
    private Batch batch2;
    private Process process1;
    private Process process2;

    @Before
    public void setUp() throws Exception {
        batch1 = new Batch();
        batch1.setBatchId(1);
        batch1.setBatchName("TestBatch1");
        batch1.setBatchLabel("TestBatch1 (2 Prozesse)");

        batch2 = new Batch();
        batch2.setBatchId(2);
        batch2.setBatchName("TestBatch2");
        batch2.setBatchLabel("TestBatch2 (1 Prozesse)");

        Project project = new Project();
        project.setTitel("TestProject");

        process1 = new Process();
        process1.setId(1);
        process1.setTitel("Process1");
        process1.setBatch(batch1);
        process1.setProjekt(project);

        process2 = new Process();
        process2.setId(2);
        process2.setTitel("Process2");
        process2.setBatch(batch1);
        process2.setProjekt(project);

        PowerMock.mockStatic(ProcessManager.class);
        PowerMock.mockStatic(JournalManager.class);
        PowerMock.mockStatic(Helper.class);
        PowerMock.mockStatic(FilterHelper.class);
        PowerMock.mockStatic(ConfigurationHelper.class);
        PowerMock.mockStatic(PropertyManager.class);

        ConfigurationHelper mockConfig = EasyMock.createMock(ConfigurationHelper.class);
        EasyMock.expect(ConfigurationHelper.getInstance()).andReturn(mockConfig).anyTimes();
        EasyMock.expect(mockConfig.getBatchMaxSize()).andReturn(100).anyTimes();

        EasyMock.expect(Helper.getTranslation(EasyMock.anyString())).andReturn("").anyTimes();
        EasyMock.expect(Helper.getCurrentUser()).andReturn(null).anyTimes();
        Helper.setFehlerMeldung(EasyMock.anyString());
        EasyMock.expectLastCall().anyTimes();

        EasyMock.expect(ProcessManager.countProcesses(EasyMock.anyString())).andReturn(2).anyTimes();
        EasyMock.expect(ProcessManager.getProcesses(EasyMock.isNull(), EasyMock.anyString(), EasyMock.anyInt(), EasyMock.anyInt(),
                EasyMock.isNull())).andReturn(Arrays.asList(process1)).anyTimes();
        EasyMock.expect(ProcessManager.getProcesses(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyInt(), EasyMock.anyInt(),
                EasyMock.isNull())).andReturn(new ArrayList<>()).anyTimes();
        EasyMock.expect(ProcessManager.getBatches(EasyMock.anyInt())).andReturn(new ArrayList<>()).anyTimes();
        EasyMock.expect(ProcessManager.getBatchById(EasyMock.anyInt())).andReturn(batch1).anyTimes();
        EasyMock.expect(ProcessManager.getProcessById(EasyMock.anyInt())).andReturn(process1).anyTimes();
        ProcessManager.saveProcessInformation(EasyMock.anyObject());
        EasyMock.expectLastCall().anyTimes();
        ProcessManager.deleteBatch(EasyMock.anyObject());
        EasyMock.expectLastCall().anyTimes();

        JournalManager.saveJournalEntry(EasyMock.anyObject());
        EasyMock.expectLastCall().anyTimes();

        EasyMock.expect(FilterHelper.criteriaBuilder(EasyMock.anyString(), EasyMock.anyBoolean(), EasyMock.isNull(), EasyMock.isNull(),
                EasyMock.isNull(), EasyMock.anyBoolean(), EasyMock.anyBoolean())).andReturn("").anyTimes();

        EasyMock.expect(PropertyManager.getPropertiesForObject(EasyMock.anyInt(), EasyMock.anyObject(PropertyOwnerType.class)))
                .andReturn(new ArrayList<>())
                .anyTimes();

        PropertyParser mockPropertyParser = EasyMock.createMock(PropertyParser.class);
        PowerMock.mockStatic(PropertyParser.class);
        EasyMock.expect(PropertyParser.getInstance()).andReturn(mockPropertyParser).anyTimes();
        EasyMock.expect(mockPropertyParser.getPropertiesForProcess(EasyMock.anyObject())).andReturn(new ArrayList<>()).anyTimes();
        EasyMock.expect(mockPropertyParser.getDisplayableMetadataForProcess(EasyMock.anyObject())).andReturn(new ArrayList<>()).anyTimes();

        EasyMock.replay(mockConfig, mockPropertyParser);
        PowerMock.replayAll();
    }

    @Test
    public void testConstructor() {
        BatchBean fixture = new BatchBean();
        assertNotNull(fixture);
    }

    @Test
    public void testInitialState() {
        BatchBean fixture = new BatchBean();
        assertNotNull(fixture.getCurrentProcesses());
        assertTrue(fixture.getCurrentProcesses().isEmpty());
        assertNotNull(fixture.getCurrentBatches());
        assertTrue(fixture.getCurrentBatches().isEmpty());
        assertNotNull(fixture.getSelectedProcesses());
        assertTrue(fixture.getSelectedProcesses().isEmpty());
        assertNotNull(fixture.getSelectedBatches());
        assertTrue(fixture.getSelectedBatches().isEmpty());
    }

    @Test
    public void testLoadBatchData() {
        BatchBean fixture = new BatchBean();
        fixture.setSelectedProcesses(Arrays.asList(process1, process2));

        fixture.loadBatchData();

        assertEquals(1, fixture.getCurrentBatches().size());
        assertEquals(batch1, fixture.getCurrentBatches().get(0));
    }

    @Test
    public void testLoadBatchDataEmptySelection() {
        BatchBean fixture = new BatchBean();
        fixture.loadBatchData();

        assertTrue(fixture.getCurrentBatches().isEmpty());
    }

    @Test
    public void testGetCurrentBatchesAsSelectItems() {
        BatchBean fixture = new BatchBean();
        batch1.setBatchLabel("TestBatch1 Label");
        fixture.setCurrentBatches(Arrays.asList(batch1, batch2));

        List<SelectItem> items = fixture.getCurrentBatchesAsSelectItems();

        assertEquals(2, items.size());
        assertEquals("1", items.get(0).getValue());
        assertEquals("TestBatch1 Label", items.get(0).getLabel());
    }

    @Test
    public void testGetCurrentProcessesAsSelectItems() {
        BatchBean fixture = new BatchBean();
        fixture.setCurrentProcesses(Arrays.asList(process1, process2));

        List<SelectItem> items = fixture.getCurrentProcessesAsSelectItems();

        assertEquals(2, items.size());
        assertEquals("1", items.get(0).getValue());
        assertEquals("Process1", items.get(0).getLabel());
        assertEquals("2", items.get(1).getValue());
        assertEquals("Process2", items.get(1).getLabel());
    }

    @Test
    public void testSetSelectedBatchIds() {
        BatchBean fixture = new BatchBean();
        fixture.setSelectedBatchIds(Arrays.asList("1"));

        assertEquals(1, fixture.getSelectedBatches().size());
        assertEquals(batch1, fixture.getSelectedBatches().get(0));
    }

    @Test
    public void testGetSelectedBatchIds() {
        BatchBean fixture = new BatchBean();
        fixture.setSelectedBatches(Arrays.asList(batch1, batch2));

        List<String> ids = fixture.getSelectedBatchIds();

        assertEquals(2, ids.size());
        assertEquals("1", ids.get(0));
        assertEquals("2", ids.get(1));
    }

    @Test
    public void testSetSelectedProcessIds() {
        BatchBean fixture = new BatchBean();
        fixture.setSelectedProcessIds(Arrays.asList("1", "2"));
        assertEquals(2, fixture.getSelectedProcessIds().size());
    }

    @Test
    public void testGetSelectedProcessIds() {
        BatchBean fixture = new BatchBean();
        fixture.setSelectedProcesses(Arrays.asList(process1, process2));

        List<String> ids = fixture.getSelectedProcessIds();

        assertEquals(2, ids.size());
        assertEquals("1", ids.get(0));
        assertEquals("2", ids.get(1));
    }

    @Test
    public void testFilterAlleStart() {
        BatchBean fixture = new BatchBean();
        String result = fixture.FilterAlleStart();
        assertEquals("batch_all", result);
    }

    @Test
    public void testDeleteBatchNoSelection() {
        BatchBean fixture = new BatchBean();
        fixture.deleteBatch();
        assertTrue(fixture.getSelectedBatches().isEmpty());
    }

    @Test
    public void testDeleteBatchWithSelection() {
        BatchBean fixture = new BatchBean();
        fixture.setSelectedBatches(Arrays.asList(batch1));
        fixture.deleteBatch();
        assertNotNull(fixture.getCurrentBatches());
    }

    @Test
    public void testDeleteBatchTooManySelected() {
        BatchBean fixture = new BatchBean();
        fixture.setSelectedBatches(Arrays.asList(batch1, batch2));
        fixture.deleteBatch();
        assertNotNull(fixture.getCurrentBatches());
    }

    @Test
    public void testAddProcessesToBatchNoSelection() {
        BatchBean fixture = new BatchBean();
        fixture.addProcessesToBatch();
        assertTrue(fixture.getSelectedBatches().isEmpty());
    }

    @Test
    public void testAddProcessesToBatchTooManyBatches() {
        BatchBean fixture = new BatchBean();
        fixture.setSelectedBatches(Arrays.asList(batch1, batch2));
        fixture.setSelectedProcesses(Arrays.asList(process1));
        fixture.addProcessesToBatch();
        assertNotNull(fixture.getCurrentBatches());
    }

    @Test
    public void testAddProcessesToBatch() {
        BatchBean fixture = new BatchBean();
        fixture.setSelectedBatches(Arrays.asList(batch1));

        Process p = new Process();
        p.setId(5);
        p.setTitel("NewProcess");

        fixture.setSelectedProcesses(Arrays.asList(p));
        fixture.addProcessesToBatch();

        assertEquals(batch1, p.getBatch());
    }

    @Test
    public void testRemoveProcessesFromBatch() {
        BatchBean fixture = new BatchBean();
        fixture.setSelectedProcesses(Arrays.asList(process1, process2));
        fixture.removeProcessesFromBatch();

        assertNull(process1.getBatch());
        assertNull(process2.getBatch());
    }

    @Test
    public void testCreateNewBatch() {
        BatchBean fixture = new BatchBean();
        fixture.setSelectedProcesses(Arrays.asList(process1, process2));
        fixture.createNewBatch();

        assertNotNull(process1.getBatch());
        assertNotNull(process2.getBatch());
        assertEquals(process1.getBatch(), process2.getBatch());
    }

    @Test
    public void testCreateNewBatchEmptySelection() {
        BatchBean fixture = new BatchBean();
        fixture.createNewBatch();
        assertNotNull(fixture.getCurrentBatches());
    }

    @Test
    public void testEditPropertiesNoSelection() {
        BatchBean fixture = new BatchBean();
        String result = fixture.editProperties();
        assertEquals("", result);
    }

    @Test
    public void testEditPropertiesTooManySelected() {
        BatchBean fixture = new BatchBean();
        fixture.setSelectedBatches(Arrays.asList(batch1, batch2));
        String result = fixture.editProperties();
        assertEquals("", result);
    }

    @Test
    public void testEditPropertiesNullBatch() {
        BatchBean fixture = new BatchBean();
        fixture.setSelectedBatches(new ArrayList<>(Arrays.asList((Batch) null)));
        String result = fixture.editProperties();
        assertEquals("", result);
    }

    @Test
    public void testEditPropertiesWithBatch() {
        BatchBean fixture = new BatchBean();
        fixture.setSelectedBatches(Arrays.asList(batch1));
        String result = fixture.editProperties();
        assertEquals("batch_edit", result);
        assertNotNull(fixture.getBatchHelper());
    }

    @Test
    public void testBatchFilter() {
        BatchBean fixture = new BatchBean();
        fixture.setBatchfilter("test");
        assertEquals("test", fixture.getBatchfilter());
    }

    @Test
    public void testProcessFilter() {
        BatchBean fixture = new BatchBean();
        fixture.setProcessfilter("myfilter");
        assertEquals("myfilter", fixture.getProcessfilter());
    }
}
