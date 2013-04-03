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
import org.goobi.beans.Step;

import de.sub.goobi.helper.enums.StepEditType;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.persistence.apache.MySQLHelper;
import de.sub.goobi.persistence.apache.MySQLUtils;

public class StepMysqlHelper {
    private static final Logger logger = Logger.getLogger(StepMysqlHelper.class);

    public static List<Step> getStepsForProcess(int processId) throws SQLException {
        Connection connection = MySQLHelper.getInstance().getConnection();
        String sql = "SELECT * FROM schritte WHERE ProzesseID = ? order by Reihenfolge";
        Object[] param = { processId };
        try {
            List<Step> list = new QueryRunner().query(connection, sql, resultSetToStepListHandler, param);
            return list;
        } finally {
            MySQLHelper.closeConnection(connection);
        }
    }

    public static int getStepCount(String order, String filter) throws SQLException {

        Connection connection = MySQLHelper.getInstance().getConnection();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(SchritteID) FROM schritte");
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

    public static List<Step> getSteps(String order, String filter, Integer start, Integer count) throws SQLException {
        Connection connection = MySQLHelper.getInstance().getConnection();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM schritte");
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
            List<Step> ret = null;
            if (filter != null && !filter.isEmpty()) {
                ret = new QueryRunner().query(connection, sql.toString(), resultSetToStepListHandler);
            } else {
                ret = new QueryRunner().query(connection, sql.toString(), resultSetToStepListHandler);
            }
            return ret;
        } finally {
            MySQLHelper.closeConnection(connection);
        }
    }

    public static ResultSetHandler<List<Step>> resultSetToStepListHandler = new ResultSetHandler<List<Step>>() {

        @Override
        public List<Step> handle(ResultSet rs) throws SQLException {
            List<Step> answer = new ArrayList<Step>();
            while (rs.next()) {
                answer.add(convert(rs));
            }
            return answer;
        }

    };

    public static ResultSetHandler<Step> resultSetToStepHandler = new ResultSetHandler<Step>() {
        public Step handle(ResultSet rs) throws SQLException {
            if (rs.next()) {
                try {
                    Step o = convert(rs);
                    return o;
                } catch (SQLException e) {
                    logger.error(e);
                }
            }
            return null;
        }
    };

    private static Step convert(ResultSet rs) throws SQLException {
        Step s = new Step();
        s.setId(rs.getInt("SchritteID"));
        s.setTitel(rs.getString("Titel"));
        s.setPrioritaet(rs.getInt("Prioritaet"));
        s.setReihenfolge(rs.getInt("Reihenfolge"));
        s.setBearbeitungsstatusEnum(StepStatus.getStatusFromValue(rs.getInt("Bearbeitungsstatus")));
        s.setBearbeitungszeitpunkt(rs.getDate("BearbeitungsZeitpunkt"));
        s.setBearbeitungsbeginn(rs.getDate("BearbeitungsBeginn"));
        s.setBearbeitungsende(rs.getDate("BearbeitungsEnde"));
        s.setHomeverzeichnisNutzen(rs.getShort("homeverzeichnisNutzen"));
        s.setTypMetadaten(rs.getBoolean("typMetadaten"));
        s.setTypAutomatisch(rs.getBoolean("typAutomatisch"));
        s.setTypImportFileUpload(rs.getBoolean("typImportFileUpload"));
        s.setTypExportRus(rs.getBoolean("typExportRus"));
        s.setTypImagesLesen(rs.getBoolean("typImagesLesen"));
        s.setTypImagesSchreiben(rs.getBoolean("typImagesSchreiben"));
        s.setTypExportDMS(rs.getBoolean("typExportDMS"));
        s.setTypBeimAnnehmenModul(rs.getBoolean("typBeimAnnehmenModul"));
        s.setTypBeimAnnehmenAbschliessen(rs.getBoolean("typBeimAnnehmenAbschliessen"));
        s.setTypBeimAnnehmenModulUndAbschliessen(rs.getBoolean("typBeimAnnehmenModulUndAbschliessen"));
        s.setTypAutomatischScriptpfad(rs.getString("typAutomatischScriptpfad"));
        s.setTypBeimAbschliessenVerifizieren(rs.getBoolean("typBeimAbschliessenVerifizieren"));
        s.setTypModulName(rs.getString("typModulName"));
        s.setTypBeimAnnehmenModul(rs.getBoolean("typBeimAnnehmenModul"));
        s.setTypBeimAnnehmenModul(rs.getBoolean("typBeimAnnehmenModul"));
        s.setTypBeimAnnehmenModul(rs.getBoolean("typBeimAnnehmenModul"));
        s.setUserId(rs.getInt("BearbeitungsBenutzerID"));
        s.setProcessId(rs.getInt("ProzesseID"));
        s.setEditTypeEnum(StepEditType.getTypeFromValue(rs.getInt("edittype")));
        s.setTypScriptStep(rs.getBoolean("typScriptStep"));
        s.setScriptname1(rs.getString("scriptName1"));
        s.setScriptname2(rs.getString("scriptName2"));
        s.setTypAutomatischScriptpfad2(rs.getString("typAutomatischScriptpfad2"));
        s.setScriptname3(rs.getString("scriptName3"));
        s.setTypAutomatischScriptpfad3(rs.getString("typAutomatischScriptpfad3"));
        s.setScriptname4(rs.getString("scriptName4"));
        s.setTypAutomatischScriptpfad4(rs.getString("typAutomatischScriptpfad4"));
        s.setScriptname5(rs.getString("scriptName5"));
        s.setTypAutomatischScriptpfad5(rs.getString("typAutomatischScriptpfad5"));
        s.setBatchStep(rs.getBoolean("batchStep"));
        s.setStepPlugin(rs.getString("stepPlugin"));
        s.setValidationPlugin(rs.getString("validationPlugin"));
        return s;
    }

    public static Step getStepById(int id) throws SQLException {
        Connection connection = MySQLHelper.getInstance().getConnection();
        String sql = "SELECT * FROM schritte WHERE SchritteID = ?";
        Object[] param = { id };
        try {
            Step s = new QueryRunner().query(connection, sql, resultSetToStepHandler, param);
            return s;
        } finally {
            MySQLHelper.closeConnection(connection);
        }
    }

    public static void deleteStep(Step o) throws SQLException {
        if (o.getId() != null) {
            String sql = "DELETE FROM schritte WHERE SchritteID = ?";
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

    public static List<Step> getAllSteps() throws SQLException {
        Connection connection = MySQLHelper.getInstance().getConnection();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM schritte");
        try {
            logger.debug(sql.toString());
            List<Step> ret = new QueryRunner().query(connection, sql.toString(), resultSetToStepListHandler);
            return ret;
        } finally {
            MySQLHelper.closeConnection(connection);
        }
    }

    public static void saveStep(Step o) throws SQLException {

        if (o.getId() == null) {
            // new process
            insertStep(o);
        } else {
            // process exists already in database
            updateStep(o);
        }

    }

    private static void insertStep(Step o) throws SQLException {
        String sql = "INSERT INTO schritte " + generateInsertQuery(false) + generateValueQuery(false);
        Object[] param = generateParameter(o, false);
        Connection connection = MySQLHelper.getInstance().getConnection();
        try {
            QueryRunner run = new QueryRunner();
            run.update(connection, sql, param);
        } finally {
            MySQLHelper.closeConnection(connection);
        }
    }

    private static Object[] generateParameter(Step o, boolean includeID) {
        if (includeID) {
            Object[] param =
                    { o.getId(), o.getTitel(), o.getPrioritaet(), o.getReihenfolge(), o.getBearbeitungsstatusAsString(),
                            o.getBearbeitungszeitpunkt() == null ? null : new Timestamp(o.getBearbeitungszeitpunkt().getTime()),
                            o.getBearbeitungsbeginn() == null ? null : new Timestamp(o.getBearbeitungsbeginn().getTime()),
                            o.getBearbeitungsende() == null ? null : new Timestamp(o.getBearbeitungsende().getTime()), o.getHomeverzeichnisNutzen(),
                            o.isTypMetadaten(), o.isTypAutomatisch(), o.isTypImportFileUpload(), o.isTypExportRus(), o.isTypImagesLesen(),
                            o.isTypImagesSchreiben(), o.isTypExportDMS(), o.isTypBeimAnnehmenModul(), o.isTypBeimAnnehmenAbschliessen(),
                            o.isTypBeimAnnehmenModulUndAbschliessen(), o.getTypAutomatischScriptpfad(), o.isTypBeimAbschliessenVerifizieren(),
                            o.getTypModulName(), o.getBearbeitungsbenutzer() == null ? null : o.getBearbeitungsbenutzer().getId(),
                            o.getProzess() == null ? null : o.getProzess().getId(), o.getEditTypeEnum().getValue(), o.getTypScriptStep(),
                            o.getScriptname1(), o.getScriptname2(), o.getTypAutomatischScriptpfad2(), o.getScriptname3(),
                            o.getTypAutomatischScriptpfad3(), o.getScriptname4(), o.getTypAutomatischScriptpfad4(), o.getScriptname5(),
                            o.getTypAutomatischScriptpfad5(), o.getBatchStep(), o.getStepPlugin(), o.getValidationPlugin() };
            return param;
        } else {
            Object[] param =
                    { o.getTitel(), o.getPrioritaet(), o.getReihenfolge(), o.getBearbeitungsstatusAsString(),
                            o.getBearbeitungszeitpunkt() == null ? null : new Timestamp(o.getBearbeitungszeitpunkt().getTime()),
                            o.getBearbeitungsbeginn() == null ? null : new Timestamp(o.getBearbeitungsbeginn().getTime()),
                            o.getBearbeitungsende() == null ? null : new Timestamp(o.getBearbeitungsende().getTime()), o.getHomeverzeichnisNutzen(),
                            o.isTypMetadaten(), o.isTypAutomatisch(), o.isTypImportFileUpload(), o.isTypExportRus(), o.isTypImagesLesen(),
                            o.isTypImagesSchreiben(), o.isTypExportDMS(), o.isTypBeimAnnehmenModul(), o.isTypBeimAnnehmenAbschliessen(),
                            o.isTypBeimAnnehmenModulUndAbschliessen(), o.getTypAutomatischScriptpfad(), o.isTypBeimAbschliessenVerifizieren(),
                            o.getTypModulName(), o.getBearbeitungsbenutzer() == null ? null : o.getBearbeitungsbenutzer().getId(),
                            o.getProzess() == null ? null : o.getProzess().getId(), o.getEditTypeEnum().getValue(), o.getTypScriptStep(),
                            o.getScriptname1(), o.getScriptname2(), o.getTypAutomatischScriptpfad2(), o.getScriptname3(),
                            o.getTypAutomatischScriptpfad3(), o.getScriptname4(), o.getTypAutomatischScriptpfad4(), o.getScriptname5(),
                            o.getTypAutomatischScriptpfad5(), o.getBatchStep(), o.getStepPlugin(), o.getValidationPlugin() };
            return param;
        }
    }

    private static String generateValueQuery(boolean includeID) {
        if (!includeID) {
            return "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?, ?, ?)";
        } else {
            return "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?, ?, ?)";
        }

    }

    private static String generateInsertQuery(boolean includeID) {
        String answer = "(";
        if (includeID) {
            answer += " SchritteID, ";
        }
        answer +=
                "Titel, Prioritaet, Reihenfolge, Bearbeitungsstatus, BearbeitungsZeitpunkt, BearbeitungsBeginn, BearbeitungsEnde, "
                        + "homeverzeichnisNutzen, typMetadaten, typAutomatisch, typImportFileUpload, typExportRus, typImagesLesen, typImagesSchreiben, "
                        + "typExportDMS, typBeimAnnehmenModul, typBeimAnnehmenAbschliessen, typBeimAnnehmenModulUndAbschliessen, typAutomatischScriptpfad, "
                        + "typBeimAbschliessenVerifizieren, typModulName, BearbeitungsBenutzerID, ProzesseID, edittype, typScriptStep, scriptName1, "
                        + "scriptName2, typAutomatischScriptpfad2, scriptName3, typAutomatischScriptpfad3, scriptName4, typAutomatischScriptpfad4, "
                        + "scriptName5, typAutomatischScriptpfad5, batchStep, stepPlugin, validationPlugin)" + " VALUES ";
        return answer;
    }

    private static void updateStep(Step o) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE schritte SET ");
        sql.append(" Titel = ?,");
        sql.append(" Prioritaet = ?,");
        sql.append(" Reihenfolge = ?,");
        sql.append(" Bearbeitungsstatus = ?,");
        sql.append(" BearbeitungsZeitpunkt = ?,");
        sql.append(" BearbeitungsBeginn = ?,");
        sql.append(" BearbeitungsEnde = ?,");
        sql.append(" homeverzeichnisNutzen = ?,");
        sql.append(" typMetadaten = ?,");
        sql.append(" typAutomatisch = ?,");
        sql.append(" typImportFileUpload = ?,");
        sql.append(" typExportRus = ?,");
        sql.append(" typImagesLesen = ?,");
        sql.append(" typImagesSchreiben = ?,");
        sql.append(" typExportDMS = ?,");
        sql.append(" typBeimAnnehmenModul = ?,");
        sql.append(" typBeimAnnehmenAbschliessen = ?,");
        sql.append(" typBeimAnnehmenModulUndAbschliessen = ?,");
        sql.append(" typAutomatischScriptpfad = ?,");
        sql.append(" typBeimAbschliessenVerifizieren = ?,");
        sql.append(" typModulName = ?,");
        sql.append(" BearbeitungsBenutzerID = ?,");
        sql.append(" ProzesseID = ?,");
        sql.append(" edittype = ?,");
        sql.append(" typScriptStep = ?,");
        sql.append(" scriptName1 = ?,");
        sql.append(" scriptName2 = ?,");
        sql.append(" typAutomatischScriptpfad2 = ?,");
        sql.append(" scriptName3 = ?,");
        sql.append(" typAutomatischScriptpfad3 = ?,");
        sql.append(" scriptName4 = ?,");
        sql.append(" typAutomatischScriptpfad4 = ?,");
        sql.append(" scriptName5 = ?,");
        sql.append(" typAutomatischScriptpfad5 = ?,");
        sql.append(" batchStep = ?,");
        sql.append(" stepPlugin = ?,");
        sql.append(" validationPlugin = ?");
        sql.append(" WHERE SchritteID = " + o.getId());

        Object[] param = generateParameter(o, false);

        Connection connection = MySQLHelper.getInstance().getConnection();
        try {
            QueryRunner run = new QueryRunner();
            run.update(connection, sql.toString(), param);
        } finally {
            MySQLHelper.closeConnection(connection);
        }

    }

    public static void updateBatchList(List<Step> stepList) throws SQLException {
        String tablename = "a" + String.valueOf(new Date().getTime());
        String tempTable = "CREATE TEMPORARY TABLE IF NOT EXISTS " + tablename + " LIKE schritte;";

        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO " + tablename + " " + generateInsertQuery(true));
        List<Object[]> paramList = new ArrayList<Object[]>();
        for (Step o : stepList) {
            sql.append(" " + generateValueQuery(true) + ",");
            Object[] param = generateParameter(o, true);
            paramList.add(param);
        }
        Object[][] paramArray = new Object[paramList.size()][];
        paramList.toArray(paramArray);

        String insertQuery = sql.toString();
        insertQuery = insertQuery.substring(0, insertQuery.length() - 1);

        StringBuilder joinQuery = new StringBuilder();

        joinQuery.append("UPDATE schritte SET schritte.Titel = " + tablename + ".Titel, ");
        joinQuery.append(" Prioritaet = " + tablename + ".Prioritaet,");
        joinQuery.append(" Reihenfolge = " + tablename + ".Reihenfolge,");
        joinQuery.append(" Bearbeitungsstatus = " + tablename + ".Bearbeitungsstatus,");
        joinQuery.append(" BearbeitungsZeitpunkt = " + tablename + ".BearbeitungsZeitpunkt,");
        joinQuery.append(" BearbeitungsBeginn = " + tablename + ".BearbeitungsBeginn,");
        joinQuery.append(" BearbeitungsEnde = " + tablename + ".BearbeitungsEnde,");
        joinQuery.append(" homeverzeichnisNutzen = " + tablename + ".homeverzeichnisNutzen,");
        joinQuery.append(" typMetadaten = " + tablename + ".typMetadaten,");
        joinQuery.append(" typAutomatisch = " + tablename + ".typAutomatisch,");
        joinQuery.append(" typImportFileUpload = " + tablename + ".typImportFileUpload,");
        joinQuery.append(" typExportRus = " + tablename + ".typExportRus,");
        joinQuery.append(" typImagesLesen = " + tablename + ".typImagesLesen,");
        joinQuery.append(" typImagesSchreiben = " + tablename + ".typImagesSchreiben,");
        joinQuery.append(" typExportDMS = " + tablename + ".typExportDMS,");
        joinQuery.append(" typBeimAnnehmenModul = " + tablename + ".typBeimAnnehmenModul,");
        joinQuery.append(" typBeimAnnehmenAbschliessen = " + tablename + ".typBeimAnnehmenAbschliessen,");
        joinQuery.append(" typBeimAnnehmenModulUndAbschliessen = " + tablename + ".typBeimAnnehmenModulUndAbschliessen,");
        joinQuery.append(" typAutomatischScriptpfad = " + tablename + ".typAutomatischScriptpfad,");
        joinQuery.append(" typBeimAbschliessenVerifizieren = " + tablename + ".typBeimAbschliessenVerifizieren,");
        joinQuery.append(" typModulName = " + tablename + ".typModulName,");
        joinQuery.append(" BearbeitungsBenutzerID = " + tablename + ".BearbeitungsBenutzerID,");
        joinQuery.append(" ProzesseID = " + tablename + ".ProzesseID,");
        joinQuery.append(" edittype = " + tablename + ".edittype,");
        joinQuery.append(" typScriptStep = " + tablename + ".typScriptStep,");
        joinQuery.append(" scriptName1 = " + tablename + ".scriptName1,");
        joinQuery.append(" scriptName2 = " + tablename + ".scriptName2,");
        joinQuery.append(" typAutomatischScriptpfad2 = " + tablename + ".typAutomatischScriptpfad2,");
        joinQuery.append(" scriptName3 = " + tablename + ".scriptName3,");
        joinQuery.append(" typAutomatischScriptpfad3 = " + tablename + ".typAutomatischScriptpfad3,");
        joinQuery.append(" scriptName4 = " + tablename + ".scriptName4,");
        joinQuery.append(" typAutomatischScriptpfad4 = " + tablename + ".typAutomatischScriptpfad4,");
        joinQuery.append(" scriptName5 = " + tablename + ".scriptName5,");
        joinQuery.append(" typAutomatischScriptpfad5 = " + tablename + ".typAutomatischScriptpfad5,");
        joinQuery.append(" batchStep = " + tablename + ".batchStep,");
        joinQuery.append(" stepPlugin = " + tablename + ".stepPlugin,");
        joinQuery.append(" validationPlugin = " + tablename + ".validationPlugin");
        joinQuery.append(" WHERE prozesse. SchritteID= " + tablename + ".SchritteID;");

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

    public static void insertBatchStepList(List<Step> stepList) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO schritte " + generateInsertQuery(false));
        List<Object[]> paramArray = new ArrayList<Object[]>();
        for (Step o : stepList) {
            sql.append(" " + generateValueQuery(false) + ",");
            Object[] param = generateParameter(o, false);
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

}
