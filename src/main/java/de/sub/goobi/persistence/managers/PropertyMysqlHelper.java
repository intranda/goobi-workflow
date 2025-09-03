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
import org.goobi.beans.GoobiProperty;
import org.goobi.beans.GoobiProperty.PropertyOwnerType;
import org.goobi.beans.Processproperty;

import de.sub.goobi.helper.enums.PropertyType;
import lombok.extern.log4j.Log4j2;

@Log4j2
@SuppressWarnings("deprecation")
final class PropertyMysqlHelper implements Serializable {

    private PropertyMysqlHelper() {
        // hide implicit public constructor
    }

    private static final long serialVersionUID = 5175567943231852013L;

    public static List<Processproperty> getProcessPropertiesForProcess(int processId) throws SQLException {
        String sql = "SELECT * FROM properties WHERE object_id = ? and object_type = 'process' ORDER BY container, property_name, id desc";
        Connection connection = null;
        Object[] param = { processId };
        try {
            connection = MySQLHelper.getInstance().getConnection();
            return new QueryRunner().query(connection, sql, resultSetToProcessPropertyListHandler, param);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static Processproperty getProcessPropertyById(int propertyId) throws SQLException {
        String sql = "SELECT * FROM properties WHERE id = ?";
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            return new QueryRunner().query(connection, sql, resultSetToProcessPropertyHandler, propertyId);
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }

    }

    private static final ResultSetHandler<GoobiProperty> resultSetToPropertyHandler = new ResultSetHandler<>() {
        @Override
        public GoobiProperty handle(ResultSet resultSet) throws SQLException {
            try {
                if (resultSet.next()) {
                    return PropertyMysqlHelper.parseProperty(resultSet);
                }
            } finally {
                resultSet.close();
            }
            return null;
        }
    };

    public static final ResultSetHandler<List<GoobiProperty>> resultSetToPropertyListHandler = new ResultSetHandler<>() {
        @Override
        public List<GoobiProperty> handle(ResultSet resultSet) throws SQLException {
            List<GoobiProperty> properties = new ArrayList<>();
            try {
                while (resultSet.next()) {
                    properties.add(PropertyMysqlHelper.parseProperty(resultSet));
                }
            } finally {
                resultSet.close();
            }
            return properties;
        }
    };

    private static final ResultSetHandler<Processproperty> resultSetToProcessPropertyHandler = new ResultSetHandler<>() {
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

    public static final ResultSetHandler<List<Processproperty>> resultSetToProcessPropertyListHandler = new ResultSetHandler<>() {
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

    private static Processproperty parseProcessProperty(ResultSet result) throws SQLException {
        Processproperty property = new Processproperty();
        property.setId(result.getInt("id"));
        property.setPropertyName(result.getString("property_name"));
        property.setPropertyValue(result.getString("property_value"));
        property.setRequired(result.getBoolean("required"));
        property.setType(PropertyType.getById(result.getInt("datatype")));
        property.setObjectId(result.getInt("object_id"));
        Timestamp time = result.getTimestamp("creation_date");
        Date creationDate = null;
        if (time != null) {
            creationDate = new Date(time.getTime());
        }
        property.setCreationDate(creationDate);
        property.setContainer(result.getString("container"));
        return property;
    }

    private static GoobiProperty parseProperty(ResultSet result) throws SQLException {
        PropertyOwnerType pot = PropertyOwnerType.getByTitle(result.getString("object_type"));
        GoobiProperty property = new GoobiProperty(pot);
        property.setId(result.getInt("id"));
        property.setPropertyName(result.getString("property_name"));
        property.setPropertyValue(result.getString("property_value"));
        property.setRequired(result.getBoolean("required"));
        property.setType(PropertyType.getById(result.getInt("datatype")));
        property.setObjectId(result.getInt("object_id"));
        Timestamp time = result.getTimestamp("creation_date");
        Date creationDate = null;
        if (time != null) {
            creationDate = new Date(time.getTime());
        }
        property.setCreationDate(creationDate);
        property.setContainer(result.getString("container"));
        return property;
    }

    public static void saveProcessproperty(Processproperty pe) throws SQLException {
        if (pe.getObjectId() == 0 && pe.getProzess() != null) {
            pe.setObjectId(pe.getProzess().getId());
        }
        if (pe.getId() == null) {
            insertProcessproperty(pe);
        } else {
            updateProcessproperty(pe);
        }
    }

    static void insertProperty(GoobiProperty pe) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO properties (property_name, property_value, required, datatype, object_id, object_type, creation_date, container) ");
        sql.append(" VALUES (?, ?, ?, ?, ?, ?, ?, ?) ");

        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();

            Integer id = run.insert(connection, sql.toString(), MySQLHelper.resultSetToIntegerHandler, pe.getPropertyName(), pe.getPropertyValue(),
                    pe.isRequired(), pe.getType().getId(), pe.getObjectId(), pe.getPropertyType().getTitle(),
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

    static void updateProperty(GoobiProperty property) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE properties set property_name = ?,  property_value = ?, required = ?, datatype = ?, object_id = ?,  ");
        sql.append("object_type = ?, creation_date = ?, container = ? WHERE id =  ");

        sql.append(property.getId());
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            QueryRunner run = new QueryRunner();
            run.update(connection, sql.toString(), property.getPropertyName(), property.getPropertyValue(), property.isRequired(),
                    property.getType().getId(), property.getObjectId(), property.getPropertyType().getTitle(),
                    property.getCreationDate() == null ? null : new Timestamp(property.getCreationDate().getTime()), property.getContainer());
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    static void deleteProperty(GoobiProperty property) throws SQLException {
        if (property.getId() != null) {
            Connection connection = null;
            try {
                connection = MySQLHelper.getInstance().getConnection();
                QueryRunner run = new QueryRunner();
                String sql = "DELETE FROM properties WHERE id = " + property.getId();
                run.update(connection, sql);
            } finally {
                if (connection != null) {
                    MySQLHelper.closeConnection(connection);
                }
            }
        }
    }

    private static void insertProcessproperty(Processproperty pe) throws SQLException {
        insertProperty(pe);
    }

    private static void updateProcessproperty(Processproperty pe) throws SQLException {
        updateProperty(pe);
    }

    public static void deleteProcessProperty(Processproperty property) throws SQLException {
        deleteProperty(property);
    }

    public static List<String> getDistinctPropertyTitles() throws SQLException {
        return getPropertyTitles(PropertyOwnerType.PROCESS);
    }

    static void saveProperty(GoobiProperty property) throws SQLException {
        if (property.getId() == null) {
            insertProperty(property);
        } else {
            updateProperty(property);
        }

    }

    public static List<String> getPropertyTitles(PropertyOwnerType propertyType) throws SQLException {
        String sql = "select distinct property_name from properties where object_type = ? and property_name is not null order by property_name";
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            if (log.isTraceEnabled()) {
                log.trace(sql);
            }
            return new QueryRunner().query(connection, sql, MySQLHelper.resultSetToStringListHandler, propertyType.getTitle());
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    public static GoobiProperty getPropertyById(int propertyId) throws SQLException {
        String sql = "SELECT * FROM properties WHERE id = ?";
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

    public static List<GoobiProperty> getPropertiesForObject(int objectId, PropertyOwnerType propertyType) throws SQLException {
        String sql = "SELECT * FROM properties WHERE object_id = ? and object_type = ? ORDER BY container, property_name, id desc";
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            return new QueryRunner().query(connection, sql, resultSetToPropertyListHandler, objectId, propertyType.getTitle());
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }
}
