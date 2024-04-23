package io.goobi.workflow.api.vocabulary.hateoas;

import io.goobi.vocabulary.exchange.HateoasHref;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public abstract class BasePageResult<T> {
    private Map<String, HateoasHref> _links;
    private PageInformation page;
    public abstract List<T> getContent();
}
