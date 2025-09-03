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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
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
        try {
            return BackgroundJobsMysqlHelper.getHitSize(filter);
        } catch (SQLException e) {
            throw new DAOException(e);
        }
    }

    @Override
    public List<? extends DatabaseObject> getList(String order, String filter, Integer start, Integer count, Institution institution)
            throws DAOException {
        return getJobs(order, filter, start, count);
    }

    public List<BackgroundJob> getJobs(String order, String filter, Integer start, Integer count) {
        try {
            return BackgroundJobsMysqlHelper.getList(order, filter, start, count);
        } catch (SQLException e) {
            log.error(e);
        }
        return Collections.emptyList();
    }

    @Override
    public List<Integer> getIdList(String order, String filter, Institution institution) {
        try {
            return BackgroundJobsMysqlHelper.getIdList(filter);
        } catch (SQLException e) {
            log.error(e);
        }
        return Collections.emptyList();
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


    public static void clearHistory() {
        try {
            BackgroundJobsMysqlHelper.clearHistoryOlderThan30Days();
        } catch (SQLException e) {
            log.error(e);
        }

    }

    public static final ResultSetHandler<BackgroundJob> RESULT_SET_TO_JOB_HANDLER = new ResultSetHandler<BackgroundJob>() {
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

    public static final ResultSetHandler<List<BackgroundJob>> RESULT_SET_TO_JOB_LIST_HANDLER = new ResultSetHandler<List<BackgroundJob>>() {
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
            r.setLastUpdateTime(time.toLocalDateTime());
        }
        r.setRetryCount(rs.getInt("retrycount"));
        r.setProperties(BackgroundJobsMysqlHelper.getAdditionalProperties(r.getId()));
        return r;
    }
}
