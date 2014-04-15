package de.sub.goobi.persistence.managers;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.goobi.production.cli.helper.StringPair;

public class MetadataManager implements Serializable {

    private static final long serialVersionUID = -1352908576515114528L;

    private static final Logger logger = Logger.getLogger(MetadataManager.class);

    public static void deleteMetadata(int processId) {
        logger.trace("Removing metadata for process with id " + processId);
        try {
            MetadataMysqlHelper.removeMetadata(processId);
        } catch (SQLException e) {
            logger.error(e);
        }
    }

    public static void insertMetadata(int processId, List<StringPair> metadata) {
        logger.trace("Insert new metadata for process with id " + processId);
        try {
            MetadataMysqlHelper.insertMetadata(processId, metadata);
        } catch (SQLException e) {
            logger.error(e);
        }
    }

    public static void updateMetadata(int processId, List<StringPair> metadata) {
        logger.trace("Update metadata for process with id " + processId);
        try {
            MetadataMysqlHelper.removeMetadata(processId);
            MetadataMysqlHelper.insertMetadata(processId, metadata);
        } catch (SQLException e) {
            logger.error(e);
        }
    }

    public static List<String> getDistinctMetadataNames() {
        try {
          return MetadataMysqlHelper.getDistinctMetadataNames();
        } catch (SQLException e) {
            logger.error(e);
        }
        return new ArrayList<>();
    }
}
