package de.sub.goobi.persistence.managers;

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
 */
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.dbutils.ResultSetHandler;
import org.goobi.beans.Batch;
import org.goobi.beans.DatabaseObject;
import org.goobi.beans.ExportValidator;
import org.goobi.beans.Institution;
import org.goobi.beans.JournalEntry;
import org.goobi.beans.Process;

import de.sub.goobi.helper.exceptions.DAOException;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ProcessManager implements IManager, Serializable {
    private static final long serialVersionUID = 3898081063234221110L;

    @Override
    public int getHitSize(String order, String filter, Institution institution) throws DAOException {
        try {
            return ProcessMysqlHelper.getProcessCount(filter, institution);
        } catch (SQLException e) {
            log.error(e);
            return 0;
        }
    }

    @Override
    public List<? extends DatabaseObject> getList(String order, String filter, Integer start, Integer count, Institution institution) {
        return getProcesses(order, filter, start, count, institution);
    }

    public static List<Process> getProcesses(String order, String filter, Integer start, Integer count, Institution institution) {
        List<Process> answer = new ArrayList<>();
        try {
            answer = ProcessMysqlHelper.getProcesses(order, filter, start, count, institution);
        } catch (SQLException e) {
            log.error("error while getting process list", e);
        }
        return answer;
    }

    public static List<Integer> getProcessIdList(String order, String filter, Integer start, Integer count) {
        List<Integer> answer = new ArrayList<>();
        try {
            answer = ProcessMysqlHelper.getProcessIdList(order, filter, start, count);
        } catch (SQLException e) {
            log.error("error while getting process id list", e);
        }
        return answer;
    }

    public static List<Process> getProcesses(String order, String filter, Institution institution) {
        return getProcesses(order, filter, 0, Integer.MAX_VALUE, institution);
    }

    public static Process getProcessById(int id) {
        Process p = null;
        try {
            p = ProcessMysqlHelper.getProcessById(id);
        } catch (SQLException e) {
            log.error(e);
        }

        return p;
    }

    public static Process getProcessByTitle(String inTitle) {
        Process p = null;
        try {
            p = ProcessMysqlHelper.getProcessByTitle(inTitle);
        } catch (SQLException e) {
            log.error(e);
        }

        return p;
    }

    public static Process getProcessByExactTitle(String inTitle) {
        Process p = null;
        try {
            p = ProcessMysqlHelper.getProcessByExactTitle(inTitle);
        } catch (SQLException e) {
            log.error(e);
        }

        return p;
    }

    public static long getSumOfFieldValue(String columnname, String filter) {
        try {
            return ProcessMysqlHelper.getSumOfFieldValue(columnname, filter);
        } catch (SQLException e) {
            log.error(e);
        }
        return 0;
    }

    public static long getCountOfFieldValue(String columnname, String filter) {
        try {
            return ProcessMysqlHelper.getCountOfFieldValue(columnname, filter);
        } catch (SQLException e) {
            log.error(e);
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
            log.error(e);
        }
    }

    public static void deleteProcess(Process o) {
        try {
            ProcessMysqlHelper.deleteProcess(o);
        } catch (SQLException e) {
            log.error(e);
        }
    }

    public static List<Process> getAllProcesses() {
        List<Process> answer = new ArrayList<>();
        try {
            answer = ProcessMysqlHelper.getAllProcesses();
        } catch (SQLException e) {
            log.error("error while getting list of all processes", e);
        }
        return answer;
    }

    public static int countProcessTitle(String title, Institution institution) {
        try {
            return ProcessMysqlHelper.getProcessCount("prozesse.titel = '" + MySQLHelper.escapeSql(title) + "'", institution);
        } catch (SQLException e) {
            log.error(e);
        }
        return 0;
    }

    public static int countProcesses(String filter) {
        try {
            return ProcessMysqlHelper.countProcesses(filter);
        } catch (SQLException e) {
            log.error(e);
        }
        return 0;
    }

    public static void saveBatch(Batch batch) {
        try {
            ProcessMysqlHelper.saveBatch(batch);
        } catch (SQLException e) {
            log.error(e);
        }
    }

    public static List<Integer> getIdsForFilter(String filter) {

        try {
            return ProcessMysqlHelper.getIDList(filter);
        } catch (SQLException e) {
            log.error(e);
        }
        return new ArrayList<>();
    }

    public static List<Batch> getBatches(int limit) {

        try {
            return ProcessMysqlHelper.getBatches(limit);
        } catch (SQLException e) {
            log.error(e);
        }
        return new ArrayList<>();
    }

    public static Batch getBatchById(int id) {

        try {
            return ProcessMysqlHelper.loadBatch(id);
        } catch (SQLException e) {
            log.error(e);
        }
        return null;
    }

    public static void deleteBatch(Batch batch) {
        try {
            ProcessMysqlHelper.deleteBatch(batch);
        } catch (SQLException e) {
            log.error(e);
        }
    }

    @SuppressWarnings("rawtypes")
    public static List runSQL(String sql) {
        try {
            return ProcessMysqlHelper.runSQL(sql);
        } catch (SQLException e) {
            log.error(e);
        }
        return new ArrayList();
    }

    public static int getNumberOfProcessesWithTitle(String title) {
        int answer = 0;
        try {
            answer = ProcessMysqlHelper.getCountOfProcessesWithTitle(title);
        } catch (SQLException e) {
            log.error("Cannot not load information about processes with title " + title, e);
        }
        return answer;
    }

    public static int getNumberOfProcessesWithRuleset(int rulesetId) {
        Integer answer = null;
        try {
            answer = ProcessMysqlHelper.getCountOfProcessesWithRuleset(rulesetId);
        } catch (SQLException e) {
            log.error("Cannot not load information about ruleset with id " + rulesetId, e);
        }
        return answer;
    }

    public static int getNumberOfProcessesWithDocket(int docketId) {
        Integer answer = null;
        try {
            answer = ProcessMysqlHelper.getCountOfProcessesWithDocket(docketId);
        } catch (SQLException e) {
            log.error("Cannot not load information about docket with id " + docketId, e);
        }
        return answer;
    }

    public static void updateImages(Integer numberOfFiles, int processId) {
        try {
            ProcessMysqlHelper.updateImages(numberOfFiles, processId);
        } catch (SQLException e) {
            log.error("Cannot not update status for process with id " + processId, e);
        }

    }

    public static void updateProcessStatus(String value, int processId) {
        try {
            ProcessMysqlHelper.updateProcessStatus(value, processId);
        } catch (SQLException e) {
            log.error("Cannot not update status for process with id " + processId, e);
        }
    }

    public static String getProcessTitle(int processId) {
        String answer = "";
        try {
            answer = ProcessMysqlHelper.getProcessTitle(processId);
        } catch (SQLException e) {
            log.error("Cannot not load information about process with id " + processId, e);
        }
        return answer;
    }

    public static String getExportPluginName(int processId) {
        String answer = "";
        try {
            answer = ProcessMysqlHelper.getExportPluginName(processId);
        } catch (SQLException e) {
            log.error("Cannot not load information about process with id " + processId, e);
        }

        return answer;
    }

    @Override
    public List<Integer> getIdList(String order, String filter, Institution institution) {
        List<Integer> idList = new LinkedList<>();
        try {
            idList = ProcessMysqlHelper.getIDList(filter);
        } catch (SQLException e) {
            log.error("error while getting id list", e);
        }
        return idList;
    }

    /**
     *
     * @deprecated use {@link JournalManager#saveJournalEntry(JournalEntry entry} instead
     */
    @Deprecated(since = "2022-09", forRemoval = true)
    public static void saveLogEntry(JournalEntry entry) {
        JournalManager.saveJournalEntry(entry);
    }

    /**
     *
     * @deprecated use {@link JournalManager#deleteJournalEntry(JournalEntry entry)} instead
     */
    @Deprecated(since = "2022-09", forRemoval = true)
    public static void deleteLogEntry(JournalEntry entry) {
        JournalManager.deleteJournalEntry(entry);
    }

    public static final ResultSetHandler<Process> resultSetToProcessHandler = new ResultSetHandler<Process>() {
        @Override
        public Process handle(ResultSet rs) throws SQLException {
            try {
                if (rs.next()) { // implies that rs != null
                    try {
                        return convert(rs);
                    } catch (DAOException e) {
                        log.error(e);
                    }
                }
            } finally {
                rs.close();
            }
            return null;
        }
    };

    public static ResultSetHandler<List<Process>> resultSetToProcessListHandler = new ResultSetHandler<List<Process>>() {
        @Override
        public List<Process> handle(ResultSet rs) throws SQLException {
            List<Process> answer = new ArrayList<>();
            try {
                while (rs.next()) {
                    try {
                        Process o = convert(rs); // implies that o != null
                        answer.add(o);
                    } catch (DAOException e) {
                        log.error(e);
                    }
                }
            } finally {
                rs.close();
            }
            return answer;
        }
    };

    private static Process convert(ResultSet rs) throws DAOException, SQLException {
        Process p = new Process();
        p.setId(rs.getInt("ProzesseID"));
        p.setTitel(rs.getString("Titel"));
        p.setAusgabename(rs.getString("ausgabename"));
        p.setIstTemplate(rs.getBoolean("IstTemplate"));
        p.setSwappedOutHibernate(rs.getBoolean("swappedOut"));
        p.setInAuswahllisteAnzeigen(rs.getBoolean("inAuswahllisteAnzeigen"));
        p.setSortHelperStatus(rs.getString("sortHelperStatus"));
        p.setSortHelperImages(rs.getInt("sortHelperImages"));
        p.setSortHelperArticles(rs.getInt("sortHelperArticles"));
        Timestamp time = rs.getTimestamp("erstellungsdatum");
        if (time != null) {
            p.setErstellungsdatum(new Date(time.getTime()));
        }
        p.setProjectId(rs.getInt("ProjekteID"));
        p.setRegelsatz(RulesetManager.getRulesetById(rs.getInt("MetadatenKonfigurationID")));
        p.setSortHelperDocstructs(rs.getInt("sortHelperDocstructs"));
        p.setSortHelperMetadata(rs.getInt("sortHelperMetadata"));
        Integer batchID = rs.getInt("batchID");
        if (!rs.wasNull()) {
            Batch batch = ProcessMysqlHelper.loadBatch(batchID);
            p.setBatch(batch);
        }
        p.setDocket(DocketManager.getDocketById(rs.getInt("docketID")));
        String exportValidatorRs = rs.getString("exportValidator");
        if ("".equals(exportValidatorRs) || exportValidatorRs == null) {
            p.setExportValidator(null);
        } else {
            p.setExportValidator(new ExportValidator(rs.getString("exportValidator")));
        }

        p.setJournal(JournalManager.getLogEntriesForProcess(p.getId()));

        p.setMediaFolderExists(rs.getBoolean("mediaFolderExists"));

        p.setPauseAutomaticExecution(rs.getBoolean("pauseAutomaticExecution"));

        return p;
    }

}
