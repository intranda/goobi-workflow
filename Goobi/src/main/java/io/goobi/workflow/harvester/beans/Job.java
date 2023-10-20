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

package io.goobi.workflow.harvester.beans;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.goobi.beans.DatabaseObject;
import org.joda.time.MutableDateTime;

import de.sub.goobi.helper.exceptions.HarvestException;
import de.sub.goobi.persistence.managers.HarvesterRepositoryManager;
import io.goobi.workflow.harvester.export.ExportHistoryEntry;
import io.goobi.workflow.harvester.export.ExportOutcome;
import io.goobi.workflow.harvester.export.ExportOutcome.ExportOutcomeStatus;
import io.goobi.workflow.harvester.repository.Repository;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Getter
@Setter
@Log4j2
public class Job implements Serializable, DatabaseObject {

    private static final long serialVersionUID = 6402861322926991487L;

    // Status of job
    public static final String WAITING = "WAITING";
    public static final String WORKING = "WORKING";
    public static final String DONE = "DONE";
    public static final String ERROR = "ERROR";
    public static final String CANCELLED = "CANCELLED";

    private Integer id;
    private final Integer repositoryId;
    private final String repositoryName;
    private String status;
    private String message;
    private Timestamp timestamp;

    /***************************************************************************************************************
     * Constructor
     *
     * @param id String (DB)
     * @param status String (WAITING,WORKING, ERROR, DONE)
     * @param repositoryId String
     * @param message String
     * @param timestamp {@link Timestamp}
     ***************************************************************************************************************/
    public Job(Integer id, String status, Integer repositoryId, String repositoryName, String message, Timestamp timestamp) {
        super();
        this.id = id;
        this.status = status;
        this.repositoryId = repositoryId;
        this.repositoryName = repositoryName;
        this.message = message;
        this.timestamp = timestamp;
    }

    /***************************************************************************************************************
     * Change status of Job, save error message and update DB.
     *
     * @param string Error message
     * @param e {@link Exception}
     ***************************************************************************************************************/
    private void error(String string, Exception e) {
        setStatus(ERROR);
        setMessage(string);
        log.error(string, e);
    }

    @Override
    public void lazyLoad() {
        // do nothing
    }

    public void run(boolean createNewJob) {
        log.debug("Executing job {}...", getId());
        try {
            setStatus(WORKING);
            HarvesterRepositoryManager.updateJobStatus(this);

            Repository repository = HarvesterRepositoryManager.getRepository(repositoryId);

            // check if repository is enabled
            if (!repository.isEnabled()) {
                setStatus(CANCELLED);
                setMessage("Repository is disabled");
                log.info(
                        "Repository {} is disabled so job {} has been cancelled. No new jobs will be created for this repository until it is re-activated.",
                        getRepositoryId(), getId());

                HarvesterRepositoryManager.updateJobStatus(this);

                return;
            }

            // ---------------------------------------------------------------------------------------------------
            // Get new Records from OAI and write them to temp xml file
            // Parse this xml Files directly to Records..
            // ---------------------------------------------------------------------------------------------------
            log.info("Harvesting repository: {}", repositoryId);
            int harvested = repository.harvest(getId());
            setMessage(harvested + " records harvested");

            // Auto-export
            if (repository.isAutoExport()) {
                log.debug("Auto-export is enabled for repository {}.", repositoryId);
                Map<String, String> filters = new HashMap<>();
                filters.put("repositoryId", "" + repositoryId);

                List<Record> unexportedRecords = HarvesterRepositoryManager.getRecords(0, Integer.MAX_VALUE, null, false, filters, false);
                log.info("Exporting {} not yet exported records...", unexportedRecords.size());

                for (Record rec : unexportedRecords) {
                    ExportHistoryEntry hist = new ExportHistoryEntry(rec);
                    ExportOutcome outcome = rec.export(hist);
                    switch (outcome.status) {
                        case OK:
                            HarvesterRepositoryManager.addExportHistoryEntry(hist);
                            break;
                        case SKIP:
                            break;
                        default:
                            log.error("Export failed ({}): {}", rec.getIdentifier(), outcome.message);
                            HarvesterRepositoryManager.addExportHistoryEntry(hist);
                            break;
                    }
                }

                // Call script configured for this repository
                String err = repository.executeScript();
                // Add script errors to history
                if (StringUtils.isNotEmpty(err)) {
                    ExportHistoryEntry hist =
                            new ExportHistoryEntry(0, "-", "shell script", repository.getId(), ExportOutcomeStatus.ERROR.name(), err);
                    HarvesterRepositoryManager.addExportHistoryEntry(hist);
                }
            }

            // ---------------------------------------------------------------------------------------------------
            // Create new Job for next harvesting and add it in DB
            // ---------------------------------------------------------------------------------------------------
            if (createNewJob) {
                createNewJob();
            }

            // ---------------------------------------------------------------------------------------------------
            // Update DB about this job
            // ---------------------------------------------------------------------------------------------------
            HarvesterRepositoryManager.updateLastHarvestingTime(repositoryId, getTimestamp());
            setStatus(DONE);
            HarvesterRepositoryManager.updateJobStatus(this);
        } catch (HarvestException e) {
            error("Database error: " + e.getMessage(), e);

            if ("noRecordsMatch".equals(e.getErrorCode())) {
                setStatus(DONE);
                setMessage("No new records found");
                log.info("No new records found. Job is done.");
                try {
                    HarvesterRepositoryManager.updateJobStatus(this);
                    HarvesterRepositoryManager.updateLastHarvestingTime(repositoryId, getTimestamp());
                    createNewJob();
                } catch (HarvestException e1) {
                    log.error(e1.getMessage(), e1);
                }
            } else {
                error("OAI Error: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            error(e.getMessage(), e);
        }
    }

    /**
     * Creates new Job with the Timestamp in the future. Status: WAITING
     *
     * @throws HarvestException
     */
    private void createNewJob() throws HarvestException {
        MutableDateTime mdt = new MutableDateTime();

        mdt.addHours(HarvesterRepositoryManager.getRepository(getRepositoryId()).getFrequency());
        Timestamp newTimestamp = new Timestamp(mdt.getMillis());
        Job newJob = new Job(null, WAITING, repositoryId, repositoryName, null, newTimestamp);

        HarvesterRepositoryManager.addNewJob(newJob);
        log.info("New job for repository {} has been scheduled for {}", getRepositoryId(), newTimestamp.toString());
    }

}
