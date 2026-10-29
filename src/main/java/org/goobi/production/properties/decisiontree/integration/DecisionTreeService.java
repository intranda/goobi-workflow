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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.production.properties.AccessCondition;
import org.goobi.production.properties.DisplayProperty;
import org.goobi.production.properties.PropertyParser;
import org.goobi.production.properties.decisiontree.binding.BindingConfig;
import org.goobi.production.properties.decisiontree.binding.TreeRegistry;
import org.goobi.production.properties.decisiontree.engine.TreeEngine;
import org.goobi.production.properties.decisiontree.integration.DecisionTreeException.Reason;
import org.goobi.production.properties.decisiontree.model.TreeDefinition;
import org.goobi.production.properties.decisiontree.view.ViewState;
import org.goobi.production.properties.decisiontree.view.ViewState.PathStep;
import org.goobi.production.properties.decisiontree.view.ViewState.State;
import org.goobi.production.properties.decisiontree.writer.PropertiesResultWriter;
import org.goobi.production.properties.decisiontree.writer.PropertyStore;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.sub.goobi.helper.Helper;

/**
 * Everything a client can do with a decision tree of a task, in one place and without any UI: read the state, answer, change an answer, start
 * again, confirm. The JSF box calls this, and so can a REST endpoint later; neither holds logic of its own.
 *
 * <p>
 * The service keeps no state. The stored result fields decide the state; only while nothing is stored the caller's path (the chosen answers so far)
 * counts. Changes are refused with a {@link DecisionTreeException} when the user or the access condition of the step does not allow them or when
 * they do not fit the current state.
 * </p>
 */
public class DecisionTreeService {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static DecisionTreeService instance;

    /** Finds the decision tree property of a step. */
    public interface PropertyLookup {
        Optional<DisplayProperty> find(Step step, String propertyName);
    }

    /** Everything needed for one call. */
    private record Bound(DisplayProperty property, BindingConfig binding, TreeDefinition tree, PropertyStore store, PropertiesResultWriter writer,
            boolean editable) {
    }

    private final TreeRegistry registry;
    private final PropertyLookup lookup;
    private final Function<Process, PropertyStore> stores;
    private final TreeEngine engine;
    /** Shows the labels untranslated, so what is stored does not depend on the language of the user. */
    private final TreeEngine plainEngine = new TreeEngine();

    /**
     * Creates a service that shows the labels of the tree file as they are.
     *
     * @param registry the loaded trees
     * @param lookup finds the decision tree property of a step
     * @param stores gives the property store of a process
     */
    public DecisionTreeService(TreeRegistry registry, PropertyLookup lookup, Function<Process, PropertyStore> stores) {
        this(registry, lookup, stores, null);
    }

    /**
     * Creates a service.
     *
     * @param registry the loaded trees
     * @param lookup finds the decision tree property of a step
     * @param stores gives the property store of a process
     * @param translator translates a label of the tree (used as a key) into the language of a locale and returns it unchanged if there is no
     *            translation; may be null
     */
    public DecisionTreeService(TreeRegistry registry, PropertyLookup lookup, Function<Process, PropertyStore> stores,
            BiFunction<String, Locale, String> translator) {
        this.registry = registry;
        this.lookup = lookup;
        this.stores = stores;
        this.engine = new TreeEngine(translator);
    }

    /**
     * Gives the shared service.
     *
     * @return the service that works on the configured properties and the properties in the database
     */
    public static synchronized DecisionTreeService getInstance() {
        if (instance == null) {
            instance = new DecisionTreeService(DecisionTreeValues.getInstance().getRegistry(),
                    (step, name) -> PropertyParser.getInstance()
                            .getDecisionTreePropertiesForStep(step)
                            .stream()
                            .filter(p -> name.equals(p.getName()))
                            .findFirst(),
                    ProcessPropertyStore::new,
                    // the label is the key: a message of the messages files if there is one, else the label itself
                    (label, locale) -> Helper.getString(Locale.forLanguageTag(locale.getLanguage()), label));
        }
        return instance;
    }

    private Bound bind(Step step, String propertyName, boolean userMayEdit) {
        DisplayProperty property = lookup.find(step, propertyName)
                .orElseThrow(() -> new DecisionTreeException(Reason.NOT_FOUND,
                        "The step '" + step.getTitel() + "' has no decision tree '" + propertyName + "'"));
        BindingConfig binding = property.getDecisionTreeBinding();
        TreeDefinition tree = registry.tree(binding.treeId())
                .orElseThrow(() -> new DecisionTreeException(Reason.UNAVAILABLE,
                        "The tree '" + binding.treeId() + "' cannot be used: " + String.join("; ", registry.problems(binding.treeId()))));
        PropertyStore store = stores.apply(step.getProzess());
        AccessCondition access = property.getCurrentStepAccessCondition();
        boolean writable = access == AccessCondition.WRITE || access == AccessCondition.WRITEREQUIRED;
        return new Bound(property, binding, tree, store, new PropertiesResultWriter(store, binding.fieldMap()), userMayEdit && writable);
    }

    private static void requireEditable(Bound bound) {
        if (!bound.editable()) {
            throw new DecisionTreeException(Reason.FORBIDDEN, "The decision tree '" + bound.property().getName() + "' cannot be changed here");
        }
    }

    private Map<String, String> stored(Bound bound) {
        Map<String, String> fields = new LinkedHashMap<>();
        bound.writer().read(bound.tree().ownedFields()).forEach((name, value) -> {
            if (StringUtils.isNotBlank(value)) {
                fields.put(name, value);
            }
        });
        return fields;
    }

    private ViewState view(Bound bound, int[] path, Locale locale) {
        ViewState state;
        Map<String, String> stored = stored(bound);
        if (stored.isEmpty()) {
            try {
                state = engine.view(bound.tree(), path == null ? new int[0] : path, locale);
            } catch (IllegalArgumentException e) {
                throw new DecisionTreeException(Reason.BAD_REQUEST, e.getMessage(), e);
            }
        } else {
            state = engine.restore(bound.tree(), stored, locale);
        }
        return bound.editable() ? state : state.asReadOnly();
    }

    /**
     * Shows the current state.
     *
     * @param step the task
     * @param propertyName name of the decision tree property
     * @param userMayEdit whether the user may change the task now; the access condition of the step is checked in addition
     * @param path the answers chosen so far, only used while no result is stored
     * @param locale language of the labels
     * @return the current state: the stored result if there is one, else the path
     */
    public ViewState state(Step step, String propertyName, boolean userMayEdit, int[] path, Locale locale) {
        return view(bind(step, propertyName, userMayEdit), path, locale);
    }

    /**
     * Chooses an answer at a step of the path; the answers below are dropped.
     *
     * @param step the task
     * @param propertyName name of the decision tree property
     * @param userMayEdit whether the user may change the task now; the access condition of the step is checked in addition
     * @param path the answers so far, only used while no result is stored
     * @param stepIndex position in the path
     * @param answerIndex index of the chosen answer
     * @param locale language of the labels
     * @return the new state of the tree
     * @throws DecisionTreeException FORBIDDEN if the tree cannot be changed, CONFLICT if a result is stored (change or start again first),
     *             BAD_REQUEST if the path or an index does not fit
     */
    public ViewState answer(Step step, String propertyName, boolean userMayEdit, int[] path, int stepIndex, int answerIndex, Locale locale) {
        Bound bound = bind(step, propertyName, userMayEdit);
        requireEditable(bound);
        if (!stored(bound).isEmpty()) {
            throw new DecisionTreeException(Reason.CONFLICT, "A result is stored: change an answer or start again first");
        }
        try {
            return engine.view(bound.tree(), engine.answer(bound.tree(), path == null ? new int[0] : path, stepIndex, answerIndex), locale);
        } catch (IllegalArgumentException e) {
            throw new DecisionTreeException(Reason.BAD_REQUEST, e.getMessage(), e);
        }
    }

    /**
     * Changes an earlier answer: the answers from that step on are dropped, and a stored result is removed at once so the data never holds a
     * result that does not fit the answers.
     *
     * @param step the task
     * @param propertyName name of the decision tree property
     * @param userMayEdit whether the user may change the task now; the access condition of the step is checked in addition
     * @param path the answers so far; ignored if a result is stored, because then the way is rebuilt from the stored fields
     * @param stepIndex position in the path
     * @param locale language of the labels
     * @return the new state of the tree
     * @throws DecisionTreeException FORBIDDEN if the tree cannot be changed, CONFLICT if a result is stored but its way is not known (start again),
     *             BAD_REQUEST if the step does not exist
     */
    public ViewState reopen(Step step, String propertyName, boolean userMayEdit, int[] path, int stepIndex, Locale locale) {
        Bound bound = bind(step, propertyName, userMayEdit);
        requireEditable(bound);
        Map<String, String> stored = stored(bound);
        int[] base = path == null ? new int[0] : path;
        if (!stored.isEmpty()) {
            ViewState restored = engine.restore(bound.tree(), stored, locale);
            if (restored.state() != State.CONFIRMED || restored.path().isEmpty()) {
                throw new DecisionTreeException(Reason.CONFLICT, "The way to the stored result is not known: start again");
            }
            base = restored.path().stream().mapToInt(PathStep::answerIndex).toArray();
        }
        int[] reopened;
        try {
            reopened = engine.reopen(bound.tree(), base, stepIndex, bound.writer());
            engine.view(bound.tree(), reopened, locale);
        } catch (IllegalArgumentException e) {
            throw new DecisionTreeException(Reason.BAD_REQUEST, e.getMessage(), e);
        }
        if (!stored.isEmpty()) {
            clearAudit(bound);
        }
        return engine.view(bound.tree(), reopened, locale);
    }

    /**
     * Chooses another answer at an earlier step in one call (a click on another answer of a row above): the answers from that step on are
     * dropped, a stored result is removed at once, and the new answer is taken.
     *
     * @param step the task
     * @param propertyName name of the decision tree property
     * @param userMayEdit whether the user may change the task now; the access condition of the step is checked in addition
     * @param path the answers so far; ignored if a result is stored
     * @param stepIndex position in the path
     * @param answerIndex index of the chosen answer
     * @param locale language of the labels
     * @return the new state of the tree
     * @throws DecisionTreeException as {@link #reopen} and {@link #answer}
     */
    public ViewState change(Step step, String propertyName, boolean userMayEdit, int[] path, int stepIndex, int answerIndex, Locale locale) {
        ViewState reopened = reopen(step, propertyName, userMayEdit, path, stepIndex, locale);
        int[] head = reopened.path().stream().mapToInt(PathStep::answerIndex).toArray();
        return answer(step, propertyName, userMayEdit, head, stepIndex, answerIndex, locale);
    }

    /**
     * Starts again from the top question; a stored result is removed, even if it fits no result of the tree.
     *
     * @param step the task
     * @param propertyName name of the decision tree property
     * @param userMayEdit whether the user may change the task now; the access condition of the step is checked in addition
     * @param locale language of the labels
     * @return the new state of the tree
     * @throws DecisionTreeException FORBIDDEN if the tree cannot be changed
     */
    public ViewState restart(Step step, String propertyName, boolean userMayEdit, Locale locale) {
        Bound bound = bind(step, propertyName, userMayEdit);
        requireEditable(bound);
        engine.restart(bound.tree(), bound.writer());
        clearAudit(bound);
        return engine.view(bound.tree(), new int[0], locale);
    }

    /**
     * Writes the result the path leads to: every field of the tree is set or deleted, and the optional result id and path properties are written.
     *
     * @param step the task
     * @param propertyName name of the decision tree property
     * @param userMayEdit whether the user may change the task now; the access condition of the step is checked in addition
     * @param path the answers chosen
     * @param locale language of the labels
     * @return the new state of the tree
     * @throws DecisionTreeException FORBIDDEN if the tree cannot be changed, CONFLICT if the path does not end at a result or a result is already
     *             stored, BAD_REQUEST if the path does not fit
     */
    public ViewState confirm(Step step, String propertyName, boolean userMayEdit, int[] path, Locale locale) {
        Bound bound = bind(step, propertyName, userMayEdit);
        requireEditable(bound);
        if (!stored(bound).isEmpty()) {
            throw new DecisionTreeException(Reason.CONFLICT, "A result is already stored: change an answer or start again first");
        }
        ViewState chosen;
        try {
            chosen = plainEngine.view(bound.tree(), path == null ? new int[0] : path, Locale.ROOT);
        } catch (IllegalArgumentException e) {
            throw new DecisionTreeException(Reason.BAD_REQUEST, e.getMessage(), e);
        }
        if (chosen.state() != State.RESULT) {
            throw new DecisionTreeException(Reason.CONFLICT, "The path does not end at a result");
        }
        engine.confirm(bound.tree(), path, bound.writer());
        writeAudit(bound, chosen);
        return view(bound, path, locale);
    }

    private void writeAudit(Bound bound, ViewState chosen) {
        BindingConfig binding = bound.binding();
        if (binding.resultIdProperty() != null) {
            bound.store().save(binding.resultIdProperty(), chosen.result().id());
        }
        if (binding.pathProperty() != null) {
            bound.store().save(binding.pathProperty(), pathJson(chosen));
        }
    }

    private void clearAudit(Bound bound) {
        BindingConfig binding = bound.binding();
        if (binding.resultIdProperty() != null) {
            bound.store().delete(binding.resultIdProperty());
        }
        if (binding.pathProperty() != null) {
            bound.store().delete(binding.pathProperty());
        }
    }

    /** The confirmed way as compact JSON: the question (its id, else its label) and the chosen answer per step. */
    static String pathJson(ViewState chosen) {
        List<Map<String, String>> steps = new ArrayList<>();
        for (PathStep step : chosen.path()) {
            Map<String, String> entry = new LinkedHashMap<>();
            entry.put("question", step.questionId() != null ? step.questionId() : step.label());
            entry.put("answer", step.answerLabel());
            steps.add(entry);
        }
        try {
            return MAPPER.writeValueAsString(steps);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
