package de.sub.goobi.forms;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.goobi.beans.Browser;
import org.goobi.beans.SessionInfo;
import org.goobi.beans.User;
import org.goobi.goobiScript.GoobiScriptManager;
import org.goobi.managedbeans.LoginBean;
import org.omnifaces.cdi.Push;
import org.omnifaces.cdi.PushContext;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.Helper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. - https://goobi.io - https://www.intranda.com - https://github.com/intranda/goobi-workflow
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
 * 
 * Die Klasse SessionForm für den überblick über die aktuell offenen Sessions
 * 
 * @author Steffen Hankiewicz
 * @author Maurice Mueller
 * @version 1.00 - 16.01.2005
 * @version 2.00 - 03.05.2021
 */

@Log4j2
@Named("SessionForm")
@ApplicationScoped
public class SessionForm implements Serializable {

    /**
     * The version id for serializing processes
     */
    private static final long serialVersionUID = 8457947420232054227L;

    /**
     * The constant string to indicate that a user is logged out.
     */
    public static final String LOGGED_OUT = " - ausgeloggt - ";

    /**
     * The constant string to indicate that a user is not logged in until now.
     */
    public static final String NOT_LOGGED_IN = " - ";

    /**
     * The list of current sessions (represented by SessionInfo objects)
     */
    private List<SessionInfo> sessions = Collections.synchronizedList(new ArrayList<>());

    /**
     * The formatter that is used for time representation strings
     */
    private SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss");

    /**
     * The formatter that is used for date representation strings
     */
    @Setter
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("EEEE', ' dd. MMMM yyyy");

    /**
     * The request object of the current session
     */
    @Inject //NOSONAR needs to be a field injection, as the been constructor does not allow arguments
    private HttpServletRequest request;

    /**
     * A message that can be shown when a user should be logged out.
     */
    @Getter
    private String logoutMessage = "";

    @Getter
    private String sessionListErrorTime = "";

    @Inject //NOSONAR needs to be a field injection, as the been constructor does not allow arguments
    @Push
    PushContext adminMessageChannel;

    @Inject //NOSONAR needs to be a field injection, as the been constructor does not allow arguments
    @Getter
    private GoobiScriptManager gsm;

    /**
     * Returns a list of all currently existing sessions, represented by a list of SessionInfo objects.
     *
     * @return The list of SessionInfo objects
     */
    public List<SessionInfo> getSessions() {
        if (this.sessions != null) {
            this.removeAbandonedSessions(false);
            return this.sessions;
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Filters the sessions by real user sessions. All sessions that contain a name unequal to "-" are returned.
     *
     * @return The list of sessions with real users
     */
    private List<SessionInfo> filterRealUserSessions() {
        List<SessionInfo> realUserSessions = new ArrayList<>();
        for (int index = 0; index < this.sessions.size(); index++) {
            if (!SessionForm.NOT_LOGGED_IN.equals(this.sessions.get(index).getUserName())) {
                realUserSessions.add(this.sessions.get(index));
            }
        }
        return realUserSessions;
    }

    /**
     * Returns the SessionInfo object with the specified id.
     *
     * @param id The id of the requested SessionInfo object
     * @return The SessionInfo object with that id
     */
    public SessionInfo getSessionInfoById(String id) {
        for (SessionInfo element : this.sessions) {
            if (element.getSessionId().equals(id)) {
                return element;
            }
        }
        return null;
    }

    /**
     * Returns the number of currently existing sessions.
     *
     * @return The number of sessions
     */
    public int getNumberOfSessions() {
        if (this.sessions != null) {
            this.removeAbandonedSessions(false);
            return this.sessions.size();
        } else {
            return 0;
        }
    }

    /**
     * Returns the number of currently existing sessions. The list of sessions is filtered for real user sessions.
     *
     * @return The number of real user sessions
     */
    public int getNumberOfRealUserSessions() {
        if (this.sessions != null) {
            return this.filterRealUserSessions().size();
        } else {
            return 0;
        }
    }

    /**
     * Adds a new session to the list of current sessions
     *
     * @param newSession The new session to add to the other sessions
     */
    private void addSession(HttpSession newSession) {

        SessionInfo sessionInfo = new SessionInfo();
        long now = System.currentTimeMillis();

        sessionInfo.setSession(newSession);
        sessionInfo.setSessionId(newSession.getId());
        sessionInfo.setSessionCreatedTimestamp(now);
        sessionInfo.setSessionCreatedFormatted(this.timeFormatter.format(now));
        sessionInfo.setLastAccessTimestamp(now);
        sessionInfo.setLastAccessFormatted(this.timeFormatter.format(now));
        sessionInfo.setUserName(NOT_LOGGED_IN);
        sessionInfo.setUserId(0);
        sessionInfo.setUserTimeout(newSession.getMaxInactiveInterval());

        if (this.request == null) {
            this.sessions.add(sessionInfo);
            return;
        }

        String address = this.request.getHeader("x-forwarded-for");
        if (address == null) {
            address = this.request.getRemoteAddr();
        }
        sessionInfo.setUserIpAddress(address);

        String browserName = this.request.getHeader("User-Agent");
        if (browserName == null) {
            browserName = "-";
        }
        List<String> monitoringChecks = ConfigurationHelper.getInstance().getExcludeMonitoringAgentNames();
        for (String agent : monitoringChecks) {
            if (browserName.contains(agent)) {
                return;
            }
        }
        sessionInfo.setBrowserName(browserName);

        Browser browser = Browser.parseBrowser(browserName);
        sessionInfo.setBrowserIconFileName(Browser.getIconFileName(browser));

        this.sessions.add(sessionInfo);
    }

    /**
     * Updates the time information about a session.
     *
     * @param updatedSession The concerning session to update
     */
    public void updateSessionLastAccess(HttpSession updatedSession) {
        if (this.sessions == null || updatedSession == null) {
            return;
        }

        String id = updatedSession.getId();
        SessionInfo knownSession = this.getSessionInfoById(id);
        if (knownSession == null) {
            this.addSession(updatedSession);
            return;
        }

        long now = System.currentTimeMillis();
        knownSession.setLastAccessTimestamp(now);
        knownSession.setLastAccessFormatted(this.timeFormatter.format(now));

        // This is needed to remove out-of-timeout-sessions
        this.removeAbandonedSessions(true);
    }

    /**
     * Updates the user information about a session in dependence of a certain user.
     *
     * @param updatedSession The session to update
     * @param updatedUser The concerning user
     */
    public void updateSessionUserName(HttpSession updatedSession, User updatedUser) {
        if (this.sessions == null || updatedSession == null) {
            return;
        }

        String id = updatedSession.getId();
        SessionInfo knownSession = this.getSessionInfoById(id);

        if (knownSession == null) {
            log.trace(LoginBean.LOGIN_LOG_PREFIX + "Created new session for user.");
            SessionInfo newSession = new SessionInfo();
            newSession.setUserName(LOGGED_OUT);
            newSession.setUserId(0);
            newSession.setSessionId("-1");
            this.sessions.add(newSession);
            this.removeAbandonedSessions(true);
            return;
        }

        if (updatedUser == null) {
            knownSession.setUserName(LOGGED_OUT);
            updatedSession.setAttribute("User", LOGGED_OUT);
            knownSession.setUserId(0);
            this.removeAbandonedSessions(true);
            return;
        }
        log.trace(LoginBean.LOGIN_LOG_PREFIX + "Session already exists and will be overwritten with new session.");

        String name = updatedUser.getNachVorname();
        int timeout = updatedUser.getSessiontimeout();

        knownSession.setUserName(name);
        updatedSession.setAttribute("User", name);
        knownSession.setUserId(updatedUser.getId());
        knownSession.setUserTimeout(timeout);
        updatedSession.setMaxInactiveInterval(timeout);
        log.trace(LoginBean.LOGIN_LOG_PREFIX + "Removing old sessions...");
        this.removeAbandonedSessions(true);
        log.trace(LoginBean.LOGIN_LOG_PREFIX + "Sessions list is up to date.");
    }

    /**
     * Removes all unused sessions. All sessions where the user is null, the user has no name, the user is logged out or the IP address is null, are
     * unused.
     *
     * @param logKeptSessions Must be true to log all sessions (kept and removed) and must be false to only log removed sessions.
     */
    private void removeAbandonedSessions(boolean logKeptSessions) {
        int index = 0;
        while (index < this.sessions.size()) {

            SessionInfo session = this.sessions.get(index);
            String userName = session.getUserName();
            long userTimeout = (session.getUserTimeout());
            long loginTimestamp = (session.getLastAccessTimestamp());
            long now = System.currentTimeMillis();
            long sessionDuration = (now - loginTimestamp) / 1000;
            String counter = (index + 1) + "/" + this.sessions.size();
            StringBuilder message = new StringBuilder();
            message.append("Session " + counter);
            message.append("\n- login name:  " + userName);
            message.append("\n- browser:     " + session.getBrowserName());
            message.append("\n- ip address:  " + session.getUserIpAddress());
            message.append("\n- timeout:     " + userTimeout + " seconds");
            message.append("\n- last access: " + sessionDuration + " seconds");
            message.append("\n- session id:  " + session.getSessionId());

            boolean overTimeout = sessionDuration > userTimeout;
            // sessionDuration > 0 is needed to not remove the login screen while the user logs in
            boolean loggedOut = LOGGED_OUT.equals(userName) || (NOT_LOGGED_IN.equals(userName) && sessionDuration > 0);
            boolean noAddress = session.getUserIpAddress() == null;

            if (overTimeout || loggedOut || noAddress) {
                message.append("\nSession " + counter + " will be removed because timeout is exceeded or session is abandoned.");
                log.trace(message.toString());
                this.sessions.remove(index);
            } else {
                message.append("\nSession " + counter + " is valid and is kept in the sessions list.");
                if (logKeptSessions) {
                    log.trace(message.toString());
                }
                index++;
            }
        }
    }

    /**
     * Returns the current time, formatted as HH:MM:SS.
     *
     * @return The current time as string representation
     */
    public String getCurrentTime() {
        return timeFormatter.format(new Date());
    }

    /**
     * Returns the current date and time in dependence of the current locale settings.
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

    /**
     * Sets the logout message.
     *
     * @param message The new logout message
     */
    public void setLogoutMessage(String message) {
        this.logoutMessage = message;
        this.publishAdminMessage();
    }

    public String sendLogoutMessage() {
        return "admin";
    }

    public void publishAdminMessage() {
        adminMessageChannel.send("update");
    }

}
