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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.goobi.beans.Process;
import org.goobi.beans.Processproperty;
import org.goobi.production.cli.helper.StringPair;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

@XmlRootElement(name = "process")
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestProcessResource {

    @Getter
    @Setter
    private int id;

    @Getter
    @Setter
    private String title;

    @Getter
    @Setter
    private String projectName;

    @Getter
    @Setter
    private Date creationDate;

    @Getter
    @Setter
    private String status;

    @Getter
    @Setter
    private Integer numberOfImages;

    @Getter
    @Setter
    private Integer numberOfMetadata;

    @Getter
    @Setter
    private Integer numberOfDocstructs;

    @Getter
    @Setter
    private String rulesetName;

    @Getter
    @Setter
    private Integer batchNumber;

    @Getter
    @Setter
    private String docketName;

    // only needed to create new processes
    @Getter
    @Setter
    private String processTemplateName;
    @Getter
    @Setter
    private String documentType;

    @Getter
    @Setter
    private List<Map<String, String>> propertiesList = new ArrayList<>();

    @Getter
    @Setter
    private List<Map<String, String>> metadataList = new ArrayList<>();

    @Getter
    @Setter
    private String metadataLevel = "topstruct";

    public RestProcessResource() {

    }

    public RestProcessResource(Process process) {
        id = process.getId();
        title = process.getTitel();
        projectName = process.getProjekt().getTitel();
        creationDate = process.getErstellungsdatum();
        status = process.getSortHelperStatus();
        numberOfImages = process.getSortHelperImages();
        numberOfMetadata = process.getSortHelperMetadata();
        numberOfDocstructs = process.getSortHelperDocstructs();
        rulesetName = process.getRegelsatz().getTitel();
        batchNumber = process.getBatch() == null ? null : process.getBatch().getBatchId();
        docketName = process.getDocket() == null ? null : process.getDocket().getName();

        initializePropertiesList(process.getEigenschaften());
        initializeMetadataList(process.getMetadataList());
    }

    private void initializePropertiesList(List<Processproperty> properties) {
        for (Processproperty property : properties) {
            String name = property.getPropertyName();
            String value = property.getPropertyValue();
            Map<String, String> propertyMap = new HashMap<>();
            propertyMap.put("name", name);
            propertyMap.put("value", value);
            propertiesList.add(propertyMap);
        }
    }

    private void initializeMetadataList(List<StringPair> list) {
        for (StringPair metadata : list) {
            String name = metadata.getOne();
            String value = metadata.getTwo();
            Map<String, String> metadataMap = new HashMap<>();
            metadataMap.put("name", name);
            metadataMap.put("value", value);
            metadataList.add(metadataMap);
        }
    }
}
