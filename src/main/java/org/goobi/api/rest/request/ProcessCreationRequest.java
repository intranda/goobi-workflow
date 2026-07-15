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
package org.goobi.api.rest.request;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.Data;

@Data
@XmlRootElement(name = "record")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProcessCreationRequest {

    private String identifier;
    private String processtitle;
    private String anchorDSType;
    private String logicalDSType;
    private Integer templateId;
    private String templateName;

    private OpacConfig opacConfig;

    @XmlElementWrapper
    @XmlElement(name = "metadata")
    private Map<String, String> metadata;

    @XmlElementWrapper
    @XmlElement(name = "anchorMetadata")
    private Map<String, String> anchorMetadata;

    @XmlElementWrapper
    @XmlElement(name = "property")
    @JsonProperty("properties")
    private Map<String, String> properties;

}
