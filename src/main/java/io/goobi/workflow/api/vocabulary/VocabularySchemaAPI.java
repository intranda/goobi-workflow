package io.goobi.workflow.api.vocabulary;

import io.goobi.vocabulary.exchange.FieldDefinition;
import io.goobi.vocabulary.exchange.Vocabulary;
import io.goobi.vocabulary.exchange.VocabularyRecord;
import io.goobi.vocabulary.exchange.VocabularySchema;
import io.goobi.workflow.api.vocabulary.hateoas.VocabularySchemaPageResult;
import io.goobi.workflow.api.vocabulary.helper.CachedLookup;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class VocabularySchemaAPI extends CRUDAPI<VocabularySchema, VocabularySchemaPageResult> {
    private static final String COMMON_ENDPOINT = "/api/v1/schemas";
    private static final String INSTANCE_ENDPOINT = COMMON_ENDPOINT + "/{{0}}";

    // TODO: Make this generic
    private Map<Long, FieldDefinition> definitionMap = new HashMap<>();
    private final CachedLookup<VocabularySchema> singleLookupCache;

    public VocabularySchemaAPI(String host, int port) {
        super(host, port, VocabularySchema.class, VocabularySchemaPageResult.class, COMMON_ENDPOINT, INSTANCE_ENDPOINT);
        this.singleLookupCache = new CachedLookup<>(this::request);
    }

    @Override
    public VocabularySchema get(long id) {
        return this.singleLookupCache.getCached(id);
    }

    private VocabularySchema request(long id) {
        VocabularySchema schema = super.get(id);
        schema.getDefinitions().forEach(d -> definitionMap.put(d.getId(), d));
        return schema;
    }

    public FieldDefinition getDefinition(Long definitionId) {
        return this.definitionMap.get(definitionId);
    }

    public VocabularySchema getSchema(VocabularyRecord vocabularyRecord) {
        return getSchema(VocabularyAPIManager.getInstance().vocabularies().get(vocabularyRecord.getVocabularyId()));
    }

    public VocabularySchema getSchema(Vocabulary vocabulary) {
        return get(vocabulary.getSchemaId());
    }

    public Optional<VocabularySchema> getMetadataSchema(VocabularyRecord vocabularyRecord) {
        return getMetadataSchema(VocabularyAPIManager.getInstance().vocabularies().get(vocabularyRecord.getVocabularyId()));
    }

    public Optional<VocabularySchema> getMetadataSchema(Vocabulary vocabulary) {
        if (vocabulary.getMetadataSchemaId() == null) {
            return Optional.empty();
        }
        return Optional.of(get(vocabulary.getMetadataSchemaId()));
    }
}
