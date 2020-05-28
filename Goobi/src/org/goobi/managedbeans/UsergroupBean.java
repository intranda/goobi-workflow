package org.goobi.managedbeans;

import java.util.ArrayList;
import java.util.List;

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
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.model.SelectItem;

import org.goobi.beans.Institution;
import org.goobi.beans.Usergroup;
import org.goobi.production.enums.UserRole;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.InstitutionManager;
import de.sub.goobi.persistence.managers.UsergroupManager;

@ManagedBean(name = "BenutzergruppenForm")
@SessionScoped
public class UsergroupBean extends BasicBean {
    private static final long serialVersionUID = 8051160917458068675L;
    private Usergroup myBenutzergruppe = new Usergroup();
    private String tempRole;

    public UsergroupBean() {
        sortierung = "titel";
    }

    public String Neu() {
        this.myBenutzergruppe = new Usergroup();
        return "usergroup_edit";
    }

    public String Speichern() {
        try {
            UsergroupManager.saveUsergroup(myBenutzergruppe);
            paginator.load();
            return FilterKein();
        } catch (DAOException e) {
            Helper.setFehlerMeldung("Error, could not save", e.getMessage());
            return "";
        }
    }

    public String Loeschen() {
        try {
            if (!this.myBenutzergruppe.getBenutzer().isEmpty()) {
                Helper.setFehlerMeldung("userGroupNotEmpty");
                return "";
            }
            if (myBenutzergruppe.getSchritte() != null && !this.myBenutzergruppe.getSchritte().isEmpty()) {
                Helper.setFehlerMeldung("userGroupAssignedError");
                return "";
            }
            UsergroupManager.deleteUsergroup(myBenutzergruppe);
            paginator.load();
        } catch (DAOException e) {
            Helper.setFehlerMeldung("Error, could not delete", e.getMessage());
            return "";
        }
        return FilterKein();
    }

    public String getTempRole() {
        return tempRole;
    }

    public void setTempRole(String tempRole) {
        this.tempRole = tempRole.trim();
    }

    public String addRole() {
        myBenutzergruppe.addUserRole(tempRole);
        tempRole = "";
        return "";
    }

    public String removeRole() {
        myBenutzergruppe.removeUserRole(tempRole);
        tempRole = "";
        return "";
    }

    public String FilterKein() {
        UsergroupManager m = new UsergroupManager();
        paginator = new DatabasePaginator(sortierung, filter, m, "usergroup_all");
        return "usergroup_all";
    }

    public String FilterKeinMitZurueck() {
        FilterKein();
        return this.zurueck;
    }

    public Usergroup getMyBenutzergruppe() {
        return this.myBenutzergruppe;
    }

    public void setMyBenutzergruppe(Usergroup myBenutzergruppe) {
        this.myBenutzergruppe = myBenutzergruppe;
    }

    public List<String> getAllAvailableRoles() {
        List<String> myroles = new ArrayList<>();
        for (String role : UserRole.getAllRoles()) {
            if (!myBenutzergruppe.getUserRoles().contains(role)) {
                myroles.add(role);
            }
        }
        return myroles;
    }

    public String cloneUsergroup() {
        Usergroup group = new Usergroup();
        group.setTitel(myBenutzergruppe.getTitel() + "_copy");
        for (String role : myBenutzergruppe.getUserRoles()) {
            group.addUserRole(role);
        }
        group.setInstitution(myBenutzergruppe.getInstitution());
        try {
            UsergroupManager.saveUsergroup(group);
        } catch (DAOException e) {
            Helper.setFehlerMeldung("Error, could not save", e.getMessage());
        }
        paginator.load();
        return "";
    }

    public Integer getCurrentInstitutionID() {
        if (myBenutzergruppe.getInstitution() != null) {
            return myBenutzergruppe.getInstitution().getId();
        } else {
            return Integer.valueOf(0);
        }
    }

    public void setCurrentInstitutionID(Integer id) {
        if (id != null && id.intValue() != 0) {
            Institution institution = InstitutionManager.getInstitutionById(id);
            myBenutzergruppe.setInstitution(institution);
        }
    }

    public List<SelectItem> getInstitutionsAsSelectList() throws DAOException {
        List<SelectItem> institutions = new ArrayList<>();
        List<Institution> temp = null;
        if (Helper.getCurrentUser().isSuperAdmin()) {
            temp = InstitutionManager.getAllInstitutionsAsList();
        } else {
            temp = new ArrayList<>();
            temp.add(Helper.getCurrentUser().getInstitution());
        }
        if (temp != null && !temp.isEmpty()) {
            for (Institution proj : temp) {
                institutions.add(new SelectItem(proj.getId(), proj.getShortName(), null));
            }
        }
        return institutions;
    }

}
