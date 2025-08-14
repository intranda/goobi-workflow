package io.goobi.workflow.api.vocabulary.hateoas;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.goobi.vocabulary.exchange.VocabularySchema;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VocabularySchemaPageResult extends BasePageResult<VocabularySchema> {
    @Data
    private final class EmbeddedWrapper {
        private List<VocabularySchema> vocabularySchemaList;
    }

    private EmbeddedWrapper _embedded;

    public List<VocabularySchema> getContent() {
        if (_embedded == null) {
            return Collections.emptyList();
        }
        return _embedded.getVocabularySchemaList();
    }
}
