/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *             - https://goobi.io
 *             - https://www.intranda.com
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
import lombok.Setter;

// Access with http://localhost:8080/goobi/api/currentusers
@Path("/currentusers")
public class CurrentUsers {

    @Inject
    @Setter
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