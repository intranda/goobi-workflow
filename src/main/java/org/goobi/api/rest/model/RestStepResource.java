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
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.beans.Usergroup;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Getter;
import lombok.Setter;

@XmlRootElement(name = "step")
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class RestStepResource {
    private Integer stepId;
    private String steptitle;

    private Integer processId;
    private String processName;

    private String status;

    private Integer priority;
    private String order;

    private Date startDate;
    private Date finishDate;

    private Map<String, Boolean> properties = new HashMap<>();

    private Map<String, String> scripts = new TreeMap<>();
    private Map<String, String> httpStepConfiguration = new HashMap<>();

    private String plugin;
    private String validationPlugin;

    private String queueType;

    private List<String> usergroups = new ArrayList<>();

    public RestStepResource() {
    }

    public RestStepResource(Process process, Step step) {
        stepId = step.getId();
        steptitle = step.getTitel();
        processId = process.getId();
        processName = process.getTitel();
        status = step.getBearbeitungsstatusEnum().getSearchString();
        priority = step.getPrioritaet();
        order = String.valueOf(step.getReihenfolge());
        startDate = step.getBearbeitungsbeginn();
        finishDate = step.getBearbeitungsende();

        properties.put("metadata", step.isTypMetadaten());
        properties.put("automatic", step.isTypAutomatisch());
        properties.put("thumbnailGeneration", step.isTypAutomaticThumbnail());
        properties.put("readimages", step.isTypImagesLesen());
        properties.put("writeimages", step.isTypImagesSchreiben());
        properties.put("exportdms", step.isTypExportDMS());
        properties.put("script", step.isTypScriptStep());
        properties.put("validate", step.isTypBeimAbschliessenVerifizieren());
        properties.put("batch", step.isBatchStep());

        properties.put("delay", step.isDelayStep());
        properties.put("updatemetadataindex", step.isUpdateMetadataIndex());
        properties.put("generatedocket", step.isGenerateDocket());

        if (StringUtils.isNotBlank(step.getScriptname1()) && StringUtils.isNotBlank(step.getTypAutomatischScriptpfad())) {
            scripts.put(step.getScriptname1(), step.getTypAutomatischScriptpfad());
        }

        if (StringUtils.isNotBlank(step.getScriptname2()) && StringUtils.isNotBlank(step.getTypAutomatischScriptpfad2())) {
            scripts.put(step.getScriptname2(), step.getTypAutomatischScriptpfad2());
        }

        if (StringUtils.isNotBlank(step.getScriptname3()) && StringUtils.isNotBlank(step.getTypAutomatischScriptpfad3())) {
            scripts.put(step.getScriptname3(), step.getTypAutomatischScriptpfad3());
        }

        if (StringUtils.isNotBlank(step.getScriptname4()) && StringUtils.isNotBlank(step.getTypAutomatischScriptpfad4())) {
            scripts.put(step.getScriptname4(), step.getTypAutomatischScriptpfad4());
        }

        if (StringUtils.isNotBlank(step.getScriptname5()) && StringUtils.isNotBlank(step.getTypAutomatischScriptpfad5())) {
            scripts.put(step.getScriptname5(), step.getTypAutomatischScriptpfad5());
        }

        if (step.isHttpStep()) {
            httpStepConfiguration.put("url", step.getHttpUrl());
            httpStepConfiguration.put("method", step.getHttpMethod());
            httpStepConfiguration.put("body", step.getHttpJsonBody());
            httpStepConfiguration.put("closeStep", String.valueOf(step.isHttpCloseStep()));
            httpStepConfiguration.put("escapeBody", String.valueOf(step.isHttpEscapeBodyJson()));
        }

        plugin = step.getStepPlugin();
        validationPlugin = step.getValidationPlugin();

        queueType = step.getMessageQueue() != null ? step.getMessageQueue().getName() : null;

        for (Usergroup grp : step.getBenutzergruppen()) {
            usergroups.add(grp.getTitel());
        }

    }

}
