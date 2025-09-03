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

package org.goobi.api.rest.model;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/view/video")
public class RestVideoVttResource {

    @GET
    @Path("/vtt")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getVttFile() {
        // check session, get Metadata class

        // load vtt from Metadata instance
        StringBuilder vtt = new StringBuilder();
        // get physical docstruct for video file

        // get total duration

        // find top logical docstruct for physical

        // find page areas

        // for each:

        // start, optional end (if end is empty, use start -1ms of next or file duration

        // get deepest logical, get title data
        vtt.append("WEBVTT - Sample");
        vtt.append("\n");
        vtt.append("\n");
        vtt.append("1");
        vtt.append("\n");
        vtt.append("00:00:00.000 --> 00:00:10.000");
        vtt.append("\n");
        vtt.append("Opening");
        vtt.append("\n");
        vtt.append("\n");
        vtt.append("2");
        vtt.append("\n");
        vtt.append("00:00:10.001 --> 00:00:20.000");
        vtt.append("\n");
        vtt.append("Main section");
        vtt.append("\n");
        vtt.append("\n");
        vtt.append("3");
        vtt.append("\n");
        vtt.append("00:00:20.001 --> 00:00:30.527");
        vtt.append("\n");
        vtt.append("End credits");
        vtt.append("\n");
        vtt.append("\n");
        String content = vtt.toString();

        return Response.ok(content, MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=\"video.vtt\"")
                .build();

    }

}
