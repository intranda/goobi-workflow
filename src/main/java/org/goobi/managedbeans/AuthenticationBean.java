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
import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;
import javax.inject.Named;

import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.goobi.beans.Ldap;
import org.goobi.security.authentication.IAuthenticationProvider.AuthenticationType;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.LdapManager;
import lombok.Getter;
import lombok.Setter;

@Named("LdapGruppenForm")
@WindowScoped
@Getter
@Setter
public class AuthenticationBean extends BasicBean implements Serializable {

    private static final long serialVersionUID = -5644561256582235244L;

    private static final String RETURN_PAGE = "ldap_all";

    private Ldap myLdapGruppe = new Ldap();
    private String displayMode = "";

    public String Neu() {
        this.myLdapGruppe = new Ldap();
        return "ldap_edit";
    }

    public String Speichern() {
        try {
            LdapManager.saveLdap(myLdapGruppe);
            paginator.load();
            return FilterKein();
        } catch (DAOException e) {
            Helper.setFehlerMeldung("Could not save", e.getMessage());
            return "";
        }
    }

    public String Loeschen() {
        try {
            LdapManager.deleteLdap(myLdapGruppe);
            paginator.load();
        } catch (DAOException e) {
            Helper.setFehlerMeldung("Could not delete from database", e.getMessage());
            return "";
        }
        return FilterKein();
    }

    public String Cancel() {
        return RETURN_PAGE;
    }

    public String FilterKein() {
        LdapManager rm = new LdapManager();
        paginator = new DatabasePaginator(sortField, filter, rm, RETURN_PAGE);
        return RETURN_PAGE;
    }

    public String FilterKeinMitZurueck() {
        FilterKein();
        return this.zurueck;
    }

    public List<SelectItem> getAllAuthenticationTypes() {
        List<SelectItem> itemList = new ArrayList<>();
        for (AuthenticationType type : AuthenticationType.values()) {
            itemList.add(new SelectItem(type.getTitle(), Helper.getTranslation(type.getTitle())));
        }
        return itemList;
    }

}
