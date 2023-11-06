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

        // TODO move this
        if (!DatabaseVersion.checkIfIndexExists("vocabulary_record_data", "definition_id")) {
            DatabaseVersion.createIndexOnTable("vocabulary_record_data", "definition_id", "definition_id", null);
            DatabaseVersion.createIndexOnTable("vocabulary_record_data", "record_id", "record_id", null);
        }

        checkIndexes();
        DatabaseVersion.checkIfEmptyDatabase();
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
