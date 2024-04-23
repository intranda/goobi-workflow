package io.goobi.workflow.api.vocabulary.hateoas;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.goobi.vocabulary.exchange.FieldType;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FieldTypePageResult extends BasePageResult<FieldType> {
    @Data
    private class EmbeddedWrapper {
        private List<FieldType> fieldTypeList;
    }

    private EmbeddedWrapper _embedded;

    public List<FieldType> getContent() {
        if (_embedded == null) {
            return Collections.emptyList();
        }
        return _embedded.getFieldTypeList();
    }
}
