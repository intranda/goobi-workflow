package io.goobi.workflow.api.vocabulary.hateoas;

import java.util.List;
import java.util.Map;

import io.goobi.vocabulary.exchange.HateoasHref;
import lombok.Data;

@Data
public abstract class BasePageResult<T> {
    //CHECKSTYLE:OFF
    // spelling is needed for automatic mapping
    private Map<String, HateoasHref> _links;
    private PageInformation page;

    public abstract List<T> getContent();
    //CHECKSTYLE:ON
}
