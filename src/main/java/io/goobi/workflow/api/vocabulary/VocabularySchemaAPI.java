package io.goobi.workflow.api.vocabulary;

import io.goobi.vocabulary.exchange.FieldDefinition;
import io.goobi.vocabulary.exchange.Vocabulary;
import io.goobi.vocabulary.exchange.VocabularyRecord;
import io.goobi.vocabulary.exchange.VocabularySchema;
import io.goobi.workflow.api.vocabulary.hateoas.VocabularySchemaPageResult;
import io.goobi.workflow.api.vocabulary.helper.CachedLookup;

import java.util.Optional;

public class VocabularySchemaAPI extends CRUDAPI<VocabularySchema, VocabularySchemaPageResult> {
    private static final String COMMON_ENDPOINT = "/api/v1/schemas";
    private static final String INSTANCE_ENDPOINT = COMMON_ENDPOINT + "/{{0}}";
    private static final String COMMON_FIELD_DEFINITIONS_ENDPOINT = "/api/v1/fieldDefinitions";
    private static final String FIELD_DEFINITION_INSTANCE_ENDPOINT = COMMON_FIELD_DEFINITIONS_ENDPOINT + "/{{0}}";

    private final CachedLookup<Long, VocabularySchema> singleLookupCache;
    private final CachedLookup<Long, FieldDefinition> definitionLookupCache;

    public VocabularySchemaAPI(String address) {
        super(address, VocabularySchema.class, VocabularySchemaPageResult.class, COMMON_ENDPOINT, INSTANCE_ENDPOINT);
        this.singleLookupCache = new CachedLookup<>(this::request);
        this.definitionLookupCache = new CachedLookup<>(this::requestDefinition);
    }

    @Override
    public VocabularySchema get(long id) {
        return this.singleLookupCache.getCached(id);
    }

    private VocabularySchema request(long id) {
        VocabularySchema schema = super.get(id);
        schema.getDefinitions().forEach(d -> this.definitionLookupCache.update(d.getId(), d));
        return schema;
    }

    private FieldDefinition requestDefinition(long id) {
        return this.restApi.get(FIELD_DEFINITION_INSTANCE_ENDPOINT, FieldDefinition.class, id);
    }

    public FieldDefinition getDefinition(long definitionId) {
        return this.definitionLookupCache.getCached(definitionId);
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
