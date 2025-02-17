package org.goobi.production.converters;

import org.goobi.production.flow.statistics.enums.TimeUnit;

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
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;

/**
 * TimeUnitConverter for statistics TimeUnits as select-items in jsf-guis
 * 
 * @author Steffen Hankiewicz
 * @version 21.05.2009
 **************************************************************************************/
public class StatisticsTimeUnitConverter implements Converter<TimeUnit> {
    public static final String CONVERTER_ID = "StatisticsTimeUnitConverter";

    /**
     * convert String to TimeUnit
     **************************************************************************************/
    @Override
    public TimeUnit getAsObject(FacesContext context, UIComponent component, String value) throws ConverterException {
        if (value == null) {
            return TimeUnit.days;
        } else {
            return TimeUnit.getById(value);
        }
    }

    /**
     * convert TimeUnit to String
     **************************************************************************************/
    @Override
    public String getAsString(FacesContext context, UIComponent component, TimeUnit value) throws ConverterException {
        if (!(value instanceof TimeUnit)) {
            return TimeUnit.days.getId();
        } else {
            return value.getId();
        }
    }

}
