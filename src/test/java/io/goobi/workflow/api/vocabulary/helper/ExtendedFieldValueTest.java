package io.goobi.workflow.api.vocabulary.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import io.goobi.vocabulary.exchange.FieldValue;
import io.goobi.vocabulary.exchange.HateoasHref;
import io.goobi.vocabulary.exchange.Language;
import io.goobi.vocabulary.exchange.TranslationDefinition;
import io.goobi.vocabulary.exchange.TranslationInstance;
import io.goobi.workflow.api.vocabulary.RESTAPI;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class ExtendedFieldValueTest {
    private static final Long VALUE_ID = 1L;
    private static final Long FIELD_VALUE_ID = 2L;
    private static final String UNTRANSLATED_VALUE = "value";
    private static final String LINK = "self";

    private Response response;

    private ExtendedFieldValue testSubjectSimple;
    private ExtendedFieldValue testSubjectTranslated;
    private FieldValue referenceValueSimple;
    private FieldValue referenceValueTranslated;

    @BeforeEach
    public void setup() {
        Client testClient = Mockito.mock(Client.class);
        WebTarget target = Mockito.mock(WebTarget.class);
        Invocation.Builder builder = Mockito.mock(Invocation.Builder.class);
        response = Mockito.mock(Response.class);

        Mockito.when(testClient.target((String) Mockito.any())).thenReturn(target);
        RESTAPI.setClient(testClient);

        Mockito.when(target.request(MediaType.APPLICATION_JSON)).thenReturn(builder);
        Mockito.when(target.request(MediaType.MULTIPART_FORM_DATA)).thenReturn(builder);

        Mockito.when(builder.header(Mockito.anyString(), Mockito.anyString())).thenReturn(builder);
        Mockito.when(builder.get()).thenReturn(response);
        Mockito.when(builder.post(Mockito.any())).thenReturn(response);
        Mockito.when(builder.put(Mockito.any())).thenReturn(response);
        Mockito.when(builder.delete()).thenReturn(response);

        Mockito.when(response.getStatus()).thenReturn(200);
        Language eng = new Language();
        eng.setId(123L);
        eng.setName("English");
        eng.setAbbreviation("eng");
        Mockito.when(response.readEntity(Language.class)).thenReturn(eng);
        response.close();

        referenceValueSimple = new FieldValue();
        referenceValueSimple.setId(VALUE_ID);
        referenceValueSimple.setFieldId(FIELD_VALUE_ID);
        referenceValueSimple.set_links(Map.of("self", new HateoasHref()));
        TranslationInstance t = new TranslationInstance();
        t.setValue(UNTRANSLATED_VALUE);
        referenceValueSimple.setTranslations(List.of(t));
        testSubjectSimple = new ExtendedFieldValue(referenceValueSimple, Collections.emptySet());

        referenceValueTranslated = new FieldValue();
        referenceValueTranslated.setId(VALUE_ID);
        referenceValueTranslated.setFieldId(FIELD_VALUE_ID);
        referenceValueTranslated.set_links(Map.of("self", new HateoasHref()));
        referenceValueTranslated.setTranslations(new ArrayList<>());
        TranslationDefinition tdEng = new TranslationDefinition();
        tdEng.setLanguage("eng");
        tdEng.setFallback(true);
        tdEng.setRequired(true);
        testSubjectTranslated = new ExtendedFieldValue(referenceValueTranslated, Set.of(tdEng));
    }

    @Test
    public void givenEmptyFieldValueWhenGetTranslationsReturnSingleBlankTranslation() {
        referenceValueSimple.setTranslations(new ArrayList<>());
        testSubjectSimple = new ExtendedFieldValue(referenceValueSimple, Collections.emptySet());
        assertEquals(1, testSubjectTranslated.getTranslations().size());
    }

    @Test
    public void givenValidObjectWhenGetWrappedReturnWrappedFieldValue() {
        assertEquals(referenceValueSimple, testSubjectSimple.getWrapped());
    }

    @Test
    public void givenSimpleFieldValueWhenGetIdReturnCorrectValue() {
        assertEquals(VALUE_ID, testSubjectSimple.getId());
    }

    @Test
    public void givenSimpleFieldValueWhenSetIdWrappedValueCorrectlyChanged() {
        testSubjectSimple.setId(5L);
        assertEquals(5L, referenceValueSimple.getId().longValue());
    }

    @Test
    public void givenSimpleFieldValueWhenGetFieldIdReturnCorrectValue() {
        assertEquals(FIELD_VALUE_ID, testSubjectSimple.getFieldId());
    }

    @Test
    public void givenSimpleFieldValueWhenSetFieldIdWrappedValueCorrectlyChanged() {
        testSubjectSimple.setFieldId(5L);
        assertEquals(5L, referenceValueSimple.getFieldId().longValue());
    }

    @Test
    public void givenSimpleFieldValueWhenGetTranslationsReturnCorrectValue() {
        assertEquals(UNTRANSLATED_VALUE, testSubjectSimple.getTranslations().get(0).getValue());
    }

    @Test
    public void givenSimpleFieldValueWhenSetTranslationsWrappedValueCorrectlyChanged() {
        TranslationInstance t = new TranslationInstance();
        t.setValue("new");
        testSubjectSimple.setTranslations(List.of(t));
        assertEquals("new", referenceValueSimple.getTranslations().get(0).getValue());
    }

    @Test
    public void givenSimpleFieldValueWhenGetLinksReturnCorrectValue() {
        assertEquals(LINK, testSubjectSimple.get_links().keySet().stream().findFirst().get());
    }

    @Test
    public void givenSimpleFieldValueWhenSetLinksWrappedValueCorrectlyChanged() {
        testSubjectSimple.set_links(Map.of("new", new HateoasHref()));
        assertEquals("new", referenceValueSimple.get_links().keySet().stream().findFirst().get());
    }

    @Test
    public void givenTranslatedFieldValueWhenGetTranslationsReturnEmptyTranslationForEachDefinedLanguage() {
        assertEquals(1, testSubjectTranslated.getTranslations().size());
    }

    @Test
    public void givenTranslatedFieldValueWhenGetExtendedTranslationsDataMethodsWork() {
        ExtendedTranslationInstance et = testSubjectTranslated.getExtendedTranslations().get(0);
        TranslationInstance t = referenceValueTranslated.getTranslations().get(0);

        assertEquals("", et.getValue());
        et.setValue("new");
        assertEquals("new", t.getValue());

        assertEquals("English", et.getLanguageName());

        assertEquals("eng", et.getLanguage());
        et.setLanguage("ger");
        assertEquals("ger", t.getLanguage());

        assertEquals(t, et.getWrapped());

        assertNotNull(et.getDefinition());
    }

    @Test
    public void givenNothingWhenGetLanguageAbbreviationReturnCorrectValue() {
        assertEquals("eng", ExtendedTranslationInstance.transformToThreeCharacterAbbreviation("en"));
        assertEquals("ger", ExtendedTranslationInstance.transformToThreeCharacterAbbreviation("de"));
        assertEquals("fre", ExtendedTranslationInstance.transformToThreeCharacterAbbreviation("fr"));
        assertEquals("eng", ExtendedTranslationInstance.transformToThreeCharacterAbbreviation("xx"));
    }
}
