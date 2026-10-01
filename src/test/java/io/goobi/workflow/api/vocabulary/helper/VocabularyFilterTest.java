package io.goobi.workflow.api.vocabulary.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.goobi.vocabulary.exchange.FieldDefinition;
import io.goobi.vocabulary.exchange.VocabularySchema;

public class VocabularyFilterTest {

    private VocabularySchema schema;

    @BeforeEach
    public void setUp() {
        FieldDefinition active = new FieldDefinition();
        active.setId(12L);
        active.setName("Aktiv");
        schema = new VocabularySchema();
        schema.setDefinitions(List.of(active));
    }

    @Test
    public void testFilterIsTranslatedToSearchQueryOnTheFieldId() {
        assertEquals(Optional.of("12:Ja"), VocabularyFilter.toSearchQuery(schema, "Aktiv=Ja"));
    }

    @Test
    public void testMissingFilterGivesNoSearchQuery() {
        assertEquals(Optional.empty(), VocabularyFilter.toSearchQuery(schema, null));
        assertEquals(Optional.empty(), VocabularyFilter.toSearchQuery(schema, ""));
    }

    @Test
    public void testFilterWithoutValueGivesNoSearchQuery() {
        assertEquals(Optional.empty(), VocabularyFilter.toSearchQuery(schema, "Aktiv"));
    }

    @Test
    public void testFilterOnUnknownFieldIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> VocabularyFilter.toSearchQuery(schema, "Active=Ja"));
    }
}
