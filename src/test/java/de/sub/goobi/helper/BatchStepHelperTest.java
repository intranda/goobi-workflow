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
import org.goobi.beans.Step;
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
public class BatchStepHelperTest extends AbstractTest {

    private List<Step> stepList;

    private Process process;

    @BeforeEach
    public void setUp() throws Exception {
        stepList = new ArrayList<>();
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

        Step step = new Step();
        step.setTitel("title");
        step.setId(1);
        step.setBatchStep(true);
        List<Step> stepsForProcess = new ArrayList<>();
        stepsForProcess.add(step);
        step.setProzess(process);
        process.setSchritte(stepsForProcess);

        stepList.add(step);

        Process process2 = MockProcess.createProcess();
        process2.setTitel("process2");
        process2.setId(2);

        Step step2 = new Step();
        step2.setTitel("title2");
        step2.setId(2);
        step2.setBatchStep(true);
        List<Step> stepsForProcess2 = new ArrayList<>();
        stepsForProcess2.add(step2);
        step2.setProzess(process2);

        process2.setSchritte(stepsForProcess2);

        stepList.add(step2);
    }

    @Test
    public void testConstructorNullValue() {
        assertThrows(NullPointerException.class, () -> {
            new BatchStepHelper(null);
        });
    }

    @Test
    public void testConstructor() {
        try (MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            setupMocks(mockedPropertyManager, mockedMetadataManager, mockedHelper);

            BatchStepHelper helper = new BatchStepHelper(stepList);
            assertNotNull(helper);
        }
    }

    @Test
    public void testCurrentProcess() {
        try (MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            setupMocks(mockedPropertyManager, mockedMetadataManager, mockedHelper);

            BatchStepHelper helper = new BatchStepHelper(stepList);
            assertNotNull(helper);
            helper.setCurrentStep(stepList.get(0));
            assertEquals(stepList.get(0).getTitel(), helper.getCurrentStep().getTitel());
        }
    }

    @Test
    public void testProcesses() {
        try (MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            setupMocks(mockedPropertyManager, mockedMetadataManager, mockedHelper);

            BatchStepHelper helper = new BatchStepHelper(stepList);
            helper.setSteps(stepList);
            assertEquals(stepList.size(), helper.getSteps().size());
        }
    }

    @Test
    public void testProcessProperty() {
        try (MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            setupMocks(mockedPropertyManager, mockedMetadataManager, mockedHelper);

            BatchStepHelper helper = new BatchStepHelper(stepList);
            DisplayProperty pp = new DisplayProperty();
            helper.setProcessProperty(pp);
            assertEquals(pp, helper.getProcessProperty());
            assertEquals(2, helper.getPropertyListSize());
        }
    }

    @Test
    public void testSaveCurrentProperty() {
        try (MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            setupMocks(mockedPropertyManager, mockedMetadataManager, mockedHelper);

            BatchStepHelper helper = new BatchStepHelper(stepList);
            DisplayProperty pp = new DisplayProperty();
            pp.setContainer("0");
            pp.setType(Type.TEXT);
            pp.setValue("value");
            pp.setName("name");
            GoobiProperty gp = new GoobiProperty(PropertyOwnerType.PROCESS);
            gp.setOwner(stepList.get(0).getProzess());
            pp.setProzesseigenschaft(gp);
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

            BatchStepHelper helper = new BatchStepHelper(stepList);
            DisplayProperty pp = new DisplayProperty();
            pp.setContainer("0");
            pp.setType(Type.TEXT);
            pp.setValue("value");
            pp.setName("name");
            GoobiProperty gp = new GoobiProperty(PropertyOwnerType.PROCESS);
            gp.setOwner(stepList.get(0).getProzess());
            pp.setProzesseigenschaft(gp);
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

            BatchStepHelper helper = new BatchStepHelper(stepList);
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

            BatchStepHelper helper = new BatchStepHelper(stepList);
            assertEquals(2, helper.getContainersSize());
        }
    }

    @Test
    public void testSortedProperties() {
        try (MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
                MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class);
                MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class)) {
            setupMocks(mockedPropertyManager, mockedMetadataManager, mockedHelper);

            BatchStepHelper helper = new BatchStepHelper(stepList);
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

            BatchStepHelper helper = new BatchStepHelper(stepList);
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

            BatchStepHelper helper = new BatchStepHelper(stepList);
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

            BatchStepHelper helper = new BatchStepHelper(stepList);
            List<DisplayProperty> list = new ArrayList<>();
            DisplayProperty pp = new DisplayProperty();
            pp.setContainer("1");
            pp.setType(Type.TEXT);
            pp.setValue("value");
            pp.setName("name");
            GoobiProperty gp = new GoobiProperty(PropertyOwnerType.PROCESS);
            gp.setOwner(stepList.get(0).getProzess());
            pp.setProzesseigenschaft(gp);
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

            BatchStepHelper helper = new BatchStepHelper(stepList);
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
        mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
        mockedHelper.when(() -> Helper.getBeanByClass(Mockito.any())).thenReturn(null);
    }
}
