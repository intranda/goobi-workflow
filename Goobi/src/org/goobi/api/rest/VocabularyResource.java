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

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.goobi.production.cli.helper.StringPair;
import org.goobi.vocabulary.VocabRecord;
import org.goobi.vocabulary.Vocabulary;

/**
 * This class represents a rest endpoint to search within a vocabulary
 * 
 */

import de.sub.goobi.persistence.managers.VocabularyManager;

@Path("/vocabulary")
public class VocabularyResource {

    /**
     * Search for a term within all fields of a vocabulary
     * 
     * @param vocabulary
     * @param searchvalue
     * @return
     */

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{vocabulary}/{searchvalue}")
    public Response findRecords(@PathParam("vocabulary") String vocabulary, @PathParam("searchvalue") String searchvalue) {
        List<VocabRecord> records = VocabularyManager.findRecords(vocabulary, searchvalue);
        Response response = Response.ok(records).build();
        return response;
    }

    /**
     * Search for a term within a field of a vocabulary
     * 
     * @param vocabulary
     * @param fieldname
     * @param searchvalue
     * @return
     */

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
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{vocabulary}")
    public Response findRecords(@PathParam("vocabulary") String vocabulary, List<StringPair> data) {
        List<VocabRecord> records = VocabularyManager.findRecords(vocabulary,data);
        Response response = Response.ok(records).build();
        return response;
    }

    /**
     * get a complete vocabulary including all records. Can be used to display drop down lists in mets editor
     * 
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{vocabulary}")
    public Response getVocabulary(@PathParam("vocabulary") String vocabularyName) {
        Vocabulary vocabulary = VocabularyManager.getVocabularyByTitle(vocabularyName);
        VocabularyManager.loadRecordsForVocabulary(vocabulary);
        Response response = Response.ok(vocabulary).build();
        return response;
    }

    /**
     * get a VocabRecord from url
     * 
     */

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("records/{vocabulary}/{record}")
    public Response getRecord(@PathParam("vocabulary") Integer vocabularyId, @PathParam("record") Integer recordId) {

        VocabRecord record = VocabularyManager.getRecord(vocabularyId, recordId);
        Response response = Response.ok(record).build();
        return response;
    }


}
