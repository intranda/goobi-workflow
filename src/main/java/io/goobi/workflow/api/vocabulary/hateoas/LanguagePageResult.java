package io.goobi.workflow.api.vocabulary.hateoas;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.goobi.vocabulary.exchange.Language;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LanguagePageResult extends BasePageResult{
    @Data
    private class EmbeddedWrapper {
        private List<Language> languageList;
    }
    private EmbeddedWrapper _embedded;

    public List<Language> getContent() {
        return _embedded.getLanguageList();
    }
}
