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
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;

import de.sub.goobi.beans.Benutzereigenschaft;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.FilesystemHelper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.ldap.LdapAuthentication;
import de.sub.goobi.persistence.managers.UserManager;
import de.sub.goobi.persistence.managers.UsergroupManager;
import dubious.sub.goobi.helper.encryption.DesEncrypter;

public class User implements Serializable, DatabaseObject {
	private static final long serialVersionUID = -7482853955996650586L;
	private static final Logger logger = Logger.getLogger(User.class);
	private Integer id;
	private String vorname;
	private String nachname;
	private String login;
	private String ldaplogin;
	private String passwort;
	private boolean istAktiv = true;
	private String isVisible;
	private String standort;
	private Integer tabellengroesse = Integer.valueOf(10);
	private Integer sessiontimeout = 7200;
	private boolean confVorgangsdatumAnzeigen = false;
	private String metadatenSprache;
	private List<Usergroup> benutzergruppen;
	private List<Step> schritte;
	private List<Step> bearbeitungsschritte;
	private List<Project> projekte;
	private List<Benutzereigenschaft> eigenschaften;
	private boolean mitMassendownload = false;
	private Ldap ldapGruppe;
	private String css;

	public void lazyLoad(){
		try {
			this.benutzergruppen = UsergroupManager.getUsergroupsForUser(this);
		} catch (DAOException e) {
			logger.error("error during lazy loading of User", e);
		}
	}
	
	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getLogin() {
		return this.login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getNachname() {
		return this.nachname;
	}

	public void setNachname(String nachname) {
		this.nachname = nachname;
	}

	public String getPasswort() {
		return this.passwort;
	}

	public void setPasswort(String inpasswort) {
		this.passwort = inpasswort;
	}

	public String getPasswortCrypt() {
		DesEncrypter encrypter = new DesEncrypter();
		String decrypted = encrypter.decrypt(this.passwort);
		return decrypted;
	}

	public void setPasswortCrypt(String inpasswort) {
		DesEncrypter encrypter = new DesEncrypter();
		String encrypted = encrypter.encrypt(inpasswort);
		this.passwort = encrypted;
	}

	public boolean isIstAktiv() {
		return this.istAktiv;
	}

	public void setIstAktiv(boolean istAktiv) {
		this.istAktiv = istAktiv;
	}

	public void setIsVisible(String isVisible) {
		this.isVisible = isVisible;
	}

	public String getIsVisible() {
		return this.isVisible;
	}

	public String getStandort() {
		return this.standort;
	}

	public void setStandort(String instandort) {
		this.standort = instandort;
	}

	public String getVorname() {
		return this.vorname;
	}

	public void setVorname(String vorname) {
		this.vorname = vorname;
	}

	public Integer getTabellengroesse() {
		if (this.tabellengroesse == null) {
			return Integer.valueOf(10);
		}
		return this.tabellengroesse;
	}

	public void setTabellengroesse(Integer tabellengroesse) {
		this.tabellengroesse = tabellengroesse;
	}

	public boolean isMitMassendownload() {
		return this.mitMassendownload;
	}

	public void setMitMassendownload(boolean mitMassendownload) {
		this.mitMassendownload = mitMassendownload;
	}

	public Ldap getLdapGruppe() {
		return this.ldapGruppe;
	}

	public void setLdapGruppe(Ldap ldapGruppe) {
		this.ldapGruppe = ldapGruppe;
	}


	public int getBenutzergruppenSize() {
		if (this.benutzergruppen == null) {
			return 0;
		} else {
			return this.benutzergruppen.size();
		}
	}

	public void setBenutzergruppen(List<Usergroup> benutzergruppen) {
		this.benutzergruppen = benutzergruppen;
	}
	
	public List<Usergroup> getBenutzergruppen() {
	    if (benutzergruppen == null || benutzergruppen.size() == 0 ) {
            try {
                this.benutzergruppen = UsergroupManager.getUsergroupsForUser(this);
            } catch (DAOException e) {
                logger.error(e);
            }
	    }
		return benutzergruppen;
	}

	public List<Step> getSchritte() {
		return this.schritte;
	}

	public void setSchritte(List<Step> schritte) {
		this.schritte = schritte;
	}

	public int getSchritteSize() {
		if (this.schritte == null) {
			return 0;
		} else {
			return this.schritte.size();
		}
	}

	public List<Step> getBearbeitungsschritte() {
		return this.bearbeitungsschritte;
	}

	public void setBearbeitungsschritte(List<Step> bearbeitungsschritte) {
		this.bearbeitungsschritte = bearbeitungsschritte;
	}

	public int getBearbeitungsschritteSize() {
		if (this.bearbeitungsschritte == null) {
			return 0;
		} else {
			return this.bearbeitungsschritte.size();
		}
	}

	public int getProjekteSize() {
		if (this.projekte == null) {
			return 0;
		} else {
			return this.projekte.size();
		}
	}

	public void setProjekte(List<Project> projekte) {
		this.projekte = projekte;
	}
	
	public List<Project> getProjekte() {
		if (this.projekte == null) {
			return new ArrayList<Project>();
		} 
		return this.projekte;
	}

	public boolean isConfVorgangsdatumAnzeigen() {
		return this.confVorgangsdatumAnzeigen;
	}

	public void setConfVorgangsdatumAnzeigen(boolean confVorgangsdatumAnzeigen) {
		this.confVorgangsdatumAnzeigen = confVorgangsdatumAnzeigen;
	}

	public String getMetadatenSprache() {
		return this.metadatenSprache;
	}

	public void setMetadatenSprache(String metadatenSprache) {
		this.metadatenSprache = metadatenSprache;
	}

	public String getLdaplogin() {
		return this.ldaplogin;
	}

	public void setLdaplogin(String ldaplogin) {
		this.ldaplogin = ldaplogin;
	}

	public boolean istPasswortKorrekt(String inPasswort) {
		if (inPasswort == null || inPasswort.length() == 0) {
			return false;
		} else {

			/* Verbindung zum LDAP-Server aufnehmen und Login prüfen, wenn LDAP genutzt wird */

			if (ConfigMain.getBooleanParameter("ldap_use")) {
			 LdapAuthentication myldap = new LdapAuthentication();
				return myldap.isUserPasswordCorrect(this, inPasswort);
			} else {
				DesEncrypter encrypter = new DesEncrypter();
				String encoded = encrypter.encrypt(inPasswort);
				return this.passwort.equals(encoded);
			}
		}
	}

	public String getNachVorname() {
		return this.nachname + ", " + this.vorname;
	}

	/**
	 * BenutzerHome ermitteln und zurückgeben (entweder aus dem LDAP oder direkt aus der Konfiguration)
	 * 
	 * @return Path as String
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public String getHomeDir() throws IOException, InterruptedException {
		String rueckgabe = "";
		/* wenn LDAP genutzt wird, HomeDir aus LDAP ermitteln, ansonsten aus der Konfiguration */

		if (ConfigMain.getBooleanParameter("ldap_use")) {
			LdapAuthentication myldap = new LdapAuthentication();
			rueckgabe = myldap.getUserHomeDirectory(this);
		} else {
			rueckgabe = ConfigMain.getParameter("dir_Users") + this.login;
		}

		if (rueckgabe.equals("")) {
			return "";
		}

		if (!rueckgabe.endsWith(File.separator)) {
			rueckgabe += File.separator;
		}
		/* wenn das Verzeichnis nicht "" ist, aber noch nicht existiert, dann jetzt anlegen */
        FilesystemHelper.createDirectoryForUser(rueckgabe, login);
		return rueckgabe;
	}

	public Integer getSessiontimeout() {
		if (this.sessiontimeout == null) {
			this.sessiontimeout = 7200;
		}
		return this.sessiontimeout;
	}

	public void setSessiontimeout(Integer sessiontimeout) {
		this.sessiontimeout = sessiontimeout;
	}

	public Integer getSessiontimeoutInMinutes() {
		return getSessiontimeout() / 60;
	}

	public void setSessiontimeoutInMinutes(Integer sessiontimeout) {
		if (sessiontimeout.intValue() < 5) {
			this.sessiontimeout = 5 * 60;
		} else {
			this.sessiontimeout = sessiontimeout * 60;
		}
	}

	public String getCss() {
		if (this.css == null || this.css.length() == 0) {
			this.css = "/css/default.css";
		}
		return this.css;
	}

	public void setCss(String css) {
		this.css = css;
	}
	
	public List<Benutzereigenschaft> getEigenschaften() {
		return this.eigenschaften;
	}

	public void setEigenschaften(List<Benutzereigenschaft> eigenschaften) {
		this.eigenschaften = eigenschaften;
	}

	public int getEigenschaftenSize() {
		try {
			Hibernate.initialize(this.eigenschaften);
		} catch (HibernateException e) {
		}
		if (this.eigenschaften == null) {
			return 0;
		} else {
			return this.eigenschaften.size();
		}
	}

	/**
	 * 
	 * @return List of filters as strings
	 */

	public List<String> getFilters() {
		return UserManager.getFilters(this.id);
	}

	/**
	 * adds a new filter to list
	 * 
	 * @param inFilter
	 *            the filter to add
	 */

	public void addFilter(String inFilter) {
		UserManager.addFilter(this.id, inFilter);
	}

	/**
	 * removes filter from list
	 * 
	 * @param inFilter
	 *            the filter to remove
	 */
	public void removeFilter(String inFilter) {
		UserManager.removeFilter(this.id, inFilter);
	}

	/**
	 * The function selfDestruct() removes a user from the environment. Since the user ID may still be referenced somewhere, the user is not hard
	 * deleted from the database, instead the account is set inactive and invisible.
	 * 
	 * To allow recreation of an account with the same login the login is cleaned - otherwise it would be blocked eternally by the login existence
	 * test performed in the BenutzerverwaltungForm.Speichern() function. In addition, all personally identifiable information is removed from the
	 * database as well.
	 */

	public User selfDestruct() {
		this.isVisible = "deleted";
		this.login = null;
		this.istAktiv = false;
		this.vorname = null;
		this.nachname = null;
		this.standort = null;
		this.benutzergruppen =null;
		this.projekte = null;
		return this;
	}
}
