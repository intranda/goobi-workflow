package de.sub.goobi.helper;

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
 *
 */
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.goobi.beans.GoobiProperty;
import org.goobi.beans.GoobiProperty.PropertyOwnerType;
import org.goobi.beans.Process;
import org.goobi.production.properties.DisplayProperty;
import org.goobi.production.properties.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.config.ConfigProjectsTest;
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.mock.MockProcess;
import de.sub.goobi.persistence.managers.MetadataManager;
import de.sub.goobi.persistence.managers.PropertyManager;

@ExtendWith(MockitoExtension.class)
public class BatchProcessHelperTest extends AbstractTest {

    private List<Process> processList;

    private GoobiProperty pp;

    private Process process;

    @BeforeEach
    public void setUp() throws Exception {
        processList = new ArrayList<>();
        Path template = Paths.get(ConfigProjectsTest.class.getClassLoader().getResource(".").getFile());
        Path goobiFolder = Paths.get(template.getParent().getParent().toString()
                + "/src/test/resources/config/goobi_config.properties"); // for junit tests in eclipse
        if (!Files.exists(goobiFolder)) {
            goobiFolder = Paths.get("target/test-classes/config/goobi_config.properties"); // to run mvn test from cli or in jenkins
        }
        ConfigurationHelper.resetConfigurationFile();
        ConfigurationHelper.getInstance().setParameter("goobiFolder", goobiFolder.getParent().getParent().toString() + "/");

        process = MockProcess.createProcess();
        process.setTitel("process");
        process.setId(1);

        pp = new GoobiProperty(PropertyOwnerType.PROCESS);
        pp.setContainer("1");
        pp.setOwner(process);
        List<GoobiProperty> pplist = new ArrayList<>();
        pplist.add(pp);
        process.setEigenschaften(pplist);

        processList.add(process);

        Process process2 = MockProcess.createProcess();
        process2.setTitel("process2");
        process2.setId(2);

        GoobiProperty pp2 = new GoobiProperty(PropertyOwnerType.PROCESS);
        List<GoobiProperty> pplist2 = new ArrayList<>();
        pplist2.add(pp2);
        pp2.setOwner(process2);
        process2.setEigenschaften(pplist2);

        processList.add(process2);
    }

    @Test
    public void testConstructorNullValue() {
        assertThrows(NullPointerException.class, () -> {
            new BatchProcessHelper(null, null);
        });
    }

    @Test
    public void testConstructorEmptyList() {
        assertThrows(IndexOutOfBoundsException.class, () -> {
            new BatchProcessHelper(new ArrayList<>(), null);
        });
    }

    @Test
    public void testConstructor() {
        try (MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            setupMocks(mockedPropertyManager, mockedMetadataManager, mockedHelper);

            BatchProcessHelper helper = new BatchProcessHelper(processList, null);
            assertNotNull(helper);
        }
    }

    @Test
    public void testCurrentProcess() {
        try (MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            setupMocks(mockedPropertyManager, mockedMetadataManager, mockedHelper);

            BatchProcessHelper helper = new BatchProcessHelper(processList, null);
            assertNotNull(helper);
            helper.setCurrentProcess(processList.get(0));
            assertEquals(processList.get(0).getTitel(), helper.getCurrentProcess().getTitel());
        }
    }

    @Test
    public void testProcesses() {
        try (MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            setupMocks(mockedPropertyManager, mockedMetadataManager, mockedHelper);

            BatchProcessHelper helper = new BatchProcessHelper(processList, null);
            helper.setProcesses(processList);
            assertEquals(processList.size(), helper.getProcesses().size());
        }
    }

    @Test
    public void testProcessPropertyList() {
        try (MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            setupMocks(mockedPropertyManager, mockedMetadataManager, mockedHelper);

            BatchProcessHelper helper = new BatchProcessHelper(processList, null);
            List<DisplayProperty> list = new ArrayList<>();
            DisplayProperty pp = new DisplayProperty();
            list.add(pp);
            helper.setProcessPropertyList(list);
            assertEquals(list.size(), helper.getProcessPropertyList().size());
        }
    }

    @Test
    public void testProcessProperty() {
        try (MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            setupMocks(mockedPropertyManager, mockedMetadataManager, mockedHelper);

            BatchProcessHelper helper = new BatchProcessHelper(processList, null);
            DisplayProperty pp = new DisplayProperty();
            helper.setProcessProperty(pp);
            assertEquals(pp, helper.getProcessProperty());
        }
    }

    @Test
    public void testPropertyListSize() {
        try (MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            setupMocks(mockedPropertyManager, mockedMetadataManager, mockedHelper);

            BatchProcessHelper helper = new BatchProcessHelper(processList, null);
            List<DisplayProperty> list = new ArrayList<>();
            DisplayProperty pp = new DisplayProperty();
            list.add(pp);
            helper.setProcessPropertyList(list);
            assertEquals(list.size(), helper.getPropertyListSize());
        }
    }

    @Test
    public void testProcessProperties() {
        try (MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            setupMocks(mockedPropertyManager, mockedMetadataManager, mockedHelper);

            BatchProcessHelper helper = new BatchProcessHelper(processList, null);
            List<DisplayProperty> list = new ArrayList<>();
            DisplayProperty pp = new DisplayProperty();
            list.add(pp);
            helper.setProcessPropertyList(list);
            assertEquals(list.size(), helper.getProcessProperties().size());
        }
    }

    @Test
    public void testProcessNameList() {
        try (MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            setupMocks(mockedPropertyManager, mockedMetadataManager, mockedHelper);

            BatchProcessHelper helper = new BatchProcessHelper(processList, null);
            List<String> fixture = new ArrayList<>();
            fixture.add("test");
            helper.setProcessNameList(fixture);
            assertEquals(fixture.size(), helper.getProcessNameList().size());
        }
    }

    @Test
    public void testProcessName() {
        try (MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            setupMocks(mockedPropertyManager, mockedMetadataManager, mockedHelper);

            BatchProcessHelper helper = new BatchProcessHelper(processList, null);
            helper.setProcessName("process");
            assertEquals("process", helper.getProcessName());
        }
    }

    @Test
    public void testSaveCurrentProperty() {
        try (MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            setupMocks(mockedPropertyManager, mockedMetadataManager, mockedHelper);

            BatchProcessHelper helper = new BatchProcessHelper(processList, null);
            DisplayProperty pp = new DisplayProperty();
            pp.setContainer("0");
            pp.setType(Type.TEXT);
            pp.setValue("value");
            pp.setName("name");
            pp.setProzesseigenschaft(this.pp);
            helper.setProcessProperty(pp);
            helper.saveCurrentProperty();
        }
    }

    @Test
    public void testSaveCurrentPropertyForAll() {
        try (MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            setupMocks(mockedPropertyManager, mockedMetadataManager, mockedHelper);

            BatchProcessHelper helper = new BatchProcessHelper(processList, null);
            DisplayProperty pp = new DisplayProperty();
            pp.setContainer("0");
            pp.setType(Type.TEXT);
            pp.setValue("value");
            pp.setName("name");
            helper.setProcessProperty(pp);
            helper.saveCurrentPropertyForAll();
        }
    }

    @Test
    public void testContainers() {
        try (MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            setupMocks(mockedPropertyManager, mockedMetadataManager, mockedHelper);

            BatchProcessHelper helper = new BatchProcessHelper(processList, null);
            Map<String, PropertyListObject> fixture = helper.getContainers();
            assertEquals(2, fixture.size());
        }
    }

    @Test
    public void testContainerSize() {
        try (MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            setupMocks(mockedPropertyManager, mockedMetadataManager, mockedHelper);

            BatchProcessHelper helper = new BatchProcessHelper(processList, null);
            assertEquals(2, helper.getContainersSize());
        }
    }

    @Test
    public void testSortedProperties() {
        try (MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            setupMocks(mockedPropertyManager, mockedMetadataManager, mockedHelper);

            BatchProcessHelper helper = new BatchProcessHelper(processList, null);
            List<DisplayProperty> fixture = helper.getSortedProperties();
            assertEquals(2, fixture.size());
        }
    }

    @Test
    public void testContainerlessProperties() {
        try (MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            setupMocks(mockedPropertyManager, mockedMetadataManager, mockedHelper);

            BatchProcessHelper helper = new BatchProcessHelper(processList, null);
            List<DisplayProperty> fixture = helper.getContainerlessProperties();
            assertEquals(1, fixture.size());
        }
    }

    @Test
    public void testContainer() {
        try (MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            setupMocks(mockedPropertyManager, mockedMetadataManager, mockedHelper);

            BatchProcessHelper helper = new BatchProcessHelper(processList, null);
            List<DisplayProperty> list = new ArrayList<>();
            DisplayProperty pp = new DisplayProperty();
            pp.setContainer("1");
            pp.setType(Type.TEXT);
            pp.setValue("value");
            pp.setName("name");
            list.add(pp);
            helper.setProcessPropertyList(list);

            helper.setContainer("1");
            assertEquals("1", helper.getContainer());
        }
    }

    @Test
    public void testDuplicateContainerForSingle() {
        try (MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            setupMocks(mockedPropertyManager, mockedMetadataManager, mockedHelper);

            BatchProcessHelper helper = new BatchProcessHelper(processList, null);
            List<DisplayProperty> list = new ArrayList<>();
            DisplayProperty pp = new DisplayProperty();
            pp.setContainer("1");
            pp.setType(Type.TEXT);
            pp.setValue("value");
            pp.setName("name");
            pp.setProzesseigenschaft(this.pp);
            list.add(pp);
            helper.setProcessPropertyList(list);
            helper.setContainer("1");
            helper.duplicateContainerForSingle();
        }
    }

    @Test
    public void testDuplicateContainerForAll() {
        try (MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            setupMocks(mockedPropertyManager, mockedMetadataManager, mockedHelper);

            BatchProcessHelper helper = new BatchProcessHelper(processList, null);
            List<DisplayProperty> list = new ArrayList<>();
            DisplayProperty pp = new DisplayProperty();
            pp.setContainer("1");
            pp.setType(Type.TEXT);
            pp.setValue("value");
            pp.setName("name");
            list.add(pp);
            helper.setProcessPropertyList(list);
            helper.setContainer("1");
            helper.duplicateContainerForAll();
        }
    }

    private void setupMocks(MockedStatic<PropertyManager> mockedPropertyManager,
            MockedStatic<MetadataManager> mockedMetadataManager,
            MockedStatic<Helper> mockedHelper) {
        mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any())).thenReturn(new ArrayList<>());
        mockedMetadataManager.when(() -> MetadataManager.getAllMetadataValues(Mockito.anyInt(), Mockito.anyString())).thenReturn(new ArrayList<>());
        mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
        mockedHelper.when(() -> Helper.getTranslation("bitteAuswaehlen")).thenReturn("Please select");
        mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
    }
}
