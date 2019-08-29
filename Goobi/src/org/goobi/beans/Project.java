package org.goobi.beans;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *     		- https://goobi.io
 * 			- https://www.intranda.com
 * 			- https://github.com/intranda/goobi
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
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.goobi.production.flow.statistics.StepInformation;

import de.sub.goobi.helper.ProjectHelper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.ProjectManager;
import de.sub.goobi.persistence.managers.UserManager;
import lombok.Getter;
import lombok.Setter;

public class Project implements Serializable, DatabaseObject, Comparable<Project> {
    private static final long serialVersionUID = -8543713331407761617L;
    private static final Logger logger = Logger.getLogger(Project.class);
    private Integer id;
    private String titel;
    private List<User> benutzer = new ArrayList<>();
    private List<Process> prozesse = new ArrayList<>();
    private List<ProjectFileGroup> filegroups = new ArrayList<>();

    private boolean useDmsImport = false;
    private Integer dmsImportTimeOut = 20000;
    private String dmsImportRootPath;
    private String dmsImportImagesPath;
    private String dmsImportSuccessPath;
    private String dmsImportErrorPath;
    private Boolean dmsImportCreateProcessFolder = false;

    private String fileFormatInternal;
    private String fileFormatDmsExport;

    private String metsRightsOwner = "";
    private String metsRightsOwnerLogo = "";
    private String metsRightsOwnerSite = "";
    private String metsRightsOwnerMail = "";
    private String metsDigiprovReference = "";
    private String metsDigiprovPresentation = "";
    private String metsDigiprovReferenceAnchor = "";
    private String metsDigiprovPresentationAnchor = "";
    private String metsPointerPath = "";
    private String metsPointerPathAnchor = "";
    private String metsPurl = "";
    private String metsContentIDs = "";

    private List<StepInformation> commonWorkFlow = null;
    private Date startDate;
    private Date endDate;
    private Integer numberOfPages;
    private Integer numberOfVolumes;
    private Boolean projectIsArchived = false;

    private String metsRightsSponsor = "";
    private String metsRightsSponsorLogo = "";
    private String metsRightsSponsorSiteURL = "";
    private String metsRightsLicense = "";

    @Getter @Setter
    private Institution institution;


    @Override
    public void lazyLoad() {
        //		try {
        //			this.benutzer = null;
        //			this.prozesse = null;
        //			this.filegroups = null;
        //		} catch (DAOException e) {
        //			logger.error("error during lazy loading of User", e);
        //		}
    }

    public Project() {
        this.useDmsImport = false;
        this.dmsImportTimeOut = 0;
        this.dmsImportImagesPath = "";
        this.dmsImportRootPath = "";
        this.dmsImportSuccessPath = "";
        this.dmsImportCreateProcessFolder = false;
        this.fileFormatInternal = "Mets";
        this.fileFormatDmsExport = "Mets";
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public List<User> getBenutzer() {
        return this.benutzer;
    }

    public void setBenutzer(List<User> benutzer) {
        this.benutzer = benutzer;
    }

    public List<Process> getProzesse() {
        return this.prozesse;
    }

    public void setProzesse(List<Process> prozesse) {
        this.prozesse = prozesse;
    }

    public String getTitel() {
        return this.titel;
    }

    public void setTitel(String titel) {
        this.titel = titel;
    }

    public String getDmsImportImagesPath() {
        return this.dmsImportImagesPath;
    }

    public void setDmsImportImagesPath(String dmsImportImagesPath) {
        this.dmsImportImagesPath = dmsImportImagesPath;
    }

    public String getDmsImportRootPath() {
        return this.dmsImportRootPath;
    }

    public void setDmsImportRootPath(String dmsImportRootPath) {
        this.dmsImportRootPath = dmsImportRootPath;
    }

    public String getDmsImportSuccessPath() {
        return this.dmsImportSuccessPath;
    }

    public void setDmsImportSuccessPath(String dmsImportSuccessPath) {
        this.dmsImportSuccessPath = dmsImportSuccessPath;
    }

    public Integer getDmsImportTimeOut() {
        return this.dmsImportTimeOut;
    }

    public void setDmsImportTimeOut(Integer dmsImportTimeOut) {
        this.dmsImportTimeOut = dmsImportTimeOut;
    }

    public boolean isUseDmsImport() {
        return this.useDmsImport;
    }

    public void setUseDmsImport(boolean useDmsImport) {
        this.useDmsImport = useDmsImport;
    }

    public String getDmsImportErrorPath() {
        return this.dmsImportErrorPath;
    }

    public void setDmsImportErrorPath(String dmsImportErrorPath) {
        this.dmsImportErrorPath = dmsImportErrorPath;
    }

    /**
     * here differet Getters and Setters for the same value, because Hibernate does not like bit-Fields with null Values (thats why Boolean) and
     * MyFaces seams not to like Boolean (thats why boolean for the GUI) ================================================================
     */
    public boolean isDmsImportCreateProcessFolder() {
        if (this.dmsImportCreateProcessFolder == null) {
            this.dmsImportCreateProcessFolder = false;
        }
        return this.dmsImportCreateProcessFolder;
    }

    public void setDmsImportCreateProcessFolder(boolean inFolder) {
        this.dmsImportCreateProcessFolder = inFolder;
    }

    public Boolean isDmsImportCreateProcessFolderHibernate() {
        return this.dmsImportCreateProcessFolder;
    }

    public void setDmsImportCreateProcessFolderHibernate(Boolean inFolder) {
        this.dmsImportCreateProcessFolder = inFolder;
    }

    public boolean isDeleteAble() {
        return (this.prozesse == null || this.prozesse.size() == 0);
    }

    public List<ProjectFileGroup> getFilegroups() {
        if (filegroups == null || filegroups.isEmpty() && id != null) {
            filegroups = ProjectManager.getFilegroupsForProjectId(id);

            for (ProjectFileGroup pfg : filegroups) {
                pfg.setProject(this);
            }
        }
        return this.filegroups;
    }

    public void setFilegroups(List<ProjectFileGroup> filegroups) {
        this.filegroups = filegroups;
    }

    public String getMetsRightsOwner() {
        return this.metsRightsOwner;
    }

    public void setMetsRightsOwner(String metsRightsOwner) {
        this.metsRightsOwner = metsRightsOwner;
    }

    public String getMetsRightsOwnerLogo() {
        return this.metsRightsOwnerLogo;
    }

    public void setMetsRightsOwnerLogo(String metsRightsOwnerLogo) {
        this.metsRightsOwnerLogo = metsRightsOwnerLogo;
    }

    public String getMetsRightsOwnerSite() {
        return this.metsRightsOwnerSite;
    }

    public void setMetsRightsOwnerSite(String metsRightsOwnerSite) {
        this.metsRightsOwnerSite = metsRightsOwnerSite;
    }

    public String getMetsRightsOwnerMail() {
        return this.metsRightsOwnerMail;
    }

    public void setMetsRightsOwnerMail(String metsRigthsOwnerMail) {
        this.metsRightsOwnerMail = metsRigthsOwnerMail;
    }

    public String getMetsDigiprovReference() {
        return this.metsDigiprovReference;
    }

    public void setMetsDigiprovReference(String metsDigiprovReference) {
        this.metsDigiprovReference = metsDigiprovReference;
    }

    public String getMetsDigiprovReferenceAnchor() {
        return this.metsDigiprovReferenceAnchor;
    }

    public void setMetsDigiprovReferenceAnchor(String metsDigiprovReferenceAnchor) {
        this.metsDigiprovReferenceAnchor = metsDigiprovReferenceAnchor;
    }

    public String getMetsDigiprovPresentation() {
        return this.metsDigiprovPresentation;
    }

    public void setMetsDigiprovPresentation(String metsDigiprovPresentation) {
        this.metsDigiprovPresentation = metsDigiprovPresentation;
    }

    public String getMetsDigiprovPresentationAnchor() {
        return this.metsDigiprovPresentationAnchor;
    }

    public void setMetsDigiprovPresentationAnchor(String metsDigiprovPresentationAnchor) {
        this.metsDigiprovPresentationAnchor = metsDigiprovPresentationAnchor;
    }

    public String getMetsPointerPath() {
        return this.metsPointerPath;
    }

    public void setMetsPointerPath(String metsPointerPath) {
        this.metsPointerPath = metsPointerPath;
    }

    public void setMetsPointerPathAnchor(String metsPointerPathAnchor) {
        this.metsPointerPathAnchor = metsPointerPathAnchor;
    }

    public String getMetsPointerPathAnchor() {
        return this.metsPointerPathAnchor;
    }

    public void setMetsPurl(String metsPurl) {
        this.metsPurl = metsPurl;
    }

    public String getMetsPurl() {
        return this.metsPurl;
    }

    public void setMetsContentIDs(String contentIDs) {
        this.metsContentIDs = contentIDs;
    }

    public String getMetsContentIDs() {
        return this.metsContentIDs;
    }

    public String getFileFormatInternal() {
        return this.fileFormatInternal;
    }

    public void setFileFormatInternal(String fileFormatInternal) {
        this.fileFormatInternal = fileFormatInternal;
    }

    public String getFileFormatDmsExport() {
        return this.fileFormatDmsExport;
    }

    public void setFileFormatDmsExport(String fileFormatDmsExport) {
        this.fileFormatDmsExport = fileFormatDmsExport;
    }

    /**
     * 
     * @return a list with informations for each step on workflow
     */
    public List<StepInformation> getWorkFlow() {
        if (this.commonWorkFlow == null) {
            logger.trace("create common workflow");
            if (this.id != null) {
                this.commonWorkFlow = ProjectHelper.getProjectWorkFlowOverview(this);
            } else {
                this.commonWorkFlow = new ArrayList<>();
            }
        }
        return this.commonWorkFlow;
    }

    /**
     * 
     * @return number of volumes for this project
     */
    public Integer getNumberOfVolumes() {
        if (this.numberOfVolumes == null) {
            this.numberOfVolumes = 0;
        }
        return this.numberOfVolumes;
    }

    /**
     * 
     * @param numberOfVolumes for this project
     */
    public void setNumberOfVolumes(Integer numberOfVolumes) {
        this.numberOfVolumes = numberOfVolumes;
    }

    public Integer getNumberOfPages() {
        if (this.numberOfPages == null) {
            this.numberOfPages = 0;
        }
        return this.numberOfPages;
    }

    public void setNumberOfPages(Integer numberOfPages) {
        this.numberOfPages = numberOfPages;
    }

    public Date getStartDate() {
        if (this.startDate == null) {
            this.startDate = new Date();
        }
        return this.startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        if (this.endDate == null) {
            this.endDate = new Date();
        }
        return this.endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public void setProjectIsArchived(Boolean projectIsArchived) {
        if (projectIsArchived == null) {
            projectIsArchived = false;
        }
        this.projectIsArchived = projectIsArchived;
    }

    public Boolean getProjectIsArchived() {
        return this.projectIsArchived;
    }

    @Override
    public int compareTo(Project o) {
        return this.getTitel().compareTo(o.getTitel());
    }

    @Override
    public boolean equals(Object obj) {
        return this.getTitel().equals(((Project) obj).getTitel());
    }

    public String getMetsRightsLicense() {
        return metsRightsLicense;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((benutzer == null) ? 0 : benutzer.hashCode());
        result = prime * result + ((commonWorkFlow == null) ? 0 : commonWorkFlow.hashCode());
        result = prime * result + ((dmsImportCreateProcessFolder == null) ? 0 : dmsImportCreateProcessFolder.hashCode());
        result = prime * result + ((dmsImportErrorPath == null) ? 0 : dmsImportErrorPath.hashCode());
        result = prime * result + ((dmsImportImagesPath == null) ? 0 : dmsImportImagesPath.hashCode());
        result = prime * result + ((dmsImportRootPath == null) ? 0 : dmsImportRootPath.hashCode());
        result = prime * result + ((dmsImportSuccessPath == null) ? 0 : dmsImportSuccessPath.hashCode());
        result = prime * result + ((dmsImportTimeOut == null) ? 0 : dmsImportTimeOut.hashCode());
        result = prime * result + ((endDate == null) ? 0 : endDate.hashCode());
        result = prime * result + ((fileFormatDmsExport == null) ? 0 : fileFormatDmsExport.hashCode());
        result = prime * result + ((fileFormatInternal == null) ? 0 : fileFormatInternal.hashCode());
        result = prime * result + ((filegroups == null) ? 0 : filegroups.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((metsContentIDs == null) ? 0 : metsContentIDs.hashCode());
        result = prime * result + ((metsDigiprovPresentation == null) ? 0 : metsDigiprovPresentation.hashCode());
        result = prime * result + ((metsDigiprovPresentationAnchor == null) ? 0 : metsDigiprovPresentationAnchor.hashCode());
        result = prime * result + ((metsDigiprovReference == null) ? 0 : metsDigiprovReference.hashCode());
        result = prime * result + ((metsDigiprovReferenceAnchor == null) ? 0 : metsDigiprovReferenceAnchor.hashCode());
        result = prime * result + ((metsPointerPath == null) ? 0 : metsPointerPath.hashCode());
        result = prime * result + ((metsPointerPathAnchor == null) ? 0 : metsPointerPathAnchor.hashCode());
        result = prime * result + ((metsPurl == null) ? 0 : metsPurl.hashCode());
        result = prime * result + ((metsRightsLicense == null) ? 0 : metsRightsLicense.hashCode());
        result = prime * result + ((metsRightsOwner == null) ? 0 : metsRightsOwner.hashCode());
        result = prime * result + ((metsRightsOwnerLogo == null) ? 0 : metsRightsOwnerLogo.hashCode());
        result = prime * result + ((metsRightsOwnerMail == null) ? 0 : metsRightsOwnerMail.hashCode());
        result = prime * result + ((metsRightsOwnerSite == null) ? 0 : metsRightsOwnerSite.hashCode());
        result = prime * result + ((metsRightsSponsor == null) ? 0 : metsRightsSponsor.hashCode());
        result = prime * result + ((metsRightsSponsorLogo == null) ? 0 : metsRightsSponsorLogo.hashCode());
        result = prime * result + ((metsRightsSponsorSiteURL == null) ? 0 : metsRightsSponsorSiteURL.hashCode());
        result = prime * result + ((numberOfPages == null) ? 0 : numberOfPages.hashCode());
        result = prime * result + ((numberOfVolumes == null) ? 0 : numberOfVolumes.hashCode());
        result = prime * result + ((projectIsArchived == null) ? 0 : projectIsArchived.hashCode());
        result = prime * result + ((prozesse == null) ? 0 : prozesse.hashCode());
        result = prime * result + ((startDate == null) ? 0 : startDate.hashCode());
        result = prime * result + ((titel == null) ? 0 : titel.hashCode());
        result = prime * result + (useDmsImport ? 1231 : 1237);
        return result;
    }

    public void setMetsRightsLicense(String metsRightsLicense) {
        this.metsRightsLicense = metsRightsLicense;
    }

    public String getMetsRightsSponsorSiteURL() {
        return metsRightsSponsorSiteURL;
    }

    public void setMetsRightsSponsorSiteURL(String metsRightsSponsorSiteURL) {
        this.metsRightsSponsorSiteURL = metsRightsSponsorSiteURL;
    }

    public String getMetsRightsSponsorLogo() {
        return metsRightsSponsorLogo;
    }

    public void setMetsRightsSponsorLogo(String metsRightsSponsorLogo) {
        this.metsRightsSponsorLogo = metsRightsSponsorLogo;
    }

    public String getMetsRightsSponsor() {
        return metsRightsSponsor;
    }

    public void setMetsRightsSponsor(String metsRightsSponsor) {
        this.metsRightsSponsor = metsRightsSponsor;
    }

    @Override
    public Project clone() {
        Project p = new Project();
        p.setDmsImportCreateProcessFolder(this.isDmsImportCreateProcessFolder());
        p.setDmsImportErrorPath(this.getDmsImportErrorPath());
        p.setDmsImportImagesPath(getDmsImportImagesPath());
        p.setDmsImportRootPath(getDmsImportRootPath());
        p.setDmsImportSuccessPath(getDmsImportSuccessPath());

        p.setDmsImportTimeOut(getDmsImportTimeOut());
        p.setEndDate(getEndDate());
        p.setFileFormatDmsExport(getFileFormatDmsExport());
        p.setFileFormatInternal(getFileFormatInternal());

        p.setMetsContentIDs(getMetsContentIDs());
        p.setMetsDigiprovPresentation(metsDigiprovPresentation);
        p.setMetsDigiprovPresentationAnchor(metsDigiprovPresentationAnchor);

        p.setMetsDigiprovReference(metsDigiprovReference);
        p.setMetsDigiprovReferenceAnchor(metsDigiprovReferenceAnchor);

        p.setMetsPointerPath(metsPointerPath);
        p.setMetsPointerPathAnchor(metsPointerPathAnchor);

        p.setMetsPurl(metsPurl);
        p.setMetsRightsLicense(metsRightsLicense);
        p.setMetsRightsOwner(metsRightsOwner);
        p.setMetsRightsOwnerLogo(metsRightsOwnerLogo);
        p.setMetsRightsOwnerMail(getMetsRightsOwnerMail());
        p.setMetsRightsOwnerSite(metsRightsOwnerSite);
        p.setMetsRightsSponsor(metsRightsSponsor);
        p.setMetsRightsSponsorLogo(metsRightsSponsorLogo);
        p.setMetsRightsSponsorSiteURL(metsRightsSponsorSiteURL);
        p.setNumberOfPages(numberOfPages);
        p.setNumberOfVolumes(numberOfVolumes);
        p.setProjectIsArchived(projectIsArchived);
        p.setStartDate(startDate);
        p.setTitel(this.getTitel() + "_copy");
        p.setUseDmsImport(useDmsImport);

        try {
            ProjectManager.saveProject(p);
        } catch (DAOException e) {
            logger.error(e);
        }

        try {
            List<User> allUsers = UserManager.getUsers(null, "", null, null);
            for (User user : allUsers) {
                if (user.getProjekte().contains(this)) {
                    user.getProjekte().add(p);
                    UserManager.addProjectAssignment(user, p.getId());
                }
            }
        } catch (DAOException e) {
            logger.error(e);
        }

        p.setFilegroups(getFilegroups());
        List<ProjectFileGroup> projectFileGroupList = new ArrayList<>();
        for (ProjectFileGroup pfg : getFilegroups()) {
            ProjectFileGroup newGroup = pfg.clone();
            newGroup.setProject(p);
            projectFileGroupList.add(newGroup);
        }
        ProjectManager.saveProjectFileGroups(projectFileGroupList);

        return p;
    }
}
