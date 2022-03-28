package org.goobi.production;

import java.sql.SQLException;

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
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.sub.goobi.persistence.managers.DatabaseVersion;
import de.sub.goobi.persistence.managers.MySQLHelper;

public class GoobiDatabaseVersionListener implements ServletContextListener {
    private static final Logger logger = LogManager.getLogger(GoobiDatabaseVersionListener.class);

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
    }

    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        if (logger.isDebugEnabled()) {
            logger.debug("Checking for database version");
        }
        int currentVersion = DatabaseVersion.getCurrentVersion();
        if (logger.isDebugEnabled()) {
            logger.debug("Current version is " + currentVersion);
        }
        if (currentVersion == DatabaseVersion.EXPECTED_VERSION) {
            if (logger.isDebugEnabled()) {
                logger.debug("Database version is up to date.");
            }
        } else {
            logger.warn("Database version is too old, updating schema from version " + currentVersion + " to current version "
                    + DatabaseVersion.EXPECTED_VERSION);
            DatabaseVersion.updateDatabase(currentVersion);
        }

        checkIndexes();

        DatabaseVersion.checkIfEmptyDatabase();


        // TODO move to DatabaseVersion after merge into develop
        if(!DatabaseVersion.checkIfColumnExists("benutzer", "displayrulesetcolumn")) {
            try {
                DatabaseVersion.runSql("ALTER TABLE benutzer ADD COLUMN displayrulesetcolumn boolean default false");
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if(DatabaseVersion.checkIfColumnExists("projekte", "srurl")) {
            try {
                DatabaseVersion.runSql("alter table projekte drop column srurl;");
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        if(!DatabaseVersion.checkIfColumnExists("projekte", "iiifUrl")) {
            try {
                DatabaseVersion.runSql("alter table projekte add column iiifUrl varchar(255) default null;");
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if(!DatabaseVersion.checkIfColumnExists("projekte", "sruUrl")) {
            try {
                DatabaseVersion.runSql("alter table projekte add column sruUrl varchar(255) default null;");
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }


    // this method is executed on every startup and checks, if some mandatory indexes exist
    // if some indexes are missing, they are created
    private void checkIndexes() {
        if (MySQLHelper.isUsingH2()) {
            try {
                DatabaseVersion.runSql("CREATE INDEX IF NOT EXISTS priority_x_status ON schritte(Prioritaet, Bearbeitungsstatus) ");
                DatabaseVersion.runSql("CREATE INDEX IF NOT EXISTS stepstatus ON schritte(Bearbeitungsstatus) ");
            }catch(SQLException e) {
                logger.error(e);
            }
        } else {
            if (!DatabaseVersion.checkIfIndexExists("schritte", "priority_x_status")) {
                DatabaseVersion.createIndexOnTable("schritte", "priority_x_status", "Prioritaet, Bearbeitungsstatus", null);
            }
            if (!DatabaseVersion.checkIfIndexExists("schritte", "stepstatus")) {
                logger.info("Create index 'stepstatus' on table 'schritte'.");
                DatabaseVersion.createIndexOnTable("schritte", "stepstatus", "Bearbeitungsstatus", null);
            }
        }
    }

}
