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
        return "ldap_all";
    }

    public String FilterKein() {
        LdapManager rm = new LdapManager();
        paginator = new DatabasePaginator(sortierung, filter, rm, "ldap_all");
        return "ldap_all";
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
