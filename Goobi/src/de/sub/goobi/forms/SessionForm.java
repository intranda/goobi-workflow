package de.sub.goobi.forms;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.goobi.beans.SessionInfo;
import org.goobi.beans.User;
import org.goobi.goobiScript.GoobiScriptManager;
import org.omnifaces.cdi.Push;
import org.omnifaces.cdi.PushContext;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.Helper;
import lombok.Getter;

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

 * Die Klasse SessionForm für den überblick über die aktuell offenen Sessions
 * 
 * @author Steffen Hankiewicz
 * @version 1.00 - 16.01.2005
 */

@Named("SessionForm")
@ApplicationScoped
public class SessionForm implements Serializable {

    /**
     * The version id for serializing processes
     */
    private static final long serialVersionUID = 8457947420232054227L;

    /**
     * The constant string to indicate that a user is logged out
     */
    private static final String LOGGED_OUT = " - ausgeloggt - ";

    /**
     * The constant string to indicate that a user is not logged in until now
     */
    private static final String NOT_LOGGED_IN = " - ";

    private List<SessionInfo> sessions = Collections.synchronizedList(new ArrayList<>());
    private SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("EEEE', ' dd. MMMM yyyy");
    private SimpleDateFormat fullFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    private String aktuelleZeit = this.formatter.format(new Date());
    private String bitteAusloggen = "";
    @Getter
    private String sessionListErrorTime = "";

    @Inject
    private HttpServletRequest request;
    @Inject
    @Push
    PushContext adminMessageChannel;
    @Inject
    @Getter
    private GoobiScriptManager gsm;

    /**
     * Returns the number of currently existing sessions
     *
     * @return The number of sessions
     */
    public int getAktiveSessions() {
        if (this.sessions == null) {
            return 0;
        } else {
            return this.sessions.size();
        }
    }

    public String getAktuelleZeit() {
        return this.aktuelleZeit;
    }

    /**
     * Returns a list of all currently existing sessions,
     * represented by a list of SessionInfo objects
     *
     * @return The list of SessionInfo objects
     */
    public List<SessionInfo> getAlleSessions() {
        try {
            return this.sessions;
        } catch (RuntimeException runtimeException) {
            runtimeException.printStackTrace();
            return null;
        }
    }

    public void publishAdminMessage() {
        adminMessageChannel.send("update");
    }

    /**
     * Adds a new session to the list of current sessions
     *
     * @param newSession The new session to add to the other sessions
     */
    private void sessionAdd(HttpSession newSession) {

        SessionInfo sessionInfo = new SessionInfo();
        long now = System.currentTimeMillis();

        sessionInfo.setSession(newSession);
        sessionInfo.setSessionId(newSession.getId());
        sessionInfo.setSessionCreatedTimestamp(now);
        sessionInfo.setSessionCreatedFormatted(this.formatter.format(now));
        sessionInfo.setLastAccessTimestamp(now);
        sessionInfo.setLastAccessFormatted(this.formatter.format(now));
        sessionInfo.setUserName(NOT_LOGGED_IN);
        sessionInfo.setUserId(0);
        sessionInfo.setUserTimeout(newSession.getMaxInactiveInterval());

        /*
        if (this.request == null) {
             FacesContext context = FacesContextHelper.getCurrentFacesContext();
             if (context != null) {
                 this.request = (HttpServletRequest) context.getExternalContext().getRequest();
            }
        }
        */

        if (this.request == null) {
            this.sessions.add(sessionInfo);
            return;
        }

        String address = this.request.getHeader("x-forwarded-for");
        if (address == null) {
            address = this.request.getRemoteAddr();
        }
        sessionInfo.setUserIpAddress(address);

        String browser = this.request.getHeader("User-Agent");
        if (browser == null) {
            browser = "-";
        }
        List<String> monitoringChecks = ConfigurationHelper.getInstance().getExcludeMonitoringAgentNames();
        for (String agent : monitoringChecks) {
            if (browser.contains(agent)) {
                return;
            }
        }
        sessionInfo.setBrowserName(browser);

        String[] browserNames = new String[]{
            "Gecko",
            "Firefox",
            "MSIE",
            "Opera",
            "Safari",
            "Chrome",
            "Konqueror",
            "Netscape"
        };
        String[] browserIcons = new String[]{
            "mozilla.png",
            "firefox.png",
            "ie.png",
            "opera.png",
            "safari.png",
            "chrome.png",
            "konqueror.png",
            "netscape.png"
        };
        String browserIcon = "none.png";
        for (int index = 0; index < browserNames.length; index++) {
            if (browser.contains(browserNames[index])) {
                browserIcon = browserIcons[index];
                break;
            }
        }
        sessionInfo.setBrowserIconFileName(browserIcon);

        this.sessions.add(sessionInfo);
    }

    /**
     * Removes all unused sessions. All sessions where the user is null,
     * the user has no name, the user is logged out or the IP address
     * is null, are unused.
     */
    private void sessionsAufraeumen() {
        for (int index = 0; index < this.sessions.size(); index++) {

            SessionInfo session = this.sessions.get(index);
            String userName = session.getUserName();
            long userTimeout = (long)(session.getUserTimeout());
            long loginTimestamp = (long)(session.getLastAccessTimestamp());
            long now = System.currentTimeMillis();
            long sessionDuration = (now - loginTimestamp) / 1000;

            boolean overTimeout = sessionDuration > userTimeout;
            //boolean notLoggedIn = userName.equals(NOT_LOGGED_IN);
            boolean loggedOut = userName.equals(LOGGED_OUT);
            boolean noAddress = session.getUserIpAddress() == null;

            if (overTimeout || loggedOut || noAddress) {
                this.sessions.remove(index);
                index--;
            }
        }
    }

    /**
     * Updates the time information about a session
     *
     * @param updatedSession The concerning session to update
     */
    public void sessionAktualisieren(HttpSession updatedSession) {
        if (this.sessions == null || updatedSession == null) {
            return;
        }

        String id = updatedSession.getId();
        SessionInfo knownSession = this.getSessionInfoById(id);
        if (knownSession == null) {
            this.sessionAdd(updatedSession);
            return;
        }

        long now = System.currentTimeMillis();
        knownSession.setLastAccessTimestamp(now);
        knownSession.setLastAccessFormatted(this.formatter.format(now));
    }

    /**
     * Updates the user information about a session in dependence of a certain user
     *
     * @param updatedSession The session to update
     * @param updatedUser The concerning user
     */
    public void sessionBenutzerAktualisieren(HttpSession updatedSession, User updatedUser) {
        if (this.sessions == null || updatedSession == null) {
            return;
        }

        String id = updatedSession.getId();
        SessionInfo knownSession = this.getSessionInfoById(id);

        if (knownSession == null) {
            SessionInfo newSession = new SessionInfo();
            newSession.setUserName(LOGGED_OUT);
            newSession.setUserId(0);
            newSession.setSessionId("-1");
            this.sessions.add(newSession);
            this.sessionsAufraeumen();
            return;
        }

        if (updatedUser == null) {
            knownSession.setUserName(LOGGED_OUT);
            updatedSession.setAttribute("User", LOGGED_OUT);
            knownSession.setUserId(0);
            this.sessionsAufraeumen();
            return;
        }

        String name = updatedUser.getNachVorname();
        int timeout = updatedUser.getSessiontimeout();

        knownSession.setUserName(name);
        updatedSession.setAttribute("User", name);
        knownSession.setUserId(updatedUser.getId());
        knownSession.setUserTimeout(timeout);
        updatedSession.setMaxInactiveInterval(timeout);
        this.sessionsAufraeumen();
    }

    /**
     * Returns the SessionInfo object with the specified id
     *
     * @param id The id of the requested SessionInfo object
     * @return The SessionInfo object with that id
     */
    public SessionInfo getSessionInfoById(String id) {
        for (int session = 0; session < this.sessions.size(); session++) {
            if (this.sessions.get(session).getSessionId().equals(id)) {
                return this.sessions.get(session);
            }
        }
        return null;
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

    /**
     * Returns the current date and time in dependence
     * of the current locale settings
     *
     * @return The formatted date string
     */
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
