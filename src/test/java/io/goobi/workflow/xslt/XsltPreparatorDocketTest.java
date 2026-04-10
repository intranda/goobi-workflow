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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.easymock.EasyMock;
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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

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

@RunWith(PowerMockRunner.class)
@PrepareForTest({ PropertyManager.class, StepManager.class, HistoryManager.class,
        MetadataManager.class, FacesContext.class, ExternalContext.class, Helper.class, InstitutionManager.class, UserManager.class })
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*" })
public class XsltPreparatorDocketTest extends AbstractTest {

    private static final Namespace XMLNS = Namespace.getNamespace("http://www.goobi.io/logfile");

    private Process process;

    @Before
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
        User user = new User();
        user.setId(1);
        user.setNachname("lastname");
        user.setVorname("firstname");
        user.setLogin("login");
        user.setStandort("location");
        step.setBearbeitungsbenutzer(user);
        steps.add(step);
        process.setSchritte(steps);

        EasyMock.expect(StepManager.getStepsForProcess(EasyMock.anyInt())).andReturn(steps).anyTimes();

        List<HistoryEvent> hel = new ArrayList<>();
        HistoryEvent he = new HistoryEvent();
        he.setHistoryType(HistoryEventType.unknown);
        he.setNumericValue(1.0);
        he.setStringValue("one");
        hel.add(he);
        EasyMock.expect(HistoryManager.getHistoryEvents(EasyMock.anyInt())).andReturn(hel).anyTimes();

        List<StringPair> metadataList = new ArrayList<>();
        StringPair sp = new StringPair("title", "value");
        metadataList.add(sp);
        EasyMock.expect(MetadataManager.getMetadata(EasyMock.anyInt())).andReturn(metadataList).anyTimes();

        EasyMock.expect(UserManager.getAllUsers()).andReturn(Collections.emptyList()).anyTimes();

        PowerMock.mockStatic(Helper.class);
        EasyMock.expect(Helper.getTranslation(EasyMock.anyString())).andReturn("fixture").anyTimes();
        EasyMock.expect(Helper.getDateAsFormattedString(EasyMock.anyObject())).andReturn("date").anyTimes();
        EasyMock.expect(Helper.getMetadataLanguage()).andReturn("en").anyTimes();
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
    public void testStartExportToOutputStream() throws Exception {
        XsltPreparatorDocket exporter = new XsltPreparatorDocket();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        exporter.startExport(process, bos, null);

        assertTrue(bos.size() > 0);
        String xml = bos.toString("UTF-8");
        assertTrue(xml.contains(XsltPreparatorDocket.ELEMENT_PROCESS));
        assertTrue(xml.contains("testprocess"));
    }

    @Test
    public void testStartExportToOutputStreamWithIncludeImagesTrue() throws Exception {
        XsltPreparatorDocket exporter = new XsltPreparatorDocket();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        exporter.startExport(process, bos, null, true);

        assertTrue(bos.size() > 0);
        String xml = bos.toString("UTF-8");
        assertTrue(xml.contains(XsltPreparatorDocket.ELEMENT_PROCESS));
    }

    @Test
    public void testStartExportToOutputStreamWithIncludeImagesFalse() throws Exception {
        XsltPreparatorDocket exporter = new XsltPreparatorDocket();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        exporter.startExport(process, bos, null, false);

        assertTrue(bos.size() > 0);
        String xml = bos.toString("UTF-8");
        assertTrue(xml.contains(XsltPreparatorDocket.ELEMENT_PROCESS));
        assertTrue(xml.contains("testprocess"));
    }

    @Test
    public void testStartExportListWithoutBoolean() throws Exception {
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

    @Test
    public void testStartExportListWithIncludeImagesTrue() throws Exception {
        List<Process> processList = new ArrayList<>();
        processList.add(process);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        XsltPreparatorDocket exporter = new XsltPreparatorDocket();
        exporter.startExport(processList, bos, null, true);

        assertTrue(bos.size() > 0);
        String xml = bos.toString("UTF-8");
        assertTrue(xml.contains(XsltPreparatorDocket.ELEMENT_PROCESSES));
    }

    @Test
    public void testXmlTransformationWithNullFilename() throws Exception {
        XsltPreparatorDocket exporter = new XsltPreparatorDocket();
        Document doc = exporter.createDocument(process, true, false);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        exporter.xmlTransformation(bos, doc, null);

        assertTrue(bos.size() > 0);
        String xml = bos.toString("UTF-8");
        assertTrue(xml.contains(XsltPreparatorDocket.ELEMENT_PROCESS));
    }

    @Test
    public void testXmlTransformationWithNonEmptyFilename() throws Exception {
        XsltPreparatorDocket exporter = new XsltPreparatorDocket();
        Document doc = exporter.createDocument(process, true, false);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        // A non-empty filename means no XSLT transformation - doc is output as-is
        exporter.xmlTransformation(bos, doc, "somefile.xsl");

        assertTrue(bos.size() > 0);
    }

    @Test
    public void testStartTransformation() throws Exception {
        XsltPreparatorDocket exporter = new XsltPreparatorDocket();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        exporter.startTransformation(process, bos, null);

        assertTrue(bos.size() > 0);
        String xml = bos.toString("UTF-8");
        assertTrue(xml.contains(XsltPreparatorDocket.ELEMENT_PROCESS));
    }

    @Test
    public void testStartTransformationAlternativeSignature() throws Exception {
        XsltPreparatorDocket exporter = new XsltPreparatorDocket();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        exporter.startTransformation(bos, process, null);

        assertTrue(bos.size() > 0);
        String xml = bos.toString("UTF-8");
        assertTrue(xml.contains(XsltPreparatorDocket.ELEMENT_PROCESS));
    }

    @Test
    public void testGetProjectExportConfiguration() {
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

    @Test
    public void testGetMetsConfiguration() {
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

    @Test
    public void testCreateDocumentStructure() throws Exception {
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
