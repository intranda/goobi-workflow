package de.sub.goobi.persistence.managers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;
import org.goobi.beans.Process;
import org.goobi.beans.Step;

import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.apache.MySQLHelper;
import de.sub.goobi.persistence.apache.MySQLUtils;

class ProcessMysqlHelper {
    private static final Logger logger = Logger.getLogger(ProcessMysqlHelper.class);

    public static Process getProcessById(int id) throws SQLException {
        Connection connection = MySQLHelper.getInstance().getConnection();
        String sql = "SELECT * FROM prozesse WHERE ProzesseID = ?";
        Object[] param = { id };
        try {
            Process p = new QueryRunner().query(connection, sql, resultSetToProcessHandler, param);
            return p;
        } finally {
            MySQLHelper.closeConnection(connection);
        }
    }

    public static void saveProcess(Process o, boolean processOnly) throws DAOException {
        try {

            if (o.getId() == null) {
                // new process
                insertProcess(o);
                // TODO prozesseID in Schritten setzen?
                //                List<Step> stepList = o.getSchritte();
                //                StepMysqlHelper.insertBatchStepList(stepList);

            } else {
                // process exists already in database
                updateProcess(o);
                //                List<Step> stepList = o.getSchritte();
                //                StepMysqlHelper.updateBatchList(stepList);
            }

            if (!processOnly) {
                List<Step> stepList = o.getSchritte();
                for (Step s : stepList) {
                    StepMysqlHelper.saveStep(s);
                }
            }
            // TODO Eigenschaften speichern
            // TODO Werkstuecke speichern
            // TODO Vorlagen speichern

        } catch (SQLException e) {
            //            logger.error("Error while saving process " + o.getTitel(), e);
            throw new DAOException(e);
        }
    }

    public static void deleteProcess(Process o) throws SQLException {
        if (o.getId() != null) {
            String sql = "DELETE FROM prozesse WHERE ProzesseID = ?";
            Object[] param = { o.getId() };
            Connection connection = MySQLHelper.getInstance().getConnection();
            try {
                QueryRunner run = new QueryRunner();
                run.update(connection, sql, param);
            } finally {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static int getProcessCount(String order, String filter) throws SQLException {

        Connection connection = MySQLHelper.getInstance().getConnection();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(ProzesseID) FROM prozesse");
        if (filter != null && !filter.isEmpty()) {
            sql.append(" WHERE " + filter);
        }
        try {
            logger.debug(sql.toString());
            if (filter != null && !filter.isEmpty()) {
                return new QueryRunner().query(connection, sql.toString(), MySQLUtils.resultSetToIntegerHandler);
            } else {
                return new QueryRunner().query(connection, sql.toString(), MySQLUtils.resultSetToIntegerHandler);
            }
        } finally {
            MySQLHelper.closeConnection(connection);
        }
    }

    public static List<Process> getProcesses(String order, String filter, Integer start, Integer count) throws SQLException {
        Connection connection = MySQLHelper.getInstance().getConnection();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM prozesse");
        if (filter != null && !filter.isEmpty()) {
            sql.append(" WHERE " + filter);
        }
        if (order != null && !order.isEmpty()) {
            sql.append(" ORDER BY " + order);
        }
        if (start != null && count != null) {
            sql.append(" LIMIT " + start + ", " + count);
        }
        try {
            logger.debug(sql.toString());
            List<Process> ret = null;
            if (filter != null && !filter.isEmpty()) {
                ret = new QueryRunner().query(connection, sql.toString(), resultSetToProcessListHandler);
            } else {
                ret = new QueryRunner().query(connection, sql.toString(), resultSetToProcessListHandler);
            }
            return ret;
        } finally {
            MySQLHelper.closeConnection(connection);
        }
    }

    public static List<Process> getAllProcesses() throws SQLException {
        Connection connection = MySQLHelper.getInstance().getConnection();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM prozesse");
        try {
            logger.debug(sql.toString());
            List<Process> ret = new QueryRunner().query(connection, sql.toString(), resultSetToProcessListHandler);
            return ret;
        } finally {
            MySQLHelper.closeConnection(connection);
        }
    }

    public static ResultSetHandler<List<Process>> resultSetToProcessListHandler = new ResultSetHandler<List<Process>>() {
        public List<Process> handle(ResultSet rs) throws SQLException {
            List<Process> answer = new ArrayList<Process>();

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
            return answer;
        }
    };

    public static ResultSetHandler<Process> resultSetToProcessHandler = new ResultSetHandler<Process>() {
        public Process handle(ResultSet rs) throws SQLException {
            if (rs.next()) {
                try {
                    Process o = convert(rs);
                    return o;
                } catch (DAOException e) {
                    logger.error(e);
                }
            }
            return null;
        }
    };

    private static void insertProcess(Process o) throws SQLException {
        String sql = "INSERT INTO prozesse " + generateInsertQuery(false) + generateValueQuery(false);
        Object[] param = generateParameter(o, false, false);
        Connection connection = MySQLHelper.getInstance().getConnection();
        try {
            QueryRunner run = new QueryRunner();
            run.update(connection, sql, param);
        } finally {
            MySQLHelper.closeConnection(connection);
        }
    }

    private static String generateInsertQuery(boolean includeProcessId) {
        if (!includeProcessId) {
            return "(Titel, ausgabename, IstTemplate, swappedOut, inAuswahllisteAnzeigen, sortHelperStatus,"
                    + "sortHelperImages, sortHelperArticles, erstellungsdatum, ProjekteID, MetadatenKonfigurationID, sortHelperDocstructs,"
                    + "sortHelperMetadata, wikifield, batchID, docketID)" + " VALUES ";
        } else {
            return "(ProzesseID, Titel, ausgabename, IstTemplate, swappedOut, inAuswahllisteAnzeigen, sortHelperStatus,"
                    + "sortHelperImages, sortHelperArticles, erstellungsdatum, ProjekteID, MetadatenKonfigurationID, sortHelperDocstructs,"
                    + "sortHelperMetadata, wikifield, batchID, docketID)" + " VALUES ";
        }
    }

    private static String generateValueQuery(boolean includeProcessId) {
        if (!includeProcessId) {
            return "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        } else {
            return "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        }
    }

    private static Object[] generateParameter(Process o, boolean createNewTimestamp, boolean includeProcessID) {
        Date d = null;
        if (createNewTimestamp) {
            d = new Date();
        } else {
            d = o.getErstellungsdatum();
        }

        Timestamp datetime = new Timestamp(d.getTime());
        if (!includeProcessID) {
            Object[] param =
                    { o.getTitel(), o.getAusgabename(), o.isIstTemplate(), o.isSwappedOutHibernate(), o.isInAuswahllisteAnzeigen(),
                            o.getSortHelperStatus(), o.getSortHelperImages(), o.getSortHelperArticles(), datetime, o.getProjectId(),
                            o.getRegelsatz().getId(), o.getSortHelperDocstructs(), o.getSortHelperMetadata(),
                            o.getWikifield().equals("") ? " " : o.getWikifield(), o.getBatchID(),
                            o.getDocket() == null ? null : o.getDocket().getId() };

            return param;
        } else {
            Object[] param =
                    { o.getId(), o.getTitel(), o.getAusgabename(), o.isIstTemplate(), o.isSwappedOutHibernate(), o.isInAuswahllisteAnzeigen(),
                            o.getSortHelperStatus(), o.getSortHelperImages(), o.getSortHelperArticles(), datetime, o.getProjectId(),
                            o.getRegelsatz().getId(), o.getSortHelperDocstructs(), o.getSortHelperMetadata(),
                            o.getWikifield().equals("") ? " " : o.getWikifield(), o.getBatchID(),
                            o.getDocket() == null ? null : o.getDocket().getId() };

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
        sql.append(" wikifield = ?,");
        sql.append(" batchID = ?,");
        sql.append(" docketID = ?");
        sql.append(" WHERE ProzesseID = " + o.getId());

        Object[] param = generateParameter(o, false, false);

        Connection connection = MySQLHelper.getInstance().getConnection();
        try {
            QueryRunner run = new QueryRunner();
            run.update(connection, sql.toString(), param);
        } finally {
            MySQLHelper.closeConnection(connection);
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
        p.setErstellungsdatum(rs.getDate("erstellungsdatum"));
        p.setProjectId(rs.getInt("ProjekteID"));
        p.setRegelsatz(RulesetManager.getRulesetById(rs.getInt("MetadatenKonfigurationID")));
        p.setSortHelperDocstructs(rs.getInt("sortHelperDocstructs"));
        p.setSortHelperMetadata(rs.getInt("sortHelperMetadata"));
        p.setWikifield(rs.getString("wikifield"));
        p.setBatchID(rs.getInt("batchID"));
        p.setDocket(DocketManager.getDocketById(rs.getInt("docketID")));

        return p;
    }

    public static void insertBatchProcessList(List<Process> processList) throws SQLException {

        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO prozesse " + generateInsertQuery(false));
        List<Object[]> paramArray = new ArrayList<Object[]>();
        for (Process o : processList) {
            sql.append(" " + generateValueQuery(false) + ",");
            Object[] param = generateParameter(o, false, false);
            paramArray.add(param);
        }
        String values = sql.toString();

        values = values.substring(0, values.length() - 1);

        Connection connection = MySQLHelper.getInstance().getConnection();
        try {
            QueryRunner run = new QueryRunner();
            run.update(connection, values, paramArray);
        } finally {
            MySQLHelper.closeConnection(connection);
        }
    }

    public static void updateBatchList(List<Process> processList) throws SQLException {

        String tablename = "a" + String.valueOf(new Date().getTime());
        String tempTable = "CREATE TEMPORARY TABLE IF NOT EXISTS " + tablename + " LIKE prozesse;";

        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO " + tablename + " " + generateInsertQuery(true));
        List<Object[]> paramList = new ArrayList<Object[]>();
        for (Process o : processList) {
            sql.append(" " + generateValueQuery(true) + ",");
            Object[] param = generateParameter(o, true, true);
            paramList.add(param);
        }
        Object[][] paramArray = new Object[paramList.size()][];
        paramList.toArray(paramArray);

        String insertQuery = sql.toString();
        insertQuery = insertQuery.substring(0, insertQuery.length() - 1);

        StringBuilder joinQuery = new StringBuilder();
        joinQuery.append("UPDATE prozesse SET prozesse.Titel = " + tablename + ".Titel, prozesse.ausgabename = " + tablename + ".ausgabename,"
                + "prozesse.IstTemplate = " + tablename + ".IstTemplate, " + "prozesse.swappedOut = " + tablename + ".swappedOut, "
                + "prozesse.inAuswahllisteAnzeigen = " + tablename + ".inAuswahllisteAnzeigen, " + "prozesse.sortHelperStatus = " + tablename
                + ".sortHelperStatus, " + "prozesse.sortHelperImages = " + tablename + ".sortHelperImages, " + "prozesse.sortHelperArticles = "
                + tablename + ".sortHelperArticles, " + "prozesse.erstellungsdatum = " + tablename + ".erstellungsdatum, " + "prozesse.ProjekteID = "
                + tablename + ".ProjekteID, " + "prozesse.MetadatenKonfigurationID = " + tablename + ".MetadatenKonfigurationID, "
                + "prozesse.sortHelperDocstructs = " + tablename + ".sortHelperDocstructs, " + "prozesse.sortHelperMetadata = " + tablename
                + ".sortHelperMetadata, " + "prozesse.wikifield = " + tablename + ".wikifield, " + "prozesse.batchID = " + tablename + ".batchID, "
                + "prozesse.docketID = " + tablename + ".docketID " + " WHERE prozesse.ProzesseID = " + tablename + ".ProzesseID;");

        String deleteTempTable = "DROP TEMPORARY TABLE " + tablename + ";";

        Connection connection = MySQLHelper.getInstance().getConnection();
        try {
            QueryRunner run = new QueryRunner();
            // create temporary table
            run.update(connection, tempTable);
            // insert bulk into a temp table
            run.batch(connection, insertQuery, paramArray);
            // update process table using join
            run.update(connection, joinQuery.toString());
            // delete temporary table
            run.update(connection, deleteTempTable);
        } finally {
            MySQLHelper.closeConnection(connection);
        }
    }

    public static void main(String[] args) throws SQLException {
        // born digital
        Process p1 = ProcessMysqlHelper.getProcessById(28);
        // MOH
        Process p2 = ProcessMysqlHelper.getProcessById(31);

        List<Process> pl = new ArrayList<Process>();
        pl.add(p1);
        pl.add(p2);
        ProcessMysqlHelper.updateBatchList(pl);
        //        updateBatchList

    }

    public static int getMaxBatchNumber() throws SQLException {
        Connection connection = MySQLHelper.getInstance().getConnection();
        String sql = "SELECT max(batchId) FROM prozesse";
        try {
            return new QueryRunner().query(connection, sql, MySQLUtils.resultSetToIntegerHandler);
        } finally {
            MySQLHelper.closeConnection(connection);
        }
    }

    public static List<Integer> getIDList(String filter) throws SQLException {
        Connection connection = MySQLHelper.getInstance().getConnection();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT prozesseID FROM prozesse");
        if (filter != null && !filter.isEmpty()) {
            sql.append(" WHERE " + filter);
        }

        try {
            logger.debug(sql.toString());
            List<Integer> ret = null;
            ret = new QueryRunner().query(connection, sql.toString(), MySQLUtils.resultSetToIntegerListHandler);

            return ret;
        } finally {
            MySQLHelper.closeConnection(connection);
        }

    }

    public static int countProcesses(String filter) throws SQLException {
        String sql = "select count(prozesseID) from prozesse ";
        if (filter != null && filter.length() > 0) {
            sql += " WHERE " + filter;
        }
        Connection connection = MySQLHelper.getInstance().getConnection();
        try {
            return new QueryRunner().query(connection, sql, MySQLUtils.resultSetToIntegerHandler);
        } finally {
            MySQLHelper.closeConnection(connection);
        }
    }
    
    public static List<Integer> getBatchIds(int limit) throws SQLException {
        String sql = "select distinct batchID from Prozess order by batchID desc ";
        if (limit > 0) {
            sql += " limit " + limit;
        }
        Connection connection = MySQLHelper.getInstance().getConnection();
        try {
            return new QueryRunner().query(connection, sql, MySQLUtils.resultSetToIntegerListHandler);
        } finally {
            MySQLHelper.closeConnection(connection);
        }
    }

    public static List runSQL(String sql) throws SQLException {
        Connection connection = MySQLHelper.getInstance().getConnection();
        List answer = new ArrayList();
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = stmt.executeQuery(sql);
            int columnCount = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                Object[] row = new Object[columnCount];
                for(int i = 1;i<=columnCount;i++){
                    row[i-1]=rs.getString(i);
                }
                answer.add(row);
            }
            return answer;

        } finally {
            MySQLHelper.closeConnection(connection);
        }
    }
    
}
