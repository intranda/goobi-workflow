package io.goobi.workflow.api.vocabulary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import io.goobi.vocabulary.exception.VocabularyException;
import io.goobi.vocabulary.exchange.Vocabulary;
import io.goobi.vocabulary.exchange.VocabularyRecord;
import io.goobi.vocabulary.exchange.VocabularySchema;
import io.goobi.workflow.api.vocabulary.hateoas.VocabularyRecordPageResult;
import io.goobi.workflow.api.vocabulary.helper.ExtendedVocabularyRecord;
import jakarta.faces.model.SelectItem;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@ExtendWith(MockitoExtension.class)
public class VocabularyRecordAPITest {
    private VocabularyRecordAPI api;

    private Response response;

    private Vocabulary vocabulary;
    private VocabularySchema schema;
    private VocabularyRecord vocabularyRecord;

    //    @BeforeEach
    public void init() {
        api = VocabularyAPIManager.getInstance().vocabularyRecords();
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

        Mockito.when(response.readEntity(VocabularyException.class)).thenReturn(generateVocabularyException());

        vocabulary = new Vocabulary();
        vocabulary.setId(0L);
        vocabulary.setSchemaId(0L);
        vocabulary.setMetadataSchemaId(0L);
        schema = new VocabularySchema();
        schema.setId(0L);
        schema.setDefinitions(Collections.emptyList());
        vocabularyRecord = new VocabularyRecord();
        vocabularyRecord.setId(0L);
        vocabularyRecord.setVocabularyId(0L);
        vocabularyRecord.setFields(Collections.emptySet());
    }

    private void setupResponseSuccess(boolean success) {
        setupResponseSuccess(success, Integer.MAX_VALUE);
    }

    private void setupResponseSuccess(boolean success, int times) {
        Mockito.when(response.getStatus()).thenReturn(success ? 200 : 500);
    }

    private <T> void setupResponse(T o, Class<T> clazz) {
        setupResponse(o, clazz, Integer.MAX_VALUE);
    }

    private <T> void setupResponse(T o, Class<T> clazz, int times) {
        Mockito.when(response.readEntity(clazz)).thenReturn(o);
    }

    private void setupResponseFinish() {
        response.close();
    }

    private VocabularyException generateVocabularyException() {
        return new VocabularyException(VocabularyException.ErrorCode.EntityNotFound, null, null, (params) -> "Vocabulary test exception");
    }

    @Test
    @Disabled
    public void givenVocabularyRecordDoesNotExistWhenGetVocabularyRecordThenThrowAPIException() {
        setupResponseSuccess(false);
        setupResponseFinish();

        api.get(0L);
    }

    @Test
    @Disabled
    public void givenVocabularyRecordDoesExistWhenGetVocabularyRecordThenReturnCorrectResult() {
        setupResponseSuccess(true);
        setupResponse(vocabularyRecord, VocabularyRecord.class);
        setupResponse(vocabulary, Vocabulary.class);
        setupResponse(schema, VocabularySchema.class);
        setupResponseFinish();

        VocabularyRecord result = api.get(0L);

        assertEquals(vocabularyRecord, result);
    }

    @Test
    @Disabled
    public void givenVocabularyRecordDoesExistWhenGetByUrlVocabularyRecordThenReturnCorrectResult() {
        setupResponseSuccess(true);
        setupResponse(vocabularyRecord, VocabularyRecord.class);
        setupResponse(vocabulary, Vocabulary.class);
        setupResponse(schema, VocabularySchema.class);
        setupResponseFinish();

        VocabularyRecord result = api.get("");

        assertEquals(vocabularyRecord, result);
    }

    @Test
    @Disabled
    public void givenVocabularyRecordDoesExistWhenListWithAllQueryParamsVocabularyRecordsThenReturnCorrectResult() {
        VocabularyRecordPageResult pageResult = new VocabularyRecordPageResult();
        pageResult.setContent(List.of(new ExtendedVocabularyRecord(vocabularyRecord)));

        setupResponseSuccess(true);
        setupResponse(vocabularyRecord, VocabularyRecord.class);
        setupResponse(vocabulary, Vocabulary.class);
        setupResponse(schema, VocabularySchema.class);
        setupResponse(pageResult, VocabularyRecordPageResult.class);
        setupResponseFinish();

        VocabularyRecordAPI.VocabularyRecordQueryBuilder query = api.list(0L);
        VocabularyRecordPageResult result = query
                .search("")
                .pageSize(0)
                .page(0)
                .sorting("")
                .all()
                .request();
        assertNotNull(result.getPage());
    }

    @Test
    @Disabled
    public void givenVocabularyRecordDoesExistWhenListWithAllEmptyQueryParamsVocabularyRecordsThenReturnCorrectResult() {
        VocabularyRecordPageResult pageResult = new VocabularyRecordPageResult();
        pageResult.setContent(List.of(new ExtendedVocabularyRecord(vocabularyRecord)));

        setupResponseSuccess(true);
        setupResponse(vocabularyRecord, VocabularyRecord.class);
        setupResponse(vocabulary, Vocabulary.class);
        setupResponse(schema, VocabularySchema.class);
        setupResponse(pageResult, VocabularyRecordPageResult.class);
        setupResponseFinish();

        VocabularyRecordAPI.VocabularyRecordQueryBuilder query = api.list(0L);
        VocabularyRecordPageResult result = query
                .search(Optional.empty())
                .pageSize(Optional.empty())
                .page(Optional.empty())
                .sorting(Optional.empty())
                .request();
        assertNotNull(result.getPage());
    }

    @Test
    @Disabled
    public void givenVocabularyRecordDoesExistWhenListWithNoQueryParamsVocabularyRecordsThenReturnCorrectResult() {
        VocabularyRecordPageResult pageResult = new VocabularyRecordPageResult();
        pageResult.setContent(List.of(new ExtendedVocabularyRecord(vocabularyRecord)));

        setupResponseSuccess(true);
        setupResponse(vocabularyRecord, VocabularyRecord.class);
        setupResponse(vocabulary, Vocabulary.class);
        setupResponse(schema, VocabularySchema.class);
        setupResponse(pageResult, VocabularyRecordPageResult.class);
        setupResponseFinish();

        VocabularyRecordAPI.VocabularyRecordQueryBuilder query = api.list(0L);
        VocabularyRecordPageResult result = query.request();
        assertNotNull(result.getPage());
    }

    @Test
    @Disabled
    public void givenVocabularyRecordThatExistsWhenSaveVocabularyRecordThenChangeExisting() {
        setupResponseSuccess(true);
        setupResponse(vocabularyRecord, VocabularyRecord.class);
        setupResponse(vocabulary, Vocabulary.class);
        setupResponse(schema, VocabularySchema.class);
        setupResponseFinish();

        VocabularyRecord result = api.save(vocabularyRecord);
        assertNotNull(result);
    }

    @Test
    @Disabled
    public void givenVocabularyRecordThatDoesNotExistWhenSaveVocabularyRecordThenCreateNew() {
        setupResponseSuccess(true);
        setupResponse(vocabularyRecord, VocabularyRecord.class);
        setupResponse(vocabulary, Vocabulary.class);
        setupResponse(schema, VocabularySchema.class);
        setupResponseFinish();

        VocabularyRecord newRecord = new VocabularyRecord();
        newRecord.setVocabularyId(0L);
        newRecord.setFields(Collections.emptySet());
        VocabularyRecord result = api.save(newRecord);
        assertNotNull(result);
    }

    @Test
    @Disabled
    public void givenVocabularyRecordThatIsMetadataWhenSaveVocabularyRecordThenChangeMetadata() {
        setupResponseSuccess(true);
        setupResponse(vocabularyRecord, VocabularyRecord.class);
        setupResponse(vocabulary, Vocabulary.class);
        setupResponse(schema, VocabularySchema.class);
        setupResponseFinish();

        VocabularyRecord newRecord = new VocabularyRecord();
        newRecord.setVocabularyId(0L);
        newRecord.setMetadata(true);
        newRecord.setFields(Collections.emptySet());
        VocabularyRecord result = api.save(newRecord);
        assertNotNull(result);
    }

    @Test
    @Disabled
    public void givenVocabularyIsNotEmptyWhenGetRecordSelectItemsThenReturnCorrectList() {
        VocabularyRecordPageResult pageResult = new VocabularyRecordPageResult();
        pageResult.setContent(new LinkedList<>(List.of(new ExtendedVocabularyRecord(vocabularyRecord))));

        setupResponseSuccess(true);
        setupResponse(vocabularyRecord, VocabularyRecord.class);
        setupResponse(vocabulary, Vocabulary.class);
        setupResponse(schema, VocabularySchema.class);
        setupResponse(pageResult, VocabularyRecordPageResult.class);
        setupResponseFinish();

        List<SelectItem> items = api.getRecordSelectItems(0L);
        assertEquals(10, items.size());
    }

    @Test
    @Disabled
    public void givenVocabularyIsNotEmptyWhenGetAllHierarchicalRecordsThenReturnCorrectList() {
        VocabularyRecordPageResult pageResult = new VocabularyRecordPageResult();
        pageResult.setContent(new LinkedList<>(List.of(new ExtendedVocabularyRecord(vocabularyRecord))));

        setupResponseSuccess(true);
        setupResponse(vocabularyRecord, VocabularyRecord.class);
        setupResponse(vocabulary, Vocabulary.class);
        setupResponse(schema, VocabularySchema.class);
        setupResponse(pageResult, VocabularyRecordPageResult.class);
        setupResponseFinish();

        List<ExtendedVocabularyRecord> items = api.getAllHierarchicalRecords(0L);
        assertEquals(10, items.size());
    }
}
