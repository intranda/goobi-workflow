package io.goobi.workflow.api.vocabulary.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import de.sub.goobi.forms.SpracheForm;
import de.sub.goobi.helper.Helper;
import io.goobi.vocabulary.exchange.FieldDefinition;
import io.goobi.vocabulary.exchange.FieldInstance;
import io.goobi.vocabulary.exchange.FieldValue;
import io.goobi.vocabulary.exchange.HateoasHref;
import io.goobi.vocabulary.exchange.Language;
import io.goobi.vocabulary.exchange.TranslationDefinition;
import io.goobi.vocabulary.exchange.TranslationInstance;
import io.goobi.vocabulary.exchange.Vocabulary;
import io.goobi.vocabulary.exchange.VocabularyRecord;
import io.goobi.vocabulary.exchange.VocabularySchema;
import io.goobi.workflow.api.vocabulary.RESTAPI;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;

/**
 * A record of a vocabulary with a translated main field. English is the fallback translation, German is the one a German user sees.
 */
@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class ExtendedVocabularyRecordTest {
    // ids no other test uses, the vocabulary API caches lookups by id
    private static final long ID = 4711L;
    private static final String FALLBACK_VALUE = "Book";
    private static final String GERMAN_VALUE = "Buch";

    private VocabularyRecord vocabularyRecord;

    @BeforeEach
    public void setup() {
        Client testClient = Mockito.mock(Client.class);
        WebTarget target = Mockito.mock(WebTarget.class);
        Invocation.Builder builder = Mockito.mock(Invocation.Builder.class);
        Response response = Mockito.mock(Response.class);

        Mockito.when(testClient.target((String) Mockito.any())).thenReturn(target);
        RESTAPI.setClient(testClient);
        Mockito.when(target.request(MediaType.APPLICATION_JSON)).thenReturn(builder);
        Mockito.when(builder.header(Mockito.anyString(), Mockito.anyString())).thenReturn(builder);
        Mockito.when(builder.get()).thenReturn(response);
        Mockito.when(response.getStatus()).thenReturn(200);

        FieldDefinition definition = new FieldDefinition();
        definition.setId(ID);
        definition.setSchemaId(ID);
        definition.setName("term");
        definition.setMainEntry(true);
        definition.setTitleField(true);
        definition.setTranslationDefinitions(Set.of(translationDefinition("eng", true), translationDefinition("ger", false)));

        VocabularySchema schema = new VocabularySchema();
        schema.setId(ID);
        schema.setDefinitions(List.of(definition));

        Vocabulary vocabulary = new Vocabulary();
        vocabulary.setId(ID);
        vocabulary.setSchemaId(ID);
        vocabulary.setName("Genre");
        vocabulary.set_links(Map.of("self", href("https://vocabulary.example.org/vocabularies/" + ID)));

        Language language = new Language();
        language.setName("Language");

        Mockito.when(response.readEntity(FieldDefinition.class)).thenReturn(definition);
        Mockito.when(response.readEntity(VocabularySchema.class)).thenReturn(schema);
        Mockito.when(response.readEntity(Vocabulary.class)).thenReturn(vocabulary);
        Mockito.when(response.readEntity(Language.class)).thenReturn(language);

        FieldValue value = new FieldValue();
        value.setTranslations(new ArrayList<>(List.of(translation("eng", FALLBACK_VALUE), translation("ger", GERMAN_VALUE))));
        FieldInstance field = new FieldInstance();
        field.setDefinitionId(ID);
        field.setRecordId(ID);
        field.setValues(new ArrayList<>(List.of(value)));

        vocabularyRecord = new VocabularyRecord();
        vocabularyRecord.setId(ID);
        vocabularyRecord.setVocabularyId(ID);
        vocabularyRecord.setFields(new HashSet<>(Set.of(field)));
        vocabularyRecord.set_links(Map.of("self", href("https://vocabulary.example.org/records/" + ID)));
    }

    private static TranslationDefinition translationDefinition(String language, boolean fallback) {
        TranslationDefinition definition = new TranslationDefinition();
        definition.setLanguage(language);
        definition.setFallback(fallback);
        definition.setRequired(fallback);
        return definition;
    }

    private static TranslationInstance translation(String language, String value) {
        TranslationInstance translation = new TranslationInstance();
        translation.setLanguage(language);
        translation.setValue(value);
        return translation;
    }

    private static HateoasHref href(String url) {
        HateoasHref href = new HateoasHref();
        href.setHref(url);
        return href;
    }

    private static MockedStatic<Helper> germanUserInterface() {
        SpracheForm languageBean = Mockito.mock(SpracheForm.class);
        Mockito.when(languageBean.getLocale()).thenReturn(Locale.GERMAN);
        MockedStatic<Helper> helper = Mockito.mockStatic(Helper.class);
        helper.when(Helper::getLanguageBean).thenReturn(languageBean);
        return helper;
    }

    /**
     * The main value is what a user reads, so it follows the language of the user interface.
     */
    @Test
    public void testMainValueFollowsTheUserInterfaceLanguage() {
        try (MockedStatic<Helper> helper = germanUserInterface()) {
            ExtendedVocabularyRecord record = new ExtendedVocabularyRecord(vocabularyRecord);

            assertEquals(GERMAN_VALUE, record.getMainValue());
        }
    }

    /**
     * The value that ends up in a METS file must not depend on the language of whoever edited it. The vocabulary server requires exactly one
     * fallback translation for every translated field, so that one is it.
     */
    @Test
    public void testLanguageIndependentMainValueIsTheFallbackTranslation() {
        try (MockedStatic<Helper> helper = germanUserInterface()) {
            ExtendedVocabularyRecord record = new ExtendedVocabularyRecord(vocabularyRecord);

            assertEquals(FALLBACK_VALUE, record.getLanguageIndependentMainValue());
        }
    }

    /**
     * A vocabularySearch field writes the record into the metadata; the value must be the fallback translation there as well.
     */
    @Test
    public void testReferenceMetadataIsWrittenInTheFallbackTranslation() throws Exception {
        MetadataType type = new MetadataType();
        type.setName("junitMetadata");
        Metadata metadata = new Metadata(type);

        try (MockedStatic<Helper> helper = germanUserInterface()) {
            ExtendedVocabularyRecord record = new ExtendedVocabularyRecord(vocabularyRecord);
            record.writeReferenceMetadata(metadata);
        }

        assertEquals(FALLBACK_VALUE, metadata.getValue());
    }
}
