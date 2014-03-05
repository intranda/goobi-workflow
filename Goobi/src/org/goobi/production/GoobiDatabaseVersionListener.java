package org.goobi.production;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

import de.sub.goobi.persistence.managers.DatabaseVersion;

public class GoobiDatabaseVersionListener implements ServletContextListener {
    private static final Logger logger = Logger.getLogger(GoobiDatabaseVersionListener.class);

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
    }

    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        logger.debug("Checking for database version");
        int currentVersion = DatabaseVersion.getCurrentVersion();
        logger.debug("Current version is " + currentVersion);
        if (currentVersion == DatabaseVersion.EXPECTED_VERSION) {
            logger.debug("Database version is up to date.");
        } else {
            logger.warn("Database version is to old, updating schema from version " + currentVersion +" to current version " + DatabaseVersion.EXPECTED_VERSION);
            DatabaseVersion.updateDatabase(currentVersion);
        }
    }

}
