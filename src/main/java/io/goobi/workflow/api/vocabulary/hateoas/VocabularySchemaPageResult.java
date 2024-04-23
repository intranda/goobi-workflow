package io.goobi.workflow.api.vocabulary.hateoas;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.goobi.vocabulary.exchange.Language;
import io.goobi.vocabulary.exchange.VocabularySchema;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VocabularySchemaPageResult extends BasePageResult{
    @Data
    private class EmbeddedWrapper {
        private List<VocabularySchema> vocabularySchemaList;
    }
    private EmbeddedWrapper _embedded;

    public List<VocabularySchema> getContent() {
        return _embedded.getVocabularySchemaList();
    }
}
