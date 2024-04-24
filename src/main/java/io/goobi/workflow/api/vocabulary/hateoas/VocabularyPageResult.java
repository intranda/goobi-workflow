package io.goobi.workflow.api.vocabulary.hateoas;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.goobi.workflow.api.vocabulary.objects.VocabularyDO;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VocabularyPageResult extends BasePageResult<VocabularyDO> {
    @Data
    private class EmbeddedWrapper {
        private List<VocabularyDO> vocabularyList;
    }

    private EmbeddedWrapper _embedded;

    public List<VocabularyDO> getContent() {
        if (_embedded == null) {
            return Collections.emptyList();
        }
        return _embedded.getVocabularyList();
    }
}
