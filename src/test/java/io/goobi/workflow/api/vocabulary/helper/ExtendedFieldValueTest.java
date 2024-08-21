package io.goobi.workflow.api.vocabulary.helper;

import io.goobi.vocabulary.exchange.FieldValue;
import io.goobi.vocabulary.exchange.HateoasHref;
import io.goobi.vocabulary.exchange.Language;
import io.goobi.vocabulary.exchange.TranslationDefinition;
import io.goobi.vocabulary.exchange.TranslationInstance;
import io.goobi.workflow.api.vocabulary.LanguageAPI;
import io.goobi.workflow.api.vocabulary.RESTAPI;
import io.goobi.workflow.api.vocabulary.VocabularyAPIManager;
import org.apache.poi.ss.formula.functions.T;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

@RunWith(PowerMockRunner.class)
@PrepareForTest({})
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*"})
public class ExtendedFieldValueTest {
    private static final Long VALUE_ID = 1L;
    private static final Long FIELD_VALUE_ID = 2L;
    private static final String UNTRANSLATED_VALUE = "value";
    private static final String LINK = "self";

    private LanguageAPI api;
    private Response response;

    private ExtendedFieldValue testSubjectSimple;
    private ExtendedFieldValue testSubjectTranslated;
    private FieldValue referenceValueSimple;
    private FieldValue referenceValueTranslated;

    @Before
    public void setup() {
        api = VocabularyAPIManager.getInstance().languages();
        Client testClient = EasyMock.createMock(Client.class);
        WebTarget target = EasyMock.createMock(WebTarget.class);
        Invocation.Builder builder = EasyMock.createMock(Invocation.Builder.class);
        response = EasyMock.createMock(Response.class);

        EasyMock.expect(testClient.target((String) EasyMock.anyObject())).andReturn(target);
        RESTAPI.setClient(testClient);

        EasyMock.expect(target.request(MediaType.APPLICATION_JSON)).andReturn(builder).anyTimes();
        EasyMock.expect(target.request(MediaType.MULTIPART_FORM_DATA)).andReturn(builder).anyTimes();

        EasyMock.expect(builder.get()).andReturn(response).anyTimes();
        EasyMock.expect(builder.post(EasyMock.anyObject())).andReturn(response).anyTimes();
        EasyMock.expect(builder.put(EasyMock.anyObject())).andReturn(response).anyTimes();
        EasyMock.expect(builder.delete()).andReturn(response).anyTimes();

        EasyMock.expect(response.getStatus()).andReturn(200).anyTimes();
        Language eng = new Language();
        eng.setId(123L);
        eng.setName("English");
        eng.setAbbreviation("eng");
        EasyMock.expect(response.readEntity(Language.class)).andReturn(eng).anyTimes();
        response.close();
        EasyMock.expectLastCall().anyTimes();
        EasyMock.replay(response);

        EasyMock.replay(testClient, target, builder);

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
    public void givenEmptyFieldValue_whenGetTranslations_returnSingleBlankTranslation() {
        referenceValueSimple.setTranslations(new ArrayList<>());
        testSubjectSimple = new ExtendedFieldValue(referenceValueSimple, Collections.emptySet());
        assertEquals(1, testSubjectTranslated.getTranslations().size());
    }

    @Test
    public void givenValidObject_whenGetWrapped_returnWrappedFieldValue() {
        assertEquals(referenceValueSimple, testSubjectSimple.getWrapped());
    }

    @Test
    public void givenSimpleFieldValue_whenGetId_returnCorrectValue() {
        assertEquals(VALUE_ID, testSubjectSimple.getId());
    }

    @Test
    public void givenSimpleFieldValue_whenSetId_wrappedValueCorrectlyChanged() {
        testSubjectSimple.setId(5L);
        assertEquals(5L, referenceValueSimple.getId().longValue());
    }

    @Test
    public void givenSimpleFieldValue_whenGetFieldId_returnCorrectValue() {
        assertEquals(FIELD_VALUE_ID, testSubjectSimple.getFieldId());
    }

    @Test
    public void givenSimpleFieldValue_whenSetFieldId_wrappedValueCorrectlyChanged() {
        testSubjectSimple.setFieldId(5L);
        assertEquals(5L, referenceValueSimple.getFieldId().longValue());
    }

    @Test
    public void givenSimpleFieldValue_whenGetTranslations_returnCorrectValue() {
        assertEquals(UNTRANSLATED_VALUE, testSubjectSimple.getTranslations().get(0).getValue());
    }

    @Test
    public void givenSimpleFieldValue_whenSetTranslations_wrappedValueCorrectlyChanged() {
        TranslationInstance t = new TranslationInstance();
        t.setValue("new");
        testSubjectSimple.setTranslations(List.of(t));
        assertEquals("new", referenceValueSimple.getTranslations().get(0).getValue());
    }

    @Test
    public void givenSimpleFieldValue_whenGetLinks_returnCorrectValue() {
        assertEquals(LINK, testSubjectSimple.get_links().keySet().stream().findFirst().get());
    }

    @Test
    public void givenSimpleFieldValue_whenSetLinks_wrappedValueCorrectlyChanged() {
        testSubjectSimple.set_links(Map.of("new", new HateoasHref()));
        assertEquals("new", referenceValueSimple.get_links().keySet().stream().findFirst().get());
    }

    @Test
    public void givenTranslatedFieldValue_whenGetTranslations_returnEmptyTranslationForEachDefinedLanguage() {
        assertEquals(1, testSubjectTranslated.getTranslations().size());
    }
}
