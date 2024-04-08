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

package org.goobi.api.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import io.goobi.workflow.harvester.HarvesterGoobiImport;
import io.goobi.workflow.harvester.MetadataParser;
import io.goobi.workflow.harvester.repository.Repository;
import ugh.dl.Fileformat;
import ugh.dl.Prefs;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.fileformats.json.JsonFileformat;

@HarvesterGoobiImport(description = "Import Json Records")
@Path("/metadata/json")
public class JsonImportParser extends MetadataService implements MetadataParser, IRestAuthentication {

    @Override
    public List<AuthenticationMethodDescription> getAuthenticationMethods() {
        return Collections.emptyList();
    }

    @Override
    public Response createNewProcess(String projectName, String templateName, String processTitle, InputStream inputStream) {
        return createRecord(projectName, templateName, processTitle, inputStream);
    }

    @Override
    public Response replaceMetadata(Integer processid, InputStream inputStream) {
        return null;
    }

    @Override
    public void extendMetadata(Repository repository, java.nio.file.Path file) {
        // not implemented
    }

    @Override
    public Fileformat readMetadataFile(InputStream inputStream, Prefs prefs)
            throws ParserConfigurationException, SAXException, IOException, ReadException, PreferencesException, TypeNotAllowedForParentException {

        JsonFileformat fileformat = new JsonFileformat(prefs);
        fileformat.read(inputStream, "Thesis");

        return fileformat;
    }

}
