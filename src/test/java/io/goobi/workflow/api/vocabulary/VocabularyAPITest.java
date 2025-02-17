package io.goobi.workflow.api.vocabulary;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import io.goobi.vocabulary.exception.VocabularyException;
import io.goobi.vocabulary.exchange.Vocabulary;
import io.goobi.workflow.api.vocabulary.hateoas.VocabularyPageResult;
import io.goobi.workflow.api.vocabulary.helper.ExtendedVocabulary;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*" })
public class VocabularyAPITest {
    private VocabularyAPI api;

    private Response response;

    @Before
    public void init() {
        api = VocabularyAPIManager.getInstance().vocabularies();
        Client testClient = EasyMock.createMock(Client.class);
        WebTarget target = EasyMock.createMock(WebTarget.class);
        Invocation.Builder builder = EasyMock.createMock(Invocation.Builder.class);
        response = EasyMock.createMock(Response.class);

        EasyMock.expect(testClient.target((String) EasyMock.anyObject())).andReturn(target);
        RESTAPI.setClient(testClient);

        EasyMock.expect(target.request(MediaType.APPLICATION_JSON)).andReturn(builder).anyTimes();
        EasyMock.expect(target.request(MediaType.MULTIPART_FORM_DATA)).andReturn(builder).anyTimes();

        EasyMock.expect(builder.header(EasyMock.anyString(), EasyMock.anyString())).andReturn(builder).anyTimes();
        EasyMock.expect(builder.get()).andReturn(response).anyTimes();
        EasyMock.expect(builder.post(EasyMock.anyObject())).andReturn(response).anyTimes();
        EasyMock.expect(builder.put(EasyMock.anyObject())).andReturn(response).anyTimes();
        EasyMock.expect(builder.delete()).andReturn(response).anyTimes();

        EasyMock.replay(testClient, target, builder);
    }

    private <T> void setupResponse(T o, Class<T> clazz) {
        EasyMock.expect(response.getStatus()).andReturn(o != null ? 200 : 500).anyTimes();

        EasyMock.expect(response.readEntity(VocabularyException.class)).andReturn(generateVocabularyException()).anyTimes();
        EasyMock.expect(response.readEntity(clazz)).andReturn(o).anyTimes();
        response.close();

        EasyMock.replay(response);
    }

    private VocabularyException generateVocabularyException() {
        return new VocabularyException(VocabularyException.ErrorCode.EntityNotFound, null, null, (params) -> "Vocabulary test exception");
    }

    @Test(expected = APIException.class)
    public void givenVocabularyDoesNotExist_whenGetVocabulary_thenThrowAPIException() {
        setupResponse(null, null);
        api.get(0L);
    }

    @Test
    public void givenVocabularyDoesExist_whenGetVocabulary_thenReturnCorrectResult() {
        Vocabulary vocabulary = new Vocabulary();
        setupResponse(vocabulary, Vocabulary.class);

        Vocabulary result = api.get(0L);

        assertEquals(vocabulary, result);
    }

    @Test
    public void givenVocabularyDoesExist_whenGetByNameVocabulary_thenReturnCorrectResult() {
        Vocabulary vocabulary = new Vocabulary();
        setupResponse(vocabulary, Vocabulary.class);

        Vocabulary result = api.findByName("Test");

        assertEquals(vocabulary, result);
    }

    @Test
    public void givenVocabularyDoesExist_whenChangeVocabulary_thenReturnCorrectResult() {
        Vocabulary vocabulary = new Vocabulary();
        vocabulary.setId(0L);

        setupResponse(vocabulary, Vocabulary.class);

        Vocabulary result = api.change(vocabulary);

        assertEquals(vocabulary, result);
    }

    @Test
    public void givenVocabularyDoesExist_whenDeleteVocabulary_thenReturnCorrectResult() {
        Vocabulary vocabulary = new Vocabulary();
        vocabulary.setId(0L);

        setupResponse(vocabulary, Vocabulary.class);

        api.delete(vocabulary);
    }

    @Test
    public void givenNothing_whenGetAll_thenReturnCorrectPageResult() {
        VocabularyPageResult pageResult = new VocabularyPageResult();
        pageResult.setContent(Collections.emptyList());

        setupResponse(pageResult, VocabularyPageResult.class);

        List<ExtendedVocabulary> result = api.all();

        assertEquals(Collections.emptyList(), result);
    }
}
