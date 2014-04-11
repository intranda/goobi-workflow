package org.goobi.production.search.api;

import org.goobi.production.flow.statistics.hibernate.FilterString;

import de.sub.goobi.helper.Helper;

public class ExtendedSearchRow {

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

    public String createSearchString() {
        String value = "";
        if (fieldName.equals("PROCESSTITLE") && !fieldValue.isEmpty()) {
            value = "\"" + this.fieldOperand + this.fieldValue + "\" ";
        } else if (fieldName.equals("PROCESSID") && !fieldValue.isEmpty()) {
            value = "\"" + FilterString.ID + this.fieldValue + "\" ";
        }

        else if (fieldName.equals("BATCH") && !fieldValue.isEmpty()) {
            value = "\"" + FilterString.BATCH + this.fieldValue + "\" ";
        }

        else if (fieldName.equals("PROJECT") && !this.projectName.equals(Helper.getTranslation("notSelected"))) {
            value = "\"" + this.fieldOperand + FilterString.PROJECT + this.projectName + "\" ";
        } else if (fieldName.equals("PROJECT") && !this.metadataName.equals(Helper.getTranslation("notSelected")) && !fieldValue.isEmpty()) {
            value = "\"" + this.fieldOperand + FilterString.METADATA + metadataName + ":" + fieldValue + "\" ";

        }

        else if (fieldName.equals("PROCESSPROPERTY") && !processPropertyName.equals(Helper.getTranslation("notSelected"))
                && !processPropertyValue.isEmpty()) {
            value = "\"" + ":" + this.fieldOperand + FilterString.PROCESSPROPERTY + this.processPropertyName + ":" + processPropertyValue + "\" ";
        }

        else if (fieldName.equals("WORKPIECE") && !masterpiecePropertyName.equals(Helper.getTranslation("notSelected"))
                && !masterpiecePropertyValue.isEmpty()) {
            value = "\"" + ":" + this.fieldOperand + FilterString.WORKPIECE + this.masterpiecePropertyName + ":" + masterpiecePropertyValue + "\" ";
        }

        else if (fieldName.equals("TEMPLATE") && !templatePropertyName.equals(Helper.getTranslation("notSelected"))
                && !templatePropertyValue.isEmpty()) {
            value = "\"" + ":" + this.fieldOperand + FilterString.TEMPLATE + this.templatePropertyName + ":" + templatePropertyValue + "\" ";
        }

        else if (fieldName.equals("STEP") && !stepStatus.equals(Helper.getTranslation("notSelected")) && !stepName.isEmpty()) {
            value = "\"" + this.fieldOperand + this.stepStatus + ":" + this.stepName + "\" ";
        }

        return value;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getFieldOperand() {
        return fieldOperand;
    }

    public String getFieldValue() {
        return fieldValue;
    }

    public String getStepStatus() {
        return stepStatus;
    }

    public String getStepName() {
        return stepName;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getProcessPropertyName() {
        return processPropertyName;
    }

    public String getProcessPropertyValue() {
        return processPropertyValue;
    }

    public String getTemplatePropertyName() {
        return templatePropertyName;
    }

    public String getTemplatePropertyValue() {
        return templatePropertyValue;
    }

    public String getMasterpiecePropertyName() {
        return masterpiecePropertyName;
    }

    public String getMasterpiecePropertyValue() {
        return masterpiecePropertyValue;
    }

    public String getMetadataName() {
        return metadataName;
    }

    public String getMetadataValue() {
        return metadataValue;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public void setFieldOperand(String fieldOperand) {
        this.fieldOperand = fieldOperand;
    }

    public void setFieldValue(String fieldValue) {
        this.fieldValue = fieldValue;
    }

    public void setStepStatus(String stepStatus) {
        this.stepStatus = stepStatus;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public void setProcessPropertyName(String processPropertyName) {
        this.processPropertyName = processPropertyName;
    }

    public void setProcessPropertyValue(String processPropertyValue) {
        this.processPropertyValue = processPropertyValue;
    }

    public void setTemplatePropertyName(String templatePropertyName) {
        this.templatePropertyName = templatePropertyName;
    }

    public void setTemplatePropertyValue(String templatePropertyValue) {
        this.templatePropertyValue = templatePropertyValue;
    }

    public void setMasterpiecePropertyName(String masterpiecePropertyName) {
        this.masterpiecePropertyName = masterpiecePropertyName;
    }

    public void setMasterpiecePropertyValue(String masterpiecePropertyValue) {
        this.masterpiecePropertyValue = masterpiecePropertyValue;
    }

    public void setMetadataName(String metadataName) {
        this.metadataName = metadataName;
    }

    public void setMetadataValue(String metadataValue) {
        this.metadataValue = metadataValue;
    }

}
