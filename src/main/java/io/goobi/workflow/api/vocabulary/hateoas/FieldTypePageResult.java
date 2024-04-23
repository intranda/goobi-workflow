package io.goobi.workflow.api.vocabulary.hateoas;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.goobi.vocabulary.exchange.FieldType;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FieldTypePageResult extends BasePageResult{
    @Data
    private class EmbeddedWrapper {
        private List<FieldType> fieldTypeList;
    }
    private EmbeddedWrapper _embedded;

    public List<FieldType> getContent() {
        return _embedded.getFieldTypeList();
    }
}
