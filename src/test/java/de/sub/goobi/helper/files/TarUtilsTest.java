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

package de.sub.goobi.helper.files;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.config.ConfigProjectsTest;
import de.sub.goobi.helper.StorageProvider;

public class TarUtilsTest extends AbstractTest {

    @TempDir
    Path tempDir;

    private Path sourceFolder;
    private Path destinationFolder;

    @BeforeEach
    public void setUp() throws IOException {
        Path template = Paths.get(ConfigProjectsTest.class.getClassLoader().getResource(".").getFile());
        // for junit tests in eclipse
        Path goobiFolder = Paths.get(template.getParent().getParent().toString() + "/src/test/resources/config/goobi_config.properties");
        if (!Files.exists(goobiFolder)) {
            goobiFolder = Paths.get("target/test-classes/config/goobi_config.properties"); // to run mvn test from cli or in jenkins
        }
        Path resourceFolder = goobiFolder.getParent().getParent();

        // create directories
        sourceFolder = tempDir.resolve("source");
        Files.createDirectories(sourceFolder);
        destinationFolder = tempDir.resolve("destination");
        Files.createDirectories(destinationFolder);
        // copy some sample files to source folder
        Path samplexml = Paths.get(resourceFolder.toString(), "metadata/1/meta.xml");
        Path sampleImage = Paths.get(resourceFolder.toString(), "file_example_TIFF_1MB.tif");

        Files.copy(samplexml, Paths.get(sourceFolder.toString(), "meta.xml"));

        Path sub = Paths.get(sourceFolder.toString(), "subfolder");
        StorageProvider.getInstance().createDirectories(sub);
        Files.copy(sampleImage, Paths.get(sub.toString(), "image.tif"));
    }

    @Test
    public void testTarFile() {

        // create zip
        Path tarFile = Paths.get(destinationFolder.toString(), "sample.tar");
        assertFalse(Files.exists(tarFile));

        TarUtils.createTar(sourceFolder, tarFile);
        assertTrue(Files.exists(tarFile));

        // extract zip
        Path extractedMetadata = Paths.get(destinationFolder.toString(), "meta.xml");
        Path extractedImage = Paths.get(destinationFolder.toString(), "subfolder", "image.tif");
        assertFalse(Files.exists(extractedMetadata));
        assertFalse(Files.exists(extractedImage));

        TarUtils.extractTar(destinationFolder, tarFile);

        assertTrue(Files.exists(extractedMetadata));
        assertTrue(Files.exists(extractedImage));
    }


    @Test
    public void testTarGZFile() {

        // create zip
        Path tarFile = Paths.get(destinationFolder.toString(), "sample.tar.gz");
        assertFalse(Files.exists(tarFile));

        TarUtils.createTarGz(sourceFolder, tarFile);
        assertTrue(Files.exists(tarFile));

        // extract zip
        Path extractedMetadata = Paths.get(destinationFolder.toString(), "meta.xml");
        Path extractedImage = Paths.get(destinationFolder.toString(), "subfolder", "image.tif");
        assertFalse(Files.exists(extractedMetadata));
        assertFalse(Files.exists(extractedImage));

        TarUtils.extractTarGz(destinationFolder, tarFile);

        assertTrue(Files.exists(extractedMetadata));
        assertTrue(Files.exists(extractedImage));
    }
}
