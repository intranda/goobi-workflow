package de.sub.goobi.persistence.managers;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *          - http://www.intranda.com
 *          - http://digiverso.com 
 *          - http://www.goobi.org
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
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.goobi.production.cli.helper.StringPair;
import org.goobi.production.flow.statistics.hibernate.SearchIndexField;

import de.sub.goobi.config.ConfigurationHelper;

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

    public static void insertMetadata(int processId, Map<String, List<String>> metadata) {

        generateIndexFields(metadata);

        logger.trace("Insert new metadata for process with id " + processId);
        try {
            MetadataMysqlHelper.insertMetadata(processId, metadata);
        } catch (SQLException e) {
            logger.error(e);
        }
    }

    public static void updateMetadata(int processId, Map<String, List<String>> metadata) {
        logger.trace("Update metadata for process with id " + processId);
        try {
            MetadataMysqlHelper.removeMetadata(processId);
            insertMetadata(processId, metadata);
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

    public static List<StringPair> getMetadata(int processId) {
        try {
            return MetadataMysqlHelper.getMetadata(processId);
        } catch (SQLException e) {
            logger.error(e);
        }
        return new ArrayList<>();
    }

    public static String getMetadataValue(int processId, String metadataName) {
        try {
            return MetadataMysqlHelper.getMetadataValue(processId, metadataName);
        } catch (SQLException e) {
            logger.error(e);
        }
        return "";
    }

    public static List<Integer> getProcessesWithMetadata(String name, String value) {
        try {
            return MetadataMysqlHelper.getAllProcessesWithMetadata(name, value);
        } catch (SQLException e) {
            logger.error(e);
        }
        return new ArrayList<>();
    }

    private static void generateIndexFields(Map<String, List<String>> metadata) {
        List<SearchIndexField> fields = ConfigurationHelper.getInstance().getIndexFields();
        if (!fields.isEmpty()) {
            for (SearchIndexField index : fields) {
                String indexName = index.getIndexName();
                StringBuilder sb = new StringBuilder();
                for (String metadataName : index.getMetadataList()) {
                    if (metadata.containsKey(metadataName)) {
                        sb.append(metadata.get(metadataName));
                        sb.append(" ");
                    }
                }
                String indexValue = sb.toString();
                if (StringUtils.isNotBlank(indexValue)) {
                    List<String> valueList = new ArrayList<>();
                    valueList.add(indexValue);
                    metadata.put(indexName, valueList);
                }
            }
        }
    }
}
