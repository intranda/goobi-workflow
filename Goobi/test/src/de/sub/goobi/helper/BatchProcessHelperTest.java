package de.sub.goobi.helper;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *          - http://www.intranda.com
 *          - http://digiverso.com 
 *          - http://www.goobi.org
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
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.easymock.EasyMock;
import org.goobi.beans.Docket;
import org.goobi.beans.Process;
import org.goobi.beans.Processproperty;
import org.goobi.beans.Project;
import org.goobi.production.properties.ProcessProperty;
import org.goobi.production.properties.Type;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.persistence.managers.PropertyManager;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ PropertyManager.class })
public class BatchProcessHelperTest {

    private List<Process> processList;

    @Before
    public void setUp() throws Exception {
        String datafolder = System.getenv("junitdata");
        if (datafolder == null) {
            datafolder = "/opt/digiverso/junit/data/";
        }
        ConfigurationHelper.CONFIG_FILE_NAME = datafolder + "goobi_config.properties";

        ConfigurationHelper.getInstance().setParameter("KonfigurationVerzeichnis", datafolder);
        
        processList = new ArrayList<>();
        Process process = new Process();
        process.setTitel("process");
        process.setId(1);
        process.setDocket(new Docket());
        process.setDocketId(0);
        process.setMetadatenKonfigurationID(0);
        process.setIstTemplate(false);

        Project project = new Project();
        project.setTitel("Project");
        process.setProjekt(project);

        Processproperty pp = new Processproperty();
        pp.setContainer(1);
        List<Processproperty> pplist = new ArrayList<Processproperty>();
        pplist = new ArrayList<Processproperty>();
        pplist.add(pp);
        process.setEigenschaften(pplist);

        processList.add(process);

        Process process2 = new Process();
        process2.setTitel("process2");
        process2.setId(2);
        process2.setDocket(new Docket());
        process2.setDocketId(0);
        process2.setMetadatenKonfigurationID(0);
        process2.setIstTemplate(true);

        Project project2 = new Project();
        project2.setTitel("Project2");
        process2.setProjekt(project2);

        Processproperty pp2 = new Processproperty();
        List<Processproperty> pplist2 = new ArrayList<Processproperty>();
        pplist2 = new ArrayList<Processproperty>();
        pplist2.add(pp2);
        process2.setEigenschaften(pplist2);

        processList.add(process2);

        prepareMocking();

    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullValue() {
        new BatchProcessHelper(null);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testConstructorEmptyList() {
        new BatchProcessHelper(new ArrayList<Process>());
    }

    @Test
    public void testConstructor() {
        BatchProcessHelper helper = new BatchProcessHelper(processList);
        assertNotNull(helper);
    }

    @Test
    public void testCurrentProcess() {
        BatchProcessHelper helper = new BatchProcessHelper(processList);
        assertNotNull(helper);
        helper.setCurrentProcess(processList.get(0));
        assertEquals(processList.get(0).getTitel(), helper.getCurrentProcess().getTitel());
    }

    @Test
    public void testProcesses() {
        BatchProcessHelper helper = new BatchProcessHelper(processList);
        helper.setProcesses(processList);
        assertEquals(processList.size(), helper.getProcesses().size());
    }

    @Test
    public void testProcessPropertyList() {
        BatchProcessHelper helper = new BatchProcessHelper(processList);
        List<ProcessProperty> list = new ArrayList<>();
        ProcessProperty pp = new ProcessProperty();
        list.add(pp);
        helper.setProcessPropertyList(list);
        assertEquals(list.size(), helper.getProcessPropertyList().size());
    }

    @Test
    public void testProcessProperty() {
        BatchProcessHelper helper = new BatchProcessHelper(processList);
        ProcessProperty pp = new ProcessProperty();
        helper.setProcessProperty(pp);
        assertEquals(pp, helper.getProcessProperty());

    }

    @Test
    public void testPropertyListSize() {
        BatchProcessHelper helper = new BatchProcessHelper(processList);
        List<ProcessProperty> list = new ArrayList<>();
        ProcessProperty pp = new ProcessProperty();
        list.add(pp);
        helper.setProcessPropertyList(list);
        assertEquals(list.size(), helper.getPropertyListSize());
    }

    @Test
    public void testProcessProperties() {
        BatchProcessHelper helper = new BatchProcessHelper(processList);
        List<ProcessProperty> list = new ArrayList<>();
        ProcessProperty pp = new ProcessProperty();
        list.add(pp);
        helper.setProcessPropertyList(list);
        assertEquals(list.size(), helper.getProcessProperties().size());
    }

    @Test
    public void testProcessNameList() {
        BatchProcessHelper helper = new BatchProcessHelper(processList);

        List<String> fixture = new ArrayList<>();
        fixture.add("test");
        helper.setProcessNameList(fixture);
        assertEquals(fixture.size(), helper.getProcessNameList().size());

    }

    @Test
    public void testProcessName() {
        BatchProcessHelper helper = new BatchProcessHelper(processList);
        helper.setProcessName("process");
        assertEquals("process", helper.getProcessName());
    }

    @Test
    public void testSaveCurrentProperty() {
        BatchProcessHelper helper = new BatchProcessHelper(processList);
        ProcessProperty pp = new ProcessProperty();
        pp.setContainer(0);
        pp.setType(Type.TEXT);
        pp.setValue("value");
        pp.setName("name");
        helper.setProcessProperty(pp);
        helper.saveCurrentProperty();
    }

    @Test
    public void testSaveCurrentPropertyForAll() {
        BatchProcessHelper helper = new BatchProcessHelper(processList);
        ProcessProperty pp = new ProcessProperty();
        pp.setContainer(0);
        pp.setType(Type.TEXT);
        pp.setValue("value");
        pp.setName("name");
        helper.setProcessProperty(pp);
        helper.saveCurrentPropertyForAll();
    }

    @Test
    public void testContainers() {
        BatchProcessHelper helper = new BatchProcessHelper(processList);

        Map<Integer, PropertyListObject> fixture = helper.getContainers();
        assertEquals(3, fixture.size());
    }

    @Test
    public void testContainerSize() {
        BatchProcessHelper helper = new BatchProcessHelper(processList);
        assertEquals(3, helper.getContainersSize());
    }

    @Test
    public void testSortedProperties() {
        BatchProcessHelper helper = new BatchProcessHelper(processList);
        List<ProcessProperty> fixture = helper.getSortedProperties();
        assertEquals(12, fixture.size());
    }

    @Test
    public void testContainerlessProperties() {
        BatchProcessHelper helper = new BatchProcessHelper(processList);
        List<ProcessProperty> fixture = helper.getContainerlessProperties();
        assertEquals(6, fixture.size());

    }

    @Test
    public void testContainer() {
        BatchProcessHelper helper = new BatchProcessHelper(processList);
        List<ProcessProperty> list = new ArrayList<>();
        ProcessProperty pp = new ProcessProperty();
        pp.setContainer(1);
        pp.setType(Type.TEXT);
        pp.setValue("value");
        pp.setName("name");
        list.add(pp);
        helper.setProcessPropertyList(list);

        helper.setContainer(1);
        assertEquals(1, helper.getContainer().intValue());
    }

    @Test
    public void testDuplicateContainerForSingle() {
        BatchProcessHelper helper = new BatchProcessHelper(processList);
        List<ProcessProperty> list = new ArrayList<>();
        ProcessProperty pp = new ProcessProperty();
        pp.setContainer(1);
        pp.setType(Type.TEXT);
        pp.setValue("value");
        pp.setName("name");
        list.add(pp);
        helper.setProcessPropertyList(list);
        helper.setContainer(1);
        helper.duplicateContainerForSingle();
    }

    @Test
    public void testDuplicateContainerForAll() {
        BatchProcessHelper helper = new BatchProcessHelper(processList);
        List<ProcessProperty> list = new ArrayList<>();
        ProcessProperty pp = new ProcessProperty();
        pp.setContainer(1);
        pp.setType(Type.TEXT);
        pp.setValue("value");
        pp.setName("name");
        list.add(pp);
        helper.setProcessPropertyList(list);
        helper.setContainer(1);
        helper.duplicateContainerForAll();
    }

    private void prepareMocking() throws Exception {
        PowerMock.mockStatic(PropertyManager.class);
        PropertyManager.saveProcessProperty(EasyMock.anyObject(Processproperty.class));

        EasyMock.expectLastCall().anyTimes();
        PowerMock.replay(PropertyManager.class);

    }
}
