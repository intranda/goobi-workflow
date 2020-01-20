package org.goobi.api.rest;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *          - https://goobi.io
 *          - https://www.intranda.com
 *          - https://github.com/intranda/goobi
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
 */

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.goobi.vocabulary.VocabRecord;

/**
 * This class represents a rest endpoint to search within a vocabulary
 * 
 */

import de.sub.goobi.persistence.managers.VocabularyManager;

@Path("/vocabulary")
public class VocabularyResource {
    //    http://localhost:8080/Goobi/api/vocabulary/test/titel
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{vocabulary}/{searchvalue}")
    public Response findRecords(@PathParam("vocabulary") String vocabulary, @PathParam("searchvalue") String searchvalue) {
        List<VocabRecord> records = VocabularyManager.findRecords(vocabulary, searchvalue, null);
        Response response = Response.ok(records).build();
        return response;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{vocabulary}/{fieldname}/{searchvalue}")
    public Response findRecords(@PathParam("vocabulary") String vocabulary, @PathParam("fieldname") String fieldname,
            @PathParam("searchvalue") String searchvalue) {
        List<VocabRecord> records = VocabularyManager.findRecords(vocabulary, searchvalue, fieldname);
        Response response = Response.ok(records).build();
        return response;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response findRecords() {
        // TODO send complex query via post
        return null;
    }

    /**
     * get a complete vocabulary including all records. Can be used to display drop down lists in mets editor
     * 
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{vocabulary}")
    public Response getVocabulary(@PathParam("vocabulary") String vocabulary) {

        return null;
    }


}
