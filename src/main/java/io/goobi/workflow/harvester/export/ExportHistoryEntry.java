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
package io.goobi.workflow.harvester.export;

import java.sql.Timestamp;

import io.goobi.workflow.harvester.beans.Record;


public class ExportHistoryEntry {

    private Integer id;
    private String timestamp;
    private Integer recordId;
    private String recordIdentifier;
    private String recordTitle;
    private Integer repositoryId;
    private String status;
    private String message;

    /**
     * Constructor for new entries.
     * 
     * @param record
     */
    public ExportHistoryEntry(Record record) {
        recordId = record.getId();
        recordIdentifier = record.getIdentifier();
        recordTitle = record.getTitle();
        repositoryId = record.getRepositoryId();
    }

    /**
     * Constructor for misc history entries.
     * 
     * @param recordId
     * @param recordIdentifier
     * @param recordTitle
     * @param repositoryId
     * @param status
     * @param message
     */
    public ExportHistoryEntry(Integer recordId, String recordIdentifier, String recordTitle, Integer repositoryId, String status, String message) {
        this.recordId = recordId;
        this.recordIdentifier = recordIdentifier;
        this.repositoryId = repositoryId;
        this.recordTitle = recordTitle;
        this.status = status;
    }

    /**
     * Constructor for entries from the DB.
     * 
     * @param id
     * @param timestamp
     * @param recordId
     * @param recordIdentifier
     * @param recordTitle
     * @param repositoryId
     * @param status
     * @param message
     */
    public ExportHistoryEntry(Integer id, Timestamp timestamp, Integer recordId, String recordIdentifier, String recordTitle, Integer repositoryId,
            String status, String message) {
        this.id = id;
        if (timestamp != null) {
            this.timestamp = timestamp.toString();
        } else {
            this.timestamp = "";
        }
        this.recordId = recordId;
        this.recordIdentifier = recordIdentifier;
        this.recordTitle = recordTitle;
        this.repositoryId = repositoryId;
        this.status = status;
        this.message = message;
    }

    public String reExportRecord() {

        return "";
    }

    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return the timestamp
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * @return the recordId
     */
    public Integer getRecordId() {
        return recordId;
    }

    /**
     * @param recordId the recordId to set
     */
    public void setRecordId(Integer recordId) {
        this.recordId = recordId;
    }

    /**
     * @return the recordIdentifier
     */
    public String getRecordIdentifier() {
        return recordIdentifier;
    }

    /**
     * @param recordIdentifier the recordIdentifier to set
     */
    public void setRecordIdentifier(String recordIdentifier) {
        this.recordIdentifier = recordIdentifier;
    }

    /**
     * @return the recordTitle
     */
    public String getRecordTitle() {
        return recordTitle;
    }

    /**
     * @param recordTitle the recordTitle to set
     */
    public void setRecordTitle(String recordTitle) {
        this.recordTitle = recordTitle;
    }

    /**
     * @return the repositoryId
     */
    public Integer getRepositoryId() {
        return repositoryId;
    }

    /**
     * @param repositoryId the repositoryId to set
     */
    public void setRepositoryId(Integer repositoryId) {
        this.repositoryId = repositoryId;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

}
