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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang3.StringUtils;
import org.goobi.api.mq.QueueType;
import org.goobi.beans.ErrorProperty;
import org.goobi.beans.GoobiProperty;
import org.goobi.beans.GoobiProperty.PropertyOwnerType;
import org.goobi.beans.Institution;
import org.goobi.beans.Step;
import org.goobi.beans.User;
import org.goobi.beans.Usergroup;
import org.goobi.production.cli.helper.StringPair;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.enums.PropertyType;
import de.sub.goobi.helper.enums.StepEditType;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.persistence.managers.MySQLHelper.SQLTYPE;
import lombok.extern.log4j.Log4j2;

@Log4j2
class StepMysqlHelper implements Serializable {

    private static final long serialVersionUID = -2064912552692963L;

    public static List<Step> getStepsForProcess(int processId) throws SQLException {
        Connection connection = null;
        String sql = "SELECT * FROM schritte WHERE schritte.ProzesseID = ? order by Reihenfolge";
        Object[] param = { processId };
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (log.isTraceEnabled()) {
                log.trace(sql + ", " + Arrays.toString(param));
            }
            return new QueryRunner().query(connection, sql, resultSetToStepListHandler, param);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static int getAllStepsCount() throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT count(1) FROM schritte");
        if (MySQLHelper.getInstance().getSqlType() == SQLTYPE.MYSQL) {
            sql.append(" WHERE schritteid > 0 ");
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

    public static int getStepCount(String filter, Institution institution) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(1) FROM schritte ");
        if (StringUtils.isNotBlank(ConfigurationHelper.getInstance().getSqlTasksIndexname()) && filter.contains("Bearbeitungsstatus")) {
            sql.append("use index (" + ConfigurationHelper.getInstance().getSqlTasksIndexname() + ") ");
        }
        sql.append("LEFT JOIN prozesse ON schritte.prozesseId = prozesse.ProzesseID ");
        sql.append("LEFT JOIN batches ON prozesse.batchID = batches.id ");
        sql.append("LEFT JOIN projekte on prozesse.ProjekteID = projekte.ProjekteID ");

        if (institution != null) {
            sql.append("and projekte.institution_id = ");
            sql.append(institution.getId());
        }

        if (filter != null && !filter.isEmpty()) {
            sql.append(" WHERE " + filter);
        } else if (MySQLHelper.getInstance().getSqlType() == SQLTYPE.MYSQL) {
            sql.append(" WHERE schritte.SchritteID > 0 ");
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

    public static List<Step> getSteps(String order, String filter, Integer start, Integer count, Institution institution) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT schritte.* ");
        sql.append(" FROM schritte ");
        if (StringUtils.isNotBlank(ConfigurationHelper.getInstance().getSqlTasksIndexname()) && filter.contains("Bearbeitungsstatus")) {
            sql.append("use index (" + ConfigurationHelper.getInstance().getSqlTasksIndexname() + ") ");
        }
        sql.append("LEFT JOIN prozesse ON schritte.prozesseId = prozesse.ProzesseID ");
        sql.append("LEFT JOIN batches ON prozesse.batchID = batches.id ");
        sql.append("LEFT JOIN projekte on prozesse.ProjekteID = projekte.ProjekteID ");

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
            List<Step> ret = new QueryRunner().query(connection, sql.toString(), resultSetToStepListHandler);
            checkBatchStatus(ret);
            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static final ResultSetHandler<List<Step>> resultSetToStepListHandler = new ResultSetHandler<>() {

        @Override
        public List<Step> handle(ResultSet rs) throws SQLException {
            List<Step> answer = new ArrayList<>();
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

    public static final ResultSetHandler<List<Step>> resultSetIdsToStepListHandler = new ResultSetHandler<>() {

        @Override
        public List<Step> handle(ResultSet rs) throws SQLException {
            List<Step> answer = new ArrayList<>();
            try {
                while (rs.next()) {
                    Integer id = rs.getInt("schritteID");
                    answer.add(getStepById(id));
                }
            } finally {
                rs.close();
            }
            return answer;
        }

    };

    private static void checkBatchStatus(List<Step> stepList) {
        List<StringPair> batchChecks = new ArrayList<>();
        for (Step step : stepList) {
            if (Boolean.TRUE.equals(step.isBatchStep()) && step.getProzess().getBatch() != null) {
                String stepTitle = step.getTitel();
                String batchNumber = "" + step.getProzess().getBatch().getBatchId();
                boolean found = false;
                for (StringPair sp : batchChecks) {
                    if (sp.getOne().equals(stepTitle) && sp.getTwo().equals(batchNumber)) {
                        found = true;
                    }
                }
                if (!found) {
                    batchChecks.add(new StringPair(stepTitle, batchNumber));
                }
            }
        }
        if (!batchChecks.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT ");
            sb.append("COUNT(SchritteID), schritte.titel, prozesse.batchID ");
            sb.append("FROM  ");
            sb.append("schritte ");
            if (StringUtils.isNotBlank(ConfigurationHelper.getInstance().getSqlTasksIndexname())) {
                sb.append("use index (" + ConfigurationHelper.getInstance().getSqlTasksIndexname() + ") ");
            }
            sb.append("LEFT JOIN ");
            sb.append("prozesse ON schritte.prozesseId = prozesse.ProzesseID ");
            sb.append("LEFT JOIN ");
            sb.append("batches ON prozesse.batchID = batches.id ");
            sb.append("WHERE ");
            StringBuilder whereClause = new StringBuilder();
            for (StringPair sp : batchChecks) {
                if (whereClause.length() > 0) {
                    whereClause.append(" OR ");
                }
                whereClause.append("(schritte.titel = '");
                whereClause.append(sp.getOne());
                whereClause.append("' AND batches.id = ");
                whereClause.append(sp.getTwo());
                whereClause.append(") ");
            }
            sb.append(whereClause.toString());
            sb.append("group by prozesse.batchID, schritte.titel ");
            List<Object[]> rawData = ControllingManager.getResultsAsObjectList(sb.toString());
            for (Object[] row : rawData) {
                int numberOfProcesses = Integer.parseInt((String) row[0]);
                String taskTitle = (String) row[1];
                int batchNumber = Integer.parseInt((String) row[2]);
                if (numberOfProcesses > 1) {
                    for (Step step : stepList) {
                        if (step.getTitel().equals(taskTitle) && step.getProzess().getBatch() != null
                                && step.getProzess().getBatch().getBatchId().intValue() == batchNumber) {
                            step.setBatchSize(true);
                        }
                    }
                }
            }
        }
    }

    public static final ResultSetHandler<Step> resultSetToStepHandler = new ResultSetHandler<>() {
        @Override
        public Step handle(ResultSet rs) throws SQLException {
            try {
                if (rs.next()) {
                    try {
                        return convert(rs);
                    } catch (SQLException e) {
                        log.error(e);
                    }
                }
            } finally {
                rs.close();
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
        Timestamp time = rs.getTimestamp("BearbeitungsZeitpunkt");
        if (time != null) {
            s.setBearbeitungszeitpunkt(new java.util.Date(time.getTime()));
        }
        Timestamp start = rs.getTimestamp("BearbeitungsBeginn");
        if (start != null) {
            s.setBearbeitungsbeginn(new java.util.Date(start.getTime()));
        }
        Timestamp end = rs.getTimestamp("BearbeitungsEnde");
        if (end != null) {
            s.setBearbeitungsende(new java.util.Date(end.getTime()));
        }
        s.setHomeverzeichnisNutzen(rs.getShort("homeverzeichnisNutzen"));
        s.setTypMetadaten(rs.getBoolean("typMetadaten"));
        s.setTypAutomatisch(rs.getBoolean("typAutomatisch"));
        s.setTypAutomaticThumbnail(rs.getBoolean("typAutomaticThumbnail"));
        s.setAutomaticThumbnailSettingsYaml(rs.getString("automaticThumbnailSettingsYaml"));
        s.setTypImportFileUpload(rs.getBoolean("typImportFileUpload"));
        s.setTypExportRus(rs.getBoolean("typExportRus"));
        s.setTypImagesLesen(rs.getBoolean("typImagesLesen"));
        s.setTypImagesSchreiben(rs.getBoolean("typImagesSchreiben"));
        s.setTypExportDMS(rs.getBoolean("typExportDMS"));
        s.setTypBeimAnnehmenModul(rs.getBoolean("typBeimAnnehmenModul"));
        s.setTypBeimAnnehmenAbschliessen(rs.getBoolean("typBeimAnnehmenAbschliessen"));
        s.setTypBeimAnnehmenModulUndAbschliessen(rs.getBoolean("typBeimAnnehmenModulUndAbschliessen"));
        String script = rs.getString("typAutomatischScriptpfad");
        if (script != null) {
            script = script.trim();
        }
        s.setTypAutomatischScriptpfad(script);
        s.setTypBeimAbschliessenVerifizieren(rs.getBoolean("typBeimAbschliessenVerifizieren"));
        s.setTypModulName(rs.getString("typModulName"));
        s.setTypBeimAnnehmenModul(rs.getBoolean("typBeimAnnehmenModul"));
        s.setTypBeimAnnehmenModul(rs.getBoolean("typBeimAnnehmenModul"));
        s.setTypBeimAnnehmenModul(rs.getBoolean("typBeimAnnehmenModul"));
        s.setUserId(rs.getInt("BearbeitungsBenutzerID"));
        if (rs.wasNull()) {
            s.setUserId(null);
        }
        s.setProcessId(rs.getInt("ProzesseID"));
        s.setEditTypeEnum(StepEditType.getTypeFromValue(rs.getInt("edittype")));
        s.setTypScriptStep(rs.getBoolean("typScriptStep"));
        s.setScriptname1(rs.getString("scriptName1"));
        s.setScriptname2(rs.getString("scriptName2"));

        script = rs.getString("typAutomatischScriptpfad2");
        if (script != null) {
            script = script.trim();
        }
        s.setTypAutomatischScriptpfad2(script);
        s.setScriptname3(rs.getString("scriptName3"));
        script = rs.getString("typAutomatischScriptpfad3");
        if (script != null) {
            script = script.trim();
        }
        s.setTypAutomatischScriptpfad3(script);
        s.setScriptname4(rs.getString("scriptName4"));
        script = rs.getString("typAutomatischScriptpfad4");
        if (script != null) {
            script = script.trim();
        }
        s.setTypAutomatischScriptpfad4(script);
        s.setScriptname5(rs.getString("scriptName5"));
        script = rs.getString("typAutomatischScriptpfad5");
        if (script != null) {
            script = script.trim();
        }
        s.setTypAutomatischScriptpfad5(script);
        s.setBatchStep(rs.getBoolean("batchStep"));
        s.setStepPlugin(rs.getString("stepPlugin"));
        s.setValidationPlugin(rs.getString("validationPlugin"));
        s.setDelayStep(rs.getBoolean("delayStep"));
        s.setUpdateMetadataIndex(rs.getBoolean("updateMetadataIndex"));
        s.setGenerateDocket(rs.getBoolean("generateDocket"));

        s.setHttpStep(rs.getBoolean("httpStep"));
        s.setHttpMethod(rs.getString("httpMethod"));
        s.setHttpUrl(rs.getString("httpUrl"));
        s.setHttpJsonBody(rs.getString("httpJsonBody"));
        s.setHttpCloseStep(rs.getBoolean("httpCloseStep"));
        s.setHttpEscapeBodyJson(rs.getBoolean("httpEscapeBodyJson"));
        s.setMessageQueue(QueueType.getByName(rs.getString("messageQueue")));
        // load error properties
        List<GoobiProperty> stepList = getErrorPropertiesForStep(s.getId());
        if (!stepList.isEmpty()) {
            for (GoobiProperty property : stepList) {
                property.setOwner(s);
            }
            s.setEigenschaften(stepList);
        }
        return s;
    }

    public static final ResultSetHandler<List<ErrorProperty>> resultSetToErrorPropertyListHandler = new ResultSetHandler<>() {

        @Override
        public List<ErrorProperty> handle(ResultSet rs) throws SQLException {
            List<ErrorProperty> properties = new ArrayList<>();
            try {
                while (rs.next()) { // implies that rs != null, while the case rs == null will be thrown as an Exception
                    int id = rs.getInt("id");
                    String title = rs.getString("property_name");
                    String value = rs.getString("property_value");
                    Boolean mandatory = rs.getBoolean("required");
                    int type = rs.getInt("datatype");
                    Timestamp time = rs.getTimestamp("creation_date");
                    Date creationDate = null;
                    if (time != null) {
                        creationDate = new Date(time.getTime());
                    }
                    String container = rs.getString("container");
                    ErrorProperty ve = new ErrorProperty();
                    ve.setId(id);
                    ve.setPropertyName(title);
                    ve.setPropertyValue(value);
                    ve.setRequired(mandatory);
                    ve.setType(PropertyType.getById(type));
                    ve.setCreationDate(creationDate);
                    ve.setContainer(container);
                    properties.add(ve);
                }
            } finally {
                rs.close();
            }
            return properties;
        }
    };

    public static final ResultSetHandler<Boolean> checkForResultHandler = new ResultSetHandler<>() {

        @Override
        public Boolean handle(ResultSet rs) throws SQLException {
            try {
                if (rs.next()) {
                    return true;
                }
            } finally {
                rs.close();
            }
            return false;
        }
    };

    public static Step getStepById(int id) throws SQLException {
        Connection connection = null;
        String sql = "SELECT * FROM schritte WHERE SchritteID = ?";
        Object[] param = { id };
        try {
            connection = MySQLHelper.getInstance().getConnection();
            return new QueryRunner().query(connection, sql, resultSetToStepHandler, param);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void deleteStep(Step o) throws SQLException {
        if (o.getId() != null) {
            for (GoobiProperty property : o.getEigenschaften()) {
                PropertyMysqlHelper.deleteProperty(property);
            }

            String schritteberechtigtebenutzer = "DELETE FROM schritteberechtigtebenutzer WHERE schritteID = ?";
            String schritteberechtigtegruppen = "DELETE FROM schritteberechtigtegruppen WHERE schritteID = ?";
            String schritte = "DELETE FROM schritte WHERE SchritteID = ?";

            Object[] param = { o.getId() };
            Connection connection = null;
            try {
                connection = MySQLHelper.getInstance().getConnection();
                QueryRunner run = new QueryRunner();
                run.update(connection, schritteberechtigtebenutzer, param);
                run.update(connection, schritteberechtigtegruppen, param);
                run.update(connection, schritte, param);
            } finally {
                if (connection != null) {
                    MySQLHelper.closeConnection(connection);
                }
            }
        }

    }

    public static List<Step> getAllSteps() throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM schritte");
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (log.isTraceEnabled()) {
                log.trace(sql.toString());
            }
            return new QueryRunner().query(connection, sql.toString(), resultSetToStepListHandler);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void saveStep(Step o) throws SQLException {

        if (StringUtils.isNotBlank(o.getTitel())) {
            o.setTitel(o.getTitel().trim());
        }

        if (o.getId() == null) {
            // new process
            insertStep(o);
        } else {
            // process exists already in database
            updateStep(o);
        }

        if (o.getEigenschaftenSize() > 0) {
            for (GoobiProperty property : o.getEigenschaften()) {
                PropertyMysqlHelper.saveProperty(property);
            }
        }

        if (o.getBenutzerSize() > 0) {
            saveUserAssignment(o);
        }
        if (o.getBenutzergruppenSize() > 0) {
            saveUserGroupAssignment(o);
        }

    }

    private static void saveErrorProperty(ErrorProperty property) throws SQLException {
        if (property.getId() == null) {
            PropertyMysqlHelper.insertProperty(property);
        } else {
            PropertyMysqlHelper.updateProperty(property);
        }
    }

    private static List<GoobiProperty> getErrorPropertiesForStep(int stepId) throws SQLException {
        return PropertyMysqlHelper.getPropertiesForObject(stepId, PropertyOwnerType.ERROR);
    }

    private static void deleteErrorProperty(ErrorProperty property) throws SQLException {
        PropertyMysqlHelper.deleteProperty(property);
    }

    private static void insertStep(Step o) throws SQLException {
        String sql = "INSERT INTO schritte " + generateInsertQuery(false) + generateValueQuery(false);
        Object[] param = generateParameter(o, false);
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

    private static Object[] generateParameter(Step o, boolean includeID) {
        if (o.getProcessId() == null && o.getProzess() != null) {
            o.setProcessId(o.getProzess().getId());
        }

        if (o.getBearbeitungsbenutzer() != null) {
            o.setUserId(o.getBearbeitungsbenutzer().getId());
        }

        if (includeID) {
            return new Object[] { o.getId(), //SchritteID
                    o.getTitel(), //Titel
                    o.getPrioritaet(), //Prioritaet
                    o.getReihenfolge(), //Reihenfolge
                    o.getBearbeitungsstatusAsString(), //Bearbeitungsstatus
                    o.getBearbeitungszeitpunkt() == null ? null : new Timestamp(o.getBearbeitungszeitpunkt().getTime()), // BearbeitungsZeitpunkt
                    o.getBearbeitungsbeginn() == null ? null : new Timestamp(o.getBearbeitungsbeginn().getTime()), // BearbeitungsBeginn
                    o.getBearbeitungsende() == null ? null : new Timestamp(o.getBearbeitungsende().getTime()), // BearbeitungsEnde
                    o.getHomeverzeichnisNutzen(), // homeverzeichnisNutzen
                    o.isTypMetadaten(), // typMetadaten
                    o.isTypAutomatisch(), // typAutomatisch
                    o.isTypAutomaticThumbnail(), // typAutomaticThumbnail
                    o.getAutomaticThumbnailSettingsYaml(), //automaticThumbnailSettingsYaml
                    o.isTypImportFileUpload(), // typImportFileUpload
                    o.isTypExportRus(), //typExportRus
                    o.isTypImagesLesen(), //typImagesLesen
                    o.isTypImagesSchreiben(), // typImagesSchreiben
                    o.isTypExportDMS(), // typExportDMS
                    o.isTypBeimAnnehmenModul(), // typBeimAnnehmenModul
                    o.isTypBeimAnnehmenAbschliessen(), // typBeimAnnehmenAbschliessen
                    o.isTypBeimAnnehmenModulUndAbschliessen(), // typBeimAnnehmenModulUndAbschliessen
                    (o.getTypAutomatischScriptpfad() == null || "".equals(o.getTypAutomatischScriptpfad())) ? null : o.getTypAutomatischScriptpfad(), // typAutomatischScriptpfad
                    o.isTypBeimAbschliessenVerifizieren(), // typBeimAbschliessenVerifizieren
                    (o.getTypModulName() == null || "".equals(o.getTypModulName())) ? null : o.getTypModulName(), // typModulName
                    o.getUserId() == null ? null : o.getUserId(), //BearbeitungsBenutzerID
                    o.getProcessId() == null ? null : o.getProcessId(), //ProzesseID
                    o.getEditTypeEnum().getValue(), //edittype
                    o.isTypScriptStep(), //typScriptStep
                    (o.getScriptname1() == null || "".equals(o.getScriptname1())) ? null : o.getScriptname1(), //scriptName1
                    (o.getScriptname2() == null || "".equals(o.getScriptname2())) ? null : o.getScriptname2(), //scriptName2
                    (o.getTypAutomatischScriptpfad2() == null || "".equals(o.getTypAutomatischScriptpfad2())) ? null
                            : o.getTypAutomatischScriptpfad2(), //typAutomatischScriptpfad2
                    (o.getScriptname3() == null || "".equals(o.getScriptname3())) ? null : o.getScriptname3(), //scriptName3
                    (o.getTypAutomatischScriptpfad3() == null || "".equals(o.getTypAutomatischScriptpfad3())) ? null
                            : o.getTypAutomatischScriptpfad3(), //typAutomatischScriptpfad3
                    (o.getScriptname4() == null || "".equals(o.getScriptname4())) ? null : o.getScriptname4(), //scriptName4
                    (o.getTypAutomatischScriptpfad4() == null || "".equals(o.getTypAutomatischScriptpfad4())) ? null
                            : o.getTypAutomatischScriptpfad4(), //typAutomatischScriptpfad4
                    (o.getScriptname5() == null || "".equals(o.getScriptname5())) ? null : o.getScriptname5(), //scriptName5
                    (o.getTypAutomatischScriptpfad5() == null || "".equals(o.getTypAutomatischScriptpfad5())) ? null
                            : o.getTypAutomatischScriptpfad5(), //typAutomatischScriptpfad5
                    o.getBatchStep(), //batchStep
                    (o.getStepPlugin() == null || "".equals(o.getStepPlugin())) ? null : o.getStepPlugin(), // stepPlugin
                    (o.getValidationPlugin() == null || "".equals(o.getValidationPlugin())) ? null : o.getValidationPlugin(), //validationPlugin
                    (o.isDelayStep()), (o.isUpdateMetadataIndex()), o.isGenerateDocket(), o.isHttpStep(), o.getHttpMethod(), o.getHttpUrl(),
                    o.getHttpJsonBody(), o.isHttpCloseStep(), o.isHttpEscapeBodyJson(), o.getMessageQueue().toString() }; //httpStep
        } else {
            return new Object[] { o.getTitel(), //Titel
                    o.getPrioritaet(), //Prioritaet
                    o.getReihenfolge(), //Reihenfolge
                    o.getBearbeitungsstatusAsString(), //Bearbeitungsstatus
                    o.getBearbeitungszeitpunkt() == null ? null : new Timestamp(o.getBearbeitungszeitpunkt().getTime()), // BearbeitungsZeitpunkt
                    o.getBearbeitungsbeginn() == null ? null : new Timestamp(o.getBearbeitungsbeginn().getTime()), // BearbeitungsBeginn
                    o.getBearbeitungsende() == null ? null : new Timestamp(o.getBearbeitungsende().getTime()), // BearbeitungsEnde
                    o.getHomeverzeichnisNutzen(), // homeverzeichnisNutzen
                    o.isTypMetadaten(), // typMetadaten
                    o.isTypAutomatisch(), // typAutomatisch
                    o.isTypAutomaticThumbnail(), // typAutomaticThumbnail
                    o.getAutomaticThumbnailSettingsYaml(), //automaticThumbnailSettingsYaml
                    o.isTypImportFileUpload(), // typImportFileUpload
                    o.isTypExportRus(), //typExportRus
                    o.isTypImagesLesen(), //typImagesLesen
                    o.isTypImagesSchreiben(), // typImagesSchreiben
                    o.isTypExportDMS(), // typExportDMS
                    o.isTypBeimAnnehmenModul(), // typBeimAnnehmenModul
                    o.isTypBeimAnnehmenAbschliessen(), // typBeimAnnehmenAbschliessen
                    o.isTypBeimAnnehmenModulUndAbschliessen(), // typBeimAnnehmenModulUndAbschliessen
                    (o.getTypAutomatischScriptpfad() == null || "".equals(o.getTypAutomatischScriptpfad())) ? null : o.getTypAutomatischScriptpfad(), // typAutomatischScriptpfad
                    o.isTypBeimAbschliessenVerifizieren(), // typBeimAbschliessenVerifizieren
                    (o.getTypModulName() == null || "".equals(o.getTypModulName())) ? null : o.getTypModulName(), // typModulName
                    o.getUserId() == null ? null : o.getUserId(), //BearbeitungsBenutzerID
                    o.getProcessId() == null ? null : o.getProcessId(), //ProzesseID
                    o.getEditTypeEnum().getValue(), //edittype
                    o.isTypScriptStep(), //typScriptStep
                    (o.getScriptname1() == null || "".equals(o.getScriptname1())) ? null : o.getScriptname1(), //scriptName1
                    (o.getScriptname2() == null || "".equals(o.getScriptname2())) ? null : o.getScriptname2(), //scriptName2
                    (o.getTypAutomatischScriptpfad2() == null || "".equals(o.getTypAutomatischScriptpfad2())) ? null
                            : o.getTypAutomatischScriptpfad2(), //typAutomatischScriptpfad2
                    (o.getScriptname3() == null || "".equals(o.getScriptname3())) ? null : o.getScriptname3(), //scriptName3
                    (o.getTypAutomatischScriptpfad3() == null || "".equals(o.getTypAutomatischScriptpfad3())) ? null
                            : o.getTypAutomatischScriptpfad3(), //typAutomatischScriptpfad3
                    (o.getScriptname4() == null || "".equals(o.getScriptname4())) ? null : o.getScriptname4(), //scriptName4
                    (o.getTypAutomatischScriptpfad4() == null || "".equals(o.getTypAutomatischScriptpfad4())) ? null
                            : o.getTypAutomatischScriptpfad4(), //typAutomatischScriptpfad4
                    (o.getScriptname5() == null || "".equals(o.getScriptname5())) ? null : o.getScriptname5(), //scriptName5
                    (o.getTypAutomatischScriptpfad5() == null || "".equals(o.getTypAutomatischScriptpfad5())) ? null
                            : o.getTypAutomatischScriptpfad5(), //typAutomatischScriptpfad5
                    o.getBatchStep(), //batchStep
                    (o.getStepPlugin() == null || "".equals(o.getStepPlugin())) ? null : o.getStepPlugin(), // stepPlugin
                    (o.getValidationPlugin() == null || "".equals(o.getValidationPlugin())) ? null : o.getValidationPlugin(), //validationPlugin
                    (o.isDelayStep()), (o.isUpdateMetadataIndex()), o.isGenerateDocket(), o.isHttpStep(), o.getHttpMethod(), o.getHttpUrl(),
                    o.getHttpJsonBody(), o.isHttpCloseStep(), o.isHttpEscapeBodyJson(),
                    o.getMessageQueue() == null ? QueueType.NONE.toString() : o.getMessageQueue().toString() }; //httpStep
        }
    }

    private static String generateValueQuery(boolean includeID) {
        if (!includeID) {
            return "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        } else {
            return "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        }

    }

    private static String generateInsertQuery(boolean includeID) {
        StringBuilder answer = new StringBuilder("(");
        if (includeID) {
            answer.append(" SchritteID, ");
        }
        answer.append("Titel, Prioritaet, Reihenfolge, Bearbeitungsstatus, BearbeitungsZeitpunkt, BearbeitungsBeginn, BearbeitungsEnde, ")
                .append("homeverzeichnisNutzen, typMetadaten, typAutomatisch, typAutomaticThumbnail, automaticThumbnailSettingsYaml, typImportFileUpload, typExportRus, typImagesLesen, typImagesSchreiben, ")
                .append("typExportDMS, typBeimAnnehmenModul, typBeimAnnehmenAbschliessen, typBeimAnnehmenModulUndAbschliessen, typAutomatischScriptpfad, ")
                .append("typBeimAbschliessenVerifizieren, typModulName, BearbeitungsBenutzerID, ProzesseID, edittype, typScriptStep, scriptName1, ")
                .append("scriptName2, typAutomatischScriptpfad2, scriptName3, typAutomatischScriptpfad3, scriptName4, typAutomatischScriptpfad4, ")
                .append("scriptName5, typAutomatischScriptpfad5, batchStep, stepPlugin, validationPlugin, delayStep, updateMetadataIndex, generateDocket,")
                .append("httpStep, httpMethod, httpUrl, httpJsonBody, httpCloseStep, httpEscapeBodyJson, messageQueue)")
                .append(" VALUES ");
        return answer.toString();
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
        sql.append(" typAutomaticThumbnail = ?,");
        sql.append(" automaticThumbnailSettingsYaml = ?,");
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
        sql.append(" validationPlugin = ?,");
        sql.append(" delayStep = ?,");
        sql.append(" updateMetadataIndex = ?, ");
        sql.append(" generateDocket = ?, ");
        sql.append(" httpStep = ?, ");
        sql.append(" httpMethod = ?, ");
        sql.append(" httpUrl = ?, ");
        sql.append(" httpJsonBody = ?, ");
        sql.append(" httpCloseStep = ?, ");
        sql.append(" httpEscapeBodyJson = ?, ");
        sql.append(" messageQueue = ? ");
        sql.append(" WHERE SchritteID = " + o.getId());

        Object[] param = generateParameter(o, false);

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

    public static void updateBatchList(List<Step> stepList) throws SQLException {
        String tablename = "a" + new Date().getTime();
        String tempTable = "CREATE TEMPORARY TABLE IF NOT EXISTS " + tablename + " LIKE schritte;";

        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO " + tablename + " " + generateInsertQuery(true));
        List<Object[]> paramList = new ArrayList<>();
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
        joinQuery.append(" typAutomaticThumbnail = " + tablename + ".typAutomaticThumbnail,");
        joinQuery.append(" automaticThumbnailSettingsYaml = " + tablename + ".automaticThumbnailSettingsYaml");
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
        joinQuery.append(" validationPlugin = " + tablename + ".validationPlugin, ");
        joinQuery.append(" delayStep = " + tablename + ".delayStep");

        joinQuery.append(" WHERE schritte.SchritteID= " + tablename + ".SchritteID;");

        String deleteTempTable = "DROP TEMPORARY TABLE " + tablename + ";";

        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
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
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void insertBatchStepList(List<Step> stepList) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO schritte " + generateInsertQuery(false));
        List<Object[]> paramArray = new ArrayList<>();
        for (Step o : stepList) {
            sql.append(" " + generateValueQuery(false) + ",");
            Object[] param = generateParameter(o, false);
            paramArray.add(param);
        }
        String values = sql.toString();

        values = values.substring(0, values.length() - 1);

        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            run.update(connection, values, paramArray);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void main(String[] args) throws SQLException {

        Step s76 = StepMysqlHelper.getStepById(76);
        Step s77 = StepMysqlHelper.getStepById(77);
        Step s78 = StepMysqlHelper.getStepById(78);
        Step s79 = StepMysqlHelper.getStepById(79);
        Step s42338 = StepMysqlHelper.getStepById(42338);
        Step s83 = StepMysqlHelper.getStepById(83);
        Step s85 = StepMysqlHelper.getStepById(85);
        Step s61351 = StepMysqlHelper.getStepById(61351);
        Step s335310 = StepMysqlHelper.getStepById(335310);
        Step s84 = StepMysqlHelper.getStepById(84);
        Step s216 = StepMysqlHelper.getStepById(216);
        Step s217 = StepMysqlHelper.getStepById(217);
        Step s316611 = StepMysqlHelper.getStepById(316611);
        Step s345846 = StepMysqlHelper.getStepById(345846);

        List<Step> stepList = new ArrayList<>();
        stepList.add(s76);
        stepList.add(s77);
        stepList.add(s78);
        stepList.add(s79);
        stepList.add(s42338);
        stepList.add(s83);
        stepList.add(s85);
        stepList.add(s61351);
        stepList.add(s335310);
        stepList.add(s84);
        stepList.add(s216);
        stepList.add(s217);
        stepList.add(s316611);
        stepList.add(s345846);

        StepMysqlHelper.updateBatchList(stepList);

    }

    public static List<Integer> getIDList(String filter) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT SchritteId FROM schritte");
        if (filter != null && !filter.isEmpty()) {
            sql.append(" WHERE " + filter);
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

    public static List<String> getDistinctStepTitlesAndOrder(String order, String filter) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("select distinct schritte.titel, schritte.reihenfolge from schritte, prozesse WHERE schritte.ProzesseID = prozesse.ProzesseID ");
        if (filter != null && !filter.isEmpty()) {
            sql.append(" AND " + filter);
        }

        if (order != null && !order.isEmpty()) {
            sql.append(" ORDER BY " + order);
        }
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (log.isTraceEnabled()) {
                log.trace(sql.toString());
            }
            return new QueryRunner().query(connection, sql.toString(), MySQLHelper.resultSetToStringListHandler);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static List<String> getDistinctStepTitles(String order, String filter) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("select distinct schritte.titel from schritte");
        if (filter != null && !filter.isEmpty()) {
            sql.append(" WHERE " + filter);
        }

        if (order != null && !order.isEmpty()) {
            sql.append(" ORDER BY " + order);
        }
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (log.isTraceEnabled()) {
                log.trace(sql.toString());
            }
            return new QueryRunner().query(connection, sql.toString(), MySQLHelper.resultSetToStringListHandler);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static Set<String> getDistinctStepPluginTitles() throws SQLException {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("select distinct stepPlugin from schritte");
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (log.isTraceEnabled()) {
                log.trace(sqlBuilder.toString());
            }
            return new HashSet<>(new QueryRunner().query(connection, sqlBuilder.toString(), MySQLHelper.resultSetToStringListHandler));
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void saveUserAssignment(Step step) throws SQLException {
        if (step.getId() != null) {
            Connection connection = null;
            try {
                connection = MySQLHelper.getInstance().getConnection();
                for (User user : step.getBenutzer()) {
                    // check if assignment exists
                    String sql = " SELECT * from schritteberechtigtebenutzer WHERE BenutzerID =" + user.getId() + " AND schritteID = " + step.getId();
                    boolean exists = new QueryRunner().query(connection, sql, checkForResultHandler);
                    if (!exists) {
                        String insert = " INSERT INTO schritteberechtigtebenutzer (BenutzerID , schritteID) VALUES (" + user.getId() + ","
                                + step.getId() + ")";
                        new QueryRunner().update(connection, insert);
                    }
                }
            } finally {
                if (connection != null) {
                    MySQLHelper.closeConnection(connection);
                }
            }
        }
    }

    private static void saveUserGroupAssignment(Step step) throws SQLException {
        if (step.getId() != null) {
            Connection connection = null;
            try {
                connection = MySQLHelper.getInstance().getConnection();
                for (Usergroup userGroup : step.getBenutzergruppen()) {
                    // check if assignment exists
                    String sql = " SELECT * from schritteberechtigtegruppen WHERE BenutzerGruppenID =" + userGroup.getId() + " AND schritteID = "
                            + step.getId();
                    boolean exists = new QueryRunner().query(connection, sql, checkForResultHandler);
                    if (!exists) {
                        String insert = " INSERT INTO schritteberechtigtegruppen (BenutzerGruppenID , schritteID) VALUES (" + userGroup.getId() + ","
                                + step.getId() + ")";
                        new QueryRunner().update(connection, insert);
                    }
                }
            } finally {
                if (connection != null) {
                    MySQLHelper.closeConnection(connection);
                }
            }
        }

    }

    public static void removeUsergroupFromStep(Step step, Usergroup usergroup) throws SQLException {
        if (step.getId() != null) {
            Connection connection = null;
            try {
                connection = MySQLHelper.getInstance().getConnection();
                String sql =
                        "DELETE FROM schritteberechtigtegruppen WHERE BenutzerGruppenID =" + usergroup.getId() + " AND schritteID = " + step.getId();

                new QueryRunner().update(connection, sql);
            } finally {
                if (connection != null) {
                    MySQLHelper.closeConnection(connection);
                }
            }
        }
    }

    public static void removeUserFromStep(Step step, User user) throws SQLException {
        if (step.getId() != null) {
            Connection connection = null;
            try {
                connection = MySQLHelper.getInstance().getConnection();
                String sql = "DELETE FROM schritteberechtigtebenutzer WHERE BenutzerID =" + user.getId() + " AND schritteID = " + step.getId();

                new QueryRunner().update(connection, sql);
            } finally {
                if (connection != null) {
                    MySQLHelper.closeConnection(connection);
                }
            }
        }
    }

    public static void removeUserFromAllSteps(User user) throws SQLException {
        if (user.getId() != null) {
            Connection connection = null;
            try {
                connection = MySQLHelper.getInstance().getConnection();
                String sql = "DELETE FROM schritteberechtigtebenutzer WHERE BenutzerID = " + user.getId();

                new QueryRunner().update(connection, sql);
            } finally {
                if (connection != null) {
                    MySQLHelper.closeConnection(connection);
                }
            }
        }
    }

    public static List<String> getScriptsForStep(int stepId) throws SQLException {
        Connection connection = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM schritte WHERE SchritteID = ? ");
        try {
            connection = MySQLHelper.getInstance().getConnection();
            Object[] params = { stepId };
            if (log.isTraceEnabled()) {
                log.trace(sql.toString() + ", " + stepId);
            }
            return new QueryRunner().query(connection, sql.toString(), resultSetToScriptsHandler, params);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static final ResultSetHandler<List<String>> resultSetToScriptsHandler = new ResultSetHandler<>() {
        @Override
        public List<String> handle(ResultSet rs) throws SQLException {
            List<String> answer = new ArrayList<>();
            try {
                if (rs.next()) {
                    if (rs.getString("typAutomatischScriptpfad") != null && rs.getString("typAutomatischScriptpfad").length() > 0) {
                        answer.add(rs.getString("typAutomatischScriptpfad"));
                    }
                    if (rs.getString("typAutomatischScriptpfad2") != null && rs.getString("typAutomatischScriptpfad2").length() > 0) {
                        answer.add(rs.getString("typAutomatischScriptpfad2"));
                    }
                    if (rs.getString("typAutomatischScriptpfad3") != null && rs.getString("typAutomatischScriptpfad3").length() > 0) {
                        answer.add(rs.getString("typAutomatischScriptpfad3"));
                    }
                    if (rs.getString("typAutomatischScriptpfad4") != null && rs.getString("typAutomatischScriptpfad4").length() > 0) {
                        answer.add(rs.getString("typAutomatischScriptpfad4"));
                    }
                    if (rs.getString("typAutomatischScriptpfad5") != null && rs.getString("typAutomatischScriptpfad5").length() > 0) {
                        answer.add(rs.getString("typAutomatischScriptpfad5"));
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

    public static long getSumOfFieldValue(String columnname, String filter, String order, String group) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("select sum(" + columnname + ") from schritte, prozesse WHERE schritte.ProzesseID = prozesse.ProzesseID  ");
        if (filter != null && filter.length() > 0) {
            sql.append(" AND " + filter);
        }

        if (group != null && !group.isEmpty()) {
            sql.append(" GROUP BY " + group);
        }

        if (order != null && !order.isEmpty()) {
            sql.append(" ORDER BY " + order);
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

    public static long getCountOfFieldValue(String columnname, String filter, String order, String group) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("select count(" + columnname + ") from schritte, prozesse WHERE schritte.ProzesseID = prozesse.ProzesseID  ");
        if (filter != null && filter.length() > 0) {
            sql.append(" AND " + filter);
        }

        if (group != null && !group.isEmpty()) {
            sql.append(" GROUP BY " + group);
        }

        if (order != null && !order.isEmpty()) {
            sql.append(" ORDER BY " + order);
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

    public static double getAverageOfFieldValue(String columnname, String filter, String order, String group) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("select avg(" + columnname + ") from schritte, prozesse WHERE schritte.ProzesseID = prozesse.ProzesseID ");
        if (filter != null && filter.length() > 0) {
            sql.append(" AND " + filter);
        }

        if (group != null && !group.isEmpty()) {
            sql.append(" GROUP BY " + group);
        }

        if (order != null && !order.isEmpty()) {
            sql.append(" ORDER BY " + order);
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

    public static void saveJobTypes(String jobTypesJson) throws SQLException {
        try (Connection connection = MySQLHelper.getInstance().getConnection()) {
            String sql = "UPDATE jobTypes SET jobTypes = ?";
            QueryRunner run = new QueryRunner();
            run.update(connection, sql, jobTypesJson);
        }
    }

    public static String getJobTypes() throws SQLException {
        try (Connection connection = MySQLHelper.getInstance().getConnection()) {
            String sql = "SELECT jobTypes FROM jobTypes LIMIT 1";
            QueryRunner run = new QueryRunner();
            return run.query(connection, sql, rs -> {
                if (rs.next()) {
                    return rs.getString(1);
                }
                return "[]";
            });
        }
    }

    public static void setStepPaused(int stepId, boolean paused) throws SQLException {
        try (Connection connection = MySQLHelper.getInstance().getConnection()) {
            String sql = "UPDATE schritte SET paused=? WHERE schritteid = ?";
            QueryRunner run = new QueryRunner();
            run.update(connection, sql, paused, stepId);
        }
    }

    public static List<Step> getPausedSteps(List<String> restartStepnames) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM schritte WHERE paused=1 AND titel in (");
        for (int i = 0; i < restartStepnames.size(); i++) {
            if (i < restartStepnames.size() - 1) {
                sql.append("?,");
            } else {
                sql.append("?)");
            }
        }
        try (Connection connection = MySQLHelper.getInstance().getConnection()) {
            QueryRunner run = new QueryRunner();
            return run.query(connection, sql.toString(), resultSetToStepListHandler, restartStepnames.toArray());
        }
    }

    public static List<Step> getUserSchritte(User user) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM schritteberechtigtebenutzer WHERE BenutzerID=" + user.getId());

        try (Connection connection = MySQLHelper.getInstance().getConnection()) {
            QueryRunner run = new QueryRunner();
            return run.query(connection, sql.toString(), resultSetIdsToStepListHandler);
        }
    }

    /**
     * Delete a list of steps in a single statement
     * 
     * @param steps
     * @throws SQLException
     */

    public static void deleteAllSteps(List<Step> steps) throws SQLException {
        // step list is empty
        if (steps == null || steps.isEmpty()) {
            return;
        }
        StringBuilder ids = new StringBuilder();
        // collect all step ids
        for (Step step : steps) {
            if (step.getId() != null) {
                if (ids.length() > 0) {
                    ids.append(", ");
                }
                ids.append(step.getId());
            }
        }

        // steps have no ids (steps are new, and not saved yet)
        if (ids.length() == 0) {
            return;
        }

        // delete error properties
        String deleteProperties = "DELETE FROM properties WHERE object_type = 'error' AND object_id in (" + ids.toString() + ")";
        // delete assigned users
        String deleteUserAssignment = "DELETE FROM schritteberechtigtebenutzer WHERE schritteID in (" + ids.toString() + ")";
        // delete assigned user groups
        String deleteGroupAssignment = "DELETE FROM schritteberechtigtegruppen WHERE schritteID in (" + ids.toString() + ")";
        // delete steps itself
        String deleteSteps = "DELETE FROM schritte WHERE SchritteID in (" + ids.toString() + ")";

        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            run.update(connection, deleteProperties);
            run.update(connection, deleteUserAssignment);
            run.update(connection, deleteGroupAssignment);
            run.update(connection, deleteSteps);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }

    }

}
