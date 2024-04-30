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
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
package de.sub.goobi.persistence.managers;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;
import org.goobi.beans.DatabaseObject;
import org.goobi.beans.Institution;

import de.sub.goobi.helper.exceptions.DAOException;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ControllingManager implements IManager, Serializable {

    private static final long serialVersionUID = -8941047628267335304L;

    private static final String ERROR_MESSAGE = "error while getting sql results";

    @Override
    public int getHitSize(String order, String filter, Institution institution) throws DAOException {
        throw new NotImplementedException();
    }

    @Override
    public List<? extends DatabaseObject> getList(String order, String filter, Integer start, Integer count, Institution institution)
            throws DAOException {
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
            log.error(ERROR_MESSAGE, e);
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
            log.error(ERROR_MESSAGE, e);
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
            log.error(ERROR_MESSAGE, e);
        }
        return answer;
    }
}
