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
package org.goobi.managedbeans;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.goobi.beans.Institution;
import org.goobi.beans.Project;
import org.goobi.beans.User;
import org.goobi.production.enums.UserRole;
import org.goobi.production.flow.statistics.hibernate.FilterHelper;
import org.reflections.Reflections;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.HarvesterRepositoryManager;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.ProjectManager;
import io.goobi.workflow.harvester.HarvesterGoobiImport;
import io.goobi.workflow.harvester.beans.Job;
import io.goobi.workflow.harvester.repository.Repository;
import jakarta.faces.model.SelectItem;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;

@Named
@WindowScoped
public class HarvesterBean extends BasicBean implements Serializable {

    private static final long serialVersionUID = -8787011738438572424L;

    private List<SelectItem> availableProjects = null;
    private List<SelectItem> availableProcessTemplates = null;

    @Getter
    private String[] repositoryTypes = { "oai", "ia", "ia cli", "bach" };

    @Getter
    @Setter
    private transient Repository repository;

    public String createNewRepository() {
        repository = new Repository();
        return "repository_edit";
    }

    public String saveRepository() {
        HarvesterRepositoryManager.saveRepository(repository);
        return filter();
    }

    public String deleteRepository() {
        HarvesterRepositoryManager.deleteRepository(repository);
        return filter();
    }

    public String cancel() {
        return filter();
    }

    public String filter() {
        HarvesterRepositoryManager m = new HarvesterRepositoryManager();
        paginator = new DatabasePaginator(sortField, filter, m, "repository_all");
        return "repository_all";
    }

    public String Speichern() {
        return saveRepository();
    }

    public String Loeschen() {
        return deleteRepository();
    }

    public String harvestNow() {
        if (repository != null) {
            Timestamp newTime = new Timestamp(new Date().getTime());

            Job j = new Job(null, Job.WAITING, repository.getId(), repository.getName(), null, newTime);

            j = HarvesterRepositoryManager.addNewJob(j);
            j.run(true);

        }

        return null;
    }

    public List<SelectItem> getProjectList() throws DAOException {
        if (availableProjects == null) {
            availableProjects = new ArrayList<>();
            List<Project> temp = null;
            LoginBean login = Helper.getLoginBean();
            if (login != null && !login.hasRole(UserRole.Workflow_General_Show_All_Projects.name())) {
                temp = ProjectManager.getProjectsForUser(login.getMyBenutzer(), false);
            } else {
                temp = ProjectManager.getAllProjects();

            }

            for (Project proj : temp) {
                availableProjects.add(new SelectItem(proj.getTitel(), proj.getTitel()));
            }
        }
        return availableProjects;
    }

    public List<SelectItem> getProcessTemplateList() {

        if (availableProcessTemplates == null) {
            availableProcessTemplates = new ArrayList<>();
            Institution inst = null;
            User user = Helper.getCurrentUser();
            if (user != null && !user.isSuperAdmin()) {
                // limit result to institution of current user
                inst = user.getInstitution();
            }

            String sql = FilterHelper.criteriaBuilder("", true, null, null, null, true, false);
            List<org.goobi.beans.Process> processes = ProcessManager.getProcesses("prozesse.titel", sql, inst);
            for (org.goobi.beans.Process p : processes) {
                availableProcessTemplates.add(new SelectItem(p.getTitel(), p.getTitel()));
            }
        }
        return availableProcessTemplates;
    }

    public List<SelectItem> getRestApiList() {

        // get all methods marked with the @HarvesterGoobiImport annotation

        Reflections reflections = new Reflections("org.goobi.api.rest.*");
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(HarvesterGoobiImport.class);
        List<String> annotationDescription = new ArrayList<>();

        for (Class<?> clazz : classes) {
            HarvesterGoobiImport annotation = clazz.getAnnotation(HarvesterGoobiImport.class);
            if (annotation != null) {
                annotationDescription.add(annotation.description());
            }
        }

        List<SelectItem> data = new ArrayList<>();
        for (String name : annotationDescription) {
            data.add(new SelectItem(name, Helper.getTranslation(name)));
        }

        return data;

    }

}
