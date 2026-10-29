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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

import org.goobi.production.properties.decisiontree.model.Answer;
import org.goobi.production.properties.decisiontree.model.Node;
import org.goobi.production.properties.decisiontree.model.Question;
import org.goobi.production.properties.decisiontree.model.Result;
import org.goobi.production.properties.decisiontree.model.Text;
import org.goobi.production.properties.decisiontree.model.TreeDefinition;
import org.goobi.production.properties.decisiontree.view.ViewState;
import org.goobi.production.properties.decisiontree.view.ViewState.AnswerView;
import org.goobi.production.properties.decisiontree.view.ViewState.Ambiguous;
import org.goobi.production.properties.decisiontree.view.ViewState.Message;
import org.goobi.production.properties.decisiontree.view.ViewState.OpenQuestion;
import org.goobi.production.properties.decisiontree.view.ViewState.PathStep;
import org.goobi.production.properties.decisiontree.view.ViewState.ResultView;
import org.goobi.production.properties.decisiontree.view.ViewState.State;
import org.goobi.production.properties.decisiontree.writer.ResultWriter;

/**
 * The logic of a decision tree. It has no state: the caller holds the path (the indices of the chosen answers) and passes it in with every call.
 * It depends neither on JSF nor on a session, so a REST endpoint can call it the same way.
 */
public class TreeEngine {

    /**
     * A way from the top question to a result.
     *
     * @param path index of the chosen answer at each step
     * @param result the result the way ends at
     * @param fields the fields collected along the way
     */
    public record LeafPath(int[] path, Result result, Map<String, String> fields) {
    }

    /**
     * A way that fits stored fields.
     *
     * @param resultId id of the result the way ends at
     * @param path index of the chosen answer at each step
     * @param fields the fields collected along the way
     */
    public record Candidate(String resultId, int[] path, Map<String, String> fields) {
    }

    private final BiFunction<String, Locale, String> translator;

    /** An engine that shows the labels of the tree file as they are. */
    public TreeEngine() {
        this(null);
    }

    /**
     * @param translator translates a label (used as a key) into the language of a locale and returns it unchanged if it has no translation, for
     *            example by the messages files; may be null
     */
    public TreeEngine(BiFunction<String, Locale, String> translator) {
        this.translator = translator;
    }

    private String text(Text text, Locale locale) {
        return text.get(locale, translator);
    }

    private record Step(Question question, int answerIndex, Answer answer) {
    }

    private record Walk(List<Step> steps, Question open, Result leaf, Map<String, String> fields) {
    }

    /**
     * Follows a path.
     *
     * @throws IllegalArgumentException if the path does not fit the tree
     */
    private Walk walk(TreeDefinition tree, int[] path) {
        Map<String, String> fields = new LinkedHashMap<>(tree.defaults());
        List<Step> steps = new ArrayList<>();
        Node node = tree.root();
        for (int i = 0; i < path.length; i++) {
            Question question = tree.asQuestion(node);
            if (question == null) {
                throw new IllegalArgumentException("Path is longer than the tree at step " + i);
            }
            if (path[i] < 0 || path[i] >= question.answers().size()) {
                throw new IllegalArgumentException("No answer " + path[i] + " at step " + i);
            }
            Answer answer = question.answers().get(path[i]);
            fields.putAll(answer.fields());
            steps.add(new Step(question, path[i], answer));
            node = answer.next();
        }
        Question open = tree.asQuestion(node);
        Result leaf = node instanceof Result r ? r : null;
        return new Walk(steps, open, leaf, clean(fields));
    }

    /** Empty values count as not set. */
    private static Map<String, String> clean(Map<String, String> fields) {
        Map<String, String> result = new LinkedHashMap<>();
        fields.forEach((name, value) -> {
            if (value != null && !value.isBlank()) {
                result.put(name, value.trim());
            }
        });
        return result;
    }

    /**
     * Collects the fields of a path.
     *
     * @param tree the tree
     * @param path index of the chosen answer at each step
     * @return the fields collected along the path: defaults, then the fields of the answers, then those of the result if a leaf is reached
     */
    public Map<String, String> fieldsOf(TreeDefinition tree, int[] path) {
        return walk(tree, path).fields();
    }

    /**
     * Chooses an answer at a step.
     *
     * @param tree the tree
     * @param path index of the chosen answer at each step
     * @param stepIndex position in the path
     * @param answerIndex index of the chosen answer
     * @return the path after choosing an answer at a step; everything below the step is dropped
     * @throws IllegalArgumentException if the step is not an open or an answered question or the answer does not exist
     */
    public int[] answer(TreeDefinition tree, int[] path, int stepIndex, int answerIndex) {
        int[] head = back(path, stepIndex);
        Question question = walk(tree, head).open();
        if (question == null) {
            throw new IllegalArgumentException("There is no question at step " + stepIndex);
        }
        if (answerIndex < 0 || answerIndex >= question.answers().size()) {
            throw new IllegalArgumentException("No answer " + answerIndex + " at step " + stepIndex);
        }
        int[] result = Arrays.copyOf(head, head.length + 1);
        result[head.length] = answerIndex;
        return result;
    }

    /**
     * Cuts a path.
     *
     * @param path index of the chosen answer at each step
     * @param stepIndex position in the path
     * @return the path up to, but not including, the step; everything below is dropped
     */
    public int[] back(int[] path, int stepIndex) {
        if (stepIndex < 0 || stepIndex > path.length) {
            throw new IllegalArgumentException("No step " + stepIndex);
        }
        return Arrays.copyOf(path, stepIndex);
    }

    /**
     * Shows a path.
     *
     * @param tree the tree
     * @param path index of the chosen answer at each step
     * @param locale language of the labels
     * @return the view of a path: ANSWERING at an open question, RESULT at a leaf
     */
    public ViewState view(TreeDefinition tree, int[] path, Locale locale) {
        Walk walk = walk(tree, path);
        List<Message> messages = List.of();
        if (walk.leaf() != null) {
            return new ViewState(State.RESULT, pathSteps(walk, locale), null, new ResultView(walk.leaf().id(), walk.fields()), null, messages);
        }
        List<AnswerView> answers = answerViews(walk.open(), locale);
        OpenQuestion open = new OpenQuestion(path.length, walk.open().id(), text(walk.open().label(), locale), text(walk.open().hint(), locale),
                answers);
        return new ViewState(State.ANSWERING, pathSteps(walk, locale), open, null, null, messages);
    }

    private List<AnswerView> answerViews(Question question, Locale locale) {
        List<AnswerView> answers = new ArrayList<>();
        for (int i = 0; i < question.answers().size(); i++) {
            answers.add(new AnswerView(i, text(question.answers().get(i).label(), locale), question.answers().get(i).next() instanceof Result));
        }
        return answers;
    }

    private List<PathStep> pathSteps(Walk walk, Locale locale) {
        List<PathStep> steps = new ArrayList<>();
        for (int i = 0; i < walk.steps().size(); i++) {
            Step step = walk.steps().get(i);
            steps.add(new PathStep(i, step.question().id(), text(step.question().label(), locale), text(step.question().hint(), locale),
                    step.answerIndex(), text(step.answer().label(), locale), answerViews(step.question(), locale), clean(step.answer().fields())));
        }
        return steps;
    }

    /**
     * Lists all ways of a tree.
     *
     * @param tree the tree
     * @return all ways from the top question to a result, goto resolved
     * @throws IllegalStateException if the tree has more than {@link TreeDefinition#MAX_PATHS} ways
     */
    public List<LeafPath> allPaths(TreeDefinition tree) {
        List<LeafPath> paths = new ArrayList<>();
        collect(tree, tree.root(), new int[0], new LinkedHashMap<>(tree.defaults()), paths);
        return paths;
    }

    private void collect(TreeDefinition tree, Node node, int[] path, Map<String, String> fields, List<LeafPath> paths) {
        if (node instanceof Result result) {
            if (paths.size() >= TreeDefinition.MAX_PATHS) {
                throw new IllegalStateException("The tree " + tree.id() + " has more than " + TreeDefinition.MAX_PATHS + " paths");
            }
            paths.add(new LeafPath(path, result, clean(fields)));
            return;
        }
        Question question = tree.asQuestion(node);
        for (int i = 0; i < question.answers().size(); i++) {
            Answer answer = question.answers().get(i);
            Map<String, String> next = new LinkedHashMap<>(fields);
            next.putAll(answer.fields());
            int[] nextPath = Arrays.copyOf(path, path.length + 1);
            nextPath[path.length] = i;
            collect(tree, answer.next(), nextPath, next, paths);
        }
    }

    /**
     * Rebuilds the state from stored fields: all ways whose collected fields equal the stored ones for every owned field.
     *
     * @param tree the tree
     * @param stored the stored fields
     * @return the ways that fit, none if the stored fields fit no result
     */
    public List<Candidate> match(TreeDefinition tree, Map<String, String> stored) {
        Map<String, String> normalized = clean(stored);
        List<Candidate> candidates = new ArrayList<>();
        for (LeafPath leaf : allPaths(tree)) {
            boolean fits = true;
            for (String name : tree.ownedFields()) {
                if (!Objects.equals(leaf.fields().get(name), normalized.get(name))) {
                    fits = false;
                    break;
                }
            }
            if (fits) {
                candidates.add(new Candidate(leaf.result().id(), leaf.path(), leaf.fields()));
            }
        }
        return candidates;
    }

    /**
     * The state when the task is opened: ANSWERING with an empty path if nothing is stored, CONFIRMED if the stored fields fit a result, MISMATCH
     * otherwise.
     *
     * @param tree the tree
     * @param stored the stored fields
     * @param locale language of the labels
     * @return the state to show
     */
    public ViewState restore(TreeDefinition tree, Map<String, String> stored, Locale locale) {
        Map<String, String> normalized = clean(stored);
        if (normalized.isEmpty()) {
            return view(tree, new int[0], locale);
        }
        List<Candidate> candidates = match(tree, normalized);
        if (candidates.isEmpty()) {
            return new ViewState(State.MISMATCH, List.of(), null, new ResultView(null, normalized), null,
                    List.of(new Message("WARN", "decisiontree.mismatch")));
        }
        LinkedHashSet<String> ids = new LinkedHashSet<>();
        candidates.forEach(c -> ids.add(c.resultId()));
        if (candidates.size() == 1) {
            ViewState shown = view(tree, candidates.get(0).path(), locale);
            return new ViewState(State.CONFIRMED, shown.path(), null, shown.result(), null, List.of());
        }
        String id = ids.size() == 1 ? ids.iterator().next() : null;
        return new ViewState(State.CONFIRMED, List.of(), null, new ResultView(id, candidates.get(0).fields()),
                new Ambiguous(List.copyOf(ids)), List.of(new Message("INFO", "decisiontree.ambiguous")));
    }

    /**
     * Writes the result: every owned field is set or deleted.
     *
     * @param tree the tree
     * @param path index of the chosen answer at each step
     * @param writer where the fields are stored
     * @throws IllegalStateException if the path does not end at a result
     */
    public void confirm(TreeDefinition tree, int[] path, ResultWriter writer) {
        Walk walk = walk(tree, path);
        if (walk.leaf() == null) {
            throw new IllegalStateException("The path does not end at a result");
        }
        writer.write(walk.fields(), tree.ownedFields());
    }

    /**
     * Deletes the stored fields.
     *
     * @param tree the tree
     * @param writer where the fields are stored
     */
    public void clear(TreeDefinition tree, ResultWriter writer) {
        writer.clear(tree.ownedFields());
    }

    /**
     * Changes an earlier answer: the path is cut before the step and, if a result was stored, its fields are removed at once, so the data never
     * holds a result that does not fit the answers.
     *
     * @param tree the tree
     * @param path index of the chosen answer at each step
     * @param stepIndex position of the answer to change
     * @param writer where the fields are stored
     * @return the new path
     */
    public int[] reopen(TreeDefinition tree, int[] path, int stepIndex, ResultWriter writer) {
        int[] head = back(path, stepIndex);
        clearIfStored(tree, writer);
        return head;
    }

    /**
     * Starts again; stored fields are removed.
     *
     * @param tree the tree
     * @param writer where the fields are stored
     * @return the empty path
     */
    public int[] restart(TreeDefinition tree, ResultWriter writer) {
        clearIfStored(tree, writer);
        return new int[0];
    }

    private void clearIfStored(TreeDefinition tree, ResultWriter writer) {
        if (!clean(writer.read(tree.ownedFields())).isEmpty()) {
            writer.clear(tree.ownedFields());
        }
    }
}
