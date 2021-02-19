package org.goobi.api.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.goobi.api.rest.model.RestProcess;
import org.goobi.api.rest.request.SearchRequest;

import de.sub.goobi.persistence.managers.MySQLHelper;

public class RestDbHelper {

    public static List<RestProcess> searchProcesses(SearchRequest req) throws SQLException {
        List<RestProcess> results = null;
        if (MySQLHelper.isJsonCapable()) {
            String sql = req.createSql();
            Object[] params = req.createSqlParams();
            try (Connection conn = MySQLHelper.getInstance().getConnection()) {
                QueryRunner run = new QueryRunner();
                results = run.query(conn, sql, new JsonResultSetToRestProcessList(), params);
            }
        } else {
            String sql = req.createLegacySql();
            Object[] params = req.createLegacySqlParams();
            try (Connection conn = MySQLHelper.getInstance().getConnection()) {
                QueryRunner run = new QueryRunner();
                results = run.query(conn, sql, new ResultSetToRestProcessList(), params);
            }
        }
        return results;
    }

    public static List<Integer> getProcessIdsForIdentifier(String ppn) throws SQLException {
        String sql = "select processid from metadata where name = ? and value = ?";
        try (Connection conn = MySQLHelper.getInstance().getConnection()) {
            QueryRunner run = new QueryRunner();
            return run.query(conn, sql, MySQLHelper.resultSetToIntegerListHandler, "CatalogIDDigital", ppn);
        }
    }

}
