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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/**
 * Records the SQL statements and parameters a MysqlHelper passes to {@link QueryRunner}, without a database connection.
 */
class SqlCapture implements AutoCloseable {

    final List<String> statements = new ArrayList<>();
    final List<Object[]> parameters = new ArrayList<>();

    private final MockedStatic<MySQLHelper> mySQLHelper;
    private final MockedConstruction<QueryRunner> queryRunner;

    /**
     * @param result value every QueryRunner call returns
     */
    SqlCapture(Object result) throws SQLException {
        MySQLHelper helper = Mockito.mock(MySQLHelper.class);
        Mockito.when(helper.getConnection()).thenReturn(Mockito.mock(Connection.class));

        mySQLHelper = Mockito.mockStatic(MySQLHelper.class);
        mySQLHelper.when(MySQLHelper::getInstance).thenReturn(helper);
        mySQLHelper.when(() -> MySQLHelper.prepareSortField(Mockito.any(), Mockito.any())).thenCallRealMethod();
        mySQLHelper.when(() -> MySQLHelper.escapeSql(Mockito.any())).thenCallRealMethod();

        queryRunner = Mockito.mockConstruction(QueryRunner.class, Mockito.withSettings().defaultAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            if (args.length > 1 && args[1] instanceof String sql) {
                statements.add(sql);
                parameters.add(Arrays.copyOfRange(args, 2, args.length));
            }
            if (invocation.getMethod().getReturnType() == int.class) {
                // update() returns the number of changed rows
                return 0;
            }
            return result;
        }));
    }

    /**
     * for additional stubbing of static MySQLHelper methods
     */
    MockedStatic<MySQLHelper> mySQLHelper() {
        return mySQLHelper;
    }

    String lastStatement() {
        return statements.get(statements.size() - 1);
    }

    Object[] lastParameters() {
        return parameters.get(parameters.size() - 1);
    }

    @Override
    public void close() {
        queryRunner.close();
        mySQLHelper.close();
    }
}
