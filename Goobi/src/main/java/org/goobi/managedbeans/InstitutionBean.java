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
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
package org.goobi.managedbeans;

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Named;

import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.goobi.beans.Institution;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.persistence.managers.InstitutionManager;
import lombok.Getter;
import lombok.Setter;

@Named
@WindowScoped
public class InstitutionBean extends BasicBean implements Serializable {

    private static final long serialVersionUID = 8888874759901347827L;

    @Getter
    @Setter
    private String displayMode = "";

    @Getter
    @Setter
    private Institution institution;

    /**
     * Create a new institution instance
     * 
     * @return
     */
    public String createNewInstitution() {
        institution = new Institution();
        return "institution_edit";
    }

    /**
     * Save the institution in the database, return to institution overview
     * 
     * @return
     */
    public String saveInstitution() {
        InstitutionManager.saveInstitution(institution);
        paginator.load();
        return FilterKein();

    }

    /**
     * Delete the current institution, return to institution overview
     * 
     * @return
     */
    public String deleteInstitution() {
        // TODO check if a project is assigned to the institution. If this is the case, stay on this page
        // otherwise delete institution and return to overview

        Path folder = Paths.get(ConfigurationHelper.getInstance().getGoobiFolder(), "uploads", "institution", institution.getShortName());
        if (StorageProvider.getInstance().isFileExists(folder)) {
            StorageProvider.getInstance().deleteDir(folder);
        }
        InstitutionManager.deleteInstitution(institution);
        paginator.load();
        return FilterKein();
    }

    public String FilterKein() {
        InstitutionManager manager = new InstitutionManager();
        paginator = new DatabasePaginator(sortField, filter, manager, "institution_all");
        displayMode = "";
        return "institution_all";
    }

    public String FilterKeinMitZurueck() {
        FilterKein();
        return this.zurueck;
    }

    /**
     * Needed from the UI, don't use it in java code, use saveInstitution instead
     * 
     * @deprecated This method is replaced by saveInstitution()
     * 
     * @return
     */
    @Deprecated(since = "23.05", forRemoval = false)
    public String Speichern() {
        return saveInstitution();
    }

    /**
     * Needed from the UI, don't use it in java code, use deleteInstitution instead
     * 
     * @deprecated This method is replaced by deleteInstitution()
     * 
     * @return
     */
    @Deprecated(since = "23.05", forRemoval = false)
    public String Loeschen() {
        return deleteInstitution();
    }
}
