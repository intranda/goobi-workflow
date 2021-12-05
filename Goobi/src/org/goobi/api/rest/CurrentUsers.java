package org.goobi.api.rest;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.goobi.beans.SessionInfo;

import de.sub.goobi.forms.SessionForm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

// Access with http://localhost:8080/goobi/api/currentusers
@Path("/currentusers")
public class CurrentUsers {

    @Inject
    private SessionForm sessionForm;

    /**
     * Returns the list of current users. The list of current users is stored in SessionForm. The list is of type List<SessionInfo>. All irrelevant
     * fields of SessionInfo are excluded with JsonIgnore annotation. The resulting list will contain the name of a user, the IP address, the login
     * time, the last access time and the name of the used browser.
     *
     * @return The list of current users as a list of SessionInfo objects.
     */
    @GET
    @Operation(summary = "Returns a list of the current users",
            description = "Returns a list with all users that are currently connected to this server")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "500", description = "Internal error")
    @Produces(MediaType.APPLICATION_JSON)
    public List<SessionInfo> getCurrentUsers() {

        // Per injection this.sessionForm should not be null.
        // To avoid the case of a NullPointerException in all other cases,
        // null is explicitly replaced by an empty list.

        if (this.sessionForm != null) {
            return this.sessionForm.getSessions();
        } else {
            return new ArrayList<>();
        }
    }
}