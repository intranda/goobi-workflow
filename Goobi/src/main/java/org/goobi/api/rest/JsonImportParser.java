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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.SAXException;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;

import de.sub.goobi.helper.Helper;
import io.goobi.workflow.harvester.HarvesterGoobiImport;
import io.goobi.workflow.harvester.MetadataParser;
import io.goobi.workflow.harvester.repository.Repository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.Prefs;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.exceptions.UGHException;
import ugh.fileformats.mets.MetsMods;

@HarvesterGoobiImport(description = "Import Json Records")
@Path("/metadata/json")
@Log4j2
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
    }

    @Override
    public Fileformat readMetadataFile(InputStream inputStream, Prefs prefs)
            throws ParserConfigurationException, SAXException, IOException, ReadException, PreferencesException, TypeNotAllowedForParentException {

        // open configuration file

        XMLConfiguration conf = getConfiguration();

        List<MappingField> jsonFields = readJsonFields(conf);

        List<MappingField> additionalFields = readAdditionalFields(conf);

        // read input stream
        String jsonString = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        // convert into object
        Object jsonObj = Configuration.defaultConfiguration().jsonProvider().parse(jsonString);

        // parse fields

        for (MappingField mf : jsonFields) {
            String value = mf.getValue();
            switch (mf.getType().toLowerCase()) {
                case "string":
                    try {
                        value = JsonPath.read(jsonObj, mf.getJsonPath());
                    } catch (Exception e) {
                        // ignore missing fields
                    }
                    break;
                case "number":
                    try {
                        int val = JsonPath.read(jsonObj, mf.getJsonPath());
                        value = String.valueOf(val);
                    } catch (Exception e) {
                        // ignore missing fields
                    }
                    break;
                case "boolean":
                    try {
                        boolean val = JsonPath.read(jsonObj, mf.getJsonPath());
                        value = String.valueOf(val);
                    } catch (Exception e) {
                        // ignore missing fields
                    }
                    break;
                default:
                    break;
            }
            mf.setValue(value);
        }

        // create docstruct

        // add metadata

        Fileformat fileformat = new MetsMods(prefs);

        DigitalDocument digDoc = new DigitalDocument();
        fileformat.setDigitalDocument(digDoc);

        DocStruct logical = digDoc.createDocStruct(prefs.getDocStrctTypeByName(conf.getString("/docstruct")));
        digDoc.setLogicalDocStruct(logical);

        for (MappingField mf : jsonFields) {
            if (StringUtils.isNotBlank(mf.getValue())) {
                addMetadata(prefs, mf.getMetadataName(), mf.getValue(), logical);
            }
        }

        for (MappingField mf : additionalFields) {
            if (StringUtils.isNotBlank(mf.getValue())) {
                addMetadata(prefs, mf.getMetadataName(), mf.getValue(), logical);
            }
        }

        return fileformat;
    }

    private List<MappingField> readJsonFields(XMLConfiguration conf) {
        List<MappingField> answer = new ArrayList<>();

        List<HierarchicalConfiguration> fields = conf.configurationsAt("/jsonFields/field");
        for (HierarchicalConfiguration hc : fields) {
            answer.add(new MappingField(hc.getString("@metadataName"), hc.getString("@path"), hc.getString("@type", "String"),
                    hc.getString("@default")));
        }
        return answer;
    }

    private List<MappingField> readAdditionalFields(XMLConfiguration conf) {
        List<MappingField> answer = new ArrayList<>();

        List<HierarchicalConfiguration> fields = conf.configurationsAt("/staticFields/field");
        for (HierarchicalConfiguration hc : fields) {
            answer.add(new MappingField(hc.getString("@metadataName"), "", "",
                    hc.getString("@value")));
        }
        return answer;
    }

    private void addMetadata(Prefs prefs, String metadataType, String value, DocStruct logical) {
        try {
            Metadata title = new Metadata(prefs.getMetadataTypeByName(metadataType));
            title.setValue(value);
            logical.addMetadata(title);
        } catch (UGHException e) {
            log.error(e);
        }
    }

    private static XMLConfiguration getConfiguration() {
        XMLConfiguration config = new XMLConfiguration();
        config.setDelimiterParsingDisabled(true);
        try {
            config.load(new Helper().getGoobiConfigDirectory() + "goobi_jsonParser.xml");
        } catch (ConfigurationException e) {
            log.error("Error while reading the json configuration file ", e);
        }
        config.setReloadingStrategy(new FileChangedReloadingStrategy());
        config.setDelimiterParsingDisabled(true);
        config.setExpressionEngine(new XPathExpressionEngine());
        return config;
    }

    @Getter
    @AllArgsConstructor
    private class MappingField {

        // name of the metadata field
        private String metadataName;

        // JSONPath to the value within the json object
        private String jsonPath;

        // type of the json field
        private String type;

        // actual value
        @Setter
        private String value;

    }
}
