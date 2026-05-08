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
package io.goobi.workflow.harvester.beans;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.persistence.managers.HarvesterRepositoryManager;
import io.goobi.workflow.harvester.export.ExportHistoryEntry;
import io.goobi.workflow.harvester.export.ExportOutcome;
import io.goobi.workflow.harvester.export.ExportOutcome.ExportOutcomeStatus;
import io.goobi.workflow.harvester.repository.Repository;

@ExtendWith(MockitoExtension.class)
public class RecordExportTest extends AbstractTest {

    private Record record;

    @BeforeEach
    public void setUp() {
        record = new Record();
        record.setId(1);
        record.setIdentifier("test-identifier");
        record.setTitle("test-title");
        record.setRepositoryId(42);
    }

    @Test
    public void testExportOutcomeOk() throws Exception {

        ExportOutcome outcome = new ExportOutcome();
        outcome.setStatus(ExportOutcomeStatus.OK);

        Repository mockRepository = Mockito.mock(Repository.class);
        Mockito.when(mockRepository.exportRecord(Mockito.any())).thenReturn(outcome);

        try (MockedStatic<HarvesterRepositoryManager> mockedHarvesterRepositoryManager = Mockito.mockStatic(HarvesterRepositoryManager.class)) {
            mockedHarvesterRepositoryManager.when(() -> HarvesterRepositoryManager.getRepository(Mockito.anyInt())).thenReturn(mockRepository);

            ExportHistoryEntry hist = new ExportHistoryEntry(record);
            ExportOutcome result = record.export(hist);

            assertEquals(ExportOutcomeStatus.OK, result.getStatus());
            assertEquals("OK", hist.getStatus());
            assertNotNull(record.getExportedDatestamp());

        }
    }

    @Test
    public void testExportOutcomeNotFound() throws Exception {

        ExportOutcome outcome = new ExportOutcome();
        outcome.setStatus(ExportOutcomeStatus.NOT_FOUND);

        Repository mockRepository = Mockito.mock(Repository.class);
        Mockito.when(mockRepository.exportRecord(Mockito.any())).thenReturn(outcome);

        try (MockedStatic<HarvesterRepositoryManager> mockedHarvesterRepositoryManager = Mockito.mockStatic(HarvesterRepositoryManager.class)) {
            mockedHarvesterRepositoryManager.when(() -> HarvesterRepositoryManager.getRepository(Mockito.anyInt())).thenReturn(mockRepository);

            ExportHistoryEntry hist = new ExportHistoryEntry(record);
            ExportOutcome result = record.export(hist);

            assertEquals(ExportOutcomeStatus.NOT_FOUND, result.getStatus());
            assertEquals("NOT_FOUND", hist.getStatus());
            assertEquals("Record missing.", hist.getMessage());
            assertNull(record.getExportedDatestamp());

        }
    }

    @Test
    public void testExportOutcomeError() throws Exception {

        ExportOutcome outcome = new ExportOutcome();
        outcome.setStatus(ExportOutcomeStatus.ERROR);

        record.setExported("previous-value");
        record.setExportedDatestamp(new java.util.Date());

        Repository mockRepository = Mockito.mock(Repository.class);
        Mockito.when(mockRepository.exportRecord(Mockito.any())).thenReturn(outcome);

        try (MockedStatic<HarvesterRepositoryManager> mockedHarvesterRepositoryManager = Mockito.mockStatic(HarvesterRepositoryManager.class)) {
            mockedHarvesterRepositoryManager.when(() -> HarvesterRepositoryManager.getRepository(Mockito.anyInt())).thenReturn(mockRepository);

            ExportHistoryEntry hist = new ExportHistoryEntry(record);
            ExportOutcome result = record.export(hist);

            assertEquals(ExportOutcomeStatus.ERROR, result.getStatus());
            assertEquals("ERROR", hist.getStatus());
            assertNull(record.getExported());
            assertNull(record.getExportedDatestamp());

        }
    }

    @Test
    public void testExportOutcomeSkip() throws Exception {

        ExportOutcome outcome = new ExportOutcome();
        outcome.setStatus(ExportOutcomeStatus.SKIP);

        Repository mockRepository = Mockito.mock(Repository.class);
        Mockito.when(mockRepository.exportRecord(Mockito.any())).thenReturn(outcome);

        try (MockedStatic<HarvesterRepositoryManager> mockedHarvesterRepositoryManager = Mockito.mockStatic(HarvesterRepositoryManager.class)) {
            mockedHarvesterRepositoryManager.when(() -> HarvesterRepositoryManager.getRepository(Mockito.anyInt())).thenReturn(mockRepository);

            ExportHistoryEntry hist = new ExportHistoryEntry(record);
            ExportOutcome result = record.export(hist);

            assertEquals(ExportOutcomeStatus.SKIP, result.getStatus());
            assertEquals("SKIP", hist.getStatus());
            assertEquals("SKIPPED", record.getExported());
            assertNotNull(record.getExportedDatestamp());

        }
    }

}
