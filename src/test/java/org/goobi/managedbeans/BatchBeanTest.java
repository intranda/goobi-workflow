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

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.goobi.beans.Batch;
import org.goobi.beans.GoobiProperty.PropertyOwnerType;
import org.goobi.beans.Process;
import org.goobi.beans.Project;
import org.goobi.production.flow.statistics.hibernate.FilterHelper;
import org.goobi.production.properties.PropertyParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.JournalManager;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.PropertyManager;
import jakarta.faces.model.SelectItem;

@ExtendWith(MockitoExtension.class)
public class BatchBeanTest extends AbstractTest {

    private Batch batch1;
    private Batch batch2;
    private Process process1;
    private Process process2;

    @BeforeEach
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
    }

    @Test
    public void testConstructor() {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<FilterHelper> mockedFilterHelper = Mockito.mockStatic(FilterHelper.class);
                //                MockedStatic<ConfigurationHelper> mockedConfigurationHelper = Mockito.mockStatic(ConfigurationHelper.class);
                MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<PropertyParser> mockedPropertyParser = Mockito.mockStatic(PropertyParser.class)) {
            PropertyParser mockPropertyParser = Mockito.mock(PropertyParser.class);

            //            mockedConfigurationHelper.when(() -> ConfigurationHelper.getInstance()).thenReturn(mockedConfigurationHelper);
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedProcessManager.when(() -> ProcessManager.countProcesses(Mockito.anyString())).thenReturn(2);

            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.isNull(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.isNull())).thenReturn(Arrays.asList(process1));
            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.isNull())).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getBatches(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getBatchById(Mockito.anyInt())).thenReturn(batch1);
            mockedProcessManager.when(() -> ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process1);
            mockedFilterHelper
                    .when(() -> FilterHelper.criteriaBuilder(Mockito.anyString(), Mockito.anyBoolean(), Mockito.isNull(), Mockito.isNull(),
                            Mockito.isNull(), Mockito.anyBoolean(), Mockito.anyBoolean()))
                    .thenReturn("");
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any(PropertyOwnerType.class)))
                    .thenReturn(new ArrayList<>());
            mockedPropertyParser.when(() -> PropertyParser.getInstance()).thenReturn(mockPropertyParser);

            BatchBean fixture = new BatchBean();
            assertNotNull(fixture);

        }
    }

    @Test
    public void testInitialState() {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<FilterHelper> mockedFilterHelper = Mockito.mockStatic(FilterHelper.class);
                //                MockedStatic<ConfigurationHelper> mockedConfigurationHelper = Mockito.mockStatic(ConfigurationHelper.class);
                MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<PropertyParser> mockedPropertyParser = Mockito.mockStatic(PropertyParser.class)) {
            PropertyParser mockPropertyParser = Mockito.mock(PropertyParser.class);

            //            mockedConfigurationHelper.when(() -> ConfigurationHelper.getInstance()).thenReturn(mockedConfigurationHelper);
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedProcessManager.when(() -> ProcessManager.countProcesses(Mockito.anyString())).thenReturn(2);

            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.isNull(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.isNull())).thenReturn(Arrays.asList(process1));
            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.isNull())).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getBatches(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getBatchById(Mockito.anyInt())).thenReturn(batch1);
            mockedProcessManager.when(() -> ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process1);
            mockedFilterHelper
                    .when(() -> FilterHelper.criteriaBuilder(Mockito.anyString(), Mockito.anyBoolean(), Mockito.isNull(), Mockito.isNull(),
                            Mockito.isNull(), Mockito.anyBoolean(), Mockito.anyBoolean()))
                    .thenReturn("");
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any(PropertyOwnerType.class)))
                    .thenReturn(new ArrayList<>());
            mockedPropertyParser.when(() -> PropertyParser.getInstance()).thenReturn(mockPropertyParser);

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
    }

    @Test
    public void testLoadBatchData() {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<FilterHelper> mockedFilterHelper = Mockito.mockStatic(FilterHelper.class);
                //                MockedStatic<ConfigurationHelper> mockedConfigurationHelper = Mockito.mockStatic(ConfigurationHelper.class);
                MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<PropertyParser> mockedPropertyParser = Mockito.mockStatic(PropertyParser.class)) {
            PropertyParser mockPropertyParser = Mockito.mock(PropertyParser.class);

            //            mockedConfigurationHelper.when(() -> ConfigurationHelper.getInstance()).thenReturn(mockedConfigurationHelper);
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedProcessManager.when(() -> ProcessManager.countProcesses(Mockito.anyString())).thenReturn(2);

            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.isNull(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.isNull())).thenReturn(Arrays.asList(process1));
            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.isNull())).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getBatches(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getBatchById(Mockito.anyInt())).thenReturn(batch1);
            mockedProcessManager.when(() -> ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process1);
            mockedFilterHelper
                    .when(() -> FilterHelper.criteriaBuilder(Mockito.anyString(), Mockito.anyBoolean(), Mockito.isNull(), Mockito.isNull(),
                            Mockito.isNull(), Mockito.anyBoolean(), Mockito.anyBoolean()))
                    .thenReturn("");
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any(PropertyOwnerType.class)))
                    .thenReturn(new ArrayList<>());
            mockedPropertyParser.when(() -> PropertyParser.getInstance()).thenReturn(mockPropertyParser);

            BatchBean fixture = new BatchBean();
            fixture.setSelectedProcesses(Arrays.asList(process1, process2));

            fixture.loadBatchData();

            assertEquals(1, fixture.getCurrentBatches().size());
            assertEquals(batch1, fixture.getCurrentBatches().get(0));

        }
    }

    @Test
    public void testLoadBatchDataEmptySelection() {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<FilterHelper> mockedFilterHelper = Mockito.mockStatic(FilterHelper.class);
                //                MockedStatic<ConfigurationHelper> mockedConfigurationHelper = Mockito.mockStatic(ConfigurationHelper.class);
                MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<PropertyParser> mockedPropertyParser = Mockito.mockStatic(PropertyParser.class)) {
            PropertyParser mockPropertyParser = Mockito.mock(PropertyParser.class);

            //            mockedConfigurationHelper.when(() -> ConfigurationHelper.getInstance()).thenReturn(mockedConfigurationHelper);
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedProcessManager.when(() -> ProcessManager.countProcesses(Mockito.anyString())).thenReturn(2);

            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.isNull(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.isNull())).thenReturn(Arrays.asList(process1));
            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.isNull())).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getBatches(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getBatchById(Mockito.anyInt())).thenReturn(batch1);
            mockedProcessManager.when(() -> ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process1);
            mockedFilterHelper
                    .when(() -> FilterHelper.criteriaBuilder(Mockito.anyString(), Mockito.anyBoolean(), Mockito.isNull(), Mockito.isNull(),
                            Mockito.isNull(), Mockito.anyBoolean(), Mockito.anyBoolean()))
                    .thenReturn("");
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any(PropertyOwnerType.class)))
                    .thenReturn(new ArrayList<>());
            mockedPropertyParser.when(() -> PropertyParser.getInstance()).thenReturn(mockPropertyParser);

            BatchBean fixture = new BatchBean();
            fixture.loadBatchData();

            assertTrue(fixture.getCurrentBatches().isEmpty());

        }
    }

    @Test
    public void testGetCurrentBatchesAsSelectItems() {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<FilterHelper> mockedFilterHelper = Mockito.mockStatic(FilterHelper.class);
                //                MockedStatic<ConfigurationHelper> mockedConfigurationHelper = Mockito.mockStatic(ConfigurationHelper.class);
                MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<PropertyParser> mockedPropertyParser = Mockito.mockStatic(PropertyParser.class)) {
            PropertyParser mockPropertyParser = Mockito.mock(PropertyParser.class);

            //            mockedConfigurationHelper.when(() -> ConfigurationHelper.getInstance()).thenReturn(mockedConfigurationHelper);
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedProcessManager.when(() -> ProcessManager.countProcesses(Mockito.anyString())).thenReturn(2);

            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.isNull(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.isNull())).thenReturn(Arrays.asList(process1));
            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.isNull())).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getBatches(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getBatchById(Mockito.anyInt())).thenReturn(batch1);
            mockedProcessManager.when(() -> ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process1);
            mockedFilterHelper
                    .when(() -> FilterHelper.criteriaBuilder(Mockito.anyString(), Mockito.anyBoolean(), Mockito.isNull(), Mockito.isNull(),
                            Mockito.isNull(), Mockito.anyBoolean(), Mockito.anyBoolean()))
                    .thenReturn("");
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any(PropertyOwnerType.class)))
                    .thenReturn(new ArrayList<>());
            mockedPropertyParser.when(() -> PropertyParser.getInstance()).thenReturn(mockPropertyParser);

            BatchBean fixture = new BatchBean();
            batch1.setBatchLabel("TestBatch1 Label");
            fixture.setCurrentBatches(Arrays.asList(batch1, batch2));

            List<SelectItem> items = fixture.getCurrentBatchesAsSelectItems();

            assertEquals(2, items.size());
            assertEquals("1", items.get(0).getValue());
            assertEquals("TestBatch1 Label", items.get(0).getLabel());

        }
    }

    @Test
    public void testGetCurrentProcessesAsSelectItems() {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<FilterHelper> mockedFilterHelper = Mockito.mockStatic(FilterHelper.class);
                //                MockedStatic<ConfigurationHelper> mockedConfigurationHelper = Mockito.mockStatic(ConfigurationHelper.class);
                MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<PropertyParser> mockedPropertyParser = Mockito.mockStatic(PropertyParser.class)) {
            PropertyParser mockPropertyParser = Mockito.mock(PropertyParser.class);

            //            mockedConfigurationHelper.when(() -> ConfigurationHelper.getInstance()).thenReturn(mockedConfigurationHelper);
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedProcessManager.when(() -> ProcessManager.countProcesses(Mockito.anyString())).thenReturn(2);

            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.isNull(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.isNull())).thenReturn(Arrays.asList(process1));
            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.isNull())).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getBatches(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getBatchById(Mockito.anyInt())).thenReturn(batch1);
            mockedProcessManager.when(() -> ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process1);
            mockedFilterHelper
                    .when(() -> FilterHelper.criteriaBuilder(Mockito.anyString(), Mockito.anyBoolean(), Mockito.isNull(), Mockito.isNull(),
                            Mockito.isNull(), Mockito.anyBoolean(), Mockito.anyBoolean()))
                    .thenReturn("");
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any(PropertyOwnerType.class)))
                    .thenReturn(new ArrayList<>());
            mockedPropertyParser.when(() -> PropertyParser.getInstance()).thenReturn(mockPropertyParser);

            BatchBean fixture = new BatchBean();
            fixture.setCurrentProcesses(Arrays.asList(process1, process2));

            List<SelectItem> items = fixture.getCurrentProcessesAsSelectItems();

            assertEquals(2, items.size());
            assertEquals("1", items.get(0).getValue());
            assertEquals("Process1", items.get(0).getLabel());
            assertEquals("2", items.get(1).getValue());
            assertEquals("Process2", items.get(1).getLabel());

        }
    }

    @Test
    public void testSetSelectedBatchIds() {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<FilterHelper> mockedFilterHelper = Mockito.mockStatic(FilterHelper.class);
                //                MockedStatic<ConfigurationHelper> mockedConfigurationHelper = Mockito.mockStatic(ConfigurationHelper.class);
                MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<PropertyParser> mockedPropertyParser = Mockito.mockStatic(PropertyParser.class)) {
            PropertyParser mockPropertyParser = Mockito.mock(PropertyParser.class);

            //            mockedConfigurationHelper.when(() -> ConfigurationHelper.getInstance()).thenReturn(mockedConfigurationHelper);
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedProcessManager.when(() -> ProcessManager.countProcesses(Mockito.anyString())).thenReturn(2);

            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.isNull(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.isNull())).thenReturn(Arrays.asList(process1));
            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.isNull())).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getBatches(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getBatchById(Mockito.anyInt())).thenReturn(batch1);
            mockedProcessManager.when(() -> ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process1);
            mockedFilterHelper
                    .when(() -> FilterHelper.criteriaBuilder(Mockito.anyString(), Mockito.anyBoolean(), Mockito.isNull(), Mockito.isNull(),
                            Mockito.isNull(), Mockito.anyBoolean(), Mockito.anyBoolean()))
                    .thenReturn("");
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any(PropertyOwnerType.class)))
                    .thenReturn(new ArrayList<>());
            mockedPropertyParser.when(() -> PropertyParser.getInstance()).thenReturn(mockPropertyParser);

            BatchBean fixture = new BatchBean();
            fixture.setSelectedBatchIds(Arrays.asList("1"));

            assertEquals(1, fixture.getSelectedBatches().size());
            assertEquals(batch1, fixture.getSelectedBatches().get(0));

        }
    }

    @Test
    public void testGetSelectedBatchIds() {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<FilterHelper> mockedFilterHelper = Mockito.mockStatic(FilterHelper.class);
                //                MockedStatic<ConfigurationHelper> mockedConfigurationHelper = Mockito.mockStatic(ConfigurationHelper.class);
                MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<PropertyParser> mockedPropertyParser = Mockito.mockStatic(PropertyParser.class)) {
            PropertyParser mockPropertyParser = Mockito.mock(PropertyParser.class);

            //            mockedConfigurationHelper.when(() -> ConfigurationHelper.getInstance()).thenReturn(mockedConfigurationHelper);
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedProcessManager.when(() -> ProcessManager.countProcesses(Mockito.anyString())).thenReturn(2);

            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.isNull(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.isNull())).thenReturn(Arrays.asList(process1));
            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.isNull())).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getBatches(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getBatchById(Mockito.anyInt())).thenReturn(batch1);
            mockedProcessManager.when(() -> ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process1);
            mockedFilterHelper
                    .when(() -> FilterHelper.criteriaBuilder(Mockito.anyString(), Mockito.anyBoolean(), Mockito.isNull(), Mockito.isNull(),
                            Mockito.isNull(), Mockito.anyBoolean(), Mockito.anyBoolean()))
                    .thenReturn("");
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any(PropertyOwnerType.class)))
                    .thenReturn(new ArrayList<>());
            mockedPropertyParser.when(() -> PropertyParser.getInstance()).thenReturn(mockPropertyParser);

            BatchBean fixture = new BatchBean();
            fixture.setSelectedBatches(Arrays.asList(batch1, batch2));

            List<String> ids = fixture.getSelectedBatchIds();

            assertEquals(2, ids.size());
            assertEquals("1", ids.get(0));
            assertEquals("2", ids.get(1));

        }
    }

    @Test
    public void testSetSelectedProcessIds() {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<FilterHelper> mockedFilterHelper = Mockito.mockStatic(FilterHelper.class);
                //                MockedStatic<ConfigurationHelper> mockedConfigurationHelper = Mockito.mockStatic(ConfigurationHelper.class);
                MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<PropertyParser> mockedPropertyParser = Mockito.mockStatic(PropertyParser.class)) {
            PropertyParser mockPropertyParser = Mockito.mock(PropertyParser.class);

            //            mockedConfigurationHelper.when(() -> ConfigurationHelper.getInstance()).thenReturn(mockedConfigurationHelper);
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedProcessManager.when(() -> ProcessManager.countProcesses(Mockito.anyString())).thenReturn(2);

            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.isNull(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.isNull())).thenReturn(Arrays.asList(process1));
            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.isNull())).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getBatches(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getBatchById(Mockito.anyInt())).thenReturn(batch1);
            mockedProcessManager.when(() -> ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process1);
            mockedFilterHelper
                    .when(() -> FilterHelper.criteriaBuilder(Mockito.anyString(), Mockito.anyBoolean(), Mockito.isNull(), Mockito.isNull(),
                            Mockito.isNull(), Mockito.anyBoolean(), Mockito.anyBoolean()))
                    .thenReturn("");
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any(PropertyOwnerType.class)))
                    .thenReturn(new ArrayList<>());
            mockedPropertyParser.when(() -> PropertyParser.getInstance()).thenReturn(mockPropertyParser);

            BatchBean fixture = new BatchBean();
            fixture.setSelectedProcessIds(Arrays.asList("1", "2"));
            assertEquals(2, fixture.getSelectedProcessIds().size());

        }
    }

    @Test
    public void testGetSelectedProcessIds() {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<FilterHelper> mockedFilterHelper = Mockito.mockStatic(FilterHelper.class);
                //                MockedStatic<ConfigurationHelper> mockedConfigurationHelper = Mockito.mockStatic(ConfigurationHelper.class);
                MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<PropertyParser> mockedPropertyParser = Mockito.mockStatic(PropertyParser.class)) {
            PropertyParser mockPropertyParser = Mockito.mock(PropertyParser.class);

            //            mockedConfigurationHelper.when(() -> ConfigurationHelper.getInstance()).thenReturn(mockedConfigurationHelper);
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedProcessManager.when(() -> ProcessManager.countProcesses(Mockito.anyString())).thenReturn(2);

            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.isNull(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.isNull())).thenReturn(Arrays.asList(process1));
            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.isNull())).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getBatches(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getBatchById(Mockito.anyInt())).thenReturn(batch1);
            mockedProcessManager.when(() -> ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process1);
            mockedFilterHelper
                    .when(() -> FilterHelper.criteriaBuilder(Mockito.anyString(), Mockito.anyBoolean(), Mockito.isNull(), Mockito.isNull(),
                            Mockito.isNull(), Mockito.anyBoolean(), Mockito.anyBoolean()))
                    .thenReturn("");
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any(PropertyOwnerType.class)))
                    .thenReturn(new ArrayList<>());
            mockedPropertyParser.when(() -> PropertyParser.getInstance()).thenReturn(mockPropertyParser);

            BatchBean fixture = new BatchBean();
            fixture.setSelectedProcesses(Arrays.asList(process1, process2));

            List<String> ids = fixture.getSelectedProcessIds();

            assertEquals(2, ids.size());
            assertEquals("1", ids.get(0));
            assertEquals("2", ids.get(1));

        }
    }

    @Test
    public void testFilterAlleStart() {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<FilterHelper> mockedFilterHelper = Mockito.mockStatic(FilterHelper.class);
                //                MockedStatic<ConfigurationHelper> mockedConfigurationHelper = Mockito.mockStatic(ConfigurationHelper.class);
                MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<PropertyParser> mockedPropertyParser = Mockito.mockStatic(PropertyParser.class)) {
            PropertyParser mockPropertyParser = Mockito.mock(PropertyParser.class);

            //            mockedConfigurationHelper.when(() -> ConfigurationHelper.getInstance()).thenReturn(mockedConfigurationHelper);
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedProcessManager.when(() -> ProcessManager.countProcesses(Mockito.anyString())).thenReturn(2);

            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.isNull(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.isNull())).thenReturn(Arrays.asList(process1));
            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.isNull())).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getBatches(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getBatchById(Mockito.anyInt())).thenReturn(batch1);
            mockedProcessManager.when(() -> ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process1);
            mockedFilterHelper
                    .when(() -> FilterHelper.criteriaBuilder(Mockito.anyString(), Mockito.anyBoolean(), Mockito.isNull(), Mockito.isNull(),
                            Mockito.isNull(), Mockito.anyBoolean(), Mockito.anyBoolean()))
                    .thenReturn("");
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any(PropertyOwnerType.class)))
                    .thenReturn(new ArrayList<>());
            mockedPropertyParser.when(() -> PropertyParser.getInstance()).thenReturn(mockPropertyParser);

            BatchBean fixture = new BatchBean();
            String result = fixture.FilterAlleStart();
            assertEquals("batch_all", result);

        }
    }

    @Test
    public void testDeleteBatchNoSelection() {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<FilterHelper> mockedFilterHelper = Mockito.mockStatic(FilterHelper.class);
                //                MockedStatic<ConfigurationHelper> mockedConfigurationHelper = Mockito.mockStatic(ConfigurationHelper.class);
                MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<PropertyParser> mockedPropertyParser = Mockito.mockStatic(PropertyParser.class)) {
            PropertyParser mockPropertyParser = Mockito.mock(PropertyParser.class);

            //            mockedConfigurationHelper.when(() -> ConfigurationHelper.getInstance()).thenReturn(mockedConfigurationHelper);
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedProcessManager.when(() -> ProcessManager.countProcesses(Mockito.anyString())).thenReturn(2);

            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.isNull(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.isNull())).thenReturn(Arrays.asList(process1));
            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.isNull())).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getBatches(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getBatchById(Mockito.anyInt())).thenReturn(batch1);
            mockedProcessManager.when(() -> ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process1);
            mockedFilterHelper
                    .when(() -> FilterHelper.criteriaBuilder(Mockito.anyString(), Mockito.anyBoolean(), Mockito.isNull(), Mockito.isNull(),
                            Mockito.isNull(), Mockito.anyBoolean(), Mockito.anyBoolean()))
                    .thenReturn("");
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any(PropertyOwnerType.class)))
                    .thenReturn(new ArrayList<>());
            mockedPropertyParser.when(() -> PropertyParser.getInstance()).thenReturn(mockPropertyParser);

            BatchBean fixture = new BatchBean();
            fixture.deleteBatch();
            assertTrue(fixture.getSelectedBatches().isEmpty());

        }
    }

    @Test
    public void testDeleteBatchWithSelection() {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<FilterHelper> mockedFilterHelper = Mockito.mockStatic(FilterHelper.class);
                //                MockedStatic<ConfigurationHelper> mockedConfigurationHelper = Mockito.mockStatic(ConfigurationHelper.class);
                MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<PropertyParser> mockedPropertyParser = Mockito.mockStatic(PropertyParser.class)) {
            PropertyParser mockPropertyParser = Mockito.mock(PropertyParser.class);

            //            mockedConfigurationHelper.when(() -> ConfigurationHelper.getInstance()).thenReturn(mockedConfigurationHelper);
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedProcessManager.when(() -> ProcessManager.countProcesses(Mockito.anyString())).thenReturn(2);

            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.isNull(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.isNull())).thenReturn(Arrays.asList(process1));
            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.isNull())).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getBatches(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getBatchById(Mockito.anyInt())).thenReturn(batch1);
            mockedProcessManager.when(() -> ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process1);
            mockedFilterHelper
                    .when(() -> FilterHelper.criteriaBuilder(Mockito.anyString(), Mockito.anyBoolean(), Mockito.isNull(), Mockito.isNull(),
                            Mockito.isNull(), Mockito.anyBoolean(), Mockito.anyBoolean()))
                    .thenReturn("");
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any(PropertyOwnerType.class)))
                    .thenReturn(new ArrayList<>());
            mockedPropertyParser.when(() -> PropertyParser.getInstance()).thenReturn(mockPropertyParser);

            BatchBean fixture = new BatchBean();
            fixture.setSelectedBatches(Arrays.asList(batch1));
            fixture.deleteBatch();
            assertNotNull(fixture.getCurrentBatches());

        }
    }

    @Test
    public void testDeleteBatchTooManySelected() {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<FilterHelper> mockedFilterHelper = Mockito.mockStatic(FilterHelper.class);
                //                MockedStatic<ConfigurationHelper> mockedConfigurationHelper = Mockito.mockStatic(ConfigurationHelper.class);
                MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<PropertyParser> mockedPropertyParser = Mockito.mockStatic(PropertyParser.class)) {
            PropertyParser mockPropertyParser = Mockito.mock(PropertyParser.class);

            //            mockedConfigurationHelper.when(() -> ConfigurationHelper.getInstance()).thenReturn(mockedConfigurationHelper);
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedProcessManager.when(() -> ProcessManager.countProcesses(Mockito.anyString())).thenReturn(2);

            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.isNull(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.isNull())).thenReturn(Arrays.asList(process1));
            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.isNull())).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getBatches(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getBatchById(Mockito.anyInt())).thenReturn(batch1);
            mockedProcessManager.when(() -> ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process1);
            mockedFilterHelper
                    .when(() -> FilterHelper.criteriaBuilder(Mockito.anyString(), Mockito.anyBoolean(), Mockito.isNull(), Mockito.isNull(),
                            Mockito.isNull(), Mockito.anyBoolean(), Mockito.anyBoolean()))
                    .thenReturn("");
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any(PropertyOwnerType.class)))
                    .thenReturn(new ArrayList<>());
            mockedPropertyParser.when(() -> PropertyParser.getInstance()).thenReturn(mockPropertyParser);

            BatchBean fixture = new BatchBean();
            fixture.setSelectedBatches(Arrays.asList(batch1, batch2));
            fixture.deleteBatch();
            assertNotNull(fixture.getCurrentBatches());

        }
    }

    @Test
    public void testAddProcessesToBatchNoSelection() {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<FilterHelper> mockedFilterHelper = Mockito.mockStatic(FilterHelper.class);
                //                MockedStatic<ConfigurationHelper> mockedConfigurationHelper = Mockito.mockStatic(ConfigurationHelper.class);
                MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<PropertyParser> mockedPropertyParser = Mockito.mockStatic(PropertyParser.class)) {
            PropertyParser mockPropertyParser = Mockito.mock(PropertyParser.class);

            //            mockedConfigurationHelper.when(() -> ConfigurationHelper.getInstance()).thenReturn(mockedConfigurationHelper);
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedProcessManager.when(() -> ProcessManager.countProcesses(Mockito.anyString())).thenReturn(2);

            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.isNull(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.isNull())).thenReturn(Arrays.asList(process1));
            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.isNull())).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getBatches(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getBatchById(Mockito.anyInt())).thenReturn(batch1);
            mockedProcessManager.when(() -> ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process1);
            mockedFilterHelper
                    .when(() -> FilterHelper.criteriaBuilder(Mockito.anyString(), Mockito.anyBoolean(), Mockito.isNull(), Mockito.isNull(),
                            Mockito.isNull(), Mockito.anyBoolean(), Mockito.anyBoolean()))
                    .thenReturn("");
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any(PropertyOwnerType.class)))
                    .thenReturn(new ArrayList<>());
            mockedPropertyParser.when(() -> PropertyParser.getInstance()).thenReturn(mockPropertyParser);

            BatchBean fixture = new BatchBean();
            fixture.addProcessesToBatch();
            assertTrue(fixture.getSelectedBatches().isEmpty());

        }
    }

    @Test
    public void testAddProcessesToBatchTooManyBatches() {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<FilterHelper> mockedFilterHelper = Mockito.mockStatic(FilterHelper.class);
                //                MockedStatic<ConfigurationHelper> mockedConfigurationHelper = Mockito.mockStatic(ConfigurationHelper.class);
                MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<PropertyParser> mockedPropertyParser = Mockito.mockStatic(PropertyParser.class)) {
            PropertyParser mockPropertyParser = Mockito.mock(PropertyParser.class);

            //            mockedConfigurationHelper.when(() -> ConfigurationHelper.getInstance()).thenReturn(mockedConfigurationHelper);
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedProcessManager.when(() -> ProcessManager.countProcesses(Mockito.anyString())).thenReturn(2);

            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.isNull(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.isNull())).thenReturn(Arrays.asList(process1));
            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.isNull())).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getBatches(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getBatchById(Mockito.anyInt())).thenReturn(batch1);
            mockedProcessManager.when(() -> ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process1);
            mockedFilterHelper
                    .when(() -> FilterHelper.criteriaBuilder(Mockito.anyString(), Mockito.anyBoolean(), Mockito.isNull(), Mockito.isNull(),
                            Mockito.isNull(), Mockito.anyBoolean(), Mockito.anyBoolean()))
                    .thenReturn("");
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any(PropertyOwnerType.class)))
                    .thenReturn(new ArrayList<>());
            mockedPropertyParser.when(() -> PropertyParser.getInstance()).thenReturn(mockPropertyParser);

            BatchBean fixture = new BatchBean();
            fixture.setSelectedBatches(Arrays.asList(batch1, batch2));
            fixture.setSelectedProcesses(Arrays.asList(process1));
            fixture.addProcessesToBatch();
            assertNotNull(fixture.getCurrentBatches());

        }
    }

    @Test
    public void testAddProcessesToBatch() {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<FilterHelper> mockedFilterHelper = Mockito.mockStatic(FilterHelper.class);
                //                MockedStatic<ConfigurationHelper> mockedConfigurationHelper = Mockito.mockStatic(ConfigurationHelper.class);
                MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<PropertyParser> mockedPropertyParser = Mockito.mockStatic(PropertyParser.class)) {
            PropertyParser mockPropertyParser = Mockito.mock(PropertyParser.class);

            //            mockedConfigurationHelper.when(() -> ConfigurationHelper.getInstance()).thenReturn(mockedConfigurationHelper);
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedProcessManager.when(() -> ProcessManager.countProcesses(Mockito.anyString())).thenReturn(2);

            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.isNull(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.isNull())).thenReturn(Arrays.asList(process1));
            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.isNull())).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getBatches(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getBatchById(Mockito.anyInt())).thenReturn(batch1);
            mockedProcessManager.when(() -> ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process1);
            mockedFilterHelper
                    .when(() -> FilterHelper.criteriaBuilder(Mockito.anyString(), Mockito.anyBoolean(), Mockito.isNull(), Mockito.isNull(),
                            Mockito.isNull(), Mockito.anyBoolean(), Mockito.anyBoolean()))
                    .thenReturn("");
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any(PropertyOwnerType.class)))
                    .thenReturn(new ArrayList<>());
            mockedPropertyParser.when(() -> PropertyParser.getInstance()).thenReturn(mockPropertyParser);

            BatchBean fixture = new BatchBean();
            fixture.setSelectedBatches(Arrays.asList(batch1));

            Process p = new Process();
            p.setId(5);
            p.setTitel("NewProcess");

            fixture.setSelectedProcesses(Arrays.asList(p));
            fixture.addProcessesToBatch();

            assertEquals(batch1, p.getBatch());

        }
    }

    @Test
    public void testRemoveProcessesFromBatch() {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<FilterHelper> mockedFilterHelper = Mockito.mockStatic(FilterHelper.class);
                //                MockedStatic<ConfigurationHelper> mockedConfigurationHelper = Mockito.mockStatic(ConfigurationHelper.class);
                MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<PropertyParser> mockedPropertyParser = Mockito.mockStatic(PropertyParser.class)) {
            PropertyParser mockPropertyParser = Mockito.mock(PropertyParser.class);

            //            mockedConfigurationHelper.when(() -> ConfigurationHelper.getInstance()).thenReturn(mockedConfigurationHelper);
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedProcessManager.when(() -> ProcessManager.countProcesses(Mockito.anyString())).thenReturn(2);

            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.isNull(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.isNull())).thenReturn(Arrays.asList(process1));
            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.isNull())).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getBatches(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getBatchById(Mockito.anyInt())).thenReturn(batch1);
            mockedProcessManager.when(() -> ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process1);
            mockedFilterHelper
                    .when(() -> FilterHelper.criteriaBuilder(Mockito.anyString(), Mockito.anyBoolean(), Mockito.isNull(), Mockito.isNull(),
                            Mockito.isNull(), Mockito.anyBoolean(), Mockito.anyBoolean()))
                    .thenReturn("");
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any(PropertyOwnerType.class)))
                    .thenReturn(new ArrayList<>());
            mockedPropertyParser.when(() -> PropertyParser.getInstance()).thenReturn(mockPropertyParser);

            BatchBean fixture = new BatchBean();
            fixture.setSelectedProcesses(Arrays.asList(process1, process2));
            fixture.removeProcessesFromBatch();

            assertNull(process1.getBatch());
            assertNull(process2.getBatch());

        }
    }

    @Test
    public void testCreateNewBatch() {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<FilterHelper> mockedFilterHelper = Mockito.mockStatic(FilterHelper.class);
                //                MockedStatic<ConfigurationHelper> mockedConfigurationHelper = Mockito.mockStatic(ConfigurationHelper.class);
                MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<PropertyParser> mockedPropertyParser = Mockito.mockStatic(PropertyParser.class)) {
            PropertyParser mockPropertyParser = Mockito.mock(PropertyParser.class);

            //            mockedConfigurationHelper.when(() -> ConfigurationHelper.getInstance()).thenReturn(mockedConfigurationHelper);
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedProcessManager.when(() -> ProcessManager.countProcesses(Mockito.anyString())).thenReturn(2);

            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.isNull(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.isNull())).thenReturn(Arrays.asList(process1));
            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.isNull())).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getBatches(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getBatchById(Mockito.anyInt())).thenReturn(batch1);
            mockedProcessManager.when(() -> ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process1);
            mockedFilterHelper
                    .when(() -> FilterHelper.criteriaBuilder(Mockito.anyString(), Mockito.anyBoolean(), Mockito.isNull(), Mockito.isNull(),
                            Mockito.isNull(), Mockito.anyBoolean(), Mockito.anyBoolean()))
                    .thenReturn("");
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any(PropertyOwnerType.class)))
                    .thenReturn(new ArrayList<>());
            mockedPropertyParser.when(() -> PropertyParser.getInstance()).thenReturn(mockPropertyParser);

            BatchBean fixture = new BatchBean();
            fixture.setSelectedProcesses(Arrays.asList(process1, process2));
            fixture.createNewBatch();

            assertNotNull(process1.getBatch());
            assertNotNull(process2.getBatch());
            assertEquals(process1.getBatch(), process2.getBatch());

        }
    }

    @Test
    public void testCreateNewBatchEmptySelection() {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<FilterHelper> mockedFilterHelper = Mockito.mockStatic(FilterHelper.class);
                //                MockedStatic<ConfigurationHelper> mockedConfigurationHelper = Mockito.mockStatic(ConfigurationHelper.class);
                MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<PropertyParser> mockedPropertyParser = Mockito.mockStatic(PropertyParser.class)) {
            PropertyParser mockPropertyParser = Mockito.mock(PropertyParser.class);

            //            mockedConfigurationHelper.when(() -> ConfigurationHelper.getInstance()).thenReturn(mockedConfigurationHelper);
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedProcessManager.when(() -> ProcessManager.countProcesses(Mockito.anyString())).thenReturn(2);

            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.isNull(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.isNull())).thenReturn(Arrays.asList(process1));
            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.isNull())).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getBatches(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getBatchById(Mockito.anyInt())).thenReturn(batch1);
            mockedProcessManager.when(() -> ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process1);
            mockedFilterHelper
                    .when(() -> FilterHelper.criteriaBuilder(Mockito.anyString(), Mockito.anyBoolean(), Mockito.isNull(), Mockito.isNull(),
                            Mockito.isNull(), Mockito.anyBoolean(), Mockito.anyBoolean()))
                    .thenReturn("");
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any(PropertyOwnerType.class)))
                    .thenReturn(new ArrayList<>());
            mockedPropertyParser.when(() -> PropertyParser.getInstance()).thenReturn(mockPropertyParser);

            BatchBean fixture = new BatchBean();
            fixture.createNewBatch();
            assertNotNull(fixture.getCurrentBatches());

        }
    }

    @Test
    public void testEditPropertiesNoSelection() {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<FilterHelper> mockedFilterHelper = Mockito.mockStatic(FilterHelper.class);
                //                MockedStatic<ConfigurationHelper> mockedConfigurationHelper = Mockito.mockStatic(ConfigurationHelper.class);
                MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<PropertyParser> mockedPropertyParser = Mockito.mockStatic(PropertyParser.class)) {
            PropertyParser mockPropertyParser = Mockito.mock(PropertyParser.class);

            //            mockedConfigurationHelper.when(() -> ConfigurationHelper.getInstance()).thenReturn(mockedConfigurationHelper);
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedProcessManager.when(() -> ProcessManager.countProcesses(Mockito.anyString())).thenReturn(2);

            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.isNull(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.isNull())).thenReturn(Arrays.asList(process1));
            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.isNull())).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getBatches(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getBatchById(Mockito.anyInt())).thenReturn(batch1);
            mockedProcessManager.when(() -> ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process1);
            mockedFilterHelper
                    .when(() -> FilterHelper.criteriaBuilder(Mockito.anyString(), Mockito.anyBoolean(), Mockito.isNull(), Mockito.isNull(),
                            Mockito.isNull(), Mockito.anyBoolean(), Mockito.anyBoolean()))
                    .thenReturn("");
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any(PropertyOwnerType.class)))
                    .thenReturn(new ArrayList<>());
            mockedPropertyParser.when(() -> PropertyParser.getInstance()).thenReturn(mockPropertyParser);

            BatchBean fixture = new BatchBean();
            String result = fixture.editProperties();
            assertEquals("", result);

        }
    }

    @Test
    public void testEditPropertiesTooManySelected() {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<FilterHelper> mockedFilterHelper = Mockito.mockStatic(FilterHelper.class);
                //                MockedStatic<ConfigurationHelper> mockedConfigurationHelper = Mockito.mockStatic(ConfigurationHelper.class);
                MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<PropertyParser> mockedPropertyParser = Mockito.mockStatic(PropertyParser.class)) {
            PropertyParser mockPropertyParser = Mockito.mock(PropertyParser.class);

            //            mockedConfigurationHelper.when(() -> ConfigurationHelper.getInstance()).thenReturn(mockedConfigurationHelper);
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedProcessManager.when(() -> ProcessManager.countProcesses(Mockito.anyString())).thenReturn(2);

            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.isNull(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.isNull())).thenReturn(Arrays.asList(process1));
            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.isNull())).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getBatches(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getBatchById(Mockito.anyInt())).thenReturn(batch1);
            mockedProcessManager.when(() -> ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process1);
            mockedFilterHelper
                    .when(() -> FilterHelper.criteriaBuilder(Mockito.anyString(), Mockito.anyBoolean(), Mockito.isNull(), Mockito.isNull(),
                            Mockito.isNull(), Mockito.anyBoolean(), Mockito.anyBoolean()))
                    .thenReturn("");
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any(PropertyOwnerType.class)))
                    .thenReturn(new ArrayList<>());
            mockedPropertyParser.when(() -> PropertyParser.getInstance()).thenReturn(mockPropertyParser);

            BatchBean fixture = new BatchBean();
            fixture.setSelectedBatches(Arrays.asList(batch1, batch2));
            String result = fixture.editProperties();
            assertEquals("", result);

        }
    }

    @Test
    public void testEditPropertiesNullBatch() {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<FilterHelper> mockedFilterHelper = Mockito.mockStatic(FilterHelper.class);
                //                MockedStatic<ConfigurationHelper> mockedConfigurationHelper = Mockito.mockStatic(ConfigurationHelper.class);
                MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<PropertyParser> mockedPropertyParser = Mockito.mockStatic(PropertyParser.class)) {
            PropertyParser mockPropertyParser = Mockito.mock(PropertyParser.class);

            //            mockedConfigurationHelper.when(() -> ConfigurationHelper.getInstance()).thenReturn(mockedConfigurationHelper);
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedProcessManager.when(() -> ProcessManager.countProcesses(Mockito.anyString())).thenReturn(2);

            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.isNull(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.isNull())).thenReturn(Arrays.asList(process1));
            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.isNull())).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getBatches(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getBatchById(Mockito.anyInt())).thenReturn(batch1);
            mockedProcessManager.when(() -> ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process1);
            mockedFilterHelper
                    .when(() -> FilterHelper.criteriaBuilder(Mockito.anyString(), Mockito.anyBoolean(), Mockito.isNull(), Mockito.isNull(),
                            Mockito.isNull(), Mockito.anyBoolean(), Mockito.anyBoolean()))
                    .thenReturn("");
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any(PropertyOwnerType.class)))
                    .thenReturn(new ArrayList<>());
            mockedPropertyParser.when(() -> PropertyParser.getInstance()).thenReturn(mockPropertyParser);

            BatchBean fixture = new BatchBean();
            fixture.setSelectedBatches(new ArrayList<>(Arrays.asList((Batch) null)));
            String result = fixture.editProperties();
            assertEquals("", result);

        }
    }

    @Test
    public void testEditPropertiesWithBatch() {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<FilterHelper> mockedFilterHelper = Mockito.mockStatic(FilterHelper.class);
                //                MockedStatic<ConfigurationHelper> mockedConfigurationHelper = Mockito.mockStatic(ConfigurationHelper.class);
                MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<PropertyParser> mockedPropertyParser = Mockito.mockStatic(PropertyParser.class)) {
            PropertyParser mockPropertyParser = Mockito.mock(PropertyParser.class);

            //            mockedConfigurationHelper.when(() -> ConfigurationHelper.getInstance()).thenReturn(mockedConfigurationHelper);
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedProcessManager.when(() -> ProcessManager.countProcesses(Mockito.anyString())).thenReturn(2);

            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.isNull(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.isNull())).thenReturn(Arrays.asList(process1));
            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.isNull())).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getBatches(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getBatchById(Mockito.anyInt())).thenReturn(batch1);
            mockedProcessManager.when(() -> ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process1);
            mockedFilterHelper
                    .when(() -> FilterHelper.criteriaBuilder(Mockito.anyString(), Mockito.anyBoolean(), Mockito.isNull(), Mockito.isNull(),
                            Mockito.isNull(), Mockito.anyBoolean(), Mockito.anyBoolean()))
                    .thenReturn("");
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any(PropertyOwnerType.class)))
                    .thenReturn(new ArrayList<>());
            mockedPropertyParser.when(() -> PropertyParser.getInstance()).thenReturn(mockPropertyParser);

            BatchBean fixture = new BatchBean();
            fixture.setSelectedBatches(Arrays.asList(batch1));
            String result = fixture.editProperties();
            assertEquals("batch_edit", result);
            assertNotNull(fixture.getBatchHelper());

        }
    }

    @Test
    public void testBatchFilter() {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<FilterHelper> mockedFilterHelper = Mockito.mockStatic(FilterHelper.class);
                //                MockedStatic<ConfigurationHelper> mockedConfigurationHelper = Mockito.mockStatic(ConfigurationHelper.class);
                MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<PropertyParser> mockedPropertyParser = Mockito.mockStatic(PropertyParser.class)) {
            PropertyParser mockPropertyParser = Mockito.mock(PropertyParser.class);

            //            mockedConfigurationHelper.when(() -> ConfigurationHelper.getInstance()).thenReturn(mockedConfigurationHelper);
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedProcessManager.when(() -> ProcessManager.countProcesses(Mockito.anyString())).thenReturn(2);

            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.isNull(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.isNull())).thenReturn(Arrays.asList(process1));
            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.isNull())).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getBatches(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getBatchById(Mockito.anyInt())).thenReturn(batch1);
            mockedProcessManager.when(() -> ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process1);
            mockedFilterHelper
                    .when(() -> FilterHelper.criteriaBuilder(Mockito.anyString(), Mockito.anyBoolean(), Mockito.isNull(), Mockito.isNull(),
                            Mockito.isNull(), Mockito.anyBoolean(), Mockito.anyBoolean()))
                    .thenReturn("");
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any(PropertyOwnerType.class)))
                    .thenReturn(new ArrayList<>());
            mockedPropertyParser.when(() -> PropertyParser.getInstance()).thenReturn(mockPropertyParser);

            BatchBean fixture = new BatchBean();
            fixture.setBatchfilter("test");
            assertEquals("test", fixture.getBatchfilter());

        }
    }

    @Test
    public void testProcessFilter() {
        try (MockedStatic<ProcessManager> mockedProcessManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<JournalManager> mockedJournalManager = Mockito.mockStatic(JournalManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<FilterHelper> mockedFilterHelper = Mockito.mockStatic(FilterHelper.class);
                MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<PropertyParser> mockedPropertyParser = Mockito.mockStatic(PropertyParser.class)) {
            PropertyParser mockPropertyParser = Mockito.mock(PropertyParser.class);

            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("");
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedProcessManager.when(() -> ProcessManager.countProcesses(Mockito.anyString())).thenReturn(2);

            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.isNull(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.isNull())).thenReturn(Arrays.asList(process1));
            mockedProcessManager.when(() -> ProcessManager.getProcesses(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.isNull())).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getBatches(Mockito.anyInt())).thenReturn(new ArrayList<>());
            mockedProcessManager.when(() -> ProcessManager.getBatchById(Mockito.anyInt())).thenReturn(batch1);
            mockedProcessManager.when(() -> ProcessManager.getProcessById(Mockito.anyInt())).thenReturn(process1);
            mockedFilterHelper
                    .when(() -> FilterHelper.criteriaBuilder(Mockito.anyString(), Mockito.anyBoolean(), Mockito.isNull(), Mockito.isNull(),
                            Mockito.isNull(), Mockito.anyBoolean(), Mockito.anyBoolean()))
                    .thenReturn("");
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any(PropertyOwnerType.class)))
                    .thenReturn(new ArrayList<>());
            mockedPropertyParser.when(() -> PropertyParser.getInstance()).thenReturn(mockPropertyParser);

            BatchBean fixture = new BatchBean();
            fixture.setProcessfilter("myfilter");
            assertEquals("myfilter", fixture.getProcessfilter());

        }
    }
}
