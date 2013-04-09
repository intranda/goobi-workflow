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

    public static void saveTemplate(Vorlage template) throws SQLException  {
        if (template.getId() == null) {
            insertTemplate(template);
        } else {
            updateTemplate(template);
        }
    }

    private static void updateTemplate(Vorlage template) throws SQLException  {
        // TODO Auto-generated method stub
        
    }

    private static void insertTemplate(Vorlage template) throws SQLException  {
        // TODO Auto-generated method stub
        
    }

}
