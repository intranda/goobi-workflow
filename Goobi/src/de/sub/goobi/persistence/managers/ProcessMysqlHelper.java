package de.sub.goobi.persistence.managers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;
import org.goobi.beans.Process;

import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.apache.MySQLHelper;
import de.sub.goobi.persistence.apache.MySQLUtils;

public class ProcessMysqlHelper {
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

    public static void saveProcess(Process o) throws DAOException {
        try {

            if (o.getId() == null) {
                // new process
                insertProcess(o);
            } else {
                // process exists already in database
                updateProcess(o);
            }
            // TODO Schritte speichern
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
            String sql = "DELETE FROM processe WHERE ProzesseID = ?";
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
            return new QueryRunner().query(connection, sql.toString(), MySQLUtils.resultSetToIntegerHandler);
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
            List<Process> ret = new QueryRunner().query(connection, sql.toString(), resultSetToProjectListHandler);
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
            List<Process> ret = new QueryRunner().query(connection, sql.toString(), resultSetToProjectListHandler);
            return ret;
        } finally {
            MySQLHelper.closeConnection(connection);
        }
    }

    public static ResultSetHandler<List<Process>> resultSetToProjectListHandler = new ResultSetHandler<List<Process>>() {
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
        String sql = "INSERT INTO processe " + generateInsertQuery();
        Object[] param = generateParameter(o, true);
        Connection connection = MySQLHelper.getInstance().getConnection();
        try {
            QueryRunner run = new QueryRunner();
            run.update(connection, sql, param);
        } finally {
            MySQLHelper.closeConnection(connection);
        }
    }

    private static String generateInsertQuery() {
        return "(Titel, ausgabename, IstTemplate, swappedOut, inAuswahllisteAnzeigen, sortHelperStatus,"
                + "sortHelperImages, sortHelperArticles, erstellungsdatum, ProjekteID, MetadatenKonfigurationID, sortHelperDocstructs,"
                + "sortHelperMetadata, wikifield, batchID, docketID)" + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    private static Object[] generateParameter(Process o, boolean createNewTimestamp) {
        Date d = null;
        if (createNewTimestamp) {
            d = new Date();
        } else {
            d = o.getErstellungsdatum();
        }

        Timestamp datetime = new Timestamp(d.getTime());
        Object[] param =
                { o.getTitel(), o.getAusgabename(), o.isIstTemplate(), o.isSwappedOutHibernate(), o.isInAuswahllisteAnzeigen(), o.getSortHelperStatus(),
                        o.getSortHelperImages(), o.getSortHelperArticles(), datetime, o.getProjectId(), o.getRegelsatz().getId(),
                        o.getSortHelperDocstructs(), o.getSortHelperMetadata(), o.getWikifield(), o.getBatchID(), o.getDocket().getId() };

        return param;
    }

    private static void updateProcess(Process o) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE prozesse SET ");
        sql.append("Titel = ?");
        sql.append("ausgabename = ?");
        sql.append("IstTemplate = ?");
        sql.append("swappedOut = ?");
        sql.append("inAuswahllisteAnzeigen = ?");
        sql.append("sortHelperStatus = ?");
        sql.append("sortHelperImages = ?");
        sql.append("sortHelperArticles = ?");
        sql.append("erstellungsdatum = ?");
        sql.append("ProjekteID = ?");
        sql.append("MetadatenKonfigurationID = ?");
        sql.append("sortHelperDocstructs = ?");
        sql.append("sortHelperMetadata = ?");
        sql.append("wikifield = ?");
        sql.append("batchID = ?");
        sql.append("docketID = ?");
        sql.append(" WHERE ProzesseID = " + o.getId());

        Object[] param = generateParameter(o, false);

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

 

}
