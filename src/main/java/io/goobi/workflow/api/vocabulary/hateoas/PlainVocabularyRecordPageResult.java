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
public class PlainVocabularyRecordPageResult extends BasePageResult<VocabularyRecord> {
    @Data
    private class EmbeddedWrapper {
        private List<VocabularyRecord> vocabularyRecordList;
    }

    private EmbeddedWrapper _embedded;

    private List<VocabularyRecord> content;

    public List<VocabularyRecord> getContent() {
        if (content == null) {
            if (_embedded == null) {
                this.content = Collections.emptyList();
            } else {
                this.content = this._embedded.getVocabularyRecordList();
            }
        }
        return this.content;
    }
}
