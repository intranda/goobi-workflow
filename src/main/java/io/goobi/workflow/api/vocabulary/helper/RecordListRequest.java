package io.goobi.workflow.api.vocabulary.helper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode
public class RecordListRequest {
    private String urlParams;
    private long vocabularyId;
}
