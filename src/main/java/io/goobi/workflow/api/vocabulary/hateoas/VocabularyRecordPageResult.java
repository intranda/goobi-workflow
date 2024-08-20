package io.goobi.workflow.api.vocabulary.hateoas;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.goobi.vocabulary.exchange.VocabularyRecord;
import io.goobi.workflow.api.vocabulary.helper.ExtendedVocabularyRecord;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VocabularyRecordPageResult extends BasePageResult<ExtendedVocabularyRecord> {
    @Data
    private class EmbeddedWrapper {
        private List<VocabularyRecord> vocabularyRecordList;
    }

    private EmbeddedWrapper _embedded;

    private List<ExtendedVocabularyRecord> content;

    public List<ExtendedVocabularyRecord> getContent() {
        if (content == null) {
            if (_embedded == null) {
                this.content = Collections.emptyList();
            } else {
                this.content = _embedded.getVocabularyRecordList().stream()
                        .map(ExtendedVocabularyRecord::new)
                        .collect(Collectors.toList());
            }
        }
        return this.content;
    }
}
