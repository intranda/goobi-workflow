package io.goobi.workflow.api.vocabulary;

import io.goobi.vocabulary.exception.VocabularyException;
import io.goobi.vocabulary.exchange.Vocabulary;
import io.goobi.vocabulary.exchange.VocabularyRecord;
import io.goobi.vocabulary.exchange.VocabularySchema;
import io.goobi.workflow.api.vocabulary.hateoas.VocabularyPageResult;
import io.goobi.workflow.api.vocabulary.hateoas.VocabularyRecordPageResult;
import io.goobi.workflow.api.vocabulary.helper.ExtendedVocabulary;
import io.goobi.workflow.api.vocabulary.helper.ExtendedVocabularyRecord;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;


@RunWith(PowerMockRunner.class)
@PrepareForTest({})
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*"})
public class VocabularyRecordAPITest {
    private VocabularyRecordAPI api;

    private Response response;

    private Vocabulary vocabulary;
    private VocabularySchema schema;
    private VocabularyRecord vocabularyRecord;

    @Before
    public void init() {
        api = VocabularyAPIManager.getInstance().vocabularyRecords();
        Client testClient = EasyMock.createMock(Client.class);
        WebTarget target = EasyMock.createMock(WebTarget.class);
        Invocation.Builder builder = EasyMock.createMock(Invocation.Builder.class);
        response = EasyMock.createMock(Response.class);

        EasyMock.expect(testClient.target((String) EasyMock.anyObject())).andReturn(target).anyTimes();
        RESTAPI.setClient(testClient);

        EasyMock.expect(target.request(MediaType.APPLICATION_JSON)).andReturn(builder).anyTimes();
        EasyMock.expect(target.request(MediaType.MULTIPART_FORM_DATA)).andReturn(builder).anyTimes();

        EasyMock.expect(builder.get()).andReturn(response).anyTimes();
        EasyMock.expect(builder.post(EasyMock.anyObject())).andReturn(response).anyTimes();
        EasyMock.expect(builder.put(EasyMock.anyObject())).andReturn(response).anyTimes();
        EasyMock.expect(builder.delete()).andReturn(response).anyTimes();

        EasyMock.expect(response.readEntity(VocabularyException.class)).andReturn(generateVocabularyException()).anyTimes();

        EasyMock.replay(testClient, target, builder);

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
        if (times == Integer.MAX_VALUE) {
            EasyMock.expect(response.getStatus()).andReturn(success ? 200 : 500).anyTimes();
        } else {
            EasyMock.expect(response.getStatus()).andReturn(success ? 200 : 500).times(times);
        }
    }

    private <T> void setupResponse(T o, Class<T> clazz) {
        setupResponse(o, clazz, Integer.MAX_VALUE);
    }

    private <T> void setupResponse(T o, Class<T> clazz, int times) {
        if (times == Integer.MAX_VALUE) {
            EasyMock.expect(response.readEntity(clazz)).andReturn(o).anyTimes();
        } else {
            EasyMock.expect(response.readEntity(clazz)).andReturn(o).times(times);
        }
    }

    private void setupResponseFinish() {
        response.close();
        EasyMock.expectLastCall().anyTimes();
        EasyMock.replay(response);
    }

    private VocabularyException generateVocabularyException() {
        return new VocabularyException(VocabularyException.ErrorCode.EntityNotFound, null, null, (params) -> "Vocabulary test exception");
    }

    @Test(expected = APIException.class)
    public void givenVocabularyRecordDoesNotExist_whenGetVocabularyRecord_thenThrowAPIException() {
        setupResponseSuccess(false);
        setupResponseFinish();

        api.get(0L);
    }

    @Test
    public void givenVocabularyRecordDoesExist_whenGetVocabularyRecord_thenReturnCorrectResult() {
        setupResponseSuccess(true);
        setupResponse(vocabularyRecord, VocabularyRecord.class);
        setupResponse(vocabulary, Vocabulary.class);
        setupResponse(schema, VocabularySchema.class);
        setupResponseFinish();

        VocabularyRecord result = api.get(0L);

        assertEquals(vocabularyRecord, result);
    }

    @Test
    public void givenVocabularyRecordDoesExist_whenGetByUrlVocabularyRecord_thenReturnCorrectResult() {
        setupResponseSuccess(true);
        setupResponse(vocabularyRecord, VocabularyRecord.class);
        setupResponse(vocabulary, Vocabulary.class);
        setupResponse(schema, VocabularySchema.class);
        setupResponseFinish();

        VocabularyRecord result = api.get("");

        assertEquals(vocabularyRecord, result);
    }

    @Test
    public void givenVocabularyRecordDoesExist_whenListWithAllQueryParamsVocabularyRecords_thenReturnCorrectResult() {
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
    }

    @Test
    public void givenVocabularyRecordDoesExist_whenListWithAllEmptyQueryParamsVocabularyRecords_thenReturnCorrectResult() {
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
    }

    @Test
    public void givenVocabularyRecordDoesExist_whenListWithNoQueryParamsVocabularyRecords_thenReturnCorrectResult() {
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
    }

    @Test
    public void givenVocabularyRecordThatExists_whenSaveVocabularyRecord_thenChangeExisting() {
        setupResponseSuccess(true);
        setupResponse(vocabularyRecord, VocabularyRecord.class);
        setupResponse(vocabulary, Vocabulary.class);
        setupResponse(schema, VocabularySchema.class);
        setupResponseFinish();

        VocabularyRecord result = api.save(vocabularyRecord);
    }

    @Test
    public void givenVocabularyRecordThatDoesNotExist_whenSaveVocabularyRecord_thenCreateNew() {
        setupResponseSuccess(true);
        setupResponse(vocabularyRecord, VocabularyRecord.class);
        setupResponse(vocabulary, Vocabulary.class);
        setupResponse(schema, VocabularySchema.class);
        setupResponseFinish();

        VocabularyRecord newRecord = new VocabularyRecord();
        newRecord.setVocabularyId(0L);
        newRecord.setFields(Collections.emptySet());
        VocabularyRecord result = api.save(newRecord);
    }

    @Test
    public void givenVocabularyRecordThatIsMetadata_whenSaveVocabularyRecord_thenChangeMetadata() {
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
    }
}
