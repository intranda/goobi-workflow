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
        int currentVersion;
        try {
            currentVersion = DatabaseVersion.getCurrentVersion();
        } catch (SQLException e) {
            log.error("Unable to detect current database version, skipping database upgrades!", e);
            return;
        }

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


        if (!DatabaseVersion.checkIfColumnExists("mq_results", "objects")) {
            try {
                // extend mq_results table
                DatabaseVersion.runSql("alter table mq_results add column id INT(11) unsigned NOT NULL AUTO_INCREMENT PRIMARY KEY");
                DatabaseVersion.runSql("alter table mq_results add column processid INT(11) default 0");
                DatabaseVersion.runSql("alter table mq_results add column stepid INT(11) default 0");
                DatabaseVersion.runSql("alter table mq_results add column scriptName VARCHAR(255)");
                DatabaseVersion.runSql("alter table mq_results add column objects INT(11) default 0");
                DatabaseVersion.runSql("alter table mq_results add column ticketType VARCHAR(255)");
                DatabaseVersion.runSql("alter table mq_results add column ticketName VARCHAR(255)");

                // move external_mq_results to mq_results
                StringBuilder sb = new StringBuilder();
                sb.append("insert into mq_results (processid, stepid, time, scriptName, status, ticketType, ticketName) ");
                sb.append("select ProzesseID, SchritteID, time, scriptName, 'ERROR_DLQ', 'external_step', scriptName from ( ");
                sb.append("select * from external_mq_results)x ");
                DatabaseVersion.runSql(sb.toString());
                DatabaseVersion.runSql("drop table external_mq_results");

                // extract additional fields from ticket data
                sb = new StringBuilder();
                sb.append("update mq_results mq set processid = (select JSON_VALUE(original_message, '$.processId')) ");
                DatabaseVersion.runSql(sb.toString());

                sb = new StringBuilder();
                sb.append("update mq_results mq set ticketType = (select JSON_VALUE(original_message, '$.processId'))");
                DatabaseVersion.runSql(sb.toString());

                sb = new StringBuilder();
                sb.append("update mq_results mq set ticketName = (select JSON_VALUE(original_message, '$.stepName'))");

                DatabaseVersion.runSql(sb.toString());

                // generate entries for all finished automatic mq tasks
                sb = new StringBuilder();
                sb.append("insert into mq_results (processid, stepid, time, scriptName, status, ticketType, ticketName) ");
                sb.append("select ProzesseID, SchritteID, BearbeitungsEnde, scriptName1, 'DONE', 'generic_automatic_step', ");
                sb.append("titel from schritte where typAutomatisch = true and messageQueue != 'NO_QUEUE' and Bearbeitungsstatus = 3; ");
                DatabaseVersion.runSql(sb.toString());

                // get number of objects for each ticket
                sb = new StringBuilder();
                sb.append("update mq_results set objects = ");
                sb.append("(select sortHelperImages from prozesse where prozesse.ProzesseID = mq_results.processid)");
                DatabaseVersion.runSql(sb.toString());

                sb = new StringBuilder();
                sb.append("update mq_results set ticketType = (select JSON_VALUE(original_message, '$.taskName')) where ticketType is null ");
                DatabaseVersion.runSql(sb.toString());

                sb = new StringBuilder();
                sb.append("update mq_results set ticketName = ticketType where ticketName is null");
                DatabaseVersion.runSql(sb.toString());


                sb = new StringBuilder();
                sb.append("update mq_results set ticketName = scriptName where ticketName is null");
                DatabaseVersion.runSql(sb.toString());

            } catch (SQLException e) {
                log.error(e);
            }
        }

        if (!DatabaseVersion.checkIfIndexExists("vocabulary_record_data", "definition_id")) {
            DatabaseVersion.createIndexOnTable("vocabulary_record_data", "definition_id", "definition_id", null);
            DatabaseVersion.createIndexOnTable("vocabulary_record_data", "record_id", "record_id", null);

        }

        if (!DatabaseVersion.checkIfColumnExists("repository", "testmode")) {
            try {
                DatabaseVersion.runSql("alter table repository add column testmode tinyint(1);");
                DatabaseVersion.runSql("alter table repository add column start_date varchar(255) DEFAULT NULL;");
                DatabaseVersion.runSql("alter table repository add column end_date varchar(255) DEFAULT NULL;");
            } catch (SQLException e) {
                log.error(e);
            }

        }

        if (!DatabaseVersion.checkIfColumnExists("benutzer", "displayNumberOfImagesColumn")) {
            try {
                DatabaseVersion.runSql("ALTER TABLE benutzer ADD COLUMN displayNumberOfImagesColumn tinyint(1);");
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
