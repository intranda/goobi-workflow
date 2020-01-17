package org.goobi.api.rest;


import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.goobi.vocabulary.VocabRecord;

import de.sub.goobi.persistence.managers.VocabularyManager;


@javax.ws.rs.Path("/vocabulary")
public class VocabularyResource {
    //    http://localhost:8080/Goobi/api/vocabulary/test/titel
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @javax.ws.rs.Path("{vocabulary}/{searchvalue}")
    public Response findRecords(@PathParam("vocabulary")String vocabulary, @PathParam("searchvalue")String searchvalue) {

        List<VocabRecord> records = VocabularyManager.findRecords(vocabulary, searchvalue, null);

        Response response =Response.ok(records).build();

        return response;
    }



}
