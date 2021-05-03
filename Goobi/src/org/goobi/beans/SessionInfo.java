package org.goobi.beans;

import java.io.Serializable;

import javax.servlet.http.HttpSession;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;
/**
 * This class represents a HTTPSession object and some more information about
 * a current session of a user. It is mainly used in the SessionForm class.
 *
 * @author Maurice Mueller
 *
 */
public class SessionInfo implements Serializable {

    /**
     * The version id for serializing processes
     */
    private static final long serialVersionUID = 8457947420232054227L;

    /**
     * The HTTP session object
     */
    @Getter
    @JsonIgnore
    @Setter
    private HttpSession session;

    /**
     * The id of the HTTP session object
     */
    @Getter
    @JsonIgnore
    @Setter
    private String sessionId;

    /**
     * The time stamp when the session was created (in milliseconds)
     */
    @Getter
    @JsonIgnore
    @Setter
    private long sessionCreatedTimestamp;

    /**
     * The time stamp when the session was created (formatted)
     */
    @Getter
    @Setter
    private String sessionCreatedFormatted;

    /**
     * The time stamp of the last access of the user in the GUI (in milliseconds)
     */
    @Getter
    @JsonIgnore
    @Setter
    private long lastAccessTimestamp;

    /**
     * The time stamp of the last access of the user in the GUI (formatted)
     */
    @Getter
    @Setter
    private String lastAccessFormatted;

    /**
     * The user id
     */
    @Getter
    @JsonIgnore
    @Setter
    private int userId;

    /**
     * The IP address of the user
     */
    @Getter
    @Setter
    private String userIpAddress;

    /**
     * The name of the user, represented by the last name and the first name
     */
    @Getter
    @Setter
    private String userName;

    /**
     * The timeout of the user in seconds
     */
    @Getter
    @JsonIgnore
    @Setter
    private int userTimeout;

    /**
     * The name of the browser of the user
     */
    @Getter
    @Setter
    private String browserName;

    /**
     * The file name of the fitting browser icon image
     */
    @Getter
    @JsonIgnore
    @Setter
    private String browserIconFileName;

    /**
     * A constructor to get a SessionInfo object.
     * All object variables can be set separately.
     */
    public SessionInfo() {
    }
}