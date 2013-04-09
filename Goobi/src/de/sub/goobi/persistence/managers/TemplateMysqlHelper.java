package de.sub.goobi.persistence.managers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

import de.sub.goobi.beans.Vorlage;
import de.sub.goobi.beans.Vorlageeigenschaft;
import de.sub.goobi.persistence.apache.MySQLHelper;
import de.sub.goobi.persistence.apache.MySQLUtils;

class TemplateMysqlHelper {

    public static List<Vorlage> getTemplatesForProcess(int processId) throws SQLException {
        Connection connection = MySQLHelper.getInstance().getConnection();

        String sql = " SELECT * from vorlagen WHERE ProzesseID = " + processId;

        try {
            List<Vorlage> ret = new QueryRunner().query(connection, sql.toString(), resultSetToTemplateListHandler);
            return ret;
        } finally {
            MySQLHelper.closeConnection(connection);
        }

    }

    public static ResultSetHandler<List<Vorlage>> resultSetToTemplateListHandler = new ResultSetHandler<List<Vorlage>>() {
        @Override
        public List<Vorlage> handle(ResultSet rs) throws SQLException {
            List<Vorlage> answer = new ArrayList<Vorlage>();

            while (rs.next()) {
                int templateId = rs.getInt("VorlagenID");
                int processId = rs.getInt("ProzesseID");
                Vorlage template = new Vorlage();
                template.setId(templateId);
                template.setProcessId(processId);
                List<Vorlageeigenschaft> properties = PropertyManager.getTemplateProperties(templateId);
                template.setEigenschaften(properties);
                answer.add(template);
            }
            return answer;
        }
    };

    public static ResultSetHandler<Vorlage> resultSetToTemplateHandler = new ResultSetHandler<Vorlage>() {
        @Override
        public Vorlage handle(ResultSet rs) throws SQLException {

            if (rs.next()) {
                int templateId = rs.getInt("VorlagenID");
                int processId = rs.getInt("ProzesseID");
                Vorlage template = new Vorlage();
                template.setId(templateId);
                template.setProcessId(processId);
                List<Vorlageeigenschaft> properties = PropertyManager.getTemplateProperties(templateId);
                template.setEigenschaften(properties);
                return template;
            }
            return null;
        }
    };

    public static Vorlage getTemplateForTemplateID(int templateId) throws SQLException {
        Connection connection = MySQLHelper.getInstance().getConnection();
        String sql = " SELECT * from vorlagen WHERE VorlagenID = " + templateId;
        try {
            Vorlage ret = new QueryRunner().query(connection, sql.toString(), resultSetToTemplateHandler);
            return ret;
        } finally {
            MySQLHelper.closeConnection(connection);
        }
    }

    public static int getCountOfTemplates() throws SQLException {
        Connection connection = MySQLHelper.getInstance().getConnection();
        String sql = " SELECT count(VorlagenID) from vorlagen";
        try {
            return new QueryRunner().query(connection, sql.toString(), MySQLUtils.resultSetToIntegerHandler);
        } finally {
            MySQLHelper.closeConnection(connection);
        }
    }

    public static void saveTemplate(Vorlage template) throws SQLException {
        Connection connection = MySQLHelper.getInstance().getConnection();
        try {
            QueryRunner run = new QueryRunner();
            String sql = "";

            if (template.getId() == null) {
                sql = "INSERT INTO vorlagen ( Herkunft, ProzesseID ) VALUES ( NULL, " + template.getProcessId() + ")";
                run.update(connection, sql.toString());
                int templateId = getLastTemplateId(connection);
                template.setId(templateId);
            } else {
                sql = "UPDATE vorlagen set Herkunft = NULL, ProzesseID = " + template.getProcessId() + " WHERE VorlagenID =" + template.getId();
                run.update(connection, sql);
            }
            List<Vorlageeigenschaft> templateProperties = template.getEigenschaften();
            for (Vorlageeigenschaft property : templateProperties) {
                property.setTemplateId(template.getId());
                PropertyManager.saveTemplateProperty(property);
            }
        } finally {
            MySQLHelper.closeConnection(connection);
        }
    }

    private static int getLastTemplateId(Connection connection) throws SQLException {
        String sql = "SELECT VorlagenID from vorlagen order by VorlagenID desc limit 1";
        QueryRunner run = new QueryRunner();
        return run.query(sql, MySQLUtils.resultSetToIntegerHandler);
    }
}
