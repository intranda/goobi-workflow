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
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.config.ConfigProjectsTest;
import de.sub.goobi.config.ConfigurationHelper;

public class CopyFileTest extends AbstractTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() throws IOException, URISyntaxException {
        Path template = Paths.get(ConfigProjectsTest.class.getClassLoader().getResource(".").getFile());
        Path goobiFolder = Paths.get(template.getParent().getParent().toString() + "/src/test/resources/config/goobi_config.properties"); // for junit tests in eclipse
        if (!Files.exists(goobiFolder)) {
            goobiFolder = Paths.get("target/test-classes/config/goobi_config.properties"); // to run mvn test from cli or in jenkins
        }
        ConfigurationHelper.resetConfigurationFile();
        ConfigurationHelper.getInstance().setParameter("goobiFolder", goobiFolder.getParent().getParent().toString() + "/");
    }

    @Test
    public void testCopyFile() throws IOException {

        Path srcFile = Paths.get(ConfigurationHelper.getInstance().getConfigurationFolder() + "/plugin_JunitImportPluginError.xml");
        Path destFile = folder.newFile("destination").toPath();

        StorageProvider.getInstance().copyFile(srcFile, destFile);
        assertTrue(Files.exists(destFile));
    }

    @Test
    public void testCreateChecksum() throws IOException {
        Path srcFile = Paths.get(ConfigurationHelper.getInstance().getConfigurationFolder() + "plugin_JunitImportPluginError.xml");
        long checksum = StorageProvider.getInstance().createChecksum(srcFile);

        assertEquals(2827966374l, checksum);
    }

    @Test
    public void testStart() throws IOException {

        Path srcFile = Paths.get(ConfigurationHelper.getInstance().getConfigurationFolder() + "plugin_JunitImportPluginError.xml");
        Path destFile = folder.newFile("destination").toPath();

        StorageProvider.getInstance().start(srcFile, destFile);
        assertTrue(Files.exists(destFile));
    }

    @Test
    public void testCopyDirectory() throws IOException {
        Path srcDir = Paths.get(ConfigurationHelper.getInstance().getConfigurationFolder());
        Path dstDir = folder.newFolder("dest").toPath();

        StorageProvider.getInstance().copyDirectory(srcDir, dstDir);
        assertTrue(Files.exists(dstDir));
        assertTrue(Files.isDirectory(dstDir));
        assertTrue(!StorageProvider.getInstance().list(dstDir.toString()).isEmpty());
    }

}
