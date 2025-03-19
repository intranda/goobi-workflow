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
import java.util.Date;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.goobi.beans.Masterpieceproperty;
import org.goobi.beans.Processproperty;
import org.goobi.beans.Templateproperty;

import de.sub.goobi.helper.enums.PropertyType;
import lombok.extern.log4j.Log4j2;

@Log4j2
class PropertyMysqlHelper implements Serializable {
    private static final long serialVersionUID = 5175567943231852013L;

    /*
     * The NOSONAR tags are used here because the string constants have exactly the same name as the database column names. Sonarcloud recommends to
     * use only uppercase and underscores, but that would make the use cases of the constants less readable.
     */
    private static final String prozesseeigenschaftenID = "prozesseeigenschaftenID"; // NOSONAR
    private static final String vorlageneigenschaftenID = "vorlageneigenschaftenID"; // NOSONAR
    private static final String werkstueckeeigenschaftenID = "werkstueckeeigenschaftenID"; // NOSONAR
    private static final String Titel = "Titel"; // NOSONAR
    private static final String Wert = "Wert"; // NOSONAR
    private static final String IstObligatorisch = "IstObligatorisch"; // NOSONAR
    private static final String DatentypenID = "DatentypenID"; // NOSONAR
    private static final String Auswahl = "Auswahl"; // NOSONAR
    private static final String prozesseID = "prozesseID"; // NOSONAR
    private static final String vorlagenID = "vorlagenID"; // NOSONAR
    private static final String werkstueckeID = "werkstueckeID"; // NOSONAR
    private static final String creationDate = "creationDate"; // NOSONAR
    private static final String container = "container"; // NOSONAR

    public static List<Processproperty> getProcessPropertiesForProcess(int processId) throws SQLException {
        String sql = "SELECT * FROM prozesseeigenschaften WHERE prozesseID = ? ORDER BY container, Titel, creationDate desc";
        Connection connection = null;
        Object[] param = { processId };
        try {
            connection = MySQLHelper.getInstance().getConnection();
            return new QueryRunner().query(connection, sql, resultSetToPropertyListHandler, param);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static Processproperty getProcessPropertyById(int propertyId) throws SQLException {
        String sql = "SELECT * FROM prozesseeigenschaften WHERE prozesseeigenschaftenID = ?";
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            return new QueryRunner().query(connection, sql, resultSetToPropertyHandler, propertyId);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }

    }

    private static final ResultSetHandler<Processproperty> resultSetToPropertyHandler = new ResultSetHandler<>() {
        @Override
        public Processproperty handle(ResultSet resultSet) throws SQLException {
            try {
                if (resultSet.next()) {
                    return PropertyMysqlHelper.parseProcessProperty(resultSet);
                }
            } finally {
                resultSet.close();
            }
            return null;
        }
    };

    public static final ResultSetHandler<List<Processproperty>> resultSetToPropertyListHandler = new ResultSetHandler<>() {
        @Override
        public List<Processproperty> handle(ResultSet resultSet) throws SQLException {
            List<Processproperty> properties = new ArrayList<>();
            try {
                while (resultSet.next()) {
                    properties.add(PropertyMysqlHelper.parseProcessProperty(resultSet));
                }
            } finally {
                resultSet.close();
            }
            return properties;
        }
    };

    private static final ResultSetHandler<List<Templateproperty>> resultSetToTemplatePropertyListHandler =
            new ResultSetHandler<>() {
                @Override
                public List<Templateproperty> handle(ResultSet resultSet) throws SQLException {
                    List<Templateproperty> properties = new ArrayList<>();
                    try {
                        while (resultSet.next()) {
                            properties.add(PropertyMysqlHelper.parseTemplateProperty(resultSet));
                        }
                    } finally {
                        resultSet.close();
                    }
                    return properties;
                }
            };

    private static final ResultSetHandler<List<Masterpieceproperty>> resultSetToMasterpiecePropertyListHandler =
            new ResultSetHandler<>() {
                @Override
                public List<Masterpieceproperty> handle(ResultSet resultSet) throws SQLException {
                    List<Masterpieceproperty> properties = new ArrayList<>();
                    try {
                        while (resultSet.next()) {
                            properties.add(PropertyMysqlHelper.parseMasterpieceProperty(resultSet));
                        }
                    } finally {
                        resultSet.close();
                    }
                    return properties;
                }
            };

    private static Processproperty parseProcessProperty(ResultSet result) throws SQLException {
        Processproperty property = new Processproperty();
        property.setId(result.getInt(prozesseeigenschaftenID));
        property.setPropertyName(result.getString(Titel));
        property.setPropertyValue(result.getString(Wert));
        property.setRequired(result.getBoolean(IstObligatorisch));
        property.setType(PropertyType.getById(result.getInt(DatentypenID)));
        property.setProcessId(result.getInt(prozesseID));
        Timestamp time = result.getTimestamp(creationDate);
        Date creationDate = null;
        if (time != null) {
            creationDate = new Date(time.getTime());
        }
        property.setCreationDate(creationDate);
        property.setContainer(result.getString(container));
        return property;
    }

    private static Templateproperty parseTemplateProperty(ResultSet result) throws SQLException {
        Templateproperty property = new Templateproperty();
        property.setId(result.getInt(vorlageneigenschaftenID));
        property.setPropertyName(result.getString(Titel));
        property.setPropertyValue(result.getString(Wert));
        property.setRequired(result.getBoolean(IstObligatorisch));
        property.setType(PropertyType.getById(result.getInt(DatentypenID)));
        property.setTemplateId(result.getInt(vorlagenID));
        Timestamp time = result.getTimestamp(creationDate);
        Date creationDate = null;
        if (time != null) {
            creationDate = new Date(time.getTime());
        }
        property.setCreationDate(creationDate);
        property.setContainer(result.getString(container));
        return property;
    }

    private static Masterpieceproperty parseMasterpieceProperty(ResultSet result) throws SQLException {
        Masterpieceproperty property = new Masterpieceproperty();
        property.setId(result.getInt(werkstueckeeigenschaftenID));
        property.setPropertyName(result.getString(Titel));
        property.setPropertyValue(result.getString(Wert));
        property.setRequired(result.getBoolean(IstObligatorisch));
        property.setType(PropertyType.getById(result.getInt(DatentypenID)));
        property.setObjectId(result.getInt(werkstueckeID));
        Timestamp time = result.getTimestamp(creationDate);
        Date creationDate = null;
        if (time != null) {
            creationDate = new Date(time.getTime());
        }
        property.setCreationDate(creationDate);
        property.setContainer(result.getString(container));
        return property;
    }

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
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO prozesseeigenschaften (Titel, WERT, IstObligatorisch, DatentypenID,  prozesseID, creationDate, container) ");
        sql.append(" VALUES (?, ?, ?, ?, ?, ?, ?) ");

        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();

            Integer id = run.insert(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler, pe.getPropertyName(), pe.getPropertyValue(),
                    pe.isRequired(), pe.getType().getId(), pe.getProzess().getId(),
                    pe.getCreationDate() == null ? null : new Timestamp(pe.getCreationDate().getTime()), pe.getContainer());
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
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE prozesseeigenschaften set Titel = ?,  WERT = ?, IstObligatorisch = ?, DatentypenID = ?, prozesseID = ?,  ");
        sql.append("creationDate = ?, container = ? WHERE prozesseeigenschaftenID =  ");
        sql.append(pe.getId());

        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            run.update(connection, sql.toString(), pe.getPropertyName(), pe.getPropertyValue(), pe.isRequired(), pe.getType().getId(),

                    pe.getProzess().getId(), pe.getCreationDate() == null ? null : new Timestamp(pe.getCreationDate().getTime()), pe.getContainer());
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
            if (log.isTraceEnabled()) {
                log.trace(sql);
            }
            return new QueryRunner().query(connection, sql, MySQLHelper.resultSetToStringListHandler);
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
            if (log.isTraceEnabled()) {
                log.trace(sql);
            }
            return new QueryRunner().query(connection, sql, MySQLHelper.resultSetToStringListHandler);
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
            if (log.isTraceEnabled()) {
                log.trace(sql);
            }
            return new QueryRunner().query(connection, sql, MySQLHelper.resultSetToStringListHandler);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static List<Templateproperty> getTemplateProperties(int templateId) throws SQLException {
        String sql = "SELECT * FROM vorlageneigenschaften WHERE vorlagenID = ? ORDER BY Titel, creationDate desc";
        Connection connection = null;
        Object[] param = { templateId };
        try {
            connection = MySQLHelper.getInstance().getConnection();
            return new QueryRunner().query(connection, sql, resultSetToTemplatePropertyListHandler, param);
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
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE vorlageneigenschaften set Titel = ?,  WERT = ?, IstObligatorisch = ?, DatentypenID = ?, vorlagenID = ?, ");
        sql.append("creationDate = ?, container = ? WHERE vorlageneigenschaftenID = ");
        sql.append(property.getId());
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            run.update(connection, sql.toString(), property.getPropertyName(), property.getPropertyValue(), property.isRequired(),
                    property.getType().getId(),
                    property.getVorlage().getId(),
                    property.getCreationDate() == null ? null : new Timestamp(property.getCreationDate().getTime()), property.getContainer());
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    private static Templateproperty insertTemplateproperty(Templateproperty property) throws SQLException {

        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO vorlageneigenschaften (Titel, WERT, IstObligatorisch, DatentypenID,  vorlagenID, creationDate, container) ");
        sql.append("VALUES (?, ?, ?, ?, ?, ?, ?)");

        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            int id = run.insert(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler, property.getPropertyName(),
                    property.getPropertyValue(),
                    property.isRequired(), property.getType().getId(), property.getVorlage().getId(),
                    property.getCreationDate() == null ? null : new Timestamp(property.getCreationDate().getTime()), property.getContainer());
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
        String sql = "SELECT * FROM werkstueckeeigenschaften WHERE werkstueckeID = ? ORDER BY container, Titel, creationDate desc";
        Connection connection = null;
        Object[] param = { templateId };
        try {
            connection = MySQLHelper.getInstance().getConnection();
            return new QueryRunner().query(connection, sql, resultSetToMasterpiecePropertyListHandler, param);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static Masterpieceproperty saveMasterpieceProperty(Masterpieceproperty property) throws SQLException {
        if (property.getObjectId() == null && property.getWerkstueck() != null) {
            property.setObjectId(property.getWerkstueck().getId());
        }

        if (property.getId() == null) {
            return insertMasterpieceproperty(property);
        } else {
            updateMasterpieceproperty(property);
            return property;
        }
    }

    private static void updateMasterpieceproperty(Masterpieceproperty property) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append(
                "UPDATE werkstueckeeigenschaften set Titel = ?,  WERT = ?, IstObligatorisch = ?, DatentypenID = ?, werkstueckeID = ?, ");
        sql.append("creationDate = ?, container = ? WHERE werkstueckeeigenschaftenID = ");
        sql.append(property.getId());

        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            run.update(connection, sql.toString(), property.getPropertyName(), property.getPropertyValue(), property.isRequired(),
                    property.getType().getId(),
                    property.getWerkstueck().getId(),
                    property.getCreationDate() == null ? null : new Timestamp(property.getCreationDate().getTime()), property.getContainer());
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    private static Masterpieceproperty insertMasterpieceproperty(Masterpieceproperty property) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO werkstueckeeigenschaften (Titel, WERT, IstObligatorisch, DatentypenID,  werkstueckeID, creationDate, ");
        sql.append("container) VALUES (?, ?, ?, ?, ?, ?, ?)");
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            int id = run.insert(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler, property.getPropertyName(),
                    property.getPropertyValue(),
                    property.isRequired(), property.getType().getId(), property.getWerkstueck().getId(),
                    property.getCreationDate() == null ? null : new Timestamp(property.getCreationDate().getTime()), property.getContainer());
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
