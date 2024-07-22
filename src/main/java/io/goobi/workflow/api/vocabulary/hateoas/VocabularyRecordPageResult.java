package io.goobi.workflow.api.vocabulary.hateoas;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.goobi.vocabulary.exchange.VocabularyRecord;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VocabularyRecordPageResult extends BasePageResult<VocabularyRecord> {
    @Data
    private class EmbeddedWrapper {
        private List<VocabularyRecord> vocabularyRecordList;
    }

    private EmbeddedWrapper _embedded;

    public List<VocabularyRecord> getContent() {
        if (_embedded == null) {
            return Collections.emptyList();
        }
        return _embedded.getVocabularyRecordList();
    }
}
