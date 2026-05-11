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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.sub.goobi.AbstractTest;

public class HarvesterRepositoryMysqlHelperTest extends AbstractTest {

    @Test
    public void testBuildExistingIdentifierSqlSingleEntryUsesSinglePlaceholder() {
        String sql = HarvesterRepositoryMysqlHelper.buildExistingIdentifierSql(1);
        assertEquals("SELECT identifier from record where identifier in (?)", sql);
    }

    @Test
    public void testBuildExistingIdentifierSqlMultipleEntriesUsesCommaSeparatedPlaceholders() {
        String sql = HarvesterRepositoryMysqlHelper.buildExistingIdentifierSql(3);
        assertEquals("SELECT identifier from record where identifier in (?, ?, ?)", sql);
    }

    @Test
    public void testBuildExistingIdentifierSqlDoesNotContainUserData() {
        String sqlInjectionPayload = "'; DROP TABLE record;--";
        // The SQL must only contain placeholders, never the actual identifier values
        String sql = HarvesterRepositoryMysqlHelper.buildExistingIdentifierSql(2);
        assertFalse(sql.contains(sqlInjectionPayload));
        assertFalse(sql.contains("DROP"));
        assertTrue(sql.contains("?"));
    }

    @Test
    public void testSetRecordExportedSqlUsesPreparedStatementPlaceholders() {
        assertTrue(HarvesterRepositoryMysqlHelper.SET_RECORD_EXPORTED_SQL.contains("?"));
    }

    @Test
    public void testSetRecordExportedSqlDoesNotContainStringConcatenation() {
        // The SQL constant must not contain any literal string delimiters that
        // would indicate user data was baked into the query template itself.
        assertFalse(HarvesterRepositoryMysqlHelper.SET_RECORD_EXPORTED_SQL.contains("'"));
        assertEquals(3, countOccurrences(HarvesterRepositoryMysqlHelper.SET_RECORD_EXPORTED_SQL, '?'));
    }

    private static int countOccurrences(String haystack, char needle) {
        int count = 0;
        for (char c : haystack.toCharArray()) {
            if (c == needle) {
                count++;
            }
        }
        return count;
    }
}
