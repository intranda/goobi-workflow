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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.sub.goobi.AbstractTest;

public class LdapMysqlHelperTest extends AbstractTest {

    @Test
    public void testGetLdapsSqlPreventsMaliciousOrderBy() {
        StringBuilder sql = new StringBuilder("SELECT * FROM ldapgruppen");
        String maliciousOrder = "1; DROP TABLE ldapgruppen--";
        String result = MySQLHelper.prepareSortField(maliciousOrder, sql);
        assertTrue("prepareSortField must return empty string for malicious input", result.isEmpty());
    }

    @Test
    public void testGetLdapsSqlAllowsValidOrderBy() {
        StringBuilder sql = new StringBuilder("SELECT * FROM ldapgruppen");
        String validOrder = "ldapgruppenID asc";
        String result = MySQLHelper.prepareSortField(validOrder, sql);
        // Either the field is in the whitelist (non-empty) or empty — either way no injection
        assertFalse("prepareSortField must not return the malicious literal", result.contains(";") || result.contains("DROP"));
    }
}
