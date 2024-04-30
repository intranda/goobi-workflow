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

package org.goobi.api.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.config.ConfigProjectsTest;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.ProjectManager;
import de.sub.goobi.persistence.managers.StepManager;
import ugh.dl.DocStruct;
import ugh.dl.Fileformat;
import ugh.dl.Prefs;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ProcessManager.class, ProjectManager.class, ProcessService.class, StepManager.class })
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*" })
public class JsonImportParserTest extends AbstractTest {

    private Path rulesetFile;

    private Prefs prefs;

    @Before
    public void setUp() throws Exception {
        Path template = Paths.get(ConfigProjectsTest.class.getClassLoader().getResource(".").getFile());
        // for junit tests in eclipse
        Path samplefiles = Paths.get(template.getParent().getParent().toString() + "/src/test/resources/samplefiles/");
        if (!Files.exists(samplefiles)) {
            samplefiles = Paths.get("target/test-classes/samplefiles/"); // to run mvn test from cli or in jenkins
        }

        rulesetFile = Paths.get(samplefiles.toString(), "ruleset.xml");
        prefs = new Prefs();
        prefs.loadPrefs(rulesetFile.toString());

    }

    @Test
    public void testAuthenticationMethods() {
        JsonImportParser fixture = new JsonImportParser();
        List<AuthenticationMethodDescription> desc = fixture.getAuthenticationMethods();
        assertTrue(desc.isEmpty());
    }

    @Test
    public void testReadMetadataFile() throws Exception {
        JsonImportParser fixture = new JsonImportParser();

        String jsonString = "{\"title\": \"Main title\", \"id\": 123}";

        InputStream stream = IOUtils.toInputStream(jsonString, StandardCharsets.UTF_8);

        Fileformat ff = fixture.readMetadataFile(stream, prefs);
        assertNotNull(ff);
        DocStruct logical = ff.getDigitalDocument().getLogicalDocStruct();
        assertEquals("Monograph", logical.getType().getName());

        assertEquals(3, logical.getAllMetadata().size());

        assertEquals("TitleDocMain", logical.getAllMetadata().get(0).getType().getName());
        assertEquals("Main title", logical.getAllMetadata().get(0).getValue());

        assertEquals("CatalogIDDigital", logical.getAllMetadata().get(1).getType().getName());
        assertEquals("123", logical.getAllMetadata().get(1).getValue());

        assertEquals("singleDigCollection", logical.getAllMetadata().get(2).getType().getName());
        assertEquals("abc", logical.getAllMetadata().get(2).getValue());
    }

}
