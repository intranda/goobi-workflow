package io.goobi.workflow.api.vocabulary;

import io.goobi.vocabulary.exchange.Vocabulary;
import io.goobi.workflow.api.vocabulary.hateoas.VocabularyPageResult;
import io.goobi.workflow.api.vocabulary.helper.CachedLookup;
import io.goobi.workflow.api.vocabulary.helper.ExtendedVocabulary;

import javax.servlet.http.Part;
import java.util.List;
import java.util.stream.Collectors;

public class VocabularyAPI extends CRUDAPI<Vocabulary, VocabularyPageResult> {
    private static final String COMMON_ENDPOINT = "/api/v1/vocabularies";
    private static final String ALL_ENDPOINT = "/api/v1/vocabularies/all";
    private static final String FIND_INSTANCE_ENDPOINT = "/api/v1/vocabularies/find/{{0}}";
    private static final String IMPORT_CSV_ENDPOINT = "/api/v1/vocabularies/{{0}}/import/csv";
    private static final String IMPORT_EXCEL_ENDPOINT = "/api/v1/vocabularies/{{0}}/import/excel";
    private static final String INSTANCE_ENDPOINT = COMMON_ENDPOINT + "/{{0}}";

    private final CachedLookup<ExtendedVocabulary> singleLookupCache;

    public VocabularyAPI(String host, int port) {
        super(host, port, Vocabulary.class, VocabularyPageResult.class, COMMON_ENDPOINT, INSTANCE_ENDPOINT);
        this.singleLookupCache = new CachedLookup<>(id -> new ExtendedVocabulary(super.get(id)));
    }

    @Override
    public ExtendedVocabulary get(long id) {
        return this.singleLookupCache.getCached(id);
    }

    public List<ExtendedVocabulary> all() {
        return restApi.get(ALL_ENDPOINT, VocabularyPageResult.class).getContent().stream()
                .map(ExtendedVocabulary::new)
                .collect(Collectors.toList());
    }

    @Override
    public Vocabulary change(Vocabulary vocabulary) {
        Vocabulary result = super.change(vocabulary);
        this.singleLookupCache.invalidate(vocabulary.getId());
        return result;
    }

    @Override
    public void delete(Vocabulary vocabulary) {
        super.delete(vocabulary);
        this.singleLookupCache.invalidate(vocabulary.getId());
    }

    public ExtendedVocabulary findByName(String name) {
        return new ExtendedVocabulary(restApi.get(FIND_INSTANCE_ENDPOINT, Vocabulary.class, name));
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
