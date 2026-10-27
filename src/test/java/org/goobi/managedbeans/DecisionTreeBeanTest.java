package org.goobi.managedbeans;

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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.goobi.beans.Step;
import org.goobi.beans.User;
import org.goobi.managedbeans.DecisionTreeBean.TreeView;
import org.goobi.production.properties.DisplayProperty;
import org.goobi.production.properties.decisiontree.integration.DecisionTreeException;
import org.goobi.production.properties.decisiontree.integration.DecisionTreeException.Reason;
import org.goobi.production.properties.decisiontree.integration.DecisionTreeService;
import org.goobi.production.properties.decisiontree.view.ViewState;
import org.goobi.production.properties.decisiontree.view.ViewState.PathStep;
import org.goobi.production.properties.decisiontree.view.ViewState.ResultView;
import org.goobi.production.properties.decisiontree.view.ViewState.State;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.enums.StepStatus;
import jakarta.faces.context.FacesContext;

public class DecisionTreeBeanTest {

    private static final String NAME = "Scan Profile";

    private DecisionTreeBean bean;
    private StepBean stepBean;
    private Step step;
    private User user;
    private DecisionTreeService service;
    private Map<Object, Object> requestAttributes;
    private MockedStatic<FacesContext> faces;
    private MockedStatic<Helper> helper;
    private MockedStatic<DecisionTreeService> services;

    private static void inject(Object target, String field, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }

    @BeforeEach
    public void setUp() throws Exception {
        user = Mockito.mock(User.class);
        Mockito.when(user.getId()).thenReturn(7);
        step = Mockito.mock(Step.class);
        Mockito.when(step.getId()).thenReturn(100);
        Mockito.when(step.getBearbeitungsstatusEnum()).thenReturn(StepStatus.INWORK);
        Mockito.when(step.getBearbeitungsbenutzer()).thenReturn(user);
        DisplayProperty property = new DisplayProperty();
        property.setName(NAME);
        stepBean = Mockito.mock(StepBean.class);
        Mockito.when(stepBean.getMySchritt()).thenReturn(step);
        Mockito.when(stepBean.getDecisionTreeProperties()).thenReturn(List.of(property));
        LoginBean login = Mockito.mock(LoginBean.class);
        Mockito.when(login.getMyBenutzer()).thenReturn(user);
        bean = new DecisionTreeBean();
        inject(bean, "stepBean", stepBean);
        inject(bean, "loginBean", login);

        service = Mockito.mock(DecisionTreeService.class);
        services = Mockito.mockStatic(DecisionTreeService.class);
        services.when(DecisionTreeService::getInstance).thenReturn(service);

        requestAttributes = new HashMap<>();
        FacesContext context = Mockito.mock(FacesContext.class);
        Mockito.when(context.getAttributes()).thenReturn(requestAttributes);
        faces = Mockito.mockStatic(FacesContext.class);
        faces.when(FacesContext::getCurrentInstance).thenReturn(context);
        helper = Mockito.mockStatic(Helper.class);
        helper.when(Helper::getSessionLocale).thenReturn(Locale.ENGLISH);
    }

    @AfterEach
    public void tearDown() {
        helper.close();
        faces.close();
        services.close();
    }

    private static ViewState answering(int... path) {
        PathStep[] steps = new PathStep[path.length];
        for (int i = 0; i < path.length; i++) {
            steps[i] = new PathStep(i, "q" + i, "Question " + i, path[i], "Answer " + path[i], Map.of("Field" + i, "value" + i));
        }
        return new ViewState(State.ANSWERING, List.of(steps), null, null, null, List.of());
    }

    private void newRequest() {
        requestAttributes.clear();
    }

    @Test
    public void treesAreReadFromTheServiceWithoutAPathAtFirst() {
        Mockito.when(service.state(any(), eq(NAME), anyBoolean(), any(), any())).thenReturn(answering());
        List<TreeView> trees = bean.getTrees();
        assertEquals(1, trees.size());
        assertEquals(NAME, trees.get(0).name());
        Mockito.verify(service).state(step, NAME, true, null, Locale.ENGLISH);
    }

    @Test
    public void stateIsReadOncePerRequest() {
        Mockito.when(service.state(any(), eq(NAME), anyBoolean(), any(), any())).thenReturn(answering());
        bean.getTrees();
        bean.getTrees();
        bean.getTrees();
        Mockito.verify(service, Mockito.times(1)).state(any(), any(), anyBoolean(), any(), any());
        newRequest();
        bean.getTrees();
        Mockito.verify(service, Mockito.times(2)).state(any(), any(), anyBoolean(), any(), any());
    }

    @Test
    public void rowsAndFieldsAreListsForThePage() {
        ViewState view = new ViewState(State.RESULT, answering(1, 0).path(), null, new ResultView("R1", new java.util.LinkedHashMap<>(Map.of("A", "1"))), null,
                List.of());
        Mockito.when(service.state(any(), eq(NAME), anyBoolean(), any(), any())).thenReturn(view);
        TreeView tree = bean.getTrees().get(0);
        assertEquals(2, tree.rows().size());
        assertEquals(List.of(new DecisionTreeBean.Field("Field0", "value0")), tree.rows().get(0).decided());
        assertEquals(List.of(new DecisionTreeBean.Field("A", "1")), tree.fields());
    }

    @Test
    public void noFieldsWithoutAResult() {
        Mockito.when(service.state(any(), eq(NAME), anyBoolean(), any(), any())).thenReturn(answering());
        assertTrue(bean.getTrees().get(0).fields().isEmpty());
    }

    @Test
    public void answerPassesOnTheChosenPathAndKeepsTheNewPath() {
        Mockito.when(service.answer(any(), eq(NAME), anyBoolean(), any(), anyInt(), anyInt(), any())).thenReturn(answering(1));
        Mockito.when(service.state(any(), eq(NAME), anyBoolean(), any(), any())).thenReturn(answering(1));
        bean.answer(NAME, 0, 1);
        Mockito.verify(service).answer(step, NAME, true, null, 0, 1, Locale.ENGLISH);

        newRequest();
        bean.getTrees();
        ArgumentCaptorHolder.verifyPath(service, step, NAME, 1);
    }

    /** Small helper to check the path argument of the next state call. */
    private static final class ArgumentCaptorHolder {
        static void verifyPath(DecisionTreeService service, Step step, String name, int... expected) {
            org.mockito.ArgumentCaptor<int[]> captor = org.mockito.ArgumentCaptor.forClass(int[].class);
            Mockito.verify(service, Mockito.atLeastOnce()).state(eq(step), eq(name), anyBoolean(), captor.capture(), any());
            assertArrayEquals(expected, captor.getValue());
        }
    }

    @Test
    public void everyActionDropsTheStateOfTheRequest() {
        Mockito.when(service.state(any(), eq(NAME), anyBoolean(), any(), any())).thenReturn(answering());
        Mockito.when(service.confirm(any(), eq(NAME), anyBoolean(), any(), any())).thenReturn(answering());
        Mockito.when(service.restart(any(), eq(NAME), anyBoolean(), any())).thenReturn(answering());
        Mockito.when(service.reopen(any(), eq(NAME), anyBoolean(), any(), anyInt(), any())).thenReturn(answering());
        bean.getTrees();
        bean.confirm(NAME);
        assertTrue(requestAttributes.isEmpty());
        bean.getTrees();
        bean.restart(NAME);
        assertTrue(requestAttributes.isEmpty());
        bean.getTrees();
        bean.reopen(NAME, 0);
        assertTrue(requestAttributes.isEmpty());
    }

    @Test
    public void aRefusedActionShowsAMessageAndKeepsThePath() {
        Mockito.when(service.answer(any(), eq(NAME), anyBoolean(), any(), eq(0), eq(1), any())).thenReturn(answering(1));
        bean.answer(NAME, 0, 1);
        Mockito.when(service.confirm(any(), eq(NAME), anyBoolean(), any(), any()))
                .thenThrow(new DecisionTreeException(Reason.CONFLICT, "not a result"));
        bean.confirm(NAME);
        helper.verify(() -> Helper.setFehlerMeldung("decisiontree.error.CONFLICT"));
        Mockito.when(service.state(any(), eq(NAME), anyBoolean(), any(), any())).thenReturn(answering(1));
        bean.getTrees();
        ArgumentCaptorHolder.verifyPath(service, step, NAME, 1);
    }

    @Test
    public void unavailableTreeShowsAMessageAndNoBox() {
        Mockito.when(service.state(any(), eq(NAME), anyBoolean(), any(), any()))
                .thenThrow(new DecisionTreeException(Reason.UNAVAILABLE, "broken"));
        assertTrue(bean.getTrees().isEmpty());
        helper.verify(() -> Helper.setFehlerMeldung("decisiontree.error.UNAVAILABLE"));
    }

    // who may edit

    private boolean editFlagOfTheNextCall() {
        Mockito.when(service.state(any(), eq(NAME), anyBoolean(), any(), any())).thenReturn(answering());
        newRequest();
        bean.getTrees();
        org.mockito.ArgumentCaptor<Boolean> captor = org.mockito.ArgumentCaptor.forClass(Boolean.class);
        Mockito.verify(service, Mockito.atLeastOnce()).state(any(), eq(NAME), captor.capture(), any(), any());
        return captor.getValue();
    }

    @Test
    public void theUserWhoWorksOnTheTaskMayEdit() {
        assertTrue(editFlagOfTheNextCall());
    }

    @Test
    public void aClosedTaskIsOnlyShown() {
        Mockito.when(step.getBearbeitungsstatusEnum()).thenReturn(StepStatus.DONE);
        assertFalse(editFlagOfTheNextCall());
    }

    @Test
    public void theTaskOfAnotherUserIsOnlyShown() {
        User other = Mockito.mock(User.class);
        Mockito.when(other.getId()).thenReturn(8);
        Mockito.when(step.getBearbeitungsbenutzer()).thenReturn(other);
        assertFalse(editFlagOfTheNextCall());
    }

    @Test
    public void aTaskWithoutAnEditorIsOnlyShown() {
        Mockito.when(step.getBearbeitungsbenutzer()).thenReturn(null);
        assertFalse(editFlagOfTheNextCall());
    }

    // the path belongs to one task

    @Test
    public void thePathIsForgottenWhenAnotherTaskIsOpen() {
        Mockito.when(service.answer(any(), eq(NAME), anyBoolean(), any(), anyInt(), anyInt(), any())).thenReturn(answering(1));
        Mockito.when(service.state(any(), eq(NAME), anyBoolean(), any(), any())).thenReturn(answering());
        bean.answer(NAME, 0, 1);

        Mockito.when(step.getId()).thenReturn(101);
        newRequest();
        bean.getTrees();
        org.mockito.ArgumentCaptor<int[]> captor = org.mockito.ArgumentCaptor.forClass(int[].class);
        Mockito.verify(service, Mockito.atLeastOnce()).state(any(), eq(NAME), anyBoolean(), captor.capture(), any());
        assertNull(captor.getValue());
    }

    @Test
    public void availableOnlyWithADecisionTree() {
        assertTrue(bean.isAvailable());
        Mockito.when(stepBean.getDecisionTreeProperties()).thenReturn(List.of());
        assertFalse(bean.isAvailable());
    }
}
