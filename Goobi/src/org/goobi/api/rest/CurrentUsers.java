package org.goobi.api.rest;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

// Access with http://localhost:8080/goobi/api/currentusers 
@Path("/currentusers")
public class CurrentUsers {

    @GET
    //@Path("/currentusers/json")
    @Produces(MediaType.APPLICATION_JSON)
    public String[] getCurrentUsers() {
        String[] currentUsers = new String[2];
        currentUsers[0] = "Linus";
        currentUsers[1] = "Tux";
        return currentUsers;
    }
    
    @GET
    //@Path("/currentusers/html")
    @Produces(MediaType.TEXT_HTML)
    public String getCurrentUsersHTML() {
        return "<!DOCTYPE html><html><head><title>Current users</title></head><body><ul><li>Linus</li><li>Tux</li></ul></body></html>";
    }
}