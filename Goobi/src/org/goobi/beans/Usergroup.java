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
import java.util.List;

import org.apache.log4j.Logger;

import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.UserManager;

/**
 * Usergroups owning different access rights, represented by integer values
 * 
 * 1: Administration - can do anything 2: Projectmanagement - may do a lot (but not user management, no user switch, no administrative form) 3: User
 * and process (basically like 4 but can be used for setting aditional boundaries later, if so desired) 4: User only: can see current steps
 * 
 * ================================================================
 */
public class Usergroup implements Serializable, Comparable<Usergroup>, DatabaseObject {
    private static final long serialVersionUID = -5924845694417474352L;
    private static final Logger logger = Logger.getLogger(Usergroup.class);
    private Integer id;
    private String titel;
    private Integer berechtigung;
    private List<User> benutzer;
    private List<Step> schritte;
    private List<String> userRoles;

    private boolean panelAusgeklappt = false;

    public void lazyLoad() {
        try {
            this.benutzer = UserManager.getUsersForUsergroup(this);
        } catch (DAOException e) {
            logger.error("error during lazy loading of Usergroup", e);
        }
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getBerechtigung() {
        if (this.berechtigung == null) {
            this.berechtigung = 4;
        } else if (this.berechtigung == 3) {
            this.berechtigung = 4;
        }
        return this.berechtigung;
    }

    public void setBerechtigung(int berechtigung) {
        this.berechtigung = berechtigung;
    }

    public String getBerechtigungAsString() {
        if (this.berechtigung == null) {
            this.berechtigung = 4;
        } else if (this.berechtigung == 3) {
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

    public void setTitel(String titel) {
        this.titel = titel;
    }

    public List<User> getBenutzer() {
        if (benutzer == null) {
            try {
                benutzer = UserManager.getUsersForUsergroup(this);
            } catch (DAOException e) {
                logger.error(e);
            }
        }
        return this.benutzer;
    }

    public void setBenutzer(List<User> benutzer) {
        this.benutzer = benutzer;
    }

    public List<Step> getSchritte() {
        return this.schritte;
    }

    public void setSchritte(List<Step> schritte) {
        this.schritte = schritte;
    }

    public boolean isPanelAusgeklappt() {
        return this.panelAusgeklappt;
    }

    public void setPanelAusgeklappt(boolean panelAusgeklappt) {
        this.panelAusgeklappt = panelAusgeklappt;
    }

    public List<String> getUserRoles() {
        return userRoles;
    }

    public void setUserRoles(List<String> userRoles) {
        this.userRoles = userRoles;
    }

    @Override
    public int compareTo(Usergroup o) {
        return this.getTitel().compareTo(o.getTitel());
    }

    @Override
    public boolean equals(Object obj) {
        return this.getTitel().equals(((Usergroup) obj).getTitel());
    }
}
