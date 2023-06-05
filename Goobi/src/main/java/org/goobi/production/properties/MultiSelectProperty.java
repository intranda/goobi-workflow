package org.goobi.production.properties;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public interface MultiSelectProperty<T> {

    /**
     * Contains a list of all possible values
     */
    public List<T> getSelectValues();

    /**
     * A list with the string representation of the selected values
     * 
     * @return
     */
    public List<String> getAllSelectedValues();

    /**
     * Remove a selected value from the selection list
     *
     * @param value
     */

    default void removeSelectedValue(String value) {
        getAllSelectedValues().remove(value);
    }

    /**
     * 
     * Get a list with selectaeble values. It is a sublist of all elements, reduced by the already selected values
     * 
     * @return
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
     * Get current value, always empty
     *
     * @return
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
