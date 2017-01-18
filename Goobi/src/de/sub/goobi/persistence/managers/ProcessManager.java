package de.sub.goobi.persistence.managers;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *          - http://www.intranda.com
 *          - http://digiverso.com 
 *          - http://www.goobi.org
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
 */
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.goobi.beans.DatabaseObject;
import org.goobi.beans.LogEntry;
import org.goobi.beans.Process;

import de.sub.goobi.helper.exceptions.DAOException;

public class ProcessManager implements IManager, Serializable {

    private static final long serialVersionUID = 3898081063234221110L;

    private static final Logger logger = Logger.getLogger(ProcessManager.class);

    @Override
    public int getHitSize(String order, String filter) throws DAOException {
        try {
            return ProcessMysqlHelper.getProcessCount(order, filter);
        } catch (SQLException e) {
            logger.error(e);
            return 0;
        }
    }

    @Override
    public List<? extends DatabaseObject> getList(String order, String filter, Integer start, Integer count) {
        return getProcesses(order, filter, start, count);
    }

    public static List<Process> getProcesses(String order, String filter, Integer start, Integer count) {
        List<Process> answer = new ArrayList<Process>();
        try {
            answer = ProcessMysqlHelper.getProcesses(order, filter, start, count);
        } catch (SQLException e) {
            logger.error("error while getting process list", e);
        }
        return answer;
    }

    public static List<Integer> getProcessIdList(String order, String filter, Integer start, Integer count) {
        List<Integer> answer = new ArrayList<Integer>();
        try {
            answer = ProcessMysqlHelper.getProcessIdList(order, filter, start, count);
        } catch (SQLException e) {
            logger.error("error while getting process list", e);
        }
        return answer;
    }

    public static List<Process> getProcesses(String order, String filter) {
        return getProcesses(order, filter, 0, Integer.MAX_VALUE);
    }

    public static Process getProcessById(int id) {
        Process p = null;
        try {
            p = ProcessMysqlHelper.getProcessById(id);
        } catch (SQLException e) {
            logger.error(e);
        }

        return p;
    }
    
    public static Process getProcessByTitle(String inTitle) {
        Process p = null;
        try {
            p = ProcessMysqlHelper.getProcessByTitle(inTitle);
        } catch (SQLException e) {
            logger.error(e);
        }

        return p;
    }

    public static long getSumOfFieldValue(String columnname, String filter) {
        try {
            return ProcessMysqlHelper.getSumOfFieldValue(columnname, filter);
        } catch (SQLException e) {
            logger.error(e);
        }
        return 0;
    }

    public static long getCountOfFieldValue(String columnname, String filter) {
        try {
            return ProcessMysqlHelper.getCountOfFieldValue(columnname, filter);
        } catch (SQLException e) {
            logger.error(e);
        }
        return 0;
    }

    public static void saveProcess(Process o) throws DAOException {
        ProcessMysqlHelper.saveProcess(o, false);

    }

    public static void saveProcessInformation(Process o) {
        try {
            ProcessMysqlHelper.saveProcess(o, true);
        } catch (DAOException e) {
            logger.error(e);
        }
    }

    public static void deleteProcess(Process o) {
        try {
            ProcessMysqlHelper.deleteProcess(o);
        } catch (SQLException e) {
            logger.error(e);
        }
    }

    public static List<Process> getAllProcesses() {
        List<Process> answer = new ArrayList<Process>();
        try {
            answer = ProcessMysqlHelper.getAllProcesses();
        } catch (SQLException e) {
            logger.error("error while getting process list", e);
        }
        return answer;
    }

    public static void updateBatchList(List<Process> processList) {
        try {
            ProcessMysqlHelper.updateBatchList(processList);
        } catch (SQLException e) {
            logger.error(e);
        }
    }

    public static void insertBatchProcessList(List<Process> processList) {
        try {
            ProcessMysqlHelper.insertBatchProcessList(processList);
        } catch (SQLException e) {
            logger.error(e);
        }
    }

    public static int countProcessTitle(String title) {
        try {
            return ProcessMysqlHelper.getProcessCount(null, "prozesse.titel = '" + StringEscapeUtils.escapeSql(title) + "'");
        } catch (SQLException e) {
            logger.error(e);
        }
        return 0;
    }

    public static int getMaxBatchNumber() {

        try {
            return ProcessMysqlHelper.getMaxBatchNumber();
        } catch (SQLException e) {
            logger.error(e);
        }
        return 0;
    }

    public static int countProcesses(String filter) {
        try {
            return ProcessMysqlHelper.countProcesses(filter);
        } catch (SQLException e) {
            logger.error(e);
        }
        return 0;
    }

    public static List<Integer> getIDList(String filter) {

        try {
            return ProcessMysqlHelper.getIDList(filter);
        } catch (SQLException e) {
            logger.error(e);
        }
        return new ArrayList<Integer>();
    }

    public static List<Integer> getBatchIds(int limit) {

        try {
            return ProcessMysqlHelper.getBatchIds(limit);
        } catch (SQLException e) {
            logger.error(e);
        }
        return new ArrayList<Integer>();
    }

    @SuppressWarnings("rawtypes")
    public static List runSQL(String sql) {
        try {
            return ProcessMysqlHelper.runSQL(sql);
        } catch (SQLException e) {
            logger.error(e);
        }
        return new ArrayList();
    }

    public static int getNumberOfProcessesWithTitle(String title) {
        int answer = 0;
        try {
            answer = ProcessMysqlHelper.getCountOfProcessesWithTitle(title);
        } catch (SQLException e) {
            logger.error("Cannot not load information about processes with title " + title, e);
        }
        return answer;
    }

    public static int getNumberOfProcessesWithRuleset(int rulesetId) {
        Integer answer = null;
        try {
            answer = ProcessMysqlHelper.getCountOfProcessesWithRuleset(rulesetId);
        } catch (SQLException e) {
            logger.error("Cannot not load information about ruleset with id " + rulesetId, e);
        }
        return answer;
    }

    public static int getNumberOfProcessesWithDocket(int docketId) {
        Integer answer = null;
        try {
            answer = ProcessMysqlHelper.getCountOfProcessesWithDocket(docketId);
        } catch (SQLException e) {
            logger.error("Cannot not load information about docket with id " + docketId, e);
        }
        return answer;
    }

    public static void updateImages(Integer numberOfFiles, int processId) {
        try {
            ProcessMysqlHelper.updateImages(numberOfFiles, processId);
        } catch (SQLException e) {
            logger.error("Cannot not update status for process with id " + processId, e);
        }

    }

    public static void updateProcessStatus(String value, int processId) {
        try {
            ProcessMysqlHelper.updateProcessStatus(value, processId);
        } catch (SQLException e) {
            logger.error("Cannot not update status for process with id " + processId, e);
        }
    }

    public static String getProcessTitle(int processId) {
        String answer = "";
        try {
            answer = ProcessMysqlHelper.getProcessTitle(processId);
        } catch (SQLException e) {
            logger.error("Cannot not load information about process with id " + processId, e);
        }
        return answer;
    }
    
    public static String getExportPluginName(int processId) {
        String answer = "";
        try {
            answer = ProcessMysqlHelper.getExportPluginName(processId);
        } catch (SQLException e) {
            logger.error("Cannot not load information about process with id " + processId, e);
        }
        
        return answer;
    }
    
    @Override
    public List<Integer> getIdList( String filter) {
        List<Integer> idList = new LinkedList<>();
        try {
            idList = ProcessMysqlHelper.getIDList(filter);
        } catch (SQLException e) {
            logger.error("error while getting id list", e);
        }
        return idList;
    }

    public static void saveLogEntry(LogEntry entry) {
        try {
            ProcessMysqlHelper.saveLogEntry(entry);
        } catch (SQLException e) {
            logger.error("Cannot not update process log for process with id " + entry.getProcessId(), e);
        }
    }

}
