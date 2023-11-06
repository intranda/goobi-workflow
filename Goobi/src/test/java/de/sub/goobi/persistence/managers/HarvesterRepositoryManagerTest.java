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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.AbstractTest;
import io.goobi.workflow.harvester.beans.Job;
import io.goobi.workflow.harvester.beans.Record;
import io.goobi.workflow.harvester.export.ExportHistoryEntry;
import io.goobi.workflow.harvester.repository.Repository;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ HarvesterRepositoryMysqlHelper.class })
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*" })
public class HarvesterRepositoryManagerTest extends AbstractTest {

    @Before
    public void setUp() throws Exception {
        PowerMock.mockStatic(HarvesterRepositoryMysqlHelper.class);
        Repository repo = new Repository();
        List<Repository> repoList = new ArrayList<>();
        repoList.add(repo);

        EasyMock.expect(HarvesterRepositoryMysqlHelper.getRepository(EasyMock.anyInt())).andReturn(repo).anyTimes();

        EasyMock.expect(HarvesterRepositoryMysqlHelper.getRepositoryCount(EasyMock.anyString())).andReturn(5).anyTimes();

        EasyMock.expect(HarvesterRepositoryMysqlHelper.getRepositories(EasyMock.anyString())).andReturn(repoList).anyTimes();
        HarvesterRepositoryMysqlHelper.saveRepository(EasyMock.anyObject());
        HarvesterRepositoryMysqlHelper.deleteRepository(EasyMock.anyObject());

        Job job = new Job(1, Job.WAITING, 0, "repository", null, new Timestamp(1l));
        EasyMock.expect(HarvesterRepositoryMysqlHelper.addNewJob(EasyMock.anyObject())).andReturn(job).anyTimes();
        HarvesterRepositoryMysqlHelper.updateJobStatus(EasyMock.anyObject());

        EasyMock.expect(HarvesterRepositoryMysqlHelper.getLastHarvest(EasyMock.anyInt())).andReturn(new Timestamp(1l)).anyTimes();
        EasyMock.expect(HarvesterRepositoryMysqlHelper.addRecords(EasyMock.anyObject(), EasyMock.anyBoolean())).andReturn(5).anyTimes();

        List<String> idList = new ArrayList<>();
        idList.add("1");
        idList.add("2");
        EasyMock.expect(HarvesterRepositoryMysqlHelper.getExistingIdentifier(EasyMock.anyObject())).andReturn(idList).anyTimes();

        List<Record> recordList = new ArrayList<>();
        Record rec = new Record();
        recordList.add(rec);

        EasyMock.expect(HarvesterRepositoryMysqlHelper.getRecords(EasyMock.anyInt(), EasyMock.anyInt(), EasyMock.anyString(), EasyMock.anyBoolean(),
                EasyMock.anyObject(), EasyMock.anyBoolean())).andReturn(recordList).anyTimes();

        HarvesterRepositoryMysqlHelper.addExportHistoryEntry(EasyMock.anyObject());
        HarvesterRepositoryMysqlHelper.updateLastHarvestingTime(EasyMock.anyInt(), EasyMock.anyObject());
        HarvesterRepositoryMysqlHelper.setRecordExported(EasyMock.anyObject());
        HarvesterRepositoryMysqlHelper.changeStatusOfRepository(EasyMock.anyObject());

        PowerMock.replay(HarvesterRepositoryMysqlHelper.class);
    }

    @Test
    public void testConstructor() {
        HarvesterRepositoryManager fixture = new HarvesterRepositoryManager();
        assertNotNull(fixture);
    }

    @Test
    public void testGetHitSize() throws Exception {
        HarvesterRepositoryManager fixture = new HarvesterRepositoryManager();
        assertNotNull(fixture);
        assertEquals(5, fixture.getHitSize("", "", null));
    }

    @Test
    public void testGetList() throws Exception {
        HarvesterRepositoryManager fixture = new HarvesterRepositoryManager();
        assertNotNull(fixture);
        assertEquals(1, fixture.getList("", "", 0, 10, null).size());
    }

    @Test
    public void testGetIdList() throws Exception {
        HarvesterRepositoryManager fixture = new HarvesterRepositoryManager();
        assertNotNull(fixture);
        assertTrue(fixture.getIdList("", "", null).isEmpty());
    }

    @Test
    public void testGetRepository() throws Exception {
        Repository repo = HarvesterRepositoryManager.getRepository(1);
        assertNotNull(repo);
    }

    @Test
    public void testSaveRepository() throws Exception {
        Repository repo = new Repository();
        HarvesterRepositoryManager.saveRepository(repo);
        assertNotNull(repo);
    }

    @Test
    public void testDeleteRepository() throws Exception {
        Repository repo = new Repository();
        HarvesterRepositoryManager.deleteRepository(repo);
        assertNotNull(repo);
    }

    @Test
    public void testChangeStatusOfRepository() throws Exception {
        Repository repo = new Repository();
        HarvesterRepositoryManager.changeStatusOfRepository(repo);
        assertNotNull(repo);
    }

    @Test
    public void testAddRecords() throws Exception {
        List<Record> recordList = new ArrayList<>();
        assertEquals(5, HarvesterRepositoryManager.addRecords(recordList, false));
        assertEquals(5, HarvesterRepositoryManager.addRecords(recordList, true));
    }

    @Test
    public void testGetExistingIdentifier() throws Exception {
        List<String> recordList = new ArrayList<>();
        recordList.add("1");
        recordList.add("2");
        recordList.add("3");
        assertEquals(2, HarvesterRepositoryManager.getExistingIdentifier(recordList).size());
    }

    @Test
    public void testAddNewJob() throws Exception {
        Job job = new Job(null, Job.WAITING, 0, "repository", null, new Timestamp(1l));
        assertNull(job.getId());
        assertEquals(1, HarvesterRepositoryManager.addNewJob(job).getId().intValue());
    }

    @Test
    public void testUpdateJobStatus() throws Exception {
        Job job = new Job(null, Job.WAITING, 0, "repository", null, new Timestamp(1l));
        HarvesterRepositoryManager.updateJobStatus(job);
        assertNotNull(job);
    }

    @Test
    public void testGetRecords() throws Exception {
        assertEquals(1, HarvesterRepositoryManager.getRecords(1, 10, "", false, null, false).size());
    }

    @Test
    public void testAddExportHistoryEntry() throws Exception {
        Record rec = new Record();
        ExportHistoryEntry hist = new ExportHistoryEntry(rec);
        HarvesterRepositoryManager.addExportHistoryEntry(hist);
        assertNotNull(hist);
    }



    @Test
    public void testUpdateLastHarvestingTime() throws Exception {
        Timestamp ts = new Timestamp(1l);
        HarvesterRepositoryManager.updateLastHarvestingTime(1, ts);
        assertNotNull(ts);
    }

    @Test
    public void testSetRecordExported() throws Exception {
        Record rec = new Record();
        HarvesterRepositoryManager.setRecordExported(rec);
        assertNotNull(rec);
    }


}
