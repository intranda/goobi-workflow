package io.goobi.workflow.api.vocabulary.helper;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import io.goobi.vocabulary.exchange.FieldDefinition;
import io.goobi.vocabulary.exchange.VocabularySchema;

/**
 * A filter on the records of a vocabulary, configured as <code>field=value</code>, e.g. <code>Aktiv=Ja</code>.
 */
public final class VocabularyFilter {

    private VocabularyFilter() {
    }

    /**
     * Translate a configured filter into the search query of the vocabulary server, which addresses the field by its id.
     *
     * @param schema the schema of the filtered vocabulary
     * @param filter the configured filter, <code>field=value</code>
     * @return the search query, empty if no filter is configured
     * @throws IllegalArgumentException if the schema has no field of the configured name
     */
    public static Optional<String> toSearchQuery(VocabularySchema schema, String filter) {
        if (StringUtils.isBlank(filter) || !filter.contains("=")) {
            return Optional.empty();
        }
        String[] parts = filter.split("=", 2);
        String fieldName = parts[0];
        FieldDefinition field = schema.getDefinitions()
                .stream()
                .filter(d -> d.getName().equals(fieldName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Field " + fieldName + " not found"));
        return Optional.of(field.getId() + ":" + parts[1]);
    }
}
