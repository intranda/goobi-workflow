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

package org.goobi.production.properties;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public interface MultiSelectProperty<T> {

    /**
     * @return a list of all possible values.
     */
    List<T> getSelectValues();

    /**
     * A list with the string representation of the selected values.
     * 
     * @return selected values
     */
    List<String> getAllSelectedValues();

    /**
     * Remove a selected value from the selection list.
     *
     * @param value
     */

    default void removeSelectedValue(String value) {
        getAllSelectedValues().remove(value);
    }

    /**
     * 
     * Get a list with selectaeble values. It is a sublist of all elements, reduced by the already selected values.
     * 
     * @return possible values
     */
    default List<T> getPossibleValues() {
        List<T> answer = new ArrayList<>();
        for (T possibleValue : getSelectValues()) {
            if (!getAllSelectedValues().contains(possibleValue.toString())) {
                answer.add(possibleValue);
            }
        }
        return answer;
    }

    /**
     * Get current value, always empty.
     *
     * @return empty string
     */
    default String getCurrentValue() {
        return "";
    }

    /**
     * 
     * Select current value. Add it to the selected value list, remove it from the possible value list
     *
     * @param value
     */

    default void setCurrentValue(String value) {
        if (StringUtils.isNotBlank(value)) {
            getAllSelectedValues().add(value);
        }
    }
}
