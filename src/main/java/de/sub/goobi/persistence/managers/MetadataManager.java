package de.sub.goobi.persistence.managers;

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
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.goobi.production.cli.helper.StringPair;
import org.goobi.production.flow.statistics.hibernate.SearchIndexField;

import de.sub.goobi.config.ConfigurationHelper;
import lombok.extern.log4j.Log4j2;

@Log4j2
public final class MetadataManager implements Serializable {
    private MetadataManager() {
        // hide implicit public constructor
    }

    private static final long serialVersionUID = -1352908576515114528L;

    public static void deleteMetadata(int processId) {
        log.trace("Removing metadata for process with id " + processId);
        try {
            MetadataMysqlHelper.removeMetadata(processId);

            MetadataMysqlHelper.removeJSONMetadata(processId);
        } catch (SQLException e) {
            log.error(e);
        }
    }

    public static void insertJSONMetadata(int processID, Map<String, List<String>> metadata) {
        log.trace("Insert new JSON metadata for process with id " + processID);
        try {
            MetadataMysqlHelper.insertJSONMetadata(processID, metadata);
        } catch (SQLException e) {
            log.error(e);
        }
    }

    public static void insertMetadata(int processId, Map<String, List<String>> metadata) {

        generateIndexFields(metadata);

        log.trace("Insert new metadata for process with id " + processId);
        try {
            MetadataMysqlHelper.insertMetadata(processId, metadata);
        } catch (SQLException e) {
            log.error(e);
        }
    }

    public static void updateJSONMetadata(int processID, Map<String, List<String>> metadata) {
        log.trace("Update JSON metadata for process with id " + processID);
        try {
            MetadataMysqlHelper.removeJSONMetadata(processID);
            insertJSONMetadata(processID, metadata);
        } catch (SQLException e) {
            log.error(e);
        }
    }

    public static void updateMetadata(int processId, Map<String, List<String>> metadata) {
        log.trace("Update metadata for process with id " + processId);
        try {
            MetadataMysqlHelper.removeMetadata(processId);
            insertMetadata(processId, metadata);
        } catch (SQLException e) {
            log.error(e);
        }
    }

    public static List<String> getDistinctMetadataNames() {
        try {
            return MetadataMysqlHelper.getDistinctMetadataNames();
        } catch (SQLException e) {
            log.error(e);
        }
        return new ArrayList<>();
    }

    public static List<StringPair> getMetadata(int processId) {
        try {
            return MetadataMysqlHelper.getMetadata(processId);
        } catch (SQLException e) {
            log.error(e);
        }
        return new ArrayList<>();
    }

    public static String getMetadataValue(int processId, String metadataName) {
        try {
            return MetadataMysqlHelper.getMetadataValue(processId, metadataName);
        } catch (SQLException e) {
            log.error(e);
        }
        return "";
    }

    public static String getAllValuesForMetadata(int processId, String metadataName) {
        try {
            return MetadataMysqlHelper.getAllValuesForMetadata(processId, metadataName);
        } catch (SQLException e) {
            log.error(e);
        }
        return "";
    }

    public static List<String> getAllMetadataValues(int processId, String metadataName) {
        try {
            return MetadataMysqlHelper.getAllMetadataValues(processId, metadataName);
        } catch (SQLException e) {
            log.error(e);
        }
        return Collections.emptyList();
    }

    public static List<Integer> getProcessesWithMetadata(String name, String value) {
        try {
            return MetadataMysqlHelper.getAllProcessesWithMetadata(name, value);
        } catch (SQLException e) {
            log.error(e);
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
