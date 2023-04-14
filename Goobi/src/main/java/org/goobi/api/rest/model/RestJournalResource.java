package org.goobi.api.rest.model;

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

import org.goobi.beans.JournalEntry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

@XmlRootElement(name = "journal")
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class RestJournalResource {

    private Integer id;
    private Integer processId;
    private Date creationDate;
    private String userName;
    private String logType;
    private String content;
    private String filename;

    public RestJournalResource() {

    }

    public RestJournalResource(JournalEntry entry) {
        id = entry.getId();
        processId = entry.getObjectId();
        creationDate = entry.getCreationDate();
        userName = entry.getUserName();
        logType = entry.getType().getTitle();
        content = entry.getContent();
        filename = entry.getFilename();
    }
}
