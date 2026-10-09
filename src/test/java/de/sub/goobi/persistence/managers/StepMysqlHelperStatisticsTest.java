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

package de.sub.goobi.persistence.managers;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import de.sub.goobi.AbstractTest;

public class StepMysqlHelperStatisticsTest extends AbstractTest {

    @Test
    public void testStepStatisticsAreGroupedByTitleInOneQuery() throws Exception {
        String filter = " WHERE prozesse.istTemplate = false AND ( projekte.titel like '%Archive_Project%') ";
        try (SqlCapture capture = new SqlCapture(new ArrayList<>())) {
            StepMysqlHelper.getStepStatisticsPerTitle(filter);

            String sql = capture.lastStatement().replaceAll("\\s+", " ");
            assertTrue(sql.contains("JOIN prozesse ON schritte.prozesseId = prozesse.ProzesseID"), sql);
            assertTrue(sql.contains(filter.replaceAll("\\s+", " ").trim()), sql);
            assertTrue(sql.trim().endsWith("GROUP BY schritte.titel"), sql);
        }
    }
}
