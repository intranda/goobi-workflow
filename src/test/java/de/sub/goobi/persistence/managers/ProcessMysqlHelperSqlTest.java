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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import de.sub.goobi.AbstractTest;

public class ProcessMysqlHelperSqlTest extends AbstractTest {

    private static final String FILTER = " WHERE prozesse.istTemplate = false ";
    private static final String METADATA_SORT = "{db_meta.TitleDocMain}";

    @Test
    public void testGetProcessesPlacesMetadataSortJoinBeforeWhere() throws Exception {
        try (SqlCapture capture = new SqlCapture(new ArrayList<>())) {
            ProcessMysqlHelper.getProcesses(METADATA_SORT, FILTER, 0, 10, null);
            assertJoinBeforeWhere(capture.lastStatement());
        }
    }

    @Test
    public void testGetProcessIdListPlacesMetadataSortJoinBeforeWhere() throws Exception {
        try (SqlCapture capture = new SqlCapture(new ArrayList<>())) {
            ProcessMysqlHelper.getProcessIdList(METADATA_SORT, FILTER, 0, 10);
            assertJoinBeforeWhere(capture.lastStatement());
        }
    }

    @Test
    public void testGetIDListPlacesMetadataSortJoinBeforeWhere() throws Exception {
        try (SqlCapture capture = new SqlCapture(new ArrayList<>())) {
            ProcessMysqlHelper.getIDList(Optional.of(METADATA_SORT), FILTER);
            assertJoinBeforeWhere(capture.lastStatement());
        }
    }

    private static void assertJoinBeforeWhere(String sql) {
        int where = sql.indexOf("WHERE prozesse.istTemplate");
        assertTrue(where >= 0, sql);
        assertFalse(sql.substring(where).contains(" JOIN "), "joins must be placed before the WHERE clause: " + sql);
        assertTrue(sql.contains("ORDER BY") && sql.contains("metadata.name = 'TitleDocMain'"), "metadata sort missing: " + sql);
    }
}
