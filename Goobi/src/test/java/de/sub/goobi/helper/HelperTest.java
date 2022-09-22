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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import org.jdom2.Element;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.config.ConfigProjectsTest;
import de.sub.goobi.config.ConfigurationHelper;

public class HelperTest extends AbstractTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private Path currentFolder;

    @Before
    public void setUp() throws IOException, URISyntaxException {
        Path template = Paths.get(ConfigProjectsTest.class.getClassLoader().getResource(".").getFile());
        Path goobiFolder = Paths.get(template.getParent().getParent().toString() + "/src/test/resources/config/goobi_config.properties"); // for junit tests in eclipse
        if (!Files.exists(goobiFolder)) {
            goobiFolder = Paths.get("target/test-classes/config/goobi_config.properties"); // to run mvn test from cli or in jenkins
        }
        ConfigurationHelper.resetConfigurationFile();
        ConfigurationHelper.getInstance().setParameter("goobiFolder", goobiFolder.getParent().getParent().toString() + "/");
        currentFolder = temporaryFolder.newFolder("temp").toPath();
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
        Path dest = temporaryFolder.newFolder("dest").toPath();
        Element element = new Element("test");
        Helper.copyDirectoryWithCrc32Check(currentFolder, dest, 10, element);
        assertTrue(Files.exists(dest));
        assertTrue(!element.getChildren().isEmpty());
    }
}
