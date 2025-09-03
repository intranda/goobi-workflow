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
 * 
 */

package org.goobi.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.sub.goobi.beans.property.IGoobiProperty;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.enums.PropertyType;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.ProjectManager;
import de.sub.goobi.persistence.managers.StepManager;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Getter
@Setter
@RequiredArgsConstructor
@Log4j2
public class GoobiProperty implements IGoobiProperty, Serializable {

    private static final long serialVersionUID = -947157110530797855L;

    // values stored in the database:

    // auto increment
    protected Integer id;
    // process id, template id, project id, ...
    protected Integer objectId;

    protected String propertyName;

    protected String propertyValue;

    protected boolean required;

    protected Date creationDate = new Date();

    protected String container;

    protected Integer dataType;

    @NonNull
    private PropertyOwnerType propertyType;

    private IPropertyHolder ownerObject;

    @AllArgsConstructor
    public enum PropertyOwnerType {

        PROCESS("process"),
        USER("user"),
        ERROR("error"),
        PROJECT("project");

        @Getter
        private String title;

        public static PropertyOwnerType getByTitle(String title) {
            for (PropertyOwnerType type : values()) {
                if (type.getTitle().equals(title)) {
                    return type;
                }
            }
            return PropertyOwnerType.PROCESS;
        }
    }

    // properties and methods to display data in UI:

    private List<String> valueList;

    @Override
    public String getContainer() {
        if (container == null) {
            return "0";
        }
        return container;
    }

    @Override
    public void setContainer(String order) {
        if (order == null) {
            container = "0";
        } else {
            container = order;
        }
    }

    @Override
    public String getNormalizedTitle() {
        return propertyName.replace(" ", "_").trim();
    }

    @Override
    public String getNormalizedValue() {
        return propertyValue.replace(" ", "_").trim();
    }

    @Override
    public String getNormalizedDate() {
        return Helper.getDateAsFormattedString(creationDate);
    }

    public List<String> getValueList() {
        if (valueList == null) {
            valueList = new ArrayList<>();
        }
        return valueList;
    }

    /**
     * set datentyp to specific value from {@link PropertyType}.
     * 
     * @param inType as {@link PropertyType}
     */
    @Override
    public void setType(PropertyType inType) {
        dataType = inType.getId();
    }

    /**
     * get datentyp as {@link PropertyType}.
     * 
     * @return current datentyp
     */
    @Override
    public PropertyType getType() {
        if (dataType == null) {
            dataType = PropertyType.STRING.getId();
        }
        return PropertyType.getById(dataType);
    }

    public IPropertyHolder getOwner() {
        if (ownerObject == null && objectId != null) {
            switch (propertyType) {
                case ERROR:
                    ownerObject = StepManager.getStepById(objectId);
                    break;

                case PROCESS:
                    ownerObject = ProcessManager.getProcessById(objectId);
                    break;
                case PROJECT:
                    try {
                        ownerObject = ProjectManager.getProjectById(objectId);
                    } catch (DAOException e) {
                        log.error(e);
                    }
                    break;

                default:
                    break;
            }

        }
        return ownerObject;

    }

    public void setOwner(IPropertyHolder owner) {
        ownerObject = owner;
        objectId = owner.getId();
    }

    // legacy methods, kept for plugin compatibility

    @Override
    @Deprecated
    public void setTitel(String name) {
        setPropertyName(name);
    }

    @Override
    @Deprecated
    public String getTitel() {
        return getPropertyName();
    }

    @Override
    @Deprecated
    public void setWert(String value) {
        setPropertyValue(value);
    }

    @Override
    @Deprecated
    public String getWert() {
        return getPropertyValue();
    }

    public Integer getObjectId() {
        if (objectId == null && ownerObject != null) {
            objectId = ownerObject.getId();
        }
        return objectId;
    }
}
