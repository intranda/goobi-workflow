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
package org.goobi.api.rest.response;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.goobi.beans.Processproperty;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;

@Data
@XmlRootElement
@JsonPropertyOrder({ "result", "title", "id", "creationDate", "processCompleted", "project", "ruleset", "step" })
public class ProcessStatusResponse {

    private String result; // success, error

    private String title;

    private int id;

    private boolean processCompleted;

    private String project;

    private String ruleset;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ", timezone = "CET")
    private Date creationDate;

    private List<StepResponse> step = new ArrayList<>();

    private List<PropertyResponse> properties = new ArrayList<>();

    @SuppressWarnings("deprecation")
    public void addProperties(List<Processproperty> propertyList) {
        for (Processproperty property : propertyList) {
            PropertyResponse resp = new PropertyResponse();
            resp.setTitle(property.getTitel());
            resp.setValue(property.getWert());
            properties.add(resp);
        }
    }

}
