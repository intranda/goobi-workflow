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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.easymock.EasyMock;
import org.goobi.beans.Process;
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
import de.sub.goobi.mock.MockProcess;
import de.sub.goobi.persistence.managers.MetadataManager;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpServletRequest;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Helper.class, MetadataManager.class, FacesContext.class, ExternalContext.class })
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*" })
public class XsltPreparatorMetadataTest extends AbstractTest {

    private static final Namespace XMLNS = Namespace.getNamespace("http://www.goobi.io/logfile");

    private Process process;

    @Before
    public void setUp() throws Exception {
        process = MockProcess.createProcess();
        // Use a process ID with no existing meta.xml so readMetadataFile() fails safely
        process.setId(9999);
        process.setErstellungsdatum(new Date());

        List<StringPair> metadataList = new ArrayList<>();
        StringPair sp = new StringPair("title", "value");
        metadataList.add(sp);

        PowerMock.mockStatic(MetadataManager.class);
        EasyMock.expect(MetadataManager.getMetadata(EasyMock.anyInt())).andReturn(metadataList).anyTimes();

        PowerMock.mockStatic(Helper.class);
        EasyMock.expect(Helper.getTranslation(EasyMock.anyString())).andReturn("fixture").anyTimes();
        EasyMock.expect(Helper.getMetadataLanguage()).andReturn("en").anyTimes();
        EasyMock.expect(Helper.getDateAsFormattedString(EasyMock.anyObject())).andReturn("date").anyTimes();

        PowerMock.mockStatic(FacesContext.class);
        PowerMock.mockStatic(ExternalContext.class);
        PowerMock.replayAll();
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
        assertNotNull(new XsltPreparatorMetadata());
    }

    @Test
    public void testCreateDocumentWithNamespace() {
        XsltPreparatorMetadata exporter = new XsltPreparatorMetadata();
        Document doc = exporter.createDocument(process, true);

        assertNotNull(doc);
        Element root = doc.getRootElement();
        assertNotNull(root);
        assertEquals(XsltPreparatorDocket.ELEMENT_PROCESS, root.getName());
        assertEquals("9999", root.getAttributeValue(XsltPreparatorDocket.ATTRIBUTE_PROCESS_ID));

        // Namespace and schema location attribute should be present
        assertNotNull(root.getAttribute(XsltPreparatorDocket.ATTRIBUTE_SCHEMA_LOCATION,
                Namespace.getNamespace(XsltPreparatorDocket.NAMESPACE_XSI, XsltPreparatorDocket.FILE_SCHEMA)));
    }

    @Test
    public void testCreateDocumentWithoutNamespace() {
        XsltPreparatorMetadata exporter = new XsltPreparatorMetadata();
        Document doc = exporter.createDocument(process, false);

        assertNotNull(doc);
        Element root = doc.getRootElement();
        assertEquals(XsltPreparatorDocket.ELEMENT_PROCESS, root.getName());
        assertEquals("9999", root.getAttributeValue(XsltPreparatorDocket.ATTRIBUTE_PROCESS_ID));

        // No schema location attribute when addNamespace is false
        assertNull(root.getAttribute(XsltPreparatorDocket.ATTRIBUTE_SCHEMA_LOCATION,
                Namespace.getNamespace(XsltPreparatorDocket.NAMESPACE_XSI, XsltPreparatorDocket.FILE_SCHEMA)));
    }

    @Test
    public void testCreateDocumentContainsBasicProcessElements() {
        XsltPreparatorMetadata exporter = new XsltPreparatorMetadata();
        Document doc = exporter.createDocument(process, true);

        Element root = doc.getRootElement();

        Element title = root.getChild(XsltPreparatorDocket.ELEMENT_TITLE, XMLNS);
        assertNotNull(title);
        assertEquals("testprocess", title.getValue());

        Element project = root.getChild(XsltPreparatorDocket.ELEMENT_PROJECT, XMLNS);
        assertNotNull(project);
        assertEquals("project", project.getValue());

        Element ruleset = root.getChild(XsltPreparatorDocket.ELEMENT_RULESET, XMLNS);
        assertNotNull(ruleset);
        assertEquals("ruleset.xml", ruleset.getValue());
    }

    @Test
    public void testStartExportEmptyList() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        XsltPreparatorMetadata exporter = new XsltPreparatorMetadata();
        exporter.startExport(new ArrayList<>(), bos, null);

        assertTrue(bos.size() > 0);
        String xml = bos.toString("UTF-8");
        assertTrue(xml.contains(XsltPreparatorDocket.ELEMENT_PROCESSES));
    }

    @Test
    public void testStartExportList() throws Exception {
        List<Process> processList = new ArrayList<>();
        processList.add(process);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        XsltPreparatorMetadata exporter = new XsltPreparatorMetadata();
        exporter.startExport(processList, bos, null);

        assertTrue(bos.size() > 0);
        String xml = bos.toString("UTF-8");
        assertTrue(xml.contains(XsltPreparatorDocket.ELEMENT_PROCESSES));
        assertTrue(xml.contains(XsltPreparatorDocket.ELEMENT_PROCESS));
        assertTrue(xml.contains("testprocess"));
    }

    @Test
    public void testStartExportListWithMultipleProcesses() throws Exception {
        List<Process> processList = new ArrayList<>();
        processList.add(process);

        Process process2 = MockProcess.createProcess();
        process2.setId(9998);
        process2.setTitel("testprocess2");
        processList.add(process2);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        XsltPreparatorMetadata exporter = new XsltPreparatorMetadata();
        exporter.startExport(processList, bos, null);

        String xml = bos.toString("UTF-8");
        assertTrue(xml.contains("testprocess"));
        assertTrue(xml.contains("testprocess2"));
    }

}
