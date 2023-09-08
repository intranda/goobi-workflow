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

        // TODO move this to DatabaseVersion after merge
        if (!DatabaseVersion.checkIfTableExists("goobiscript_template")) {
            StringBuilder sql = new StringBuilder();
            sql.append("CREATE TABLE goobiscript_template ( ");
            sql.append("id INT(11)  unsigned NOT NULL AUTO_INCREMENT, ");
            sql.append("title VARCHAR(255), ");
            sql.append("description VARCHAR(255), ");
            sql.append("goobiscript text, ");
            sql.append("PRIMARY KEY (id) ");
            sql.append(") ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4; ");
            try {
                DatabaseVersion.runSql(sql.toString());
            } catch (SQLException e) {
                log.error(e);
            }
        }

        if (!DatabaseVersion.checkIfTableExists("export_history")) {
            StringBuilder sql = new StringBuilder();
            sql.append("CREATE TABLE export_history ( ");
            sql.append("id INT(11)  unsigned NOT NULL AUTO_INCREMENT, ");
            sql.append("timestamp timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, ");
            sql.append("record_id int NOT NULL, ");
            sql.append("record_identifier varchar(255) NOT NULL, ");
            sql.append("record_title text, ");
            sql.append("repository_id varchar(255) NOT NULL, ");
            sql.append("status varchar(255) NOT NULL, ");
            sql.append("message varchar(255) DEFAULT NULL, ");
            sql.append("PRIMARY KEY (id) ");
            sql.append(") ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4; ");
            try {
                DatabaseVersion.runSql(sql.toString());
            } catch (SQLException e) {
                log.error(e);
            }
        }

        if (!DatabaseVersion.checkIfTableExists("job")) {
            StringBuilder sql = new StringBuilder();
            sql.append("CREATE TABLE job ( ");
            sql.append("id INT(11)  unsigned NOT NULL AUTO_INCREMENT, ");
            sql.append("timestamp timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, ");
            sql.append("status varchar(64) NOT NULL DEFAULT 'WAITING', ");
            sql.append("repository_id int NOT NULL, ");
            sql.append("repository_name varchar(255) NOT NULL, ");
            sql.append("message varchar(255) DEFAULT NULL, ");
            sql.append("PRIMARY KEY (id) ");
            sql.append(") ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4; ");
            try {
                DatabaseVersion.runSql(sql.toString());
            } catch (SQLException e) {
                log.error(e);
            }
        }
        if (!DatabaseVersion.checkIfTableExists("record")) {
            StringBuilder sql = new StringBuilder();
            sql.append("CREATE TABLE record ( ");
            sql.append("id INT(11)  unsigned NOT NULL AUTO_INCREMENT, ");
            sql.append("repository_id int NOT NULL, ");
            sql.append("title text, ");
            sql.append("creator text, ");
            sql.append("timestamp timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, ");
            sql.append("repository_datestamp datetime NOT NULL, ");
            sql.append("setSpec text, ");
            sql.append("identifier text NOT NULL, ");
            sql.append("job_id int NOT NULL, ");
            sql.append("source text, ");
            sql.append("exported text, ");
            sql.append("exported_datestamp datetime DEFAULT NULL, ");
            sql.append("subquery varchar(255) DEFAULT NULL, ");
            sql.append("PRIMARY KEY (id), ");
            sql.append("KEY identifier (identifier(12)), ");
            sql.append("KEY exported_datestamp (exported_datestamp) ");
            sql.append(") ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4; ");
            try {
                DatabaseVersion.runSql(sql.toString());
            } catch (SQLException e) {
                log.error(e);
            }
        }

        if (!DatabaseVersion.checkIfTableExists("repository")) {
            StringBuilder sql = new StringBuilder();
            sql.append("CREATE TABLE repository ( ");
            sql.append("id INT(11)  unsigned NOT NULL AUTO_INCREMENT, ");
            sql.append("name varchar(255) NOT NULL, ");
            sql.append("base_url text NOT NULL, ");
            sql.append("export_folder varchar(255) DEFAULT NULL, ");
            sql.append("script_path varchar(255) DEFAULT NULL, ");
            sql.append("last_harvest timestamp NULL DEFAULT NULL, ");
            sql.append("freq int NOT NULL DEFAULT '6', ");
            sql.append("delay int NOT NULL DEFAULT '0', ");
            sql.append("enabled tinyint(1) NOT NULL DEFAULT '1', ");
            sql.append("type text, ");
            sql.append("PRIMARY KEY (id) ");
            sql.append(") ENGINE = InnoDB DEFAULT CHARACTER SET = utf8mb4; ");
            try {
                DatabaseVersion.runSql(sql.toString());
            } catch (SQLException e) {
                log.error(e);
            }
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
