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
 */

package de.sub.goobi.persistence.managers;

import java.io.Serializable;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import org.goobi.beans.DatabaseObject;
import org.goobi.beans.Institution;

import de.sub.goobi.helper.exceptions.DAOException;
import io.goobi.workflow.harvester.beans.Record;
import io.goobi.workflow.harvester.repository.Repository;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class HarvesterRepositoryManager implements IManager, Serializable {

    private static final long serialVersionUID = 5591131855633585870L;

    @Override
    public int getHitSize(String order, String filter, Institution institution) throws DAOException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public List<? extends DatabaseObject> getList(String order, String filter, Integer start, Integer count, Institution institution)
            throws DAOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Integer> getIdList(String order, String filter, Institution institution) {
        // TODO Auto-generated method stub
        return null;
    }

    public static Repository getRepository(String repositoryId) {
        try {
            return HarvesterRepositoryMysqlHelper.getRepository(repositoryId);
        } catch (SQLException e) {
            log.error(e);
        }
        return null;
    }



    public static void changeStatusOfRepository(Repository repository)  {
        //        Connection connection = null;
        //        try {
        //            connection = getConnection();
        //            int i = 0;
        //            if (repository.isEnabled()) {
        //                i = 1;
        //            }
        //            Object[] param = { repository.getId() };
        //            new QueryRunner().update(connection, "UPDATE repository SET enabled=" + i + " WHERE id= ?", param);
        //        } catch (SQLException e) {
        //            throw new DBException(e);
        //        } finally {
        //            if (connection != null) {
        //                try {
        //                    closeConnection(connection);
        //                } catch (SQLException e) {
        //                    throw new DBException(e);
        //                }
        //            }
        //        }
    }



    public static int addRecords(List<Record> recList, boolean allowUpdates) {

        // TODO

        return 0;
    }


    public static Timestamp getLastHarvest(String repositoryId)  {
        //        Connection connection = null;
        //        try {
        //            connection = getConnection();
        //            Object[] param = { repositoryId };
        //            String sql = "SELECT last_harvest FROM repository WHERE id=?";
        //            Timestamp t = new QueryRunner().query(connection, sql, new ResultSetHandler<Timestamp>() {
        //                @Override
        //                public Timestamp handle(ResultSet rs) throws SQLException {
        //                    if (!rs.next()) {
        //                        return null;
        //                    }
        //                    Timestamp timestamp = rs.getTimestamp("last_harvest");
        //
        //                    return timestamp;
        //                }
        //            }, param);
        //            return t;
        //        } catch (SQLException e) {
        //            throw new DBException(e);
        //        } finally {
        //            if (connection != null) {
        //                try {
        //                    closeConnection(connection);
        //                } catch (SQLException e) {
        //                    throw new DBException(e);
        //                }
        //            }
        //        }
        return null;
    }

    public static List<String> getExistingIdentifier(List<String> outputChannel) {
        // TODO Auto-generated method stub
        return null;
    }

}
