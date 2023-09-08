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
 */
package io.goobi.workflow.harvester.repository;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import de.sub.goobi.helper.exceptions.HarvestException;
import de.sub.goobi.persistence.managers.HarvesterRepositoryManager;
import io.goobi.workflow.harvester.beans.Record;
import io.goobi.workflow.harvester.export.ExportOutcome;
import io.goobi.workflow.harvester.export.IConverter.ExportMode;
import io.goobi.workflow.harvester.helper.SParser;
import io.goobi.workflow.harvester.helper.Utils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 * Abstract class for work with repository. All the logic about harvestering from repository is here. If you have some kind of "strange" Repository,
 * you have to extend this class.
 * 
 */

@Log4j2
@Getter
@Setter
public abstract class Repository {

    // generated
    protected String id;

    // repository name, should be unique
    protected String name;

    // repository url
    protected String url;

    // additional query parameter like set=xyz or metadataPrefix=oai_dc
    protected Map<String, String> parameter = new HashMap<>();

    // timestamp of the last harvest
    protected Timestamp lastHarvest;

    // harvest frequency in hours
    protected int frequency;

    // delay in days
    protected int delay = 0;

    // active/disabled
    protected boolean enabled;

    // store files in this folder
    protected String exportFolderPath;

    // run this script after file was stored
    protected String scriptPath;

    // update previous harvested files
    protected boolean allowUpdates = true;

    protected Repository() {
    }

    /**
     * @param id {@link String}
     * @param name
     * @param url {@link String}
     * @param exportFolderPath
     * @param scriptPath
     * @param lastHarvest {@link Timestamp}
     * @param frequency
     * @param delay
     * @param enabled
     */
    protected Repository(String id, String name, String url, String exportFolderPath, String scriptPath, Timestamp lastHarvest, int frequency,
            int delay,
            boolean enabled) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.exportFolderPath = exportFolderPath;
        this.scriptPath = scriptPath;
        this.lastHarvest = lastHarvest;
        this.frequency = frequency;
        this.delay = delay;
        this.enabled = enabled;

    }

    /**
     * 
     * @return
     */
    public abstract String getType();

    /**
     * 
     * @return
     */
    public abstract boolean isAutoExport();

    /***************************************************************************************************************
     * This method is for GUI. Activate/Deactivates Repository
     ***************************************************************************************************************/
    public String changeStatus() {
        if (enabled) {
            enabled = false;
        } else {
            enabled = true;
        }

        HarvesterRepositoryManager.changeStatusOfRepository(this);

        return null;
    }

    /**
     * @return {@link String} Label for "activate" button in GUI.
     */
    public String getStatus() {
        if (enabled) {
            return "deactivate";
        }
        return "activate";
    }

    /**
     * 
     * @param jobId
     * @return
     * @throws HarvestException
     * @throws ParserException
     * @throws DBException
     */
    public abstract int harvest(String jobId) throws HarvestException;

    /**
     * Exports record to viewer or goobi.
     * 
     * @param mode {@link ExportMode} VIEWER or GOOBI
     */
    public abstract ExportOutcome exportRecord(Record rec, ExportMode mode);

    /**
     * Parse Answer to {@link ArrayList} of {@link Record} and add this Records to DB.
     * 
     * @param f {@link File} xml file with OAI Answer
     * @param jobId {@link String}
     * @param requiredSetSpec {@link String}
     * @return Number of records harvested from the given file.
     * @throws ParserException
     * @throws HarvestException
     * @throws DBException
     */
    protected int parseAndRecordXmlFile(File f, String jobId, String requiredSetSpec) throws HarvestException {
        // parse files
        int harvested = 0;
        SParser parser = new SParser();
        parser.setRepositoryType(getType());
        List<Record> recordList = parser.parseXML(f, jobId, getId(), requiredSetSpec, null);
        if (!recordList.isEmpty()) {
            harvested = HarvesterRepositoryManager.addRecords(recordList, allowUpdates);
            log.info("{} records have been harvested, {} of which were already in the DB.", recordList.size(), (recordList.size() - harvested));
        } else {
            log.debug("No new records harvested.");
        }

        return harvested;
    }

    /**
     * 
     * @return
     * @throws FileNotFoundException
     */
    public String executeScript() throws FileNotFoundException {
        if (StringUtils.isNotEmpty(scriptPath)) {
            String path = scriptPath;
            List<String> args = new ArrayList<>();
            if (path.contains("$exportpath")) {
                path = path.replace("$exportpath", "");
                args.add(exportFolderPath);
            }
            return Utils.runScript(path, args.toArray(new String[args.size()]));
        }

        return null;
    }

    /**
     * @param
     * @should create custom download folder correctly
     * @should create default download folder correctly
     */
    protected File checkAndCreateDownloadFolder(String defaultDownloadFolderPath) {
        File downloadFolder = null;
        if (StringUtils.isNotEmpty(exportFolderPath)) {
            downloadFolder = new File(exportFolderPath);
            log.trace("Found custom export path: {}", exportFolderPath);
            if (!downloadFolder.exists()) {
                downloadFolder.mkdirs();
            }
        }
        if (downloadFolder == null || !downloadFolder.isDirectory()) {
            downloadFolder = new File(defaultDownloadFolderPath);
            log.trace("Using default download folder: {}", defaultDownloadFolderPath);
        }
        if (!downloadFolder.exists()) {
            downloadFolder.mkdirs();
        }

        return downloadFolder;
    }

}
