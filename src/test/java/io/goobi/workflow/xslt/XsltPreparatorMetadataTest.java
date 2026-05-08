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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.mock.MockProcess;
import de.sub.goobi.persistence.managers.MetadataManager;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
@ExtendWith(MockitoExtension.class)
public class XsltPreparatorMetadataTest extends AbstractTest {

    private static final Namespace XMLNS = Namespace.getNamespace("http://www.goobi.io/logfile");

    private Process process;

    private List<StringPair> metadataList;

    @BeforeEach
    public void setUp() throws Exception {
        process = MockProcess.createProcess();
        // Use a process ID with no existing meta.xml so readMetadataFile() fails safely
        process.setId(9999);
        process.setErstellungsdatum(new Date());

        metadataList = new ArrayList<>();
        StringPair sp = new StringPair("title", "value");
        metadataList.add(sp);



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

        EasyMock.replay(facesContext); EasyMock.replay(externalContext); EasyMock.replay(request); }

    @Test
    public void testConstructor() {
        try (MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class);
             MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
             MockedStatic<FacesContext> mockedFacesContext = Mockito.mockStatic(FacesContext.class);
             MockedStatic<ExternalContext> mockedExternalContext = Mockito.mockStatic(ExternalContext.class)) {
            mockedMetadataManager.when(() -> MetadataManager.getMetadata(Mockito.anyInt())).thenReturn(metadataList);
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("fixture");
            mockedHelper.when(() -> Helper.getMetadataLanguage()).thenReturn("en");
            mockedHelper.when(() -> Helper.getDateAsFormattedString(Mockito.any())).thenReturn("date");


            assertNotNull(new XsltPreparatorMetadata());
    
        }
}

    @Test
    public void testCreateDocumentWithNamespace() {
        try (MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class);
             MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
             MockedStatic<FacesContext> mockedFacesContext = Mockito.mockStatic(FacesContext.class);
             MockedStatic<ExternalContext> mockedExternalContext = Mockito.mockStatic(ExternalContext.class)) {
            mockedMetadataManager.when(() -> MetadataManager.getMetadata(Mockito.anyInt())).thenReturn(metadataList);
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("fixture");
            mockedHelper.when(() -> Helper.getMetadataLanguage()).thenReturn("en");
            mockedHelper.when(() -> Helper.getDateAsFormattedString(Mockito.any())).thenReturn("date");


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
}

    @Test
    public void testCreateDocumentWithoutNamespace() {
        try (MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class);
             MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
             MockedStatic<FacesContext> mockedFacesContext = Mockito.mockStatic(FacesContext.class);
             MockedStatic<ExternalContext> mockedExternalContext = Mockito.mockStatic(ExternalContext.class)) {
            mockedMetadataManager.when(() -> MetadataManager.getMetadata(Mockito.anyInt())).thenReturn(metadataList);
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("fixture");
            mockedHelper.when(() -> Helper.getMetadataLanguage()).thenReturn("en");
            mockedHelper.when(() -> Helper.getDateAsFormattedString(Mockito.any())).thenReturn("date");


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
}

    @Test
    public void testCreateDocumentContainsBasicProcessElements() {
        try (MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class);
             MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
             MockedStatic<FacesContext> mockedFacesContext = Mockito.mockStatic(FacesContext.class);
             MockedStatic<ExternalContext> mockedExternalContext = Mockito.mockStatic(ExternalContext.class)) {
            mockedMetadataManager.when(() -> MetadataManager.getMetadata(Mockito.anyInt())).thenReturn(metadataList);
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("fixture");
            mockedHelper.when(() -> Helper.getMetadataLanguage()).thenReturn("en");
            mockedHelper.when(() -> Helper.getDateAsFormattedString(Mockito.any())).thenReturn("date");


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
}

    @Test
    public void testStartExportEmptyList() throws Exception {
        try (MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class);
             MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
             MockedStatic<FacesContext> mockedFacesContext = Mockito.mockStatic(FacesContext.class);
             MockedStatic<ExternalContext> mockedExternalContext = Mockito.mockStatic(ExternalContext.class)) {
            mockedMetadataManager.when(() -> MetadataManager.getMetadata(Mockito.anyInt())).thenReturn(metadataList);
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("fixture");
            mockedHelper.when(() -> Helper.getMetadataLanguage()).thenReturn("en");
            mockedHelper.when(() -> Helper.getDateAsFormattedString(Mockito.any())).thenReturn("date");


            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            XsltPreparatorMetadata exporter = new XsltPreparatorMetadata();
            exporter.startExport(new ArrayList<>(), bos, null);

            assertTrue(bos.size() > 0);
            String xml = bos.toString("UTF-8");
            assertTrue(xml.contains(XsltPreparatorDocket.ELEMENT_PROCESSES));
    
        }
}

    @Test
    public void testStartExportList() throws Exception {
        try (MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class);
             MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
             MockedStatic<FacesContext> mockedFacesContext = Mockito.mockStatic(FacesContext.class);
             MockedStatic<ExternalContext> mockedExternalContext = Mockito.mockStatic(ExternalContext.class)) {
            mockedMetadataManager.when(() -> MetadataManager.getMetadata(Mockito.anyInt())).thenReturn(metadataList);
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("fixture");
            mockedHelper.when(() -> Helper.getMetadataLanguage()).thenReturn("en");
            mockedHelper.when(() -> Helper.getDateAsFormattedString(Mockito.any())).thenReturn("date");


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
}

    @Test
    public void testStartExportListWithMultipleProcesses() throws Exception {
        try (MockedStatic<MetadataManager> mockedMetadataManager = Mockito.mockStatic(MetadataManager.class);
             MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
             MockedStatic<FacesContext> mockedFacesContext = Mockito.mockStatic(FacesContext.class);
             MockedStatic<ExternalContext> mockedExternalContext = Mockito.mockStatic(ExternalContext.class)) {
            mockedMetadataManager.when(() -> MetadataManager.getMetadata(Mockito.anyInt())).thenReturn(metadataList);
            mockedHelper.when(() -> Helper.getTranslation(Mockito.anyString())).thenReturn("fixture");
            mockedHelper.when(() -> Helper.getMetadataLanguage()).thenReturn("en");
            mockedHelper.when(() -> Helper.getDateAsFormattedString(Mockito.any())).thenReturn("date");


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

}
