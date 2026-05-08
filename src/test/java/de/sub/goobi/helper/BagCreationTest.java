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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.config.ConfigurationHelper;

@ExtendWith(MockitoExtension.class)
public class BagCreationTest extends AbstractTest {

    @TempDir
    private Path tempDir;

    private String bagitFolder;

    private ConfigurationHelper configurationHelper;

    @BeforeEach
    public void setUp() throws Exception {
        bagitFolder = tempDir.toAbsolutePath().toString();
        configurationHelper = Mockito.mock(ConfigurationHelper.class);
        Mockito.lenient().when(configurationHelper.getTemporaryFolder()).thenReturn(bagitFolder);
        Mockito.lenient().when(configurationHelper.useS3()).thenReturn(false);
    }

    @Test
    public void testConstructor() {
        try (MockedStatic<ConfigurationHelper> mockedConfigurationHelper = Mockito.mockStatic(ConfigurationHelper.class)) {
            mockedConfigurationHelper.when(() -> ConfigurationHelper.getInstance()).thenReturn(configurationHelper);

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
    }

    @Test
    public void testCreateIEFolder() {
        try (MockedStatic<ConfigurationHelper> mockedConfigurationHelper = Mockito.mockStatic(ConfigurationHelper.class)) {
            mockedConfigurationHelper.when(() -> ConfigurationHelper.getInstance()).thenReturn(configurationHelper);

            BagCreation creation = new BagCreation(bagitFolder + "/fixture");
            assertEquals("fixture", creation.getBagitRoot().getFileName().toString());
            creation.createIEFolder("subfolder", "objects");
            assertEquals("subfolder", creation.getIeFolder().getFileName().toString());
            assertEquals("metadata", creation.getMetadataFolder().getFileName().toString());
            assertEquals("objects", creation.getObjectsFolder().getFileName().toString());

        }
    }

    @Test
    public void testMetadata() {
        try (MockedStatic<ConfigurationHelper> mockedConfigurationHelper = Mockito.mockStatic(ConfigurationHelper.class)) {
            mockedConfigurationHelper.when(() -> ConfigurationHelper.getInstance()).thenReturn(configurationHelper);

            BagCreation creation = new BagCreation(bagitFolder + "/fixture");
            assertTrue(creation.getMetadata().isEmpty());
            creation.addMetadata("key", "value");
            assertFalse(creation.getMetadata().isEmpty());

        }
    }

    @Test
    public void testCreateBag() throws IOException {
        try (MockedStatic<ConfigurationHelper> mockedConfigurationHelper = Mockito.mockStatic(ConfigurationHelper.class)) {
            mockedConfigurationHelper.when(() -> ConfigurationHelper.getInstance()).thenReturn(configurationHelper);

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
}
