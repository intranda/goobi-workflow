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

import javax.inject.Named;

import org.apache.deltaspike.core.api.scope.WindowScoped;

import de.sub.goobi.persistence.managers.HarvesterRepositoryManager;
import io.goobi.workflow.harvester.repository.Repository;
import lombok.Getter;
import lombok.Setter;

@Named
@WindowScoped
public class HarvesterBean extends BasicBean implements Serializable {

    private static final long serialVersionUID = -8787011738438572424L;

    @Getter
    private String[] repositoryTypes = { "oai", "ia", "ia cli" };

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
}
