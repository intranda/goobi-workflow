package io.goobi.workflow.api.vocabulary.hateoas;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.goobi.vocabulary.exchange.Vocabulary;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VocabularyPageResult extends BasePageResult {
    @Data
    private class EmbeddedWrapper {
        private List<Vocabulary> vocabularyList;
    }

    private EmbeddedWrapper _embedded;

    public List<Vocabulary> getContent() {
        if (_embedded == null) {
            return Collections.emptyList();
        }
        return _embedded.getVocabularyList();
    }
}
