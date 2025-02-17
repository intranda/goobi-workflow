package io.goobi.workflow.api.vocabulary;

import java.util.List;

import io.goobi.vocabulary.exchange.Vocabulary;
import io.goobi.workflow.api.vocabulary.hateoas.VocabularyPageResult;
import io.goobi.workflow.api.vocabulary.helper.CachedLookup;
import io.goobi.workflow.api.vocabulary.helper.ExtendedVocabulary;
import jakarta.servlet.http.Part;

public class VocabularyAPI extends CRUDAPI<Vocabulary, VocabularyPageResult> {
    private static final String COMMON_ENDPOINT = "/api/v1/vocabularies";
    private static final String FIND_INSTANCE_ENDPOINT = "/api/v1/vocabularies/by-name/{{0}}";
    private static final String IMPORT_CSV_ENDPOINT = "/api/v1/vocabularies/{{0}}/import/csv";
    private static final String IMPORT_EXCEL_ENDPOINT = "/api/v1/vocabularies/{{0}}/import/excel";
    private static final String INSTANCE_ENDPOINT = COMMON_ENDPOINT + "/{{0}}";

    private final CachedLookup<Long, ExtendedVocabulary> singleLookupCache;
    private final CachedLookup<String, ExtendedVocabulary> singleNameLookupCache;

    public VocabularyAPI(String address) {
        super(address, Vocabulary.class, VocabularyPageResult.class, COMMON_ENDPOINT, INSTANCE_ENDPOINT);
        this.singleLookupCache = new CachedLookup<>(id -> new ExtendedVocabulary(super.get(id)));
        this.singleNameLookupCache = new CachedLookup<>(name -> new ExtendedVocabulary(restApi.get(FIND_INSTANCE_ENDPOINT, Vocabulary.class, name)));
    }

    @Override
    public ExtendedVocabulary get(long id) {
        return this.singleLookupCache.getCached(id);
    }

    public List<ExtendedVocabulary> all() {
        return restApi.get(COMMON_ENDPOINT, VocabularyPageResult.class, "sort=name", "all=1").getContent();
    }

    @Override
    public ExtendedVocabulary change(Vocabulary vocabulary) {
        ExtendedVocabulary result = new ExtendedVocabulary(super.change(vocabulary));
        this.singleLookupCache.update(vocabulary.getId(), result);
        this.singleNameLookupCache.update(vocabulary.getName(), result);
        return result;
    }

    @Override
    public void delete(Vocabulary vocabulary) {
        super.delete(vocabulary);
        this.singleLookupCache.invalidate(vocabulary.getId());
    }

    public ExtendedVocabulary findByName(String name) {
        return this.singleNameLookupCache.getCached(name);
    }

    public void cleanImportCsv(long id, Part uploadedFile) {
        restApi.put(IMPORT_CSV_ENDPOINT, uploadedFile, id);
    }

    public void importCsv(long id, Part uploadedFile) {
        restApi.post(IMPORT_CSV_ENDPOINT, uploadedFile, id);
    }

    public void cleanImportExcel(long id, Part uploadedFile) {
        restApi.put(IMPORT_EXCEL_ENDPOINT, uploadedFile, id);
    }

    public void importExcel(long id, Part uploadedFile) {
        restApi.post(IMPORT_EXCEL_ENDPOINT, uploadedFile, id);
    }
}
