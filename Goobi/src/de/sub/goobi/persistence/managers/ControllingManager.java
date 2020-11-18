package de.sub.goobi.persistence.managers;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.goobi.beans.DatabaseObject;
import org.goobi.beans.Institution;

import de.sub.goobi.helper.exceptions.DAOException;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ControllingManager implements IManager, Serializable {

    private static final long serialVersionUID = -8941047628267335304L;

    @Override
    public int getHitSize(String order, String filter, Institution institution) throws DAOException {
        throw new NotImplementedException();
    }

    @Override
    public List<? extends DatabaseObject> getList(String order, String filter, Integer start, Integer count, Institution institution) throws DAOException {
        throw new NotImplementedException();
    }

    @Override
    public List<Integer> getIdList(String order, String filter, Institution institution) {
        throw new NotImplementedException();
    }

    /**
     * search for a query and return the result values as a list of Object[] for each row.
     * 
     * @param sql
     * @return
     * @throws SQLException
     */

    public static List<Object[]> getResultsAsObjectList(String sql) {
        List<Object[]> answer = null;
        try {
            answer = ControllingMysqlHelper.getResultsAsObjectList(sql);
        } catch (SQLException e) {
            log.error("error while getting sql results", e);
        }
        return answer;
    }

    /**
     * search for a query and return the first result as a map. The column names are used as keys
     * 
     * @param sql
     * @return
     * @throws SQLException
     */

    public static Map<String, String> getSingleResultAsMap(String sql) {
        try {
            return ControllingMysqlHelper.getSingleResultAsMap(sql);
        } catch (SQLException e) {
            log.error("error while getting sql results", e);
        }
        return null;
    }

    /**
     * search for a query and return the result as a list of maps. Each result row is converted into a map, using the column names as keys and all
     * rows are returned as a list. The order of the entries in the list is the same as in the sql result
     * 
     * @param sql
     * @return
     * @throws SQLException
     */

    public static List<Map<String, String>> getResultsAsMaps(String sql) {
        List<Map<String, String>> answer = null;
        try {
            answer = ControllingMysqlHelper.getResultsAsMaps(sql);
        } catch (SQLException e) {
            log.error("error while getting sql results", e);
        }
        return answer;
    }
}
