package de.sub.goobi.persistence.managers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.easymock.EasyMock;
import org.goobi.production.flow.jobs.BackgroundJob;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.AbstractTest;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ BackgroundJobsMysqlHelper.class })
@PowerMockIgnore({ "com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*", "org.w3c.*", "javax.management.*" })
public class BackgroundJobManagerTest extends AbstractTest {

    @Before
    public void setUp() throws Exception {

        PowerMock.mockStatic(BackgroundJobsMysqlHelper.class);
        EasyMock.expect(BackgroundJobsMysqlHelper.getHitSize(EasyMock.anyString())).andReturn(10).anyTimes();

        BackgroundJob job = EasyMock.createMock(BackgroundJob.class);
        List<BackgroundJob> jobs = new ArrayList<>();
        jobs.add(job);

        EasyMock.expect(BackgroundJobsMysqlHelper.getList(EasyMock.anyString(), EasyMock.anyString(), EasyMock.anyInt(), EasyMock.anyInt()))
        .andReturn(jobs)
        .anyTimes();

        List<Integer> ids = new ArrayList<>();
        ids.add(1);
        EasyMock.expect(BackgroundJobsMysqlHelper.getIdList(EasyMock.anyString())).andReturn(ids).anyTimes();


        EasyMock.expect(BackgroundJobsMysqlHelper.getBackgroundJobById(EasyMock.anyInt())).andReturn(job).anyTimes();
        BackgroundJobsMysqlHelper.saveBackgroundJob(EasyMock.anyObject(BackgroundJob.class));
        BackgroundJobManager.deleteBackgroundJob(EasyMock.anyObject(BackgroundJob.class));
        BackgroundJobsMysqlHelper.clearHistoryOlderThan30Days();
        PowerMock.replay(BackgroundJobsMysqlHelper.class);
    }

    @Test
    public void testConstructor() {
        BackgroundJobManager fixture = new BackgroundJobManager();
        assertNotNull(fixture);
    }

    @Test
    public void testGetHitSize() throws Exception {
        BackgroundJobManager fixture = new BackgroundJobManager();
        assertNotNull(fixture);
        assertEquals(10, fixture.getHitSize("", "", null));
    }

    @Test
    public void testGetList() throws Exception {
        BackgroundJobManager fixture = new BackgroundJobManager();
        assertNotNull(fixture);
        assertEquals(1, fixture.getList("", "", 0, 10, null).size());
    }

    @Test
    public void testGetJobs() throws Exception {
        BackgroundJobManager fixture = new BackgroundJobManager();
        assertNotNull(fixture);
        assertEquals(1, fixture.getJobs("", "", 0, 10).size());
    }

    @Test
    public void testGetIdList() throws Exception {
        BackgroundJobManager fixture = new BackgroundJobManager();
        assertNotNull(fixture);
        assertEquals(1, fixture.getIdList("", "", null).size());
    }

    @Test
    public void getBackgroundJobById() {
        BackgroundJob job = BackgroundJobManager.getBackgroundJobById(1);
        assertNotNull(job);
    }

    @Test
    public void saveBackgroundJob() {
        BackgroundJob job = EasyMock.createMock( BackgroundJob.class);
        BackgroundJobManager.saveBackgroundJob(job);
        assertNotNull(job);
    }

    @Test
    public void deleteBackgroundJob() {
        BackgroundJob job = EasyMock.createMock( BackgroundJob.class);
        BackgroundJobManager.deleteBackgroundJob(job);
        assertNotNull(job);

    }

    @Test
    public void clearHistory() {
        BackgroundJobManager.clearHistory();
    }

}
