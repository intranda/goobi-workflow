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

package de.sub.goobi.persistence.managers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.sub.goobi.AbstractTest;
import io.goobi.workflow.harvester.beans.Job;
import io.goobi.workflow.harvester.beans.Record;
import io.goobi.workflow.harvester.export.ExportHistoryEntry;
import io.goobi.workflow.harvester.repository.Repository;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
@ExtendWith(MockitoExtension.class)
public class HarvesterRepositoryManagerTest extends AbstractTest {

    private List<String> idList;
    private Job job;
    private Record rec;
    private List<Record> recordList;
    private Repository repo;
    private List<Repository> repoList;

    @BeforeEach
    public void setUp() throws Exception {
        repo = new Repository();
        repoList = new ArrayList<>();
        repoList.add(repo);




        job = new Job(1, Job.WAITING, 0, "repository", null, new Timestamp(1L));


        idList = new ArrayList<>();
        idList.add("1");
        idList.add("2");

        recordList = new ArrayList<>();
        rec = new Record();
        recordList.add(rec);



    }

    @Test
    public void testConstructor() {
        try (MockedStatic<HarvesterRepositoryMysqlHelper> mockedHarvesterRepositoryMysqlHelper = Mockito.mockStatic(HarvesterRepositoryMysqlHelper.class)) {
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepository(Mockito.anyInt())).thenReturn(repo);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositoryCount(Mockito.anyString())).thenReturn(5);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositories(Mockito.anyString())).thenReturn(repoList);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.addNewJob(Mockito.any())).thenReturn(job);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getLastHarvest(Mockito.anyInt())).thenReturn(new Timestamp(1L));
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.addRecords(Mockito.any(), Mockito.anyBoolean())).thenReturn(5);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getExistingIdentifier(Mockito.any())).thenReturn(idList);
                        mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRecords(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyBoolean(),
                Mockito.any(), Mockito.anyBoolean())).thenReturn(recordList);


            HarvesterRepositoryManager fixture = new HarvesterRepositoryManager();
            assertNotNull(fixture);
    
        }
}

    @Test
    public void testGetHitSize() throws Exception {
        try (MockedStatic<HarvesterRepositoryMysqlHelper> mockedHarvesterRepositoryMysqlHelper = Mockito.mockStatic(HarvesterRepositoryMysqlHelper.class)) {
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepository(Mockito.anyInt())).thenReturn(repo);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositoryCount(Mockito.anyString())).thenReturn(5);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositories(Mockito.anyString())).thenReturn(repoList);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.addNewJob(Mockito.any())).thenReturn(job);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getLastHarvest(Mockito.anyInt())).thenReturn(new Timestamp(1L));
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.addRecords(Mockito.any(), Mockito.anyBoolean())).thenReturn(5);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getExistingIdentifier(Mockito.any())).thenReturn(idList);
                        mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRecords(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyBoolean(),
                Mockito.any(), Mockito.anyBoolean())).thenReturn(recordList);


            HarvesterRepositoryManager fixture = new HarvesterRepositoryManager();
            assertNotNull(fixture);
            assertEquals(5, fixture.getHitSize("", "", null));
    
        }
}

    @Test
    public void testGetList() throws Exception {
        try (MockedStatic<HarvesterRepositoryMysqlHelper> mockedHarvesterRepositoryMysqlHelper = Mockito.mockStatic(HarvesterRepositoryMysqlHelper.class)) {
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepository(Mockito.anyInt())).thenReturn(repo);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositoryCount(Mockito.anyString())).thenReturn(5);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositories(Mockito.anyString())).thenReturn(repoList);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.addNewJob(Mockito.any())).thenReturn(job);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getLastHarvest(Mockito.anyInt())).thenReturn(new Timestamp(1L));
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.addRecords(Mockito.any(), Mockito.anyBoolean())).thenReturn(5);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getExistingIdentifier(Mockito.any())).thenReturn(idList);
                        mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRecords(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyBoolean(),
                Mockito.any(), Mockito.anyBoolean())).thenReturn(recordList);


            HarvesterRepositoryManager fixture = new HarvesterRepositoryManager();
            assertNotNull(fixture);
            assertEquals(1, fixture.getList("", "", 0, 10, null).size());
    
        }
}

    @Test
    public void testGetIdList() throws Exception {
        try (MockedStatic<HarvesterRepositoryMysqlHelper> mockedHarvesterRepositoryMysqlHelper = Mockito.mockStatic(HarvesterRepositoryMysqlHelper.class)) {
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepository(Mockito.anyInt())).thenReturn(repo);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositoryCount(Mockito.anyString())).thenReturn(5);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositories(Mockito.anyString())).thenReturn(repoList);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.addNewJob(Mockito.any())).thenReturn(job);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getLastHarvest(Mockito.anyInt())).thenReturn(new Timestamp(1L));
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.addRecords(Mockito.any(), Mockito.anyBoolean())).thenReturn(5);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getExistingIdentifier(Mockito.any())).thenReturn(idList);
                        mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRecords(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyBoolean(),
                Mockito.any(), Mockito.anyBoolean())).thenReturn(recordList);


            HarvesterRepositoryManager fixture = new HarvesterRepositoryManager();
            assertNotNull(fixture);
            assertTrue(fixture.getIdList("", "", null).isEmpty());
    
        }
}

    @Test
    public void testGetRepository() throws Exception {
        try (MockedStatic<HarvesterRepositoryMysqlHelper> mockedHarvesterRepositoryMysqlHelper = Mockito.mockStatic(HarvesterRepositoryMysqlHelper.class)) {
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepository(Mockito.anyInt())).thenReturn(repo);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositoryCount(Mockito.anyString())).thenReturn(5);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositories(Mockito.anyString())).thenReturn(repoList);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.addNewJob(Mockito.any())).thenReturn(job);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getLastHarvest(Mockito.anyInt())).thenReturn(new Timestamp(1L));
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.addRecords(Mockito.any(), Mockito.anyBoolean())).thenReturn(5);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getExistingIdentifier(Mockito.any())).thenReturn(idList);
                        mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRecords(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyBoolean(),
                Mockito.any(), Mockito.anyBoolean())).thenReturn(recordList);


            Repository repo = HarvesterRepositoryManager.getRepository(1);
            assertNotNull(repo);
    
        }
}

    @Test
    public void testSaveRepository() throws Exception {
        try (MockedStatic<HarvesterRepositoryMysqlHelper> mockedHarvesterRepositoryMysqlHelper = Mockito.mockStatic(HarvesterRepositoryMysqlHelper.class)) {
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepository(Mockito.anyInt())).thenReturn(repo);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositoryCount(Mockito.anyString())).thenReturn(5);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositories(Mockito.anyString())).thenReturn(repoList);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.addNewJob(Mockito.any())).thenReturn(job);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getLastHarvest(Mockito.anyInt())).thenReturn(new Timestamp(1L));
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.addRecords(Mockito.any(), Mockito.anyBoolean())).thenReturn(5);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getExistingIdentifier(Mockito.any())).thenReturn(idList);
                        mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRecords(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyBoolean(),
                Mockito.any(), Mockito.anyBoolean())).thenReturn(recordList);


            Repository repo = new Repository();
            HarvesterRepositoryManager.saveRepository(repo);
            assertNotNull(repo);
    
        }
}

    @Test
    public void testDeleteRepository() throws Exception {
        try (MockedStatic<HarvesterRepositoryMysqlHelper> mockedHarvesterRepositoryMysqlHelper = Mockito.mockStatic(HarvesterRepositoryMysqlHelper.class)) {
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepository(Mockito.anyInt())).thenReturn(repo);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositoryCount(Mockito.anyString())).thenReturn(5);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositories(Mockito.anyString())).thenReturn(repoList);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.addNewJob(Mockito.any())).thenReturn(job);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getLastHarvest(Mockito.anyInt())).thenReturn(new Timestamp(1L));
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.addRecords(Mockito.any(), Mockito.anyBoolean())).thenReturn(5);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getExistingIdentifier(Mockito.any())).thenReturn(idList);
                        mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRecords(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyBoolean(),
                Mockito.any(), Mockito.anyBoolean())).thenReturn(recordList);


            Repository repo = new Repository();
            HarvesterRepositoryManager.deleteRepository(repo);
            assertNotNull(repo);
    
        }
}

    @Test
    public void testChangeStatusOfRepository() throws Exception {
        try (MockedStatic<HarvesterRepositoryMysqlHelper> mockedHarvesterRepositoryMysqlHelper = Mockito.mockStatic(HarvesterRepositoryMysqlHelper.class)) {
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepository(Mockito.anyInt())).thenReturn(repo);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositoryCount(Mockito.anyString())).thenReturn(5);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositories(Mockito.anyString())).thenReturn(repoList);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.addNewJob(Mockito.any())).thenReturn(job);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getLastHarvest(Mockito.anyInt())).thenReturn(new Timestamp(1L));
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.addRecords(Mockito.any(), Mockito.anyBoolean())).thenReturn(5);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getExistingIdentifier(Mockito.any())).thenReturn(idList);
                        mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRecords(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyBoolean(),
                Mockito.any(), Mockito.anyBoolean())).thenReturn(recordList);


            Repository repo = new Repository();
            HarvesterRepositoryManager.changeStatusOfRepository(repo);
            assertNotNull(repo);
    
        }
}

    @Test
    public void testAddRecords() throws Exception {
        try (MockedStatic<HarvesterRepositoryMysqlHelper> mockedHarvesterRepositoryMysqlHelper = Mockito.mockStatic(HarvesterRepositoryMysqlHelper.class)) {
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepository(Mockito.anyInt())).thenReturn(repo);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositoryCount(Mockito.anyString())).thenReturn(5);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositories(Mockito.anyString())).thenReturn(repoList);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.addNewJob(Mockito.any())).thenReturn(job);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getLastHarvest(Mockito.anyInt())).thenReturn(new Timestamp(1L));
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.addRecords(Mockito.any(), Mockito.anyBoolean())).thenReturn(5);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getExistingIdentifier(Mockito.any())).thenReturn(idList);
                        mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRecords(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyBoolean(),
                Mockito.any(), Mockito.anyBoolean())).thenReturn(recordList);


            List<Record> recordList = new ArrayList<>();
            assertEquals(5, HarvesterRepositoryManager.addRecords(recordList, false));
            assertEquals(5, HarvesterRepositoryManager.addRecords(recordList, true));
    
        }
}

    @Test
    public void testGetExistingIdentifier() throws Exception {
        try (MockedStatic<HarvesterRepositoryMysqlHelper> mockedHarvesterRepositoryMysqlHelper = Mockito.mockStatic(HarvesterRepositoryMysqlHelper.class)) {
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepository(Mockito.anyInt())).thenReturn(repo);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositoryCount(Mockito.anyString())).thenReturn(5);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositories(Mockito.anyString())).thenReturn(repoList);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.addNewJob(Mockito.any())).thenReturn(job);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getLastHarvest(Mockito.anyInt())).thenReturn(new Timestamp(1L));
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.addRecords(Mockito.any(), Mockito.anyBoolean())).thenReturn(5);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getExistingIdentifier(Mockito.any())).thenReturn(idList);
                        mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRecords(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyBoolean(),
                Mockito.any(), Mockito.anyBoolean())).thenReturn(recordList);


            List<String> recordList = new ArrayList<>();
            recordList.add("1");
            recordList.add("2");
            recordList.add("3");
            assertEquals(2, HarvesterRepositoryManager.getExistingIdentifier(recordList).size());
    
        }
}

    @Test
    public void testAddNewJob() throws Exception {
        try (MockedStatic<HarvesterRepositoryMysqlHelper> mockedHarvesterRepositoryMysqlHelper = Mockito.mockStatic(HarvesterRepositoryMysqlHelper.class)) {
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepository(Mockito.anyInt())).thenReturn(repo);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositoryCount(Mockito.anyString())).thenReturn(5);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositories(Mockito.anyString())).thenReturn(repoList);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.addNewJob(Mockito.any())).thenReturn(job);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getLastHarvest(Mockito.anyInt())).thenReturn(new Timestamp(1L));
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.addRecords(Mockito.any(), Mockito.anyBoolean())).thenReturn(5);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getExistingIdentifier(Mockito.any())).thenReturn(idList);
                        mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRecords(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyBoolean(),
                Mockito.any(), Mockito.anyBoolean())).thenReturn(recordList);


            Job job = new Job(null, Job.WAITING, 0, "repository", null, new Timestamp(1L));
            assertNull(job.getId());
            assertEquals(1, HarvesterRepositoryManager.addNewJob(job).getId().intValue());
    
        }
}

    @Test
    public void testUpdateJobStatus() throws Exception {
        try (MockedStatic<HarvesterRepositoryMysqlHelper> mockedHarvesterRepositoryMysqlHelper = Mockito.mockStatic(HarvesterRepositoryMysqlHelper.class)) {
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepository(Mockito.anyInt())).thenReturn(repo);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositoryCount(Mockito.anyString())).thenReturn(5);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositories(Mockito.anyString())).thenReturn(repoList);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.addNewJob(Mockito.any())).thenReturn(job);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getLastHarvest(Mockito.anyInt())).thenReturn(new Timestamp(1L));
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.addRecords(Mockito.any(), Mockito.anyBoolean())).thenReturn(5);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getExistingIdentifier(Mockito.any())).thenReturn(idList);
                        mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRecords(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyBoolean(),
                Mockito.any(), Mockito.anyBoolean())).thenReturn(recordList);


            Job job = new Job(null, Job.WAITING, 0, "repository", null, new Timestamp(1L));
            HarvesterRepositoryManager.updateJobStatus(job);
            assertNotNull(job);
    
        }
}

    @Test
    public void testGetRecords() throws Exception {
        try (MockedStatic<HarvesterRepositoryMysqlHelper> mockedHarvesterRepositoryMysqlHelper = Mockito.mockStatic(HarvesterRepositoryMysqlHelper.class)) {
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepository(Mockito.anyInt())).thenReturn(repo);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositoryCount(Mockito.anyString())).thenReturn(5);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositories(Mockito.anyString())).thenReturn(repoList);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.addNewJob(Mockito.any())).thenReturn(job);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getLastHarvest(Mockito.anyInt())).thenReturn(new Timestamp(1L));
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.addRecords(Mockito.any(), Mockito.anyBoolean())).thenReturn(5);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getExistingIdentifier(Mockito.any())).thenReturn(idList);
                        mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRecords(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyBoolean(),
                Mockito.any(), Mockito.anyBoolean())).thenReturn(recordList);


            assertEquals(1, HarvesterRepositoryManager.getRecords(1, 10, "", false, null, false).size());
    
        }
}

    @Test
    public void testAddExportHistoryEntry() throws Exception {
        try (MockedStatic<HarvesterRepositoryMysqlHelper> mockedHarvesterRepositoryMysqlHelper = Mockito.mockStatic(HarvesterRepositoryMysqlHelper.class)) {
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepository(Mockito.anyInt())).thenReturn(repo);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositoryCount(Mockito.anyString())).thenReturn(5);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositories(Mockito.anyString())).thenReturn(repoList);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.addNewJob(Mockito.any())).thenReturn(job);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getLastHarvest(Mockito.anyInt())).thenReturn(new Timestamp(1L));
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.addRecords(Mockito.any(), Mockito.anyBoolean())).thenReturn(5);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getExistingIdentifier(Mockito.any())).thenReturn(idList);
                        mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRecords(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyBoolean(),
                Mockito.any(), Mockito.anyBoolean())).thenReturn(recordList);


            Record rec = new Record();
            ExportHistoryEntry hist = new ExportHistoryEntry(rec);
            HarvesterRepositoryManager.addExportHistoryEntry(hist);
            assertNotNull(hist);
    
        }
}

    @Test
    public void testUpdateLastHarvestingTime() throws Exception {
        try (MockedStatic<HarvesterRepositoryMysqlHelper> mockedHarvesterRepositoryMysqlHelper = Mockito.mockStatic(HarvesterRepositoryMysqlHelper.class)) {
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepository(Mockito.anyInt())).thenReturn(repo);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositoryCount(Mockito.anyString())).thenReturn(5);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositories(Mockito.anyString())).thenReturn(repoList);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.addNewJob(Mockito.any())).thenReturn(job);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getLastHarvest(Mockito.anyInt())).thenReturn(new Timestamp(1L));
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.addRecords(Mockito.any(), Mockito.anyBoolean())).thenReturn(5);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getExistingIdentifier(Mockito.any())).thenReturn(idList);
                        mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRecords(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyBoolean(),
                Mockito.any(), Mockito.anyBoolean())).thenReturn(recordList);


            Timestamp ts = new Timestamp(1L);
            HarvesterRepositoryManager.updateLastHarvestingTime(1, ts);
            assertNotNull(ts);
    
        }
}

    @Test
    public void testSetRecordExported() throws Exception {
        try (MockedStatic<HarvesterRepositoryMysqlHelper> mockedHarvesterRepositoryMysqlHelper = Mockito.mockStatic(HarvesterRepositoryMysqlHelper.class)) {
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepository(Mockito.anyInt())).thenReturn(repo);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositoryCount(Mockito.anyString())).thenReturn(5);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRepositories(Mockito.anyString())).thenReturn(repoList);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.addNewJob(Mockito.any())).thenReturn(job);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getLastHarvest(Mockito.anyInt())).thenReturn(new Timestamp(1L));
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.addRecords(Mockito.any(), Mockito.anyBoolean())).thenReturn(5);
            mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getExistingIdentifier(Mockito.any())).thenReturn(idList);
                        mockedHarvesterRepositoryMysqlHelper.when(() -> HarvesterRepositoryMysqlHelper.getRecords(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyBoolean(),
                Mockito.any(), Mockito.anyBoolean())).thenReturn(recordList);


            Record rec = new Record();
            HarvesterRepositoryManager.setRecordExported(rec);
            assertNotNull(rec);
    
        }
}

}
