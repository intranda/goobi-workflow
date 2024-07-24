package io.goobi.workflow.api.vocabulary;

import io.goobi.vocabulary.exchange.FieldDefinition;
import io.goobi.vocabulary.exchange.Vocabulary;
import io.goobi.vocabulary.exchange.VocabularyRecord;
import io.goobi.vocabulary.exchange.VocabularySchema;
import io.goobi.workflow.api.vocabulary.hateoas.VocabularySchemaPageResult;

import java.util.HashMap;
import java.util.Map;

public class VocabularySchemaAPI extends CRUDAPI<VocabularySchema, VocabularySchemaPageResult> {
    private static final String COMMON_ENDPOINT = "/api/v1/schemas";
    private static final String INSTANCE_ENDPOINT = COMMON_ENDPOINT + "/{{0}}";

    // TODO: Make this generic
    private Map<Long, FieldDefinition> definitionMap = new HashMap<>();

    public VocabularySchemaAPI(String host, int port) {
        super(host, port, VocabularySchema.class, VocabularySchemaPageResult.class, COMMON_ENDPOINT, INSTANCE_ENDPOINT);
    }

    @Override
    public VocabularySchema get(long id) {
        VocabularySchema schema = super.get(id);
        schema.getDefinitions().forEach(d -> definitionMap.put(d.getId(), d));
        return schema;
    }

    public FieldDefinition getDefinition(Long definitionId) {
        return this.definitionMap.get(definitionId);
    }

    public VocabularySchema get(VocabularyRecord vocabularyRecord) {
        Vocabulary vocabulary = VocabularyAPIManager.getInstance().vocabularies().get(vocabularyRecord.getVocabularyId());
        return get(vocabulary.getSchemaId());
    }
}
