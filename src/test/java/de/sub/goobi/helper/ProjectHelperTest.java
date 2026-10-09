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

package de.sub.goobi.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.goobi.beans.Project;
import org.goobi.production.flow.statistics.StepInformation;
import org.goobi.production.flow.statistics.StepTitleStatistics;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.StepManager;

public class ProjectHelperTest extends AbstractTest {

    private List<StepInformation> overview(List<StepTitleStatistics> statistics, MockedStatic<StepManager> stepManager) {
        Project project = new Project();
        project.setTitel("Archive_Project");
        try (MockedStatic<ProcessManager> processManager = Mockito.mockStatic(ProcessManager.class);
                MockedStatic<Helper> helper = Mockito.mockStatic(Helper.class)) {
            helper.when(Helper::getCurrentUser).thenReturn(null);
            processManager.when(() -> ProcessManager.getCountOfFieldValue(Mockito.eq("ProzesseID"), Mockito.anyString())).thenReturn(10L);
            processManager.when(() -> ProcessManager.getSumOfFieldValue(Mockito.eq("sortHelperImages"), Mockito.anyString())).thenReturn(500L);
            stepManager.when(() -> StepManager.getStepStatisticsPerTitle(Mockito.anyString())).thenReturn(statistics);
            return ProjectHelper.getProjectWorkFlowOverview(project);
        }
    }

    @Test
    public void testOverviewContainsStepsOfAllProcessesSortedByOrder() {
        try (MockedStatic<StepManager> stepManager = Mockito.mockStatic(StepManager.class)) {
            List<StepInformation> overview = overview(List.of(
                    new StepTitleStatistics("Export", 5.0, 10, 4, 200),
                    new StepTitleStatistics("Scanning", 2.0, 10, 10, 500),
                    // only some processes contain this step
                    new StepTitleStatistics("Correction", 3.0, 3, 1, 50)), stepManager);

            assertEquals(2, overview.size());
            StepInformation scanning = overview.get(0);
            assertEquals("Scanning", scanning.getTitle());
            assertEquals(2.0, scanning.getAverageStepOrder());
            assertEquals(10, scanning.getNumberOfTotalSteps());
            assertEquals(500, scanning.getNumberOfTotalImages());
            assertEquals(10, scanning.getNumberOfStepsDone());
            assertEquals(500, scanning.getNumberOfImagesDone());
            StepInformation export = overview.get(1);
            assertEquals("Export", export.getTitle());
            assertEquals(4, export.getNumberOfStepsDone());
            assertEquals(200, export.getNumberOfImagesDone());
        }
    }

    @Test
    public void testOverviewReadsTheStepsOnce() {
        try (MockedStatic<StepManager> stepManager = Mockito.mockStatic(StepManager.class)) {
            overview(List.of(new StepTitleStatistics("Export", 5.0, 10, 4, 200)), stepManager);

            stepManager.verify(() -> StepManager.getStepStatisticsPerTitle(Mockito.anyString()), Mockito.times(1));
            // no queries per step title anymore
            stepManager.verify(() -> StepManager.getCountOfFieldValue(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()), Mockito.never());
            stepManager.verify(() -> StepManager.getAverageOfFieldValue(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()), Mockito.never());
        }
    }
}
