package org.goobi.vocabulary.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
        Path testFile = Paths.get(template.getParent().getParent().getParent().toString() + "/test/resources/" + "vocabulary.json");

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
