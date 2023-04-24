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

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;
import ugh.dl.Corporate;
import ugh.dl.Metadata;
import ugh.dl.Person;

@XmlRootElement(name = "property")
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class RestMetadataResource {

    private String name;
    private String value;
    private String metadataType;
    private String authorityValue;
    private String firstname;
    private String lastname;

    public RestMetadataResource() {
    }

    public RestMetadataResource(Metadata metadata) {

        name = metadata.getType().getName();

        if (metadata.getType().getIsPerson()) {
            Person p = (Person) metadata;
            firstname = p.getFirstname();
            lastname = p.getLastname();
        } else if (metadata.getType().isCorporate()) {
            Corporate c = (Corporate) metadata;
            value = c.getMainName();
        } else {
            value = metadata.getValue();
        }
    }
}
