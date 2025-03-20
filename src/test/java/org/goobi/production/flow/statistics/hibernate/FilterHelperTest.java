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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.goobi.production.flow.statistics.hibernate.FilterHelper.StepFilter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.persistence.managers.MySQLHelper;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ MySQLHelper.class, Helper.class })
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*",
        "javax.crypto.*" })
@SuppressStaticInitializationFor("de.sub.goobi.persistence.managers.MySQLHelper")
public class FilterHelperTest extends AbstractTest {

    @Before
    public void setUp() throws Exception {
        PowerMock.mockStatic(Helper.class);
        EasyMock.expect(Helper.getLoginBean()).andReturn(null).anyTimes();
        EasyMock.expect(Helper.getCurrentUser()).andReturn(null).anyTimes();

        PowerMock.mockStatic(MySQLHelper.class);

        EasyMock.expect(MySQLHelper.escapeSql(EasyMock.anyString()))
                .andAnswer(new IAnswer<String>() {
                    @Override
                    public String answer() throws Throwable {
                        return (String) EasyMock.getCurrentArguments()[0];
                    }
                })
                .anyTimes();

        PowerMock.replay(MySQLHelper.class);
        PowerMock.replay(Helper.class);
    }

    @Test
    public void testLimitToUserAccessRights() {
        String result = FilterHelper.limitToUserAccessRights();
        assertEquals("", result);
    }

    @Test
    public void testLimitToUserAssignedSteps() {
        String result = FilterHelper.limitToUserAssignedSteps(true, false, false);
        assertEquals("", result);
    }

    @Test
    public void testGetStepStartWithValidParameter() {
        String parameter = "10-20";
        Integer result = FilterHelper.getStepStart(parameter);
        assertEquals(10, result.intValue());
    }

    @Test
    public void testGetStepStartWithInvalidParameter() {
        String parameter = "invalid-parameter";

        // It should throw NumberFormatException when the parameter is invalid
        assertThrows(NumberFormatException.class, () -> {
            FilterHelper.getStepStart(parameter);
        });
    }

    @Test
    public void testGetStepStartWithNullParameter() {
        String parameter = null;

        // It should throw NullPointerException when the parameter is null
        assertThrows(NullPointerException.class, () -> {
            FilterHelper.getStepStart(parameter);
        });
    }

    @Test
    public void testGetStepEndWithValidParameter() {
        String parameter = "10-20";
        Integer result = FilterHelper.getStepEnd(parameter);

        // Assert that the method returns the correct ending integer value
        assertEquals(20, result.intValue());
    }

    @Test
    public void testGetStepEndWithInvalidParameter() {
        String parameter = "invalid-parameter";

        // It should throw NumberFormatException when the parameter is invalid
        assertThrows(NumberFormatException.class, () -> {
            FilterHelper.getStepEnd(parameter);
        });
    }

    @Test
    public void testGetStepEndWithNullParameter() {
        String parameter = null;

        // It should throw NullPointerException when the parameter is null
        assertThrows(NullPointerException.class, () -> {
            FilterHelper.getStepEnd(parameter);
        });
    }

    @Test
    public void testGetStepFilterWithRangeParameter() {
        String parameters = "10-20";
        StepFilter result = FilterHelper.getStepFilter(parameters);

        // Assert that the method returns StepFilter.range for range parameters
        assertEquals(StepFilter.range, result);
    }

    @Test
    public void testGetStepFilterWithMaxParameter() {
        String parameters = "-20";
        StepFilter result = FilterHelper.getStepFilter(parameters);

        // Assert that the method returns StepFilter.max for max parameter
        assertEquals(StepFilter.max, result);
    }

    @Test
    public void testGetStepFilterWithMinParameter() {
        String parameters = "10-";
        StepFilter result = FilterHelper.getStepFilter(parameters);

        // Assert that the method returns StepFilter.min for min parameter
        assertEquals(StepFilter.min, result);
    }

    @Test
    public void testGetStepFilterWithExactParameter() {
        String parameters = "10";
        StepFilter result = FilterHelper.getStepFilter(parameters);

        // Assert that the method returns StepFilter.exact for exact parameter
        assertEquals(StepFilter.exact, result);
    }

    @Test
    public void testGetStepFilterWithNameParameter() {
        String parameters = "invalid";
        StepFilter result = FilterHelper.getStepFilter(parameters);

        // Assert that the method returns StepFilter.name for name parameter
        assertEquals(StepFilter.name, result);
    }

    @Test
    public void testFilterStepRangeWithoutNegate() {
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

    @Test
    public void testFilterStepRangeWithNegate() {
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

    @Test
    public void testFilterStepNameWithoutNegate() {
        String parameters = "StepName";
        StepStatus status = StepStatus.OPEN;
        boolean negate = false;
        List<String> dateFilter = new ArrayList<>();

        String result = FilterHelper.filterStepName(parameters, status, negate, dateFilter);

        // Assert that the generated SQL query matches the expected query without negation
        assertTrue(result.contains("schritte.Titel like '%StepName%'"));
        assertTrue(result.contains("schritte.Bearbeitungsstatus = " + status.getValue().intValue()));
    }

    @Test
    public void testFilterStepNameWithNegate() {
        String parameters = "StepName";
        StepStatus status = StepStatus.OPEN;
        boolean negate = true;
        List<String> dateFilter = new ArrayList<>();

        String result = FilterHelper.filterStepName(parameters, status, negate, dateFilter);
        // Assert that the generated SQL query matches the expected query with negation
        assertTrue(result.contains("not in (select ProzesseID from schritte"));
        assertTrue(result.contains("schritte.Titel like '%StepName%'"));
        assertTrue(result.contains("schritte.Bearbeitungsstatus = " + status.getValue().intValue()));
    }

    @Test
    public void testFilterAutomaticStepsTrue() {
        String tok = "someToken:true";
        List<String> dateFilter = new ArrayList<>();

        String result = FilterHelper.filterAutomaticSteps(tok, dateFilter);

        // Assert that the generated SQL query matches the expected query for true case
        assertTrue(result.contains("schritte.typAutomatisch = true"));
    }

    @Test
    public void testFilterAutomaticStepsFalse() {
        String tok = "someToken:false";
        List<String> dateFilter = new ArrayList<>();

        String result = FilterHelper.filterAutomaticSteps(tok, dateFilter);

        // Assert that the generated SQL query matches the expected query for false case
        assertTrue(result.contains("schritte.typAutomatisch = false"));
    }

    @Test
    public void testFilterAutomaticStepsWithDateFilter() {
        String tok = "someToken:true";
        List<String> dateFilter = Arrays.asList("someDateFilter"); // Set date filters

        String result = FilterHelper.filterAutomaticSteps(tok, dateFilter);

        // Assert that the generated SQL query includes the date filter
        assertTrue(result.contains("AND someDateFilter"));
    }

    @Test
    public void testFilterStepMinWithoutNegate() {
        String parameters = "5";
        StepStatus status = StepStatus.OPEN;
        boolean negate = false;
        List<String> dateFilter = new ArrayList<>();

        String result = FilterHelper.filterStepMin(parameters, status, negate, dateFilter);

        // Assert that the generated SQL query matches the expected query without negation
        assertTrue(result.contains("schritte.Reihenfolge >= 5"));
        assertTrue(result.contains("schritte.Bearbeitungsstatus = " + status.getValue().intValue()));
    }

    @Test
    public void testFilterStepMinWithNegate() {
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

    @Test
    public void testFilterStepMaxWithoutNegate() {
        String parameters = "5-10";
        StepStatus status = StepStatus.OPEN;
        boolean negate = false;
        List<String> dateFilter = new ArrayList<>();

        String result = FilterHelper.filterStepMax(parameters, status, negate, dateFilter);

        // Assert that the generated SQL query matches the expected query without negation
        assertTrue(result.contains("schritte.Reihenfolge <= 10"));
        assertTrue(result.contains("schritte.Bearbeitungsstatus = " + status.getValue().intValue()));
    }

    @Test
    public void testFilterStepMaxWithNegate() {
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

    @Test
    public void testFilterStepExactWithoutNegate() {
        String parameters = "5";
        StepStatus status = StepStatus.OPEN;
        boolean negate = false;
        List<String> dateFilter = new ArrayList<>();

        String result = FilterHelper.filterStepExact(parameters, status, negate, dateFilter);

        // Assert that the generated SQL query matches the expected query without negation
        assertTrue(result.contains("schritte.Reihenfolge = 5"));
        assertTrue(result.contains("schritte.Bearbeitungsstatus = " + status.getValue().intValue()));
    }

    @Test
    public void testFilterStepExactWithNegate() {
        String parameters = "5";
        StepStatus status = StepStatus.OPEN;
        boolean negate = true;
        List<String> dateFilter = new ArrayList<>();

        String result = FilterHelper.filterStepExact(parameters, status, negate, dateFilter);

        // Assert that the generated SQL query matches the expected query with negation
        assertTrue(result.contains("schritte.Reihenfolge <> 5"));
        assertTrue(result.contains("schritte.Bearbeitungsstatus = " + status.getValue().intValue()));
    }

    @Test
    public void testFilterStepDoneUser() {
        String tok = "user:username";
        String expectedQuery =
                " prozesse.ProzesseID in (select ProzesseID from schritte where schritte.BearbeitungsBenutzerID = (select BenutzerID from benutzer where benutzer.login = 'username'))";

        String result = FilterHelper.filterStepDoneUser(tok);

        // Assert that the generated SQL query matches the expected query
        assertEquals(expectedQuery, result);
    }

    @Test
    public void testFilterProjectWithoutNegate() {
        String tok = "project:ProjectName";
        String expectedQuery = " prozesse.ProjekteID in (select ProjekteID from projekte where titel like '%ProjectName%')";

        String result = FilterHelper.filterProject(tok, false);

        // Assert that the generated SQL query matches the expected query without negation
        assertEquals(expectedQuery, result);
    }

    @Test
    public void testFilterProjectWithNegate() {
        String tok = "project:ProjectName";
        String expectedQuery = " prozesse.ProjekteID in (select ProjekteID from projekte where titel not like '%ProjectName%')";

        String result = FilterHelper.filterProject(tok, true);

        // Assert that the generated SQL query matches the expected query with negation
        assertEquals(expectedQuery, result);
    }

    @Test
    public void testFilterScanTemplateWithoutNegate() {
        String tok = "template:Title:Value";
        String expectedQuery =
                " prozesse.prozesseID in (select prozesseID from vorlagen where vorlagenID in (select object_id from properties where object_type = 'template' AND properties.property_value like '%Value%' AND properties.property_name LIKE '%Title%'))";

        String result = FilterHelper.filterScanTemplate(tok, false);

        // Assert that the generated SQL query matches the expected query without negation
        assertEquals(expectedQuery, result);
    }

    @Test
    public void testFilterScanTemplateWithNegate() {
        String tok = "template:Title:Value";
        String expectedQuery =
                " prozesse.prozesseID not in (select prozesseID from vorlagen where vorlagenID in (select object_id from properties where object_type = 'template' AND properties.property_value like '%Value%' AND properties.property_name LIKE '%Title%'))";

        String result = FilterHelper.filterScanTemplate(tok, true);

        // Assert that the generated SQL query matches the expected query with negation
        assertEquals(expectedQuery, result);
    }

    @Test
    public void testFilterStepPropertyWithoutNegate() {
        String tok = "stepproperty:Title:Value";
        String expectedQuery =
                " prozesse.prozesseID in (select distinct ProzesseID from schritte where schritte.schritteID in (select object_id from properties where object_type = 'error' AND property_value like ''%Value%'  AND property_name like '%Title%' ))";

        String result = FilterHelper.filterStepProperty(tok, false);

        // Assert that the generated SQL query matches the expected query without negation
        assertEquals(expectedQuery, result);
    }

    @Test
    public void testFilterStepPropertyWithNegate() {
        String tok = "stepproperty:Title:Value";
        String expectedQuery =
                " prozesse.prozesseID in (select distinct ProzesseID from schritte where schritte.schritteID not in (select object_id from properties where object_type = 'error' AND property_value like '%Value%'  AND property_name like '%Title%' ))";

        String result = FilterHelper.filterStepProperty(tok, true);

        // Assert that the generated SQL query matches the expected query with negation
        assertEquals(expectedQuery, result);
    }

    @Test
    public void testFilterDateWithISOString() {
        String dateField = "dateField";
        String value = "2023-01-15T10:30:00Z";
        String operand = "=";
        String expectedQuery = "dateField= '2023-01-15 10:30:00'";

        String result = FilterHelper.filterDate(dateField, value, operand);

        // Assert that the generated SQL query matches the expected query for ISO string date format
        assertEquals(expectedQuery, result);
    }

    @Test
    public void testFilterDateWithDateString() {
        String dateField = "dateField";
        String value = "2023-01-15";
        String operand = "<";
        String expectedQuery = "dateField < '2023-01-15 23:59:59' ";

        String result = FilterHelper.filterDate(dateField, value, operand);

        // Assert that the generated SQL query matches the expected query for date string format
        assertEquals(expectedQuery, result);
    }

    @Test
    public void testFilterProcessPropertyWithoutNegate() {
        String tok = "processproperty:Title:Value";
        String expectedQuery =
                "prozesse.ProzesseID in (select object_id from properties where object_type = 'process' AND properties.property_name like '%Title%' AND properties.property_value like '%Value%' )";
        String result = FilterHelper.filterProcessProperty(tok, false);

        // Assert that the generated SQL query matches the expected query without negation
        assertEquals(expectedQuery, result);
    }

    @Test
    public void testFilterProcessPropertyWithNegate() {
        String tok = "processproperty:Title:Value";
        String expectedQuery =
                "prozesse.ProzesseID not in (select object_id from properties where object_type = 'process' AND properties.property_name like  '%Title%' AND properties.property_value like '%Value%' )";

        String result = FilterHelper.filterProcessProperty(tok, true);

        // Assert that the generated SQL query matches the expected query with negation
        assertEquals(expectedQuery, result);
    }

    @Test
    public void testFilterMetadataValueWithoutNegate() {
        String tok = "metadata:Title:Value";
        boolean negate = false;
        String expectedQuery =
                "prozesse.ProzesseID in (select distinct processid from metadata where metadata.name like  '%Title%' AND metadata.value like '%Value%' )";

        String result = FilterHelper.filterMetadataValue(tok, negate);

        // Assert that the generated SQL query matches the expected query without negation
        assertEquals(expectedQuery, result);
    }

    @Test
    public void testFilterMetadataValueWithNegate() {
        String tok = "metadata:Title:Value";
        boolean negate = true;
        String expectedQuery =
                "prozesse.ProzesseID not in (select distinct processid from metadata where metadata.name like  '%Title%' AND metadata.value like '%Value%' )";

        String result = FilterHelper.filterMetadataValue(tok, negate);

        // Assert that the generated SQL query matches the expected query with negation
        assertEquals(expectedQuery, result);
    }

    @Test
    public void testFilterProcessJournalWithoutNegate() {
        String tok = "log:SomeContent";
        boolean negate = false;
        String expectedQuery =
                "prozesse.ProzesseID in (select distinct objectId from journal where journal.content like '%SomeContent%' and entrytype = 'process')";

        String result = FilterHelper.filterProcessJournal(tok, negate);

        // Assert that the generated SQL query matches the expected query without negation
        assertEquals(expectedQuery, result);
    }

    @Test
    public void testFilterProcessJournalWithNegate() {
        String tok = "log:SomeContent";
        boolean negate = true;
        String expectedQuery =
                "prozesse.ProzesseID not in (select distinct objectId from journal where journal.content like '%SomeContent%' and entrytype = 'process')";

        String result = FilterHelper.filterProcessJournal(tok, negate);

        // Assert that the generated SQL query matches the expected query with negation
        assertEquals(expectedQuery, result);
    }

    @Test
    public void testFilterInstitutionWithoutNegate() {
        String tok = "institution:ShortName";
        boolean negate = false;
        String expectedQuery =
                "prozesse.ProjekteID in (select ProjekteID from projekte left join institution on projekte.institution_id = institution.id WHERE institution.shortName LIKE '%"
                        + MySQLHelper.escapeSql("ShortName") + "%')";

        String result = FilterHelper.filterInstitution(tok, negate);

        // Assert that the generated SQL query matches the expected query without negation
        assertEquals(expectedQuery, result);
    }

    @Test
    public void testFilterInstitutionWithNegate() {
        String tok = "institution:ShortName";
        boolean negate = true;
        String expectedQuery =
                "prozesse.ProjekteID not sin (select ProjekteID from projekte left join institution on projekte.institution_id = institution.id WHERE institution.shortName LIKE '%"
                        + MySQLHelper.escapeSql("ShortName") + "%')";

        String result = FilterHelper.filterInstitution(tok, negate);

        // Assert that the generated SQL query matches the expected query with negation
        assertEquals(expectedQuery, result);
    }

    @Test
    public void testFilterIdsWithValidIds() {
        String tok = "someToken:1 2 3";
        boolean negate = false;
        String expectedQuery = " prozesse.prozesseId in (1, 2, 3)";

        String result = FilterHelper.filterIds(tok, negate);

        // Assert that the generated SQL query matches the expected query with valid IDs and no negation
        assertEquals(expectedQuery, result);
    }

    @Test
    public void testFilterIdsWithNegationAndValidIds() {
        String tok = "someToken:4 5 6";
        boolean negate = true;
        String expectedQuery = " prozesse.prozesseId not in (4, 5, 6)";

        String result = FilterHelper.filterIds(tok, negate);

        // Assert that the generated SQL query matches the expected query with valid IDs and negation
        assertEquals(expectedQuery, result);
    }

    @Test
    public void testFilterIdsWithEmptyToken() {
        String tok = "someToken:";
        boolean negate = false;
        String expectedQuery = "";

        String result = FilterHelper.filterIds(tok, negate);

        // Assert that the generated SQL query is empty when the token is empty
        assertEquals(expectedQuery, result);
    }

    @Test
    public void testFilterWorkpiece() {
        String tok = "workpiece:title:value";
        boolean negate = false;
        String expectedQuery =
                " prozesse.prozesseID in (select werkstuecke.prozesseID from werkstuecke where WerkstueckeID in (select object_id from properties where object_type = 'masterpiece' AND properties.property_value like '%value%' AND properties.property_name LIKE '%title%'))";
        String result = FilterHelper.filterWorkpiece(tok, negate);

        // Assert that the generated SQL query matches the expected query with a valid signature and no negation
        assertEquals(expectedQuery, result);
    }

    @Test
    public void testFilterWorkpieceWithNegation() {
        String tok = "workpiece:title:value";
        boolean negate = true;
        String expectedQuery =
                " prozesse.prozesseID in (select werkstuecke.prozesseID from werkstuecke where WerkstueckeID not in (select object_id from properties where object_type = 'masterpiece' AND properties.property_value like '%value%' AND properties.property_name LIKE '%title%'))";

        String result = FilterHelper.filterWorkpiece(tok, negate);

        // Assert that the generated SQL query matches the expected query with a valid signature and negation
        assertEquals(expectedQuery, result);
    }

    @Test
    public void testFilterWorkpieceWithNoTitle() {
        String tok = "workpiece:value";
        boolean negate = false;
        String expectedQuery =
                " prozesse.prozesseID in (select werkstuecke.prozesseID from werkstuecke where WerkstueckeID in (select object_id from properties where object_type = 'masterpiece' AND properties.property_value like '%value%'))";
        String result = FilterHelper.filterWorkpiece(tok, negate);

        // Assert that the generated SQL query matches the expected query with no title in the signature and no negation
        assertEquals(expectedQuery, result);
    }

    @Test
    public void testGetStepDoneNameWithValidExpression() {
        String myFilterExpression = "stepdone:MyStepName someOtherFilter";
        String expectedStepName = "MyStepName";

        String result = FilterHelper.getStepDoneName(myFilterExpression);

        // Assert that the extracted step name matches the expected step name
        assertEquals(expectedStepName, result);
    }

    @Test
    public void testGetStepDoneNameWithNoStepName() {
        String myFilterExpression = "stepdone:";
        String expectedStepName = "";

        String result = FilterHelper.getStepDoneName(myFilterExpression);

        // Assert that when no step name is provided after 'stepdone:', the method returns null
        assertEquals(expectedStepName, result);
    }

    @Test
    public void testGetStepDoneNameWithSpaceAfterStepName() {
        String myFilterExpression = "stepdone:MyStepName someOtherFilter";
        String expectedStepName = "MyStepName";

        String result = FilterHelper.getStepDoneName(myFilterExpression);

        // Assert that the extracted step name matches the expected step name even if there's content after the step name
        assertEquals(expectedStepName, result);
    }

    @Test
    public void testGetStepDoneWithValidExpression() {
        String myFilterExpression = "stepdone:1-5";
        Integer expectedStartStep = 1;

        Integer result = FilterHelper.getStepDone(myFilterExpression);

        // Assert that the extracted start step matches the expected start step
        assertEquals(expectedStartStep, result);
    }

    @Test
    public void testGetStepDoneWithInvalidExpression() {
        String myFilterExpression = "stepdone";
        Integer expectedStartStep = null;

        Integer result = FilterHelper.getStepDone(myFilterExpression);

        // Assert that when the filter expression is not in the expected format, the method returns null
        assertEquals(expectedStartStep, result);
    }

}
