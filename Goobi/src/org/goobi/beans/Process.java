package org.goobi.beans;

import java.io.Serializable;
import java.util.Date;

import org.apache.log4j.Logger;

public class Process implements Serializable, DatabaseObject, Comparable<Process> {

    private static final long serialVersionUID = -8289856935984212479L;
    private static final Logger logger = Logger.getLogger(Process.class);

    private Integer id;
    private String titel;
    private String ausgabename;
    private Boolean istTemplate;
    private Boolean inAuswahllisteAnzeigen;
    private Project projekt;
    // temporary
    private Integer projectId;
    private Date erstellungsdatum;
    //    private List<Schritt> schritte;
    //    private List<HistoryEvent> history;
    //    private List<Werkstueck> werkstuecke;
    //    private List<Vorlage> vorlagen;
    //    private List<Prozesseigenschaft> eigenschaften;
    private String sortHelperStatus;
    private Integer sortHelperImages;
    private Integer sortHelperArticles;
    private Integer sortHelperMetadata;
    private Integer sortHelperDocstructs;
    private Boolean swappedOut = false;
    private Ruleset regelsatz;
    private Integer batchID;
    private Docket docket;
    private String wikifield = "";

    @Override
    public int compareTo(Process o) {
        return this.getId().compareTo(o.getId());
    }

    @Override
    public void lazyLoad() {
        // TODO Auto-generated method stub

    }

    
    
    
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitel() {
        return titel;
    }

    public void setTitel(String titel) {
        this.titel = titel;
    }

    public String getAusgabename() {
        return ausgabename;
    }

    public void setAusgabename(String ausgabename) {
        this.ausgabename = ausgabename;
    }

    public Boolean getIstTemplate() {
        return istTemplate;
    }

    public void setIstTemplate(Boolean istTemplate) {
        this.istTemplate = istTemplate;
    }

    public Boolean getInAuswahllisteAnzeigen() {
        return inAuswahllisteAnzeigen;
    }

    public void setInAuswahllisteAnzeigen(Boolean inAuswahllisteAnzeigen) {
        this.inAuswahllisteAnzeigen = inAuswahllisteAnzeigen;
    }

    public Project getProjekt() {
        return projekt;
    }

    public void setProjekt(Project projekt) {
        this.projekt = projekt;
    }

    public Date getErstellungsdatum() {
        return erstellungsdatum;
    }

    public void setErstellungsdatum(Date erstellungsdatum) {
        this.erstellungsdatum = erstellungsdatum;
    }

    public String getSortHelperStatus() {
        return sortHelperStatus;
    }

    public void setSortHelperStatus(String sortHelperStatus) {
        this.sortHelperStatus = sortHelperStatus;
    }

    public Integer getSortHelperImages() {
        return sortHelperImages;
    }

    public void setSortHelperImages(Integer sortHelperImages) {
        this.sortHelperImages = sortHelperImages;
    }

    public Integer getSortHelperArticles() {
        return sortHelperArticles;
    }

    public void setSortHelperArticles(Integer sortHelperArticles) {
        this.sortHelperArticles = sortHelperArticles;
    }

    public Integer getSortHelperMetadata() {
        return sortHelperMetadata;
    }

    public void setSortHelperMetadata(Integer sortHelperMetadata) {
        this.sortHelperMetadata = sortHelperMetadata;
    }

    public Integer getSortHelperDocstructs() {
        return sortHelperDocstructs;
    }

    public void setSortHelperDocstructs(Integer sortHelperDocstructs) {
        this.sortHelperDocstructs = sortHelperDocstructs;
    }

    public Ruleset getRegelsatz() {
        return regelsatz;
    }

    public void setRegelsatz(Ruleset regelsatz) {
        this.regelsatz = regelsatz;
    }

    public Integer getBatchID() {
        return batchID;
    }

    public void setBatchID(Integer batchID) {
        this.batchID = batchID;
    }

    public Docket getDocket() {
        return docket;
    }

    public void setDocket(Docket docket) {
        this.docket = docket;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public Boolean getSwappedOut() {
        return swappedOut;
    }

    public void setSwappedOut(Boolean swappedOut) {
        this.swappedOut = swappedOut;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public String getWikifield() {
        return wikifield;
    }

    public void setWikifield(String wikifield) {
        this.wikifield = wikifield;
    }
}
