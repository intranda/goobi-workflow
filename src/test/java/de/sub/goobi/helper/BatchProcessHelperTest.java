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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.easymock.EasyMock;
import org.goobi.beans.Process;
import org.goobi.beans.Processproperty;
import org.goobi.production.properties.ProcessProperty;
import org.goobi.production.properties.Type;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.config.ConfigProjectsTest;
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.mock.MockProcess;
import de.sub.goobi.persistence.managers.MetadataManager;
import de.sub.goobi.persistence.managers.PropertyManager;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ PropertyManager.class, MetadataManager.class, Helper.class })
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*" })
public class BatchProcessHelperTest extends AbstractTest {

    private List<Process> processList;

    @Before
    public void setUp() throws Exception {
        processList = new ArrayList<>();
        Path template = Paths.get(ConfigProjectsTest.class.getClassLoader().getResource(".").getFile());
        Path goobiFolder = Paths.get(template.getParent().getParent().toString() + "/src/test/resources/config/goobi_config.properties"); // for junit tests in eclipse
        if (!Files.exists(goobiFolder)) {
            goobiFolder = Paths.get("target/test-classes/config/goobi_config.properties"); // to run mvn test from cli or in jenkins
        }
        ConfigurationHelper.resetConfigurationFile();
        ConfigurationHelper.getInstance().setParameter("goobiFolder", goobiFolder.getParent().getParent().toString() + "/");

        Process process = MockProcess.createProcess();
        process.setTitel("process");
        process.setId(1);

        Processproperty pp = new Processproperty();
        pp.setContainer("1");
        List<Processproperty> pplist = new ArrayList<>();
        pplist.add(pp);
        process.setEigenschaften(pplist);

        processList.add(process);

        Process process2 = MockProcess.createProcess();
        process2.setTitel("process2");
        process2.setId(2);

        Processproperty pp2 = new Processproperty();
        List<Processproperty> pplist2 = new ArrayList<>();
        pplist2.add(pp2);
        process2.setEigenschaften(pplist2);

        processList.add(process2);

        prepareMocking();

    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullValue() {
        new BatchProcessHelper(null, null);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testConstructorEmptyList() {
        new BatchProcessHelper(new ArrayList<>(), null);
    }

    @Test
    public void testConstructor() {
        BatchProcessHelper helper = new BatchProcessHelper(processList, null);
        assertNotNull(helper);
    }

    @Test
    public void testCurrentProcess() {
        BatchProcessHelper helper = new BatchProcessHelper(processList, null);
        assertNotNull(helper);
        helper.setCurrentProcess(processList.get(0));
        assertEquals(processList.get(0).getTitel(), helper.getCurrentProcess().getTitel());
    }

    @Test
    public void testProcesses() {
        BatchProcessHelper helper = new BatchProcessHelper(processList, null);
        helper.setProcesses(processList);
        assertEquals(processList.size(), helper.getProcesses().size());
    }

    @Test
    public void testProcessPropertyList() {
        BatchProcessHelper helper = new BatchProcessHelper(processList, null);
        List<ProcessProperty> list = new ArrayList<>();
        ProcessProperty pp = new ProcessProperty();
        list.add(pp);
        helper.setProcessPropertyList(list);
        assertEquals(list.size(), helper.getProcessPropertyList().size());
    }

    @Test
    public void testProcessProperty() {
        BatchProcessHelper helper = new BatchProcessHelper(processList, null);
        ProcessProperty pp = new ProcessProperty();
        helper.setProcessProperty(pp);
        assertEquals(pp, helper.getProcessProperty());

    }

    @Test
    public void testPropertyListSize() {
        BatchProcessHelper helper = new BatchProcessHelper(processList, null);
        List<ProcessProperty> list = new ArrayList<>();
        ProcessProperty pp = new ProcessProperty();
        list.add(pp);
        helper.setProcessPropertyList(list);
        assertEquals(list.size(), helper.getPropertyListSize());
    }

    @Test
    public void testProcessProperties() {
        BatchProcessHelper helper = new BatchProcessHelper(processList, null);
        List<ProcessProperty> list = new ArrayList<>();
        ProcessProperty pp = new ProcessProperty();
        list.add(pp);
        helper.setProcessPropertyList(list);
        assertEquals(list.size(), helper.getProcessProperties().size());
    }

    @Test
    public void testProcessNameList() {
        BatchProcessHelper helper = new BatchProcessHelper(processList, null);

        List<String> fixture = new ArrayList<>();
        fixture.add("test");
        helper.setProcessNameList(fixture);
        assertEquals(fixture.size(), helper.getProcessNameList().size());

    }

    @Test
    public void testProcessName() {
        BatchProcessHelper helper = new BatchProcessHelper(processList, null);
        helper.setProcessName("process");
        assertEquals("process", helper.getProcessName());
    }

    @Test
    public void testSaveCurrentProperty() {
        BatchProcessHelper helper = new BatchProcessHelper(processList, null);
        ProcessProperty pp = new ProcessProperty();
        pp.setContainer("0");
        pp.setType(Type.TEXT);
        pp.setValue("value");
        pp.setName("name");
        helper.setProcessProperty(pp);
        helper.saveCurrentProperty();
    }

    @Test
    public void testSaveCurrentPropertyForAll() {
        BatchProcessHelper helper = new BatchProcessHelper(processList, null);
        ProcessProperty pp = new ProcessProperty();
        pp.setContainer("0");
        pp.setType(Type.TEXT);
        pp.setValue("value");
        pp.setName("name");
        helper.setProcessProperty(pp);
        helper.saveCurrentPropertyForAll();
    }

    @Test
    public void testContainers() {
        BatchProcessHelper helper = new BatchProcessHelper(processList, null);

        Map<String, PropertyListObject> fixture = helper.getContainers();
        assertEquals(2, fixture.size());
    }

    @Test
    public void testContainerSize() {
        BatchProcessHelper helper = new BatchProcessHelper(processList, null);
        assertEquals(2, helper.getContainersSize());
    }

    @Test
    public void testSortedProperties() {
        BatchProcessHelper helper = new BatchProcessHelper(processList, null);
        List<ProcessProperty> fixture = helper.getSortedProperties();
        assertEquals(2, fixture.size());
    }

    @Test
    public void testContainerlessProperties() {
        BatchProcessHelper helper = new BatchProcessHelper(processList, null);
        List<ProcessProperty> fixture = helper.getContainerlessProperties();
        assertEquals(1, fixture.size());

    }

    @Test
    public void testContainer() {
        BatchProcessHelper helper = new BatchProcessHelper(processList, null);
        List<ProcessProperty> list = new ArrayList<>();
        ProcessProperty pp = new ProcessProperty();
        pp.setContainer("1");
        pp.setType(Type.TEXT);
        pp.setValue("value");
        pp.setName("name");
        list.add(pp);
        helper.setProcessPropertyList(list);

        helper.setContainer("1");
        assertEquals("1", helper.getContainer());
    }

    @Test
    public void testDuplicateContainerForSingle() {
        BatchProcessHelper helper = new BatchProcessHelper(processList, null);
        List<ProcessProperty> list = new ArrayList<>();
        ProcessProperty pp = new ProcessProperty();
        pp.setContainer("1");
        pp.setType(Type.TEXT);
        pp.setValue("value");
        pp.setName("name");
        list.add(pp);
        helper.setProcessPropertyList(list);
        helper.setContainer("1");
        helper.duplicateContainerForSingle();
    }

    @Test
    public void testDuplicateContainerForAll() {
        BatchProcessHelper helper = new BatchProcessHelper(processList, null);
        List<ProcessProperty> list = new ArrayList<>();
        ProcessProperty pp = new ProcessProperty();
        pp.setContainer("1");
        pp.setType(Type.TEXT);
        pp.setValue("value");
        pp.setName("name");
        list.add(pp);
        helper.setProcessPropertyList(list);
        helper.setContainer("1");
        helper.duplicateContainerForAll();
    }

    private void prepareMocking() {
        PowerMock.mockStatic(PropertyManager.class);
        EasyMock.expect(PropertyManager.getProcessPropertiesForProcess(EasyMock.anyInt())).andReturn(new ArrayList<>()).anyTimes();
        PropertyManager.saveProcessProperty(EasyMock.anyObject(Processproperty.class));
        PropertyManager.saveProcessProperty(EasyMock.anyObject(Processproperty.class));

        PowerMock.mockStatic(MetadataManager.class);
        EasyMock.expect(MetadataManager.getAllMetadataValues(EasyMock.anyInt(), EasyMock.anyString())).andReturn(new ArrayList<>()).anyTimes();

        PowerMock.mockStatic(Helper.class);
        EasyMock.expect(Helper.getLoginBean()).andReturn(null).anyTimes();
        EasyMock.expect(Helper.getTranslation("bitteAuswaehlen")).andReturn("Please select").anyTimes();
        EasyMock.expect(Helper.getCurrentUser()).andReturn(null).anyTimes();
        Helper.setMeldung(EasyMock.anyString());
        Helper.setMeldung(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyString());
        PowerMock.replay(Helper.class);

        PowerMock.replay(MetadataManager.class);
        PowerMock.replay(PropertyManager.class);
    }
}
