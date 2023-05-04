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


package de.sub.goobi.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.easymock.EasyMock;
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

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ConfigurationHelper.class })
@PowerMockIgnore({ "javax.management.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.net.ssl.*", "jdk.internal.reflect.*" })
public class BagCreationTest extends AbstractTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private String bagitFolder;

    @Before
    public void setUp() throws Exception {
        Path template = Paths.get(ConfigProjectsTest.class.getClassLoader().getResource(".").getFile());
        // for junit tests in eclipse
        Path goobiFolder = Paths.get(template.getParent().getParent().toString() + "/src/test/resources/config/goobi_config.properties");
        if (!Files.exists(goobiFolder)) {
            goobiFolder = Paths.get("target/test-classes/config/goobi_config.properties"); // to run mvn test from cli or in jenkins
        }

        File tempdir = folder.newFolder("tmp");
        tempdir.mkdirs();
        bagitFolder = tempdir.getAbsolutePath();
        PowerMock.mockStatic(ConfigurationHelper.class);
        ConfigurationHelper configurationHelper = EasyMock.createMock(ConfigurationHelper.class);
        EasyMock.expect(ConfigurationHelper.getInstance()).andReturn(configurationHelper).anyTimes();
        EasyMock.expect(configurationHelper.getTemporaryFolder()).andReturn(tempdir.getAbsolutePath()).anyTimes();
        EasyMock.expect(configurationHelper.useS3()).andReturn(false).anyTimes();
        EasyMock.replay(configurationHelper);
        PowerMock.replay(ConfigurationHelper.class);
    }

    @Test
    public void testConstructor() {
        // use a root name
        BagCreation creation = new BagCreation(bagitFolder + "/fixture");
        assertEquals("fixture", creation.getBagitRoot().getFileName().toString());

        // blank root name, create a uuid
        creation = new BagCreation("");
        assertEquals(36, creation.getBagitRoot().getFileName().toString().length());

        // null as root name, create a uuid
        creation = new BagCreation(null);
        assertEquals(36, creation.getBagitRoot().getFileName().toString().length());
    }

    @Test
    public void testCreateIEFolder() {
        BagCreation creation = new BagCreation(bagitFolder + "/fixture");
        assertEquals("fixture", creation.getBagitRoot().getFileName().toString());
        creation.createIEFolder("subfolder", "objects");
        assertEquals("subfolder", creation.getIeFolder().getFileName().toString());
        assertEquals("metadata", creation.getMetadataFolder().getFileName().toString());
        assertEquals("objects", creation.getObjectsFolder().getFileName().toString());
    }

    @Test
    public void testMetadata() {
        BagCreation creation = new BagCreation(bagitFolder + "/fixture");
        assertTrue(creation.getMetadata().isEmpty());
        creation.addMetadata("key", "value");
        assertFalse(creation.getMetadata().isEmpty());
    }

    @Test
    public void testCreateBag() throws IOException {
        // folder preparation
        BagCreation creation = new BagCreation(bagitFolder + "/fixture");
        creation.createIEFolder("subfolder", "objects");
        // metadata prepararation
        creation.addMetadata("Source-Organization", "example library");
        creation.addMetadata("Contact-Email", "user@example.org");
        creation.addMetadata("External-Description", "Main title");
        creation.addMetadata("External-Identifier", "opac id");
        creation.addMetadata("Internal-Sender-Identifier", "goobi id");
        creation.addMetadata("Internal-Sender-Description", "Main title");

        // add some files
        Path p = Paths.get("src/test/resources/metadata/1/meta.xml");
        if (!Files.exists(p)) {
            p = Paths.get("target/test-classes/metadata/1/meta.xml");
        }
        Files.copy(p, Paths.get(creation.getMetadataFolder().toString(), "mets.xml"));

        p = Paths.get("src/test/resources/file_example_TIFF_1MB.tif");
        if (!Files.exists(p)) {
            p = Paths.get("target/test-classes/file_example_TIFF_1MB.tif");
        }
        Files.copy(p, Paths.get(creation.getObjectsFolder().toString(), "example.tif"));

        Path bagInfo = Paths.get(creation.getBagitRoot().toString(), "bag-info.txt");
        Path bagit = Paths.get(creation.getBagitRoot().toString(), "bagit.txt");
        Path manifest = Paths.get(creation.getBagitRoot().toString(), "manifest-sha256.txt");
        Path tagmanifest = Paths.get(creation.getBagitRoot().toString(), "tagmanifest-sha256.txt");

        // create bagit data
        creation.createBag();

        // this creates 4 files
        assertTrue(Files.exists(bagInfo));
        assertTrue(Files.exists(bagit));
        assertTrue(Files.exists(manifest));
        assertTrue(Files.exists(tagmanifest));

    }
}
