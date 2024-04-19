package io.goobi.workflow.api.vocabulary.hateoas;

import lombok.Data;

@Data
public class PageInformation {
    private long size;
    private long totalElements;
    private long totalPages;
    private long number;
}
