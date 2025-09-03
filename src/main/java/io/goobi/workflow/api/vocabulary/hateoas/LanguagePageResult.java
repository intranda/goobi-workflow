package io.goobi.workflow.api.vocabulary.hateoas;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.goobi.vocabulary.exchange.Language;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LanguagePageResult extends BasePageResult<Language> {
    @Data
    private final class EmbeddedWrapper {
        private List<Language> languageList;
    }

    //CHECKSTYLE:OFF
    // spelling is needed for automatic mapping
    private EmbeddedWrapper _embedded;
    //CHECKSTYLE:ON

    @Override
    public List<Language> getContent() {
        if (_embedded == null) {
            return Collections.emptyList();
        }
        return _embedded.getLanguageList();
    }
}
