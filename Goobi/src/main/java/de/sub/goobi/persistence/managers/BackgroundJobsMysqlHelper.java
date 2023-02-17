package de.sub.goobi.persistence.managers;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
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
                sql.append(job.getJobName());
                sql.append(", ");
                sql.append(job.getJobType());
                sql.append(", ");
                sql.append(job.getJobStatus().getId());
                sql.append(", ");
                sql.append(job.getRetryCount());
                sql.append(", ");
                sql.append(Timestamp.valueOf(job.getLastUpdateTime()));
                sql.append(") ");
                Integer id = run.insert(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler);
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
}
