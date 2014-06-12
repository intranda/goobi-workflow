package de.sub.goobi.persistence.managers;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;
import org.goobi.beans.Masterpieceproperty;
import org.goobi.beans.Processproperty;
import org.goobi.beans.Templateproperty;

import de.sub.goobi.helper.enums.PropertyType;

class PropertyMysqlHelper implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 5175567943231852013L;

    private static final Logger logger = Logger.getLogger(PropertyMysqlHelper.class);

    public static List<Processproperty> getProcessPropertiesForProcess(int processId) throws SQLException {
        String sql = "SELECT * FROM prozesseeigenschaften WHERE prozesseID = ? ORDER BY container, Titel";
        Connection connection = null;
        Object[] param = { processId };
        try {
            connection = MySQLHelper.getInstance().getConnection();
            List<Processproperty> ret = new QueryRunner().query(connection, sql.toString(), resultSetToPropertyListHandler, param);
            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static ResultSetHandler<Processproperty> resultSetToPropertyHandler = new ResultSetHandler<Processproperty>() {
        @Override
        public Processproperty handle(ResultSet rs) throws SQLException {
            try {
                if (rs.next()) {
                    int id = rs.getInt("prozesseeigenschaftenID");
                    String title = rs.getString("Titel");
                    String value = rs.getString("Wert");
                    Boolean mandatory = rs.getBoolean("IstObligatorisch");
                    int type = rs.getInt("DatentypenID");
                    String choice = rs.getString("Auswahl");
                    int processId = rs.getInt("prozesseID");
                    Timestamp time = rs.getTimestamp("creationDate");
                    Date creationDate = null;
                    if (time != null) {
                        creationDate = new Date(time.getTime());
                    }
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
            } finally {
                if (rs != null) {
                    rs.close();
                }
            }
            return null;
        }
    };

    public static ResultSetHandler<List<Processproperty>> resultSetToPropertyListHandler = new ResultSetHandler<List<Processproperty>>() {
        @Override
        public List<Processproperty> handle(ResultSet rs) throws SQLException {
            List<Processproperty> properties = new ArrayList<Processproperty>();
            try {
                while (rs.next()) {
                    int id = rs.getInt("prozesseeigenschaftenID");
                    String title = rs.getString("Titel");
                    String value = rs.getString("Wert");
                    Boolean mandatory = rs.getBoolean("IstObligatorisch");
                    int type = rs.getInt("DatentypenID");
                    String choice = rs.getString("Auswahl");
                    int processId = rs.getInt("prozesseID");
                    Timestamp time = rs.getTimestamp("creationDate");
                    Date creationDate = null;
                    if (time != null) {
                        creationDate = new Date(time.getTime());
                    }
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
            } finally {
                if (rs != null) {
                    rs.close();
                }
            }
            return properties;
        }
    };

    public static ResultSetHandler<List<Templateproperty>> resultSetToTemplatePropertyListHandler = new ResultSetHandler<List<Templateproperty>>() {
        @Override
        public List<Templateproperty> handle(ResultSet rs) throws SQLException {
            List<Templateproperty> properties = new ArrayList<Templateproperty>();
            try {
                while (rs.next()) {
                    int id = rs.getInt("vorlageneigenschaftenID");
                    String title = rs.getString("Titel");
                    String value = rs.getString("Wert");
                    Boolean mandatory = rs.getBoolean("IstObligatorisch");
                    int type = rs.getInt("DatentypenID");
                    String choice = rs.getString("Auswahl");
                    int templateId = rs.getInt("vorlagenID");
                    Timestamp time = rs.getTimestamp("creationDate");
                    Date creationDate = null;
                    if (time != null) {
                        creationDate = new Date(time.getTime());
                    }
                    int container = rs.getInt("container");
                    Templateproperty ve = new Templateproperty();
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
            } finally {
                if (rs != null) {
                    rs.close();
                }
            }
            return properties;
        }
    };

    public static ResultSetHandler<Templateproperty> resultSetToTemplatePropertyHandler = new ResultSetHandler<Templateproperty>() {
        @Override
        public Templateproperty handle(ResultSet rs) throws SQLException {
            try {
                if (rs.next()) {
                    int id = rs.getInt("vorlageneigenschaftenID");
                    String title = rs.getString("Titel");
                    String value = rs.getString("Wert");
                    Boolean mandatory = rs.getBoolean("IstObligatorisch");
                    int type = rs.getInt("DatentypenID");
                    String choice = rs.getString("Auswahl");
                    int templateId = rs.getInt("vorlagenID");
                    Timestamp time = rs.getTimestamp("creationDate");
                    Date creationDate = null;
                    if (time != null) {
                        creationDate = new Date(time.getTime());
                    }
                    int container = rs.getInt("container");
                    Templateproperty ve = new Templateproperty();
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
            } finally {
                if (rs != null) {
                    rs.close();
                }
            }
            return null;
        }
    };

    public static ResultSetHandler<List<Masterpieceproperty>> resultSetToMasterpiecePropertyListHandler =
            new ResultSetHandler<List<Masterpieceproperty>>() {
                @Override
                public List<Masterpieceproperty> handle(ResultSet rs) throws SQLException {
                    List<Masterpieceproperty> properties = new ArrayList<Masterpieceproperty>();
                    try {
                        while (rs.next()) {
                            int id = rs.getInt("werkstueckeeigenschaftenID");
                            String title = rs.getString("Titel");
                            String value = rs.getString("Wert");
                            Boolean mandatory = rs.getBoolean("IstObligatorisch");
                            int type = rs.getInt("DatentypenID");
                            String choice = rs.getString("Auswahl");
                            int templateId = rs.getInt("werkstueckeID");
                            Timestamp time = rs.getTimestamp("creationDate");
                            Date creationDate = null;
                            if (time != null) {
                                creationDate = new Date(time.getTime());
                            }
                            int container = rs.getInt("container");
                            Masterpieceproperty ve = new Masterpieceproperty();
                            ve.setId(id);
                            ve.setTitel(title);
                            ve.setWert(value);
                            ve.setIstObligatorisch(mandatory);
                            ve.setType(PropertyType.getById(type));
                            ve.setAuswahl(choice);
                            ve.setMasterpieceId(templateId);
                            ve.setCreationDate(creationDate);
                            ve.setContainer(container);
                            properties.add(ve);
                        }
                    } finally {
                        if (rs != null) {
                            rs.close();
                        }
                    }
                    return properties;
                }
            };

    public static ResultSetHandler<Masterpieceproperty> resultSetToMasterpiecePropertyHandler = new ResultSetHandler<Masterpieceproperty>() {
        @Override
        public Masterpieceproperty handle(ResultSet rs) throws SQLException {
            try {
                if (rs.next()) {
                    int id = rs.getInt("werkstueckeeigenschaftenID");
                    String title = rs.getString("Titel");
                    String value = rs.getString("Wert");
                    Boolean mandatory = rs.getBoolean("IstObligatorisch");
                    int type = rs.getInt("DatentypenID");
                    String choice = rs.getString("Auswahl");
                    int templateId = rs.getInt("werkstueckeID");
                    Timestamp time = rs.getTimestamp("creationDate");
                    Date creationDate = null;
                    if (time != null) {
                        creationDate = new Date(time.getTime());
                    }
                    int container = rs.getInt("container");
                    Masterpieceproperty ve = new Masterpieceproperty();
                    ve.setId(id);
                    ve.setTitel(title);
                    ve.setWert(value);
                    ve.setIstObligatorisch(mandatory);
                    ve.setType(PropertyType.getById(type));
                    ve.setAuswahl(choice);
                    ve.setMasterpieceId(templateId);
                    ve.setCreationDate(creationDate);
                    ve.setContainer(container);
                    return ve;
                }
            } finally {
                if (rs != null) {
                    rs.close();
                }
            }
            return null;
        }
    };

    public static void saveProcessproperty(Processproperty pe) throws SQLException {
        if (pe.getProcessId() == 0 && pe.getProzess() != null) {
            pe.setProcessId(pe.getProzess().getId());
        }
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
                { pe.getTitel(), pe.getWert(), pe.isIstObligatorisch(), pe.getType().getId(), pe.getAuswahl(), pe.getProzess().getId(),
                        pe.getCreationDate() == null ? null : new Timestamp(pe.getCreationDate().getTime()), pe.getContainer() };
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            logger.debug(sql.toString() + ", " + Arrays.toString(param));
            Integer id = run.insert(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler, param);
            if (id != null) {
                pe.setId(id);
            }
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }

    }

    private static void updateProcessproperty(Processproperty pe) throws SQLException {
        String sql =
                "UPDATE prozesseeigenschaften set Titel = ?,  WERT = ?, IstObligatorisch = ?, DatentypenID = ?, Auswahl = ?, prozesseID = ?, creationDate = ?, container = ? WHERE prozesseeigenschaftenID = "
                        + pe.getId();
        Object[] param =
                { pe.getTitel(), pe.getWert(), pe.isIstObligatorisch(), pe.getType().getId(), pe.getAuswahl(), pe.getProzess().getId(),
                        pe.getCreationDate() == null ? null : new Timestamp(pe.getCreationDate().getTime()), pe.getContainer() };
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            run.update(connection, sql, param);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void deleteProcessProperty(Processproperty property) throws SQLException {
        if (property.getId() != null) {
            Connection connection = null;
            try {
                connection = MySQLHelper.getInstance().getConnection();
                QueryRunner run = new QueryRunner();
                String sql = "DELETE FROM prozesseeigenschaften WHERE prozesseeigenschaftenID = " + property.getId();
                run.update(connection, sql);
            } finally {
                if (connection != null) {
                    MySQLHelper.closeConnection(connection);
                }
            }
        }
    }

    public static List<String> getDistinctPropertyTitles() throws SQLException {
        String sql = "select distinct titel from prozesseeigenschaften order by Titel";
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            logger.debug(sql.toString());
            return new QueryRunner().query(connection, sql.toString(), MySQLHelper.resultSetToStringListHandler);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static List<String> getDistinctTemplatePropertyTitles() throws SQLException {
        String sql = "select distinct titel from vorlageneigenschaften order by Titel";
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            logger.debug(sql.toString());
            return new QueryRunner().query(connection, sql.toString(), MySQLHelper.resultSetToStringListHandler);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static List<String> getDistinctMasterpiecePropertyTitles() throws SQLException {
        String sql = "select distinct titel from werkstueckeeigenschaften order by Titel";
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            logger.debug(sql.toString());
            return new QueryRunner().query(connection, sql.toString(), MySQLHelper.resultSetToStringListHandler);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static List<Templateproperty> getTemplateProperties(int templateId) throws SQLException {
        String sql = "SELECT * FROM vorlageneigenschaften WHERE vorlagenID = ? ORDER BY Titel";
        Connection connection = null;
        Object[] param = { templateId };
        try {
            connection = MySQLHelper.getInstance().getConnection();
            List<Templateproperty> ret = new QueryRunner().query(connection, sql.toString(), resultSetToTemplatePropertyListHandler, param);
            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static Templateproperty saveTemplateproperty(Templateproperty property) throws SQLException {
        if (property.getTemplateId() == null && property.getVorlage() != null) {
            property.setTemplateId(property.getVorlage().getId());
        }

        if (property.getId() == null) {
            return insertTemplateproperty(property);
        } else {
            updateTemplateproperty(property);
            return property;
        }
    }

    private static void updateTemplateproperty(Templateproperty property) throws SQLException {
        String sql =
                "UPDATE vorlageneigenschaften set Titel = ?,  WERT = ?, IstObligatorisch = ?, DatentypenID = ?, Auswahl = ?, vorlagenID = ?, creationDate = ?, container = ? WHERE vorlageneigenschaftenID = "
                        + property.getId();
        Object[] param =
                { property.getTitel(), property.getWert(), property.isIstObligatorisch(), property.getType().getId(), property.getAuswahl(),
                        property.getVorlage().getId(),
                        property.getCreationDate() == null ? null : new Timestamp(property.getCreationDate().getTime()), property.getContainer() };
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            run.update(connection, sql, param);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    private static Templateproperty insertTemplateproperty(Templateproperty property) throws SQLException {
        String sql =
                "INSERT INTO vorlageneigenschaften (Titel, WERT, IstObligatorisch, DatentypenID, Auswahl, vorlagenID, creationDate, container) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        Object[] param =
                { property.getTitel(), property.getWert(), property.isIstObligatorisch(), property.getType().getId(), property.getAuswahl(),
                        property.getVorlage().getId(),
                        property.getCreationDate() == null ? null : new Timestamp(property.getCreationDate().getTime()), property.getContainer() };
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            int id = run.insert(connection, sql, MySQLHelper.resultSetToIntegerHandler, param);
            property.setId(id);
            return property;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void deleteTemplateProperty(Templateproperty property) throws SQLException {
        if (property.getId() != null) {
            Connection connection = null;
            try {
                connection = MySQLHelper.getInstance().getConnection();
                QueryRunner run = new QueryRunner();
                String sql = "DELETE FROM vorlageneigenschaften WHERE vorlageneigenschaftenID = " + property.getId();
                run.update(connection, sql);
            } finally {
                if (connection != null) {
                    MySQLHelper.closeConnection(connection);
                }
            }
        }
    }

    public static List<Masterpieceproperty> getMasterpieceProperties(int templateId) throws SQLException {
        String sql = "SELECT * FROM werkstueckeeigenschaften WHERE werkstueckeID = ? ORDER BY container, Titel";
        Connection connection = null;
        Object[] param = { templateId };
        try {
            connection = MySQLHelper.getInstance().getConnection();
            List<Masterpieceproperty> ret = new QueryRunner().query(connection, sql.toString(), resultSetToMasterpiecePropertyListHandler, param);
            return ret;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static Masterpieceproperty saveMasterpieceProperty(Masterpieceproperty property) throws SQLException {
        if (property.getMasterpieceId() == null && property.getWerkstueck() != null) {
            property.setMasterpieceId(property.getWerkstueck().getId());
        }

        if (property.getId() == null) {
            return insertMasterpieceproperty(property);
        } else {
            updateMasterpieceproperty(property);
            return property;
        }
    }

    private static void updateMasterpieceproperty(Masterpieceproperty property) throws SQLException {
        String sql =
                "UPDATE werkstueckeeigenschaften set Titel = ?,  WERT = ?, IstObligatorisch = ?, DatentypenID = ?, Auswahl = ?, werkstueckeID = ?, creationDate = ?, container = ? WHERE werkstueckeeigenschaftenID = "
                        + property.getId();
        Object[] param =
                { property.getTitel(), property.getWert(), property.isIstObligatorisch(), property.getType().getId(), property.getAuswahl(),
                        property.getWerkstueck().getId(),
                        property.getCreationDate() == null ? null : new Timestamp(property.getCreationDate().getTime()), property.getContainer() };
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            run.update(connection, sql, param);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    private static Masterpieceproperty insertMasterpieceproperty(Masterpieceproperty property) throws SQLException {
        String sql =
                "INSERT INTO werkstueckeeigenschaften (Titel, WERT, IstObligatorisch, DatentypenID, Auswahl, werkstueckeID, creationDate, container) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        Object[] param =
                { property.getTitel(), property.getWert(), property.isIstObligatorisch(), property.getType().getId(), property.getAuswahl(),
                        property.getWerkstueck().getId(),
                        property.getCreationDate() == null ? null : new Timestamp(property.getCreationDate().getTime()), property.getContainer() };
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            int id = run.insert(connection, sql, MySQLHelper.resultSetToIntegerHandler, param);
            property.setId(id);
            return property;
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static void deleteMasterpieceProperty(Masterpieceproperty property) throws SQLException {
        if (property.getId() != null) {
            Connection connection = null;
            try {
                connection = MySQLHelper.getInstance().getConnection();
                QueryRunner run = new QueryRunner();
                String sql = "DELETE FROM werkstueckeeigenschaften WHERE werkstueckeeigenschaftenID = " + property.getId();
                run.update(connection, sql);
            } finally {
                if (connection != null) {
                    MySQLHelper.closeConnection(connection);
                }
            }
        }
    }
}
