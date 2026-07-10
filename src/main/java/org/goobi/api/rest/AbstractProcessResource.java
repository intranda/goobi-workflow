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

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.goobi.beans.GoobiProperty;
import org.goobi.beans.GoobiProperty.PropertyOwnerType;
import org.goobi.beans.Process;
import org.goobi.production.enums.LogType;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.enums.PropertyType;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.ProjectManager;
import de.sub.goobi.persistence.managers.PropertyManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import ugh.dl.Corporate;
import ugh.dl.DocStruct;
import ugh.dl.Metadata;
import ugh.dl.MetadataType;
import ugh.dl.Person;
import ugh.exceptions.MetadataTypeNotAllowedException;

@Log4j2
public abstract class AbstractProcessResource {

    @Context
    @Setter
    @Getter
    private HttpServletRequest request;

    protected Response checkProcessAccess(Process process) {
        AuthenticationToken token = getRequest() != null ? (AuthenticationToken) getRequest().getAttribute("authToken") : null;
        if (token == null) {
            return null;
        }
        try {
            if (!ProjectManager.isUserMemberOfProject(token.getUserId(), process.getProjekt().getId())) {
                return Response.status(403).entity("Access denied").build();
            }
        } catch (DAOException e) {
            log.error(e);
            return Response.status(500).entity("Internal error").build();
        }
        return null;
    }

    protected GoobiProperty saveNewProcessproperty(Process process, String key, String value, Date creationDate, String container) {
        GoobiProperty property = new GoobiProperty(PropertyOwnerType.PROCESS);
        property.setPropertyName(key);
        property.setPropertyValue(value);
        property.setOwner(process);
        property.setType(PropertyType.STRING);
        property.setContainer(container);
        if (creationDate != null) {
            property.setCreationDate(creationDate);
        } else {
            property.setCreationDate(new Date());
        }
        Helper.addMessageToProcessJournal(property.getObjectId(), LogType.DEBUG, "Property added using REST-API: " + property.getPropertyName());

        PropertyManager.saveProperty(property);

        return property;
    }

    protected void addNewMetadataToDocStruct(DocStruct dst, MetadataType mdType, String metadataValue, String authorityValue, String inFirstName,
            String inLastName) throws MetadataTypeNotAllowedException {
        String firstName = inFirstName;
        String lastName = inLastName;
        if (mdType.getIsPerson()) {
            Person p = new Person(mdType);
            // if firstName and lastName are both blank, create them using metadataValue
            if (StringUtils.isBlank(firstName) && StringUtils.isBlank(lastName) && metadataValue != null) {
                String[] nameParts = metadataValue.split(" ", 2);
                firstName = nameParts[0];
                lastName = nameParts.length > 1 ? nameParts[1] : "";
            }
            p.setFirstname(firstName);
            p.setLastname(lastName);
            p.setAuthorityValue(authorityValue);
            dst.addPerson(p);
        } else if (mdType.isCorporate()) {
            Corporate c = new Corporate(mdType);
            c.setMainName(metadataValue);
            c.setAuthorityValue(authorityValue);
            dst.addCorporate(c);
        } else {
            Metadata md = new Metadata(mdType);
            md.setValue(metadataValue);
            md.setAuthorityValue(authorityValue);
            dst.addMetadata(md);
        }
    }
}
