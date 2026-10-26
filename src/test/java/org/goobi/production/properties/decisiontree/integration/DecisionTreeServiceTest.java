package org.goobi.production.properties.decisiontree.integration;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.production.properties.AccessCondition;
import org.goobi.production.properties.DisplayProperty;
import org.goobi.production.properties.Type;
import org.goobi.production.properties.decisiontree.binding.BindingConfig;
import org.goobi.production.properties.decisiontree.binding.TreeRegistry;
import org.goobi.production.properties.decisiontree.engine.TreeEngine;
import org.goobi.production.properties.decisiontree.engine.TreeEngine.LeafPath;
import org.goobi.production.properties.decisiontree.integration.DecisionTreeException.Reason;
import org.goobi.production.properties.decisiontree.model.TreeDefinition;
import org.goobi.production.properties.decisiontree.view.ViewState;
import org.goobi.production.properties.decisiontree.view.ViewState.State;
import org.goobi.production.properties.decisiontree.writer.PropertyStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

public class DecisionTreeServiceTest {

    private static final String SAMPLE = "/org/goobi/production/properties/decisiontree/goobi_decisionTrees.xml";
    private static final String NAME = "Scan Profile";
    private static final Locale EN = Locale.ENGLISH;

    /** Fake for the properties of the process; keeps everything the service writes. */
    private static class FakeStore implements PropertyStore {
        private final Map<String, String> values = new LinkedHashMap<>();

        @Override
        public Map<String, String> read(Set<String> names) {
            Map<String, String> result = new LinkedHashMap<>();
            values.forEach((k, v) -> {
                if (names.contains(k)) {
                    result.put(k, v);
                }
            });
            return result;
        }

        @Override
        public void save(String name, String value) {
            values.put(name, value);
        }

        @Override
        public void delete(String name) {
            values.remove(name);
        }
    }

    @TempDir
    Path dir;

    private final TreeEngine engine = new TreeEngine();
    private final FakeStore store = new FakeStore();
    private TreeDefinition tree;
    private DecisionTreeService service;
    private Step step;
    private AccessCondition access = AccessCondition.WRITEREQUIRED;
    private BindingConfig binding = new BindingConfig("scan-profile", null, null, Map.of());

    @BeforeEach
    public void setUp() throws Exception {
        Path file = dir.resolve("goobi_decisionTrees.xml");
        try (InputStream in = getClass().getResourceAsStream(SAMPLE)) {
            Files.write(file, in.readAllBytes());
        }
        TreeRegistry registry = new TreeRegistry(file);
        tree = registry.tree("scan-profile").orElseThrow();
        Process process = Mockito.mock(Process.class);
        step = Mockito.mock(Step.class);
        Mockito.when(step.getProzess()).thenReturn(process);
        Mockito.when(step.getTitel()).thenReturn("decide");
        service = new DecisionTreeService(registry, (s, name) -> NAME.equals(name) ? Optional.of(property()) : Optional.empty(), p -> store);
    }

    private DisplayProperty property() {
        DisplayProperty property = new DisplayProperty();
        property.setName(NAME);
        property.setType(Type.DECISIONTREE);
        property.setCurrentStepAccessCondition(access);
        property.setDecisionTreeBinding(binding);
        return property;
    }

    /** A leaf whose stored fields fit only this one way. */
    private LeafPath uniqueLeaf() {
        return engine.allPaths(tree).stream().filter(l -> engine.match(tree, l.fields()).size() == 1).findFirst().orElseThrow();
    }

    private LeafPath ambiguousLeaf() {
        return engine.allPaths(tree).stream().filter(l -> engine.match(tree, l.fields()).size() > 1).findFirst().orElseThrow();
    }

    private ViewState confirmed(LeafPath leaf) {
        return service.confirm(step, NAME, true, leaf.path(), EN);
    }

    private static void assertReason(Reason reason, Runnable action) {
        DecisionTreeException e = assertThrows(DecisionTreeException.class, action::run);
        assertEquals(reason, e.getReason(), e.getMessage());
    }

    // reading

    @Test
    public void nothingStoredStartsAtTheTopQuestion() {
        ViewState state = service.state(step, NAME, true, null, EN);
        assertEquals(State.ANSWERING, state.state());
        assertTrue(state.path().isEmpty());
    }

    @Test
    public void theGivenPathIsFollowedWhileNothingIsStored() {
        LeafPath leaf = uniqueLeaf();
        ViewState state = service.state(step, NAME, true, new int[] { leaf.path()[0] }, EN);
        assertEquals(1, state.path().size());
    }

    @Test
    public void storedResultWinsOverTheGivenPath() {
        LeafPath leaf = uniqueLeaf();
        confirmed(leaf);
        ViewState state = service.state(step, NAME, true, new int[0], EN);
        assertEquals(State.CONFIRMED, state.state());
        assertEquals(leaf.result().id(), state.result().id());
        assertEquals(leaf.path().length, state.path().size());
    }

    @Test
    public void readOnlyWhenTheUserMayNotEditOrTheStepOnlyReads() {
        assertEquals(State.READ_ONLY, service.state(step, NAME, false, null, EN).state());
        access = AccessCondition.READ;
        assertEquals(State.READ_ONLY, service.state(step, NAME, true, null, EN).state());
        access = AccessCondition.WRITE;
        assertEquals(State.ANSWERING, service.state(step, NAME, true, null, EN).state());
    }

    @Test
    public void unknownPropertyIsNotFound() {
        assertReason(Reason.NOT_FOUND, () -> service.state(step, "Other", true, null, EN));
    }

    @Test
    public void unknownTreeIsUnavailable() {
        binding = new BindingConfig("no-such-tree", null, null, Map.of());
        assertReason(Reason.UNAVAILABLE, () -> service.state(step, NAME, true, null, EN));
    }

    @Test
    public void pathThatDoesNotFitIsABadRequest() {
        assertReason(Reason.BAD_REQUEST, () -> service.state(step, NAME, true, new int[] { 99 }, EN));
        assertReason(Reason.BAD_REQUEST, () -> service.answer(step, NAME, true, new int[0], 0, 99, EN));
        assertReason(Reason.BAD_REQUEST, () -> service.answer(step, NAME, true, new int[0], 5, 0, EN));
        assertReason(Reason.BAD_REQUEST, () -> service.confirm(step, NAME, true, new int[] { 99 }, EN));
        assertReason(Reason.BAD_REQUEST, () -> service.reopen(step, NAME, true, new int[0], 3, EN));
    }

    // answering and confirming

    @Test
    public void answeringStepByStepAndConfirmingWritesTheFields() {
        LeafPath leaf = uniqueLeaf();
        int[] path = new int[0];
        ViewState state = service.state(step, NAME, true, path, EN);
        for (int i = 0; i < leaf.path().length; i++) {
            assertEquals(State.ANSWERING, state.state());
            state = service.answer(step, NAME, true, path, i, leaf.path()[i], EN);
            path = java.util.Arrays.copyOf(leaf.path(), i + 1);
            assertTrue(store.values.isEmpty(), "nothing is written before the result is confirmed");
        }
        assertEquals(State.RESULT, state.state());
        assertEquals(leaf.result().id(), state.result().id());

        ViewState done = service.confirm(step, NAME, true, path, EN);
        assertEquals(State.CONFIRMED, done.state());
        assertEquals(leaf.fields(), store.values);
    }

    @Test
    public void answerDropsTheAnswersBelow() {
        LeafPath leaf = uniqueLeaf();
        int other = leaf.path()[0] == 0 ? 1 : 0;
        ViewState state = service.answer(step, NAME, true, leaf.path(), 0, other, EN);
        assertEquals(1, state.path().size());
        assertEquals(other, state.path().get(0).answerIndex());
    }

    @Test
    public void confirmNeedsAResult() {
        LeafPath leaf = uniqueLeaf();
        assertReason(Reason.CONFLICT, () -> service.confirm(step, NAME, true, new int[0], EN));
        assertReason(Reason.CONFLICT, () -> service.confirm(step, NAME, true, new int[] { leaf.path()[0] }, EN));
        assertTrue(store.values.isEmpty());
    }

    @Test
    public void confirmTwiceAndAnswerAfterConfirmAreConflicts() {
        LeafPath leaf = uniqueLeaf();
        confirmed(leaf);
        assertReason(Reason.CONFLICT, () -> service.confirm(step, NAME, true, leaf.path(), EN));
        assertReason(Reason.CONFLICT, () -> service.answer(step, NAME, true, leaf.path(), 0, 0, EN));
        assertEquals(leaf.fields(), store.values);
    }

    @Test
    public void foreignPropertiesAreLeftAlone() {
        store.values.put("Unrelated", "keep");
        LeafPath leaf = uniqueLeaf();
        confirmed(leaf);
        service.restart(step, NAME, true, EN);
        assertEquals(Map.of("Unrelated", "keep"), store.values);
    }

    @Test
    public void mappedFieldsAreStoredUnderTheirTargetName() {
        binding = new BindingConfig("scan-profile", null, null, Map.of("Bit Depth", "BitDepth"));
        LeafPath leaf = uniqueLeaf();
        confirmed(leaf);
        assertTrue(store.values.containsKey("BitDepth"));
        assertFalse(store.values.containsKey("Bit Depth"));
        assertEquals(State.CONFIRMED, service.state(step, NAME, true, null, EN).state());
    }

    @Test
    public void ambiguousResultIsConfirmedAndListsTheCandidates() {
        LeafPath leaf = ambiguousLeaf();
        confirmed(leaf);
        ViewState state = service.state(step, NAME, true, null, EN);
        assertEquals(State.CONFIRMED, state.state());
        assertTrue(state.ambiguous().candidates().size() > 1);
    }

    // audit properties

    @Test
    public void resultIdAndPathAreWrittenOnConfirmAndRemovedAgain() {
        binding = new BindingConfig("scan-profile", "Scan Profile Id", "Scan Profile Path", Map.of());
        LeafPath leaf = uniqueLeaf();
        confirmed(leaf);
        assertEquals(leaf.result().id(), store.values.get("Scan Profile Id"));
        String json = store.values.get("Scan Profile Path");
        assertTrue(json.startsWith("[{\"question\":"), json);
        assertEquals(leaf.path().length, json.split("\"answer\"", -1).length - 1);

        service.reopen(step, NAME, true, null, 0, EN);
        assertNull(store.values.get("Scan Profile Id"));
        assertNull(store.values.get("Scan Profile Path"));
        assertTrue(store.values.isEmpty());
    }

    // changing and starting again

    @Test
    public void reopenRemovesTheStoredResultAtOnceAndShowsTheQuestion() {
        LeafPath leaf = uniqueLeaf();
        confirmed(leaf);
        ViewState state = service.reopen(step, NAME, true, new int[0], 0, EN);
        assertTrue(store.values.isEmpty());
        assertEquals(State.ANSWERING, state.state());
        assertTrue(state.path().isEmpty());
        assertEquals(0, state.open().stepIndex());
    }

    @Test
    public void reopenAtALaterStepKeepsTheEarlierAnswers() {
        LeafPath leaf = engine.allPaths(tree).stream()
                .filter(l -> l.path().length >= 2 && engine.match(tree, l.fields()).size() == 1)
                .findFirst()
                .orElseThrow();
        confirmed(leaf);
        ViewState state = service.reopen(step, NAME, true, null, 1, EN);
        assertTrue(store.values.isEmpty());
        assertEquals(1, state.path().size());
        assertEquals(leaf.path()[0], state.path().get(0).answerIndex());
        assertEquals(1, state.open().stepIndex());
    }

    @Test
    public void reopenWithoutAStoredResultFollowsTheGivenPath() {
        LeafPath leaf = uniqueLeaf();
        ViewState state = service.reopen(step, NAME, true, leaf.path(), 1, EN);
        assertEquals(1, state.path().size());
    }

    @Test
    public void reopenNeedsAKnownWayWhenTheStoredFieldsAreAmbiguousOrFitNothing() {
        confirmed(ambiguousLeaf());
        assertReason(Reason.CONFLICT, () -> service.reopen(step, NAME, true, new int[0], 0, EN));
        assertFalse(store.values.isEmpty(), "nothing is removed when the action is refused");

        store.values.put("DPI", "by hand");
        assertEquals(State.MISMATCH, service.state(step, NAME, true, null, EN).state());
        assertReason(Reason.CONFLICT, () -> service.reopen(step, NAME, true, new int[0], 0, EN));
    }

    @Test
    public void restartClearsAResultAndAMismatch() {
        confirmed(uniqueLeaf());
        assertEquals(State.ANSWERING, service.restart(step, NAME, true, EN).state());
        assertTrue(store.values.isEmpty());

        store.values.put("DPI", "by hand");
        assertEquals(State.MISMATCH, service.state(step, NAME, true, null, EN).state());
        assertEquals(State.ANSWERING, service.restart(step, NAME, true, EN).state());
        assertTrue(store.values.isEmpty());
    }

    // access

    @Test
    public void changesAreForbiddenWhenTheUserMayNotEdit() {
        LeafPath leaf = uniqueLeaf();
        assertReason(Reason.FORBIDDEN, () -> service.answer(step, NAME, false, new int[0], 0, 0, EN));
        assertReason(Reason.FORBIDDEN, () -> service.confirm(step, NAME, false, leaf.path(), EN));
        assertReason(Reason.FORBIDDEN, () -> service.reopen(step, NAME, false, leaf.path(), 0, EN));
        assertReason(Reason.FORBIDDEN, () -> service.restart(step, NAME, false, EN));
        assertTrue(store.values.isEmpty());
    }

    @Test
    public void changesAreForbiddenWhenTheStepOnlyReadsTheTree() {
        access = AccessCondition.READ;
        LeafPath leaf = uniqueLeaf();
        assertReason(Reason.FORBIDDEN, () -> service.confirm(step, NAME, true, leaf.path(), EN));
        assertTrue(store.values.isEmpty());
    }

    @Test
    public void forbiddenChangesLeaveAStoredResultUntouched() {
        LeafPath leaf = uniqueLeaf();
        confirmed(leaf);
        assertReason(Reason.FORBIDDEN, () -> service.restart(step, NAME, false, EN));
        assertReason(Reason.FORBIDDEN, () -> service.reopen(step, NAME, false, null, 0, EN));
        assertEquals(leaf.fields(), store.values);
        assertEquals(State.READ_ONLY, service.state(step, NAME, false, null, EN).state());
    }

    @Test
    public void everyResultOfTheReferenceTreeCanBeAnsweredAndConfirmed() {
        for (LeafPath leaf : engine.allPaths(tree)) {
            confirmed(leaf);
            assertEquals(leaf.fields(), store.values, leaf.result().id());
            assertEquals(State.CONFIRMED, service.state(step, NAME, true, null, EN).state());
            service.restart(step, NAME, true, EN);
        }
        assertEquals(List.of(), List.copyOf(store.values.keySet()));
    }
}
