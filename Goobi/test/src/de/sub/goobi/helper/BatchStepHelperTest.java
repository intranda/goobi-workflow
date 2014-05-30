package de.sub.goobi.helper;

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
import org.goobi.beans.Step;
import org.goobi.production.properties.ProcessProperty;
import org.goobi.production.properties.Type;

import de.sub.goobi.persistence.managers.PropertyManager;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ PropertyManager.class })
public class BatchStepHelperTest {

    private List<Step> stepList;

    @Before
    public void setUp() throws Exception {
        stepList = new ArrayList<>();
        Process process = new Process();
        process.setTitel("process");
        process.setId(1);
        process.setDocket(new Docket());
        process.setDocketId(0);
        process.setMetadatenKonfigurationID(0);
        process.setIstTemplate(true);

        Project project = new Project();
        project.setTitel("Project");
        process.setProjekt(project);

        Processproperty pp = new Processproperty();
        pp.setContainer(1);
        List<Processproperty> pplist = new ArrayList<Processproperty>();
        pplist = new ArrayList<Processproperty>();
        pplist.add(pp);
        process.setEigenschaften(pplist);

        Step step = new Step();
        step.setTitel("title");
        step.setId(1);
        step.setBatchStep(true);
        List<Step> stepsForProcess = new ArrayList<>();
        stepsForProcess.add(step);
        step.setProzess(process);
        process.setSchritte(stepsForProcess);

        stepList.add(step);

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

        Step step2 = new Step();
        step2.setTitel("title2");
        step2.setId(2);
        step2.setBatchStep(true);
        List<Step> stepsForProcess2 = new ArrayList<>();
        stepsForProcess2.add(step2);
        step2.setProzess(process2);

        process2.setSchritte(stepsForProcess2);
        
        stepList.add(step2);

        prepareMocking();

    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullValue() {
        new BatchStepHelper(null);
    }

   

    @Test
    public void testConstructor() {
        BatchStepHelper helper = new BatchStepHelper(stepList);
        assertNotNull(helper);
    }

    @Test
    public void testCurrentProcess() {
        BatchStepHelper helper = new BatchStepHelper(stepList);
        assertNotNull(helper);
        helper.setCurrentStep(stepList.get(0));
        assertEquals(stepList.get(0).getTitel(), helper.getCurrentStep().getTitel());
    }

    @Test
    public void testProcesses() {
        BatchStepHelper helper = new BatchStepHelper(stepList);
        helper.setSteps(stepList);
        assertEquals(stepList.size(), helper.getSteps().size());
    }

    @Test
    public void testProcessProperty() {
        BatchStepHelper helper = new BatchStepHelper(stepList);
        ProcessProperty pp = new ProcessProperty();
        helper.setProcessProperty(pp);
        assertEquals(pp, helper.getProcessProperty());
        assertEquals(0, helper.getPropertyListSize());

    }

   
    @Test
    public void testProcessNameList() {
        BatchStepHelper helper = new BatchStepHelper(stepList);

        List<String> fixture = new ArrayList<>();
        fixture.add("test");
        helper.setProcessNameList(fixture);
        assertEquals(fixture.size(), helper.getProcessNameList().size());

    }

    @Test
    public void testProcessName() {
        BatchStepHelper helper = new BatchStepHelper(stepList);
        helper.setProcessName("process");
        assertEquals("process", helper.getProcessName());
    }

    @Test
    public void testSaveCurrentProperty() {
        BatchStepHelper helper = new BatchStepHelper(stepList);
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
        BatchStepHelper helper = new BatchStepHelper(stepList);
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
        BatchStepHelper helper = new BatchStepHelper(stepList);

        Map<Integer, PropertyListObject> fixture = helper.getContainers();
        assertEquals(2, fixture.size());
    }

    @Test
    public void testContainerSize() {
        BatchStepHelper helper = new BatchStepHelper(stepList);
        assertEquals(2, helper.getContainersSize());
    }

    @Test
    public void testSortedProperties() {
        BatchStepHelper helper = new BatchStepHelper(stepList);
        List<ProcessProperty> fixture = helper.getSortedProperties();
        assertEquals(0, fixture.size());
    }

    @Test
    public void testContainerlessProperties() {
        BatchStepHelper helper = new BatchStepHelper(stepList);
        List<ProcessProperty> fixture = helper.getContainerlessProperties();
        assertEquals(0, fixture.size());

    }

    @Test
    public void testContainer() {
        BatchStepHelper helper = new BatchStepHelper(stepList);
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
        BatchStepHelper helper = new BatchStepHelper(stepList);
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
        BatchStepHelper helper = new BatchStepHelper(stepList);
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
