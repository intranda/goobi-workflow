package org.goobi.production.properties.decisiontree.engine;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. - https://goobi.io - https://www.intranda.com - https://github.com/intranda/goobi-workflow
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiFunction;

import org.goobi.production.properties.decisiontree.engine.TreeEngine.Candidate;
import org.goobi.production.properties.decisiontree.engine.TreeEngine.LeafPath;
import org.goobi.production.properties.decisiontree.model.TreeDefinition;
import org.goobi.production.properties.decisiontree.parser.LoadReport;
import org.goobi.production.properties.decisiontree.parser.TreeFileParser;
import org.goobi.production.properties.decisiontree.view.ViewState;
import org.goobi.production.properties.decisiontree.view.ViewState.State;
import org.goobi.production.properties.decisiontree.writer.InMemoryResultWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TreeEngineTest {

    private static final String BASE = "/org/goobi/production/properties/decisiontree/";
    private static final List<String> SCAN_FIELDS = List.of("Scanner", "Bit Depth", "DPI", "Format", "Colour");

    private final TreeEngine engine = new TreeEngine();
    private TreeDefinition scan;

    /**
     * Small tree: "Yes" sets the colour and asks for the size; "Small" and "No" both continue with the question "size" (a goto target nested in
     * "Small"), whose results R3 and R4 give identical fields.
     */
    private static final String SMALL = "<decisionTrees><decisionTree id=\"s\"><defaults><field name=\"Format\">TIFF</field></defaults>"
            + "<question id=\"q1\" label=\"Colour?\" label-de=\"Farbe?\" hint=\"Pick one\">"
            + "<answer label=\"Yes\" label-de=\"Ja\"><field name=\"Colour\">Full</field><question label=\"Large?\">"
            + "<answer label=\"Big\"><result id=\"R1\"><field name=\"DPI\">600</field></result></answer>"
            + "<answer label=\"Small\"><question id=\"size\" label=\"Which?\">"
            + "<answer label=\"x\"><result id=\"R3\"><field name=\"DPI\">400</field></result></answer>"
            + "<answer label=\"y\"><result id=\"R4\"><field name=\"DPI\">400</field></result></answer></question></answer></question></answer>"
            + "<answer label=\"No\"><field name=\"Colour\">Gray</field><goto ref=\"size\"/></answer></question></decisionTree></decisionTrees>";

    @BeforeEach
    public void load() throws Exception {
        try (InputStream in = getClass().getResourceAsStream(BASE + "goobi_decisionTrees.xml")) {
            scan = new TreeFileParser().parse(in).trees().get("scan-profile");
        }
        assertNotNull(scan);
    }

    private TreeDefinition small() throws Exception {
        LoadReport report = new TreeFileParser().parse(new ByteArrayInputStream(SMALL.getBytes(StandardCharsets.UTF_8)));
        assertFalse(report.hasErrors(), report.messages().toString());
        return report.trees().get("s");
    }

    // 2. fields and paths

    private Set<List<String>> expectedOutcomes() throws Exception {
        Set<List<String>> expected = new TreeSet<>((a, b) -> String.join("\t", a).compareTo(String.join("\t", b)));
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(getClass().getResourceAsStream(BASE + "scan-profile-outcomes.tsv"), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("#") && !line.isBlank()) {
                    expected.add(List.of(line.split("\t", -1)));
                }
            }
        }
        return expected;
    }

    @Test
    public void referenceTreeGivesExactlyTheExpectedOutcomes() throws Exception {
        List<LeafPath> paths = engine.allPaths(scan);
        assertEquals(35, paths.size());
        Set<List<String>> got = new TreeSet<>((a, b) -> String.join("\t", a).compareTo(String.join("\t", b)));
        for (LeafPath leaf : paths) {
            List<String> row = new ArrayList<>();
            SCAN_FIELDS.forEach(f -> row.add(leaf.fields().getOrDefault(f, "")));
            got.add(row);
        }
        Set<List<String>> expected = expectedOutcomes();
        assertEquals(22, expected.size());
        assertEquals(expected, got);
    }

    @Test
    public void everyPathOfTheReferenceTreeEndsAtOneOfThe13Results() {
        Set<String> ids = new TreeSet<>();
        engine.allPaths(scan).forEach(l -> ids.add(l.result().id()));
        assertEquals(13, ids.size());
        assertEquals(scan.resultsById().keySet(), ids);
    }

    @Test
    public void fieldsAreCollectedAlongThePathAndLaterOnesOverride() throws Exception {
        TreeDefinition tree = small();
        assertEquals(Map.of("Format", "TIFF"), engine.fieldsOf(tree, new int[0]));
        assertEquals(Map.of("Format", "TIFF", "Colour", "Full"), engine.fieldsOf(tree, new int[] { 0 }));
        assertEquals(Map.of("Format", "TIFF", "Colour", "Full", "DPI", "600"), engine.fieldsOf(tree, new int[] { 0, 0 }));
    }

    @Test
    public void gotoContinuesAtTheTargetQuestion() throws Exception {
        TreeDefinition tree = small();
        ViewState view = engine.view(tree, new int[] { 1 }, Locale.ENGLISH);
        assertEquals(State.ANSWERING, view.state());
        assertEquals("size", view.open().id());
        assertEquals(2, view.open().answers().size());
        assertEquals(Map.of("Format", "TIFF", "Colour", "Gray", "DPI", "400"), engine.fieldsOf(tree, new int[] { 1, 0 }));
    }

    @Test
    public void answerDropsEverythingBelowTheStep() throws Exception {
        TreeDefinition tree = small();
        assertArrayEquals(new int[] { 1 }, engine.answer(tree, new int[] { 0, 1 }, 0, 1));
        assertArrayEquals(new int[] { 0, 0 }, engine.answer(tree, new int[] { 0 }, 1, 0));
        assertArrayEquals(new int[] { 0 }, engine.answer(tree, new int[0], 0, 0));
    }

    @Test
    public void answerRejectsWrongStepsAndAnswers() throws Exception {
        TreeDefinition tree = small();
        assertThrows(IllegalArgumentException.class, () -> engine.answer(tree, new int[] { 0 }, 2, 0));
        assertThrows(IllegalArgumentException.class, () -> engine.answer(tree, new int[] { 0 }, 1, 5));
        assertThrows(IllegalArgumentException.class, () -> engine.answer(tree, new int[] { 0, 0 }, 2, 0));
        assertThrows(IllegalArgumentException.class, () -> engine.answer(tree, new int[0], -1, 0));
        assertThrows(IllegalArgumentException.class, () -> engine.view(tree, new int[] { 9 }, Locale.ENGLISH));
        assertThrows(IllegalArgumentException.class, () -> engine.view(tree, new int[] { 0, 0, 0 }, Locale.ENGLISH));
    }

    // labels and the messages

    /** Translates a label like a messages file would: only known keys, everything else stays as it is. */
    private static BiFunction<String, Locale, String> messages(Map<String, String> german) {
        return (key, locale) -> Locale.GERMAN.getLanguage().equals(locale.getLanguage()) ? german.getOrDefault(key, key) : key;
    }

    @Test
    public void labelsAreTranslatedWhenTheTranslatorKnowsThem() throws Exception {
        TreeEngine translating = new TreeEngine(messages(Map.of("Colour?", "Farbe (Nachricht)?", "Yes", "Ja (Nachricht)", "Pick one", "Eins w\u00E4hlen")));
        ViewState view = translating.view(small(), new int[0], Locale.GERMAN);
        // the question has label-de in the file, which is more specific than a message
        assertEquals("Farbe?", view.open().label());
        // the hint and the answer without a language attribute come from the messages
        assertEquals("Eins w\u00E4hlen", view.open().hint());
        assertEquals("Ja", view.open().answers().get(0).label(), "label-de of the file wins over the message");
        assertEquals("No", view.open().answers().get(1).label(), "no message: the label itself");
    }

    @Test
    public void labelsWithoutLanguageAttributeUseTheMessageAsKey() throws Exception {
        TreeEngine translating = new TreeEngine(messages(Map.of("No", "Nein", "Big", "Gro\u00DF", "Large?", "Gro\u00DF?")));
        ViewState view = translating.view(small(), new int[] { 0 }, Locale.GERMAN);
        assertEquals("Gro\u00DF?", view.open().label());
        assertEquals("Gro\u00DF", view.open().answers().get(0).label());
        assertEquals("Small", view.open().answers().get(1).label());
        assertEquals("Ja", view.path().get(0).answerLabel());
        // another language: the translator returns the label unchanged
        assertEquals("Large?", translating.view(small(), new int[] { 0 }, Locale.ENGLISH).open().label());
    }

    @Test
    public void emptyTranslationFallsBackToTheLabel() throws Exception {
        TreeEngine translating = new TreeEngine((key, locale) -> "");
        assertEquals("No", translating.view(small(), new int[0], Locale.ENGLISH).open().answers().get(1).label());
        TreeEngine nullTranslating = new TreeEngine((key, locale) -> null);
        assertEquals("No", nullTranslating.view(small(), new int[0], Locale.ENGLISH).open().answers().get(1).label());
    }

    @Test
    public void withoutTranslatorTheLabelsAreShownAsTheyAre() throws Exception {
        ViewState view = new TreeEngine().view(small(), new int[0], Locale.ENGLISH);
        assertEquals("Colour?", view.open().label());
        assertEquals("Yes", view.open().answers().get(0).label());
    }

    @Test
    public void translationNeverChangesTheStoredFieldValues() throws Exception {
        TreeEngine translating = new TreeEngine(messages(Map.of("Big", "Gro\u00DF", "600", "sechshundert", "Full", "Voll")));
        TreeDefinition tree = small();
        assertEquals(engine.fieldsOf(tree, new int[] { 0, 0 }), translating.fieldsOf(tree, new int[] { 0, 0 }));
        ViewState view = translating.view(tree, new int[] { 0, 0 }, Locale.GERMAN);
        assertEquals("600", view.result().fields().get("DPI"));
        assertEquals("Full", view.path().get(0).decided().get("Colour"));
    }

    // 3. view state

    @Test
    public void viewOfAnOpenQuestion() throws Exception {
        ViewState view = engine.view(small(), new int[0], Locale.GERMAN);
        assertEquals(State.ANSWERING, view.state());
        assertTrue(view.path().isEmpty());
        assertEquals("Farbe?", view.open().label());
        assertEquals("Pick one", view.open().hint());
        assertEquals("Ja", view.open().answers().get(0).label());
        assertEquals("No", view.open().answers().get(1).label());
        assertEquals(0, view.open().stepIndex());
        assertNull(view.result());
    }

    @Test
    public void viewOfALeaf() throws Exception {
        ViewState view = engine.view(small(), new int[] { 0, 0 }, Locale.ENGLISH);
        assertEquals(State.RESULT, view.state());
        assertEquals(2, view.path().size());
        assertEquals("q1", view.path().get(0).questionId());
        assertEquals("Yes", view.path().get(0).answerLabel());
        assertEquals(Map.of("Colour", "Full"), view.path().get(0).decided());
        assertEquals("Big", view.path().get(1).answerLabel());
        assertEquals("R1", view.result().id());
        assertEquals(Map.of("Format", "TIFF", "Colour", "Full", "DPI", "600"), view.result().fields());
        assertNull(view.open());
    }

    @Test
    public void viewStateCanBeSerializedToJsonAndRead() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        for (int[] path : new int[][] { {}, { 0 }, { 0, 1 } }) {
            ViewState view = engine.view(small(), path, Locale.ENGLISH);
            String json = mapper.writeValueAsString(view);
            Map<?, ?> parsed = mapper.readValue(json, Map.class);
            assertEquals(view.state().name(), parsed.get("state"));
        }
        ViewState restored = engine.restore(scan, Map.of("Scanner", "none"), Locale.ENGLISH);
        assertEquals(State.MISMATCH.name(), mapper.readValue(mapper.writeValueAsString(restored), Map.class).get("state"));
    }

    @Test
    public void readOnlyKeepsTheContent() throws Exception {
        ViewState view = engine.view(small(), new int[] { 0, 1 }, Locale.ENGLISH);
        ViewState readOnly = view.asReadOnly();
        assertEquals(State.READ_ONLY, readOnly.state());
        assertEquals(view.result(), readOnly.result());
        assertEquals(view.path(), readOnly.path());
    }

    // 4. restoring from stored fields

    private int[] firstPathTo(TreeDefinition tree, String resultId) {
        return engine.allPaths(tree).stream().filter(l -> resultId.equals(l.result().id())).findFirst().orElseThrow().path();
    }

    @Test
    public void nothingStoredStartsAtTheTop() {
        ViewState view = engine.restore(scan, Map.of(), Locale.ENGLISH);
        assertEquals(State.ANSWERING, view.state());
        assertTrue(view.path().isEmpty());
    }

    @Test
    public void blankValuesCountAsNotStored() {
        assertEquals(State.ANSWERING, engine.restore(scan, Map.of("Scanner", "  ", "DPI", ""), Locale.ENGLISH).state());
    }

    @Test
    public void storedFieldsOfAUniquePathAreRestoredWithThePath() throws Exception {
        TreeDefinition tree = small();
        Map<String, String> stored = engine.fieldsOf(tree, new int[] { 0, 0 });
        ViewState view = engine.restore(tree, stored, Locale.ENGLISH);
        assertEquals(State.CONFIRMED, view.state());
        assertEquals(2, view.path().size());
        assertEquals("R1", view.result().id());
        assertNull(view.ambiguous());
    }

    @Test
    public void everyOutcomeOfTheReferenceTreeIsRestorable() {
        for (LeafPath leaf : engine.allPaths(scan)) {
            ViewState view = engine.restore(scan, leaf.fields(), Locale.ENGLISH);
            assertEquals(State.CONFIRMED, view.state(), leaf.result().id());
            assertEquals(leaf.fields(), view.result().fields());
        }
    }

    @Test
    public void severalWaysToTheSameResultAreAmbiguousButConfirmed() throws Exception {
        TreeDefinition tree = small();
        // R3 and R4 give the same fields; over the goto from "No" the colour is Gray, so only these two ways fit
        Map<String, String> stored = engine.fieldsOf(tree, new int[] { 1, 0 });
        List<Candidate> candidates = engine.match(tree, stored);
        assertEquals(2, candidates.size());
        ViewState view = engine.restore(tree, stored, Locale.ENGLISH);
        assertEquals(State.CONFIRMED, view.state());
        assertTrue(view.path().isEmpty(), "the way cannot be shown");
        assertEquals(List.of("R3", "R4"), view.ambiguous().candidates());
        assertNull(view.result().id());
        assertEquals(stored, view.result().fields());
    }

    @Test
    public void referenceTreeHasAmbiguousResultsWithDifferentIds() {
        // BND-COLOUR and REF-COLOUR give identical fields
        int ambiguous = 0;
        for (LeafPath leaf : engine.allPaths(scan)) {
            ViewState view = engine.restore(scan, leaf.fields(), Locale.ENGLISH);
            if (view.ambiguous() != null && view.ambiguous().candidates().size() > 1) {
                ambiguous++;
            }
        }
        assertTrue(ambiguous > 0);
        ViewState view = engine.restore(scan, engine.fieldsOf(scan, firstPathTo(scan, "BND-COLOUR")), Locale.ENGLISH);
        assertTrue(view.ambiguous().candidates().containsAll(List.of("BND-COLOUR", "REF-COLOUR")), view.ambiguous().toString());
    }

    @Test
    public void storedFieldsThatFitNoResultAreAMismatch() {
        Map<String, String> stored = new LinkedHashMap<>(engine.allPaths(scan).get(0).fields());
        stored.put("DPI", "edited by hand");
        ViewState view = engine.restore(scan, stored, Locale.ENGLISH);
        assertEquals(State.MISMATCH, view.state());
        assertEquals(stored, view.result().fields());
        assertEquals("decisiontree.mismatch", view.messages().get(0).key());
    }

    @Test
    public void aMissingFieldIsAMismatch() {
        Map<String, String> stored = new LinkedHashMap<>(engine.allPaths(scan).get(0).fields());
        stored.remove("Colour");
        assertEquals(State.MISMATCH, engine.restore(scan, stored, Locale.ENGLISH).state());
    }

    @Test
    public void foreignFieldsAreIgnoredWhenMatching() throws Exception {
        TreeDefinition tree = small();
        Map<String, String> stored = new LinkedHashMap<>(engine.fieldsOf(tree, new int[] { 0, 0 }));
        stored.put("Unrelated", "value");
        assertEquals(State.CONFIRMED, engine.restore(tree, stored, Locale.ENGLISH).state());
    }

    // 5. writer

    @Test
    public void confirmSetsEveryOwnedFieldAndDeletesTheOthers() throws Exception {
        TreeDefinition tree = small();
        InMemoryResultWriter writer = new InMemoryResultWriter();
        writer.store().put("Unrelated", "keep");
        engine.confirm(tree, new int[] { 0, 0 }, writer);
        assertEquals(Map.of("Format", "TIFF", "Colour", "Full", "DPI", "600", "Unrelated", "keep"), writer.store());
    }

    @Test
    public void confirmingAnotherResultReplacesTheOldValuesCompletely() {
        InMemoryResultWriter writer = new InMemoryResultWriter();
        int[] first = firstPathTo(scan, "BND-COLOUR");
        engine.confirm(scan, first, writer);
        for (LeafPath other : engine.allPaths(scan)) {
            engine.confirm(scan, other.path(), writer);
            assertEquals(other.fields(), writer.read(scan.ownedFields()), other.result().id());
        }
    }

    @Test
    public void confirmNeedsALeaf() throws Exception {
        TreeDefinition tree = small();
        assertThrows(IllegalStateException.class, () -> engine.confirm(tree, new int[] { 0 }, new InMemoryResultWriter()));
    }

    @Test
    public void confirmedFieldsAreRestoredFromTheWriter() {
        InMemoryResultWriter writer = new InMemoryResultWriter();
        for (LeafPath leaf : engine.allPaths(scan)) {
            engine.confirm(scan, leaf.path(), writer);
            assertEquals(State.CONFIRMED, engine.restore(scan, writer.read(scan.ownedFields()), Locale.ENGLISH).state());
        }
    }

    @Test
    public void changingAnAnswerAfterConfirmRemovesTheFieldsAtOnce() throws Exception {
        TreeDefinition tree = small();
        InMemoryResultWriter writer = new InMemoryResultWriter();
        writer.store().put("Unrelated", "keep");
        int[] path = { 0, 0 };
        engine.confirm(tree, path, writer);
        int[] reopened = engine.reopen(tree, path, 0, writer);
        assertEquals(0, reopened.length);
        assertEquals(Map.of("Unrelated", "keep"), writer.store());
        assertEquals(State.ANSWERING, engine.restore(tree, writer.read(tree.ownedFields()), Locale.ENGLISH).state());
    }

    @Test
    public void reopeningWithNothingStoredDoesNotTouchTheWriter() throws Exception {
        TreeDefinition tree = small();
        InMemoryResultWriter writer = new InMemoryResultWriter() {
            @Override
            public void clear(Set<String> owned) {
                throw new AssertionError("nothing was stored, so nothing must be cleared");
            }
        };
        assertArrayEquals(new int[] { 0 }, engine.reopen(tree, new int[] { 0, 1 }, 1, writer));
    }

    @Test
    public void restartClearsTheStoredResult() throws Exception {
        TreeDefinition tree = small();
        InMemoryResultWriter writer = new InMemoryResultWriter();
        engine.confirm(tree, new int[] { 0, 0 }, writer);
        assertEquals(0, engine.restart(tree, writer).length);
        assertTrue(writer.store().isEmpty());
    }

    @Test
    public void restartAlsoClearsAMismatch() throws Exception {
        TreeDefinition tree = small();
        InMemoryResultWriter writer = new InMemoryResultWriter();
        writer.store().put("DPI", "by hand");
        assertEquals(State.MISMATCH, engine.restore(tree, writer.read(tree.ownedFields()), Locale.ENGLISH).state());
        engine.restart(tree, writer);
        assertTrue(writer.store().isEmpty());
    }

    @Test
    public void blankFieldValuesAreNotWritten() throws Exception {
        String xml = "<decisionTrees><decisionTree id=\"b\"><question label=\"Q\"><answer label=\"Yes\"><result id=\"r\"><field name=\"A\">1</field>"
                + "<field name=\"B\">  </field></result></answer><answer label=\"No\"><result id=\"s\"><field name=\"A\">2</field><field name=\"B\">x</field></result></answer>"
                + "</question></decisionTree></decisionTrees>";
        TreeDefinition tree = new TreeFileParser().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))).trees().get("b");
        InMemoryResultWriter writer = new InMemoryResultWriter();
        engine.confirm(tree, new int[] { 1 }, writer);
        assertEquals(Map.of("A", "2", "B", "x"), writer.store());
        engine.confirm(tree, new int[] { 0 }, writer);
        assertEquals(Map.of("A", "1"), writer.store());
    }
}
