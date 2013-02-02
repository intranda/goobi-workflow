package de.sub.goobi.persistence.managers;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.log4j.Logger;
import org.goobi.beans.Ruleset;

import de.sub.goobi.persistence.apache.MySQLHelper;
import de.sub.goobi.persistence.apache.MySQLUtils;

public class RulesetMysqlHelper {
	private static final Logger logger = Logger
			.getLogger(RulesetMysqlHelper.class);

	public static List<Ruleset> getRulesets(String order,
			HashMap<String, String> filter, int start, int count)
			throws SQLException {
		Connection connection = MySQLHelper.getInstance().getConnection();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM metadatenkonfigurationen LIMIT " + start
				+ ", " + count);
		try {
			logger.debug(sql.toString());
			List<Ruleset> ret = new QueryRunner().query(connection,
					sql.toString(),
					RulesetManager.resultSetToRulesetObjectListHandler);
			return ret;
		} finally {
			MySQLHelper.closeConnection(connection);
		}
	}

	public static int getRulesetCount(String order,
			HashMap<String, String> filter) throws SQLException {
		Connection connection = MySQLHelper.getInstance().getConnection();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT(MetadatenKonfigurationID) FROM metadatenkonfigurationen");
		try {
			logger.debug(sql.toString());
			return new QueryRunner().query(connection, sql.toString(),
					MySQLUtils.resultSetToIntegerHandler);
		} finally {
			MySQLHelper.closeConnection(connection);
		}
	}

	public static Ruleset getRulesetById(int id) throws SQLException {
		Connection connection = MySQLHelper.getInstance().getConnection();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM metadatenkonfigurationen WHERE MetadatenKonfigurationID = "
				+ id);
		try {
			logger.debug(sql.toString());
			Ruleset ret = new QueryRunner().query(connection, sql.toString(),
					RulesetManager.resultSetToRulesetObjectHandler);
			return ret;
		} finally {
			MySQLHelper.closeConnection(connection);
		}
	}

	public static void saveRuleset(Ruleset ro) throws SQLException {
		Connection connection = MySQLHelper.getInstance().getConnection();
		try {
			QueryRunner run = new QueryRunner();
			StringBuilder sql = new StringBuilder();

			if (ro.getId() == null) {
				String propNames = "Titel, Datei, orderMetadataByRuleset";
				String propValues = "'" + ro.getTitel() + "','" + ro.getDatei()
						+ "'," + ro.isOrderMetadataByRuleset() + "";
				sql.append("INSERT INTO metadatenkonfigurationen (" + propNames
						+ ") VALUES (" + propValues + ")");
			} else {
				sql.append("UPDATE metadatenkonfigurationen SET ");
				sql.append("Titel = '" + ro.getTitel() + "', ");
				sql.append("Datei = '" + ro.getDatei() + "', ");
				sql.append("orderMetadataByRuleset = "
						+ ro.isOrderMetadataByRuleset() + " ");
				sql.append(" WHERE MetadatenKonfigurationID = " + ro.getId()
						+ ";");
			}
			logger.debug(sql.toString());
			run.update(connection, sql.toString());
		} finally {
			MySQLHelper.closeConnection(connection);
		}
	}

	public static void deleteRuleset(Ruleset ro) throws SQLException {
		if (ro.getId() != null) {
			Connection connection = MySQLHelper.getInstance().getConnection();
			try {
				QueryRunner run = new QueryRunner();
				String sql = "DELETE FROM metadatenkonfigurationen WHERE MetadatenKonfigurationID = "
						+ ro.getId() + ";";
				logger.debug(sql);
				run.update(connection, sql);
			} finally {
				MySQLHelper.closeConnection(connection);
			}
		}
	}
}
