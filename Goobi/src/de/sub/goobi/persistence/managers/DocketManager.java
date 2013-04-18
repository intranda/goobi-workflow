package de.sub.goobi.persistence.managers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;
import org.goobi.beans.DatabaseObject;
import org.goobi.beans.Docket;

import de.sub.goobi.helper.exceptions.DAOException;

public class DocketManager implements IManager {
	private static final Logger logger = Logger.getLogger(DocketManager.class);

	public static Docket getDocketById(int id) throws DAOException {
		Docket o = null;
		try {
			o = DocketMysqlHelper.getDocketById(id);
		} catch (SQLException e) {
			logger.error("error while getting Docket with id " + id, e);
			throw new DAOException(e);
		}
		return o;
	}

	public static void saveDocket(Docket o) throws DAOException {
		try {
			DocketMysqlHelper.saveDocket(o);
		} catch (SQLException e) {
			logger.error("error while saving Docket with id " + o.getId(), e);
			throw new DAOException(e);
		}
	}

	public static void deleteDocket(Docket o) throws DAOException {
		try {
			DocketMysqlHelper.deleteDocket(o);
		} catch (SQLException e) {
			logger.error("error while deleting Docket with id " + o.getId(), e);
			throw new DAOException(e);
		}
	}

	public static List<Docket> getDockets(String order, String filter, Integer start, Integer count) throws DAOException {
		List<Docket> answer = new ArrayList<Docket>();
		try {
			answer = DocketMysqlHelper.getDockets(order, filter, start, count);
		} catch (SQLException e) {
			logger.error("error while getting Dockets", e);
			throw new DAOException(e);
		}
		return answer;
	}

	public List<? extends DatabaseObject> getList(String order, String filter, Integer start, Integer count) throws DAOException {
		return (List<? extends DatabaseObject>) getDockets(order, filter, start, count);
	}

	public int getHitSize(String order, String filter) throws DAOException {
		int num = 0;
		try {
			num = DocketMysqlHelper.getDocketCount(order, filter);
		} catch (SQLException e) {
			logger.error("error while getting Docket hit size", e);
			throw new DAOException(e);
		}
		return num;
	}

	/* +++++++++++++++++++++++++++++++++++++++++ Converter +++++++++++++++++++++++++++++++++++++++++++++++ */

	public static Docket convert(ResultSet rs) throws SQLException {
		Docket r = new Docket();
		r.setId(rs.getInt("docketID"));
		r.setName(rs.getString("name"));
		r.setFile(rs.getString("file"));
		return r;
	}

	public static ResultSetHandler<Docket> resultSetToDocketHandler = new ResultSetHandler<Docket>() {
		@Override
		public Docket handle(ResultSet rs) throws SQLException {
			if (rs.next()) {
				return convert(rs);
			}
			return null;
		}
	};

	public static ResultSetHandler<List<Docket>> resultSetToDocketListHandler = new ResultSetHandler<List<Docket>>() {
		@Override
		public List<Docket> handle(ResultSet rs) throws SQLException {
			List<Docket> answer = new ArrayList<Docket>();

			while (rs.next()) {
				Docket o = convert(rs);
				if (o != null) {
					answer.add(o);
				}
			}
			return answer;
		}
	};

    public static List<Docket> getAllDockets() {
        List<Docket> answer = new ArrayList<Docket>();
        try {
            answer = DocketMysqlHelper.getAllDockets();
        } catch (SQLException e) {
            logger.error("error while getting Dockets", e);
        }
        return answer;
    }
}
