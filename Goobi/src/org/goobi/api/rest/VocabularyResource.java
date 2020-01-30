package org.goobi.api.rest;

import java.util.ArrayList;
import java.util.HashMap;

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
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.goobi.production.cli.helper.StringPair;
import org.goobi.vocabulary.Definition;
import org.goobi.vocabulary.Field;
import org.goobi.vocabulary.JskosRecord;
import org.goobi.vocabulary.JskosRecord.Fields;
import org.goobi.vocabulary.JskosRecord.Publisher;
import org.goobi.vocabulary.JskosRecord.Schema;
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
        List<VocabRecord> records = VocabularyManager.findRecords(vocabulary, data);
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
    public Response getVocabularyByName(@PathParam("vocabulary") String vocabularyName) {
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

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("records/jskos/{vocabulary}/{record}")
    public Response getRecordAsJskos(@Context UriInfo uriInfo, @PathParam("vocabulary") Integer vocabularyId, @PathParam("record") Integer recordId) {
        VocabRecord record = VocabularyManager.getRecord(vocabularyId, recordId);
        if (record == null) {
            return Response.status(404).build();
        }
        Vocabulary vocabulary = VocabularyManager.getVocabularyById(vocabularyId);

        String uri = uriInfo.getBaseUri() + "vocabulary/records/jskos/" + vocabularyId + "/" + recordId;

        JskosRecord jskosRecord = new JskosRecord();
        jskosRecord.setUri(uri);
        List<String> types = new ArrayList<>();
        types.add("http://www.w3.org/2004/02/skos/core#Concept");
        jskosRecord.setType(types);
        jskosRecord.setContext("https://goobi.io/");

        Schema schema = jskosRecord.new Schema();
        schema.setUri(uriInfo.getBaseUri() + "vocabulary/" + vocabulary.getTitle());
        Map<String, String> prefLabel = new HashMap<>();
        prefLabel.put("title", vocabulary.getTitle());
        prefLabel.put("description", vocabulary.getDescription());
        List<String> schemaType = new ArrayList<>();
        schemaType.add("http://www.w3.org/2004/02/skos/core#ConceptScheme");
        schemaType.add("http://w3id.org/nkos/nkostype#list");
        schema.setType(schemaType);
        Fields schemaPrefLabel  =jskosRecord.new Fields();
        schemaPrefLabel.setValues(prefLabel);
        schema.setPrefLabel(schemaPrefLabel);
        List<org.goobi.vocabulary.JskosRecord.Schema> inScheme = new ArrayList<>();
        inScheme.add(schema);
        jskosRecord.setInScheme(inScheme);

        Publisher publisher = jskosRecord.new Publisher();
        Fields publisherPrefLabel  =jskosRecord.new Fields();
        publisherPrefLabel.setValue("Goobi");
        publisher.setPrefLabel(publisherPrefLabel);
        publisher.setUri(uriInfo.getBaseUri().toString());
        jskosRecord.setPublisher(publisher);

        String mainFieldName = "";
        for (Definition definition : vocabulary.getStruct()) {
            if (definition.isMainEntry()) {
                mainFieldName = definition.getLabel();
            }
        }
        Map<String, String> mainFields = new HashMap<>();
        List<String> otherFields = new ArrayList<>();
        for (Field field : record.getFields()) {
            if (field.getLabel().equals(mainFieldName)) {
                mainFields.put(StringUtils.isBlank(field.getLanguage()) ? "-" : field.getLanguage(), field.getValue());
            } else if (!otherFields.contains(field.getLabel())) {
                otherFields.add(field.getLabel());
            }
        }
        Fields jskosPrefLabel = jskosRecord.new Fields();
        if (StringUtils.isNotBlank(mainFields.get("-"))) {
            jskosPrefLabel.setValue(mainFields.get("-"));
        } else {
            jskosPrefLabel.setValues(mainFields);
        }
        jskosRecord.setPrefLabel(jskosPrefLabel);

        Map<String, Fields> otherFieldValues = new HashMap<>();
        for (String fieldName : otherFields) {
            Map<String, String> values = new HashMap<>();
            for (Field field : record.getFields()) {
                if (field.getLabel().equals(fieldName)) {
                    values.put(StringUtils.isBlank(field.getLanguage()) ? "-" : field.getLanguage(), field.getValue());
                }
            }
            Fields jskosField = jskosRecord.new Fields();
            if (StringUtils.isNotBlank(values.get("-"))) {
                jskosField.setValue(values.get("-"));
            } else {
                jskosField.setValues(values);
            }

            otherFieldValues.put(fieldName,jskosField);
        }
        jskosRecord.setFields(otherFieldValues);

        Response response = Response.ok(jskosRecord).build();
        return response;
    }

}
