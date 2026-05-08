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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
@ExtendWith(MockitoExtension.class)
public class GeneratePdfFromXsltTest extends AbstractTest {

    private static final Namespace XMLNS = Namespace.getNamespace("http://www.goobi.io/logfile");

    @TempDir
    Path tempDir;

    private Process process;
    private String xsltfile;

    private List<HistoryEvent> hel;
    private List<InstitutionConfigurationObject> icolist;
    private List<StringPair> metadataList;
    private List<GoobiProperty> props;
    private Step step;
    private List<Step> steps;

    @BeforeEach
    public void setUp() throws Exception {
        Path path = Paths.get(ConfigProjectsTest.class.getClassLoader().getResource(".").getFile());
        Path goobiFolder = Paths.get(path.getParent().getParent().toString()
                + "/src/test/resources/config/goobi_config.properties"); // for junit tests in eclipse
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

        icolist = new ArrayList<>();
        icolist.add(ico);

        process.getProjekt().setInstitution(inst);

        process.setSortHelperStatus("12345567890");



        props = new ArrayList<>();
        GoobiProperty p = new GoobiProperty(PropertyOwnerType.PROCESS);
        p.setPropertyName("title");
        p.setPropertyValue("value");
        props.add(p);

        steps = new ArrayList<>();
        step = new Step();
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


        hel = new ArrayList<>();
        HistoryEvent he = new HistoryEvent();
        he.setHistoryType(HistoryEventType.unknown);
        he.setNumericValue(1.0);
        he.setStringValue("one");
        hel.add(he);

        metadataList = new ArrayList<>();
        StringPair sp = new StringPair("title", "value");
        metadataList.add(sp);


        //        PowerMock.createMockAndExpectNew(Image.class, EasyMock.anyObject(Paths.class), EasyMock.anyInt(), EasyMock.anyInt());
        //        EasyMock.expect(new HelperForm()).andReturn(helperFormMock);
        //        EasyMock.expect(helperFormMock.getServletPathWithHostAsUrl()).andReturn("http://example.com");


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

        EasyMock.replay(facesContext); EasyMock.replay(externalContext); EasyMock.replay(request);
    }

    @Test
    public void testConstructor() {
        try (MockedStatic<InstitutionManager> mockedInstitutionManager = Mockito.mockStatic(InstitutionManager.class);
             MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
             MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class);
             MockedStatic<HistoryManager> mockedHistoryManager = Mockito.mockStatic(HistoryManager.class);
             MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class);
             MockedStatic<UserManager> mockedUserManager = Mockito.mockStatic(UserManager.class);
             MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
             MockedStatic<ExternalContext> mockedExternalContext = Mockito.mockStatic(ExternalContext.class);
             MockedStatic<FacesContext> mockedFacesContext = Mockito.mockStatic(FacesContext.class)) {
            mockedInstitutionManager.when(() -> InstitutionManager.getConfiguredRulesets(Mockito.anyInt())).thenReturn(icolist);
            mockedInstitutionManager.when(() -> InstitutionManager.getConfiguredDockets(Mockito.anyInt())).thenReturn(icolist);
            mockedInstitutionManager.when(() -> InstitutionManager.getConfiguredAuthentications(Mockito.anyInt())).thenReturn(icolist);
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any())).thenReturn(props);
            mockedStepManager.when(() -> StepManager.getStepsForProcess(Mockito.anyInt())).thenReturn(steps);
            mockedHistoryManager.when(() -> HistoryManager.getHistoryEvents(Mockito.anyInt())).thenReturn(hel);
            mockedMetadataManager.when(() -> MetadataManager.getMetadata(Mockito.anyInt())).thenReturn(metadataList);
            mockedUserManager.when(() -> UserManager.getAllUsers()).thenReturn(Collections.emptyList());
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("fixture");
            mockedHelper.when(() -> Helper.getDateAsFormattedString(Mockito.any())).thenReturn("date");
            mockedHelper.when(() -> Helper.getMetadataLanguage()).thenReturn("en");


            XsltToPdf xslt = new XsltToPdf();
            assertNotNull(xslt);
    
        }
}

    @Test
    public void testXmlLog() throws Exception {
        try (MockedStatic<InstitutionManager> mockedInstitutionManager = Mockito.mockStatic(InstitutionManager.class);
             MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
             MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class);
             MockedStatic<HistoryManager> mockedHistoryManager = Mockito.mockStatic(HistoryManager.class);
             MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class);
             MockedStatic<UserManager> mockedUserManager = Mockito.mockStatic(UserManager.class);
             MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
             MockedStatic<ExternalContext> mockedExternalContext = Mockito.mockStatic(ExternalContext.class);
             MockedStatic<FacesContext> mockedFacesContext = Mockito.mockStatic(FacesContext.class)) {
            mockedInstitutionManager.when(() -> InstitutionManager.getConfiguredRulesets(Mockito.anyInt())).thenReturn(icolist);
            mockedInstitutionManager.when(() -> InstitutionManager.getConfiguredDockets(Mockito.anyInt())).thenReturn(icolist);
            mockedInstitutionManager.when(() -> InstitutionManager.getConfiguredAuthentications(Mockito.anyInt())).thenReturn(icolist);
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any())).thenReturn(props);
            mockedStepManager.when(() -> StepManager.getStepsForProcess(Mockito.anyInt())).thenReturn(steps);
            mockedHistoryManager.when(() -> HistoryManager.getHistoryEvents(Mockito.anyInt())).thenReturn(hel);
            mockedMetadataManager.when(() -> MetadataManager.getMetadata(Mockito.anyInt())).thenReturn(metadataList);
            mockedUserManager.when(() -> UserManager.getAllUsers()).thenReturn(Collections.emptyList());
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("fixture");
            mockedHelper.when(() -> Helper.getDateAsFormattedString(Mockito.any())).thenReturn("date");
            mockedHelper.when(() -> Helper.getMetadataLanguage()).thenReturn("en");



            XsltPreparatorDocket xmlExport = new XsltPreparatorDocket();
            assertNotNull(xmlExport);

            File fixture = tempDir.resolve("log.xml").toFile();

            xmlExport.startExport(process, fixture.toPath());

            assertNotNull(fixture);

            SAXBuilder saxBuilder = new SAXBuilder();
            Document document = saxBuilder.build(fixture);
            Element root = document.getRootElement();

            assertEquals("process", root.getName());

            assertEquals(root.getAttributeValue("processID"), "1");

            Element title = root.getChild("title", XMLNS);
            assertEquals("testprocess", title.getValue());

            Element properties = root.getChild("properties", XMLNS);
            Element prop = properties.getChildren().get(0);
            assertEquals("Test1", prop.getAttributeValue("propertyIdentifier"));
            assertEquals("1", prop.getAttributeValue("value"));

            Element steps = root.getChild("steps", XMLNS);
            Element step = steps.getChildren().get(0);

            assertEquals("title", step.getChildText("title", XMLNS));
            assertEquals("1", step.getChildText("processingstatus", XMLNS));

            Element metadatalist = root.getChild("metadatalist", XMLNS);
            Element metadata = metadatalist.getChildren().get(0);

            assertEquals("title", metadata.getAttributeValue("name"));
            assertEquals("value", metadata.getValue());
    
        }
}

    @Disabled
    @Test
    public void startMassExport() throws Exception {
        try (MockedStatic<InstitutionManager> mockedInstitutionManager = Mockito.mockStatic(InstitutionManager.class);
             MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
             MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class);
             MockedStatic<HistoryManager> mockedHistoryManager = Mockito.mockStatic(HistoryManager.class);
             MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class);
             MockedStatic<UserManager> mockedUserManager = Mockito.mockStatic(UserManager.class);
             MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
             MockedStatic<ExternalContext> mockedExternalContext = Mockito.mockStatic(ExternalContext.class);
             MockedStatic<FacesContext> mockedFacesContext = Mockito.mockStatic(FacesContext.class)) {
            mockedInstitutionManager.when(() -> InstitutionManager.getConfiguredRulesets(Mockito.anyInt())).thenReturn(icolist);
            mockedInstitutionManager.when(() -> InstitutionManager.getConfiguredDockets(Mockito.anyInt())).thenReturn(icolist);
            mockedInstitutionManager.when(() -> InstitutionManager.getConfiguredAuthentications(Mockito.anyInt())).thenReturn(icolist);
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any())).thenReturn(props);
            mockedStepManager.when(() -> StepManager.getStepsForProcess(Mockito.anyInt())).thenReturn(steps);
            mockedHistoryManager.when(() -> HistoryManager.getHistoryEvents(Mockito.anyInt())).thenReturn(hel);
            mockedMetadataManager.when(() -> MetadataManager.getMetadata(Mockito.anyInt())).thenReturn(metadataList);
            mockedUserManager.when(() -> UserManager.getAllUsers()).thenReturn(Collections.emptyList());
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("fixture");
            mockedHelper.when(() -> Helper.getDateAsFormattedString(Mockito.any())).thenReturn("date");
            mockedHelper.when(() -> Helper.getMetadataLanguage()).thenReturn("en");



            List<Process> processList = new ArrayList<>();
            processList.add(process);

            File fixture = tempDir.resolve("docket.pdf").toFile();

            OutputStream os = new FileOutputStream(fixture);

            XsltToPdf xslt = new XsltToPdf();
            assertNotNull(xslt);

            xslt.startExport(processList, os, xsltfile);

            assertTrue(fixture.exists());
            assertTrue(fixture.length() > 0);
    
        }
}

    @Disabled
    @Test
    public void startSingleDocketExport() throws Exception {
        try (MockedStatic<InstitutionManager> mockedInstitutionManager = Mockito.mockStatic(InstitutionManager.class);
             MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
             MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class);
             MockedStatic<HistoryManager> mockedHistoryManager = Mockito.mockStatic(HistoryManager.class);
             MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class);
             MockedStatic<UserManager> mockedUserManager = Mockito.mockStatic(UserManager.class);
             MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
             MockedStatic<ExternalContext> mockedExternalContext = Mockito.mockStatic(ExternalContext.class);
             MockedStatic<FacesContext> mockedFacesContext = Mockito.mockStatic(FacesContext.class)) {
            mockedInstitutionManager.when(() -> InstitutionManager.getConfiguredRulesets(Mockito.anyInt())).thenReturn(icolist);
            mockedInstitutionManager.when(() -> InstitutionManager.getConfiguredDockets(Mockito.anyInt())).thenReturn(icolist);
            mockedInstitutionManager.when(() -> InstitutionManager.getConfiguredAuthentications(Mockito.anyInt())).thenReturn(icolist);
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any())).thenReturn(props);
            mockedStepManager.when(() -> StepManager.getStepsForProcess(Mockito.anyInt())).thenReturn(steps);
            mockedHistoryManager.when(() -> HistoryManager.getHistoryEvents(Mockito.anyInt())).thenReturn(hel);
            mockedMetadataManager.when(() -> MetadataManager.getMetadata(Mockito.anyInt())).thenReturn(metadataList);
            mockedUserManager.when(() -> UserManager.getAllUsers()).thenReturn(Collections.emptyList());
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("fixture");
            mockedHelper.when(() -> Helper.getDateAsFormattedString(Mockito.any())).thenReturn("date");
            mockedHelper.when(() -> Helper.getMetadataLanguage()).thenReturn("en");



            File fixture = tempDir.resolve("docket.pdf").toFile();

            OutputStream os = new FileOutputStream(fixture);

            XsltToPdf xslt = new XsltToPdf();
            assertNotNull(xslt);
            xslt.startExport(process, os, xsltfile, new XsltPreparatorDocket());

            assertTrue(fixture.exists());
            assertTrue(fixture.length() > 0);
    
        }
}

    @Disabled
    @Test
    public void startMetadataExport() throws Exception {
        try (MockedStatic<InstitutionManager> mockedInstitutionManager = Mockito.mockStatic(InstitutionManager.class);
             MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
             MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class);
             MockedStatic<HistoryManager> mockedHistoryManager = Mockito.mockStatic(HistoryManager.class);
             MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class);
             MockedStatic<UserManager> mockedUserManager = Mockito.mockStatic(UserManager.class);
             MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
             MockedStatic<ExternalContext> mockedExternalContext = Mockito.mockStatic(ExternalContext.class);
             MockedStatic<FacesContext> mockedFacesContext = Mockito.mockStatic(FacesContext.class)) {
            mockedInstitutionManager.when(() -> InstitutionManager.getConfiguredRulesets(Mockito.anyInt())).thenReturn(icolist);
            mockedInstitutionManager.when(() -> InstitutionManager.getConfiguredDockets(Mockito.anyInt())).thenReturn(icolist);
            mockedInstitutionManager.when(() -> InstitutionManager.getConfiguredAuthentications(Mockito.anyInt())).thenReturn(icolist);
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any())).thenReturn(props);
            mockedStepManager.when(() -> StepManager.getStepsForProcess(Mockito.anyInt())).thenReturn(steps);
            mockedHistoryManager.when(() -> HistoryManager.getHistoryEvents(Mockito.anyInt())).thenReturn(hel);
            mockedMetadataManager.when(() -> MetadataManager.getMetadata(Mockito.anyInt())).thenReturn(metadataList);
            mockedUserManager.when(() -> UserManager.getAllUsers()).thenReturn(Collections.emptyList());
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("fixture");
            mockedHelper.when(() -> Helper.getDateAsFormattedString(Mockito.any())).thenReturn("date");
            mockedHelper.when(() -> Helper.getMetadataLanguage()).thenReturn("en");



            File fixture = tempDir.resolve("docket.pdf").toFile();

            OutputStream os = new FileOutputStream(fixture);

            XsltToPdf xslt = new XsltToPdf();
            assertNotNull(xslt);
            xslt.startExport(process, os, xsltfile, new XsltPreparatorMetadata());

            assertTrue(fixture.exists());
            assertTrue(fixture.length() > 0);
    
        }
}

    @Test
    public void testStartExportList() throws Exception {
        try (MockedStatic<InstitutionManager> mockedInstitutionManager = Mockito.mockStatic(InstitutionManager.class);
             MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
             MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class);
             MockedStatic<HistoryManager> mockedHistoryManager = Mockito.mockStatic(HistoryManager.class);
             MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class);
             MockedStatic<UserManager> mockedUserManager = Mockito.mockStatic(UserManager.class);
             MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
             MockedStatic<ExternalContext> mockedExternalContext = Mockito.mockStatic(ExternalContext.class);
             MockedStatic<FacesContext> mockedFacesContext = Mockito.mockStatic(FacesContext.class)) {
            mockedInstitutionManager.when(() -> InstitutionManager.getConfiguredRulesets(Mockito.anyInt())).thenReturn(icolist);
            mockedInstitutionManager.when(() -> InstitutionManager.getConfiguredDockets(Mockito.anyInt())).thenReturn(icolist);
            mockedInstitutionManager.when(() -> InstitutionManager.getConfiguredAuthentications(Mockito.anyInt())).thenReturn(icolist);
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any())).thenReturn(props);
            mockedStepManager.when(() -> StepManager.getStepsForProcess(Mockito.anyInt())).thenReturn(steps);
            mockedHistoryManager.when(() -> HistoryManager.getHistoryEvents(Mockito.anyInt())).thenReturn(hel);
            mockedMetadataManager.when(() -> MetadataManager.getMetadata(Mockito.anyInt())).thenReturn(metadataList);
            mockedUserManager.when(() -> UserManager.getAllUsers()).thenReturn(Collections.emptyList());
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("fixture");
            mockedHelper.when(() -> Helper.getDateAsFormattedString(Mockito.any())).thenReturn("date");
            mockedHelper.when(() -> Helper.getMetadataLanguage()).thenReturn("en");


            List<Process> processList = new ArrayList<>();
            processList.add(process);

            File fixture = tempDir.resolve("docket.pdf").toFile();

            OutputStream os = new FileOutputStream(fixture);

            XsltPreparatorDocket xslt = new XsltPreparatorDocket();
            assertNotNull(xslt);
            xslt.startExport(processList, os, xsltfile, false);
            assertTrue(fixture.exists());
            assertTrue(fixture.length() > 0);
    
        }
}

    @Test
    public void testCreateExtendedDocument() throws Exception {
        try (MockedStatic<InstitutionManager> mockedInstitutionManager = Mockito.mockStatic(InstitutionManager.class);
             MockedStatic<PropertyManager> mockedPropertyManager = Mockito.mockStatic(PropertyManager.class);
             MockedStatic<StepManager> mockedStepManager = Mockito.mockStatic(StepManager.class);
             MockedStatic<HistoryManager> mockedHistoryManager = Mockito.mockStatic(HistoryManager.class);
             MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class);
             MockedStatic<UserManager> mockedUserManager = Mockito.mockStatic(UserManager.class);
             MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
             MockedStatic<ExternalContext> mockedExternalContext = Mockito.mockStatic(ExternalContext.class);
             MockedStatic<FacesContext> mockedFacesContext = Mockito.mockStatic(FacesContext.class)) {
            mockedInstitutionManager.when(() -> InstitutionManager.getConfiguredRulesets(Mockito.anyInt())).thenReturn(icolist);
            mockedInstitutionManager.when(() -> InstitutionManager.getConfiguredDockets(Mockito.anyInt())).thenReturn(icolist);
            mockedInstitutionManager.when(() -> InstitutionManager.getConfiguredAuthentications(Mockito.anyInt())).thenReturn(icolist);
            mockedPropertyManager.when(() -> PropertyManager.getPropertiesForObject(Mockito.anyInt(), Mockito.any())).thenReturn(props);
            mockedStepManager.when(() -> StepManager.getStepsForProcess(Mockito.anyInt())).thenReturn(steps);
            mockedHistoryManager.when(() -> HistoryManager.getHistoryEvents(Mockito.anyInt())).thenReturn(hel);
            mockedMetadataManager.when(() -> MetadataManager.getMetadata(Mockito.anyInt())).thenReturn(metadataList);
            mockedUserManager.when(() -> UserManager.getAllUsers()).thenReturn(Collections.emptyList());
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("fixture");
            mockedHelper.when(() -> Helper.getDateAsFormattedString(Mockito.any())).thenReturn("date");
            mockedHelper.when(() -> Helper.getMetadataLanguage()).thenReturn("en");



            XsltPreparatorDocket xslt = new XsltPreparatorDocket();

            Document fixture = xslt.createExtendedDocument(process);
            assertNotNull(fixture);
    
        }
}

}
