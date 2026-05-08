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

import java.util.ArrayList;
import java.util.List;

import org.goobi.production.flow.jobs.BackgroundJob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import de.sub.goobi.AbstractTest;

@ExtendWith(MockitoExtension.class)
public class BackgroundJobManagerTest extends AbstractTest {

    private List<Integer> ids;
    private BackgroundJob job;
    private List<BackgroundJob> jobs;

    @BeforeEach
    public void setUp() throws Exception {

        job = Mockito.mock(BackgroundJob.class);
        jobs = new ArrayList<>();
        jobs.add(job);

        ids = new ArrayList<>();
        ids.add(1);
    }

    @Test
    public void testConstructor() {
        try (MockedStatic<BackgroundJobsMysqlHelper> mockedBackgroundJobsMysqlHelper = Mockito.mockStatic(BackgroundJobsMysqlHelper.class)) {
            mockedBackgroundJobsMysqlHelper.when(() -> BackgroundJobsMysqlHelper.getHitSize(Mockito.anyString())).thenReturn(10);
            mockedBackgroundJobsMysqlHelper
                    .when(() -> BackgroundJobsMysqlHelper.getList(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt()))
                    .thenReturn(jobs);
            mockedBackgroundJobsMysqlHelper.when(() -> BackgroundJobsMysqlHelper.getIdList(Mockito.anyString())).thenReturn(ids);
            mockedBackgroundJobsMysqlHelper.when(() -> BackgroundJobsMysqlHelper.getBackgroundJobById(Mockito.anyInt())).thenReturn(job);

            BackgroundJobManager fixture = new BackgroundJobManager();
            assertNotNull(fixture);

        }
    }

    @Test
    public void testGetHitSize() throws Exception {
        try (MockedStatic<BackgroundJobsMysqlHelper> mockedBackgroundJobsMysqlHelper = Mockito.mockStatic(BackgroundJobsMysqlHelper.class)) {
            mockedBackgroundJobsMysqlHelper.when(() -> BackgroundJobsMysqlHelper.getHitSize(Mockito.anyString())).thenReturn(10);
            mockedBackgroundJobsMysqlHelper
                    .when(() -> BackgroundJobsMysqlHelper.getList(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt()))
                    .thenReturn(jobs);
            mockedBackgroundJobsMysqlHelper.when(() -> BackgroundJobsMysqlHelper.getIdList(Mockito.anyString())).thenReturn(ids);
            mockedBackgroundJobsMysqlHelper.when(() -> BackgroundJobsMysqlHelper.getBackgroundJobById(Mockito.anyInt())).thenReturn(job);

            BackgroundJobManager fixture = new BackgroundJobManager();
            assertNotNull(fixture);
            assertEquals(10, fixture.getHitSize("", "", null));

        }
    }

    @Test
    public void testGetList() throws Exception {
        try (MockedStatic<BackgroundJobsMysqlHelper> mockedBackgroundJobsMysqlHelper = Mockito.mockStatic(BackgroundJobsMysqlHelper.class)) {
            mockedBackgroundJobsMysqlHelper.when(() -> BackgroundJobsMysqlHelper.getHitSize(Mockito.anyString())).thenReturn(10);
            mockedBackgroundJobsMysqlHelper
                    .when(() -> BackgroundJobsMysqlHelper.getList(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt()))
                    .thenReturn(jobs);
            mockedBackgroundJobsMysqlHelper.when(() -> BackgroundJobsMysqlHelper.getIdList(Mockito.anyString())).thenReturn(ids);
            mockedBackgroundJobsMysqlHelper.when(() -> BackgroundJobsMysqlHelper.getBackgroundJobById(Mockito.anyInt())).thenReturn(job);

            BackgroundJobManager fixture = new BackgroundJobManager();
            assertNotNull(fixture);
            assertEquals(1, fixture.getList("", "", 0, 10, null).size());

        }
    }

    @Test
    public void testGetJobs() throws Exception {
        try (MockedStatic<BackgroundJobsMysqlHelper> mockedBackgroundJobsMysqlHelper = Mockito.mockStatic(BackgroundJobsMysqlHelper.class)) {
            mockedBackgroundJobsMysqlHelper.when(() -> BackgroundJobsMysqlHelper.getHitSize(Mockito.anyString())).thenReturn(10);
            mockedBackgroundJobsMysqlHelper
                    .when(() -> BackgroundJobsMysqlHelper.getList(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt()))
                    .thenReturn(jobs);
            mockedBackgroundJobsMysqlHelper.when(() -> BackgroundJobsMysqlHelper.getIdList(Mockito.anyString())).thenReturn(ids);
            mockedBackgroundJobsMysqlHelper.when(() -> BackgroundJobsMysqlHelper.getBackgroundJobById(Mockito.anyInt())).thenReturn(job);

            BackgroundJobManager fixture = new BackgroundJobManager();
            assertNotNull(fixture);
            assertEquals(1, fixture.getJobs("", "", 0, 10).size());

        }
    }

    @Test
    public void testGetIdList() throws Exception {
        try (MockedStatic<BackgroundJobsMysqlHelper> mockedBackgroundJobsMysqlHelper = Mockito.mockStatic(BackgroundJobsMysqlHelper.class)) {
            mockedBackgroundJobsMysqlHelper.when(() -> BackgroundJobsMysqlHelper.getHitSize(Mockito.anyString())).thenReturn(10);
            mockedBackgroundJobsMysqlHelper
                    .when(() -> BackgroundJobsMysqlHelper.getList(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt()))
                    .thenReturn(jobs);
            mockedBackgroundJobsMysqlHelper.when(() -> BackgroundJobsMysqlHelper.getIdList(Mockito.anyString())).thenReturn(ids);
            mockedBackgroundJobsMysqlHelper.when(() -> BackgroundJobsMysqlHelper.getBackgroundJobById(Mockito.anyInt())).thenReturn(job);

            BackgroundJobManager fixture = new BackgroundJobManager();
            assertNotNull(fixture);
            assertEquals(1, fixture.getIdList("", "", null).size());

        }
    }

    @Test
    public void getBackgroundJobById() {
        try (MockedStatic<BackgroundJobsMysqlHelper> mockedBackgroundJobsMysqlHelper = Mockito.mockStatic(BackgroundJobsMysqlHelper.class)) {
            mockedBackgroundJobsMysqlHelper.when(() -> BackgroundJobsMysqlHelper.getHitSize(Mockito.anyString())).thenReturn(10);
            mockedBackgroundJobsMysqlHelper
                    .when(() -> BackgroundJobsMysqlHelper.getList(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt()))
                    .thenReturn(jobs);
            mockedBackgroundJobsMysqlHelper.when(() -> BackgroundJobsMysqlHelper.getIdList(Mockito.anyString())).thenReturn(ids);
            mockedBackgroundJobsMysqlHelper.when(() -> BackgroundJobsMysqlHelper.getBackgroundJobById(Mockito.anyInt())).thenReturn(job);

            BackgroundJob job = BackgroundJobManager.getBackgroundJobById(1);
            assertNotNull(job);

        }
    }

    @Test
    public void saveBackgroundJob() {
        try (MockedStatic<BackgroundJobsMysqlHelper> mockedBackgroundJobsMysqlHelper = Mockito.mockStatic(BackgroundJobsMysqlHelper.class)) {
            mockedBackgroundJobsMysqlHelper.when(() -> BackgroundJobsMysqlHelper.getHitSize(Mockito.anyString())).thenReturn(10);
            mockedBackgroundJobsMysqlHelper
                    .when(() -> BackgroundJobsMysqlHelper.getList(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt()))
                    .thenReturn(jobs);
            mockedBackgroundJobsMysqlHelper.when(() -> BackgroundJobsMysqlHelper.getIdList(Mockito.anyString())).thenReturn(ids);
            mockedBackgroundJobsMysqlHelper.when(() -> BackgroundJobsMysqlHelper.getBackgroundJobById(Mockito.anyInt())).thenReturn(job);

            BackgroundJob job = Mockito.mock(BackgroundJob.class);
            BackgroundJobManager.saveBackgroundJob(job);
            assertNotNull(job);

        }
    }

    @Test
    public void deleteBackgroundJob() {
        try (MockedStatic<BackgroundJobsMysqlHelper> mockedBackgroundJobsMysqlHelper = Mockito.mockStatic(BackgroundJobsMysqlHelper.class)) {
            mockedBackgroundJobsMysqlHelper.when(() -> BackgroundJobsMysqlHelper.getHitSize(Mockito.anyString())).thenReturn(10);
            mockedBackgroundJobsMysqlHelper
                    .when(() -> BackgroundJobsMysqlHelper.getList(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt()))
                    .thenReturn(jobs);
            mockedBackgroundJobsMysqlHelper.when(() -> BackgroundJobsMysqlHelper.getIdList(Mockito.anyString())).thenReturn(ids);
            mockedBackgroundJobsMysqlHelper.when(() -> BackgroundJobsMysqlHelper.getBackgroundJobById(Mockito.anyInt())).thenReturn(job);

            BackgroundJob job = Mockito.mock(BackgroundJob.class);
            BackgroundJobManager.deleteBackgroundJob(job);
            assertNotNull(job);

        }
    }

    @Test
    public void clearHistory() {
        try (MockedStatic<BackgroundJobsMysqlHelper> mockedBackgroundJobsMysqlHelper = Mockito.mockStatic(BackgroundJobsMysqlHelper.class)) {
            mockedBackgroundJobsMysqlHelper.when(() -> BackgroundJobsMysqlHelper.getHitSize(Mockito.anyString())).thenReturn(10);
            mockedBackgroundJobsMysqlHelper
                    .when(() -> BackgroundJobsMysqlHelper.getList(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt()))
                    .thenReturn(jobs);
            mockedBackgroundJobsMysqlHelper.when(() -> BackgroundJobsMysqlHelper.getIdList(Mockito.anyString())).thenReturn(ids);
            mockedBackgroundJobsMysqlHelper.when(() -> BackgroundJobsMysqlHelper.getBackgroundJobById(Mockito.anyInt())).thenReturn(job);

            BackgroundJobManager.clearHistory();

        }
    }

}
