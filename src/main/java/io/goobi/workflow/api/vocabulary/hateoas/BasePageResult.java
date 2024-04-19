package io.goobi.workflow.api.vocabulary.hateoas;

import io.goobi.vocabulary.exchange.HateoasHref;
import lombok.Data;

import java.util.Map;

@Data
public abstract class BasePageResult {
    private Map<String, HateoasHref> _links;
    private PageInformation page;
}
