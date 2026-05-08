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
package io.goobi.workflow.xslt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.goobi.beans.GoobiProperty;
import org.goobi.beans.GoobiProperty.PropertyOwnerType;
import org.goobi.beans.HistoryEvent;
import org.goobi.beans.Institution;
import org.goobi.beans.InstitutionConfigurationObject;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.beans.User;
import org.goobi.production.cli.helper.StringPair;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import de.sub.goobi.AbstractTest;
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

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class XsltPreparatorDocketTest extends AbstractTest {

    private static final Namespace XMLNS = Namespace.getNamespace("http://www.goobi.io/logfile");

    private Process process;

    private List<HistoryEvent> hel;
    private List<InstitutionConfigurationObject> icolist;
    private List<StringPair> metadataList;
    private List<GoobiProperty> props;
    private List<Step> steps;

    @BeforeEach
    public void setUp() throws Exception {
        process = MockProcess.createProcess();
        process.setErstellungsdatum(new Date());

        // Institution
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
        Step step = new Step();
        step.setTitel("title");
        step.setBearbeitungsstatusEnum(StepStatus.OPEN);
        User user = new User();
        user.setId(1);
        user.setNachname("lastname");
        user.setVorname("firstname");
        user.setLogin("login");
        user.setStandort("location");
        step.setBearbeitungsbenutzer(user);
        steps.add(step);
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

        FacesContext facesContext = Mockito.mock(FacesContext.class);
        FacesContextHelper.setFacesContext(facesContext);
        ExternalContext externalContext = Mockito.mock(ExternalContext.class);

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

        Mockito.when(facesContext.getExternalContext()).thenReturn(externalContext);
        Mockito.when(externalContext.getRequestContextPath()).thenReturn("junit");
        Mockito.when(externalContext.getRequest()).thenReturn(request);
        Mockito.when(request.getScheme()).thenReturn("https");
        Mockito.when(request.getServerName()).thenReturn("example.com");
        Mockito.when(request.getServerPort()).thenReturn(443);
        Mockito.when(request.getContextPath()).thenReturn("/goobi");

    }

    @Test
    public void testStartExportToOutputStream() throws Exception {
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

            XsltPreparatorDocket exporter = new XsltPreparatorDocket();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            exporter.startExport(process, bos, null);

            assertTrue(bos.size() > 0);
            String xml = bos.toString("UTF-8");
            assertTrue(xml.contains(XsltPreparatorDocket.ELEMENT_PROCESS));
            assertTrue(xml.contains("testprocess"));

        }
    }

    @Test
    public void testStartExportToOutputStreamWithIncludeImagesTrue() throws Exception {
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

            XsltPreparatorDocket exporter = new XsltPreparatorDocket();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            exporter.startExport(process, bos, null, true);

            assertTrue(bos.size() > 0);
            String xml = bos.toString("UTF-8");
            assertTrue(xml.contains(XsltPreparatorDocket.ELEMENT_PROCESS));

        }
    }

    @Test
    public void testStartExportToOutputStreamWithIncludeImagesFalse() throws Exception {
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

            XsltPreparatorDocket exporter = new XsltPreparatorDocket();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            exporter.startExport(process, bos, null, false);

            assertTrue(bos.size() > 0);
            String xml = bos.toString("UTF-8");
            assertTrue(xml.contains(XsltPreparatorDocket.ELEMENT_PROCESS));
            assertTrue(xml.contains("testprocess"));

        }
    }

    @Test
    public void testStartExportListWithoutBoolean() throws Exception {
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

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            XsltPreparatorDocket exporter = new XsltPreparatorDocket();
            exporter.startExport(processList, bos, null);

            assertTrue(bos.size() > 0);
            String xml = bos.toString("UTF-8");
            assertTrue(xml.contains(XsltPreparatorDocket.ELEMENT_PROCESSES));
            assertTrue(xml.contains(XsltPreparatorDocket.ELEMENT_PROCESS));

        }
    }

    @Test
    public void testStartExportListWithIncludeImagesTrue() throws Exception {
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

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            XsltPreparatorDocket exporter = new XsltPreparatorDocket();
            exporter.startExport(processList, bos, null, true);

            assertTrue(bos.size() > 0);
            String xml = bos.toString("UTF-8");
            assertTrue(xml.contains(XsltPreparatorDocket.ELEMENT_PROCESSES));

        }
    }

    @Test
    public void testXmlTransformationWithNullFilename() throws Exception {
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

            XsltPreparatorDocket exporter = new XsltPreparatorDocket();
            Document doc = exporter.createDocument(process, true, false);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            exporter.xmlTransformation(bos, doc, null);

            assertTrue(bos.size() > 0);
            String xml = bos.toString("UTF-8");
            assertTrue(xml.contains(XsltPreparatorDocket.ELEMENT_PROCESS));

        }
    }

    @Test
    public void testXmlTransformationWithNonEmptyFilename() throws Exception {
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

            XsltPreparatorDocket exporter = new XsltPreparatorDocket();
            Document doc = exporter.createDocument(process, true, false);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            // A non-empty filename means no XSLT transformation - doc is output as-is
            exporter.xmlTransformation(bos, doc, "somefile.xsl");

            assertTrue(bos.size() > 0);

        }
    }

    @Test
    public void testStartTransformation() throws Exception {
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

            XsltPreparatorDocket exporter = new XsltPreparatorDocket();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            exporter.startTransformation(process, bos, null);

            assertTrue(bos.size() > 0);
            String xml = bos.toString("UTF-8");
            assertTrue(xml.contains(XsltPreparatorDocket.ELEMENT_PROCESS));

        }
    }

    @Test
    public void testStartTransformationAlternativeSignature() throws Exception {
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

            XsltPreparatorDocket exporter = new XsltPreparatorDocket();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            exporter.startTransformation(bos, process, null);

            assertTrue(bos.size() > 0);
            String xml = bos.toString("UTF-8");
            assertTrue(xml.contains(XsltPreparatorDocket.ELEMENT_PROCESS));

        }
    }

    @Test
    public void testGetProjectExportConfiguration() {
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

            XsltPreparatorDocket exporter = new XsltPreparatorDocket();
            Namespace ns = Namespace.getNamespace(XsltPreparatorDocket.FILE_LOG);
            Element projectElement = new Element(XsltPreparatorDocket.ELEMENT_PROJECT, ns);

            exporter.getProjectExportConfiguration(process, projectElement);

            Element exportConfig = projectElement.getChild(XsltPreparatorDocket.ELEMENT_EXPORT_CONFIGURATION, ns);
            assertNotNull(exportConfig);

            // useDmsImport should be present
            String useDmsImport = exportConfig.getAttributeValue(XsltPreparatorDocket.ATTRIBUTE_USE_DMS_IMPORT);
            assertNotNull(useDmsImport);
            assertEquals("true", useDmsImport);

        }
    }

    @Test
    public void testGetMetsConfiguration() {
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

            XsltPreparatorDocket exporter = new XsltPreparatorDocket();
            Namespace ns = Namespace.getNamespace(XsltPreparatorDocket.FILE_LOG);
            Element projectElement = new Element(XsltPreparatorDocket.ELEMENT_PROJECT, ns);

            exporter.getMetsConfiguration(process, projectElement);

            Element metsConfig = projectElement.getChild(XsltPreparatorDocket.ELEMENT_METS_CONFIGURATION, ns);
            assertNotNull(metsConfig);
            assertNotNull(metsConfig.getChild(XsltPreparatorDocket.ELEMENT_METS_RIGHTS_OWNER, ns));
            assertNotNull(metsConfig.getChild(XsltPreparatorDocket.ELEMENT_METS_DIGIPROV_REFERENCE, ns));
            assertNotNull(metsConfig.getChild(XsltPreparatorDocket.ELEMENT_METS_POINTER_PATH, ns));

        }
    }

    @Test
    public void testCreateDocumentStructure() throws Exception {
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

            XsltPreparatorDocket exporter = new XsltPreparatorDocket();
            Document doc = exporter.createDocument(process, true, false);

            assertNotNull(doc);
            Element root = doc.getRootElement();
            assertEquals(XsltPreparatorDocket.ELEMENT_PROCESS, root.getName());
            assertEquals("1", root.getAttributeValue(XsltPreparatorDocket.ATTRIBUTE_PROCESS_ID));

            Element title = root.getChild(XsltPreparatorDocket.ELEMENT_TITLE, XMLNS);
            assertNotNull(title);
            assertEquals("testprocess", title.getValue());

        }
    }

}
