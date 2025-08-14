package io.goobi.workflow.api.vocabulary.hateoas;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.goobi.vocabulary.exchange.Language;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LanguagePageResult extends BasePageResult<Language> {
    @Data
    private final class EmbeddedWrapper {
        private List<Language> languageList;
    }

    private EmbeddedWrapper _embedded;

    public List<Language> getContent() {
        if (_embedded == null) {
            return Collections.emptyList();
        }
        return _embedded.getLanguageList();
    }
}
