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

import de.sub.goobi.beans.Prozesseigenschaft;
import de.sub.goobi.helper.enums.PropertyType;
import de.sub.goobi.persistence.apache.MySQLHelper;
import de.sub.goobi.persistence.apache.MySQLUtils;

public class PropertyMysqlHelper {
    private static final Logger logger = Logger.getLogger(PropertyMysqlHelper.class);

    public static List<Prozesseigenschaft> getProcessPropertiesForProcess(int processId) throws SQLException {
        String sql = "SELECT * FROM prozesseeigenschaften WHERE prozesseID = ? ORDER BY container, Titel";
        Connection connection = MySQLHelper.getInstance().getConnection();
        Object[] param = {processId};
        try {
            List<Prozesseigenschaft> ret = new QueryRunner().query(connection, sql.toString(), resultSetToPropertyListHandler, param);
            return ret;
        } finally {
            MySQLHelper.closeConnection(connection);
        }
    }

    public static ResultSetHandler<Prozesseigenschaft> resultSetToPropertyHandler = new ResultSetHandler<Prozesseigenschaft>() {
        @Override
        public Prozesseigenschaft handle(ResultSet rs) throws SQLException {
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
                Prozesseigenschaft pe = new Prozesseigenschaft();
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

    public static ResultSetHandler<List<Prozesseigenschaft>> resultSetToPropertyListHandler = new ResultSetHandler<List<Prozesseigenschaft>>() {
        @Override
        public List<Prozesseigenschaft> handle(ResultSet rs) throws SQLException {
            List<Prozesseigenschaft> properties = new ArrayList<Prozesseigenschaft>();
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
                Prozesseigenschaft pe = new Prozesseigenschaft();
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

    public static void save(Prozesseigenschaft pe) throws SQLException {
        if (pe.getId() == null) {
            insert(pe);
        } else {
            update(pe);
        }
    }

    private static void insert(Prozesseigenschaft pe) throws SQLException {
        String sql =
                "INSERT INTO prozesseeigenschaften (Titel, WERT, IstObligatorisch, DatentypenID, Auswahl, prozesseID, creationDate, container) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
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

    private static void update(Prozesseigenschaft pe) throws SQLException {
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
}
