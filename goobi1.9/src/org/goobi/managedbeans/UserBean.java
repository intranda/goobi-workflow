package org.goobi.managedbeans;

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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.goobi.beans.Ldap;
import org.goobi.beans.User;
import org.goobi.beans.Usergroup;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;


import de.sub.goobi.beans.Projekt;
import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.forms.BasisForm;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.Page;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.ldap.LdapAuthentication;
import de.sub.goobi.persistence.ProjektDAO;
import de.sub.goobi.persistence.managers.LdapManager;
import de.sub.goobi.persistence.managers.UserManager;
import de.sub.goobi.persistence.managers.UsergroupManager;

@ManagedBean(name="BenutzerverwaltungForm") 
@SessionScoped
public class UserBean extends BasisForm {
	private static final long serialVersionUID = -3635859455444639614L;
	private User myClass = new User();
	private boolean hideInactiveUsers = true;
	private static final Logger logger = Logger.getLogger(UserBean.class);
	private String displayMode = "";

	public String Neu() {
		this.myClass = new User();
		this.myClass.setVorname("");
		this.myClass.setNachname("");
		this.myClass.setLogin("");
		this.myClass.setLdaplogin("");
		this.myClass.setPasswortCrypt("Passwort");
		return "user_edit";
	}

	public String FilterKein() {
		displayMode = "";
		this.filter = null;
		try {
			Session session = Helper.getHibernateSession();
			session.clear();
			Criteria crit = session.createCriteria(User.class);
			crit.add(Restrictions.isNull("isVisible"));
			if (this.hideInactiveUsers) {
				crit.add(Restrictions.eq("istAktiv", true));
			}
			crit.addOrder(Order.asc("nachname"));
			crit.addOrder(Order.asc("vorname"));
			this.page = new Page(crit, 0);
		} catch (HibernateException he) {
			Helper.setFehlerMeldung("Error, could not read", he.getMessage());
			return "";
		}
		return "user_all";
	}

	public String FilterKeinMitZurueck() {
		FilterKein();
		return this.zurueck;
	}

	/**
	 * Anzeige der gefilterten Nutzer
	 */
	public String FilterAlleStart() {
		try {
			Session session = Helper.getHibernateSession();
			session.clear();
			Criteria crit = session.createCriteria(User.class);
			crit.add(Restrictions.isNull("isVisible"));
			if (this.hideInactiveUsers) {
				crit.add(Restrictions.eq("istAktiv", true));
			}

			if (this.filter != null || this.filter.length() != 0) {
				Disjunction ex = Restrictions.disjunction();
				ex.add(Restrictions.like("vorname", "%" + this.filter + "%"));
				ex.add(Restrictions.like("nachname", "%" + this.filter + "%"));
//				crit.createCriteria("projekte", "proj");
//				ex.add(Restrictions.like("proj.titel", "%" + this.filter + "%"));
				
//				crit.createCriteria("benutzergruppen", "group");
//				ex.add(Restrictions.like("group.titel", "%" + this.filter + "%"));
				crit.add(ex);
			}
			crit.addOrder(Order.asc("nachname"));
			crit.addOrder(Order.asc("vorname"));
			this.page = new Page(crit, 0);
		} catch (HibernateException he) {
			Helper.setFehlerMeldung("Error, could not read", he.getMessage());
			return "";
		}
		return "user_all";
	}

	public String Speichern() {
		Session session = Helper.getHibernateSession();
		session.evict(this.myClass);
		String bla = this.myClass.getLogin();

		if (!LoginValide(bla)) {
			return "";
		}

		Integer blub = this.myClass.getId();
		try {
			/* prüfen, ob schon ein anderer Benutzer mit gleichem Login existiert */
			int num = new UserManager().getHitSize(null, "login='" + bla + "'AND BenutzerID<>" + blub);
			if (num == 0) {
				UserManager.saveUser(this.myClass);
				return "user_all";
			} else {
				Helper.setFehlerMeldung("", Helper.getTranslation("loginBereitsVergeben"));
				return "";
			}
		} catch (DAOException e) {
			Helper.setFehlerMeldung("Error, could not save", e.getMessage());
			logger.error(e);
			return "";
		}
	}

	private boolean LoginValide(String inLogin) {
		boolean valide = true;
		String patternStr = "[A-Za-z0-9@_\\-.]*";
		Pattern pattern = Pattern.compile(patternStr);
		Matcher matcher = pattern.matcher(inLogin);
		valide = matcher.matches();
		if (!valide) {
			Helper.setFehlerMeldung("", Helper.getTranslation("loginNotValid"));
		}

		/* Pfad zur Datei ermitteln */
		FacesContext context = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
		String filename = session.getServletContext().getRealPath("/WEB-INF") + File.separator + "classes" + File.separator
				+ "goobi_loginBlacklist.txt";
		/* Datei zeilenweise durchlaufen und die auf ungültige Zeichen vergleichen */
		try {
			FileInputStream fis = new FileInputStream(filename);
			InputStreamReader isr = new InputStreamReader(fis, "UTF8");
			BufferedReader in = new BufferedReader(isr);
			String str;
			while ((str = in.readLine()) != null) {
				if (str.length() > 0 && inLogin.equalsIgnoreCase(str)) {
					valide = false;
					Helper.setFehlerMeldung("", "Login " + str + Helper.getTranslation("loginNotValid"));
				}
			}
			in.close();
		} catch (IOException e) {
		}
		return valide;
	}

	/**
	 * The function Loeschen() deletes a user account.
	 * 
	 * Please note that deleting a user in goobi.production will not delete the user from a connected LDAP service.
	 * 
	 * @return a string indicating the screen showing up after the command has been performed.
	 */
	public String Loeschen() {
		try {
			UserManager.deleteUser(myClass);
		} catch (DAOException e) {
			Helper.setFehlerMeldung("Error, could not save", e.getMessage());
			logger.error(e);
			return "";
		}
		return "user_all";
	}

	public String AusGruppeLoeschen() {
		int gruppenID = Integer.parseInt(Helper.getRequestParameter("ID"));

		List<Usergroup> neu = new ArrayList<Usergroup>();
		for (Iterator<Usergroup> iter = this.myClass.getBenutzergruppenList().iterator(); iter.hasNext();) {
			Usergroup element = iter.next();
			if (element.getId().intValue() != gruppenID) {
				neu.add(element);
			}
		}
		this.myClass.setBenutzergruppen(neu);
		return "";
	}

	public String ZuGruppeHinzufuegen() {
		Integer gruppenID = Integer.valueOf(Helper.getRequestParameter("ID"));
		try {
			Usergroup usergroup = UsergroupManager.getUsergroupById(gruppenID);
			for (Usergroup b : this.myClass.getBenutzergruppenList()) {
				if (b.equals(usergroup)) {
					return "";
				}
			}
			this.myClass.getBenutzergruppenList().add(usergroup);
		} catch (DAOException e) {
			Helper.setFehlerMeldung("Error on reading database", e.getMessage());
			return null;
		}
		displayMode="";
		return "";
	}

	public String AusProjektLoeschen() {
		int projektID = Integer.parseInt(Helper.getRequestParameter("ID"));
		Set<Projekt> neu = new HashSet<Projekt>();
		for (Iterator<Projekt> iter = this.myClass.getProjekte().iterator(); iter.hasNext();) {
			Projekt element = iter.next();
			if (element.getId().intValue() != projektID) {
				neu.add(element);
			}
		}
		this.myClass.setProjekte(neu);
		return "";
	}

	public String ZuProjektHinzufuegen() {
		Integer projektID = Integer.valueOf(Helper.getRequestParameter("ID"));
		try {
			Projekt project = new ProjektDAO().get(projektID);
			for (Projekt p : this.myClass.getProjekte()) {
				if (p.equals(project)) {
					return "";
				}
			}
			this.myClass.getProjekte().add(project);
		} catch (DAOException e) {
			Helper.setFehlerMeldung("Error on reading database", e.getMessage());
			return null;
		}
		displayMode="";
		return "";
	}

	/*
	 * Getter und Setter
	 */

	public User getMyClass() {
		return this.myClass;
	}

	public void setMyClass(User inMyClass) {
		this.myClass = inMyClass;
	}

	/*
	 * Ldap-Konfiguration
	 */

	public Integer getLdapGruppeAuswahl() {
		if (this.myClass.getLdapGruppe() != null) {
			return this.myClass.getLdapGruppe().getId();
		} else {
			return Integer.valueOf(0);
		}
	}

	public void setLdapGruppeAuswahl(Integer inAuswahl) {
		if (inAuswahl.intValue() != 0) {
			try {
				this.myClass.setLdapGruppe(LdapManager.getLdapById(inAuswahl));
			} catch (DAOException e) {
				Helper.setFehlerMeldung("Error on writing to database", "");
				logger.error(e);
			}
		}
	}

	public List<SelectItem> getLdapGruppeAuswahlListe() throws DAOException {
		List<SelectItem> myLdapGruppen = new ArrayList<SelectItem>();
		List<Ldap> temp = LdapManager.getLdaps("titel", null, 0, 0);
		for (Ldap gru : temp) {
			myLdapGruppen.add(new SelectItem(gru.getId(), gru.getTitel(), null));
		}
		return myLdapGruppen;
	}

	/**
	 * Ldap-Konfiguration für den Benutzer schreiben
	 * 
	 * @return
	 */
	public String LdapKonfigurationSchreiben() {
		LdapAuthentication myLdap = new LdapAuthentication();
		try {
			myLdap.createNewUser(this.myClass, this.myClass.getPasswortCrypt());
		} catch (Exception e) {
			logger.warn("Could not generate ldap entry: " + e.getMessage());
			Helper.setFehlerMeldung(e.getMessage());
		}
		return "";
	}

	public boolean isHideInactiveUsers() {
		return this.hideInactiveUsers;
	}

	public void setHideInactiveUsers(boolean hideInactiveUsers) {
		this.hideInactiveUsers = hideInactiveUsers;
	}
	
	public String getDisplayMode() {
		return displayMode;
	}
	
	public void setDisplayMode(String displayMode) {
		this.displayMode = displayMode;
	}

	public boolean getLdapUsage() {
		return ConfigMain.getBooleanParameter("ldap_use");
	}
}
