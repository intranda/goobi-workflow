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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.goobi.beans.DatabaseObject;

import de.sub.goobi.helper.exceptions.HarvestException;
import de.sub.goobi.persistence.managers.HarvesterRepositoryManager;
import io.goobi.workflow.harvester.export.ExportHistoryEntry;
import io.goobi.workflow.harvester.export.ExportOutcome;
import io.goobi.workflow.harvester.repository.Repository;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Getter
@Setter
@Log4j2
public class Record implements Serializable, DatabaseObject {

    private static final long serialVersionUID = 8459493710194133861L;

    private Integer id;
    private String timestamp;
    private String identifier;
    private String repositoryTimestamp;
    private String title;
    private String creator;
    private Integer repositoryId;
    private List<String> setSpecList = new ArrayList<>();
    private String subquery;
    private Integer jobId;
    private String source;
    private String exported;
    private Date exportedDatestamp;

    public Record() {
    }

    public Record(Integer id, Timestamp timestamp, String identifier, Date repositoryDatestamp, String title, String creator, Integer repositoryId,
            String setSpecAsString, Integer jobId, String source, String exported, Date exportedDatestamp, String subquery) {
        this.id = id;
        if (timestamp != null) {
            this.timestamp = timestamp.toString();
        } else {
            this.timestamp = "";
        }

        this.identifier = identifier;
        if (repositoryDatestamp != null) {
            this.repositoryTimestamp = repositoryDatestamp.toString();
        } else {
            this.repositoryTimestamp = "";
        }
        this.title = title;
        this.creator = creator;
        this.repositoryId = repositoryId;
        this.setSpecList = new ArrayList<>();
        for (String setSpec : setSpecAsString.split(",")) {
            setSpecList.add(setSpec);
        }
        this.jobId = jobId;
        this.source = source;
        this.exported = exported;
        this.exportedDatestamp = exportedDatestamp;
        this.subquery = subquery;
    }

    /**
     * Exports this record to the given destination.
     * 
     * @param mode Export destination.
     * @param hist The ExportHistoryEntry object that protocols this export.
     * @return
     * @throws DBException
     */
    public ExportOutcome export(ExportHistoryEntry hist) throws HarvestException {
        Repository repository = HarvesterRepositoryManager.getRepository(getRepositoryId());
        ExportOutcome outcome = repository.exportRecord(this);
        hist.setStatus(outcome.status.toString());
        switch (outcome.status) {
            case OK:
                setExportedDatestamp(new Date());
                HarvesterRepositoryManager.setRecordExported(this);
                break;
            case NOT_FOUND:
                log.error("Record '{}' not found.", getIdentifier());
                hist.setMessage("Record missing.");
                break;
            case ERROR:
                log.error("Export failed for '{}'.", getIdentifier());
                setExported(null);
                setExportedDatestamp(null);
                break;
            case SKIP:
                setExported("SKIPPED");
                setExportedDatestamp(new Date());
                HarvesterRepositoryManager.setRecordExported(this);
                break;
            default:
                log.error("Unknown export outcome: {}", outcome.status.toString());
                hist.setMessage("Unknown export outcome");
        }

        return outcome;
    }

    @Override
    public void lazyLoad() {
        // do nothing
    }

    public String getSetSpec() {
        if (!setSpecList.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String str : setSpecList) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(str);
            }
            return sb.toString();
        }
        return "";
    }

}
