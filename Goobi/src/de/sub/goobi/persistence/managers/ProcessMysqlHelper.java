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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;
import org.goobi.beans.Batch;
import org.goobi.beans.LogEntry;
import org.goobi.beans.Masterpiece;
import org.goobi.beans.Process;
import org.goobi.beans.Processproperty;
import org.goobi.beans.Step;
import org.goobi.beans.Template;
import org.goobi.production.enums.LogType;

import de.sub.goobi.helper.exceptions.DAOException;

class ProcessMysqlHelper implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 5087816359001071079L;

    private static final Logger logger = Logger.getLogger(ProcessMysqlHelper.class);

    public static Process getProcessById(int id) throws SQLException {
        Connection connection = null;
        String sql = "SELECT * FROM prozesse WHERE ProzesseID = ?";
        Object[] param = { id };
        try {
            connection = MySQLHelper.getInstance().getConnection();
            Process p = new QueryRunner().query(connection, sql, resultSetToProcessHandler, param);
            return p;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static Process getProcessByTitle(String inTitle) throws SQLException {
        Connection connection = null;
        String sql = "SELECT * FROM prozesse WHERE Titel LIKE ?";
        Object[] param = { inTitle };
        try {
            connection = MySQLHelper.getInstance().getConnection();
            Process p = new QueryRunner().query(connection, sql, resultSetToProcessHandler, param);
            return p;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void saveProcess(Process o, boolean processOnly) throws DAOException {
        try {
            o.setSortHelperStatus(o.getFortschritt());

            if (o.getBatch() != null) {
                saveBatch(o.getBatch());
            }

            if (o.getId() == null) {
                // new process
                insertProcess(o);

            } else {
                // process exists already in database
                updateProcess(o);
            }

            if (!processOnly) {
                List<Step> stepList = o.getSchritte();
                for (Step s : stepList) {
                    StepMysqlHelper.saveStep(s);
                }
            }
            List<Processproperty> properties = o.getEigenschaften();
            for (Processproperty pe : properties) {
                PropertyManager.saveProcessProperty(pe);
            }

            for (Masterpiece object : o.getWerkstuecke()) {
                MasterpieceManager.saveMasterpiece(object);
            }

            List<Template> templates = o.getVorlagen();
            for (Template template : templates) {
                TemplateManager.saveTemplate(template);
            }

            for (LogEntry logEntry : o.getProcessLog()) {
                saveLogEntry(logEntry);
            }

        } catch (SQLException e) {
            //            logger.error("Error while saving process " + o.getTitel(), e);
            throw new DAOException(e);
        }
    }

    public static void saveBatch(Batch batch) throws SQLException {
        Connection connection = null;
        Timestamp start = null;
        Timestamp end = null;

        if (batch.getStartDate() != null) {
            start = new Timestamp(batch.getStartDate().getTime());
        }

        if (batch.getEndDate() != null) {
            end = new Timestamp(batch.getEndDate().getTime());
        }

        
        if (batch.getBatchId() == null) {
            StringBuilder sql = new StringBuilder();
            sql.append("INSERT INTO batches (batchName, startDate, endDate) VALUES (?,?,?)");
            try {
                connection = MySQLHelper.getInstance().getConnection();
                QueryRunner run = new QueryRunner();
                Integer id = run.insert(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler, batch.getBatchName(), start, end);
                if (id != null) {
                    batch.setBatchId(id);
                }
            } finally {
                if (connection != null) {
                    MySQLHelper.closeConnection(connection);
                }
            }

        } else {
            StringBuilder sql = new StringBuilder();
            sql.append("UPDATE batches set batchName = ?, startDate = ?, endDate= ? where id = ?");
            try {
                connection = MySQLHelper.getInstance().getConnection();
                QueryRunner run = new QueryRunner();
                run.update(connection, sql.toString(), batch.getBatchName(), start, end, batch.getBatchId());
            } finally {
                if (connection != null) {
                    MySQLHelper.closeConnection(connection);
                }
            }

        }

    }

    public static void deleteProcess(Process o) throws SQLException {
        if (o.getId() != null) {

            // delete metadata
            MetadataManager.deleteMetadata(o.getId());

            // delete properties

            for (Processproperty object : o.getEigenschaften()) {
                PropertyManager.deleteProcessProperty(object);
            }
            // delete templates

            for (Template object : o.getVorlagen()) {
                TemplateManager.deleteTemplate(object);
            }

            // delete masterpieces
            for (Masterpiece object : o.getWerkstuecke()) {
                MasterpieceManager.deleteMasterpiece(object);
            }

            for (Step object : o.getSchritte()) {
                StepManager.deleteStep(object);
            }

            // delete process
            String sql = "DELETE FROM prozesse WHERE ProzesseID = ?";
            Object[] param = { o.getId() };
            Connection connection = null;
            try {
                connection = MySQLHelper.getInstance().getConnection();
                QueryRunner run = new QueryRunner();
                run.update(connection, sql, param);
            } finally {
                if (connection != null) {
                    MySQLHelper.closeConnection(connection);
                }
            }
        }
    }

    public static int getProcessCount(String order, String filter) throws SQLException {

        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(ProzesseID) FROM prozesse, projekte WHERE prozesse.ProjekteID = projekte.ProjekteID ");
        if (filter != null && !filter.isEmpty()) {
            sql.append(" AND " + filter);
        }
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (logger.isTraceEnabled()) {
                logger.trace(sql.toString());
            }
            //            if (filter != null && !filter.isEmpty()) {
            //                return new QueryRunner().query(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler);
            //            } else {
            return new QueryRunner().query(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler);
            //            }
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static List<Process> getProcesses(String order, String filter, Integer start, Integer count) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT  * FROM prozesse, projekte WHERE prozesse.ProjekteID = projekte.ProjekteID ");
        if (filter != null && !filter.isEmpty()) {
            sql.append(" AND " + filter);
        }
        if (order != null && !order.isEmpty()) {
            sql.append(" ORDER BY " + order);
        }
        if (start != null && count != null) {
            sql.append(" LIMIT " + start + ", " + count);
        }
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (logger.isTraceEnabled()) {
                logger.trace(sql.toString());
            }
            List<Process> ret = null;
            ret = new QueryRunner().query(connection, sql.toString(), resultSetToProcessListHandler);
            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static List<Integer> getProcessIdList(String order, String filter, Integer start, Integer count) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT prozesse.ProzesseID FROM prozesse, projekte WHERE prozesse.ProjekteID = projekte.ProjekteID ");
        if (filter != null && !filter.isEmpty()) {
            sql.append(" AND " + filter);
        }
        if (order != null && !order.isEmpty()) {
            sql.append(" ORDER BY " + order);
        }
        if (start != null && count != null) {
            sql.append(" LIMIT " + start + ", " + count);
        }
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (logger.isTraceEnabled()) {
                logger.trace(sql.toString());
            }
            List<Integer> ret = null;
            ret = new QueryRunner().query(connection, sql.toString(), MySQLHelper.resultSetToIntegerListHandler);
            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static List<Process> getAllProcesses() throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM prozesse");
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (logger.isTraceEnabled()) {
                logger.trace(sql.toString());
            }
            List<Process> ret = new QueryRunner().query(connection, sql.toString(), resultSetToProcessListHandler);
            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static ResultSetHandler<List<Process>> resultSetToProcessListHandler = new ResultSetHandler<List<Process>>() {
        @Override
        public List<Process> handle(ResultSet rs) throws SQLException {
            List<Process> answer = new ArrayList<Process>();
            try {
                while (rs.next()) {
                    try {
                        Process o = convert(rs);
                        if (o != null) {
                            answer.add(o);
                        }
                    } catch (DAOException e) {
                        logger.error(e);
                    }
                }
            } finally {
                if (rs != null) {
                    rs.close();
                }
            }
            return answer;
        }
    };

    public static ResultSetHandler<Process> resultSetToProcessHandler = new ResultSetHandler<Process>() {
        @Override
        public Process handle(ResultSet rs) throws SQLException {
            try {
                if (rs.next()) {
                    try {
                        Process o = convert(rs);
                        return o;
                    } catch (DAOException e) {
                        logger.error(e);
                    }
                }
            } finally {
                if (rs != null) {
                    rs.close();
                }
            }
            return null;
        }
    };

    private static void insertProcess(Process o) throws SQLException {
        String sql = "INSERT INTO prozesse " + generateInsertQuery(false) + generateValueQuery(false);
        Object[] param = generateParameter(o, false, false);
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            if (logger.isTraceEnabled()) {
                logger.trace(sql.toString() + ", " + Arrays.toString(param));
            }
            Integer id = run.insert(connection, sql, MySQLHelper.resultSetToIntegerHandler, param);
            if (id != null) {
                o.setId(id);
            }
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    private static String generateInsertQuery(boolean includeProcessId) {
        if (!includeProcessId) {
            return "(Titel, ausgabename, IstTemplate, swappedOut, inAuswahllisteAnzeigen, sortHelperStatus,"
                    + "sortHelperImages, sortHelperArticles, erstellungsdatum, ProjekteID, MetadatenKonfigurationID, sortHelperDocstructs,"
                    + "sortHelperMetadata, batchID, docketID)" + " VALUES ";
        } else {
            return "(ProzesseID, Titel, ausgabename, IstTemplate, swappedOut, inAuswahllisteAnzeigen, sortHelperStatus,"
                    + "sortHelperImages, sortHelperArticles, erstellungsdatum, ProjekteID, MetadatenKonfigurationID, sortHelperDocstructs,"
                    + "sortHelperMetadata, batchID, docketID)" + " VALUES ";
        }
    }

    private static String generateValueQuery(boolean includeProcessId) {
        if (!includeProcessId) {
            return "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        } else {
            return "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        }
    }

    private static Object[] generateParameter(Process o, boolean createNewTimestamp, boolean includeProcessID) {
        Date d = null;
        if (createNewTimestamp) {
            d = new Date();
        } else {
            d = o.getErstellungsdatum();
        }
        if (o.getProjectId() == null && o.getProjekt() != null) {
            o.setProjectId(o.getProjekt().getId());
        }

        Timestamp datetime = new Timestamp(d.getTime());
        if (!includeProcessID) {
            Object[] param = { o.getTitel(), o.getAusgabename(), o.isIstTemplate(), o.isSwappedOutHibernate(), o.isInAuswahllisteAnzeigen(), o
                    .getSortHelperStatus(), o.getSortHelperImages(), o.getSortHelperArticles(), datetime, o.getProjectId(), o.getRegelsatz().getId(),
                    o.getSortHelperDocstructs(), o.getSortHelperMetadata(), o.getBatch() == null ? null : o.getBatch().getBatchId(), o
                            .getDocket() == null ? null : o.getDocket().getId() };

            return param;
        } else {
            Object[] param = { o.getId(), o.getTitel(), o.getAusgabename(), o.isIstTemplate(), o.isSwappedOutHibernate(), o
                    .isInAuswahllisteAnzeigen(), o.getSortHelperStatus(), o.getSortHelperImages(), o.getSortHelperArticles(), datetime, o
                            .getProjectId(), o.getRegelsatz().getId(), o.getSortHelperDocstructs(), o.getSortHelperMetadata(), o.getBatch() == null
                                    ? null : o.getBatch().getBatchId(), o.getDocket() == null ? null : o.getDocket().getId() };

            return param;
        }
    }

    private static void updateProcess(Process o) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE prozesse SET ");
        sql.append(" Titel = ?,");
        sql.append(" ausgabename = ?,");
        sql.append(" IstTemplate = ?,");
        sql.append(" swappedOut = ?,");
        sql.append(" inAuswahllisteAnzeigen = ?,");
        sql.append(" sortHelperStatus = ?,");
        sql.append(" sortHelperImages = ?,");
        sql.append(" sortHelperArticles = ?,");
        sql.append(" erstellungsdatum = ?,");
        sql.append(" ProjekteID = ?,");
        sql.append(" MetadatenKonfigurationID = ?,");
        sql.append(" sortHelperDocstructs = ?,");
        sql.append(" sortHelperMetadata = ?,");
        //        sql.append(" wikifield = ?,");
        sql.append(" batchID = ?,");
        sql.append(" docketID = ?");
        sql.append(" WHERE ProzesseID = " + o.getId());

        Object[] param = generateParameter(o, false, false);

        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            run.update(connection, sql.toString(), param);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

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
        //        p.setWikifield(rs.getString("wikifield"));
        Integer batchID = rs.getInt("batchID");
        if (!rs.wasNull()) {
            Batch batch = loadBatch(batchID);
            p.setBatch(batch);

        } else {
        }
        p.setDocket(DocketManager.getDocketById(rs.getInt("docketID")));

        p.setProcessLog(getLogEntriesForProcess(p.getId()));

        return p;
    }

    public static Batch loadBatch(Integer batchID) throws SQLException {
        String sql = "SELECT * FROM batches WHERE id = ?";
        Connection connection = null;

        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (logger.isTraceEnabled()) {
                logger.trace(sql.toString());
            }
            return new QueryRunner().query(connection, sql.toString(), resultSetToBatchHandler, batchID);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static List<Integer> getIDList(String filter) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT prozesseID FROM prozesse");
        if (filter != null && !filter.isEmpty()) {
            sql.append(" WHERE " + filter);
        }

        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (logger.isTraceEnabled()) {
                logger.trace(sql.toString());
            }
            List<Integer> ret = null;
            ret = new QueryRunner().query(connection, sql.toString(), MySQLHelper.resultSetToIntegerListHandler);

            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }

    }

    public static int countProcesses(String filter) throws SQLException {
        String sql = "select count(prozesseID) from prozesse ";
        if (filter != null && filter.length() > 0) {
            sql += " WHERE " + filter;
        }
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            return new QueryRunner().query(connection, sql, MySQLHelper.resultSetToIntegerHandler);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static List<Batch> getBatches(int limit) throws SQLException {
        String sql = "SELECT * FROM batches";

        sql += " ORDER BY id desc ";
        if (limit > 0) {
            sql += " limit " + limit;
        }
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            return new QueryRunner().query(connection, sql, resultSetToBatchListHandler);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static List runSQL(String sql) throws SQLException {
        Connection connection = null;
        List answer = new ArrayList();
        try {
            connection = MySQLHelper.getInstance().getConnection();
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = stmt.executeQuery(sql);
            int columnCount = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = rs.getString(i);
                }
                answer.add(row);
            }
            return answer;

        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void updateImages(Integer numberOfFiles, int processId) throws SQLException {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            StringBuilder sql = new StringBuilder();
            Object[] param = { numberOfFiles, processId };
            sql.append("UPDATE prozesse SET sortHelperImages = ? WHERE ProzesseID = ?");
            if (logger.isTraceEnabled()) {
                logger.trace(sql.toString() + ", " + Arrays.toString(param));
            }
            run.update(connection, sql.toString(), param);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void updateProcessStatus(String value, int processId) throws SQLException {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            StringBuilder sql = new StringBuilder();
            Object[] param = { value, processId };

            sql.append("UPDATE prozesse SET sortHelperStatus = ? WHERE ProzesseID = ?");
            if (logger.isTraceEnabled()) {
                logger.trace(sql.toString() + ", " + Arrays.toString(param));
            }
            run.update(connection, sql.toString(), param);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static int getCountOfProcessesWithRuleset(int rulesetId) throws SQLException {
        Connection connection = null;

        String query = "select count(ProzesseID) from prozesse where MetadatenKonfigurationID = ?";
        try {
            connection = MySQLHelper.getInstance().getConnection();
            Object[] param = { rulesetId };
            return new QueryRunner().query(connection, query, MySQLHelper.resultSetToIntegerHandler, param);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static int getCountOfProcessesWithDocket(int docketId) throws SQLException {
        Connection connection = null;
        String query = "select count(ProzesseID) from prozesse where  docketID= ?";
        try {
            connection = MySQLHelper.getInstance().getConnection();
            Object[] param = { docketId };
            return new QueryRunner().query(connection, query, MySQLHelper.resultSetToIntegerHandler, param);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static int getCountOfProcessesWithTitle(String title) throws SQLException {
        Connection connection = null;
        String query = "select count(prozesse.ProzesseID) from prozesse where  titel = ?";
        try {
            connection = MySQLHelper.getInstance().getConnection();
            Object[] param = { title };
            return new QueryRunner().query(connection, query, MySQLHelper.resultSetToIntegerHandler, param);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static long getSumOfFieldValue(String columnname, String filter) throws SQLException {
        String sql = "select sum(prozesse." + columnname + ") from prozesse ";
        if (filter != null && filter.length() > 0) {
            sql += " WHERE " + filter;
        }
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            return new QueryRunner().query(connection, sql, MySQLHelper.resultSetToLongHandler);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static long getCountOfFieldValue(String columnname, String filter) throws SQLException {
        String sql = "select count(prozesse." + columnname + ") from prozesse ";
        if (filter != null && filter.length() > 0) {
            sql += " WHERE " + filter;
        }
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            return new QueryRunner().query(connection, sql, MySQLHelper.resultSetToLongHandler);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static String getProcessTitle(int processId) throws SQLException {
        String sql = "SELECT titel from prozesse WHERE ProzesseID = " + processId;
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            return new QueryRunner().query(connection, sql, MySQLHelper.resultSetToStringHandler);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static String getExportPluginName(int processId) throws SQLException {
        String sql = "SELECT stepPlugin FROM schritte WHERE schritte.stepPlugin != '' AND schritte.typExportDMS = true AND schritte.ProzesseID = "
                + processId + " LIMIT 1";
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            return new QueryRunner().query(connection, sql, MySQLHelper.resultSetToStringHandler);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static LogEntry saveLogEntry(LogEntry logEntry) throws SQLException {

        if (logEntry.getId() == null) {
            return inserLogEntry(logEntry);
        } else {
            updateLogEntry(logEntry);
            return logEntry;
        }
    }

    private static void updateLogEntry(LogEntry logEntry) throws SQLException {
        String sql =
                "UPDATE processlog set processID =?, creationDate = ?, userName = ?, type = ? , content = ?, secondContent = ?, thirdContent = ? WHERE id = ?";

        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            run.update(connection, sql, logEntry.getProcessId(), logEntry.getCreationDate() == null ? null : new Timestamp(logEntry.getCreationDate()
                    .getTime()), logEntry.getUserName(), logEntry.getType().getTitle(), logEntry.getContent(), logEntry.getSecondContent(), logEntry
                            .getThirdContent(), logEntry.getId());
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    private static LogEntry inserLogEntry(LogEntry logEntry) throws SQLException {
        String sql =
                "INSERT INTO processlog (processID, creationDate, userName, type , content, secondContent, thirdContent ) VALUES (?, ?,  ?, ?, ?, ?, ?);";
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            int id = run.insert(connection, sql, MySQLHelper.resultSetToIntegerHandler, logEntry.getProcessId(), logEntry.getCreationDate() == null
                    ? null : new Timestamp(logEntry.getCreationDate().getTime()), logEntry.getUserName(), logEntry.getType().getTitle(), logEntry
                            .getContent(), logEntry.getSecondContent(), logEntry.getThirdContent()

            );
            logEntry.setId(id);
            return logEntry;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void deleteLogEntry(LogEntry logEntry) throws SQLException {
        if (logEntry.getId() != null) {
            Connection connection = null;
            try {
                connection = MySQLHelper.getInstance().getConnection();
                QueryRunner run = new QueryRunner();
                String sql = "DELETE FROM processlog WHERE id = " + logEntry.getId();
                run.update(connection, sql);
            } finally {
                if (connection != null) {
                    MySQLHelper.closeConnection(connection);
                }
            }
        }
    }

    public static List<LogEntry> getLogEntriesForProcess(int processId) throws SQLException {
        Connection connection = null;

        String sql = " SELECT * from processlog WHERE processId = " + processId + " ORDER BY creationDate";

        try {
            connection = MySQLHelper.getInstance().getConnection();
            List<LogEntry> ret = new QueryRunner().query(connection, sql.toString(), resultSetToLogEntryListHandler);
            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }

    }

    public static ResultSetHandler<Batch> resultSetToBatchHandler = new ResultSetHandler<Batch>() {
        @Override
        public Batch handle(ResultSet rs) throws SQLException {

            try {
                if (rs.next()) {
                    return convertBatch(rs);
                }
            } finally {
                if (rs != null) {
                    rs.close();
                }
            }
            return null;
        }
    };

    public static ResultSetHandler<List<Batch>> resultSetToBatchListHandler = new ResultSetHandler<List<Batch>>() {
        @Override
        public List<Batch> handle(ResultSet rs) throws SQLException {
            List<Batch> answer = new ArrayList<>();
            try {
                while (rs.next()) {
                    answer.add(convertBatch(rs));
                }
            } finally {
                if (rs != null) {
                    rs.close();
                }
            }
            return answer;
        }
    };

    private static Batch convertBatch(ResultSet rs) throws SQLException {
        Batch batch = new Batch();
        batch.setBatchId(rs.getInt("id"));
        batch.setBatchName(rs.getString("batchName"));
        Timestamp start = rs.getTimestamp("startDate");
        if (start != null) {
            batch.setStartDate(new Date(start.getTime()));
        }
        Timestamp end = rs.getTimestamp("endDate");
        if (end != null) {
            batch.setEndDate(new Date(end.getTime()));
        }
        return batch;
    }

    public static ResultSetHandler<List<LogEntry>> resultSetToLogEntryListHandler = new ResultSetHandler<List<LogEntry>>() {
        @Override
        public List<LogEntry> handle(ResultSet rs) throws SQLException {
            List<LogEntry> answer = new ArrayList<LogEntry>();
            try {
                while (rs.next()) {

                    int id = rs.getInt("id");
                    int processId = rs.getInt("processID");
                    Timestamp time = rs.getTimestamp("creationDate");
                    Date creationDate = null;
                    if (time != null) {
                        creationDate = new Date(time.getTime());
                    }
                    String userName = rs.getString("userName");
                    LogType type = LogType.getByTitle(rs.getString("type"));
                    String content = rs.getString("content");
                    String secondContent = rs.getString("secondContent");
                    String thirdContent = rs.getString("thirdContent");

                    LogEntry entry = new LogEntry();
                    entry.setId(id);
                    entry.setProcessId(processId);
                    entry.setCreationDate(creationDate);
                    entry.setUserName(userName);
                    entry.setType(type);
                    entry.setContent(content);
                    entry.setSecondContent(secondContent);
                    entry.setThirdContent(thirdContent);
                    answer.add(entry);
                }
            } finally {
                if (rs != null) {
                    rs.close();
                }
            }
            return answer;
        }
    };

}
