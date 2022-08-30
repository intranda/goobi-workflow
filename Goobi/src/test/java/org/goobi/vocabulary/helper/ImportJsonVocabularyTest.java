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
package org.goobi.vocabulary.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.goobi.vocabulary.VocabRecord;
import org.goobi.vocabulary.Vocabulary;
import org.junit.Test;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.config.ConfigProjectsTest;

public class ImportJsonVocabularyTest extends AbstractTest {

    @Test
    public void testConvertVocabulary() {

        // find existing vocabulary with this title or create new one

        Vocabulary vocab = new Vocabulary();
        vocab.setTitle("");
        vocab.setDescription("");

        Path template = Paths.get(ConfigProjectsTest.class.getClassLoader().getResource(".").getFile());

        Path testFile = Paths.get(template.getParent().getParent().toString() + "/src/test/resources/vocabulary.json"); // for junit tests in eclipse
        if (!Files.exists(testFile)) {
            testFile = Paths.get("target/test-classes/vocabulary.json"); // to run mvn test from cli or in jenkins
        }

        ImportJsonVocabulary.convertJsonVocabulary(vocab, testFile);
        assertNotNull(vocab);
        assertEquals(3, vocab.getRecords().size());
        VocabRecord rec = vocab.getRecords().get(2);
        assertEquals("Title", rec.getFields().get(0).getLabel());
        assertEquals("25 auf das Gesäß", rec.getFields().get(0).getValue());

        assertEquals("Keywords", rec.getFields().get(1).getLabel());
        assertEquals("25 auf das Gesäß", rec.getFields().get(1).getValue());

        assertEquals("Description", rec.getFields().get(2).getLabel());
        assertEquals("<p>(lit. 25 on the backside)</p>", rec.getFields().get(2).getValue());

        assertEquals("Source", rec.getFields().get(3).getLabel());
        assertEquals("", rec.getFields().get(3).getValue());

    }

}
