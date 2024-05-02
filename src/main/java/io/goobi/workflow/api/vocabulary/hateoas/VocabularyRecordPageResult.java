package io.goobi.workflow.api.vocabulary.hateoas;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.goobi.workflow.api.vocabulary.jsfwrapper.JSFVocabularyRecord;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VocabularyRecordPageResult extends BasePageResult<JSFVocabularyRecord> {
    @Data
    private class EmbeddedWrapper {
        private List<JSFVocabularyRecord> vocabularyRecordList;
    }

    private EmbeddedWrapper _embedded;

    public List<JSFVocabularyRecord> getContent() {
        if (_embedded == null) {
            return Collections.emptyList();
        }
        return _embedded.getVocabularyRecordList();
    }
}
