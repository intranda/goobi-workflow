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
package io.goobi.workflow.harvester.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.jdom2.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.config.ConfigProjectsTest;
import de.sub.goobi.persistence.managers.HarvesterRepositoryManager;
import io.goobi.workflow.api.connection.HttpUtils;

@ExtendWith(MockitoExtension.class)
public class IaToolsTest extends AbstractTest {

    @TempDir
    private Path tempFolder;

    private Path testFile;

    private String response;

    @BeforeEach
    public void setUp() throws Exception {
        Path template = Paths.get(ConfigProjectsTest.class.getClassLoader().getResource(".").getFile());
        // for junit tests in eclipse
        Path samplefiles = Paths.get(template.getParent().getParent().toString() + "/src/test/resources/samplefiles/");
        if (!Files.exists(samplefiles)) {
            samplefiles = Paths.get("target/test-classes/samplefiles/"); // to run mvn test from cli or in jenkins
        }

        Files.createDirectories(template);

        Path marcFile = Paths.get(samplefiles.toString(), "marc_monograph.xml");
        testFile = Paths.get(tempFolder.toString(), "b20411595_marc.xml");
        Files.copy(marcFile, testFile);

        response = "<root><result numFound=\"1\"><doc><field name=\"identifier\">1234</field><field name=\"publicdate\">1234</field>"
                + "<field name=\"title\">1234</field><field name=\"creator\">1234</field></doc></result></root>";

    }

    @Test
    public void testOutputDirName() {
        try (MockedStatic<HttpUtils> mockedHttpUtils = Mockito.mockStatic(HttpUtils.class);
                MockedStatic<HarvesterRepositoryManager> mockedHarvesterRepositoryManager = Mockito.mockStatic(HarvesterRepositoryManager.class)) {
            mockedHttpUtils.when(() -> HttpUtils.getStreamFromUrl(Mockito.any(), Mockito.any())).thenReturn(null);
            mockedHttpUtils.when(() -> HttpUtils.getStringFromUrl(Mockito.anyString())).thenReturn(response);
            mockedHarvesterRepositoryManager.when(() -> HarvesterRepositoryManager.addRecords(Mockito.any(), Mockito.anyBoolean())).thenReturn(1);

            // 1.) no underscore
            String identifier = "123456";
            assertEquals("monograph", IaTools.getOutputDirName(identifier));

            // 2.) underscore, followed by digits
            identifier = "123456_1234";
            assertEquals("multivolume", IaTools.getOutputDirName(identifier));

            // 3.) ends with underscore
            identifier = "123456_";
            assertEquals("monograph", IaTools.getOutputDirName(identifier));

            // 4.) underscore, non digits
            identifier = "123456_aaa";
            assertEquals("monograph", IaTools.getOutputDirName(identifier));

        }
    }

    @Test
    public void testDownloadFile() throws Exception {
        try (MockedStatic<HttpUtils> mockedHttpUtils = Mockito.mockStatic(HttpUtils.class);
                MockedStatic<HarvesterRepositoryManager> mockedHarvesterRepositoryManager = Mockito.mockStatic(HarvesterRepositoryManager.class)) {
            mockedHttpUtils.when(() -> HttpUtils.getStreamFromUrl(Mockito.any(), Mockito.any())).thenReturn(null);
            mockedHttpUtils.when(() -> HttpUtils.getStringFromUrl(Mockito.anyString())).thenReturn(response);
            mockedHarvesterRepositoryManager.when(() -> HarvesterRepositoryManager.addRecords(Mockito.any(), Mockito.anyBoolean())).thenReturn(1);

            Path fixture = IaTools.downloadFile("url", tempFolder.toString(), testFile.getFileName().toString());
            assertEquals("b20411595_marc.xml", fixture.getFileName().toString());

        }
    }

    @Test
    public void testQuerySolrToJsonResult() throws Exception {
        try (MockedStatic<HttpUtils> mockedHttpUtils = Mockito.mockStatic(HttpUtils.class);
                MockedStatic<HarvesterRepositoryManager> mockedHarvesterRepositoryManager = Mockito.mockStatic(HarvesterRepositoryManager.class)) {
            mockedHttpUtils.when(() -> HttpUtils.getStreamFromUrl(Mockito.any(), Mockito.any())).thenReturn(null);
            mockedHttpUtils.when(() -> HttpUtils.getStringFromUrl(Mockito.anyString())).thenReturn(response);
            mockedHarvesterRepositoryManager.when(() -> HarvesterRepositoryManager.addRecords(Mockito.any(), Mockito.anyBoolean())).thenReturn(1);

            Document doc = IaTools.querySolrToJsonResult("url");
            assertNotNull(doc);

        }
    }

    @Test
    public void testQuerySolrToDB() throws Exception {
        try (MockedStatic<HttpUtils> mockedHttpUtils = Mockito.mockStatic(HttpUtils.class);
                MockedStatic<HarvesterRepositoryManager> mockedHarvesterRepositoryManager = Mockito.mockStatic(HarvesterRepositoryManager.class)) {
            mockedHttpUtils.when(() -> HttpUtils.getStreamFromUrl(Mockito.any(), Mockito.any())).thenReturn(null);
            mockedHttpUtils.when(() -> HttpUtils.getStringFromUrl(Mockito.anyString())).thenReturn(response);
            mockedHarvesterRepositoryManager.when(() -> HarvesterRepositoryManager.addRecords(Mockito.any(), Mockito.anyBoolean())).thenReturn(1);

            //querySolrToDB(String query, Integer jobId, Integer repositoryId) {
            assertEquals(1, IaTools.querySolrToDB("url", 1, 1));

        }
    }

}
