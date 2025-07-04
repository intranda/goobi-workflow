package org.goobi.production.properties;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.goobi.beans.GoobiProperty;
import org.goobi.beans.Step;
import org.goobi.managedbeans.FormInputMultiSelectBean;
import org.goobi.managedbeans.FormInputMultiSelectHelper;
import org.goobi.production.cli.helper.StringPair;

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

import io.goobi.workflow.api.vocabulary.VocabularyAPIManager;
import io.goobi.workflow.api.vocabulary.helper.ExtendedVocabularyRecord;
import jakarta.faces.model.SelectItem;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class DisplayProperty implements IProperty, Serializable {

    private static final long serialVersionUID = 6413183995622426678L;
    @Getter
    @Setter
    private String name;
    @Getter
    @Setter
    private String container;
    @Getter
    @Setter
    private String validation;
    @Getter
    @Setter
    private Type type;
    @Getter
    private String value;
    @Getter
    @Setter
    private String readValue;
    @Getter
    @Setter
    private List<SelectItem> possibleValues;
    @Getter
    @Setter
    private List<String> projects;
    @Getter
    @Setter
    private List<String> workflows;
    @Getter
    @Setter
    private List<ShowStepCondition> showStepConditions;
    @Getter
    @Setter
    private AccessCondition showProcessGroupAccessCondition;

    @Getter
    @Setter
    private List<StringPair> processCreationConditions = new ArrayList<>();

    @Getter
    @Setter
    private GoobiProperty prozesseigenschaft;
    @Getter
    @Setter
    private AccessCondition currentStepAccessCondition;
    @Getter
    @Setter
    private boolean duplicationAllowed = false;
    @Getter
    @Setter
    private String pattern = "dd.MM.yyyy";

    @Getter
    private FormInputMultiSelectBean normalSelectionBean;
    @Getter
    private FormInputMultiSelectBean vocabularySelectionBean;

    public DisplayProperty() {
        this.possibleValues = new ArrayList<>();
        this.projects = new ArrayList<>();
        this.workflows = new ArrayList<>();
        this.showStepConditions = new ArrayList<>();
        this.normalSelectionBean = new FormInputMultiSelectHelper(() -> this.possibleValues, this::getSelectedValues, this::setSelectedValues);
        this.vocabularySelectionBean =
                new FormInputMultiSelectHelper(() -> this.possibleValues, this::getSelectedVocabularyRecords, this::setSelectedValues);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.goobi.production.properties.IProperty#setValue(java.lang.String)
     */
    @Override
    public void setValue(String value) {
        this.value = value;
        this.readValue = value;
        if (Type.VOCABULARYREFERENCE.equals(this.type)) {
            this.readValue = readVocabularyMainValueForRecord(this.value);
        }
    }

    @Override
    public void setDateValue(Date inDate) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        value = format.format(inDate);
        this.readValue = value;
    }

    @Override
    public Date getDateValue() {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(format.parse(value));
            cal.set(Calendar.HOUR, 12);
            return cal.getTime();
        } catch (ParseException | NullPointerException e) {
            return new Date();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.goobi.production.properties.IProperty#isValid()
     */
    @Override
    public boolean isValid() {
        if (this.validation != null && this.validation.length() > 0) {
            Pattern pattern = Pattern.compile(this.validation);
            Matcher matcher = pattern.matcher(this.value);
            return matcher.matches();
        } else {
            return true;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.goobi.production.properties.IProperty#save(de.sub.goobi.Beans.Schritt)
     */

    public void save(Step step) {
        // TODO: Is this method required?
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.goobi.production.properties.IProperty#getClone()
     */
    @Override
    public DisplayProperty getClone(String containerName) {
        DisplayProperty p = new DisplayProperty();
        p.setContainer(containerName);
        p.setName(this.name);
        p.setValidation(this.validation);
        p.setType(this.type);
        p.setPattern(this.pattern);
        p.setValue(this.value);
        p.setShowProcessGroupAccessCondition(this.showProcessGroupAccessCondition);
        p.setDuplicationAllowed(this.isDuplicationAllowed());
        p.setShowStepConditions(new ArrayList<>(getShowStepConditions()));
        p.setPossibleValues(new ArrayList<>(getPossibleValues()));
        p.setProjects(new ArrayList<>(getProjects()));
        p.setCurrentStepAccessCondition(currentStepAccessCondition);
        return p;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.goobi.production.properties.IProperty#transfer()
     */
    @Override
    public void transfer() {
        this.prozesseigenschaft.setPropertyValue(value);
        this.prozesseigenschaft.setPropertyName(name);
        this.prozesseigenschaft.setContainer(this.container);
    }

    public List<String> getValueList() {
        if (this.value != null && this.value.contains("; ")) {
            return Arrays.asList(this.value.split("; "));
        } else {
            return Collections.emptyList();
        }
    }

    public void setValueList(List<String> valueList) {
        StringBuilder bld = new StringBuilder();
        for (String val : valueList) {
            bld.append(val).append("; ");
        }
        this.value = bld.toString();
        this.readValue = value;
    }

    private List<SelectItem> getSelectedValues() {
        return new LinkedList<>(
                getValueList().stream()
                        .map(value -> new SelectItem(value, value))
                        .toList());
    }

    public List<String> getMultiVocabularyReferenceList() {
        return getValueList().stream()
                .map(this::readVocabularyMainValueForRecord)
                .toList();
    }

    private String readVocabularyMainValueForRecord(String ref) {
        if (StringUtils.isBlank(ref)) {
            return "";
        }
        try {
            ExtendedVocabularyRecord rec = VocabularyAPIManager.getInstance().vocabularyRecords().get(Long.parseLong(ref));
            return rec.getMainValue();
        } catch (NumberFormatException e) {
            log.error("Wrong ID format \"{}\"", ref);
            return "Broken vocabulary reference";
        } catch (Exception e) {
            log.warn("Unable to retrieve vocabulary record reference \"{}\"", ref);
            return "Broken vocabulary reference";
        }
    }

    private List<SelectItem> getSelectedVocabularyRecords() {
        return new LinkedList<>(
                getValueList().stream()
                        .map(ref -> new SelectItem(ref, readVocabularyMainValueForRecord(ref)))
                        .toList());
    }

    private void setSelectedValues(List<SelectItem> selectItems) {
        this.setValueList(selectItems.stream()
                .map(SelectItem::getValue)
                .map(Object::toString)
                .toList());
    }

    public boolean getBooleanValue() {
        return "true".equalsIgnoreCase(this.value);
    }

    public void setBooleanValue(boolean val) {
        if (val) {
            this.value = "true";
        } else {
            this.value = "false";
        }
        this.readValue = value;
    }

    public static class CompareProperties implements Comparator<DisplayProperty>, Serializable {
        private static final long serialVersionUID = 8047374873015931547L;

        @Override
        public int compare(DisplayProperty o1, DisplayProperty o2) {
            return o1.getContainer().compareTo(o2.getContainer());
        }

    }

    public boolean getIsNew() {
        return this.name == null || this.name.length() == 0;
    }

}
