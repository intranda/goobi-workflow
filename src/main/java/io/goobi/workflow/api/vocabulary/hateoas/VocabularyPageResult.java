package io.goobi.workflow.api.vocabulary.hateoas;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.goobi.vocabulary.exchange.Vocabulary;
import io.goobi.workflow.api.vocabulary.helper.ExtendedVocabulary;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VocabularyPageResult extends BasePageResult<ExtendedVocabulary> {
    @Data
    private final class EmbeddedWrapper {
        private List<Vocabulary> vocabularyList;
    }

    //CHECKSTYLE:OFF
    // spelling is needed for automatic mapping
    private List<ExtendedVocabulary> content;

    private EmbeddedWrapper _embedded;
    //CHECKSTYLE:ON

    @Override
    public List<ExtendedVocabulary> getContent() {
        if (content == null) {
            if (_embedded == null) {
                this.content = Collections.emptyList();
            } else {
                this.content = _embedded.getVocabularyList()
                        .stream()
                        .map(ExtendedVocabulary::new)
                        .collect(Collectors.toList());
            }
        }
        return this.content;
    }
}
