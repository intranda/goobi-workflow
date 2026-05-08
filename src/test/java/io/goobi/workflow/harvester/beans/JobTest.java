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
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package io.goobi.workflow.harvester.beans;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.MutableDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.persistence.managers.HarvesterRepositoryManager;
import io.goobi.workflow.harvester.export.ExportOutcome;
import io.goobi.workflow.harvester.repository.Repository;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class JobTest extends AbstractTest {

    private List<Record> recordList;
    private Repository repository;

    @BeforeEach
    public void setUp() throws Exception {

        repository = Mockito.mock(Repository.class);
        //        Record rec = Mockito.mock(Record.class);
        Record rec = new Record();
        recordList = new ArrayList<>();
        recordList.add(rec);

        Mockito.when(repository.isEnabled()).thenReturn(true);
        Mockito.when(repository.isAutoExport()).thenReturn(true);
        Mockito.when(repository.executeScript()).thenReturn(null);

        ExportOutcome out = new ExportOutcome();
        Mockito.when(repository.exportRecord(Mockito.any())).thenReturn(out);

        Mockito.when(repository.harvest(Mockito.anyInt())).thenReturn(1);

    }

    @Test
    public void testJobStatusTypes() {
        try (MockedStatic<HarvesterRepositoryManager> mockedHarvesterRepositoryManager = Mockito.mockStatic(HarvesterRepositoryManager.class)) {
            mockedHarvesterRepositoryManager.when(() -> HarvesterRepositoryManager.getRepository(Mockito.anyInt())).thenReturn(repository);
            mockedHarvesterRepositoryManager
                    .when(() -> HarvesterRepositoryManager.getRecords(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyBoolean(),
                            Mockito.any(), Mockito.anyBoolean()))
                    .thenReturn(recordList);

            assertEquals("WAITING", Job.WAITING);
            assertEquals("WORKING", Job.WORKING);
            assertEquals("DONE", Job.DONE);
            assertEquals("ERROR", Job.ERROR);
            assertEquals("CANCELLED", Job.CANCELLED);

        }
    }

    @Test
    public void testJobConstructor() {
        try (MockedStatic<HarvesterRepositoryManager> mockedHarvesterRepositoryManager = Mockito.mockStatic(HarvesterRepositoryManager.class)) {
            mockedHarvesterRepositoryManager.when(() -> HarvesterRepositoryManager.getRepository(Mockito.anyInt())).thenReturn(repository);
            mockedHarvesterRepositoryManager
                    .when(() -> HarvesterRepositoryManager.getRecords(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyBoolean(),
                            Mockito.any(), Mockito.anyBoolean()))
                    .thenReturn(recordList);

            MutableDateTime mdt = new MutableDateTime();
            Job job = new Job(null, Job.WAITING, 0, "repository", null, new Timestamp(mdt.getMillis()));
            assertNotNull(job);

        }
    }

    @Test
    public void testJobId() {
        try (MockedStatic<HarvesterRepositoryManager> mockedHarvesterRepositoryManager = Mockito.mockStatic(HarvesterRepositoryManager.class)) {
            mockedHarvesterRepositoryManager.when(() -> HarvesterRepositoryManager.getRepository(Mockito.anyInt())).thenReturn(repository);
            mockedHarvesterRepositoryManager
                    .when(() -> HarvesterRepositoryManager.getRecords(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyBoolean(),
                            Mockito.any(), Mockito.anyBoolean()))
                    .thenReturn(recordList);

            MutableDateTime mdt = new MutableDateTime();
            Job job = new Job(null, Job.WAITING, 0, "repository", null, new Timestamp(mdt.getMillis()));
            assertNotNull(job);
            assertNull(job.getId());
            job.setId(1);
            assertEquals(1, job.getId().intValue());

        }
    }

    @Test
    public void testRepositoryId() {
        try (MockedStatic<HarvesterRepositoryManager> mockedHarvesterRepositoryManager = Mockito.mockStatic(HarvesterRepositoryManager.class)) {
            mockedHarvesterRepositoryManager.when(() -> HarvesterRepositoryManager.getRepository(Mockito.anyInt())).thenReturn(repository);
            mockedHarvesterRepositoryManager
                    .when(() -> HarvesterRepositoryManager.getRecords(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyBoolean(),
                            Mockito.any(), Mockito.anyBoolean()))
                    .thenReturn(recordList);

            MutableDateTime mdt = new MutableDateTime();
            Job job = new Job(null, Job.WAITING, 1, "repository", null, new Timestamp(mdt.getMillis()));
            assertNotNull(job);
            assertEquals(1, job.getRepositoryId().intValue());

        }
    }

    @Test
    public void testRepositoryName() {
        try (MockedStatic<HarvesterRepositoryManager> mockedHarvesterRepositoryManager = Mockito.mockStatic(HarvesterRepositoryManager.class)) {
            mockedHarvesterRepositoryManager.when(() -> HarvesterRepositoryManager.getRepository(Mockito.anyInt())).thenReturn(repository);
            mockedHarvesterRepositoryManager
                    .when(() -> HarvesterRepositoryManager.getRecords(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyBoolean(),
                            Mockito.any(), Mockito.anyBoolean()))
                    .thenReturn(recordList);

            MutableDateTime mdt = new MutableDateTime();
            Job job = new Job(null, Job.WAITING, 1, "repository", null, new Timestamp(mdt.getMillis()));
            assertNotNull(job);
            assertEquals("repository", job.getRepositoryName());

        }
    }

    @Test
    public void testStatus() {
        try (MockedStatic<HarvesterRepositoryManager> mockedHarvesterRepositoryManager = Mockito.mockStatic(HarvesterRepositoryManager.class)) {
            mockedHarvesterRepositoryManager.when(() -> HarvesterRepositoryManager.getRepository(Mockito.anyInt())).thenReturn(repository);
            mockedHarvesterRepositoryManager
                    .when(() -> HarvesterRepositoryManager.getRecords(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyBoolean(),
                            Mockito.any(), Mockito.anyBoolean()))
                    .thenReturn(recordList);

            MutableDateTime mdt = new MutableDateTime();
            Job job = new Job(null, Job.WAITING, 1, "repository", null, new Timestamp(mdt.getMillis()));
            assertNotNull(job);
            assertEquals(Job.WAITING, job.getStatus());
            job.setStatus(Job.DONE);
            assertEquals(Job.DONE, job.getStatus());

        }
    }

    @Test
    public void testMessage() {
        try (MockedStatic<HarvesterRepositoryManager> mockedHarvesterRepositoryManager = Mockito.mockStatic(HarvesterRepositoryManager.class)) {
            mockedHarvesterRepositoryManager.when(() -> HarvesterRepositoryManager.getRepository(Mockito.anyInt())).thenReturn(repository);
            mockedHarvesterRepositoryManager
                    .when(() -> HarvesterRepositoryManager.getRecords(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyBoolean(),
                            Mockito.any(), Mockito.anyBoolean()))
                    .thenReturn(recordList);

            MutableDateTime mdt = new MutableDateTime();
            Job job = new Job(null, Job.WAITING, 1, "repository", null, new Timestamp(mdt.getMillis()));
            assertNotNull(job);
            assertNull(job.getMessage());
            job.setMessage("fixture");
            assertEquals("fixture", job.getMessage());

        }
    }

    @Test
    public void testTimestamp() {
        try (MockedStatic<HarvesterRepositoryManager> mockedHarvesterRepositoryManager = Mockito.mockStatic(HarvesterRepositoryManager.class)) {
            mockedHarvesterRepositoryManager.when(() -> HarvesterRepositoryManager.getRepository(Mockito.anyInt())).thenReturn(repository);
            mockedHarvesterRepositoryManager
                    .when(() -> HarvesterRepositoryManager.getRecords(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyBoolean(),
                            Mockito.any(), Mockito.anyBoolean()))
                    .thenReturn(recordList);

            MutableDateTime mdt = new MutableDateTime();
            Job job = new Job(null, Job.WAITING, 1, "repository", null, new Timestamp(mdt.getMillis()));
            assertNotNull(job);
            assertEquals(mdt.getMillis(), job.getTimestamp().getTime());
            job.setTimestamp(new Timestamp(1L));
            assertEquals(1L, job.getTimestamp().getTime());

        }
    }

    @Test
    public void testRun() {
        try (MockedStatic<HarvesterRepositoryManager> mockedHarvesterRepositoryManager = Mockito.mockStatic(HarvesterRepositoryManager.class)) {
            mockedHarvesterRepositoryManager.when(() -> HarvesterRepositoryManager.getRepository(Mockito.anyInt())).thenReturn(repository);
            mockedHarvesterRepositoryManager
                    .when(() -> HarvesterRepositoryManager.getRecords(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyBoolean(),
                            Mockito.any(), Mockito.anyBoolean()))
                    .thenReturn(recordList);

            MutableDateTime mdt = new MutableDateTime();
            Job job = new Job(1, Job.WAITING, 1, "repository", null, new Timestamp(mdt.getMillis()));
            assertEquals(Job.WAITING, job.getStatus());
            job.run(false);
            assertEquals(Job.DONE, job.getStatus());

        }
    }
}
