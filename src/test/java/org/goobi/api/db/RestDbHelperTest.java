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
 *
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
package org.goobi.api.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.goobi.api.rest.model.RestProcess;
import org.goobi.api.rest.request.SearchRequest;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import de.sub.goobi.AbstractTest;
import de.sub.goobi.persistence.managers.MySQLHelper;

public class RestDbHelperTest extends AbstractTest {

    private final List<String> statements = new ArrayList<>();

    private List<RestProcess> search(SearchRequest req, List<Integer> projectIds) throws Exception {
        MySQLHelper helper = Mockito.mock(MySQLHelper.class);
        Mockito.when(helper.getConnection()).thenReturn(Mockito.mock(Connection.class));
        try (MockedStatic<MySQLHelper> mySQLHelper = Mockito.mockStatic(MySQLHelper.class);
                MockedConstruction<QueryRunner> queryRunner = Mockito.mockConstruction(QueryRunner.class,
                        Mockito.withSettings().defaultAnswer(invocation -> {
                            String sql = invocation.getArgument(1);
                            statements.add(sql);
                            return sql.startsWith("SELECT ProjekteID FROM projekte") ? projectIds : new ArrayList<RestProcess>();
                        }))) {
            mySQLHelper.when(MySQLHelper::getInstance).thenReturn(helper);
            return RestDbHelper.searchProcesses(req);
        }
    }

    @Test
    public void testSearchResolvesProjectTitlesToIds() throws Exception {
        SearchRequest req = new SearchRequest();
        req.setFilterProjects(Arrays.asList("Manuscript_Project"));

        search(req, Arrays.asList(2));

        assertEquals(2, statements.size(), String.join("\n", statements));
        assertEquals(Arrays.asList(2), req.getFilterProjectIds());
        assertTrue(statements.get(1).contains("prozesse.ProjekteID IN (?)"), statements.get(1));
    }

    @Test
    public void testSearchWithUnknownProjectReturnsNothing() throws Exception {
        SearchRequest req = new SearchRequest();
        req.setFilterProjects(Arrays.asList("unknown project"));

        List<RestProcess> result = search(req, new ArrayList<>());

        assertTrue(result.isEmpty());
        // no search without a matching project
        assertEquals(1, statements.size(), String.join("\n", statements));
    }
}
