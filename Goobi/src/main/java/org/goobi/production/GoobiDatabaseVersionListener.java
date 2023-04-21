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

import de.sub.goobi.persistence.managers.DatabaseVersion;
import de.sub.goobi.persistence.managers.MySQLHelper;
import de.sub.goobi.persistence.managers.MySQLHelper.SQLTYPE;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GoobiDatabaseVersionListener implements ServletContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        // do nothing
    }

    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        if (log.isDebugEnabled()) {
            log.debug("Checking for database version");
        }
        int currentVersion = DatabaseVersion.getCurrentVersion();
        if (log.isDebugEnabled()) {
            log.debug("Current version is " + currentVersion);
        }
        if (currentVersion == DatabaseVersion.EXPECTED_VERSION) {
            if (log.isDebugEnabled()) {
                log.debug("Database version is up to date.");
            }
        } else {
            log.warn("Database version is too old, updating schema from version " + currentVersion + " to current version "
                    + DatabaseVersion.EXPECTED_VERSION);
            DatabaseVersion.updateDatabase(currentVersion);
        }
        checkIndexes();
        DatabaseVersion.checkIfEmptyDatabase();

        if (!DatabaseVersion.checkIfTableExists("api_token")) {

            //            CREATE TABLE api_token (
            //                    id INT(11)  unsigned NOT NULL AUTO_INCREMENT,
            //                    user_id INT(11),
            //                    token_name VARCHAR(255),
            //                    token_description VARCHAR(255),
            //                    PRIMARY KEY (id)
            //                ) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8;
            //
            //                CREATE TABLE api_token_method (
            //                    id INT(11)  unsigned NOT NULL AUTO_INCREMENT,
            //                    token_id INT(11),
            //                    method_type VARCHAR(255),
            //                    method_description VARCHAR(255),
            //                    method_url VARCHAR(255),
            //                    selected tinyint(1),
            //                    PRIMARY KEY (id),
            //                    KEY `tokenid` (`token_id`)
            //                ) ENGINE = InnoDB DEFAULT CHARACTER SET = utf8;

        }

    }

    // this method is executed on every startup and checks, if some mandatory indexes exist
    // if some indexes are missing, they are created
    private void checkIndexes() {
        if (MySQLHelper.getInstance().getSqlType() == SQLTYPE.H2) {
            try {
                DatabaseVersion.runSql("CREATE INDEX IF NOT EXISTS institution_id ON projekte(institution_id) ");
                DatabaseVersion.runSql("CREATE INDEX IF NOT EXISTS priority_x_status ON schritte(Prioritaet, Bearbeitungsstatus) ");
                DatabaseVersion.runSql("CREATE INDEX IF NOT EXISTS stepstatus ON schritte(Bearbeitungsstatus) ");
                DatabaseVersion.runSql("CREATE INDEX IF NOT EXISTS automatic_tasks ON schritte(titel, typAutomatisch) ");
            } catch (SQLException e) {
                log.error(e);
            }
        } else {
            if (!DatabaseVersion.checkIfIndexExists("projekte", "institution_id")) {
                DatabaseVersion.createIndexOnTable("projekte", "institution_id", "institution_id", null);
            }

            if (!DatabaseVersion.checkIfIndexExists("schritte", "priority_x_status")) {
                DatabaseVersion.createIndexOnTable("schritte", "priority_x_status", "Prioritaet, Bearbeitungsstatus", null);
            }
            if (!DatabaseVersion.checkIfIndexExists("schritte", "stepstatus")) {
                log.info("Create index 'stepstatus' on table 'schritte'.");
                DatabaseVersion.createIndexOnTable("schritte", "stepstatus", "Bearbeitungsstatus", null);
            }

            if (!DatabaseVersion.checkIfIndexExists("schritte", "automatic_tasks")) {
                DatabaseVersion.createIndexOnTable("schritte", "automatic_tasks", "titel, typAutomatisch", null);
            }

        }
    }

}
