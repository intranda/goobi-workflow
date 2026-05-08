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

package org.goobi.production.flow.statistics.hibernate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.goobi.api.rest.model.RestProcessQueryResource;
import org.goobi.production.flow.statistics.hibernate.FilterHelper.StepFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.persistence.managers.MySQLHelper;

@ExtendWith(MockitoExtension.class)
public class FilterHelperTest extends AbstractTest {

    @Test
    public void testLimitToUserAccessRights() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<MySQLHelper> mockedMySQLHelper = Mockito.mockStatic(MySQLHelper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);

            mockedMySQLHelper.when(() -> MySQLHelper.escapeSql(Mockito.anyString())).thenAnswer(i -> i.getArguments()[0]);

            String result = FilterHelper.limitToUserAccessRights();
            assertEquals("", result);

        }
    }

    @Test
    public void testGetStepStartWithValidParameter() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<MySQLHelper> mockedMySQLHelper = Mockito.mockStatic(MySQLHelper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedMySQLHelper.when(() -> MySQLHelper.escapeSql(Mockito.anyString())).thenAnswer(i -> i.getArguments()[0]);

            String parameter = "10-20";
            Integer result = FilterHelper.getStepStart(parameter);
            assertEquals(10, result.intValue());

        }
    }

    @Test
    public void testGetStepStartWithInvalidParameter() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<MySQLHelper> mockedMySQLHelper = Mockito.mockStatic(MySQLHelper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedMySQLHelper.when(() -> MySQLHelper.escapeSql(Mockito.anyString())).thenAnswer(i -> i.getArguments()[0]);

            String parameter = "invalid-parameter";

            // It should throw NumberFormatException when the parameter is invalid
            assertThrows(NumberFormatException.class, () -> {
                FilterHelper.getStepStart(parameter);
            });

        }
    }

    @Test
    public void testGetStepStartWithNullParameter() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<MySQLHelper> mockedMySQLHelper = Mockito.mockStatic(MySQLHelper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedMySQLHelper.when(() -> MySQLHelper.escapeSql(Mockito.anyString())).thenAnswer(i -> i.getArguments()[0]);

            String parameter = null;

            // It should throw NullPointerException when the parameter is null
            assertThrows(NullPointerException.class, () -> {
                FilterHelper.getStepStart(parameter);
            });

        }
    }

    @Test
    public void testGetStepEndWithValidParameter() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<MySQLHelper> mockedMySQLHelper = Mockito.mockStatic(MySQLHelper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedMySQLHelper.when(() -> MySQLHelper.escapeSql(Mockito.anyString())).thenAnswer(i -> i.getArguments()[0]);

            String parameter = "10-20";
            Integer result = FilterHelper.getStepEnd(parameter);

            // Assert that the method returns the correct ending integer value
            assertEquals(20, result.intValue());

        }
    }

    @Test
    public void testGetStepEndWithInvalidParameter() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<MySQLHelper> mockedMySQLHelper = Mockito.mockStatic(MySQLHelper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedMySQLHelper.when(() -> MySQLHelper.escapeSql(Mockito.anyString())).thenAnswer(i -> i.getArguments()[0]);

            String parameter = "invalid-parameter";

            // It should throw NumberFormatException when the parameter is invalid
            assertThrows(NumberFormatException.class, () -> {
                FilterHelper.getStepEnd(parameter);
            });

        }
    }

    @Test
    public void testGetStepEndWithNullParameter() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<MySQLHelper> mockedMySQLHelper = Mockito.mockStatic(MySQLHelper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedMySQLHelper.when(() -> MySQLHelper.escapeSql(Mockito.anyString())).thenAnswer(i -> i.getArguments()[0]);

            String parameter = null;

            // It should throw NullPointerException when the parameter is null
            assertThrows(NullPointerException.class, () -> {
                FilterHelper.getStepEnd(parameter);
            });

        }
    }

    @Test
    public void testGetStepFilterWithRangeParameter() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<MySQLHelper> mockedMySQLHelper = Mockito.mockStatic(MySQLHelper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedMySQLHelper.when(() -> MySQLHelper.escapeSql(Mockito.anyString())).thenAnswer(i -> i.getArguments()[0]);
            String parameters = "10-20";
            StepFilter result = FilterHelper.getStepFilter(parameters);

            // Assert that the method returns StepFilter.range for range parameters
            assertEquals(StepFilter.range, result);

        }
    }

    @Test
    public void testGetStepFilterWithMaxParameter() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<MySQLHelper> mockedMySQLHelper = Mockito.mockStatic(MySQLHelper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedMySQLHelper.when(() -> MySQLHelper.escapeSql(Mockito.anyString())).thenAnswer(i -> i.getArguments()[0]);
            String parameters = "-20";
            StepFilter result = FilterHelper.getStepFilter(parameters);

            // Assert that the method returns StepFilter.max for max parameter
            assertEquals(StepFilter.max, result);

        }
    }

    @Test
    public void testGetStepFilterWithMinParameter() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<MySQLHelper> mockedMySQLHelper = Mockito.mockStatic(MySQLHelper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedMySQLHelper.when(() -> MySQLHelper.escapeSql(Mockito.anyString())).thenAnswer(i -> i.getArguments()[0]);

            String parameters = "10-";
            StepFilter result = FilterHelper.getStepFilter(parameters);

            // Assert that the method returns StepFilter.min for min parameter
            assertEquals(StepFilter.min, result);

        }
    }

    @Test
    public void testGetStepFilterWithExactParameter() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<MySQLHelper> mockedMySQLHelper = Mockito.mockStatic(MySQLHelper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedMySQLHelper.when(() -> MySQLHelper.escapeSql(Mockito.anyString())).thenAnswer(i -> i.getArguments()[0]);

            String parameters = "10";
            StepFilter result = FilterHelper.getStepFilter(parameters);

            // Assert that the method returns StepFilter.exact for exact parameter
            assertEquals(StepFilter.exact, result);

        }
    }

    @Test
    public void testGetStepFilterWithNameParameter() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<MySQLHelper> mockedMySQLHelper = Mockito.mockStatic(MySQLHelper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedMySQLHelper.when(() -> MySQLHelper.escapeSql(Mockito.anyString())).thenAnswer(i -> i.getArguments()[0]);
            String parameters = "invalid";
            StepFilter result = FilterHelper.getStepFilter(parameters);

            // Assert that the method returns StepFilter.name for name parameter
            assertEquals(StepFilter.name, result);

        }
    }

    @Test
    public void testFilterStepRangeWithoutNegate() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<MySQLHelper> mockedMySQLHelper = Mockito.mockStatic(MySQLHelper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedMySQLHelper.when(() -> MySQLHelper.escapeSql(Mockito.anyString())).thenAnswer(i -> i.getArguments()[0]);

            String parameters = "5-10";
            StepStatus status = StepStatus.OPEN;
            boolean negate = false;
            List<String> dateFilter = new ArrayList<>();

            String result = FilterHelper.filterStepRange(parameters, status, negate, dateFilter);

            // Assert that the generated SQL query matches the expected query without negation
            assertTrue(result.contains("schritte.Reihenfolge > 5"));
            assertTrue(result.contains("schritte.Reihenfolge < 10"));
            assertTrue(result.contains("schritte.Bearbeitungsstatus = " + status.getValue().intValue()));

        }
    }

    @Test
    public void testFilterStepRangeWithNegate() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<MySQLHelper> mockedMySQLHelper = Mockito.mockStatic(MySQLHelper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedMySQLHelper.when(() -> MySQLHelper.escapeSql(Mockito.anyString())).thenAnswer(i -> i.getArguments()[0]);
            String parameters = "5-10";
            StepStatus status = StepStatus.OPEN;
            boolean negate = true;
            List<String> dateFilter = new ArrayList<>();

            String result = FilterHelper.filterStepRange(parameters, status, negate, dateFilter);
            // Assert that the generated SQL query matches the expected query with negation
            assertTrue(result.contains("not in (select schritteId from schritte"));
            assertTrue(result.contains("schritte.Reihenfolge > 5"));
            assertTrue(result.contains("schritte.Reihenfolge < 10"));
            assertTrue(result.contains("schritte.Bearbeitungsstatus = " + status.getValue().intValue()));

        }
    }

    @Test
    public void testFilterStepNameWithoutNegate() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<MySQLHelper> mockedMySQLHelper = Mockito.mockStatic(MySQLHelper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedMySQLHelper.when(() -> MySQLHelper.escapeSql(Mockito.anyString())).thenAnswer(i -> i.getArguments()[0]);

            String parameters = "StepName";
            StepStatus status = StepStatus.OPEN;
            boolean negate = false;
            List<String> dateFilter = new ArrayList<>();

            String result = FilterHelper.filterStepName(parameters, status, negate, dateFilter, true);

            // Assert that the generated SQL query matches the expected query without negation
            assertTrue(result.contains("schritte.Titel like '%StepName%'"));
            assertTrue(result.contains("schritte.Bearbeitungsstatus = " + status.getValue().intValue()));

        }
    }

    @Test
    public void testFilterStepNameWithNegate() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<MySQLHelper> mockedMySQLHelper = Mockito.mockStatic(MySQLHelper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedMySQLHelper.when(() -> MySQLHelper.escapeSql(Mockito.anyString())).thenAnswer(i -> i.getArguments()[0]);
            String parameters = "StepName";
            StepStatus status = StepStatus.OPEN;
            boolean negate = true;
            List<String> dateFilter = new ArrayList<>();

            String result = FilterHelper.filterStepName(parameters, status, negate, dateFilter, true);
            // Assert that the generated SQL query matches the expected query with negation
            assertTrue(result.contains("schritte.Titel not like '%StepName%'"));
            assertTrue(result.contains("schritte.Bearbeitungsstatus = " + status.getValue().intValue()));

        }
    }

    @Test
    public void testFilterAutomaticStepsTrue() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<MySQLHelper> mockedMySQLHelper = Mockito.mockStatic(MySQLHelper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedMySQLHelper.when(() -> MySQLHelper.escapeSql(Mockito.anyString())).thenAnswer(i -> i.getArguments()[0]);

            String tok = "someToken:true";
            List<String> dateFilter = new ArrayList<>();

            String result = FilterHelper.filterAutomaticSteps(tok, dateFilter);

            // Assert that the generated SQL query matches the expected query for true case
            assertTrue(result.contains("schritte.typAutomatisch = true"));

        }
    }

    @Test
    public void testFilterAutomaticStepsFalse() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<MySQLHelper> mockedMySQLHelper = Mockito.mockStatic(MySQLHelper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedMySQLHelper.when(() -> MySQLHelper.escapeSql(Mockito.anyString())).thenAnswer(i -> i.getArguments()[0]);
            String tok = "someToken:false";
            List<String> dateFilter = new ArrayList<>();

            String result = FilterHelper.filterAutomaticSteps(tok, dateFilter);

            // Assert that the generated SQL query matches the expected query for false case
            assertTrue(result.contains("schritte.typAutomatisch = false"));

        }
    }

    @Test
    public void testFilterAutomaticStepsWithDateFilter() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<MySQLHelper> mockedMySQLHelper = Mockito.mockStatic(MySQLHelper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedMySQLHelper.when(() -> MySQLHelper.escapeSql(Mockito.anyString())).thenAnswer(i -> i.getArguments()[0]);
            String tok = "someToken:true";
            List<String> dateFilter = Arrays.asList("someDateFilter"); // Set date filters

            String result = FilterHelper.filterAutomaticSteps(tok, dateFilter);

            // Assert that the generated SQL query includes the date filter
            assertTrue(result.contains("AND someDateFilter"));

        }
    }

    @Test
    public void testFilterStepMinWithoutNegate() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<MySQLHelper> mockedMySQLHelper = Mockito.mockStatic(MySQLHelper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedMySQLHelper.when(() -> MySQLHelper.escapeSql(Mockito.anyString())).thenAnswer(i -> i.getArguments()[0]);

            String parameters = "5";
            StepStatus status = StepStatus.OPEN;
            boolean negate = false;
            List<String> dateFilter = new ArrayList<>();

            String result = FilterHelper.filterStepMin(parameters, status, negate, dateFilter);

            // Assert that the generated SQL query matches the expected query without negation
            assertTrue(result.contains("schritte.Reihenfolge >= 5"));
            assertTrue(result.contains("schritte.Bearbeitungsstatus = " + status.getValue().intValue()));

        }
    }

    @Test
    public void testFilterStepMinWithNegate() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<MySQLHelper> mockedMySQLHelper = Mockito.mockStatic(MySQLHelper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedMySQLHelper.when(() -> MySQLHelper.escapeSql(Mockito.anyString())).thenAnswer(i -> i.getArguments()[0]);

            String parameters = "5";
            StepStatus status = StepStatus.OPEN;
            boolean negate = true;
            List<String> dateFilter = new ArrayList<>();

            String result = FilterHelper.filterStepMin(parameters, status, negate, dateFilter);

            // Assert that the generated SQL query matches the expected query with negation
            assertTrue(result.contains("not in (select schritteId from schritte"));
            assertTrue(result.contains("schritte.Reihenfolge >= 5"));
            assertTrue(result.contains("schritte.Bearbeitungsstatus = " + status.getValue().intValue()));

        }
    }

    @Test
    public void testFilterStepMaxWithoutNegate() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<MySQLHelper> mockedMySQLHelper = Mockito.mockStatic(MySQLHelper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedMySQLHelper.when(() -> MySQLHelper.escapeSql(Mockito.anyString())).thenAnswer(i -> i.getArguments()[0]);

            String parameters = "5-10";
            StepStatus status = StepStatus.OPEN;
            boolean negate = false;
            List<String> dateFilter = new ArrayList<>();

            String result = FilterHelper.filterStepMax(parameters, status, negate, dateFilter);

            // Assert that the generated SQL query matches the expected query without negation
            assertTrue(result.contains("schritte.Reihenfolge <= 10"));
            assertTrue(result.contains("schritte.Bearbeitungsstatus = " + status.getValue().intValue()));

        }
    }

    @Test
    public void testFilterStepMaxWithNegate() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<MySQLHelper> mockedMySQLHelper = Mockito.mockStatic(MySQLHelper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedMySQLHelper.when(() -> MySQLHelper.escapeSql(Mockito.anyString())).thenAnswer(i -> i.getArguments()[0]);
            String parameters = "5-10";
            StepStatus status = StepStatus.OPEN;
            boolean negate = true;
            List<String> dateFilter = new ArrayList<>();

            String result = FilterHelper.filterStepMax(parameters, status, negate, dateFilter);

            // Assert that the generated SQL query matches the expected query with negation
            assertTrue(result.contains("not in (select schritteId from schritte"));
            assertTrue(result.contains("schritte.Reihenfolge <= 10"));
            assertTrue(result.contains("schritte.Bearbeitungsstatus = " + status.getValue().intValue()));

        }
    }

    @Test
    public void testFilterStepExactWithoutNegate() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<MySQLHelper> mockedMySQLHelper = Mockito.mockStatic(MySQLHelper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedMySQLHelper.when(() -> MySQLHelper.escapeSql(Mockito.anyString())).thenAnswer(i -> i.getArguments()[0]);
            String parameters = "5";
            StepStatus status = StepStatus.OPEN;
            boolean negate = false;
            List<String> dateFilter = new ArrayList<>();

            String result = FilterHelper.filterStepExact(parameters, status, negate, dateFilter);

            // Assert that the generated SQL query matches the expected query without negation
            assertTrue(result.contains("schritte.Reihenfolge = 5"));
            assertTrue(result.contains("schritte.Bearbeitungsstatus = " + status.getValue().intValue()));

        }
    }

    @Test
    public void testFilterStepExactWithNegate() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<MySQLHelper> mockedMySQLHelper = Mockito.mockStatic(MySQLHelper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedMySQLHelper.when(() -> MySQLHelper.escapeSql(Mockito.anyString())).thenAnswer(i -> i.getArguments()[0]);

            String parameters = "5";
            StepStatus status = StepStatus.OPEN;
            boolean negate = true;
            List<String> dateFilter = new ArrayList<>();

            String result = FilterHelper.filterStepExact(parameters, status, negate, dateFilter);

            // Assert that the generated SQL query matches the expected query with negation
            assertTrue(result.contains("schritte.Reihenfolge <> 5"));
            assertTrue(result.contains("schritte.Bearbeitungsstatus = " + status.getValue().intValue()));

        }
    }

    @Test
    public void testFilterStepDoneUser() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<MySQLHelper> mockedMySQLHelper = Mockito.mockStatic(MySQLHelper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedMySQLHelper.when(() -> MySQLHelper.escapeSql(Mockito.anyString())).thenAnswer(i -> i.getArguments()[0]);
            String tok = "user:username";
            String expectedQuery =
                    " prozesse.ProzesseID in (select ProzesseID from schritte where schritte.BearbeitungsBenutzerID"
                            + " = (select BenutzerID from benutzer where benutzer.login = 'username'))";

            String result = FilterHelper.filterStepDoneUser(tok);

            // Assert that the generated SQL query matches the expected query
            assertEquals(expectedQuery, result);

        }
    }

    @Test
    public void testFilterProjectWithoutNegate() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<MySQLHelper> mockedMySQLHelper = Mockito.mockStatic(MySQLHelper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedMySQLHelper.when(() -> MySQLHelper.escapeSql(Mockito.anyString())).thenAnswer(i -> i.getArguments()[0]);
            String tok = "project:ProjectName";
            String expectedQuery = " projekte.titel like '%ProjectName%'";

            String result = FilterHelper.filterProject(tok, false);

            // Assert that the generated SQL query matches the expected query without negation
            assertEquals(expectedQuery, result);

        }
    }

    @Test
    public void testFilterProjectWithNegate() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<MySQLHelper> mockedMySQLHelper = Mockito.mockStatic(MySQLHelper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedMySQLHelper.when(() -> MySQLHelper.escapeSql(Mockito.anyString())).thenAnswer(i -> i.getArguments()[0]);
            String tok = "project:ProjectName";
            String expectedQuery = " projekte.titel  not like '%ProjectName%'";

            String result = FilterHelper.filterProject(tok, true);

            // Assert that the generated SQL query matches the expected query with negation
            assertEquals(expectedQuery, result);

        }
    }

    @Test
    public void testFilterStepPropertyWithoutNegate() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<MySQLHelper> mockedMySQLHelper = Mockito.mockStatic(MySQLHelper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedMySQLHelper.when(() -> MySQLHelper.escapeSql(Mockito.anyString())).thenAnswer(i -> i.getArguments()[0]);
            String tok = "stepproperty:Title:Value";
            String expectedQuery =
                    " prozesse.prozesseID in (select distinct ProzesseID from schritte where schritte.schritteID in "
                            + "(select object_id from properties where object_type = 'error' AND property_value like"
                            + " ''%Value%'  AND property_name like '%Title%' ))";

            String result = FilterHelper.filterStepProperty(tok, false);

            // Assert that the generated SQL query matches the expected query without negation
            assertEquals(expectedQuery, result);

        }
    }

    @Test
    public void testFilterStepPropertyWithNegate() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<MySQLHelper> mockedMySQLHelper = Mockito.mockStatic(MySQLHelper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedMySQLHelper.when(() -> MySQLHelper.escapeSql(Mockito.anyString())).thenAnswer(i -> i.getArguments()[0]);
            String tok = "stepproperty:Title:Value";
            String expectedQuery =
                    " prozesse.prozesseID in (select distinct ProzesseID from schritte where schritte.schritteID not in "
                            + "(select object_id from properties where object_type = 'error' AND property_value like "
                            + "'%Value%'  AND property_name like '%Title%' ))";

            String result = FilterHelper.filterStepProperty(tok, true);

            // Assert that the generated SQL query matches the expected query with negation
            assertEquals(expectedQuery, result);

        }
    }

    @Test
    public void testFilterDateWithISOString() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<MySQLHelper> mockedMySQLHelper = Mockito.mockStatic(MySQLHelper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedMySQLHelper.when(() -> MySQLHelper.escapeSql(Mockito.anyString())).thenAnswer(i -> i.getArguments()[0]);
            String dateField = "dateField";
            String value = "2023-01-15T10:30:00Z";
            String operand = "=";
            String expectedQuery = "dateField= '2023-01-15 10:30:00'";

            String result = FilterHelper.filterDate(dateField, value, operand);

            // Assert that the generated SQL query matches the expected query for ISO string date format
            assertEquals(expectedQuery, result);

        }
    }

    @Test
    public void testFilterDateWithDateString() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<MySQLHelper> mockedMySQLHelper = Mockito.mockStatic(MySQLHelper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedMySQLHelper.when(() -> MySQLHelper.escapeSql(Mockito.anyString())).thenAnswer(i -> i.getArguments()[0]);
            String dateField = "dateField";
            String value = "2023-01-15";
            String operand = "<";
            String expectedQuery = "dateField < '2023-01-15 23:59:59' ";

            String result = FilterHelper.filterDate(dateField, value, operand);

            // Assert that the generated SQL query matches the expected query for date string format
            assertEquals(expectedQuery, result);

        }
    }

    @Test
    public void testFilterProcessPropertyWithoutNegate() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<MySQLHelper> mockedMySQLHelper = Mockito.mockStatic(MySQLHelper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedMySQLHelper.when(() -> MySQLHelper.escapeSql(Mockito.anyString())).thenAnswer(i -> i.getArguments()[0]);
            String tok = "processproperty:Title:Value";
            String expectedQuery =
                    " prozesse.prozesseID in (select object_id from properties where object_type = 'process' AND property_"
                            + "value like '%Value%'  AND property_name like '%Title%' )";
            String result = FilterHelper.filterProcessProperty(tok, false);

            // Assert that the generated SQL query matches the expected query without negation
            assertEquals(expectedQuery, result);

        }
    }

    @Test
    public void testFilterProcessPropertyWithNegate() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<MySQLHelper> mockedMySQLHelper = Mockito.mockStatic(MySQLHelper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedMySQLHelper.when(() -> MySQLHelper.escapeSql(Mockito.anyString())).thenAnswer(i -> i.getArguments()[0]);

            String tok = "processproperty:Title:Value";
            String expectedQuery =
                    " prozesse.prozesseID in (select object_id from properties where object_type = 'process' AND property_"
                            + "value like '%Value%'  AND property_name like '%Title%' )";

            String result = FilterHelper.filterProcessProperty(tok, true);

            // Assert that the generated SQL query matches the expected query with negation
            assertEquals(expectedQuery, result);

        }
    }

    @Test
    public void testFilterMetadataValueWithoutNegate() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<MySQLHelper> mockedMySQLHelper = Mockito.mockStatic(MySQLHelper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedMySQLHelper.when(() -> MySQLHelper.escapeSql(Mockito.anyString())).thenAnswer(i -> i.getArguments()[0]);
            String tok = "metadata:Title:Value";
            boolean negate = false;
            String expectedQuery =
                    "prozesse.ProzesseID in (select distinct processid from metadata where metadata.name like  '%Title%' "
                            + "AND metadata.value like '%Value%' )";

            String result = FilterHelper.filterMetadataValue(tok, negate);

            // Assert that the generated SQL query matches the expected query without negation
            assertEquals(expectedQuery, result);

        }
    }

    @Test
    public void testFilterMetadataValueWithNegate() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<MySQLHelper> mockedMySQLHelper = Mockito.mockStatic(MySQLHelper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedMySQLHelper.when(() -> MySQLHelper.escapeSql(Mockito.anyString())).thenAnswer(i -> i.getArguments()[0]);

            String tok = "metadata:Title:Value";
            boolean negate = true;
            String expectedQuery =
                    "prozesse.ProzesseID not in (select distinct processid from metadata where metadata.name like  '%Title%' "
                            + "AND metadata.value like '%Value%' )";

            String result = FilterHelper.filterMetadataValue(tok, negate);

            // Assert that the generated SQL query matches the expected query with negation
            assertEquals(expectedQuery, result);

        }
    }

    @Test
    public void testFilterProcessJournalWithoutNegate() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<MySQLHelper> mockedMySQLHelper = Mockito.mockStatic(MySQLHelper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedMySQLHelper.when(() -> MySQLHelper.escapeSql(Mockito.anyString())).thenAnswer(i -> i.getArguments()[0]);
            String tok = "log:SomeContent";
            boolean negate = false;
            String expectedQuery =
                    "prozesse.ProzesseID in (select distinct objectId from journal where journal.content like '%SomeContent%' "
                            + "and entrytype = 'process')";

            String result = FilterHelper.filterProcessJournal(tok, negate);

            // Assert that the generated SQL query matches the expected query without negation
            assertEquals(expectedQuery, result);

        }
    }

    @Test
    public void testFilterProcessJournalWithNegate() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<MySQLHelper> mockedMySQLHelper = Mockito.mockStatic(MySQLHelper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedMySQLHelper.when(() -> MySQLHelper.escapeSql(Mockito.anyString())).thenAnswer(i -> i.getArguments()[0]);

            String tok = "log:SomeContent";
            boolean negate = true;
            String expectedQuery =
                    "prozesse.ProzesseID not in (select distinct objectId from journal where journal.content like '%SomeContent%'"
                            + " and entrytype = 'process')";

            String result = FilterHelper.filterProcessJournal(tok, negate);

            // Assert that the generated SQL query matches the expected query with negation
            assertEquals(expectedQuery, result);

        }
    }

    @Test
    public void testFilterInstitutionWithoutNegate() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<MySQLHelper> mockedMySQLHelper = Mockito.mockStatic(MySQLHelper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedMySQLHelper.when(() -> MySQLHelper.escapeSql(Mockito.anyString())).thenAnswer(i -> i.getArguments()[0]);

            String tok = "institution:ShortName";
            boolean negate = false;
            String expectedQuery =
                    "prozesse.ProjekteID in (select ProjekteID from projekte left join institution on projekte.institution_id = "
                            + "institution.id WHERE institution.shortName LIKE '%"
                            + MySQLHelper.escapeSql("ShortName") + "%') ";

            String result = FilterHelper.filterInstitution(tok, negate);

            // Assert that the generated SQL query matches the expected query without negation
            assertEquals(expectedQuery, result);

        }
    }

    @Test
    public void testFilterInstitutionWithNegate() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<MySQLHelper> mockedMySQLHelper = Mockito.mockStatic(MySQLHelper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedMySQLHelper.when(() -> MySQLHelper.escapeSql(Mockito.anyString())).thenAnswer(i -> i.getArguments()[0]);
            String tok = "institution:ShortName";
            boolean negate = true;
            String expectedQuery =
                    "prozesse.ProjekteID not in (select ProjekteID from projekte left join institution on projekte.institution_id = "
                            + "institution.id WHERE institution.shortName LIKE '%"
                            + MySQLHelper.escapeSql("ShortName") + "%') ";

            String result = FilterHelper.filterInstitution(tok, negate);

            // Assert that the generated SQL query matches the expected query with negation
            assertEquals(expectedQuery, result);

        }
    }

    @Test
    public void testFilterIdsWithValidIds() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<MySQLHelper> mockedMySQLHelper = Mockito.mockStatic(MySQLHelper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedMySQLHelper.when(() -> MySQLHelper.escapeSql(Mockito.anyString())).thenAnswer(i -> i.getArguments()[0]);

            String tok = "someToken:1 2 3";
            boolean negate = false;
            String expectedQuery = " prozesse.prozesseId in (1, 2, 3)";

            String result = FilterHelper.filterIds(tok, negate);

            // Assert that the generated SQL query matches the expected query with valid IDs and no negation
            assertEquals(expectedQuery, result);

        }
    }

    @Test
    public void testFilterIdsWithNegationAndValidIds() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<MySQLHelper> mockedMySQLHelper = Mockito.mockStatic(MySQLHelper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedMySQLHelper.when(() -> MySQLHelper.escapeSql(Mockito.anyString())).thenAnswer(i -> i.getArguments()[0]);

            String tok = "someToken:4 5 6";
            boolean negate = true;
            String expectedQuery = " prozesse.prozesseId not in (4, 5, 6)";

            String result = FilterHelper.filterIds(tok, negate);

            // Assert that the generated SQL query matches the expected query with valid IDs and negation
            assertEquals(expectedQuery, result);

        }
    }

    @Test
    public void testFilterIdsWithEmptyToken() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<MySQLHelper> mockedMySQLHelper = Mockito.mockStatic(MySQLHelper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedMySQLHelper.when(() -> MySQLHelper.escapeSql(Mockito.anyString())).thenAnswer(i -> i.getArguments()[0]);

            String tok = "someToken:";
            boolean negate = false;
            String expectedQuery = "";

            String result = FilterHelper.filterIds(tok, negate);

            // Assert that the generated SQL query is empty when the token is empty
            assertEquals(expectedQuery, result);

        }
    }

    @Test
    public void testGetStepDoneNameWithValidExpression() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<MySQLHelper> mockedMySQLHelper = Mockito.mockStatic(MySQLHelper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedMySQLHelper.when(() -> MySQLHelper.escapeSql(Mockito.anyString())).thenAnswer(i -> i.getArguments()[0]);

            String myFilterExpression = "stepdone:MyStepName someOtherFilter";
            String expectedStepName = "MyStepName";

            String result = FilterHelper.getStepDoneName(myFilterExpression);

            // Assert that the extracted step name matches the expected step name
            assertEquals(expectedStepName, result);

        }
    }

    @Test
    public void testGetStepDoneNameWithNoStepName() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<MySQLHelper> mockedMySQLHelper = Mockito.mockStatic(MySQLHelper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedMySQLHelper.when(() -> MySQLHelper.escapeSql(Mockito.anyString())).thenAnswer(i -> i.getArguments()[0]);

            String myFilterExpression = "stepdone:";
            String expectedStepName = "";

            String result = FilterHelper.getStepDoneName(myFilterExpression);

            // Assert that when no step name is provided after 'stepdone:', the method returns null
            assertEquals(expectedStepName, result);

        }
    }

    @Test
    public void testGetStepDoneNameWithSpaceAfterStepName() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<MySQLHelper> mockedMySQLHelper = Mockito.mockStatic(MySQLHelper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedMySQLHelper.when(() -> MySQLHelper.escapeSql(Mockito.anyString())).thenAnswer(i -> i.getArguments()[0]);

            String myFilterExpression = "stepdone:MyStepName someOtherFilter";
            String expectedStepName = "MyStepName";

            String result = FilterHelper.getStepDoneName(myFilterExpression);

            // Assert that the extracted step name matches the expected step name even if there's content after the step name
            assertEquals(expectedStepName, result);

        }
    }

    @Test
    public void testGetStepDoneWithValidExpression() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<MySQLHelper> mockedMySQLHelper = Mockito.mockStatic(MySQLHelper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedMySQLHelper.when(() -> MySQLHelper.escapeSql(Mockito.anyString())).thenAnswer(i -> i.getArguments()[0]);

            String myFilterExpression = "stepdone:1-5";
            Integer expectedStartStep = 1;

            Integer result = FilterHelper.getStepDone(myFilterExpression);

            // Assert that the extracted start step matches the expected start step
            assertEquals(expectedStartStep, result);

        }
    }

    @Test
    public void testGetStepDoneWithInvalidExpression() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<MySQLHelper> mockedMySQLHelper = Mockito.mockStatic(MySQLHelper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedMySQLHelper.when(() -> MySQLHelper.escapeSql(Mockito.anyString())).thenAnswer(i -> i.getArguments()[0]);
            String myFilterExpression = "stepdone";
            Integer expectedStartStep = null;

            Integer result = FilterHelper.getStepDone(myFilterExpression);

            // Assert that when the filter expression is not in the expected format, the method returns null
            assertEquals(expectedStartStep, result);

        }
    }

    /**
     * Verifies that multiple filter conditions joined into a single string and passed to criteriaBuilder produce valid SQL with only one WHERE
     * clause.
     *
     * Previously, ProcessService called criteriaBuilder per condition and concatenated with AND, producing invalid "... AND WHERE ..." SQL.
     */
    @Test
    public void testMultipleConditionsDoNotProduceDuplicateWhere() {
        try (MockedStatic<Helper> mockedHelper = Mockito.mockStatic(Helper.class);
                MockedStatic<MySQLHelper> mockedMySQLHelper = Mockito.mockStatic(MySQLHelper.class)) {
            mockedHelper.when(() -> Helper.getLoginBean()).thenReturn(null);
            mockedHelper.when(() -> Helper.getCurrentUser()).thenReturn(null);
            mockedMySQLHelper.when(() -> MySQLHelper.escapeSql(Mockito.anyString())).thenAnswer(i -> i.getArguments()[0]);
            mockedMySQLHelper.when(() -> MySQLHelper.escapeString(Mockito.anyString())).thenAnswer(i -> i.getArguments()[0]);

            RestProcessQueryResource resource = new RestProcessQueryResource();
            resource.setFilter("processproperty:Template:value'id:12819");
            String[] conditions = resource.getConditions();
            assertEquals(2, conditions.length);

            // Replicate the fixed ProcessService logic: join conditions, single criteriaBuilder call
            String combinedFilter = String.join(" ", conditions);
            String criteria = FilterHelper.criteriaBuilder(combinedFilter, false, null, null, null, true, false);

            // Must not contain duplicate WHERE clauses
            assertFalse(
                    criteria.contains("WHERE") && criteria.indexOf("WHERE") != criteria.lastIndexOf("WHERE"),
                    "SQL criteria must not contain duplicate WHERE clauses");

            // Must contain both filter conditions
            assertTrue(criteria.contains("property_name like"), "Should filter by process property");
            assertTrue(criteria.contains("prozesse.prozesseId in (12819)"), "Should filter by process id");

        }
    }

}
