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

package de.sub.goobi.persistence.managers;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.goobi.beans.GoobiProperty;
import org.goobi.beans.GoobiProperty.PropertyOwnerType;
import org.goobi.beans.Processproperty;

import lombok.extern.log4j.Log4j2;

@Log4j2
public final class PropertyManager implements Serializable {

    private PropertyManager() {
        // hide implicit public constructor
    }

    private static final long serialVersionUID = 900347502238407686L;

    @Deprecated
    public static List<Processproperty> getProcessPropertiesForProcess(int processId) {
        List<Processproperty> propertyList = new ArrayList<>();
        try {
            propertyList = PropertyMysqlHelper.getProcessPropertiesForProcess(processId);
        } catch (SQLException e) {
            log.error(e);
        }
        return propertyList;
    }

    @Deprecated
    public static Processproperty getProcessPropertyById(int propertyId) {
        try {
            return PropertyMysqlHelper.getProcessPropertyById(propertyId);
        } catch (SQLException e) {
            log.error(e);
        }
        return null;
    }

    @Deprecated
    public static void saveProcessProperty(Processproperty pe) {
        try {
            PropertyMysqlHelper.saveProcessproperty(pe);
        } catch (SQLException e) {
            log.error(e);
        }
    }

    @Deprecated

    public static void deleteProcessProperty(Processproperty pe) {
        try {
            PropertyMysqlHelper.deleteProcessProperty(pe);
        } catch (SQLException e) {
            log.error(e);
        }
    }

    public static List<String> getDistinctProcessPropertyTitles() {
        List<String> titleList = new ArrayList<>();
        try {
            titleList = PropertyMysqlHelper.getDistinctPropertyTitles();
        } catch (SQLException e) {
            log.error(e);
        }
        return titleList;
    }

    public static List<GoobiProperty> getPropertiesForObject(int objectId, PropertyOwnerType propertyType) {
        List<GoobiProperty> propertyList = new ArrayList<>();
        try {
            propertyList = PropertyMysqlHelper.getPropertiesForObject(objectId, propertyType);
            if (propertyList == null) {
                propertyList = new ArrayList<>();
            }
        } catch (SQLException e) {
            log.error(e);
        }
        return propertyList;
    }

    public static GoobiProperty getPropertById(int propertyId) {
        try {
            return PropertyMysqlHelper.getPropertyById(propertyId);
        } catch (SQLException e) {
            log.error(e);
        }
        return null;
    }

    public static void saveProperty(GoobiProperty pe) {
        try {
            PropertyMysqlHelper.saveProperty(pe);
        } catch (SQLException e) {
            log.error(e);
        }
    }

    public static void deleteProperty(GoobiProperty pe) {
        try {
            PropertyMysqlHelper.deleteProperty(pe);
        } catch (SQLException e) {
            log.error(e);
        }
    }

    public static List<String> getPropertyTitles(PropertyOwnerType propertyType) {
        List<String> titleList = new ArrayList<>();
        try {
            titleList = PropertyMysqlHelper.getPropertyTitles(propertyType);
        } catch (SQLException e) {
            log.error(e);
        }
        return titleList;
    }
}
