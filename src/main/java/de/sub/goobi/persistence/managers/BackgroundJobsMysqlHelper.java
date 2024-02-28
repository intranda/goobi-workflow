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
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package de.sub.goobi.persistence.managers;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.lang3.StringUtils;
import org.goobi.production.flow.jobs.BackgroundJob;
import org.goobi.production.flow.jobs.BackgroundJobProperty;

import lombok.extern.log4j.Log4j2;

@Log4j2
class BackgroundJobsMysqlHelper implements Serializable {
    private static final long serialVersionUID = 8227638850137164176L;

    public static BackgroundJob getBackgroundJobById(int id) throws SQLException {
        Connection connection = null;

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM background_job WHERE id = " + id);
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (log.isTraceEnabled()) {
                log.trace(sql.toString());
            }
            return new QueryRunner().query(connection, sql.toString(), BackgroundJobManager.resultSetToJobHandler);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static List<BackgroundJobProperty> getAdditionalProperties(int id) throws SQLException {
        Connection connection = null;

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM background_job_properties WHERE job_id = " + id);
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (log.isTraceEnabled()) {
                log.trace(sql.toString());
            }
            return new QueryRunner().query(connection, sql.toString(), new BeanListHandler<>(BackgroundJobProperty.class));
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void saveBackgroundJob(BackgroundJob job) throws SQLException {
        StringBuilder sql = new StringBuilder();
        Connection connection = null;

        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();

            if (job.getId() == null) {
                // insert new job
                sql.append("INSERT INTO background_job (jobname, jobtype, jobstatus, retrycount, lastAltered) VALUES (");
                sql.append("?,?,?,?,?");
                sql.append(") ");
                Integer id = run.insert(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler, job.getJobName(), job.getJobType(),
                        job.getJobStatus().getId(), job.getRetryCount(), Timestamp.valueOf(job.getLastUpdateTime()));
                if (id != null) {
                    job.setId(id);
                }
            } else {
                // update job
                sql.append("UPDATE background_job SET ");
                sql.append("jobname = ?,");
                sql.append("jobtype = ?, ");
                sql.append("jobstatus = ?, ");
                sql.append("retrycount = ?, ");
                sql.append("lastAltered = ? ");
                sql.append("WHERE id = ?");
                run.update(connection, sql.toString(), job.getJobName(), job.getJobType(), job.getJobStatus().getId(), job.getRetryCount(),
                        Timestamp.valueOf(job.getLastUpdateTime()), job.getId());

            }
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
        // save all properties
        for (BackgroundJobProperty prop : job.getProperties()) {
            saveProperty(job.getId(), prop);
        }
    }

    private static void saveProperty(Integer jobId, BackgroundJobProperty prop) throws SQLException {
        StringBuilder sql = new StringBuilder();
        Connection connection = null;

        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();

            if (prop.getId() == null) {

                sql.append("INSERT INTO background_job_properties (job_id, title, value) VALUES (");
                sql.append(jobId);
                sql.append(", ");
                sql.append(prop.getTitle());
                sql.append(", ");
                sql.append(prop.getValue());
                sql.append(")");
                Integer id = run.insert(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler);
                if (id != null) {
                    prop.setId(id);
                }
            } else {
                sql.append("UPDATE background_job_properties SET ");
                sql.append("title = ?,");
                sql.append("value = ? ");
                sql.append("WHERE id = ?");
                run.update(connection, sql.toString(), prop.getTitle(), prop.getValue(), prop.getId());
            }
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void deleteBackgroundJob(BackgroundJob o) throws SQLException {
        Integer id = o.getId();
        String deleteProperties = "DELETE FROM background_job_properties WHERE job_id = ?";
        String deleteJob = "DELETE FROM background_job WHERE id = ?";
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            run.execute(connection, deleteProperties, id);
            run.execute(connection, deleteJob, id);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static int getHitSize(String filter) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT count(1) FROM background_job ");
        if (StringUtils.isNotBlank(filter)) {
            sb.append("WHERE jobname LIKE \"%");
            sb.append(MySQLHelper.escapeSql(filter));
            sb.append("%\"");
        }
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            return run.query(connection, sb.toString(), MySQLHelper.resultSetToIntegerHandler);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static List<BackgroundJob> getList(String order, String filter, Integer start, Integer count) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM background_job ");

        if (StringUtils.isNotBlank(filter)) {
            sb.append("WHERE jobname LIKE \"%");
            sb.append(MySQLHelper.escapeSql(filter));
            sb.append("%\"");
        }

        sb.append("ORDER BY ");
        sb.append(order);

        if (start != null && count != null) {
            sb.append(" LIMIT " + start + ", " + count);
        }

        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            return run.query(connection, sb.toString(), BackgroundJobManager.resultSetToJoListbHandler);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static List<Integer> getIdList(String filter) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT id FROM background_job ");

        if (StringUtils.isNotBlank(filter)) {
            sb.append("WHERE jobname LIKE \"%");
            sb.append(MySQLHelper.escapeSql(filter));
            sb.append("%\"");
        }

        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            return run.query(connection, sb.toString(), MySQLHelper.resultSetToIntegerListHandler);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void clearHistoryOlderThan30Days() throws SQLException {
        LocalDateTime now = LocalDateTime.now();
        String clearBackgroundJobs = "DELETE FROM background_job WHERE lastAltered < ? ";
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            run.execute(connection, clearBackgroundJobs, Timestamp.valueOf(now.minusDays(30)));
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }
}
