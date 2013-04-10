package de.sub.goobi.persistence.managers;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.goobi.beans.Ruleset;

import de.sub.goobi.persistence.apache.MySQLHelper;
import de.sub.goobi.persistence.apache.MySQLUtils;

class RulesetMysqlHelper {
	private static final Logger logger = Logger.getLogger(RulesetMysqlHelper.class);

	public static List<Ruleset> getRulesets(String order, String filter, Integer start, Integer count) throws SQLException {
		Connection connection = MySQLHelper.getInstance().getConnection();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM metadatenkonfigurationen");
		if (filter != null && !filter.isEmpty()) {
			sql.append(" WHERE " + filter);
		}
		if (order != null && !order.isEmpty()) {
			sql.append(" ORDER BY " + order);
		}
		if (start != null && count != null) {
			sql.append(" LIMIT " + start + ", " + count);
		}
		try {
			logger.debug(sql.toString());
			List<Ruleset> ret = new QueryRunner().query(connection, sql.toString(), RulesetManager.resultSetToRulesetListHandler);
			return ret;
		} finally {
			MySQLHelper.closeConnection(connection);
		}
	}
	
	
	   public static List<Ruleset> getAllRulesets() throws SQLException {
	        Connection connection = MySQLHelper.getInstance().getConnection();
	        StringBuilder sql = new StringBuilder();
	        sql.append("SELECT * FROM metadatenkonfigurationen");
	        try {
	            logger.debug(sql.toString());
	            List<Ruleset> ret = new QueryRunner().query(connection, sql.toString(), RulesetManager.resultSetToRulesetListHandler);
	            return ret;
	        } finally {
	            MySQLHelper.closeConnection(connection);
	        }
	    }

	public static int getRulesetCount(String order, String filter) throws SQLException {
		Connection connection = MySQLHelper.getInstance().getConnection();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT(MetadatenKonfigurationID) FROM metadatenkonfigurationen");
		if (filter != null && !filter.isEmpty()) {
			sql.append(" WHERE " + filter);
		}
		try {
			logger.debug(sql.toString());
			return new QueryRunner().query(connection, sql.toString(), MySQLUtils.resultSetToIntegerHandler);
		} finally {
			MySQLHelper.closeConnection(connection);
		}
	}

	public static Ruleset getRulesetForId(int rulesetId) throws SQLException {
		Connection connection = MySQLHelper.getInstance().getConnection();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM metadatenkonfigurationen WHERE MetadatenKonfigurationID = ? ");
		try {
			Object[] params = { rulesetId };
			logger.debug(sql.toString() + ", " + rulesetId);
			Ruleset ret = new QueryRunner().query(connection, sql.toString(), RulesetManager.resultSetToRulesetHandler, params);
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
				String propValues = "'" + StringEscapeUtils.escapeSql(ro.getTitel()) + "','" + StringEscapeUtils.escapeSql(ro.getDatei()) + "'," + ro.isOrderMetadataByRuleset() + "";
				sql.append("INSERT INTO metadatenkonfigurationen (");
				sql.append(propNames);
				sql.append(") VALUES (");
				sql.append(propValues);
				sql.append(")");
				
                logger.debug(sql.toString());
                Integer id = run.insert(connection, sql.toString(), MySQLUtils.resultSetToIntegerHandler);
                if (id != null) {
                    ro.setId(id);
                }
			} else {
				sql.append("UPDATE metadatenkonfigurationen SET ");
				sql.append("Titel = '" + StringEscapeUtils.escapeSql(ro.getTitel()) + "', ");
				sql.append("Datei = '" + StringEscapeUtils.escapeSql(ro.getDatei()) + "', ");
				sql.append("orderMetadataByRuleset = " + ro.isOrderMetadataByRuleset() + " ");
				sql.append(" WHERE MetadatenKonfigurationID = " + ro.getId() + ";");
				logger.debug(sql.toString());
				run.update(connection, sql.toString());
			}
		} finally {
			MySQLHelper.closeConnection(connection);
		}
	}

	public static void deleteRuleset(Ruleset ro) throws SQLException {
		if (ro.getId() != null) {
			Connection connection = MySQLHelper.getInstance().getConnection();
			try {
				QueryRunner run = new QueryRunner();
				String sql = "DELETE FROM metadatenkonfigurationen WHERE MetadatenKonfigurationID = " + ro.getId() + ";";
				logger.debug(sql);
				run.update(connection, sql);
			} finally {
				MySQLHelper.closeConnection(connection);
			}
		}
	}
}
