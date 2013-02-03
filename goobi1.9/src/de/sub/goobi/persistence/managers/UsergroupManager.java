package de.sub.goobi.persistence.managers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;
import org.goobi.beans.DatabaseObject;
import org.goobi.beans.User;
import org.goobi.beans.Usergroup;

import de.sub.goobi.helper.exceptions.DAOException;

public class UsergroupManager implements IManager {
	private static final Logger logger = Logger.getLogger(UsergroupManager.class);

	public static Usergroup getUsergroupById(int id) throws DAOException {
		Usergroup o = null;
		try {
			o = UsergroupMysqlHelper.getUsergroupById(id);
		} catch (SQLException e) {
			logger.error("error while getting Usergroup with id " + id, e);
			throw new DAOException(e);
		}
		return o;
	}

	public static void saveUsergroup(Usergroup o) throws DAOException {
		try {
			UsergroupMysqlHelper.saveUsergroup(o);
		} catch (SQLException e) {
			logger.error("error while saving Usergroup with id " + o.getId(), e);
			throw new DAOException(e);
		}
	}

	public static void deleteUsergroup(Usergroup o) throws DAOException {
		try {
			UsergroupMysqlHelper.deleteUsergroup(o);
		} catch (SQLException e) {
			logger.error("error while deleting Usergroup with id " + o.getId(), e);
			throw new DAOException(e);
		}
	}

	public static List<Usergroup> getUsergroups(String order, String filter, Integer start, Integer count) throws DAOException {
		List<Usergroup> answer = new ArrayList<Usergroup>();
		try {
			answer = UsergroupMysqlHelper.getUsergroups(order, filter, start, count);
		} catch (SQLException e) {
			logger.error("error while getting Usergroups", e);
			throw new DAOException(e);
		}
		return answer;
	}

	public static List<Usergroup> getUsergroupsForUser(User user) throws DAOException{
		List<Usergroup> answer = new ArrayList<Usergroup>();
		try {
			answer = UsergroupMysqlHelper.getUsergroupsForUser(user);
		} catch (SQLException e) {
			logger.error("error while getting Usergroups", e);
			throw new DAOException(e);
		}
		return answer;
	}
	
	public List<? extends DatabaseObject> getList(String order, String filter, Integer start, Integer count) throws DAOException {
		return (List<? extends DatabaseObject>) getUsergroups(order, filter, start, count);
	}

	public int getHitSize(String order, String filter) throws DAOException {
		int num = 0;
		try {
			num = UsergroupMysqlHelper.getUsergroupCount(order, filter);
		} catch (SQLException e) {
			logger.error("error while getting Usergroup hit size", e);
			throw new DAOException(e);
		}
		return num;
	}

	/* +++++++++++++++++++++++++++++++++++++++++ Converter +++++++++++++++++++++++++++++++++++++++++++++++ */

	public static Usergroup convert(ResultSet rs) throws SQLException {
		Usergroup r = new Usergroup();
		r.setId(rs.getInt("BenutzergruppenID"));
		r.setTitel(rs.getString("titel"));
		r.setBerechtigung(rs.getInt("berechtigung"));
		return r;
	}

	public static ResultSetHandler<Usergroup> resultSetToUsergroupHandler = new ResultSetHandler<Usergroup>() {
		@Override
		public Usergroup handle(ResultSet rs) throws SQLException {
			if (rs.next()) {
				return convert(rs);
			}
			return null;
		}
	};

	public static ResultSetHandler<List<Usergroup>> resultSetToUsergroupListHandler = new ResultSetHandler<List<Usergroup>>() {
		@Override
		public List<Usergroup> handle(ResultSet rs) throws SQLException {
			List<Usergroup> answer = new ArrayList<Usergroup>();

			while (rs.next()) {
				Usergroup o = convert(rs);
				if (o != null) {
					answer.add(o);
				}
			}
			return answer;
		}
	};



}
