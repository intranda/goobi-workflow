package de.sub.goobi.persistence.managers;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.ResultSetHandler;
import org.goobi.beans.DatabaseObject;
import org.goobi.beans.Institution;
import org.goobi.production.flow.jobs.BackgroundJob;
import org.goobi.production.flow.jobs.BackgroundJob.JobStatus;

import de.sub.goobi.helper.exceptions.DAOException;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class BackgroundJobManager implements IManager, Serializable {

    private static final long serialVersionUID = -2193872225938495842L;

    @Override
    public int getHitSize(String order, String filter, Institution institution) throws DAOException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public List<? extends DatabaseObject> getList(String order, String filter, Integer start, Integer count, Institution institution)
            throws DAOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Integer> getIdList(String order, String filter, Institution institution) {
        // TODO Auto-generated method stub
        return null;
    }

    public static BackgroundJob getBackgroundJobById(int id) {
        BackgroundJob o = null;
        try {
            o = BackgroundJobsMysqlHelper.getBackgroundJobById(id);
        } catch (SQLException e) {
            log.error("error while getting job with id " + id, e);
        }
        return o;
    }

    public static void saveBackgroundJob(BackgroundJob o) {
        try {
            BackgroundJobsMysqlHelper.saveBackgroundJob(o);
        } catch (SQLException e) {
            log.error("error while saving job with id " + o.getId(), e);
        }
    }

    public static void deleteBackgroundJob(BackgroundJob o) {
        try {
            BackgroundJobsMysqlHelper.deleteBackgroundJob(o);
        } catch (SQLException e) {
            log.error("error while deleting job with id " + o.getId(), e);
        }
    }

    public static ResultSetHandler<BackgroundJob> resultSetToJobHandler = new ResultSetHandler<BackgroundJob>() {
        @Override
        public BackgroundJob handle(ResultSet rs) throws SQLException {
            try {
                if (rs.next()) {
                    return convert(rs);
                }
            } finally {
                rs.close();
            }
            return null;
        }
    };

    public static ResultSetHandler<List<BackgroundJob>> resultSetToJoListbHandler = new ResultSetHandler<List<BackgroundJob>>() {
        @Override
        public List<BackgroundJob> handle(ResultSet rs) throws SQLException {
            List<BackgroundJob> answer = new ArrayList<>();
            try {
                while (rs.next()) {
                    answer.add(convert(rs));
                }
            } finally {
                rs.close();
            }
            return answer;
        }
    };

    private static BackgroundJob convert(ResultSet rs) throws SQLException {
        BackgroundJob r = new BackgroundJob();
        r.setId(rs.getInt("id"));
        r.setJobName(rs.getString("jobname"));
        r.setJobType(rs.getString("jobtype"));
        r.setJobStatus(JobStatus.getById(rs.getInt("jobstatus")));
        Timestamp time = rs.getTimestamp("lastAltered");
        if (time != null) {
            r.setLastUpdateTime(LocalDateTime.from(time.toInstant()));
        }
        r.setRetryCount(rs.getInt("retrycount"));
        r.setProperties(BackgroundJobsMysqlHelper.getAdditionalProperties(r.getId()));
        return r;
    }

}
