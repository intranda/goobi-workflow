package org.goobi.api.rest;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.goobi.api.rest.model.RestUserInfo;
import org.goobi.beans.SessionInfo;

import de.sub.goobi.forms.SessionForm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

// Access with http://localhost:8080/goobi/api/currentusers
@Path("/currentusers")
public class CurrentUsers {

    @Inject
    private SessionForm sessionForm;

    @GET
    @Operation(summary = "Returns a list of the current users",
            description = "Returns a list with all users that are currently connected to this server")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "500", description = "Internal error")
    @Produces(MediaType.APPLICATION_JSON)
    public List<RestUserInfo> getCurrentUsers() {
        // Now return the expected list of users / sessions
        // Otherwise sessionForm stills null, this is a warning
        if (this.sessionForm != null) {
            return this.generateUserList();

        } else {
            return new ArrayList<>();
        }
    }

    private List<RestUserInfo> generateUserList() {

        List<SessionInfo> sessions = this.sessionForm.getAlleSessions();
        List<RestUserInfo> ruiList = new ArrayList<>();

        for (int index = 0; index < sessions.size(); index++) {
            RestUserInfo user = new RestUserInfo();
            SessionInfo session = sessions.get(index);

            user.setUser(session.getUserName());
            user.setAddress(session.getUserIpAddress());
            user.setBrowser(session.getBrowserName());
            user.setCreated(session.getSessionCreatedFormatted());
            user.setLast(session.getLastAccessFormatted());
            ruiList.add(user);
        }
        return (ruiList);
    }

}