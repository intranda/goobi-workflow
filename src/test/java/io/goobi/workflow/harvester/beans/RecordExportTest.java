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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.persistence.managers.HarvesterRepositoryManager;
import io.goobi.workflow.harvester.export.ExportHistoryEntry;
import io.goobi.workflow.harvester.export.ExportOutcome;
import io.goobi.workflow.harvester.export.ExportOutcome.ExportOutcomeStatus;
import io.goobi.workflow.harvester.repository.Repository;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ HarvesterRepositoryManager.class })
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*" })
public class RecordExportTest extends AbstractTest {

    private Record record;

    @Before
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

        Repository mockRepository = EasyMock.createMock(Repository.class);
        EasyMock.expect(mockRepository.exportRecord(EasyMock.anyObject())).andReturn(outcome);

        PowerMock.mockStatic(HarvesterRepositoryManager.class);
        EasyMock.expect(HarvesterRepositoryManager.getRepository(EasyMock.anyInt())).andReturn(mockRepository);
        HarvesterRepositoryManager.setRecordExported(EasyMock.anyObject());
        EasyMock.expectLastCall().once();

        EasyMock.replay(mockRepository);
        PowerMock.replay(HarvesterRepositoryManager.class);

        ExportHistoryEntry hist = new ExportHistoryEntry(record);
        ExportOutcome result = record.export(hist);

        assertEquals(ExportOutcomeStatus.OK, result.getStatus());
        assertEquals("OK", hist.getStatus());
        assertNotNull(record.getExportedDatestamp());

        EasyMock.verify(mockRepository);
        PowerMock.verify(HarvesterRepositoryManager.class);
    }

    @Test
    public void testExportOutcomeNotFound() throws Exception {
        ExportOutcome outcome = new ExportOutcome();
        outcome.setStatus(ExportOutcomeStatus.NOT_FOUND);

        Repository mockRepository = EasyMock.createMock(Repository.class);
        EasyMock.expect(mockRepository.exportRecord(EasyMock.anyObject())).andReturn(outcome);

        PowerMock.mockStatic(HarvesterRepositoryManager.class);
        EasyMock.expect(HarvesterRepositoryManager.getRepository(EasyMock.anyInt())).andReturn(mockRepository);

        EasyMock.replay(mockRepository);
        PowerMock.replay(HarvesterRepositoryManager.class);

        ExportHistoryEntry hist = new ExportHistoryEntry(record);
        ExportOutcome result = record.export(hist);

        assertEquals(ExportOutcomeStatus.NOT_FOUND, result.getStatus());
        assertEquals("NOT_FOUND", hist.getStatus());
        assertEquals("Record missing.", hist.getMessage());
        assertNull(record.getExportedDatestamp());

        EasyMock.verify(mockRepository);
        PowerMock.verify(HarvesterRepositoryManager.class);
    }

    @Test
    public void testExportOutcomeError() throws Exception {
        ExportOutcome outcome = new ExportOutcome();
        outcome.setStatus(ExportOutcomeStatus.ERROR);

        record.setExported("previous-value");
        record.setExportedDatestamp(new java.util.Date());

        Repository mockRepository = EasyMock.createMock(Repository.class);
        EasyMock.expect(mockRepository.exportRecord(EasyMock.anyObject())).andReturn(outcome);

        PowerMock.mockStatic(HarvesterRepositoryManager.class);
        EasyMock.expect(HarvesterRepositoryManager.getRepository(EasyMock.anyInt())).andReturn(mockRepository);

        EasyMock.replay(mockRepository);
        PowerMock.replay(HarvesterRepositoryManager.class);

        ExportHistoryEntry hist = new ExportHistoryEntry(record);
        ExportOutcome result = record.export(hist);

        assertEquals(ExportOutcomeStatus.ERROR, result.getStatus());
        assertEquals("ERROR", hist.getStatus());
        assertNull(record.getExported());
        assertNull(record.getExportedDatestamp());

        EasyMock.verify(mockRepository);
        PowerMock.verify(HarvesterRepositoryManager.class);
    }

    @Test
    public void testExportOutcomeSkip() throws Exception {
        ExportOutcome outcome = new ExportOutcome();
        outcome.setStatus(ExportOutcomeStatus.SKIP);

        Repository mockRepository = EasyMock.createMock(Repository.class);
        EasyMock.expect(mockRepository.exportRecord(EasyMock.anyObject())).andReturn(outcome);

        PowerMock.mockStatic(HarvesterRepositoryManager.class);
        EasyMock.expect(HarvesterRepositoryManager.getRepository(EasyMock.anyInt())).andReturn(mockRepository);
        HarvesterRepositoryManager.setRecordExported(EasyMock.anyObject());
        EasyMock.expectLastCall().once();

        EasyMock.replay(mockRepository);
        PowerMock.replay(HarvesterRepositoryManager.class);

        ExportHistoryEntry hist = new ExportHistoryEntry(record);
        ExportOutcome result = record.export(hist);

        assertEquals(ExportOutcomeStatus.SKIP, result.getStatus());
        assertEquals("SKIP", hist.getStatus());
        assertEquals("SKIPPED", record.getExported());
        assertNotNull(record.getExportedDatestamp());

        EasyMock.verify(mockRepository);
        PowerMock.verify(HarvesterRepositoryManager.class);
    }

}
