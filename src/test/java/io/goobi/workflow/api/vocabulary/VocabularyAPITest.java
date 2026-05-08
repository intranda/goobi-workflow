package io.goobi.workflow.api.vocabulary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import io.goobi.vocabulary.exception.VocabularyException;
import io.goobi.vocabulary.exchange.Vocabulary;
import io.goobi.workflow.api.vocabulary.hateoas.VocabularyPageResult;
import io.goobi.workflow.api.vocabulary.helper.ExtendedVocabulary;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class VocabularyAPITest {
    private VocabularyAPI api;

    private Response response;

    @BeforeEach
    public void init() {
        api = VocabularyAPIManager.getInstance().vocabularies();
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

    }

    private <T> void setupResponse(T o, Class<T> clazz) {
        Mockito.when(response.getStatus()).thenReturn(o != null ? 200 : 500);

        Mockito.when(response.readEntity(VocabularyException.class)).thenReturn(generateVocabularyException());
        Mockito.when(response.readEntity(clazz)).thenReturn(o);
        response.close();

    }

    private VocabularyException generateVocabularyException() {
        return new VocabularyException(VocabularyException.ErrorCode.EntityNotFound, null, null, (params) -> "Vocabulary test exception");
    }

    @Test
    public void givenVocabularyDoesNotExistWhenGetVocabularyThenThrowAPIException() {
        assertThrows(APIException.class, () -> {
            setupResponse(null, null);
            api.get(0L);
        });
    }

    @Test
    public void givenVocabularyDoesExistWhenGetVocabularyThenReturnCorrectResult() {
        Vocabulary vocabulary = new Vocabulary();
        vocabulary.setId(0L);
        setupResponse(vocabulary, Vocabulary.class);

        Vocabulary result = api.get(0L);

        assertEquals(vocabulary.getId(), result.getId());
    }

    @Test
    public void givenVocabularyDoesExistWhenGetByNameVocabularyThenReturnCorrectResult() {
        Vocabulary vocabulary = new Vocabulary();
        setupResponse(vocabulary, Vocabulary.class);

        Vocabulary result = api.findByName("Test");

        assertEquals(vocabulary, result);
    }

    @Test
    public void givenVocabularyDoesExistWhenChangeVocabularyThenReturnCorrectResult() {
        Vocabulary vocabulary = new Vocabulary();
        vocabulary.setId(0L);

        setupResponse(vocabulary, Vocabulary.class);

        Vocabulary result = api.change(vocabulary);

        assertEquals(vocabulary, result);
    }

    @Test
    public void givenVocabularyDoesExistWhenDeleteVocabularyThenReturnCorrectResult() {
        Vocabulary vocabulary = new Vocabulary();
        vocabulary.setId(0L);

        setupResponse(vocabulary, Vocabulary.class);

        api.delete(vocabulary);
    }

    @Test
    public void givenNothingWhenGetAllThenReturnCorrectPageResult() {
        VocabularyPageResult pageResult = new VocabularyPageResult();
        pageResult.setContent(Collections.emptyList());

        setupResponse(pageResult, VocabularyPageResult.class);

        List<ExtendedVocabulary> result = api.all();

        assertEquals(Collections.emptyList(), result);
    }
}
