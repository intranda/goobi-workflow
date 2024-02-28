package de.sub.goobi.helper;

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
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.goobi.beans.Process;
import org.junit.Before;
import org.junit.Test;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.mock.MockProcess;
import ugh.dl.DigitalDocument;
import ugh.dl.Prefs;

public class VariableReplacerTest extends AbstractTest {

    private Prefs prefs;
    private Process process;

    private DigitalDocument digitalDocument;

    @Before
    public void setUp() throws Exception {
        process = MockProcess.createProcess();
        prefs = process.getRegelsatz().getPreferences();
        digitalDocument = process.readMetadataFile().getDigitalDocument();
    }

    @Test
    public void testConstructor() {
        VariableReplacer replacer = new VariableReplacer(digitalDocument, prefs, process, null);
        assertNotNull(replacer);
    }

    @Test
    public void testSimpleReplacer() {
        // empty string
        assertEquals("", VariableReplacer.simpleReplace("", process));

        // nothing to replace
        assertEquals("fixture", VariableReplacer.simpleReplace("fixture", process));

        // replace process title
        assertEquals("testprocess", VariableReplacer.simpleReplace("{processtitle}", process));

        // replace process id
        assertEquals("1", VariableReplacer.simpleReplace("{processid}", process));

        // project id
        assertEquals("666", VariableReplacer.simpleReplace("{projectid}", process));

        // project name
        assertEquals("project", VariableReplacer.simpleReplace("{projectname}", process));

        // project identifier
        assertEquals("identifier", VariableReplacer.simpleReplace("{projectidentifier}", process));
    }

    @Test
    public void testReplace() {
        VariableReplacer replacer = new VariableReplacer(digitalDocument, prefs, process, null);
        // null
        assertEquals("", replacer.replace(null));
        // empty string
        assertEquals("", replacer.replace(""));

        // metadata
        assertEquals("main title", replacer.replace("{meta.topstruct.TitleDocMain}"));
        assertEquals("main title", replacer.replace("{meta.TitleDocMain}"));
        assertEquals("main title", replacer.replace("{metas.topstruct.TitleDocMain}"));
        assertEquals("main title", replacer.replace("{metas.TitleDocMain}"));

        // missing metadata
        assertEquals("", replacer.replace("{meta.abc}"));

        // path and folder replacement
        assertEquals("1/images/testprocess_media/", replacer.replace("{s3_tifpath}"));
        assertEquals("1/images/testprocess_master/", replacer.replace("{s3_origpath}"));
        assertEquals("1/images/", replacer.replace("{s3_imagepath}"));
        assertEquals("1/", replacer.replace("{s3_processpath}"));
        assertEquals("1/import/", replacer.replace("{s3_importpath}"));

        assertEquals("1/images/testprocess_source/", replacer.replace("{s3_sourcepath}"));
        assertEquals("1/ocr/", replacer.replace("{s3_ocrbasispath}"));
        assertEquals("1/ocr/testprocess_txt/", replacer.replace("{s3_ocrplaintextpath}"));

        assertTrue(replacer.replace("{tifpath}").endsWith("metadata/1/images/testprocess_media"));
        assertTrue(replacer.replace("{origpath}").endsWith("metadata/1/images/testprocess_master"));
        assertTrue(replacer.replace("{imagepath}").endsWith("metadata/1/images"));
        assertTrue(replacer.replace("{processpath}").endsWith("metadata/1"));
        assertTrue(replacer.replace("{importpath}").endsWith("metadata/1/import"));
        assertTrue(replacer.replace("{sourcepath}").endsWith("metadata/1/images/testprocess_source"));
        assertTrue(replacer.replace("{ocrbasispath}").endsWith("metadata/1/ocr"));
        assertTrue(replacer.replace("{ocrplaintextpath}").endsWith("metadata/1/ocr/testprocess_txt"));

        assertTrue(replacer.replace("{iiifMediaFolder}").contains("api/process/image/1/testprocess_media/00000001.tif"));
        assertTrue(replacer.replace("{iiifMasterFolder}").contains("api/process/image/1/testprocess_master/00000001.tif"));

    }

    //    @Test
    public void testReplaceProperties() {
        // TODO
        // {product.NAME}
        // {template.NAME}
        // {process.NAME}
        // {folder.name}
    }

    @Test
    public void testCombineValues() {
        VariableReplacer replacer = new VariableReplacer(digitalDocument, prefs, process, null);
        assertTrue(replacer.replace("{processpath}/thumbs/{processtitle}_media_1920").endsWith("metadata/1/thumbs/testprocess_media_1920"));
    }

    //  @Test
    public void testReplaceBashScript() {

    }

}
