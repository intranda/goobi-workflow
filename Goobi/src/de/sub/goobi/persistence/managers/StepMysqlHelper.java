package de.sub.goobi.persistence.managers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;


import de.sub.goobi.beans.Schritt;
import de.sub.goobi.helper.enums.StepEditType;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.persistence.apache.MySQLHelper;

public class StepMysqlHelper {
    private static final Logger logger = Logger.getLogger(StepMysqlHelper.class);

    public static List<Schritt> getStepsForProcess(int processId) throws SQLException {
        Connection connection = MySQLHelper.getInstance().getConnection();
        String sql = "SELECT * FROM schritte WHERE ProzesseID = ? order by Reihenfolge";
        Object[] param = { processId };
        try {
            List<Schritt> list = new QueryRunner().query(connection, sql, resultSetToStepListHandler, param);
            return list;
        } finally {
            MySQLHelper.closeConnection(connection);
        }
    }

    public static ResultSetHandler<List<Schritt>> resultSetToStepListHandler = new ResultSetHandler<List<Schritt>>() {

        @Override
        public List<Schritt> handle(ResultSet rs) throws SQLException {
            List<Schritt> answer = new ArrayList<Schritt>();
            while (rs.next()) {
                answer.add(convert(rs));
            }
            return answer;
        }

    };

    private static Schritt convert(ResultSet rs) throws SQLException {
        Schritt s = new Schritt();
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
}
