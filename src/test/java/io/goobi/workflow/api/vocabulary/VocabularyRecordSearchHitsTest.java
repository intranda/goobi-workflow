/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 *
 * Visit the websites for more information.
 *          - https://goobi.io
 *          - https://www.intranda.com
 *          - https://github.com/intranda/goobi-workflow
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package io.goobi.workflow.api.vocabulary;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import de.sub.goobi.AbstractTest;
import io.goobi.workflow.api.vocabulary.VocabularyRecordAPI.VocabularyRecordQueryBuilder;
import io.goobi.workflow.api.vocabulary.hateoas.VocabularyRecordPageResult;
import io.goobi.workflow.api.vocabulary.helper.ExtendedVocabularyRecord;
import jakarta.faces.model.SelectItem;

/**
 * A search on a vocabulary returns every matching record, at any level of a hierarchical vocabulary, so the records of a filtered dropdown are
 * exactly the hits. Loading their children as well, as for an unfiltered list of top level records, listed children twice and offered children
 * that do not match.
 */
public class VocabularyRecordSearchHitsTest extends AbstractTest {

    /** Creates a record; parents are given from the top level down. */
    private static ExtendedVocabularyRecord record(long id, String name, List<ExtendedVocabularyRecord> parents, Long... children) {
        ExtendedVocabularyRecord rec = Mockito.mock(ExtendedVocabularyRecord.class);
        Mockito.when(rec.getId()).thenReturn(id);
        Mockito.when(rec.getMainValue()).thenReturn(name);
        Mockito.when(rec.getParents()).thenReturn(parents);
        Mockito.when(rec.getLevel()).thenReturn(parents.size());
        Mockito.when(rec.getSelectItemLabel()).thenReturn("--".repeat(parents.size()) + (parents.isEmpty() ? "" : " ") + name);
        Mockito.when(rec.getChildren()).thenReturn(children.length == 0 ? null : new LinkedHashSet<>(Arrays.asList(children)));
        return rec;
    }

    private static List<Long> ids(List<ExtendedVocabularyRecord> records) {
        return records.stream().map(ExtendedVocabularyRecord::getId).toList();
    }

    @Test
    public void testMatchingChildrenOfAMatchingRecordAreListedOnce() {
        ExtendedVocabularyRecord allgemein = record(94, "Allgemein", List.of(), 96L, 95L);
        ExtendedVocabularyRecord first = record(95, "Allgemeine Lieferung 01", List.of(allgemein));
        ExtendedVocabularyRecord second = record(96, "Allgemeine Lieferung 02", List.of(allgemein));

        List<ExtendedVocabularyRecord> arranged = VocabularyRecordAPI.arrangeSearchHits(List.of(second, allgemein, first));

        assertEquals(List.of(94L, 95L, 96L), ids(arranged));
    }

    @Test
    public void testChildrenThatDoNotMatchAreNotListed() {
        ExtendedVocabularyRecord project = record(1, "Project", List.of(), 2L, 3L);
        ExtendedVocabularyRecord active = record(2, "Active delivery", List.of(project));

        List<ExtendedVocabularyRecord> arranged = VocabularyRecordAPI.arrangeSearchHits(List.of(project, active));

        assertEquals(List.of(1L, 2L), ids(arranged));
    }

    @Test
    public void testMatchingChildOfANonMatchingParentIsListed() {
        ExtendedVocabularyRecord allgemein = record(94, "Allgemein", List.of());
        ExtendedVocabularyRecord altesProjekt = record(97, "Altes Projekt", List.of(), 98L);
        ExtendedVocabularyRecord alteLieferung = record(98, "Alte Lieferung 01", List.of(altesProjekt));

        List<ExtendedVocabularyRecord> arranged = VocabularyRecordAPI.arrangeSearchHits(List.of(alteLieferung, allgemein));

        assertEquals(List.of(94L, 98L), ids(arranged));
    }

    @Test
    public void testHitsAreListedDepthFirstBelowTheirNearestMatchingAncestorSortedByName() {
        ExtendedVocabularyRecord ub = record(61, "UB", List.of(), 64L, 62L);
        ExtendedVocabularyRecord maelzer = record(64, "Mälzerstraße", List.of(ub), 65L, 66L);
        ExtendedVocabularyRecord r115 = record(66, "R115", List.of(ub, maelzer));
        ExtendedVocabularyRecord r114 = record(65, "R114", List.of(ub, maelzer));
        ExtendedVocabularyRecord bhg = record(62, "BHG", List.of(ub));
        ExtendedVocabularyRecord herbarium = record(67, "Herbarium", List.of());

        // Mälzerstraße does not match: its rooms are listed below UB
        List<ExtendedVocabularyRecord> arranged = VocabularyRecordAPI.arrangeSearchHits(List.of(r115, ub, herbarium, r114, bhg));

        assertEquals(List.of(67L, 61L, 62L, 65L, 66L), ids(arranged));
    }

    @Test
    public void testFilteredSelectItemsListEachHitOnceWithItsLabel() {
        ExtendedVocabularyRecord allgemein = record(94, "Allgemein", List.of(), 96L, 95L);
        ExtendedVocabularyRecord first = record(95, "Allgemeine Lieferung 01", List.of(allgemein));
        ExtendedVocabularyRecord second = record(96, "Allgemeine Lieferung 02", List.of(allgemein));
        VocabularyRecordPageResult result = Mockito.mock(VocabularyRecordPageResult.class);
        Mockito.when(result.getContent()).thenReturn(List.of(allgemein, first, second));
        VocabularyRecordQueryBuilder query = Mockito.mock(VocabularyRecordQueryBuilder.class);
        Mockito.when(query.hasSearch()).thenReturn(true);
        Mockito.when(query.all()).thenReturn(query);
        Mockito.when(query.request()).thenReturn(result);

        List<SelectItem> items = new VocabularyRecordAPI("http://localhost:8081").getRecordSelectItems(query);

        assertEquals(List.of("94", "95", "96"), items.stream().map(SelectItem::getValue).toList());
        assertEquals(List.of("Allgemein", "-- Allgemeine Lieferung 01", "-- Allgemeine Lieferung 02"),
                items.stream().map(SelectItem::getLabel).toList());
    }
}
