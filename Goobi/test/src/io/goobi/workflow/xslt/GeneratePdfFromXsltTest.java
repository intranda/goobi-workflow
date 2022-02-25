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

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

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

import de.sub.goobi.AbstractTest;
import de.sub.goobi.config.ConfigProjectsTest;
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.helper.Helper;
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
    MetadataManager.class, FacesContext.class, ExternalContext.class, Helper.class })
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*"})
public class GeneratePdfFromXsltTest extends AbstractTest {

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
        EasyMock.expect(MetadataManager.getMetadata(EasyMock.anyInt())).andReturn(metadataList).anyTimes();

        //        PowerMock.createMockAndExpectNew(Image.class, EasyMock.anyObject(Paths.class), EasyMock.anyInt(), EasyMock.anyInt());
        //        EasyMock.expect(new HelperForm()).andReturn(helperFormMock).anyTimes();
        //        EasyMock.expect(helperFormMock.getServletPathWithHostAsUrl()).andReturn("http://example.com").anyTimes();
        //        EasyMock.replay(helperFormMock);
        PowerMock.mockStatic(Helper.class);
        EasyMock.expect(Helper.getTranslation(EasyMock.anyString())).andReturn("fixture").anyTimes();

        EasyMock.expect(Helper.getDateAsFormattedString(EasyMock.anyObject())).andReturn("date").anyTimes();
        EasyMock.expect(Helper.getMetadataLanguage()).andReturn("en").anyTimes();


        EasyMock.expectLastCall();
        PowerMock.replayAll();

        PowerMock.mockStatic(ExternalContext.class);
        PowerMock.mockStatic(FacesContext.class);
        FacesContext facesContext = EasyMock.createMock(FacesContext.class);
        FacesContextHelper.setFacesContext(facesContext);
        ExternalContext externalContext = EasyMock.createMock(ExternalContext.class);

        HttpServletRequest request = EasyMock.createMock(HttpServletRequest.class);

        EasyMock.expect(facesContext.getExternalContext()).andReturn(externalContext).anyTimes();
        EasyMock.expect(externalContext.getRequestContextPath()).andReturn("junit").anyTimes();
        EasyMock.expect(externalContext.getRequest()).andReturn(request).anyTimes();
        EasyMock.expect(request.getScheme()).andReturn("https").anyTimes();
        EasyMock.expect(request.getServerName()).andReturn("example.com").anyTimes();
        EasyMock.expect(request.getServerPort()).andReturn(443).anyTimes();
        EasyMock.expect(request.getContextPath()).andReturn("/goobi").anyTimes();

        EasyMock.replay(request);
        EasyMock.replay(externalContext);
        EasyMock.replay(facesContext);

    }

    @Test
    public void testConstructor() {
        XsltToPdf xslt = new XsltToPdf();
        assertNotNull(xslt);
    }

    @Test
    public void testXmlLog() throws Exception {

        XsltPreparatorDocket xmlExport = new XsltPreparatorDocket();
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

        Element metadatalist = root.getChild("metadatalist", xmlns);
        Element metadata = metadatalist.getChildren().get(0);

        assertEquals("title", metadata.getAttributeValue("name"));
        assertEquals("value", metadata.getValue());
    }

    @Test
    public void startMassExport() throws Exception {

        List<Process> processList = new ArrayList<>();
        processList.add(process);

        File fixture = folder.newFile("docket.pdf");

        OutputStream os = new FileOutputStream(fixture);

        XsltToPdf xslt = new XsltToPdf();
        assertNotNull(xslt);

        xslt.startExport(processList, os, xsltfile);

        assertTrue(fixture.exists());
        assertTrue(fixture.length() > 0);
    }

    @Test
    public void startSingleDocketExport() throws Exception {

        File fixture = folder.newFile("docket.pdf");

        OutputStream os = new FileOutputStream(fixture);

        XsltToPdf xslt = new XsltToPdf();
        assertNotNull(xslt);
        xslt.startExport(process, os, xsltfile, new XsltPreparatorDocket());

        assertTrue(fixture.exists());
        assertTrue(fixture.length() > 0);
    }

    @Test
    public void startMetadataExport() throws Exception {

        File fixture = folder.newFile("docket.pdf");

        OutputStream os = new FileOutputStream(fixture);

        XsltToPdf xslt = new XsltToPdf();
        assertNotNull(xslt);
        xslt.startExport(process, os, xsltfile, new XsltPreparatorMetadata());

        assertTrue(fixture.exists());
        assertTrue(fixture.length() > 0);
    }

}
