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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;

import lombok.extern.log4j.Log4j2;

/**
 * 
 * @author Robert Sehr
 * 
 */
@Log4j2
public class ConnectionManager implements Serializable {

    private static final long serialVersionUID = 5383856824401199510L;

    private transient DataSource ds = null;
    @SuppressWarnings("rawtypes")
    private static GenericObjectPool pool = null; // NOSONAR

    public ConnectionManager() {
        connectToDB();
    }

    private void connectToDB() {
        if (log.isTraceEnabled()) {
            log.trace("Trying to connect to database...");
        }
        try {
            Context ctx = new InitialContext();
            ds = (DataSource) ctx.lookup("java:comp/env/goobi");
            if (log.isTraceEnabled()) {
                log.trace("Connection attempt to database succeeded.");
            }
        } catch (NamingException e) {
            log.error("Error when attempting to connect to DB ", e);
        }
    }

    @SuppressWarnings("rawtypes")
    public static void printDriverStats() throws Exception {
        ObjectPool connectionPool = ConnectionManager.pool;
        if (log.isTraceEnabled()) {
            log.trace("NumActive: " + connectionPool.getNumActive());
            log.trace("NumIdle: " + connectionPool.getNumIdle());
        }
    }

    /**
     * getNumLockedProcesses - gets the number of currently locked processes on the MySQL db.
     * 
     * @return Number of locked processes
     */
    public int getNumLockedProcesses() {
        int numLockedConnections = 0;
        Connection con = null;
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        try {
            con = this.ds.getConnection(); //NOSONAR as it is closed in the finally statement
            preparedStatement = con.prepareStatement("SHOW PROCESSLIST"); //NOSONAR as it is closed in the finally statement
            rs = preparedStatement.executeQuery(); //NOSONAR as it is closed in the finally statement
            while (rs.next()) {
                if (rs.getString("State") != null && "Locked".equals(rs.getString("State"))) {
                    numLockedConnections++;
                }
            }
        } catch (SQLException e) {
            if (log.isTraceEnabled()) {
                log.trace("Failed to get get Locked Connections - Exception: " + e.toString());
            }
        } finally {
            try {
                rs.close();
                preparedStatement.close();
                con.close();
            } catch (java.sql.SQLException ex) {
                log.error(ex.toString());
            }
        }
        return numLockedConnections;
    }

    public DataSource getDataSource() {
        return this.ds;
    }

}
