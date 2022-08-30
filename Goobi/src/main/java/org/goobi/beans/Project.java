package org.goobi.beans;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *     		- https://goobi.io
 * 			- https://www.intranda.com
 * 			- https://github.com/intranda/goobi-workflow
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

import org.goobi.production.flow.statistics.StepInformation;

import de.sub.goobi.helper.ProjectHelper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.ProjectManager;
import de.sub.goobi.persistence.managers.UserManager;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class Project implements Serializable, DatabaseObject, Comparable<Project> {
    private static final long serialVersionUID = -8543713331407761617L;
    @Getter
    @Setter
    private Integer id;
    @Getter
    @Setter
    private String titel;
    @Getter
    @Setter
    private String projectIdentifier;

    @Getter
    @Setter
    private List<User> benutzer = new ArrayList<>();
    @Getter
    @Setter
    private List<Process> prozesse = new ArrayList<>();
    @Setter
    private List<ProjectFileGroup> filegroups = new ArrayList<>();

    @Getter
    @Setter
    private boolean useDmsImport = false;
    @Getter
    @Setter
    private Integer dmsImportTimeOut = 20000;
    @Getter
    @Setter
    private String dmsImportRootPath;
    @Getter
    @Setter
    private String dmsImportImagesPath;
    @Getter
    @Setter
    private String dmsImportSuccessPath;
    @Getter
    @Setter
    private String dmsImportErrorPath;
    private Boolean dmsImportCreateProcessFolder = false;

    @Getter
    @Setter
    private String fileFormatInternal;
    @Getter
    @Setter
    private String fileFormatDmsExport;

    @Getter
    @Setter
    private String metsRightsOwner = "";
    @Getter
    @Setter
    private String metsRightsOwnerLogo = "";
    @Getter
    @Setter
    private String metsRightsOwnerSite = "";
    @Getter
    @Setter
    private String metsRightsOwnerMail = "";
    @Getter
    @Setter
    private String metsDigiprovReference = "";
    @Getter
    @Setter
    private String metsDigiprovPresentation = "";
    @Getter
    @Setter
    private String metsDigiprovReferenceAnchor = "";
    @Getter
    @Setter
    private String metsDigiprovPresentationAnchor = "";
    @Getter
    @Setter
    private String metsPointerPath = "";
    @Getter
    @Setter
    private String metsPointerPathAnchor = "";
    @Getter
    @Setter
    private String metsPurl = "";
    @Getter
    @Setter
    private String metsContentIDs = "";

    private List<StepInformation> commonWorkFlow = null;
    @Setter
    private Date startDate;
    @Setter
    private Date endDate;
    @Setter
    private Integer numberOfPages;
    @Setter
    private Integer numberOfVolumes;
    private Boolean projectIsArchived = false;

    @Getter
    @Setter
    private String metsRightsSponsor = "";
    @Getter
    @Setter
    private String metsRightsSponsorLogo = "";
    @Getter
    @Setter
    private String metsRightsSponsorSiteURL = "";
    @Getter
    @Setter
    private String metsRightsLicense = "";

    @Getter
    @Setter
    private Institution institution;

    @Getter
    @Setter
    private String metsSruUrl = "";

    @Getter
    @Setter
    private String metsIIIFUrl = "";

    @Override
    public void lazyLoad() {
    	
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

    /**
     * 
     * @return a list with informations for each step on workflow
     */
    public List<StepInformation> getWorkFlow() {
        if (this.commonWorkFlow == null) {
            log.trace("create common workflow");
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

    public Integer getNumberOfPages() {
        if (this.numberOfPages == null) {
            this.numberOfPages = 0;
        }
        return this.numberOfPages;
    }

    public Date getStartDate() {
        if (this.startDate == null) {
            this.startDate = new Date();
        }
        return this.startDate;
    }

    public Date getEndDate() {
        if (this.endDate == null) {
            this.endDate = new Date();
        }
        return this.endDate;
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
        int comp = getTitel().compareTo(o.getTitel());
        if (comp == 0) {
            comp = getInstitution().getShortName().compareTo(o.getInstitution().getShortName());
        }
        return comp;
    }

    @Override
    public boolean equals(Object obj) { // NOSONAR
        if (obj == null) {
            return false;
        }
        return this.getTitel().equals(((Project) obj).getTitel())
                && (getInstitution().getShortName().equals(((Project) obj).getInstitution().getShortName()));
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

    public Project(Project source) {
        setDmsImportCreateProcessFolder(source.isDmsImportCreateProcessFolder());
        setDmsImportErrorPath(source.getDmsImportErrorPath());
        setDmsImportImagesPath(source.getDmsImportImagesPath());
        setDmsImportRootPath(source.getDmsImportRootPath());
        setDmsImportSuccessPath(source.getDmsImportSuccessPath());

        setDmsImportTimeOut(source.getDmsImportTimeOut());
        setEndDate(source.getEndDate());
        setFileFormatDmsExport(source.getFileFormatDmsExport());
        setFileFormatInternal(source.getFileFormatInternal());

        setMetsContentIDs(source.getMetsContentIDs());
        setMetsDigiprovPresentation(source.getMetsDigiprovPresentation());
        setMetsDigiprovPresentationAnchor(source.getMetsDigiprovPresentationAnchor());

        setMetsDigiprovReference(source.getMetsDigiprovReference());
        setMetsDigiprovReferenceAnchor(source.getMetsDigiprovReferenceAnchor());

        setMetsPointerPath(source.getMetsPointerPath());
        setMetsPointerPathAnchor(source.getMetsPointerPathAnchor());

        setMetsPurl(source.getMetsPurl());
        setMetsRightsLicense(source.getMetsRightsLicense());
        setMetsRightsOwner(source.getMetsRightsOwner());
        setMetsRightsOwnerLogo(source.getMetsRightsOwnerLogo());
        setMetsRightsOwnerMail(source.getMetsRightsOwnerMail());
        setMetsRightsOwnerSite(source.getMetsRightsOwnerSite());
        setMetsRightsSponsor(source.getMetsRightsSponsor());
        setMetsRightsSponsorLogo(source.getMetsRightsSponsorLogo());
        setMetsRightsSponsorSiteURL(source.getMetsRightsSponsorSiteURL());
        setNumberOfPages(source.getNumberOfPages());
        setNumberOfVolumes(source.getNumberOfVolumes());
        setProjectIsArchived(source.getProjectIsArchived());
        setStartDate(source.getStartDate());
        setTitel(source.getTitel() + "_copy");
        setUseDmsImport(source.isUseDmsImport());
        setInstitution(source.getInstitution());
        setProjectIdentifier(source.getProjectIdentifier());
        try {
            ProjectManager.saveProject(this);
        } catch (DAOException e) {
            log.error(e);
        }

        try {
            List<User> allUsers = UserManager.getUsers(null, "", null, null, null);
            for (User user : allUsers) {
                if (user.getProjekte().contains(this)) {
                    user.getProjekte().add(this);
                    UserManager.addProjectAssignment(user, getId());
                }
            }
        } catch (DAOException e) {
            log.error(e);
        }

        setFilegroups(source.getFilegroups());
        List<ProjectFileGroup> projectFileGroupList = new ArrayList<>();
        for (ProjectFileGroup pfg : getFilegroups()) {
            ProjectFileGroup newGroup = new ProjectFileGroup(pfg);
            newGroup.setProject(this);
            projectFileGroupList.add(newGroup);
        }
        ProjectManager.saveProjectFileGroups(projectFileGroupList);

    }
}
