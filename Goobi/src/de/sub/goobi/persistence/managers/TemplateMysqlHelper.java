package de.sub.goobi.persistence.managers;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.goobi.beans.Processproperty;
import org.goobi.beans.Template;
import org.goobi.beans.Templateproperty;

class TemplateMysqlHelper {

    public static List<Template> getTemplatesForProcess(int processId) throws SQLException {
        Connection connection = MySQLHelper.getInstance().getConnection();

        String sql = " SELECT * from vorlagen WHERE ProzesseID = " + processId;

        try {
            List<Template> ret = new QueryRunner().query(connection, sql.toString(), resultSetToTemplateListHandler);
            return ret;
        } finally {
            MySQLHelper.closeConnection(connection);
        }

    }

    public static ResultSetHandler<List<Template>> resultSetToTemplateListHandler = new ResultSetHandler<List<Template>>() {
        @Override
        public List<Template> handle(ResultSet rs) throws SQLException {
            List<Template> answer = new ArrayList<Template>();

            while (rs.next()) {
                int templateId = rs.getInt("VorlagenID");
                String herkunft = rs.getString("Herkunft");
                int processId = rs.getInt("ProzesseID");
                Template template = new Template();
                template.setId(templateId);
                template.setHerkunft(herkunft);
                template.setProcessId(processId);
                List<Templateproperty> properties = PropertyManager.getTemplateProperties(templateId);
                template.setEigenschaften(properties);
                answer.add(template);
            }
            return answer;
        }
    };

    public static ResultSetHandler<Template> resultSetToTemplateHandler = new ResultSetHandler<Template>() {
        @Override
        public Template handle(ResultSet rs) throws SQLException {

            if (rs.next()) {
                int templateId = rs.getInt("VorlagenID");
                String herkunft = rs.getString("Herkunft");
                int processId = rs.getInt("ProzesseID");
                Template template = new Template();
                template.setId(templateId);
                template.setHerkunft(herkunft);
                template.setProcessId(processId);
                List<Templateproperty> properties = PropertyManager.getTemplateProperties(templateId);
                template.setEigenschaften(properties);
                return template;
            }
            return null;
        }
    };

    public static Template getTemplateForTemplateID(int templateId) throws SQLException {
        Connection connection = MySQLHelper.getInstance().getConnection();
        String sql = " SELECT * from vorlagen WHERE VorlagenID = " + templateId;
        try {
            Template ret = new QueryRunner().query(connection, sql.toString(), resultSetToTemplateHandler);
            return ret;
        } finally {
            MySQLHelper.closeConnection(connection);
        }
    }

    public static int getCountOfTemplates() throws SQLException {
        Connection connection = MySQLHelper.getInstance().getConnection();
        String sql = " SELECT count(VorlagenID) from vorlagen";
        try {
            return new QueryRunner().query(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler);
        } finally {
            MySQLHelper.closeConnection(connection);
        }
    }

    public static void saveTemplate(Template template) throws SQLException {
        Connection connection = MySQLHelper.getInstance().getConnection();
        try {
            QueryRunner run = new QueryRunner();
            String sql = "";

            if (template.getId() == null) {
                sql = "INSERT INTO vorlagen ( Herkunft, ProzesseID ) VALUES ( ?, ?)";
                Object[] param = { template.getHerkunft() == null ? null : template.getHerkunft(), template.getProzess().getId() };

                int id = run.insert(connection, sql, MySQLHelper.resultSetToIntegerHandler, param);
                template.setId(id);
            } else {
                sql = "UPDATE vorlagen set Herkunft = ?, ProzesseID = ? WHERE VorlagenID =" + template.getId();
                Object[] param = { template.getHerkunft() == null ? null : template.getHerkunft(), template.getProzess().getId() };
                run.update(connection, sql, param);
            }

        } finally {
            MySQLHelper.closeConnection(connection);
        }

        List<Templateproperty> templateProperties = template.getEigenschaften();
        for (Templateproperty property : templateProperties) {
            property.setTemplateId(template.getId());
            property = PropertyManager.saveTemplateProperty(property);
        }
    }

    public static void deleteTemplate(Template template) throws SQLException {
        if (template.getId() != null) {
            for (Templateproperty property : template.getEigenschaften()) {
                PropertyManager.deleteTemplateProperty(property);
            }
            Connection connection = MySQLHelper.getInstance().getConnection();
            try {
                QueryRunner run = new QueryRunner();
                String sql = "DELETE FROM vorlagen WHERE VorlagenID = " + template.getId();
                run.update(connection, sql);
            } finally {
                MySQLHelper.closeConnection(connection);
            }
        }

    }
}
