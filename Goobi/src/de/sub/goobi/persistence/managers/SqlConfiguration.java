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

import de.sub.goobi.config.ConfigMain;

public class SqlConfiguration {

    private String username = "goobi";
    private String password = "CHANGEIT";
    private String url = "jdbc:mysql://localhost/goobi?autoReconnect=true&amp;autoReconnectForPools=true";
    private String driverClassName = "com.mysql.jdbc.Driver";
    private long maxWait = -1l;
    private boolean removeAbandoned = false;
    private int removeAbandonedTimeout = 300;
    private int initialSize = 1;
    private int maxActive = 20;

    private static SqlConfiguration sqlConfiguration = new SqlConfiguration();

    private SqlConfiguration() {
        username = ConfigMain.getParameter("DatabaseUsername", "goobi");
        password = ConfigMain.getParameter("DatabasePassword", "goobi");
        url = ConfigMain.getParameter("DatabaseConnection", "jdbc:mysql://localhost/goobi?autoReconnect=true&amp;autoReconnectForPools=true");
        driverClassName = ConfigMain.getParameter("DatabaseDriverClassName", "com.mysql.jdbc.Driver");
        initialSize = ConfigMain.getIntParameter("DatabaseConnectionInitialSize", 1);
        maxActive = ConfigMain.getIntParameter("DatabaseConnectionMaxSize", 20);
        maxWait = ConfigMain.getLongParameter("DatabaseConnectionTimeout", 30000);
        removeAbandoned = ConfigMain.getBooleanParameter("DatabaseRemoveAbandonedConnection", false);
        removeAbandonedTimeout = ConfigMain.getIntParameter("DatabaseRemoveAbandonedConnectionTimeout", 300);
    }

    public static SqlConfiguration getInstance() {
        return sqlConfiguration;
    }

    public String getDbDriverName() {
        return this.driverClassName;
    }

    public String getDbUser() {
        return this.username;
    }

    public String getDbPassword() {
        return this.password;
    }

    public String getDbURI() {
        return this.url;
    }

    public int getDbPoolMinSize() {
        return this.initialSize;
    }

    public int getDbPoolMaxSize() {
        return this.maxActive;
    }

    public long getMaxWait() {
        return maxWait;
    }

    public int getRemoveAbandonedTimeout() {
        return removeAbandonedTimeout;
    }

    public boolean isRemoveAbandoned() {
        return removeAbandoned;
    }

}
