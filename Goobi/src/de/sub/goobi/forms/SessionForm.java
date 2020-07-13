package de.sub.goobi.forms;

import java.io.Serializable;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *     		- https://goobi.io
 * 			- https://www.intranda.com
 * 			- https://github.com/intranda/goobi-workflow
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.goobi.beans.User;
import org.goobi.goobiScript.GoobiScriptManager;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.Helper;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * Die Klasse SessionForm für den überblick über die aktuell offenen Sessions
 * 
 * @author Steffen Hankiewicz
 * @version 1.00 - 16.01.2005
 */

@Log4j2
@Named("SessionForm")
@ApplicationScoped
public class SessionForm implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 8457947420232054227L;
    @SuppressWarnings("rawtypes")
    private List<Map> alleSessions = Collections.synchronizedList(new ArrayList<Map>());
    private SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("EEEE', ' dd. MMMM yyyy");
    private SimpleDateFormat fullFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    private String aktuelleZeit = this.formatter.format(new Date());
    private String bitteAusloggen = "";
    @Getter
    private String sessionListErrorTime = "";
    @Getter
    private GoobiScriptManager gsm = new GoobiScriptManager();

    public int getAktiveSessions() {
        if (this.alleSessions == null) {
            return 0;
        } else {
            return this.alleSessions.size();
        }
    }

    public String getAktuelleZeit() {
        return this.aktuelleZeit;
    }

    @SuppressWarnings("rawtypes")
    public List getAlleSessions() {
        try {
            return this.alleSessions;
        } catch (RuntimeException e) {
            return null;
        }
    }

    @Inject
    private HttpServletRequest request;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void sessionAdd(HttpSession insession) {
        Map map = new HashMap<>();
        map.put("id", insession.getId());
        map.put("created", this.formatter.format(new Date()));
        map.put("last", this.formatter.format(new Date()));
        map.put("last2", Long.valueOf(System.currentTimeMillis()));
        map.put("user", " - ");
        map.put("userid", Integer.valueOf(0));
        map.put("session", insession);
        map.put("browserIcon", "none.png");
        //        FacesContext context = FacesContextHelper.getCurrentFacesContext();
        //        if (context != null) {
        //            HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        if (request != null) {
            String address = request.getHeader("x-forwarded-for");
            if (address == null) {
                address = request.getRemoteAddr();
            }

            map.put("address", address);

            String mybrowser = request.getHeader("User-Agent");
            if (mybrowser == null) {
                mybrowser = "-";
            }
            List<String> monitoringChecks = ConfigurationHelper.getInstance().getExcludeMonitoringAgentNames();
            for (String agent : monitoringChecks) {
                if (mybrowser.contains(agent)) {
                    return;
                }
            }
            map.put("browser", mybrowser);
            if (mybrowser.indexOf("Gecko") > 0) {
                map.put("browserIcon", "mozilla.png");
            }
            if (mybrowser.indexOf("Firefox") > 0) {
                map.put("browserIcon", "firefox.png");
            }
            if (mybrowser.indexOf("MSIE") > 0) {
                map.put("browserIcon", "ie.png");
            }
            if (mybrowser.indexOf("Opera") > 0) {
                map.put("browserIcon", "opera.png");
            }
            if (mybrowser.indexOf("Safari") > 0) {
                map.put("browserIcon", "safari.png");
            }
            if (mybrowser.indexOf("Chrome") > 0) {
                map.put("browserIcon", "chrome.png");
            }
            if (mybrowser.indexOf("Konqueror") > 0) {
                map.put("browserIcon", "konqueror.png");
            }
            if (mybrowser.indexOf("Netscape") > 0) {
                map.put("browserIcon", "netscape.png");
            }
        }
        this.alleSessions.add(map);
    }

    @SuppressWarnings("rawtypes")
    private void sessionsAufraeumen(int time) {
        List<Map> temp = new ArrayList<>(this.alleSessions);
        for (Map map : temp) {
            long differenz = System.currentTimeMillis() - ((Long) map.get("last2")).longValue();
            if (differenz / 1000 > time || map.get("address") == null || (map.get("user").equals("- ausgeloggt - "))) {
                this.alleSessions.remove(map);
            }
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void sessionAktualisieren(HttpSession insession) {
        boolean gefunden = false;
        this.aktuelleZeit = this.formatter.format(new Date());
        if (alleSessions != null && insession != null) {
            for (Map map : alleSessions) {
                if (map.get("id").equals(insession.getId())) {
                    map.put("last", this.formatter.format(new Date()));
                    map.put("last2", Long.valueOf(System.currentTimeMillis()));
                    gefunden = true;
                    break;
                }
            }
        }
        if (!gefunden) {
            sessionAdd(insession);
        }
        sessionsAufraeumen(insession.getMaxInactiveInterval());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void sessionBenutzerAktualisieren(HttpSession insession, User inBenutzer) {
        // logger.debug("sessionBenutzerAktualisieren-start");
        if (alleSessions != null && insession != null) {
            for (Map map : alleSessions) {
                if (map != null && map.get("id").equals(insession.getId())) {
                    if (inBenutzer != null) {
                        insession.setAttribute("User", inBenutzer.getNachVorname());
                        map.put("user", inBenutzer.getNachVorname());
                        map.put("userid", inBenutzer.getId());
                        insession.setMaxInactiveInterval(inBenutzer.getSessiontimeout());
                    } else {
                        map.put("user", "- ausgeloggt - ");
                        map.put("userid", Integer.valueOf(0));
                    }
                    break;
                }
            }
        }
    }

    public String getBitteAusloggen() {
        return this.bitteAusloggen;
    }

    public void setBitteAusloggen(String bitteAusloggen) {
        this.bitteAusloggen = bitteAusloggen;
    }

    public String sendLogoutMessage() {
        return "admin";
    }

    public String getDate() {
        if (dateFormatter == null) {
            Locale language = Locale.ENGLISH;
            SpracheForm sf = Helper.getLanguageBean();
            if (sf != null) {
                language = sf.getLocale();
            }
            dateFormatter = new SimpleDateFormat("EEEE', ' dd. MMMM yyyy", language);
        }
        return dateFormatter.format(new Date());
    }

    public void setDateFormatter(SimpleDateFormat dateFormatter) {
        this.dateFormatter = dateFormatter;
    }

}
