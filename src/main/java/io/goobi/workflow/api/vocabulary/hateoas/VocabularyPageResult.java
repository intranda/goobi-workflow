package io.goobi.workflow.api.vocabulary.hateoas;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.goobi.workflow.api.vocabulary.jsfwrapper.JSFVocabulary;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VocabularyPageResult extends BasePageResult<JSFVocabulary> {
    @Data
    private class EmbeddedWrapper {
        private List<JSFVocabulary> vocabularyList;
    }

    private EmbeddedWrapper _embedded;

    public List<JSFVocabulary> getContent() {
        if (_embedded == null) {
            return Collections.emptyList();
        }
        return _embedded.getVocabularyList();
    }
}
