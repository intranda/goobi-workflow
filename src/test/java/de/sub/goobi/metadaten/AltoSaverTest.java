package de.sub.goobi.metadaten;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.goobi.beans.AltoChange;
import org.goobi.beans.NamedEntity;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.config.ConfigProjectsTest;
import de.sub.goobi.helper.XmlTools;

public class AltoSaverTest extends AbstractTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private Path sampleAltoFile;

    Namespace namespace = Namespace.getNamespace("alto", "http://www.loc.gov/standards/alto/ns-v2#");

    @Before
    public void setUp() throws Exception {
        // copy sample file
        Path template = Paths.get(ConfigProjectsTest.class.getClassLoader().getResource(".").getFile());
        // for junit tests in eclipse
        Path altoFile = Paths.get(template.getParent().getParent().toString() + "/src/test/resources/samplefiles/alto.xml");
        if (!Files.exists(altoFile)) {
            altoFile = Paths.get("target/test-classes/samplefiles/alto.xml"); // to run mvn test from cli or in jenkins
        }

        sampleAltoFile = Paths.get(folder.newFolder().toString(), "alto.xml");

        Files.copy(altoFile, sampleAltoFile);
    }

    @Test
    public void testGetWordById() throws Exception {
        Document doc = XmlTools.getSAXBuilder().build(sampleAltoFile.toFile());
        Element element = AltoSaver.getWordById(doc, namespace, "Textword_1");
        assertEquals("WIE", element.getAttributeValue("CONTENT"));
    }

    @Test
    public void testSaveAltoChanges() throws Exception {
        // get original value
        Document doc = XmlTools.getSAXBuilder().build(sampleAltoFile.toFile());
        Element element = AltoSaver.getWordById(doc, namespace, "Textword_1");
        assertEquals("WIE", element.getAttributeValue("CONTENT"));

        // apply changes
        AltoChange change = new AltoChange();
        change.setWordId("Textword_1");
        change.setAction("changeContent");
        change.setValue("new");
        AltoChange[] changes = { change };
        AltoSaver.saveAltoChanges(sampleAltoFile, changes);

        // check for new value
        doc = XmlTools.getSAXBuilder().build(sampleAltoFile.toFile());
        element = AltoSaver.getWordById(doc, namespace, "Textword_1");
        assertEquals("new", element.getAttributeValue("CONTENT"));
    }

    @Test
    public void testSaveAltoNamedEntity() throws Exception {
        AltoChange change = new AltoChange();
        change.setWordId("Textword_1");
        change.setAction("setNamedEntity");
        NamedEntity entity = new NamedEntity();
        entity.setLabel("label");
        entity.setType("type");
        entity.setUri("uri");
        change.setEntity(entity);
        List<String> words = new ArrayList<>();
        words.add("Textword_1");
        change.setWords(words);
        AltoChange[] changes = { change };
        AltoSaver.saveAltoChanges(sampleAltoFile, changes);

        Document doc = XmlTools.getSAXBuilder().build(sampleAltoFile.toFile());
        Element element = AltoSaver.getWordById(doc, namespace, "Textword_1");
        assertEquals("Tag1", element.getAttributeValue("TAGREFS"));
    }

    @Test
    public void testAltoChange() {
        AltoChange altoChange = new AltoChange();

        // Test wordId
        altoChange.setWordId("word1");
        assertEquals("word1", altoChange.getWordId());

        // Test action
        altoChange.setAction("insert");
        assertEquals("insert", altoChange.getAction());

        // Test value
        altoChange.setValue("newValue");
        assertEquals("newValue", altoChange.getValue());

        // Test words
        List<String> wordsList = new ArrayList<>();
        wordsList.add("word1");
        wordsList.add("word2");
        altoChange.setWords(wordsList);
        assertEquals(wordsList, altoChange.getWords());

        // Test entity
        NamedEntity entity = new NamedEntity();
        altoChange.setEntity(entity);
        assertEquals(entity, altoChange.getEntity());
    }

    @Test
    public void testNamedEntityDefaultConstructor() {
        NamedEntity entity = new NamedEntity();
        assertNull(entity.getId());
        assertNull(entity.getLabel());
        assertNull(entity.getType());
        assertNull(entity.getUri());
    }

    @Test
    public void testNamedEntityConstructor() {
        NamedEntity entity = new NamedEntity("1", "Label1", "Type1", "http://example.com");
        assertEquals("1", entity.getId());
        assertEquals("Label1", entity.getLabel());
        assertEquals("Type1", entity.getType());
        assertEquals("http://example.com", entity.getUri());
    }

    @Test
    public void testNamedEntityGettersAndSetters() {
        NamedEntity entity = new NamedEntity();
        entity.setId("2");
        entity.setLabel("Label2");
        entity.setType("Type2");
        entity.setUri("http://example.org");

        assertEquals("2", entity.getId());
        assertEquals("Label2", entity.getLabel());
        assertEquals("Type2", entity.getType());
        assertEquals("http://example.org", entity.getUri());
    }

    @Test
    public void testNamedEntityEquals() {
        NamedEntity entity1 = new NamedEntity("1", "Label1", "Type1", "http://example.com");
        NamedEntity entity2 = new NamedEntity("1", "Label2", "Type2", "http://example.org");
        NamedEntity entity3 = new NamedEntity("2", "Label3", "Type3", "http://example.net");

        assertEquals(entity1, entity2);
        assertNotEquals(entity1, entity3);
        assertNotEquals(entity1, null);
        assertNotEquals("someString", entity1);
    }

    @Test
    public void testNamedEntityHashCode() {
        NamedEntity entity1 = new NamedEntity("1", "Label1", "Type1", "http://example.com");
        NamedEntity entity2 = new NamedEntity("1", "Label2", "Type2", "http://example.org");
        NamedEntity entity3 = new NamedEntity("2", "Label3", "Type3", "http://example.net");

        assertEquals(entity1.hashCode(), entity2.hashCode());
        assertNotEquals(entity1.hashCode(), entity3.hashCode());

        NamedEntity entityWithoutId = new NamedEntity(null, "Label4", "Type4", "http://example.biz");
        assertEquals(0, entityWithoutId.hashCode());
    }

}
