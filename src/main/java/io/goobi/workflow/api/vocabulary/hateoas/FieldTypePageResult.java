package io.goobi.workflow.api.vocabulary.hateoas;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.goobi.vocabulary.exchange.FieldType;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FieldTypePageResult extends BasePageResult<FieldType> {
    @Data
    private final class EmbeddedWrapper {
        private List<FieldType> fieldTypeList;
    }

    //CHECKSTYLE:OFF
    // spelling is needed for automatic mapping
    private EmbeddedWrapper _embedded;
    //CHECKSTYLE:ON

    @Override
    public List<FieldType> getContent() {
        if (_embedded == null) {
            return Collections.emptyList();
        }
        return _embedded.getFieldTypeList();
    }
}
