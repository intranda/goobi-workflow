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
 *
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
package org.goobi.managedbeans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.goobi.beans.Step;
import org.goobi.production.properties.DisplayProperty;
import org.goobi.production.properties.decisiontree.integration.DecisionTreeException;
import org.goobi.production.properties.decisiontree.integration.DecisionTreeService;
import org.goobi.production.properties.decisiontree.view.ViewState;
import org.goobi.production.properties.decisiontree.view.ViewState.PathStep;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.enums.StepStatus;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.extern.log4j.Log4j2;

/**
 * The decision trees in the task view. The bean only holds the path the user has chosen so far and hands every action to the
 * {@link DecisionTreeService}; there is no decision tree logic in here, so a REST endpoint can call the same service.
 */
@Named("DecisionTreeForm")
@WindowScoped
@Log4j2
public class DecisionTreeBean implements Serializable {

    private static final long serialVersionUID = -5614318492067151809L;

    private static final String VIEWS_ATTRIBUTE = DecisionTreeBean.class.getName() + ".views";

    /** A name and its value, as the page lists fields. The page cannot iterate a map in a repeat, so the fields come as lists. */
    public record Field(String name, String value) {
    }

    /** An answered question with the fields its answer decides. */
    public record Row(PathStep step, List<Field> decided) {
    }

    /**
     * A decision tree of the task as the page shows it.
     *
     * @param name name of the property
     * @param view the state
     * @param rows the answered questions
     * @param fields the fields of the result, if there is one
     */
    public record TreeView(String name, ViewState view, List<Row> rows, List<Field> fields) {

        static TreeView of(String name, ViewState view) {
            List<Row> rows = view.path().stream().map(step -> new Row(step, fields(step.decided()))).toList();
            return new TreeView(name, view, rows, view.result() == null ? List.of() : fields(view.result().fields()));
        }

        private static List<Field> fields(Map<String, String> map) {
            return map.entrySet().stream().map(e -> new Field(e.getKey(), e.getValue())).toList();
        }
    }

    @Inject // NOSONAR needs to be a field injection, as the bean constructor does not allow arguments
    private StepBean stepBean;

    @Inject // NOSONAR needs to be a field injection, as the bean constructor does not allow arguments
    private LoginBean loginBean;

    /** The answers chosen so far, per decision tree, for the task that is open. Only used while no result is stored. */
    private Integer pathStepId;
    private final Map<String, int[]> paths = new HashMap<>();

    private Step step() {
        return stepBean.getMySchritt();
    }

    /** The paths belong to one task; they are dropped when another task is open. */
    private void checkStep() {
        Integer id = step() == null ? null : step().getId();
        if (id == null ? pathStepId != null : !id.equals(pathStepId)) {
            paths.clear();
            pathStepId = id;
        }
    }

    /** @return whether the user works on the task right now; a closed task or the task of someone else is only shown */
    boolean userMayEdit() {
        Step step = step();
        return step != null && step.getBearbeitungsstatusEnum() == StepStatus.INWORK && step.getBearbeitungsbenutzer() != null
                && loginBean.getMyBenutzer() != null && step.getBearbeitungsbenutzer().getId().equals(loginBean.getMyBenutzer().getId());
    }

    /** @return whether the task has a decision tree, so the page can leave the box out */
    public boolean isAvailable() {
        return !stepBean.getDecisionTreeProperties().isEmpty();
    }

    /**
     * @return the decision trees of the open task with their current state. The state is read once per request: the page asks for several parts
     *         of it, and every action drops it.
     */
    @SuppressWarnings("unchecked")
    public List<TreeView> getTrees() {
        checkStep();
        Map<Object, Object> request = FacesContext.getCurrentInstance().getAttributes();
        List<TreeView> trees = (List<TreeView>) request.get(VIEWS_ATTRIBUTE);
        if (trees == null) {
            trees = new ArrayList<>();
            for (DisplayProperty property : stepBean.getDecisionTreeProperties()) {
                try {
                    trees.add(TreeView.of(property.getName(), DecisionTreeService.getInstance()
                            .state(step(), property.getName(), userMayEdit(), paths.get(property.getName()), Helper.getSessionLocale())));
                } catch (DecisionTreeException e) {
                    log.warn("Decision tree '{}' cannot be shown: {}", property.getName(), e.getMessage());
                    Helper.setFehlerMeldung("decisiontree.error." + e.getReason().name());
                }
            }
            request.put(VIEWS_ATTRIBUTE, trees);
        }
        return trees;
    }

    /** Chooses an answer in the open question or at an earlier step. */
    public void answer(String name, int stepIndex, int answerIndex) {
        run(name, () -> DecisionTreeService.getInstance()
                .answer(step(), name, userMayEdit(), paths.get(name), stepIndex, answerIndex, Helper.getSessionLocale()));
    }

    /** Changes an earlier answer: everything below it is dropped, and a stored result is removed. */
    public void reopen(String name, int stepIndex) {
        run(name, () -> DecisionTreeService.getInstance()
                .reopen(step(), name, userMayEdit(), paths.get(name), stepIndex, Helper.getSessionLocale()));
    }

    /** Writes the result the chosen answers lead to. */
    public void confirm(String name) {
        run(name, () -> DecisionTreeService.getInstance()
                .confirm(step(), name, userMayEdit(), paths.get(name), Helper.getSessionLocale()));
    }

    /** Starts again from the first question; a stored result is removed. */
    public void restart(String name) {
        run(name, () -> DecisionTreeService.getInstance().restart(step(), name, userMayEdit(), Helper.getSessionLocale()));
    }

    private void run(String name, java.util.function.Supplier<ViewState> action) {
        checkStep();
        try {
            ViewState state = action.get();
            paths.put(name, state.path().stream().mapToInt(PathStep::answerIndex).toArray());
        } catch (DecisionTreeException e) {
            log.warn("Decision tree '{}': {}", name, e.getMessage());
            Helper.setFehlerMeldung("decisiontree.error." + e.getReason().name());
        }
        FacesContext.getCurrentInstance().getAttributes().remove(VIEWS_ATTRIBUTE);
    }
}
