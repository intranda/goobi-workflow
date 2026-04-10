package org.goobi.production.search.api;

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
 */
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import de.sub.goobi.AbstractTest;

public class ExtendedSearchRowTest extends AbstractTest {

    private ExtendedSearchRow row;

    @Before
    public void setUp() {
        row = new ExtendedSearchRow();
    }

    @Test
    public void testConstructor() {
        assertNotNull(row);
    }

    @Test
    public void testCreateSearchStringProcessTitle() {
        row.setFieldName("PROCESSTITLE");
        row.setFieldValue("Testprozess");
        assertEquals("\"Testprozess\" ", row.createSearchString());
    }

    @Test
    public void testCreateSearchStringProcessTitleWithOperand() {
        row.setFieldName("PROCESSTITLE");
        row.setFieldOperand("-");
        row.setFieldValue("Testprozess");
        assertEquals("\"-Testprozess\" ", row.createSearchString());
    }

    @Test
    public void testCreateSearchStringProcessTitleEmptyValueReturnsBlank() {
        row.setFieldName("PROCESSTITLE");
        row.setFieldValue("");
        assertEquals(" ", row.createSearchString());
    }

    @Test
    public void testCreateSearchStringProcessId() {
        row.setFieldName("PROCESSID");
        row.setFieldValue("42");
        assertEquals("\"id:42\" ", row.createSearchString());
    }

    @Test
    public void testCreateSearchStringProcessIdWithOperand() {
        row.setFieldName("PROCESSID");
        row.setFieldOperand("!=");
        row.setFieldValue("42");
        assertEquals("\"!=id:42\" ", row.createSearchString());
    }

    @Test
    public void testCreateSearchStringProcessIdEmptyValueReturnsBlank() {
        row.setFieldName("PROCESSID");
        row.setFieldValue("");
        assertEquals(" ", row.createSearchString());
    }

    @Test
    public void testCreateSearchStringBatch() {
        row.setFieldName("BATCH");
        row.setFieldValue("5");
        assertEquals("\"batch:5\" ", row.createSearchString());
    }

    @Test
    public void testCreateSearchStringBatchWithOperand() {
        row.setFieldName("BATCH");
        row.setFieldOperand("-");
        row.setFieldValue("5");
        assertEquals("\"-batch:5\" ", row.createSearchString());
    }

    @Test
    public void testCreateSearchStringBatchEmptyValueReturnsBlank() {
        row.setFieldName("BATCH");
        row.setFieldValue("");
        assertEquals(" ", row.createSearchString());
    }

    @Test
    public void testCreateSearchStringProject() {
        row.setFieldName("PROJECT");
        row.setProjectName("MyProject");
        assertEquals("\"project:MyProject\" ", row.createSearchString());
    }

    @Test
    public void testCreateSearchStringProjectWithOperand() {
        row.setFieldName("PROJECT");
        row.setFieldOperand("-");
        row.setProjectName("MyProject");
        assertEquals("\"-project:MyProject\" ", row.createSearchString());
    }

    @Test
    public void testCreateSearchStringProjectNotSelectedReturnsBlank() {
        row.setFieldName("PROJECT");
        row.setProjectName("Not selected");
        assertEquals(" ", row.createSearchString());
    }

    @Test
    public void testCreateSearchStringMetadata() {
        row.setFieldName("METADATA");
        row.setMetadataName("TitleDocMain");
        row.setMetadataValue("Faust");
        assertEquals("\"meta:TitleDocMain:Faust\" ", row.createSearchString());
    }

    @Test
    public void testCreateSearchStringMetadataWithOperand() {
        row.setFieldName("METADATA");
        row.setFieldOperand("-");
        row.setMetadataName("TitleDocMain");
        row.setMetadataValue("Faust");
        assertEquals("\"-meta:TitleDocMain:Faust\" ", row.createSearchString());
    }

    @Test
    public void testCreateSearchStringMetadataNotSelectedNameReturnsBlank() {
        row.setFieldName("METADATA");
        row.setMetadataName("Not selected");
        row.setMetadataValue("Faust");
        assertEquals(" ", row.createSearchString());
    }

    @Test
    public void testCreateSearchStringMetadataEmptyValueReturnsBlank() {
        row.setFieldName("METADATA");
        row.setMetadataName("TitleDocMain");
        row.setMetadataValue("");
        assertEquals(" ", row.createSearchString());
    }

    @Test
    public void testCreateSearchStringProcessProperty() {
        row.setFieldName("PROCESSPROPERTY");
        row.setProcessPropertyName("Schriftart");
        row.setProcessPropertyValue("Fraktur");
        assertEquals("\"processproperty:Schriftart:Fraktur\" ", row.createSearchString());
    }

    @Test
    public void testCreateSearchStringProcessPropertyWithOperand() {
        row.setFieldName("PROCESSPROPERTY");
        row.setFieldOperand("-");
        row.setProcessPropertyName("Schriftart");
        row.setProcessPropertyValue("Fraktur");
        assertEquals("\"-processproperty:Schriftart:Fraktur\" ", row.createSearchString());
    }

    @Test
    public void testCreateSearchStringProcessPropertyNotSelectedNameReturnsBlank() {
        row.setFieldName("PROCESSPROPERTY");
        row.setProcessPropertyName("Not selected");
        row.setProcessPropertyValue("Fraktur");
        assertEquals(" ", row.createSearchString());
    }

    @Test
    public void testCreateSearchStringProcessPropertyEmptyValueReturnsBlank() {
        row.setFieldName("PROCESSPROPERTY");
        row.setProcessPropertyName("Schriftart");
        row.setProcessPropertyValue("");
        assertEquals(" ", row.createSearchString());
    }

    @Test
    public void testCreateSearchStringStep() {
        row.setFieldName("STEP");
        row.setStepStatus("stepdone:");
        row.setStepName("Scannen");
        assertEquals("\"stepdone::Scannen\" ", row.createSearchString());
    }

    @Test
    public void testCreateSearchStringStepWithOperand() {
        row.setFieldName("STEP");
        row.setFieldOperand("-");
        row.setStepStatus("stepdone:");
        row.setStepName("Scannen");
        assertEquals("\"-stepdone::Scannen\" ", row.createSearchString());
    }

    @Test
    public void testCreateSearchStringStepNotSelectedStatusReturnsBlank() {
        row.setFieldName("STEP");
        row.setStepStatus("Not selected");
        row.setStepName("Scannen");
        assertEquals(" ", row.createSearchString());
    }

    @Test
    public void testCreateSearchStringStepEmptyNameReturnsBlank() {
        row.setFieldName("STEP");
        row.setStepStatus("stepdone:");
        row.setStepName("");
        assertEquals(" ", row.createSearchString());
    }

    @Test
    public void testCreateSearchStringStepNotSelectedNameReturnsBlank() {
        row.setFieldName("STEP");
        row.setStepStatus("stepdone:");
        row.setStepName("Not selected");
        assertEquals(" ", row.createSearchString());
    }

    @Test
    public void testCreateSearchStringJournal() {
        row.setFieldName("JOURNAL");
        row.setFieldValue("error");
        assertEquals("\"journal:error\" ", row.createSearchString());
    }

    @Test
    public void testCreateSearchStringJournalWithOperand() {
        row.setFieldName("JOURNAL");
        row.setFieldOperand("-");
        row.setFieldValue("error");
        assertEquals("\"-journal:error\" ", row.createSearchString());
    }

    @Test
    public void testCreateSearchStringJournalEmptyValueReturnsBlank() {
        row.setFieldName("JOURNAL");
        row.setFieldValue("");
        assertEquals(" ", row.createSearchString());
    }

    @Test
    public void testCreateSearchStringInstitution() {
        row.setFieldName("INSTITUTION");
        row.setInstitutionName("SUB");
        assertEquals("\"institution:SUB\" ", row.createSearchString());
    }

    @Test
    public void testCreateSearchStringInstitutionWithOperand() {
        row.setFieldName("INSTITUTION");
        row.setFieldOperand("-");
        row.setInstitutionName("SUB");
        assertEquals("\"-institution:SUB\" ", row.createSearchString());
    }

    @Test
    public void testCreateSearchStringInstitutionBlankNameReturnsBlank() {
        row.setFieldName("INSTITUTION");
        row.setInstitutionName("");
        assertEquals(" ", row.createSearchString());
    }

    @Test
    public void testCreateSearchStringProcessDate() {
        row.setFieldName("PROCESSDATE");
        row.setFieldValue("2024-01-01");
        assertEquals("\"processdate2024-01-01\" ", row.createSearchString());
    }

    @Test
    public void testCreateSearchStringProcessDateWithOperand() {
        row.setFieldName("PROCESSDATE");
        row.setFieldOperand(">");
        row.setFieldValue("2024-01-01");
        assertEquals("\"processdate>2024-01-01\" ", row.createSearchString());
    }

    @Test
    public void testCreateSearchStringProcessDateEmptyValueReturnsBlank() {
        row.setFieldName("PROCESSDATE");
        row.setFieldValue("");
        assertEquals(" ", row.createSearchString());
    }

    @Test
    public void testCreateSearchStringStepStartDate() {
        row.setFieldName("STEPSTARTDATE");
        row.setFieldValue("2024-01-01");
        assertEquals("\"stepstartdate2024-01-01\" ", row.createSearchString());
    }

    @Test
    public void testCreateSearchStringStepStartDateWithOperand() {
        row.setFieldName("STEPSTARTDATE");
        row.setFieldOperand(">");
        row.setFieldValue("2024-01-01");
        assertEquals("\"stepstartdate>2024-01-01\" ", row.createSearchString());
    }

    @Test
    public void testCreateSearchStringStepStartDateEmptyValueReturnsBlank() {
        row.setFieldName("STEPSTARTDATE");
        row.setFieldValue("");
        assertEquals(" ", row.createSearchString());
    }

    @Test
    public void testCreateSearchStringStepFinishDate() {
        row.setFieldName("STEPFINISHDATE");
        row.setFieldValue("2024-12-31");
        assertEquals("\"stepfinishdate2024-12-31\" ", row.createSearchString());
    }

    @Test
    public void testCreateSearchStringStepFinishDateWithOperand() {
        row.setFieldName("STEPFINISHDATE");
        row.setFieldOperand("<");
        row.setFieldValue("2024-12-31");
        assertEquals("\"stepfinishdate<2024-12-31\" ", row.createSearchString());
    }

    @Test
    public void testCreateSearchStringStepFinishDateEmptyValueReturnsBlank() {
        row.setFieldName("STEPFINISHDATE");
        row.setFieldValue("");
        assertEquals(" ", row.createSearchString());
    }

    @Test
    public void testCreateSearchStringUnknownFieldNameReturnsBlank() {
        row.setFieldName("UNKNOWN");
        row.setFieldValue("someValue");
        assertEquals(" ", row.createSearchString());
    }
}
