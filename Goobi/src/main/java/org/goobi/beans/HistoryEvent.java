package org.goobi.beans;

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
import java.io.Serializable;
import java.util.Date;

import de.sub.goobi.helper.enums.HistoryEventType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * HistoryItem for any kind of history event of a {@link Prozess}
 * 
 * @author Steffen Hankiewicz
 * @version 24.05.2009
 */
@Getter
@Setter
public class HistoryEvent implements Serializable {
    private static final long serialVersionUID = 991946177515032238L;
    private Integer id;
    private Date date;
    private Double numericValue;
    private String stringValue;
    /**
     * Getter and Setter for type as private methods for Hibernate only
     */
    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private Integer type;
    private Process process;

    /**
     * This constructor is only public for hibernate usage. If you want to create a new HistoryEvent please use HistoryEvent(Date date, Number
     * inNumericValue, String inStringValue, HistoryEventType inHistoryEventType, Prozess process)
     * 
     * 
     */
    public HistoryEvent() {

    }

    /**
     * Please use only this constructor.
     * 
     * @param date Date of HistoryEvent
     * @param inNumericValue value as Number (pages, size,...)
     * @param inStringValue value as string
     * @param inHistoryEventType type of HistoryEvent ( {@link HistoryEventType} )
     * @param process process of HistoryEvent
     */

    public HistoryEvent(Date date, Number inNumericValue, String inStringValue, HistoryEventType inHistoryEventType, Process process) {
        super();
        this.date = date;
        numericValue = inNumericValue.doubleValue();
        stringValue = inStringValue;
        type = inHistoryEventType.getValue();
        this.process = process;
    }

    /**
     * Getter for type
     * 
     * @return type as HistoryEventType
     */
    public HistoryEventType getHistoryType() {
        return HistoryEventType.getTypeFromValue(type);
    }

    /**
     * Setter for type
     * 
     * @param type as HistoryEventType
     */
    public void setHistoryType(HistoryEventType type) {
        this.type = type.getValue();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((date == null) ? 0 : date.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((numericValue == null) ? 0 : numericValue.hashCode());
        result = prime * result + ((process == null) ? 0 : process.hashCode());
        result = prime * result + ((stringValue == null) ? 0 : stringValue.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        HistoryEvent other = (HistoryEvent) obj;
        if (date == null) {
            if (other.date != null) {
                return false;
            }
        } else if (!date.equals(other.date)) {
            return false;
        }
        if (numericValue == null) {
            if (other.numericValue != null) {
                return false;
            }
        } else if (!numericValue.equals(other.numericValue)) {
            return false;
        }
        if (stringValue == null) {
            if (other.stringValue != null) {
                return false;
            }
        } else if (!stringValue.equals(other.stringValue)) {
            return false;
        }
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        return true;
    }

}
