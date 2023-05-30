package org.goobi.beans;

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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.InstitutionManager;
import de.sub.goobi.persistence.managers.UserManager;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 * Usergroups owning different access rights, represented by integer values
 * 
 * 1: Administration - can do anything <br />
 * 2: Projectmanagement - may do a lot (but not user management, no user switch, no administrative form) <br />
 * 3: User and process (basically like 4 but can be used for setting aditional boundaries later, if so desired) <br />
 * 4: User only: can see current steps
 * 
 * ================================================================
 */
@Log4j2
public class Usergroup implements Serializable, Comparable<Usergroup>, DatabaseObject {
    private static final long serialVersionUID = -5924845694417474352L;
    @Getter
    @Setter
    private Integer id;
    @Setter
    private String titel;
    @Setter
    private Integer berechtigung;
    @Setter
    private List<User> benutzer;
    @Getter
    @Setter
    private List<Step> schritte;
    @Setter
    private List<String> userRoles;

    @Setter
    private Institution institution;
    @Getter
    @Setter
    private Integer institutionId;

    @Getter
    @Setter
    private boolean panelAusgeklappt = false;

    @Override
    public void lazyLoad() {
        try {
            this.benutzer = UserManager.getUsersForUsergroup(this);
        } catch (DAOException e) {
            log.error("error during lazy loading of Usergroup", e);
        }
    }

    public Integer getBerechtigung() {
        if (this.berechtigung == null || this.berechtigung == 3) {
            this.berechtigung = 4;
        }
        return this.berechtigung;
    }

    public String getBerechtigungAsString() {
        if (this.berechtigung == null || this.berechtigung == 3) {
            this.berechtigung = 4;
        }
        return String.valueOf(this.berechtigung.intValue());
    }

    public void setBerechtigungAsString(String berechtigung) {
        this.berechtigung = Integer.parseInt(berechtigung);
    }

    public String getTitel() {
        if (this.titel == null) {
            return "";
        } else {
            return this.titel;
        }
    }

    public List<User> getBenutzer() {
        if (benutzer == null) {
            try {
                benutzer = UserManager.getUsersForUsergroup(this);
            } catch (DAOException e) {
                log.error(e);
            }
        }
        return this.benutzer;
    }

    public boolean isDeletable() {
        return this.benutzer == null || this.benutzer.isEmpty();
    }

    public List<String> getUserRoles() {
        if (userRoles == null) {
            userRoles = new ArrayList<>();
        }
        return userRoles;
    }

    public void addUserRole(String inRole) {
        if (userRoles == null) {
            userRoles = new ArrayList<>();
        }
        if (!userRoles.contains(inRole)) {
            userRoles.add(inRole);
            Collections.sort(userRoles);
        }
    }

    public void removeUserRole(String inRole) {
        if (userRoles != null) {
            userRoles.remove(inRole);
            Collections.sort(userRoles);
        }
    }

    @Override
    public int compareTo(Usergroup o) {
        if (!titel.equals(o.getTitel())) {
            return this.getTitel().compareTo(o.getTitel());
        } else {
            return getInstitution().getShortName().compareTo(o.getInstitution().getShortName());
        }
    }

    @Override
    public boolean equals(Object obj) { //NOSONAR
        if (obj == null) {
            return false;
        }
        return this.getTitel().equals(((Usergroup) obj).getTitel()) && (getInstitutionId().equals(((Usergroup) obj).getInstitutionId()));
    }

    // this method is needed for ajaxPlusMinusButton.xhtml
    public String getTitelLokalisiert() {
        return titel;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((benutzer == null) ? 0 : benutzer.hashCode());
        result = prime * result + ((berechtigung == null) ? 0 : berechtigung.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + (panelAusgeklappt ? 1231 : 1237);
        result = prime * result + ((schritte == null) ? 0 : schritte.hashCode());
        result = prime * result + ((titel == null) ? 0 : titel.hashCode());
        result = prime * result + ((userRoles == null) ? 0 : userRoles.hashCode());
        return result;
    }

    public Institution getInstitution() {
        if (institution == null && institutionId != null && institutionId.intValue() != 0) {
            institution = InstitutionManager.getInstitutionById(institutionId);
        }
        return institution;
    }

}
