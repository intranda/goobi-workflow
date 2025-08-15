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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.ResultSetHandler;
import org.goobi.beans.DatabaseObject;
import org.goobi.beans.Docket;
import org.goobi.beans.Institution;

import de.sub.goobi.helper.exceptions.DAOException;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class DocketManager implements IManager, Serializable {
    private static final long serialVersionUID = 7112845186301877542L;

    public static Docket getDocketById(int id) throws DAOException {
        Docket o = null;
        try {
            o = DocketMysqlHelper.getDocketById(id);
        } catch (SQLException e) {
            log.error("error while getting Docket with id " + id, e);
            throw new DAOException(e);
        }
        return o;
    }

    public static void saveDocket(Docket o) throws DAOException {
        try {
            DocketMysqlHelper.saveDocket(o);
        } catch (SQLException e) {
            log.error("error while saving Docket with id " + o.getId(), e);
            throw new DAOException(e);
        }
    }

    public static void deleteDocket(Docket o) throws DAOException {
        try {
            DocketMysqlHelper.deleteDocket(o);
        } catch (SQLException e) {
            log.error("error while deleting Docket with id " + o.getId(), e);
            throw new DAOException(e);
        }
    }

    public static List<Docket> getDockets(String order, String filter, Integer start, Integer count, Institution institution) throws DAOException {
        List<Docket> answer = new ArrayList<>();
        try {
            answer = DocketMysqlHelper.getDockets(order, filter, start, count, institution);
        } catch (SQLException e) {
            log.error("error while getting Dockets", e);
            throw new DAOException(e);
        }
        return answer;
    }

    @Override
    public List<? extends DatabaseObject> getList(String order, String filter, Integer start, Integer count, Institution institution)
            throws DAOException {
        return getDockets(order, filter, start, count, institution);
    }

    @Override
    public int getHitSize(String order, String filter, Institution institution) throws DAOException {
        int num = 0;
        try {
            num = DocketMysqlHelper.getDocketCount(filter, institution);
        } catch (SQLException e) {
            log.error("error while getting Docket hit size", e);
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

    public static final ResultSetHandler<Docket> RESULTSET_TO_DOCKET_HANDLER = new ResultSetHandler<>() {
        @Override
        public Docket handle(ResultSet rs) throws SQLException {
            try {
                if (rs.next()) {
                    return convert(rs);
                }
            } finally {
                rs.close();
            }
            return null;
        }
    };

    public static final ResultSetHandler<List<Docket>> RESULTSET_TO_DOCKET_LIST_HANDLER = new ResultSetHandler<>() {
        @Override
        public List<Docket> handle(ResultSet rs) throws SQLException {
            List<Docket> answer = new ArrayList<>();
            try {
                while (rs.next()) {
                    Docket o = convert(rs);
                    answer.add(o);
                }
            } finally {
                rs.close();
            }
            return answer;
        }
    };

    public static List<Docket> getAllDockets() {
        List<Docket> answer = new ArrayList<>();
        try {
            answer = DocketMysqlHelper.getAllDockets();
        } catch (SQLException e) {
            log.error("error while getting Dockets", e);
        }

        return answer;
    }

    @Override
    public List<Integer> getIdList(String order, String filter, Institution institution) {
        return null;
    }

    public static Docket getDocketByName(String name) throws DAOException {
        Docket o = null;
        try {
            o = DocketMysqlHelper.getDocketByName(name);
        } catch (SQLException e) {
            log.error("error while getting Docket with name " + name, e);
            throw new DAOException(e);
        }
        return o;
    }

}
