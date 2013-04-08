package org.goobi.beans;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- http://launchpad.net/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
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

import de.sub.goobi.beans.ProjectFileGroup;
import org.goobi.beans.Process;
import de.sub.goobi.helper.ProjectHelper;
import de.sub.goobi.helper.enums.MetadataFormat;
import de.sub.goobi.persistence.managers.ProjectManager;

public class Project implements Serializable, DatabaseObject, Comparable<Project> {
	private static final long serialVersionUID = -8543713331407761617L;
	private static final Logger logger = Logger.getLogger(Project.class);
	private Integer id;
	private String titel;
	private List<User> benutzer;
	private List<Process> prozesse;
	private List<ProjectFileGroup> filegroups;

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

	public void lazyLoad(){
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
		this.fileFormatInternal = MetadataFormat.getDefaultFileFormat().getName();
		this.fileFormatDmsExport = MetadataFormat.getDefaultFileFormat().getName();
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
	 * here differet Getters and Setters for the same value, because Hibernate
	 * does not like bit-Fields with null Values (thats why Boolean) and MyFaces
	 * seams not to like Boolean (thats why boolean for the GUI)
	 * ================================================================
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
	    if (filegroups == null || filegroups.isEmpty()) {
	        filegroups = ProjectManager.getFilegroupsForProjectId(id);
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
			if (this.id != null) {
				this.commonWorkFlow = ProjectHelper.getProjectWorkFlowOverview(this);
			} else {
				this.commonWorkFlow = new ArrayList<StepInformation>();
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
	 * @param numberOfVolumes
	 *            for this project
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
		return this.getTitel().equals(((Project)obj).getTitel());
	}
}
