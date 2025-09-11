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
        StringBuilder value = new StringBuilder();

        if ("PROCESSTITLE".equals(fieldName) && !fieldValue.isEmpty()) {
            value.append("\"");
            if (StringUtils.isNotBlank(fieldOperand)) {
                value.append(fieldOperand);
            }
            value.append(fieldValue);
            value.append("\"");
        } else if ("PROCESSID".equals(fieldName) && !fieldValue.isEmpty()) {
            value.append("\"");
            if (StringUtils.isNotBlank(fieldOperand)) {
                value.append(fieldOperand);
            }
            value.append(FilterString.ID);
            value.append(fieldValue);
            value.append("\"");
        } else if ("BATCH".equals(fieldName) && !fieldValue.isEmpty()) {
            value.append("\"");
            if (StringUtils.isNotBlank(fieldOperand)) {
                value.append(fieldOperand);
            }
            value.append(FilterString.BATCH);
            value.append(fieldValue);
            value.append("\"");
        } else if ("PROJECT".equals(fieldName) && !this.projectName.equals(Helper.getTranslation("notSelected"))) {
            value.append("\"");
            if (StringUtils.isNotBlank(fieldOperand)) {
                value.append(fieldOperand);
            }
            value.append(FilterString.PROJECT);
            value.append(projectName);
            value.append("\"");
        } else if ("METADATA".equals(fieldName) && !this.metadataName.equals(Helper.getTranslation("notSelected")) && !metadataValue.isEmpty()) {
            value.append("\"");
            if (StringUtils.isNotBlank(fieldOperand)) {
                value.append(fieldOperand);
            }
            value.append(FilterString.METADATA);
            value.append(metadataName);
            value.append(":");
            value.append(metadataValue);
            value.append("\"");
        } else if ("PROCESSPROPERTY".equals(fieldName) && !processPropertyName.equals(Helper.getTranslation("notSelected"))
                && !processPropertyValue.isEmpty()) {
            value.append("\"");
            if (StringUtils.isNotBlank(fieldOperand)) {
                value.append(fieldOperand);
            }
            value.append(FilterString.PROCESSPROPERTY);
            value.append(processPropertyName);
            value.append(":");
            value.append(processPropertyValue);
            value.append("\"");
        } else if ("STEP".equals(fieldName) && !stepStatus.equals(Helper.getTranslation("notSelected")) && !stepName.isEmpty()
                && !stepName.equals(Helper.getTranslation("notSelected"))) {
            value.append("\"");
            if (StringUtils.isNotBlank(fieldOperand)) {
                value.append(fieldOperand);
            }
            value.append(stepStatus);
            value.append(":");
            value.append(stepName);
            value.append("\"");
        } else if ("JOURNAL".equals(fieldName) && !fieldValue.isEmpty()) {
            value.append("\"");
            if (StringUtils.isNotBlank(fieldOperand)) {
                value.append(fieldOperand);
            }
            value.append(FilterString.JOURNAL);
            value.append(fieldValue);
            value.append("\"");
        } else if ("INSTITUTION".equals(fieldName) && StringUtils.isNotBlank(institutionName)) {
            value.append("\"");
            if (StringUtils.isNotBlank(fieldOperand)) {
                value.append(fieldOperand);
            }
            value.append(FilterString.INSTITUTION);
            value.append(":");
            value.append(institutionName);
            value.append("\"");
        } else if ("PROCESSDATE".equals(fieldName) && !fieldValue.isEmpty()) {
            value.append("\"");
            value.append(FilterString.PROCESS_DATE);
            if (StringUtils.isNotBlank(fieldOperand)) {
                value.append(fieldOperand);
            }
            value.append(fieldValue);
            value.append("\"");
        } else if ("STEPSTARTDATE".equals(fieldName) && !fieldValue.isEmpty()) {
            value.append("\"");
            value.append(FilterString.STEP_START_DATE);
            if (StringUtils.isNotBlank(fieldOperand)) {
                value.append(fieldOperand);
            }
            value.append(fieldValue);
            value.append("\"");
        } else if ("STEPFINISHDATE".equals(fieldName) && !fieldValue.isEmpty()) {
            value.append("\"");
            value.append(FilterString.STEP_FINISH_DATE);
            if (StringUtils.isNotBlank(fieldOperand)) {
                value.append(fieldOperand);
            }
            value.append(fieldValue);
            value.append("\"");
        }
        value.append(" ");
        return value.toString();
    }

}
