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
package de.sub.goobi.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;

import org.jdom2.Element;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.config.ConfigProjectsTest;
import de.sub.goobi.config.ConfigurationHelper;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.PartialViewContext;
import jakarta.faces.application.FacesMessage;

public class HelperTest extends AbstractTest {

    @TempDir
    private Path tempDir;

    private Path currentFolder;

    @BeforeEach
    public void setUp() throws IOException, URISyntaxException {
        Path template = Paths.get(ConfigProjectsTest.class.getClassLoader().getResource(".").getFile());
        Path goobiFolder = Paths.get(template.getParent().getParent().toString()
                + "/src/test/resources/config/goobi_config.properties"); // for junit tests in eclipse
        if (!Files.exists(goobiFolder)) {
            goobiFolder = Paths.get("target/test-classes/config/goobi_config.properties"); // to run mvn test from cli or in jenkins
        }
        ConfigurationHelper.resetConfigurationFile();
        ConfigurationHelper.getInstance().setParameter("goobiFolder", goobiFolder.getParent().getParent().toString() + "/");
        currentFolder = tempDir.resolve("temp");
        Files.createDirectories(currentFolder);
        Path tif = Paths.get(currentFolder.toString(), "00000001.tif");
        Files.createFile(tif);
    }

    @Test
    public void testGetDateAsFormattedString() {
        String value = Helper.getDateAsFormattedString(null);
        assertEquals("-", value);
        Date current = new Date();

        value = Helper.getDateAsFormattedString(current);
        assertNotNull(value);

    }

    @Test
    public void testDeleteInDir() {
        assertEquals(1, StorageProvider.getInstance().list(currentFolder.toString()).size());
        StorageProvider.getInstance().deleteInDir(currentFolder);
        assertEquals(0, StorageProvider.getInstance().list(currentFolder.toString()).size());
    }

    @Test
    public void testDeleteDataInDir() throws IOException {
        assertEquals(1, StorageProvider.getInstance().list(currentFolder.toString()).size());
        StorageProvider.getInstance().deleteDataInDir(currentFolder);
        assertEquals(0, StorageProvider.getInstance().list(currentFolder.toString()).size());

    }

    @Test
    public void testCopyDirectoryWithCrc32Check() throws IOException {
        Path dest = tempDir.resolve("dest");
        Files.createDirectories(dest);
        Element element = new Element("test");
        Helper.copyDirectoryWithCrc32Check(currentFolder, dest, 10, element);
        assertTrue(Files.exists(dest));
        assertTrue(!element.getChildren().isEmpty());
    }

    private FacesContext prepareMockedFacesContext(MockedStatic<FacesContext> mockedStatic) {
        FacesContext facesContext = Mockito.mock(FacesContext.class);
        jakarta.faces.context.ExternalContext externalContext = Mockito.mock(jakarta.faces.context.ExternalContext.class);
        PartialViewContext pvc = Mockito.mock(PartialViewContext.class);

        Mockito.when(facesContext.getExternalContext()).thenReturn(externalContext);
        Mockito.when(externalContext.getContext()).thenReturn(null);
        Mockito.when(facesContext.getPartialViewContext()).thenReturn(pvc);
        Mockito.when(pvc.getRenderIds()).thenReturn(new ArrayList<>());

        FacesContextHelper.setFacesContext(facesContext);
        mockedStatic.when(FacesContext::getCurrentInstance).thenReturn(facesContext);
        return facesContext;
    }

    @Test
    public void testSetFehlerMeldungUntranslatedBlocksHtmlEntityInjection() {
        try (MockedStatic<FacesContext> mockedStatic = Mockito.mockStatic(FacesContext.class)) {
            FacesContext facesContext = prepareMockedFacesContext(mockedStatic);
            ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);

            // Attacker sends HTML entities as the message title (summary) – reconstructs script tag with escape=false
            Helper.setFehlerMeldungUntranslated("&lt;script&gt;alert(1)&lt;/script&gt;");

            Mockito.verify(facesContext).addMessage(Mockito.any(), captor.capture());
            String summary = captor.getValue().getSummary();

            // Simulate what the browser does when rendering with escape=false
            String rendered = summary.replace("&lt;", "<").replace("&gt;", ">").replace("&amp;", "&");
            assertFalse(rendered.contains("<script>"),
                    "HTML entity injection must not reconstruct a script tag after browser rendering");
        }
    }

    @Test
    public void testSetFehlerMeldungUntranslatedPreservesLineBreaks() {
        try (MockedStatic<FacesContext> mockedStatic = Mockito.mockStatic(FacesContext.class)) {
            FacesContext facesContext = prepareMockedFacesContext(mockedStatic);
            ArgumentCaptor<FacesMessage> captor = ArgumentCaptor.forClass(FacesMessage.class);

            Helper.setFehlerMeldungUntranslated("line1\nline2");

            Mockito.verify(facesContext).addMessage(Mockito.any(), captor.capture());
            String summary = captor.getValue().getSummary();
            assertTrue(summary.contains("<br"), "Newlines in messages must be rendered as HTML line breaks");
        }
    }
}
