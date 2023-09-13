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

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringEscapeUtils;

import io.goobi.workflow.harvester.beans.Record;
import io.goobi.workflow.harvester.repository.Repository;
import io.goobi.workflow.harvester.repository.oai.OAIDublinCoreRepository;

class HarvesterRepositoryMysqlHelper implements Serializable {

    private static final long serialVersionUID = -8160933323894230856L;

    public static Repository getRepository(String repositoryId) throws SQLException {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            String sql = "SELECT * FROM repository WHERE id=?";
            List<Repository> retList = new QueryRunner().query(connection, sql, resultSetToRepositoriesList, repositoryId);
            if (!retList.isEmpty()) {
                return retList.get(0);
            } else {
                return null;
            }
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    /**
     * Converts {@link ResultSet} to {@link ArrayList} of {@link OAIDublinCoreRepository}
     */
    public static ResultSetHandler<List<Repository>> resultSetToRepositoriesList = new ResultSetHandler<List<Repository>>() {
        @Override
        public List<Repository> handle(ResultSet rs) throws SQLException {
            List<Repository> retList = new ArrayList<>(rs.getFetchSize());
            while (rs.next()) {
                Repository r = new Repository(rs.getInt("id"), rs.getString("name"), rs.getString("base_url"), rs.getString("export_folder"),
                        rs.getString("script_path"), rs.getTimestamp("last_harvest"), rs.getInt("freq"), rs.getInt("delay"),
                        rs.getBoolean("enabled"));
                r.setRepositoryType(rs.getString("type"));

                String query = "SELECT name, value FROM repository_parameter WHERE repository_id = ?";
                Connection connection = null;
                try {
                    connection = MySQLHelper.getInstance().getConnection();
                    Map<String, String> parameter = new QueryRunner().query(connection, query, resultSetToMapHandler, r.getId());
                    r.setParameter(parameter);
                } finally {
                    if (connection != null) {
                        MySQLHelper.closeConnection(connection);
                    }
                }
                retList.add(r);
            }
            return retList;
        }
    };

    public static int getRepositoryCount(String filter) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder("SELECT count(1) FROM repository");
        if (filter != null && !filter.isEmpty()) {
            sql.append(" WHERE " + filter);
        }

        try {
            connection = MySQLHelper.getInstance().getConnection();
            return new QueryRunner().query(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }

    }

    public static List<Repository> getRepositories(String order, String filter, Integer start, Integer count) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder("SELECT * FROM repository");
        if (filter != null && !filter.isEmpty()) {
            sql.append(" WHERE " + filter);
        }

        try {
            connection = MySQLHelper.getInstance().getConnection();

            return new QueryRunner().query(connection, sql.toString(), resultSetToRepositoriesList);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void saveRepository(Repository repository) throws SQLException {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner runner = new QueryRunner();
            if (repository.getId() == null) {

                StringBuilder sql = new StringBuilder();
                sql.append("INSERT INTO repository ");
                sql.append("(name, base_url, export_folder, script_path, last_harvest, freq, delay, enabled, type) ");
                sql.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");

                Integer id = runner.insert(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler, repository.getName(),
                        repository.getUrl(), repository.getExportFolderPath(), repository.getScriptPath(), repository.getLastHarvest(),
                        repository.getFrequency(), repository.getDelay(), repository.isEnabled(), repository.getRepositoryType());
                repository.setId(id);
            } else {
                StringBuilder sql = new StringBuilder();
                sql.append("UPDATE repository SET name = ?, base_url = ?, export_folder = ?, script_path = ?, last_harvest = ?, ");
                sql.append("freq = ?, delay = ?, enabled = ?, type = ? WHERE id = ?");
                runner.update(connection, sql.toString(), repository.getName(), repository.getUrl(), repository.getExportFolderPath(),
                        repository.getScriptPath(), repository.getLastHarvest(), repository.getFrequency(), repository.getDelay(),
                        repository.isEnabled(), repository.getRepositoryType(), repository.getId());
            }

            if (!repository.getParameter().isEmpty()) {
                String deletion = "delete from repository_parameter where repository_id = ?";
                runner.update(connection, deletion, repository.getId());

                String insert = "INSERT INTO repository_parameter (repository_id, name, value) VALUES (?,?,?)";
                for (Entry<String, String> param : repository.getParameter().entrySet()) {

                    runner.update(connection, insert, repository.getId(), param.getKey(), param.getValue());

                }
            }
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }

    }

    public static void deleteRepository(Repository repository) throws SQLException {
        if (repository.getId() != null) {
            String deletion = "DELETE FROM repository_parameter WHERE repository_id = ?";
            String sql = "DELETE FROM repository WHERE id = ?";
            Connection connection = null;
            try {
                connection = MySQLHelper.getInstance().getConnection();
                new QueryRunner().update(connection, deletion, repository.getId());
                new QueryRunner().update(connection, sql, repository.getId());
            } finally {
                if (connection != null) {
                    MySQLHelper.closeConnection(connection);
                }
            }
        }
    }

    private static ResultSetHandler<Map<String, String>> resultSetToMapHandler = new ResultSetHandler<Map<String, String>>() {
        @Override
        public Map<String, String> handle(ResultSet rs) throws SQLException {
            Map<String, String> map = new HashMap<>();
            while (rs.next()) {
                map.put(rs.getString("name"), rs.getString("value"));
            }
            return map;
        }
    };

    public static Timestamp getLastHarvest(Integer repositoryId) throws SQLException {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            String sql = "SELECT last_harvest FROM repository WHERE id=?";
            return new QueryRunner().query(connection, sql, new ResultSetHandler<Timestamp>() {
                @Override
                public Timestamp handle(ResultSet rs) throws SQLException {
                    if (!rs.next()) {
                        return null;
                    }
                    return rs.getTimestamp("last_harvest");
                }
            }, repositoryId);

        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static int addRecords(List<Record> recordList, boolean allowUpdates) throws SQLException {
        int counterNew = 0;
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();

            // Check if same Record is already in DB. This check makes this method pretty slow, but secure..
            String sqlCheck = "SELECT count(1) FROM record WHERE identifier=?";
            QueryRunner run = new QueryRunner();
            for (Record record : recordList) {
                // Some repository types (e.g. intranda viewer) may allow multiple instances of the same record identifier, with the difference being different time frame subqueries
                String subquery = record.getSubquery() == null ? "" : " AND subquery='" + record.getSubquery() + "'";
                // Check whether the record has previously been harvested
                int i = run.query(connection, sqlCheck + subquery, MySQLHelper.resultSetToIntegerHandler,
                        StringEscapeUtils.escapeSql(record.getIdentifier()));
                if (i == 0) {
                    // Add record to DB
                    String sql =
                            "INSERT INTO record (repository_id,title,creator,repository_datestamp,setSpec,identifier,job_id,source,subquery) VALUES (?,?,?,?,?,?,?,?,?)";
                    run.update(connection, sql, record.getRepositoryId(), StringEscapeUtils.escapeSql(record.getTitle()),
                            StringEscapeUtils.escapeSql(record.getCreator()), record.getRepositoryTimestamp(), record.getSetSpec(),
                            StringEscapeUtils.escapeSql(record.getIdentifier()), record.getJobId(), StringEscapeUtils.escapeSql(record.getSource()),
                            StringEscapeUtils.escapeSql(record.getSubquery()));
                    counterNew++;
                } else if (allowUpdates) {
                    // Update existing record and reset exported status
                    String sql =
                            "UPDATE record SET exported=NULL, title=?, creator=?, repository_datestamp=?, setSpec=?, job_id=?, source=?, subquery=? WHERE identifier=?";
                    run.update(connection, sql, StringEscapeUtils.escapeSql(record.getTitle()), StringEscapeUtils.escapeSql(record.getCreator()),
                            record.getRepositoryTimestamp(), record.getSetSpec(), record.getJobId(), StringEscapeUtils.escapeSql(record.getSource()),
                            StringEscapeUtils.escapeSql(record.getSubquery()), record.getIdentifier());
                }
            }
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
        return counterNew;
    }
}
