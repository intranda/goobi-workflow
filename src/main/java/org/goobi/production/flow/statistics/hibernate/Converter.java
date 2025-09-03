package org.goobi.production.flow.statistics.hibernate;

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
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.goobi.production.flow.statistics.StatisticsManager;

import lombok.extern.log4j.Log4j2;

/**
 * class helps to convert results returned from Projections or Queries, where data types don't match the target data type.
 */
@Log4j2
public class Converter {

    private Object myObject = null;
    private SimpleDateFormat sdf;

    /**
     * constructor retrieves current locale and uses it for formatting data
     */
    private Converter() {
        try {
            this.sdf = new SimpleDateFormat("yyyy.MM.dd", new DateFormatSymbols(StatisticsManager.getLocale()));
        } catch (NullPointerException e) {
            log.error("Class statistics.hibernate.Converter Error, can't get FacesContext");
        }
    }

    /**
     * constructor (parameterless constructor is set to private).
     * 
     * @param obj Object which will get converted
     */
    public Converter(Object obj) {
        this();
        if (obj == null) {
            throw new NullPointerException();
        }
        this.myObject = obj;
    }

    /**
     * 
     * @return Integer if possible
     */
    protected Integer getInteger() {
        if (this.myObject instanceof Integer) {
            return (Integer) this.myObject;
        } else if (this.myObject instanceof Double) {
            return ((Double) this.myObject).intValue();
        } else if (this.myObject instanceof String) {
            return Integer.parseInt((String) this.myObject);
        } else if (this.myObject instanceof Long) {
            return ((Long) this.myObject).intValue();
        } else {
            throw new NumberFormatException();
        }
    }

    /**
     * 
     * @return Double if possible
     */
    protected Double getDouble() {
        if (this.myObject instanceof Integer) {
            return Double.valueOf((Integer) this.myObject);
        } else if (this.myObject instanceof Double) {

            return (Double) this.myObject;
        } else if (this.myObject instanceof String) {

            return Double.parseDouble((String) this.myObject);
        } else if (this.myObject instanceof Long) {
            return ((Long) this.myObject).doubleValue();
        } else {
            throw new NumberFormatException();
        }
    }

    /**
     * 
     * @return String, fall back is toString() method
     */
    public String getString() {
        if (this.myObject instanceof Date) {
            return this.sdf.format(this.myObject);
        } else {
            return this.myObject.toString();

        }
    }

    /**
     * 
     * @return Double value of GB, calculated on the basis of Bytes
     */
    public Double getGB() {
        return getDouble() / (1024 * 1024 * 1024);

    }

}
