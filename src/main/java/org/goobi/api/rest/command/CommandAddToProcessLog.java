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
 */
package org.goobi.api.rest.command;

import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.production.enums.LogType;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.StepManager;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

@Deprecated
@Path("/addtoprocesslog")
public class CommandAddToProcessLog {

    /**
     * Adds message to process log of process identified by process title.
     *
     * @param processTitle
     * @param type
     * @param value
     * @return Response
     */
    @Deprecated
    @POST
    @Path("/processtitles/{processtitle}/{type}")
    public Response addToLogByProcessTitle(@PathParam("processtitle") String processTitle, @PathParam("type") String type, String value) {
        Process process = ProcessManager.getProcessByExactTitle(processTitle);
        return addToLog(type, value, process);
    }

    @Deprecated
    @POST
    @Path("/steps/{stepid}/{type}")
    public Response addToLogByStepId(@PathParam("stepid") Integer id, @PathParam("type") String type, String value) {
        Process process = null;
        int processId = 0;

        Step so = StepManager.getStepById(id);
        if (so == null) {
            String message = "Could not load step with id: " + id;
            return Response.status(500).entity(message).build();
            //              return new CommandResponse(title, message);
        }
        processId = so.getProcessId();
        process = ProcessManager.getProcessById(processId);

        return addToLog(type, value, process);
    }

    @Deprecated
    @POST
    @Path("/processes/{processid}/{type}")
    public Response addToLogByProcessId(@PathParam("processid") Integer processId, @PathParam("type") String type, String value) {
        Process process = null;

        process = ProcessManager.getProcessById(processId);

        if (process == null) {
            String message = "Could not load process with id: " + processId;
            return Response.status(500).entity(message).build();
        }

        return addToLog(type, value, process);
    }

    private Response addToLog(String type, String value, Process process) {
        Helper.addMessageToProcessJournal(process.getId(), LogType.getByTitle(type), value, "webapi");

        return Response.ok().build();
    }
}
