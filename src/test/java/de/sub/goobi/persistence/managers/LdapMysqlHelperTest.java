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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import de.sub.goobi.AbstractTest;

public class LdapMysqlHelperTest extends AbstractTest {

    @Test
    public void testGetLdapsSqlPreventsMaliciousOrderBy() {
        StringBuilder sql = new StringBuilder("SELECT * FROM ldapgruppen");
        String maliciousOrder = "1; DROP TABLE ldapgruppen--";
        String result = MySQLHelper.prepareSortField(maliciousOrder, sql);
        assertTrue(result.isEmpty(), "prepareSortField must return empty string for malicious input");
    }

    @Test
    public void testGetLdapsSqlAllowsValidOrderBy() {
        StringBuilder sql = new StringBuilder("SELECT * FROM ldapgruppen");
        String validOrder = "ldapgruppenID asc";
        String result = MySQLHelper.prepareSortField(validOrder, sql);
        assertFalse(result.contains(";") || result.contains("DROP"), "prepareSortField must not return the malicious literal");
    }

    @Test
    public void testBuildLdapFilterClauseReturnsPreparedStatementPlaceholder() {
        List<Object> params = new ArrayList<>();
        String clause = LdapMysqlHelper.buildLdapFilterClause("admin", params);
        assertTrue(clause.contains("?"), "Filter clause must use ? placeholder, not string concatenation");
        assertFalse(clause.contains("admin"), "Filter value must not appear literally in the SQL clause");
    }

    @Test
    public void testBuildLdapFilterClauseAddsValueToParams() {
        List<Object> params = new ArrayList<>();
        LdapMysqlHelper.buildLdapFilterClause("admin", params);
        assertFalse(params.isEmpty(), "Filter value must be added to params list for PreparedStatement");
        assertTrue(params.get(0).toString().contains("admin"), "Params list must contain the filter value");
    }

    @Test
    public void testBuildLdapFilterClauseEscapesLikeWildcards() {
        List<Object> params = new ArrayList<>();
        LdapMysqlHelper.buildLdapFilterClause("test%hack_all", params);
        String paramValue = (String) params.get(0);
        assertTrue(paramValue.contains("\\%"), "Percent sign must be escaped for LIKE");
        assertTrue(paramValue.contains("\\_"), "Underscore must be escaped for LIKE");
    }

    @Test
    public void testBuildLdapFilterClauseReturnsNullForBlankInput() {
        assertNull(LdapMysqlHelper.buildLdapFilterClause(null, new ArrayList<>()));
        assertNull(LdapMysqlHelper.buildLdapFilterClause("", new ArrayList<>()));
    }
}
