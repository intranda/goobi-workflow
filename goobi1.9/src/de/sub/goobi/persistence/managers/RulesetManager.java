package de.sub.goobi.persistence.managers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;
import org.goobi.beans.DatabaseObject;
import org.goobi.beans.Ruleset;

import de.sub.goobi.helper.exceptions.DAOException;

public class RulesetManager implements IManager {
	private static final Logger logger = Logger.getLogger(RulesetManager.class);

	public static Ruleset getRulesetById(int id) throws DAOException {
		Ruleset o = null;
		try {
			o = RulesetMysqlHelper.getRulesetById(id);
		} catch (SQLException e) {
			logger.error("error while getting ruleset with id " + id, e);
			throw new DAOException(e);
		}
		return o;
	}

	public static void saveRuleset(Ruleset ruleset) throws DAOException {
		try {
			RulesetMysqlHelper.saveRuleset(ruleset);
		} catch (SQLException e) {
			logger.error("error while saving ruleset with id " + ruleset.getId(), e);
			throw new DAOException(e);
		}
	}

	public static void deleteRuleset(Ruleset ruleset) throws DAOException {
		try {
			RulesetMysqlHelper.deleteRuleset(ruleset);
		} catch (SQLException e) {
			logger.error("error while deleting ruleset with id " + ruleset.getId(), e);
			throw new DAOException(e);
		}
	}

	public static List<Ruleset> getRulesets(String order, HashMap<String, String> filter, int start, int count) throws DAOException {
		List<Ruleset> answer = new ArrayList<Ruleset>();
		try {
			answer = RulesetMysqlHelper.getRulesets(order, filter, start, count);
		} catch (SQLException e) {
			logger.error("error while getting rulesets", e);
			throw new DAOException(e);
		}
		return answer;
	}

	public List<? extends DatabaseObject> getList(String order, HashMap<String, String> filter, int start, int count) throws DAOException {
		return (List<? extends DatabaseObject>) getRulesets(order, filter, start, count);
	}

	public int getHitSize(String order, HashMap<String, String> filter) throws DAOException {
		int num = 0;
		try {
			num = RulesetMysqlHelper.getRulesetCount(order, filter);
		} catch (SQLException e) {
			logger.error("error while getting ruleset hit size", e);
			throw new DAOException(e);
		}
		return num;
	}

	/* +++++++++++++++++++++++++++++++++++++++++ Converter +++++++++++++++++++++++++++++++++++++++++++++++ */

	public static Ruleset convert(ResultSet rs) throws SQLException {
		Ruleset r = new Ruleset();
		r.setId(rs.getInt("MetadatenKonfigurationID"));
		r.setTitel(rs.getString("Titel"));
		r.setDatei(rs.getString("Datei"));
		r.setOrderMetadataByRuleset(rs.getBoolean("orderMetadataByRuleset"));
		return r;
	}

	public static ResultSetHandler<Ruleset> resultSetToRulesetHandler = new ResultSetHandler<Ruleset>() {
		@Override
		public Ruleset handle(ResultSet rs) throws SQLException {
			if (rs.next()) {
				return convert(rs);
			}
			return null;
		}
	};

	public static ResultSetHandler<List<Ruleset>> resultSetToRulesetListHandler = new ResultSetHandler<List<Ruleset>>() {
		@Override
		public List<Ruleset> handle(ResultSet rs) throws SQLException {
			List<Ruleset> answer = new ArrayList<Ruleset>();

			while (rs.next()) {
				Ruleset o = convert(rs);
				if (o != null) {
					answer.add(o);
				}
			}
			return answer;
		}
	};

}
