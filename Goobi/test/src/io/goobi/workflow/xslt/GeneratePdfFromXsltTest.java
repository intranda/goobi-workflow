package io.goobi.workflow.xslt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.goobi.beans.HistoryEvent;
import org.goobi.beans.Masterpiece;
import org.goobi.beans.Masterpieceproperty;
import org.goobi.beans.Process;
import org.goobi.beans.Processproperty;
import org.goobi.beans.Step;
import org.goobi.beans.Template;
import org.goobi.beans.Templateproperty;
import org.goobi.production.cli.helper.StringPair;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.config.ConfigProjectsTest;
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.enums.HistoryEventType;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.mock.MockProcess;
import de.sub.goobi.persistence.managers.HistoryManager;
import de.sub.goobi.persistence.managers.MasterpieceManager;
import de.sub.goobi.persistence.managers.MetadataManager;
import de.sub.goobi.persistence.managers.PropertyManager;
import de.sub.goobi.persistence.managers.StepManager;
import de.sub.goobi.persistence.managers.TemplateManager;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ PropertyManager.class, StepManager.class, TemplateManager.class, MasterpieceManager.class, HistoryManager.class,
    MetadataManager.class })
@PowerMockIgnore("javax.management.*")
public class GeneratePdfFromXsltTest {

    private static final Namespace xmlns = Namespace.getNamespace("http://www.goobi.io/logfile");

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private Process process;
    private String xsltfile;

    @Before
    public void setUp() throws Exception {

        Path path = Paths.get(ConfigProjectsTest.class.getClassLoader().getResource(".").getFile());
        String goobiFolder = path.getParent().getParent().getParent().toString() + "/test/resources/";
        ConfigurationHelper.CONFIG_FILE_NAME = goobiFolder + "config/goobi_config.properties";
        ConfigurationHelper.resetConfigurationFile();
        ConfigurationHelper.getInstance().setParameter("goobiFolder", goobiFolder);

        xsltfile = goobiFolder + "/xslt/docket.xsl";

        process = MockProcess.createProcess();

        PowerMock.mockStatic(PropertyManager.class);
        PowerMock.mockStatic(StepManager.class);
        PowerMock.mockStatic(TemplateManager.class);
        PowerMock.mockStatic(MasterpieceManager.class);
        PowerMock.mockStatic(HistoryManager.class);
        PowerMock.mockStatic(MetadataManager.class);

        List<Processproperty> props = new ArrayList<>();
        Processproperty p = new Processproperty();
        p.setTitel("title");
        p.setWert("value");
        props.add(p);

        EasyMock.expect(PropertyManager.getProcessPropertiesForProcess(EasyMock.anyInt())).andReturn(props);

        List<Step> steps = new ArrayList<>();
        Step step = new Step();
        step.setTitel("title");
        step.setBearbeitungsstatusEnum(StepStatus.OPEN);
        steps.add(step);
        process.setSchritte(steps);

        EasyMock.expect(StepManager.getStepsForProcess(EasyMock.anyInt())).andReturn(steps);
        Template template = new Template();
        Templateproperty tp = new Templateproperty();
        tp.setTitel("title");
        tp.setWert("value");
        List<Templateproperty> tpl = new ArrayList<>();
        tpl.add(tp);
        template.setEigenschaften(tpl);
        List<Template> tl = new ArrayList<>();
        tl.add(template);

        EasyMock.expect(TemplateManager.getTemplatesForProcess(EasyMock.anyInt())).andReturn(tl);

        EasyMock.expect(StepManager.getStepsForProcess(EasyMock.anyInt())).andReturn(steps);

        Masterpiece masterpiece = new Masterpiece();
        Masterpieceproperty mp = new Masterpieceproperty();
        mp.setTitel("title");
        mp.setWert("value");
        List<Masterpieceproperty> mpl = new ArrayList<>();
        mpl.add(mp);
        masterpiece.setEigenschaften(mpl);
        List<Masterpiece> ml = new ArrayList<>();
        ml.add(masterpiece);

        EasyMock.expect(MasterpieceManager.getMasterpiecesForProcess(EasyMock.anyInt())).andReturn(ml);

        List<HistoryEvent> hel = new ArrayList<>();
        HistoryEvent he = new HistoryEvent();
        he.setHistoryType(HistoryEventType.unknown);
        he.setNumericValue(1.0);
        he.setStringValue("one");
        hel.add(he);
        EasyMock.expect(HistoryManager.getHistoryEvents(EasyMock.anyInt())).andReturn(hel);

        List<StringPair> metadataList = new ArrayList<>();
        StringPair sp = new StringPair("title", "value");
        metadataList.add(sp);
        EasyMock.expect(MetadataManager.getMetadata(EasyMock.anyInt())).andReturn(metadataList);

        EasyMock.expectLastCall();
        PowerMock.replayAll();
    }

    @Test
    public void testConstructor() {
        GeneratePdfFromXslt xslt = new GeneratePdfFromXslt();
        assertNotNull(xslt);
    }

    @Test
    public void testXmlLog() throws Exception {

        XsltPreparatorXmlLog xmlExport = new XsltPreparatorXmlLog();
        assertNotNull(xmlExport);

        File fixture = folder.newFile("log.xml");

        xmlExport.startExport(process, fixture.toPath());

        assertNotNull(fixture);

        SAXBuilder saxBuilder = new SAXBuilder();
        Document document = saxBuilder.build(fixture);
        Element root = document.getRootElement();

        assertEquals("process", root.getName());

        assertEquals(root.getAttributeValue("processID"), "1");

        Element title = root.getChild("title", xmlns);
        assertEquals("testprocess", title.getValue());

        Element properties = root.getChild("properties", xmlns);
        Element prop = properties.getChildren().get(0);
        assertEquals("Test1", prop.getAttributeValue("propertyIdentifier"));
        assertEquals("1", prop.getAttributeValue("value"));

        Element steps = root.getChild("steps", xmlns);
        Element step = steps.getChildren().get(0);

        assertEquals("title", step.getChildText("title", xmlns));
        assertEquals("1", step.getChildText("processingstatus", xmlns));


        Element metadatalist =  root.getChild("metadatalist", xmlns);
        Element metadata =metadatalist.getChildren().get(0);

        assertEquals("title", metadata.getAttributeValue("name"));
        assertEquals("value", metadata.getValue());
    }

    @Test
    public void startMassExport() throws Exception {

        List<Process> processList = new ArrayList<>();
        processList.add(process);

        File fixture = folder.newFile("docket.pdf");

        OutputStream os = new FileOutputStream(fixture);

        GeneratePdfFromXslt xslt = new GeneratePdfFromXslt();
        assertNotNull(xslt);

        xslt.startExport(processList, os, xsltfile);

        assertTrue(fixture.exists());
        assertTrue(fixture.length() > 0);
    }

    @Test
    public void startSingleDocketExport() throws Exception {

        File fixture = folder.newFile("docket.pdf");

        OutputStream os = new FileOutputStream(fixture);

        GeneratePdfFromXslt xslt = new GeneratePdfFromXslt();
        assertNotNull(xslt);
        xslt.startExport(process, os, xsltfile, new XsltPreparatorXmlLog());

        assertTrue(fixture.exists());
        assertTrue(fixture.length() > 0);
    }

    @Test
    public void startMetadataExport() throws Exception {

        File fixture = folder.newFile("docket.pdf");

        OutputStream os = new FileOutputStream(fixture);

        GeneratePdfFromXslt xslt = new GeneratePdfFromXslt();
        assertNotNull(xslt);
        xslt.startExport(process, os, xsltfile, new XsltPreparatorSimplifiedMetadata());

        assertTrue(fixture.exists());
        assertTrue(fixture.length() > 0);
    }

}
