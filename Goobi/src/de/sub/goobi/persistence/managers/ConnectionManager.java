package de.sub.goobi.persistence.managers;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- http://launchpad.net/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.log4j.Logger;

/**
 * 
 * @author Robert Sehr
 * 
 */
public class ConnectionManager {

    private static final Logger logger = Logger.getLogger(ConnectionManager.class);

    private DataSource ds = null;
    private static GenericObjectPool _pool = null;

    /**
     * @param config configuration from an XML file.
     */
    public ConnectionManager() {
        try {
            //			connectToDB(config);
            connectToDB();
        } catch (Exception e) {
            logger.error("Failed to construct ConnectionManager", e);
        }
    }

    /**
     * destructor
     */
    @Override
    protected void finalize() {
        if (logger.isDebugEnabled()) {
            logger.debug("Finalizing ConnectionManager");
        }
        try {
            super.finalize();
        } catch (Throwable ex) {
            logger.error("ConnectionManager finalize failed to disconnect from mysql: ", ex);
        }
    }

    private void connectToDB() {
        if (logger.isDebugEnabled()) {
            logger.debug("Trying to connect to database...");
        }
        try {
            Context ctx = new InitialContext();
            ds = (DataSource) ctx.lookup("java:comp/env/goobi");
            if (logger.isDebugEnabled()) {
                logger.debug("Connection attempt to database succeeded.");
            }
        } catch (Exception e) {
            logger.error("Error when attempting to connect to DB ", e);
        }
    }

    public static void printDriverStats() throws Exception {
        ObjectPool connectionPool = ConnectionManager._pool;
        if (logger.isDebugEnabled()) {
            logger.debug("NumActive: " + connectionPool.getNumActive());
            logger.debug("NumIdle: " + connectionPool.getNumIdle());
        }
    }

    /**
     * getNumLockedProcesses - gets the number of currently locked processes on the MySQL db
     * 
     * @return Number of locked processes
     */
    public int getNumLockedProcesses() {
        int num_locked_connections = 0;
        Connection con = null;
        PreparedStatement p_stmt = null;
        ResultSet rs = null;
        try {
            con = this.ds.getConnection();
            p_stmt = con.prepareStatement("SHOW PROCESSLIST");
            rs = p_stmt.executeQuery();
            while (rs.next()) {
                if (rs.getString("State") != null && rs.getString("State").equals("Locked")) {
                    num_locked_connections++;
                }
            }
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Failed to get get Locked Connections - Exception: " + e.toString());
            }
        } finally {
            try {
                rs.close();
                p_stmt.close();
                con.close();
            } catch (java.sql.SQLException ex) {
                logger.error(ex.toString());
            }
        }
        return num_locked_connections;
    }

    public DataSource getDataSource() {
        return this.ds;
    }

}
