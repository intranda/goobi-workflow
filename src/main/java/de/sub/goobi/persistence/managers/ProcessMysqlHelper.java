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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.*;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang3.StringUtils;
import org.goobi.beans.Batch;
import org.goobi.beans.Institution;
import org.goobi.beans.JournalEntry;
import org.goobi.beans.JournalEntry.EntryType;
import org.goobi.beans.Masterpiece;
import org.goobi.beans.Process;
import org.goobi.beans.Processproperty;
import org.goobi.beans.Step;
import org.goobi.beans.Template;
import org.joda.time.LocalDate;

import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.MySQLHelper.SQLTYPE;
import lombok.extern.log4j.Log4j2;

@Log4j2
class ProcessMysqlHelper implements Serializable {
    private static final long serialVersionUID = 5087816359001071079L;

    public static Process getProcessById(int id) throws SQLException {
        Connection connection = null;
        String sql = "SELECT * FROM prozesse WHERE ProzesseID = ?";
        Object[] param = { id };
        try {
            connection = MySQLHelper.getInstance().getConnection();
            return new QueryRunner().query(connection, sql, ProcessManager.resultSetToProcessHandler, param);
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
            return new QueryRunner().query(connection, sql, ProcessManager.resultSetToProcessHandler, param);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static Process getProcessByExactTitle(String inTitle) throws SQLException {
        Connection connection = null;
        String sql = "SELECT * FROM prozesse WHERE Titel = ?";
        Object[] param = { inTitle };
        try {
            connection = MySQLHelper.getInstance().getConnection();
            return new QueryRunner().query(connection, sql, ProcessManager.resultSetToProcessHandler, param);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void saveProcess(Process o, boolean processOnly) throws DAOException {
        try {
            o.setSortHelperStatus(o.getFortschritt());

            if (StringUtils.isNotBlank(o.getTitel())) {
                o.setTitel(o.getTitel().trim());
            }

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

            for (JournalEntry logEntry : o.getJournal()) {
                JournalManager.saveJournalEntry(logEntry);
            }

        } catch (SQLException e) {
            throw new DAOException(e);
        }
    }

    public static void saveBatch(Batch batch) throws SQLException {
        Connection connection = null;
        Timestamp start = null;
        Timestamp end = null;

        if (batch.getStartDate() != null) {
            LocalDate localDate = new LocalDate(batch.getStartDate());
            start = new Timestamp(localDate.toDateTimeAtStartOfDay().getMillis());

        }

        if (batch.getEndDate() != null) {
            LocalDate localDate = new LocalDate(batch.getEndDate());
            end = new Timestamp(localDate.toDateTimeAtStartOfDay().getMillis());
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

    public static void deleteBatch(Batch batch) throws SQLException {
        Connection connection = null;
        if (batch != null && batch.getBatchId() != null) {
            String deleteBatchIdFromProcess = "UPDATE prozesse set batchID = NULL WHERE batchID = ?";
            String deleteBatchFromBatches = "DELETE from batches where id = ?";
            try {
                connection = MySQLHelper.getInstance().getConnection();
                QueryRunner run = new QueryRunner();
                run.update(connection, deleteBatchIdFromProcess, batch.getBatchId());
                run.update(connection, deleteBatchFromBatches, batch.getBatchId());
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

            StepManager.deleteAllSteps(o.getSchritte());

            JournalManager.deleteAllJournalEntries(o.getId(), EntryType.PROCESS);

            // delete process
            String sql = "DELETE FROM prozesse WHERE ProzesseID = ?";

            Connection connection = null;
            try {
                connection = MySQLHelper.getInstance().getConnection();
                QueryRunner run = new QueryRunner();
                run.update(connection, sql, o.getId());
            } finally {
                if (connection != null) {
                    MySQLHelper.closeConnection(connection);
                }
            }
        }
    }

    public static int getProcessCount(String filter, Institution institution) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(1) FROM prozesse left join batches on prozesse.batchID = batches.id ");
        sql.append("left JOIN projekte on prozesse.ProjekteID = projekte.ProjekteID ");
        if (institution != null) {
            sql.append("and projekte.institution_id = ");
            sql.append(institution.getId());
        }

        if (filter != null && !filter.isEmpty()) {
            sql.append(" WHERE " + filter);
        } else if (MySQLHelper.getInstance().getSqlType() == SQLTYPE.MYSQL) {
            sql.append("WHERE ProzesseID > 0 ");
        }

        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (log.isTraceEnabled()) {
                log.trace(sql.toString());
            }
            return new QueryRunner().query(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static List<Process> getProcesses(String order, String filter, Integer start, Integer count, Institution institution) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT prozesse.* FROM prozesse left join batches on prozesse.batchID = batches.id ");
        sql.append("left JOIN projekte on prozesse.ProjekteID = projekte.ProjekteID ");
        if (institution != null) {
            sql.append("and projekte.institution_id = ");
            sql.append(institution.getId());
        }

        String sortfield = MySQLHelper.prepareSortField(order, sql);

        if (filter != null && !filter.isEmpty()) {
            sql.append(" WHERE " + filter);
        }
        if (StringUtils.isNotBlank(sortfield)) {
            sql.append(" ORDER BY " + sortfield);
        }
        if (start != null && count != null) {
            sql.append(" LIMIT " + start + ", " + count);
        }
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (log.isTraceEnabled()) {
                log.trace(sql.toString());
            }
            List<Process> ret = null;
            ret = new QueryRunner().query(connection, sql.toString(), ProcessManager.resultSetToProcessListHandler);
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
        sql.append("SELECT ProzesseID FROM prozesse left join batches on prozesse.batchID = batches.id ");
        sql.append("left join projekte on prozesse.ProjekteID = projekte.ProjekteID ");
        String sortfield = MySQLHelper.prepareSortField(order, sql);
        if (filter != null && !filter.isEmpty()) {
            sql.append(" AND " + filter);
        }
        if (StringUtils.isNotBlank(sortfield)) {
            sql.append(" ORDER BY " + sortfield);
        }
        if (start != null && count != null) {
            sql.append(" LIMIT " + start + ", " + count);
        }
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (log.isTraceEnabled()) {
                log.trace(sql.toString());
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
            if (log.isTraceEnabled()) {
                log.trace(sql.toString());
            }
            return new QueryRunner().query(connection, sql.toString(), ProcessManager.resultSetToProcessListHandler);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    private static void insertProcess(Process o) throws SQLException {
        String sql = "INSERT INTO prozesse " + generateInsertQuery(false) + generateValueQuery(false);
        Object[] param = generateParameter(o, false, false);
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            if (log.isTraceEnabled()) {
                log.trace(sql + ", " + Arrays.toString(param));
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
                    + "sortHelperMetadata, batchID, docketID, mediaFolderExists, pauseAutomaticExecution, exportValidator)" + " VALUES ";
        } else {
            return "(ProzesseID, Titel, ausgabename, IstTemplate, swappedOut, inAuswahllisteAnzeigen, sortHelperStatus,"
                    + "sortHelperImages, sortHelperArticles, erstellungsdatum, ProjekteID, MetadatenKonfigurationID, sortHelperDocstructs,"
                    + "sortHelperMetadata, batchID, docketID, mediaFolderExists, pauseAutomaticExecution, exportValidator)" + " VALUES ";
        }
    }

    private static String generateValueQuery(boolean includeProcessId) {
        if (!includeProcessId) {
            return "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        } else {
            return "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
            return new Object[] { o.getTitel(), o.getAusgabename(), o.isIstTemplate(), o.isSwappedOutHibernate(), o.isInAuswahllisteAnzeigen(),
                    o.getSortHelperStatus(), o.getSortHelperImages(), o.getSortHelperArticles(), datetime, o.getProjectId(), o.getRegelsatz().getId(),
                    o.getSortHelperDocstructs(), o.getSortHelperMetadata(), o.getBatch() == null ? null : o.getBatch().getBatchId(),
                    o.getDocket() == null ? null : o.getDocket().getId(), o.isMediaFolderExists(), o.isPauseAutomaticExecution(),
                    o.getExportValidator() == null ? null : o.getExportValidator().getLabel() };

        } else {
            return new Object[] { o.getId(), o.getTitel(), o.getAusgabename(), o.isIstTemplate(), o.isSwappedOutHibernate(),
                    o.isInAuswahllisteAnzeigen(), o.getSortHelperStatus(), o.getSortHelperImages(), o.getSortHelperArticles(), datetime,
                    o.getProjectId(), o.getRegelsatz().getId(), o.getSortHelperDocstructs(), o.getSortHelperMetadata(),
                    o.getBatch() == null ? null : o.getBatch().getBatchId(), o.getDocket() == null ? null : o.getDocket().getId(),
                    o.isMediaFolderExists(), o.isPauseAutomaticExecution(),
                    o.getExportValidator() == null ? null : o.getExportValidator().getLabel() };
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
        sql.append(" batchID = ?,");
        sql.append(" docketID = ?,");
        sql.append(" mediaFolderExists = ?,");
        sql.append(" pauseAutomaticExecution = ?,");
        sql.append(" exportValidator = ?");
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

    public static Batch loadBatch(Integer batchID) throws SQLException {
        String sql = "SELECT * FROM batches WHERE id = ?";
        Connection connection = null;

        try {
            connection = MySQLHelper.getInstance().getConnection();
            return new QueryRunner().query(connection, sql, resultSetToBatchHandler, batchID);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static List<Integer> getIDList(Optional<String> order, String filter) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT prozesseID FROM prozesse left join batches on prozesse.batchId = batches.id ");
        sql.append("left join projekte on prozesse.ProjekteID = projekte.ProjekteID ");

        if (filter != null && !filter.isEmpty()) {
            sql.append(" WHERE " + filter);
        }
        if (order.isPresent()) {
            String sortfield = MySQLHelper.prepareSortField(order.get(), sql);
            if (StringUtils.isNotBlank(sortfield)) {
                sql.append(" ORDER BY " + sortfield);
            }
        }
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (log.isTraceEnabled()) {
                log.trace(sql.toString());
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
        StringBuilder sql = new StringBuilder("select count(1) from prozesse ");
        if (filter != null && filter.length() > 0) {
            sql.append(" WHERE ").append(filter);
        } else if (MySQLHelper.getInstance().getSqlType() == SQLTYPE.MYSQL) {
            sql.append(" WHERE ProzesseID > 0");
        }
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            return new QueryRunner().query(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static List<Batch> getBatches(int limit) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM batches");

        sql.append(" ORDER BY id desc ");
        if (limit > 0) {
            sql.append(" limit ").append(limit);
        }
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            return new QueryRunner().query(connection, sql.toString(), resultSetToBatchListHandler);
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
            try (Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
                ResultSet rs = stmt.executeQuery(sql);
                int columnCount = rs.getMetaData().getColumnCount();
                while (rs.next()) {
                    Object[] row = new Object[columnCount];
                    for (int i = 1; i <= columnCount; i++) {
                        row[i - 1] = rs.getString(i);
                    }
                    answer.add(row);
                }
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
            if (log.isTraceEnabled()) {
                log.trace(sql.toString() + ", " + Arrays.toString(param));
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
            if (log.isTraceEnabled()) {
                log.trace(sql.toString() + ", " + Arrays.toString(param));
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

        String query = "select count(1) from prozesse where MetadatenKonfigurationID = ?";
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
        String query = "select count(1) from prozesse where docketID= ?";
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
        String query = "select count(1) from prozesse where titel = ?";
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
        StringBuilder sql = new StringBuilder("select sum(prozesse.").append(columnname).append(") from prozesse ");
        if (filter != null && filter.length() > 0) {
            sql.append(" WHERE ").append(filter);
        }
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            return new QueryRunner().query(connection, sql.toString(), MySQLHelper.resultSetToLongHandler);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static long getCountOfFieldValue(String columnname, String filter) throws SQLException {
        StringBuilder sql = new StringBuilder("select count(prozesse.").append(columnname).append(") from prozesse ");
        if (filter != null && filter.length() > 0) {
            sql.append(" WHERE ").append(filter);
        }
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            return new QueryRunner().query(connection, sql.toString(), MySQLHelper.resultSetToLongHandler);
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

    public static final ResultSetHandler<Batch> resultSetToBatchHandler = new ResultSetHandler<Batch>() {
        @Override
        public Batch handle(ResultSet rs) throws SQLException {

            try {
                if (rs.next()) {
                    return convertBatch(rs);
                }
            } finally {
                rs.close();
            }
            return null;
        }
    };

    public static final ResultSetHandler<List<Batch>> resultSetToBatchListHandler = new ResultSetHandler<List<Batch>>() {
        @Override
        public List<Batch> handle(ResultSet rs) throws SQLException {
            List<Batch> answer = new ArrayList<>();
            try {
                while (rs.next()) {
                    answer.add(convertBatch(rs));
                }
            } finally {
                rs.close();
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
            LocalDate localDate = new LocalDate(start);

            batch.setStartDate(localDate.toDate());
        }
        Timestamp end = rs.getTimestamp("endDate");
        if (end != null) {
            LocalDate localDate = new LocalDate(end);
            batch.setEndDate(localDate.toDate());
        }
        return batch;
    }
}
