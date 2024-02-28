package org.goobi.production.search.api;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
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
import org.goobi.production.flow.statistics.hibernate.FilterString;

import de.sub.goobi.helper.Helper;
import lombok.Data;

@Data
public class ExtendedSearchRow implements Serializable {

    private static final long serialVersionUID = 4020492017029891060L;

    private String fieldName;

    private String fieldOperand;

    // process title, id, batch
    private String fieldValue;

    // step
    private String stepStatus;

    private String stepName;

    // project
    private String projectName;

    // properties
    private String processPropertyName;

    private String processPropertyValue;

    private String templatePropertyName;

    private String templatePropertyValue;

    private String masterpiecePropertyName;

    private String masterpiecePropertyValue;

    private String metadataName;

    private String metadataValue;

    private String institutionName;

    public String createSearchString() {
        String value = "";
        if (fieldName.equals("PROCESSTITLE") && !fieldValue.isEmpty()) {
            value = "\"" + this.fieldOperand + this.fieldValue + "\" ";
        } else if (fieldName.equals("PROCESSID") && !fieldValue.isEmpty()) {
            value = "\"" + this.fieldOperand + FilterString.ID + this.fieldValue + "\" ";
        }

        else if (fieldName.equals("BATCH") && !fieldValue.isEmpty()) {
            value = "\"" + this.fieldOperand + FilterString.BATCH + this.fieldValue + "\" ";
        }

        else if (fieldName.equals("PROJECT") && !this.projectName.equals(Helper.getTranslation("notSelected"))) {
            value = "\"" + this.fieldOperand + FilterString.PROJECT + this.projectName + "\" ";

        } else if (fieldName.equals("METADATA") && !this.metadataName.equals(Helper.getTranslation("notSelected")) && !metadataValue.isEmpty()) {
            value = "\"" + this.fieldOperand + FilterString.METADATA + metadataName + ":" + metadataValue + "\" ";

        }

        else if (fieldName.equals("PROCESSPROPERTY") && !processPropertyName.equals(Helper.getTranslation("notSelected"))
                && !processPropertyValue.isEmpty()) {
            value = "\"" + this.fieldOperand + FilterString.PROCESSPROPERTY + this.processPropertyName + ":" + processPropertyValue + "\" ";
        }

        else if (fieldName.equals("WORKPIECE") && !masterpiecePropertyName.equals(Helper.getTranslation("notSelected"))
                && !masterpiecePropertyValue.isEmpty()) {
            value = "\"" + this.fieldOperand + FilterString.WORKPIECE + this.masterpiecePropertyName + ":" + masterpiecePropertyValue + "\" ";
        }

        else if (fieldName.equals("TEMPLATE") && !templatePropertyName.equals(Helper.getTranslation("notSelected"))
                && !templatePropertyValue.isEmpty()) {
            value = "\"" + this.fieldOperand + FilterString.TEMPLATE + this.templatePropertyName + ":" + templatePropertyValue + "\" ";
        }

        else if (fieldName.equals("STEP") && !stepStatus.equals(Helper.getTranslation("notSelected")) && !stepName.isEmpty()
                && !stepName.equals(Helper.getTranslation("notSelected"))) {
            value = "\"" + this.fieldOperand + this.stepStatus + ":" + this.stepName + "\" ";
        }

        else if (fieldName.equals("JOURNAL") && !fieldValue.isEmpty()) {
            value = "\"" + this.fieldOperand + FilterString.JOURNAL + fieldValue + "\" ";
        }

        else if (fieldName.equals("INSTITUTION") && StringUtils.isNotBlank(institutionName)) {
            value = "\"" + this.fieldOperand + FilterString.INSTITUTION + ":" + institutionName + "\" ";
        } else if (fieldName.equals("PROCESSDATE") && !fieldValue.isEmpty()) {
            value = "\"" + FilterString.PROCESS_DATE + this.fieldOperand + fieldValue + "\" ";
        } else if (fieldName.equals("STEPSTARTDATE") && !fieldValue.isEmpty()) {
            value = "\"" + FilterString.STEP_START_DATE + this.fieldOperand + fieldValue + "\" ";
        } else if (fieldName.equals("STEPFINISHDATE") && !fieldValue.isEmpty()) {
            value = "\"" + FilterString.STEP_FINISH_DATE + this.fieldOperand + fieldValue + "\" ";
        }

        return value;
    }

}
