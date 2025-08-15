package io.goobi.workflow.api.vocabulary.hateoas;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.goobi.vocabulary.exchange.VocabularySchema;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VocabularySchemaPageResult extends BasePageResult<VocabularySchema> {
    @Data
    private final class EmbeddedWrapper {
        private List<VocabularySchema> vocabularySchemaList;
    }

    //CHECKSTYLE:OFF
    // spelling is needed for automatic mapping
    private EmbeddedWrapper _embedded;
    //CHECKSTYLE:ON

    @Override
    public List<VocabularySchema> getContent() {
        if (_embedded == null) {
            return Collections.emptyList();
        }
        return _embedded.getVocabularySchemaList();
    }
}
