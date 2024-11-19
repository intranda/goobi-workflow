package org.goobi.production.properties;

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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;
import lombok.Setter;

import javax.faces.model.SelectItem;

public class ImportProperty implements IProperty {

    @Getter
    @Setter
    private String name = "";
    private String container = "0";
    @Getter
    @Setter
    private String validation = "";
    @Getter
    @Setter
    private Type type = Type.TEXT;
    @Getter
    @Setter
    private String value = "";
    @Getter
    @Setter
    private List<SelectItem> possibleValues = Collections.emptyList();
    @Getter
    @Setter
    private List<String> projects = new ArrayList<>();
    @Getter
    @Setter
    private boolean required = false;
    @Getter
    @Setter
    private String pattern = "dd.MM.yyyy";

    public ImportProperty() {
        this.possibleValues = new ArrayList<>();
        this.projects = new ArrayList<>();
    }

    @Override
    public String getContainer() {
        return this.container;
    }

    @Override
    public void setContainer(String container) {
        this.container = container;
    }

    @Override
    public ArrayList<ShowStepCondition> getShowStepConditions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setShowStepConditions(List<ShowStepCondition> showStepConditions) {
    }

    @Override
    public AccessCondition getShowProcessGroupAccessCondition() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setShowProcessGroupAccessCondition(AccessCondition showProcessGroupAccessCondition) {
    }

    @Override
    public boolean isValid() {
        Pattern pattern = Pattern.compile(this.validation);
        Matcher matcher = pattern.matcher(this.value);
        return matcher.matches();
    }

    @Override
    public ImportProperty getClone(String containerNumber) {
        return new ImportProperty();
    }

    @Override
    public void transfer() {

    }

    public List<String> getValueList() {
        return Arrays.asList(this.value.split("; "));
    }

    public void setValueList(List<String> valueList) {
        StringBuilder bld = new StringBuilder();
        for (String val : valueList) {
            bld.append(val).append("; ");
        }
        this.value = bld.toString();
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
    }

    @Override
    public void setDateValue(Date inDate) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        value = format.format(inDate);
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
}
