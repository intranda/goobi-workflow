package de.sub.goobi.persistence.managers;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.log4j.Logger;
import org.goobi.beans.User;
import org.goobi.beans.Usergroup;

import de.sub.goobi.persistence.apache.MySQLHelper;
import de.sub.goobi.persistence.apache.MySQLUtils;

public class UsergroupMysqlHelper {
	private static final Logger logger = Logger.getLogger(UsergroupMysqlHelper.class);

	public static List<Usergroup> getUsergroups(String order, String filter, Integer start, Integer count) throws SQLException {
		Connection connection = MySQLHelper.getInstance().getConnection();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM benutzergruppen");
		if (filter!=null && !filter.isEmpty()){
			sql.append(" WHERE " + filter);
		}
		if (order!=null && !order.isEmpty()){
			sql.append(" ORDER BY " + order);
		}
		if (start != null && count != null){
			sql.append(" LIMIT " + start + ", " + count);
		}
		try {
			logger.debug(sql.toString());
			List<Usergroup> ret = new QueryRunner().query(connection, sql.toString(), UsergroupManager.resultSetToUsergroupListHandler);
			return ret;
		} finally {
			MySQLHelper.closeConnection(connection);
		}
	}

	public static List<Usergroup> getUsergroupsForUser(User user) throws SQLException {
		return getUsergroups(null,"BenutzergruppenID IN (SELECT BenutzerGruppenID FROM benutzergruppenmitgliedschaft WHERE BenutzerID=" + user.getId() +")",null,null);
	}
	
	public static int getUsergroupCount(String order, String filter) throws SQLException {
		Connection connection = MySQLHelper.getInstance().getConnection();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT(BenutzergruppenID) FROM benutzergruppen");
		if (filter!=null && !filter.isEmpty()){
			sql.append(" WHERE " + filter);
		}
		try {
			logger.debug(sql.toString());
			return new QueryRunner().query(connection, sql.toString(), MySQLUtils.resultSetToIntegerHandler);
		} finally {
			MySQLHelper.closeConnection(connection);
		}
	}

	public static Usergroup getUsergroupById(int id) throws SQLException {
		Connection connection = MySQLHelper.getInstance().getConnection();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM benutzergruppen WHERE BenutzergruppenID = " + id);
		try {
			logger.debug(sql.toString());
			Usergroup ret = new QueryRunner().query(connection, sql.toString(), UsergroupManager.resultSetToUsergroupHandler);
			return ret;
		} finally {
			MySQLHelper.closeConnection(connection);
		}
	}

	public static void saveUsergroup(Usergroup ro) throws SQLException {
		Connection connection = MySQLHelper.getInstance().getConnection();
		try {
			QueryRunner run = new QueryRunner();
			StringBuilder sql = new StringBuilder();

			if (ro.getId() == null) {
				String propNames = "titel, berechtigung";
				String propValues = "'" + ro.getTitel() + "'," + ro.getBerechtigung()
						+ "";
				sql.append("INSERT INTO benutzergruppen (");
				sql.append(propNames);
				sql.append(") VALUES (");
				sql.append(propValues);
				sql.append(")");
			} else {
				sql.append("UPDATE benutzergruppen SET ");
				sql.append("titel = '" + ro.getTitel() + "', ");
				sql.append("berechtigung = '" + ro.getBerechtigung() + "' ");
				sql.append(" WHERE BenutzergruppenID = " + ro.getId()
						+ ";");
			}
			logger.debug(sql.toString());
			run.update(connection, sql.toString());
		} finally {
			MySQLHelper.closeConnection(connection);
		}
	}

	public static void deleteUsergroup(Usergroup ro) throws SQLException {
		if (ro.getId() != null) {
			Connection connection = MySQLHelper.getInstance().getConnection();
			try {
				QueryRunner run = new QueryRunner();
				String sql = "DELETE FROM benutzergruppen WHERE BenutzergruppenID = " + ro.getId() + ";";
				logger.debug(sql);
				run.update(connection, sql);
			} finally {
				MySQLHelper.closeConnection(connection);
			}
		}
	}
}
