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
import org.goobi.beans.Processproperty;

import de.sub.goobi.beans.Vorlageeigenschaft;
import de.sub.goobi.helper.enums.PropertyType;
import de.sub.goobi.persistence.apache.MySQLHelper;
import de.sub.goobi.persistence.apache.MySQLUtils;

class PropertyMysqlHelper {
    private static final Logger logger = Logger.getLogger(PropertyMysqlHelper.class);

    public static List<Processproperty> getProcessPropertiesForProcess(int processId) throws SQLException {
        String sql = "SELECT * FROM prozesseeigenschaften WHERE prozesseID = ? ORDER BY container, Titel";
        Connection connection = MySQLHelper.getInstance().getConnection();
        Object[] param = {processId};
        try {
            List<Processproperty> ret = new QueryRunner().query(connection, sql.toString(), resultSetToPropertyListHandler, param);
            return ret;
        } finally {
            MySQLHelper.closeConnection(connection);
        }
    }

    public static ResultSetHandler<Processproperty> resultSetToPropertyHandler = new ResultSetHandler<Processproperty>() {
        @Override
        public Processproperty handle(ResultSet rs) throws SQLException {
            if (rs.next()) {
                int id = rs.getInt("prozesseeigenschaftenID");
                String title = rs.getString("Titel");
                String value = rs.getString("Wert");
                Boolean mandatory = rs.getBoolean("IstObligatorisch");
                int type = rs.getInt("DatentypenID");
                String choice = rs.getString("Auswahl");
                int processId = rs.getInt("prozesseID");
                Date creationDate = rs.getDate("creationDate");
                int container = rs.getInt("container");
                Processproperty pe = new Processproperty();
                pe.setId(id);
                pe.setTitel(title);
                pe.setWert(value);
                pe.setIstObligatorisch(mandatory);
                pe.setType(PropertyType.getById(type));
                pe.setAuswahl(choice);
                pe.setProcessId(processId);
                pe.setCreationDate(creationDate);
                pe.setContainer(container);
                return pe;
            }
            return null;
        }
    };

    public static ResultSetHandler<List<Processproperty>> resultSetToPropertyListHandler = new ResultSetHandler<List<Processproperty>>() {
        @Override
        public List<Processproperty> handle(ResultSet rs) throws SQLException {
            List<Processproperty> properties = new ArrayList<Processproperty>();
            while (rs.next()) {
                int id = rs.getInt("prozesseeigenschaftenID");
                String title = rs.getString("Titel");
                String value = rs.getString("Wert");
                Boolean mandatory = rs.getBoolean("IstObligatorisch");
                int type = rs.getInt("DatentypenID");
                String choice = rs.getString("Auswahl");
                int processId = rs.getInt("prozesseID");
                Date creationDate = rs.getDate("creationDate");
                int container = rs.getInt("container");
                Processproperty pe = new Processproperty();
                pe.setId(id);
                pe.setTitel(title);
                pe.setWert(value);
                pe.setIstObligatorisch(mandatory);
                pe.setType(PropertyType.getById(type));
                pe.setAuswahl(choice);
                pe.setProcessId(processId);
                pe.setCreationDate(creationDate);
                pe.setContainer(container);
                properties.add(pe);
            }
            return properties;
        }
    };
    
    public static ResultSetHandler<List<Vorlageeigenschaft>> resultSetToTemplatePropertyListHandler = new ResultSetHandler<List<Vorlageeigenschaft>>() {
        @Override
        public List<Vorlageeigenschaft> handle(ResultSet rs) throws SQLException {
            List<Vorlageeigenschaft> properties = new ArrayList<Vorlageeigenschaft>();
            while (rs.next()) {
                int id = rs.getInt("vorlageneigenschaftenID");
                String title = rs.getString("Titel");
                String value = rs.getString("Wert");
                Boolean mandatory = rs.getBoolean("IstObligatorisch");
                int type = rs.getInt("DatentypenID");
                String choice = rs.getString("Auswahl");
                int templateId = rs.getInt("vorlagenID");
                Date creationDate = rs.getDate("creationDate");
                int container = rs.getInt("container");
                Vorlageeigenschaft ve = new Vorlageeigenschaft();
                ve.setId(id);
                ve.setTitel(title);
                ve.setWert(value);
                ve.setIstObligatorisch(mandatory);
                ve.setType(PropertyType.getById(type));
                ve.setAuswahl(choice);
                ve.setTemplateId(templateId);
                ve.setCreationDate(creationDate);
                ve.setContainer(container);
                properties.add(ve);
            }
            return properties;
        }
    };
    
    public static ResultSetHandler<Vorlageeigenschaft> resultSetToTemplatePropertyHandler = new ResultSetHandler<Vorlageeigenschaft>() {
        @Override
        public Vorlageeigenschaft handle(ResultSet rs) throws SQLException {
            if (rs.next()) {
                int id = rs.getInt("vorlageneigenschaftenID");
                String title = rs.getString("Titel");
                String value = rs.getString("Wert");
                Boolean mandatory = rs.getBoolean("IstObligatorisch");
                int type = rs.getInt("DatentypenID");
                String choice = rs.getString("Auswahl");
                int templateId = rs.getInt("vorlagenID");
                Date creationDate = rs.getDate("creationDate");
                int container = rs.getInt("container");
                Vorlageeigenschaft ve = new Vorlageeigenschaft();
                ve.setId(id);
                ve.setTitel(title);
                ve.setWert(value);
                ve.setIstObligatorisch(mandatory);
                ve.setType(PropertyType.getById(type));
                ve.setAuswahl(choice);
                ve.setTemplateId(templateId);
                ve.setCreationDate(creationDate);
                ve.setContainer(container);
                return ve;
            }
            return null;
        }
    };

    public static void saveProcessproperty(Processproperty pe) throws SQLException {
        if (pe.getId() == null) {
            insertProcessproperty(pe);
        } else {
            updateProcessproperty(pe);
        }
    }

    private static void insertProcessproperty(Processproperty pe) throws SQLException {
        String sql =
                "INSERT INTO prozesseeigenschaften (Titel, WERT, IstObligatorisch, DatentypenID, Auswahl, prozesseID, creationDate, container) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        Object[] param =
                { pe.getTitel(), pe.getWert(), pe.isIstObligatorisch(), pe.getType().getId(), pe.getAuswahl(), pe.getProcessId(),
                        pe.getCreationDate() == null ? null : new Timestamp(pe.getCreationDate().getTime()), pe.getContainer() };
        Connection connection = MySQLHelper.getInstance().getConnection();
        try {
            QueryRunner run = new QueryRunner();
            logger.debug(sql.toString());
            Integer id = run.insert(connection, sql.toString(), MySQLUtils.resultSetToIntegerHandler, param);
            if (id != null) {
                pe.setId(id);
            }
        } finally {
            MySQLHelper.closeConnection(connection);
        }

    }

    private static void updateProcessproperty(Processproperty pe) throws SQLException {
        String sql =
                "UPDATE prozesseeigenschaften set Titel = ?,  WERT = ?, IstObligatorisch = ?, DatentypenID = ?, Auswahl = ?, prozesseID = ?, creationDate = ?, container = ? WHERE prozesseeigenschaftenID = "
                        + pe.getId();
        Object[] param =
                { pe.getTitel(), pe.getWert(), pe.isIstObligatorisch(), pe.getType().getId(), pe.getAuswahl(), pe.getProcessId(),
                        pe.getCreationDate() == null ? null : new Timestamp(pe.getCreationDate().getTime()), pe.getContainer() };
        Connection connection = MySQLHelper.getInstance().getConnection();
        try {
            QueryRunner run = new QueryRunner();
            run.update(connection, sql, param);
        } finally {
            MySQLHelper.closeConnection(connection);
        }
    }
    
    
    public static List<String> getDistinctPropertyTitles() throws SQLException {
        String sql = "select distinct titel from prozesseeigenschaften";
        Connection connection = MySQLHelper.getInstance().getConnection();
        try {
            logger.debug(sql.toString());
            return new QueryRunner().query(connection, sql.toString(), MySQLUtils.resultSetToStringListHandler);

        } finally {
            MySQLHelper.closeConnection(connection);
        }
    }

    public static List<String> getDistinctTemplatePropertyTitles() throws SQLException {
        String sql = "select distinct titel from vorlageneigenschaften";
        Connection connection = MySQLHelper.getInstance().getConnection();
        try {
            logger.debug(sql.toString());
            return new QueryRunner().query(connection, sql.toString(), MySQLUtils.resultSetToStringListHandler);

        } finally {
            MySQLHelper.closeConnection(connection);
        }
    }

    public static List<String> getDistinctMasterpiecePropertyTitles() throws SQLException {
        String sql = "select distinct titel from werkstueckeeigenschaften";
        Connection connection = MySQLHelper.getInstance().getConnection();
        try {
            logger.debug(sql.toString());
            return new QueryRunner().query(connection, sql.toString(), MySQLUtils.resultSetToStringListHandler);

        } finally {
            MySQLHelper.closeConnection(connection);
        }
    }

    public static List<Vorlageeigenschaft> getTemplateProperties(int templateId) throws SQLException {
        String sql = "SELECT * FROM vorlageneigenschaften WHERE vorlagenID = ? ORDER BY container, Titel";
        Connection connection = MySQLHelper.getInstance().getConnection();
        Object[] param = {templateId};
        try {
            List<Vorlageeigenschaft> ret = new QueryRunner().query(connection, sql.toString(), resultSetToTemplatePropertyListHandler, param);
            return ret;
        } finally {
            MySQLHelper.closeConnection(connection);
        }
    }

    public static Vorlageeigenschaft saveTemplateproperty(Vorlageeigenschaft property) throws SQLException {
        if (property.getId() == null) {
            return insertTemplateproperty(property);
        } else {
            updateTemplateproperty(property);
            return property;
        }
    }

    private static void updateTemplateproperty(Vorlageeigenschaft property) throws SQLException {
        String sql =
                "UPDATE vorlageneigenschaften set Titel = ?,  WERT = ?, IstObligatorisch = ?, DatentypenID = ?, Auswahl = ?, vorlagenID = ?, creationDate = ?, container = ? WHERE vorlageneigenschaftenID = "
                        + property.getId();
        Object[] param =
                { property.getTitel(), property.getWert(), property.isIstObligatorisch(), property.getType().getId(), property.getAuswahl(), property.getTemplateId(),
                property.getCreationDate() == null ? null : new Timestamp(property.getCreationDate().getTime()), property.getContainer() };
        Connection connection = MySQLHelper.getInstance().getConnection();
        try {
            QueryRunner run = new QueryRunner();
            run.update(connection, sql, param);
        } finally {
            MySQLHelper.closeConnection(connection);
        }
    }

    private static Vorlageeigenschaft insertTemplateproperty(Vorlageeigenschaft property) throws SQLException {
        String sql =
                "INSERT INTO vorlageneigenschaften (Titel, WERT, IstObligatorisch, DatentypenID, Auswahl, vorlagenID, creationDate, container) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        Object[] param =
                { property.getTitel(), property.getWert(), property.isIstObligatorisch(), property.getType().getId(), property.getAuswahl(), property.getTemplateId(),
                property.getCreationDate() == null ? null : new Timestamp(property.getCreationDate().getTime()), property.getContainer() };
        Connection connection = MySQLHelper.getInstance().getConnection();
        try {
            QueryRunner run = new QueryRunner();
            int  id = run.insert(connection, sql, MySQLUtils.resultSetToIntegerHandler,  param);
            property.setId(id);
            return property;
        } finally {
            MySQLHelper.closeConnection(connection);
        }
        // TODO retrieve propertyId
    }
}
