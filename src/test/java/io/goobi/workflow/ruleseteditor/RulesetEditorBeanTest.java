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

package io.goobi.workflow.ruleseteditor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.goobi.beans.Ruleset;
import org.goobi.managedbeans.ProcessBean;
import org.goobi.production.flow.statistics.hibernate.FilterHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.ProcessManager;

/**
 * Tests the parts of the ruleset editor that belong to the process check and the validation box.
 */
public class RulesetEditorBeanTest extends AbstractTest {

    private static final String RULESET = "src/test/resources/rulesets/ruleset.xml";

    private RulesetEditorBean bean;

    @BeforeEach
    public void setUp() {
        bean = new RulesetEditorBean();
    }

    @Test
    public void testProcessCheckQueryWithoutFilter() throws Exception {
        assertEquals(" WHERE prozesse.MetadatenKonfigurationID = 5", startProcessCheckAndGetQuery(""));
    }

    @Test
    public void testProcessCheckQueryKeepsTheFilterInBrackets() throws Exception {
        // the ruleset condition must not weaken an OR in the filter, so the filter stays in brackets
        String criteria = "left join schritte ON prozesse.ProzesseID = schritte.ProzesseID WHERE a = 1 OR b = 2";
        assertEquals("left join schritte ON prozesse.ProzesseID = schritte.ProzesseID WHERE (a = 1 OR b = 2) AND prozesse.MetadatenKonfigurationID = 5",
                startProcessCheckAndGetQuery(criteria));
    }

    @Test
    public void testProcessCheckDoesNotStartWithInvalidRuleset() throws Exception {
        setField("currentRuleset", createRuleset(5));
        setField("currentRulesetFileContent", "this is no ruleset");
        try (MockedStatic<Helper> helper = Mockito.mockStatic(Helper.class);
                MockedStatic<ProcessManager> processManager = Mockito.mockStatic(ProcessManager.class)) {
            bean.startProcessCheck();

            assertNull(bean.getProcessCheck());
            helper.verify(() -> Helper.setFehlerMeldung(Mockito.eq("rulesetEditor"), Mockito.any(), Mockito.anyString()));
            processManager.verifyNoInteractions();
        }
    }

    @Test
    public void testShowErrorsInProcessListWithoutTrailingSpace() throws Exception {
        assertEquals("\"id:1 2 3\"", showErrorsInProcessListAndGetFilter(1, 2, 3));
    }

    @Test
    public void testShowErrorsInProcessListWithOneError() throws Exception {
        assertEquals("\"id:7\"", showErrorsInProcessListAndGetFilter(7));
    }

    @Test
    public void testProcessCheckOutdated() throws Exception {
        RulesetProcessCheck check = Mockito.mock(RulesetProcessCheck.class);
        setField("processCheck", check);
        setField("processCheckContent", "checked content");

        // unchanged editor content
        setField("currentRulesetFileContent", "checked content");
        assertFalse(bean.isProcessCheckOutdated());

        // changed editor content
        setField("currentRulesetFileContent", "changed content");
        assertTrue(bean.isProcessCheckOutdated());

        // a running check is never outdated, it shows its progress
        Mockito.when(check.isRunning()).thenReturn(true);
        assertFalse(bean.isProcessCheckOutdated());
    }

    @Test
    public void testProcessCheckNotOutdatedWithoutCheck() {
        assertFalse(bean.isProcessCheckOutdated());
    }

    @Test
    public void testInfoMessagesAvailable() throws Exception {
        // not validated yet
        assertFalse(bean.isInfoMessagesAvailable());

        List<RulesetValidationError> errors = new ArrayList<>();
        errors.add(new RulesetValidationError(1, 0, "ERROR", "error", null, null));
        setField("validationErrors", errors);
        assertFalse(bean.isInfoMessagesAvailable());

        errors.add(new RulesetValidationError(2, 0, "INFO", "info", null, null));
        assertTrue(bean.isInfoMessagesAvailable());
    }

    /**
     * Starts the process check for ruleset 5 with a valid ruleset in the editor and returns the query that reaches the database.
     */
    private String startProcessCheckAndGetQuery(String criteria) throws Exception {
        setField("currentRuleset", createRuleset(5));
        setField("currentRulesetFileContent", readRuleset());
        try (MockedStatic<FilterHelper> filterHelper = Mockito.mockStatic(FilterHelper.class);
                MockedStatic<ProcessManager> processManager = Mockito.mockStatic(ProcessManager.class)) {
            filterHelper.when(() -> FilterHelper.criteriaBuilder(Mockito.anyString(), Mockito.anyBoolean(), Mockito.any(), Mockito.any(),
                    Mockito.any(), Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(criteria);
            processManager.when(() -> ProcessManager.getIdsForFilter(Mockito.anyString())).thenReturn(new ArrayList<>());

            bean.startProcessCheck();

            ArgumentCaptor<String> query = ArgumentCaptor.forClass(String.class);
            processManager.verify(() -> ProcessManager.getIdsForFilter(query.capture()));
            return query.getValue();
        }
    }

    /**
     * Opens the process list for a finished check with the given failed processes and returns the filter passed to the process list.
     */
    private String showErrorsInProcessListAndGetFilter(Integer... processIds) throws Exception {
        List<RulesetProcessCheck.ProcessError> errors = new ArrayList<>();
        for (Integer processId : Arrays.asList(processIds)) {
            errors.add(new RulesetProcessCheck.ProcessError(processId, "title", "project", "message", true));
        }
        RulesetProcessCheck check = Mockito.mock(RulesetProcessCheck.class);
        Mockito.when(check.getErrors()).thenReturn(errors);
        setField("processCheck", check);

        ProcessBean processBean = Mockito.mock(ProcessBean.class);
        try (MockedStatic<Helper> helper = Mockito.mockStatic(Helper.class)) {
            helper.when(() -> Helper.getBeanByClass(ProcessBean.class)).thenReturn(processBean);

            bean.showProcessCheckErrorsInProcessList();

            ArgumentCaptor<String> filter = ArgumentCaptor.forClass(String.class);
            Mockito.verify(processBean).setFilter(filter.capture());
            return filter.getValue();
        }
    }

    private Ruleset createRuleset(int id) {
        Ruleset ruleset = new Ruleset();
        ruleset.setId(id);
        return ruleset;
    }

    private String readRuleset() throws Exception {
        Path path = Paths.get(RULESET);
        if (!Files.exists(path)) {
            // when run from the command line
            path = Paths.get("target/test-classes/rulesets/ruleset.xml");
        }
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    private void setField(String name, Object value) throws Exception {
        Field field = RulesetEditorBean.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(bean, value);
    }
}
