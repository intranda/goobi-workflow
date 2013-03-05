package de.sub.goobi.forms;

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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;

import org.goobi.beans.Docket;
import org.goobi.beans.Ruleset;
import org.goobi.production.GoobiVersion;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.PluginLoader;

import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.enums.MetadataFormat;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.DocketManager;
import de.sub.goobi.persistence.managers.RulesetManager;

@ManagedBean(name = "HelperForm")
@SessionScoped
public class HelperForm {

    // TODO re-added temporary for compiling issues
    public static final String MAIN_JSF_PATH = "/newpages";

    //	public static final String IMAGE_PATH = "/nimages";
    //	public static final String CSS_PATH = "/css";

    public String getBuildVersion() {
        return GoobiVersion.getBuildversion();
    }

    public String getVersion() {
        return GoobiVersion.getBuildversion();
    }

    //	/**
    //	 * @author Wulf
    //	 * @param none
    //	 * @return returns dynamically resolved path for Version Logo
    //	 */
    //	public String getApplicationVersionLogo() {
    //		String logo = getServletPathWithHostAsUrl() + IMAGE_PATH + "/template/";
    //		logo += ConfigMain.getParameter("ApplicationVersionLogo", "Goobi151Logo.jpg");
    //		return logo;
    //
    //	}

    //	public String getApplicationLogo() {
    //		// GetMethod method = null;
    //		// try {
    //		// HttpClient httpclient = new HttpClient();
    //		// method = new GetMethod("http://is.gd/feWO5");
    //		// int statusCode = httpclient.executeMethod(method);
    //		// if (statusCode == HttpStatus.SC_OK) {
    //		// return method.getURI().getURI();
    //		// }
    //		// } catch (URIException e) {
    //		// // do nothing, no internet connection found, using local image
    //		// } catch (HttpException e) {
    //		// // do nothing, no internet connection found, using local image
    //		// } catch (IOException e) {
    //		// // do nothing, no internet connection found, using local image
    //		// } finally {
    //		// method.releaseConnection();
    //		// }
    //		String logo = getServletPathWithHostAsUrl() + IMAGE_PATH + "/template/";
    //		logo += ConfigMain.getParameter("ApplicationLogo", "goobi_meta_klein.jpg");
    //
    //		return logo;
    //	}

    //	public String getApplicationHeaderBackground() {
    //		String logo = getServletPathWithHostAsUrl() + IMAGE_PATH + "/template/";
    //		logo += ConfigMain.getParameter("ApplicationHeaderBackground", "goobi_meta_verlauf.jpg");
    //		/* wenn ein Background angegeben wurde, dann diesen jetzt strecken */
    //		if (logo.length() > 0) {
    //			logo = "background: url(" + logo + ") repeat-x;";
    //		}
    //		return logo;
    //	}

    // TODO: Change the defaults
    public String getApplicationHeaderTitle() {
        String rueck = ConfigMain.getParameter("ApplicationHeaderTitle", "Goobi - Universitätsbibliothek Göttingen");
        return rueck;
    }

    public String getApplicationTitle() {
        String rueck = ConfigMain.getParameter("ApplicationTitle", "http://goobi.gdz.uni-goettingen.de");
        return rueck;
    }

    public String getApplicationTitlexe() {
        String rueck = ConfigMain.getParameter("ApplicationTitleStyle", "font-size:17; font-family:verdana; color: black;");
        return rueck;
    }

    public String getApplicationWebsiteUrl() {
        return getServletPathAsUrl();
    }

    public String getApplicationWebsiteMsg() {
        String rueck = ConfigMain.getParameter("ApplicationWebsiteMsg", getApplicationWebsiteUrl());
        return Helper.getTranslation(rueck);
    }

    public String getApplicationHomepageMsg() {
        String rueck = ConfigMain.getParameter("ApplicationHomepageMsg", getApplicationWebsiteUrl());
        return Helper.getTranslation(rueck);
    }

    public String getApplicationTechnicalBackgroundMsg() {
        String rueck = ConfigMain.getParameter("ApplicationTechnicalBackgroundMsg", getApplicationWebsiteUrl());
        return Helper.getTranslation(rueck);
    }

    public String getApplicationImpressumMsg() {
        String rueck = ConfigMain.getParameter("ApplicationImpressumMsg", getApplicationWebsiteUrl());
        return Helper.getTranslation(rueck);
    }

    public String getApplicationIndividualHeader() {
        String rueck = ConfigMain.getParameter("ApplicationIndividualHeader", "");
        return rueck;
    }

    public boolean getAnonymized() {
        return ConfigMain.getBooleanParameter("anonymize");
    }

    public List<SelectItem> getRegelsaetze() throws DAOException {
        List<SelectItem> myPrefs = new ArrayList<SelectItem>();
        List<Ruleset> temp = RulesetManager.getRulesets("titel", null, null, null);
        for (Iterator<Ruleset> iter = temp.iterator(); iter.hasNext();) {
            Ruleset an = iter.next();
            myPrefs.add(new SelectItem(an, an.getTitel(), null));
        }
        return myPrefs;
    }

    public List<SelectItem> getDockets() {
        List<SelectItem> answer = new ArrayList<SelectItem>();
        try {
            List<Docket> temp = DocketManager.getDockets("name", null, null, null);
            for (Docket d : temp) {
                answer.add(new SelectItem(d, d.getName(), null));
            }
        } catch (DAOException e) {

        }

        return answer;
    }

    public List<SelectItem> getFileFormats() {
        ArrayList<SelectItem> ffs = new ArrayList<SelectItem>();
        for (MetadataFormat ffh : MetadataFormat.values()) {
            if (!ffh.equals(MetadataFormat.RDF)) {
                ffs.add(new SelectItem(ffh.getName(), null));
            }
        }
        return ffs;
    }

    public List<SelectItem> getFileFormatsInternalOnly() {
        ArrayList<SelectItem> ffs = new ArrayList<SelectItem>();
        for (MetadataFormat ffh : MetadataFormat.values()) {
            if (ffh.isUsableForInternal())
                if (!ffh.equals(MetadataFormat.RDF)) {
                    ffs.add(new SelectItem(ffh.getName(), null));
                }
        }
        return ffs;
    }

    public String getServletPathAsUrl() {
        FacesContext context = FacesContext.getCurrentInstance();
        return context.getExternalContext().getRequestContextPath() + "/";
    }

    public String getServletPathWithHostAsUrl() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        String scheme = request.getScheme(); // http
        String serverName = request.getServerName(); // hostname.com
        int serverPort = request.getServerPort(); // 80
        String contextPath = request.getContextPath(); // /mywebapp
        String reqUrl = scheme + "://" + serverName + ":" + serverPort + contextPath;
        return reqUrl;
    }

    public boolean getMessagesExist() {
        return FacesContext.getCurrentInstance().getMessages().hasNext();
    }

    //	public List<SelectItem> getCssFiles() {
    //		List<SelectItem> myList = new ArrayList<SelectItem>();
    //
    //		FacesContext context = FacesContext.getCurrentInstance();
    //		HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
    //		String filename = session.getServletContext().getRealPath("/css") + File.separator;
    //		File cssDir = new File(filename);
    //		FilenameFilter filter = new FilenameFilter() {
    //			@Override
    //			public boolean accept(File dir, String name) {
    //				return (name.endsWith(".css"));
    //			}
    //		};
    //
    //		String[] dateien = cssDir.list(filter);
    //		for (String string : dateien) {
    //			myList.add(new SelectItem("/css/" + string, string));
    //		}
    //		return myList;
    //	}

    //	/*
    //	 * method returns a valid css file, which is the suggestion unless
    //	 * suggestion is not available if not available default.css is returned
    //	 * 
    //	 * @author Wulf
    //	 * 
    //	 * @param suggested css file
    //	 * 
    //	 * @return valid css file
    //	 */
    //	public String getCssLinkIfExists(String cssFileName) {
    //		FacesContext context = FacesContext.getCurrentInstance();
    //		HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
    //		String filename = session.getServletContext().getRealPath(CSS_PATH) + File.separator;
    //		File cssDir = new File(filename);
    //		FilenameFilter filter = new FilenameFilter() {
    //			@Override
    //			public boolean accept(File dir, String name) {
    //				return (name.endsWith(".css"));
    //			}
    //		};
    //
    //		String[] dateien = cssDir.list(filter);
    //		for (String string : dateien) {
    //			if ((CSS_PATH + "/" + string).equals(cssFileName)) {
    //				return cssFileName;
    //			}
    //		}
    //		return CSS_PATH + "/default.css";
    //	}

    public TimeZone getTimeZone() {
        return TimeZone.getDefault();
    }

    public boolean getMassImportAllowed() {
        boolean value = false;
        if (ConfigMain.getBooleanParameter("massImportAllowed", false)) {
            return !PluginLoader.getPluginList(PluginType.Import).isEmpty();
        }
        return value;
    }

    public boolean getIsIE() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        if (request.getHeader("User-Agent").contains("MSIE")) {
            return true;
        } else {
            return false;
        }
    }

    public String getUserAgent() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();

        return request.getHeader("User-Agent");
    }

    public boolean isLdapIsWritable() {
        return ConfigMain.getBooleanParameter("ldap_use", true) && !ConfigMain.getBooleanParameter("ldap_readonly", false);
    }
}
