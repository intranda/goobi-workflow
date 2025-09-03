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
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
package io.goobi.workflow.xslt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.easymock.EasyMock;
import org.goobi.beans.Batch;
import org.goobi.beans.GoobiProperty;
import org.goobi.beans.GoobiProperty.PropertyOwnerType;
import org.goobi.beans.HistoryEvent;
import org.goobi.beans.Institution;
import org.goobi.beans.InstitutionConfigurationObject;
import org.goobi.beans.JournalEntry;
import org.goobi.beans.JournalEntry.EntryType;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.beans.User;
import org.goobi.production.cli.helper.StringPair;
import org.goobi.production.enums.LogType;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.junit.Before;
import org.junit.Ignore;
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
import de.sub.goobi.persistence.managers.InstitutionManager;
import de.sub.goobi.persistence.managers.MetadataManager;
import de.sub.goobi.persistence.managers.PropertyManager;
import de.sub.goobi.persistence.managers.StepManager;
import de.sub.goobi.persistence.managers.UserManager;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpServletRequest;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ PropertyManager.class, StepManager.class, HistoryManager.class,
        MetadataManager.class, FacesContext.class, ExternalContext.class, Helper.class, InstitutionManager.class, UserManager.class })
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*" })
public class GeneratePdfFromXsltTest extends AbstractTest {

    private static final Namespace xmlns = Namespace.getNamespace("http://www.goobi.io/logfile");

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private Process process;
    private String xsltfile;

    @Before
    public void setUp() throws Exception {
        Path path = Paths.get(ConfigProjectsTest.class.getClassLoader().getResource(".").getFile());
        Path goobiFolder = Paths.get(path.getParent().getParent().toString() + "/src/test/resources/config/goobi_config.properties"); // for junit tests in eclipse
        if (!Files.exists(goobiFolder)) {
            goobiFolder = Paths.get("target/test-classes/config/goobi_config.properties"); // to run mvn test from cli or in jenkins
        }
        ConfigurationHelper.configFileName = goobiFolder.toString();
        ConfigurationHelper.resetConfigurationFile();
        ConfigurationHelper.getInstance().setParameter("goobiFolder", goobiFolder.getParent().getParent().toString() + "/");
        xsltfile = goobiFolder.getParent().getParent() + "/xslt/docket.xsl";

        // journal

        process = MockProcess.createProcess();
        List<JournalEntry> journal = new ArrayList<>();
        JournalEntry e1 = new JournalEntry(1, 1, new Date(), "user", LogType.INFO, "content", "", EntryType.PROCESS, null);
        journal.add(e1);
        JournalEntry e2 =
                new JournalEntry(2, 1, new Date(), "user", LogType.FILE, "content", "filename", EntryType.PROCESS, Paths.get("/path/to/fle"));
        journal.add(e2);
        process.setJournal(journal);

        // batch
        Batch batch = new Batch();
        batch.setBatchId(1);
        batch.setBatchLabel("label");
        batch.setBatchName("name");
        batch.setStartDate(new Date());
        batch.setEndDate(new Date());
        process.setBatch(batch);

        // institution
        Institution inst = new Institution();
        inst.setId(1);
        inst.setShortName("short name");
        inst.setLongName("long name");

        InstitutionConfigurationObject ico = new InstitutionConfigurationObject();
        ico.setInstitution_id(1);
        ico.setId(1);

        ico.setObject_id(1);
        ico.setObject_name("name");
        ico.setObject_type("type");

        List<InstitutionConfigurationObject> icolist = new ArrayList<>();
        icolist.add(ico);
        PowerMock.mockStatic(InstitutionManager.class);
        EasyMock.expect(InstitutionManager.getConfiguredRulesets(EasyMock.anyInt())).andReturn(icolist).anyTimes();
        EasyMock.expect(InstitutionManager.getConfiguredDockets(EasyMock.anyInt())).andReturn(icolist).anyTimes();
        EasyMock.expect(InstitutionManager.getConfiguredAuthentications(EasyMock.anyInt())).andReturn(icolist).anyTimes();

        process.getProjekt().setInstitution(inst);

        process.setSortHelperStatus("12345567890");

        PowerMock.mockStatic(PropertyManager.class);
        PowerMock.mockStatic(StepManager.class);
        PowerMock.mockStatic(HistoryManager.class);
        PowerMock.mockStatic(MetadataManager.class);

        PowerMock.mockStatic(UserManager.class);

        List<GoobiProperty> props = new ArrayList<>();
        GoobiProperty p = new GoobiProperty(PropertyOwnerType.PROCESS);
        p.setPropertyName("title");
        p.setPropertyValue("value");
        props.add(p);

        EasyMock.expect(PropertyManager.getPropertiesForObject(EasyMock.anyInt(), EasyMock.anyObject())).andReturn(props).anyTimes();
        List<Step> steps = new ArrayList<>();
        Step step = new Step();
        step.setTitel("title");
        step.setBearbeitungsstatusEnum(StepStatus.OPEN);
        steps.add(step);
        User user = new User();
        user.setId(1);
        user.setNachname("lastname");
        user.setVorname("firstname");
        user.setLogin("login");
        user.setStandort("location");
        step.setBearbeitungsbenutzer(user);

        process.setSchritte(steps);

        EasyMock.expect(StepManager.getStepsForProcess(EasyMock.anyInt())).andReturn(steps);

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

        EasyMock.expect(UserManager.getAllUsers()).andReturn(Collections.emptyList()).anyTimes();

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

    @Ignore
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

    @Ignore
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

    @Ignore
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

    @Test
    public void testStartExportList() throws Exception {
        List<Process> processList = new ArrayList<>();
        processList.add(process);

        File fixture = folder.newFile("docket.pdf");

        OutputStream os = new FileOutputStream(fixture);

        XsltPreparatorDocket xslt = new XsltPreparatorDocket();
        assertNotNull(xslt);
        xslt.startExport(processList, os, xsltfile, false);
        assertTrue(fixture.exists());
        assertTrue(fixture.length() > 0);
    }

    @Test
    public void testCreateExtendedDocument() throws Exception {

        XsltPreparatorDocket xslt = new XsltPreparatorDocket();

        Document fixture = xslt.createExtendedDocument(process);
        assertNotNull(fixture);
    }

}
