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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.sub.goobi.beans.property.IGoobiProperty;
import de.sub.goobi.helper.enums.PropertyType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public abstract class AbstractProperty implements IGoobiProperty {
    @Setter
    @Getter
    public Integer id;
    @Setter
    @Getter
    public String titel;
    @Setter
    @Getter
    public String wert;
    @Setter
    @Getter
    public Boolean istObligatorisch;
    @Setter(AccessLevel.PRIVATE)
    @Getter(AccessLevel.PRIVATE)
    public Integer datentyp;
    @Setter
    @Getter
    public String auswahl;
    @Setter
    @Getter
    public Date creationDate;

    private String container;

    @Setter
    public List<String> valueList;

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
            order = "0";
        }
        container = order;
    }

    @Override
    public String getNormalizedTitle() {
        return titel.replace(" ", "_").trim();
    }

    @Override
    public String getNormalizedValue() {
        return wert.replace(" ", "_").trim();
    }

    @Override
    public Boolean isIstObligatorisch() {
        if (istObligatorisch == null) {
            istObligatorisch = false;
        }
        return istObligatorisch;
    }

    /**
     * set datentyp to specific value from {@link PropertyType}
     * 
     * @param inType as {@link PropertyType}
     */
    @Override
    public void setType(PropertyType inType) {
        datentyp = inType.getId();
    }

    /**
     * get datentyp as {@link PropertyType}
     * 
     * @return current datentyp
     */
    @Override
    public PropertyType getType() {
        if (datentyp == null) {
            datentyp = PropertyType.STRING.getId();
        }
        return PropertyType.getById(datentyp);
    }

    public List<String> getValueList() {
        if (valueList == null) {
            valueList = new ArrayList<>();
        }
        return valueList;
    }
}