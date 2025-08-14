package io.goobi.workflow.api.vocabulary.hateoas;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.goobi.vocabulary.exchange.Vocabulary;
import io.goobi.workflow.api.vocabulary.helper.ExtendedVocabulary;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VocabularyPageResult extends BasePageResult<ExtendedVocabulary> {
    @Data
    private final class EmbeddedWrapper {
        private List<Vocabulary> vocabularyList;
    }

    private List<ExtendedVocabulary> content;

    private EmbeddedWrapper _embedded;

    public List<ExtendedVocabulary> getContent() {
        if (content == null) {
            if (_embedded == null) {
                this.content = Collections.emptyList();
            } else {
                this.content = _embedded.getVocabularyList().stream()
                        .map(ExtendedVocabulary::new)
                        .collect(Collectors.toList());
            }
        }
        return this.content;
    }
}
