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
import java.io.File;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.goobi.beans.Docket;

import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.apache.ProcessManager;
import de.sub.goobi.persistence.managers.DocketManager;

@ManagedBean(name="DocketForm") 
@SessionScoped
public class DocketBean extends BasicBean {
	private static final long serialVersionUID = 3006854499230483171L;
	private Docket myDocket = new Docket();

	public String Neu() {
		this.myDocket = new Docket();
		return "docket_edit";
	}

	public String Speichern() {
		try {
			if (hasValidRulesetFilePath(myDocket, ConfigMain.getParameter("xsltFolder"))) {
				DocketManager.saveDocket(myDocket);
				paginator.load();
				return "docket_all";
			} else {
				Helper.setFehlerMeldung("DocketNotFound");
				return "";
			}
		} catch (DAOException e) {
			Helper.setFehlerMeldung("fehlerNichtSpeicherbar", e.getMessage());
			return "";
		}
	}

	private boolean hasValidRulesetFilePath(Docket d, String pathToRulesets) {
		File rulesetFile = new File(pathToRulesets + d.getFile());
		return rulesetFile.exists();
	}

	public String Loeschen() {
		try {
			if (hasAssignedProcesses(myDocket)) {
				Helper.setFehlerMeldung("DocketInUse");
				return "";
			} else {
				DocketManager.deleteDocket(myDocket);
				paginator.load();
			}
		} catch (DAOException e) {
			Helper.setFehlerMeldung("fehlerNichtLoeschbar", e.getMessage());
			return "";
		}
		return "docket_all";
	}

	private boolean hasAssignedProcesses(Docket d) {
		Integer number = ProcessManager.getNumberOfProcessesWithDocket(d.getId());
		if (number != null && number > 0) {
			return true;
		}
		return false;
	}

	public String FilterKein() {
		DocketManager m = new DocketManager();
		paginator = new DatabasePaginator(sortierung, filter, m);
		return "docket_all";
	}

	public String FilterKeinMitZurueck() {
		FilterKein();
		return this.zurueck;
	}

	public Docket getMyDocket() {
		return this.myDocket;
	}

	public void setMyDocket(Docket docket) {
		this.myDocket = docket;
	}
}
