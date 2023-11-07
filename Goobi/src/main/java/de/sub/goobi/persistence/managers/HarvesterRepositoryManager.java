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
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.goobi.beans.DatabaseObject;
import org.goobi.beans.Institution;

import de.sub.goobi.helper.exceptions.DAOException;
import io.goobi.workflow.harvester.beans.Job;
import io.goobi.workflow.harvester.beans.Record;
import io.goobi.workflow.harvester.export.ExportHistoryEntry;
import io.goobi.workflow.harvester.repository.Repository;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class HarvesterRepositoryManager implements IManager, Serializable {

    private static final long serialVersionUID = 5591131855633585870L;

    @Override
    public int getHitSize(String order, String filter, Institution institution) throws DAOException {
        int num = 0;
        try {
            num = HarvesterRepositoryMysqlHelper.getRepositoryCount(filter);
        } catch (SQLException e) {
            log.error("error while getting repository hit size", e);
            throw new DAOException(e);
        }
        return num;
    }

    @Override
    public List<? extends DatabaseObject> getList(String order, String filter, Integer start, Integer count, Institution institution)
            throws DAOException {
        return getRepositories(filter);
    }

    public static List<Repository> getRepositories(String filter) throws DAOException {
        List<Repository> answer = new ArrayList<>();
        try {
            answer = HarvesterRepositoryMysqlHelper.getRepositories(filter);
        } catch (SQLException e) {
            log.error("error while getting repositories", e);
            throw new DAOException(e);
        }
        return answer;

    }

    @Override
    public List<Integer> getIdList(String order, String filter, Institution institution) {
        return java.util.Collections.emptyList();
    }

    public static Repository getRepository(Integer repositoryId) {
        try {
            return HarvesterRepositoryMysqlHelper.getRepository(repositoryId);
        } catch (SQLException e) {
            log.error(e);
        }
        return null;
    }

    public static void saveRepository(Repository repository) {
        try {
            HarvesterRepositoryMysqlHelper.saveRepository(repository);
        } catch (SQLException e) {
            log.error(e);
        }
    }

    public static void deleteRepository(Repository repository) {
        try {
            HarvesterRepositoryMysqlHelper.deleteRepository(repository);
        } catch (SQLException e) {
            log.error(e);
        }
    }

    public static void changeStatusOfRepository(Repository repository) {
        try {
            HarvesterRepositoryMysqlHelper.changeStatusOfRepository(repository);
        } catch (SQLException e) {
            log.error(e);
        }
    }

    public static int addRecords(List<Record> recordList, boolean allowUpdates) {
        if (allowUpdates) {
            log.debug("Adding {} records to DB...", recordList.size());
        } else {
            log.debug("Adding {} records to DB (minus any duplicates)...", recordList.size());
        }

        try {
            return HarvesterRepositoryMysqlHelper.addRecords(recordList, allowUpdates);
        } catch (SQLException e) {
            log.error(e);
        }
        return 0;
    }


    public static List<String> getExistingIdentifier(List<String> identifiers) {
        try {
            return HarvesterRepositoryMysqlHelper.getExistingIdentifier(identifiers);
        } catch (SQLException e) {
            log.error(e);
        }
        return Collections.emptyList();
    }

    public static Job addNewJob(Job j) {
        try {
            return HarvesterRepositoryMysqlHelper.addNewJob(j);
        } catch (SQLException e) {
            log.error(e);
        }
        return j;

    }

    public static void updateJobStatus(Job job) {
        try {
            HarvesterRepositoryMysqlHelper.updateJobStatus(job);
        } catch (SQLException e) {
            log.error(e);
        }

    }

    public static List<Record> getRecords(int first, int pageSize, String sortField, boolean sortOrder, Map<String, String> filters,
            boolean exported) {
        try {
            return HarvesterRepositoryMysqlHelper.getRecords(first, pageSize, sortField, sortOrder, filters, exported);
        } catch (SQLException e) {
            log.error(e);
        }
        return Collections.emptyList();

    }

    public static void addExportHistoryEntry(ExportHistoryEntry hist) {
        try {
            HarvesterRepositoryMysqlHelper.addExportHistoryEntry(hist);
        } catch (SQLException e) {
            log.error(e);
        }
    }

    public static void updateLastHarvestingTime(Integer repositoryId, Timestamp timestamp) {
        try {
            HarvesterRepositoryMysqlHelper.updateLastHarvestingTime(repositoryId, timestamp);
        } catch (SQLException e) {
            log.error(e);
        }
    }

    public static void setRecordExported(Record record) {
        try {
            HarvesterRepositoryMysqlHelper.setRecordExported(record);
        } catch (SQLException e) {
            log.error(e);
        }
    }

}
