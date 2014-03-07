package org.goobi.production.search.api;

public class ExtendedSearchRow {

    private String firstFieldName;

    private String firstFieldOperand;

    // process title, id, batch
    private String firstFieldValue;

    // step
    private String firstStepStatus;

    private String firstStepName;

    // project
    private String firstProjectName;

    // properties
    private String firstProcessPropertyName;
    
    private String firstProcessPropertyValue;

    private String firstTemplatePropertyName;
    
    private String firstTemplatePropertyValue;

    private String firstMasterpiecePropertyName;
    
    private String firstMasterpiecePropertyValue;
    
    private String firstMetadataName;
    
    private String firstMetadataValue;  

    private String fieldOperand;

    private String secondFieldName;

    private String secondFieldOperand;

    // process title, id, batch
    private String secondFieldValue;

    // step
    private String secondStepStatus;

    private String secondStepName;

    // project
    private String secondProjectName;

    // properties
    private String secondProcessPropertyName;
    
    private String secondProcessPropertyValue;

    private String secondTemplatePropertyName;
    
    private String secondTemplatePropertyValue;

    private String secondMasterpiecePropertyName;
    
    private String secondMasterpiecePropertyValue;

    private String secondMetadataName;
    
    private String secondMetadataValue;  
    
    public String getFirstFieldName() {
        return firstFieldName;
    }

    public String getFirstFieldOperand() {
        return firstFieldOperand;
    }

    public String getFirstFieldValue() {
        return firstFieldValue;
    }

    public String getFieldOperand() {
        return fieldOperand;
    }

    public String getSecondFieldName() {
        return secondFieldName;
    }

    public String getSecondFieldOperand() {
        return secondFieldOperand;
    }

    public String getSecondFieldValue() {
        return secondFieldValue;
    }

    public void setFirstFieldName(String firstFieldName) {
        this.firstFieldName = firstFieldName;
    }

    public void setFirstFieldOperand(String firstFieldOperand) {
        this.firstFieldOperand = firstFieldOperand;
    }

    public void setFirstFieldValue(String firstFieldValue) {
        this.firstFieldValue = firstFieldValue;
    }

    public void setFieldOperand(String fieldOperand) {
        this.fieldOperand = fieldOperand;
    }

    public void setSecondFieldName(String secondFieldName) {
        this.secondFieldName = secondFieldName;
    }

    public void setSecondFieldOperand(String secondFieldOperand) {
        this.secondFieldOperand = secondFieldOperand;
    }

    public void setSecondFieldValue(String secondFieldValue) {
        this.secondFieldValue = secondFieldValue;
    }

    public String getFirstStepStatus() {
        return firstStepStatus;
    }

    public String getFirstStepName() {
        return firstStepName;
    }

    public String getFirstProjectName() {
        return firstProjectName;
    }

    public String getFirstProcessPropertyName() {
        return firstProcessPropertyName;
    }

    public String getFirstTemplatePropertyName() {
        return firstTemplatePropertyName;
    }

    public String getFirstMasterpiecePropertyName() {
        return firstMasterpiecePropertyName;
    }

    public void setFirstStepStatus(String firstStepStatus) {
        this.firstStepStatus = firstStepStatus;
    }

    public void setFirstStepName(String firstStepName) {
        this.firstStepName = firstStepName;
    }

    public void setFirstProjectName(String firstProjectName) {
        this.firstProjectName = firstProjectName;
    }

    public void setFirstProcessPropertyName(String firstProcessPropertyName) {
        this.firstProcessPropertyName = firstProcessPropertyName;
    }

    public void setFirstTemplatePropertyName(String firstTemplatePropertyName) {
        this.firstTemplatePropertyName = firstTemplatePropertyName;
    }

    public void setFirstMasterpiecePropertyName(String firstMasterpiecePropertyName) {
        this.firstMasterpiecePropertyName = firstMasterpiecePropertyName;
    }

    public String getFirstMasterpiecePropertyValue() {
        return firstMasterpiecePropertyValue;
    }

    public String getFirstProcessPropertyValue() {
        return firstProcessPropertyValue;
    }

    public String getFirstTemplatePropertyValue() {
        return firstTemplatePropertyValue;
    }

    public void setFirstMasterpiecePropertyValue(String firstMasterpiecePropertyValue) {
        this.firstMasterpiecePropertyValue = firstMasterpiecePropertyValue;
    }

    public void setFirstProcessPropertyValue(String firstProcessPropertyValue) {
        this.firstProcessPropertyValue = firstProcessPropertyValue;
    }

    public void setFirstTemplatePropertyValue(String firstTemplatePropertyValue) {
        this.firstTemplatePropertyValue = firstTemplatePropertyValue;
    }
    

    public String getSecondStepStatus() {
        return secondStepStatus;
    }

    public String getSecondStepName() {
        return secondStepName;
    }

    public String getSecondProjectName() {
        return secondProjectName;
    }

    public String getSecondProcessPropertyName() {
        return secondProcessPropertyName;
    }

    public String getSecondProcessPropertyValue() {
        return secondProcessPropertyValue;
    }

    public String getSecondTemplatePropertyName() {
        return secondTemplatePropertyName;
    }

    public String getSecondTemplatePropertyValue() {
        return secondTemplatePropertyValue;
    }

    public String getSecondMasterpiecePropertyName() {
        return secondMasterpiecePropertyName;
    }

    public String getSecondMasterpiecePropertyValue() {
        return secondMasterpiecePropertyValue;
    }

    public void setSecondStepStatus(String secondStepStatus) {
        this.secondStepStatus = secondStepStatus;
    }

    public void setSecondStepName(String secondStepName) {
        this.secondStepName = secondStepName;
    }

    public void setSecondProjectName(String secondProjectName) {
        this.secondProjectName = secondProjectName;
    }

    public void setSecondProcessPropertyName(String secondProcessPropertyName) {
        this.secondProcessPropertyName = secondProcessPropertyName;
    }

    public void setSecondProcessPropertyValue(String secondProcessPropertyValue) {
        this.secondProcessPropertyValue = secondProcessPropertyValue;
    }

    public void setSecondTemplatePropertyName(String secondTemplatePropertyName) {
        this.secondTemplatePropertyName = secondTemplatePropertyName;
    }

    public void setSecondTemplatePropertyValue(String secondTemplatePropertyValue) {
        this.secondTemplatePropertyValue = secondTemplatePropertyValue;
    }

    public void setSecondMasterpiecePropertyName(String secondMasterpiecePropertyName) {
        this.secondMasterpiecePropertyName = secondMasterpiecePropertyName;
    }

    public void setSecondMasterpiecePropertyValue(String secondMasterpiecePropertyValue) {
        this.secondMasterpiecePropertyValue = secondMasterpiecePropertyValue;
    }

    public String createSearchString() {
        // TODO Auto-generated method stub
        return toString();
    }

    public String getFirstMetadataName() {
        return firstMetadataName;
    }

    public void setFirstMetadataName(String firstMetadataName) {
        this.firstMetadataName = firstMetadataName;
    }

    public String getFirstMetadataValue() {
        return firstMetadataValue;
    }

    public void setFirstMetadataValue(String firstMetadataValue) {
        this.firstMetadataValue = firstMetadataValue;
    }

    public String getSecondMetadataName() {
        return secondMetadataName;
    }

    public void setSecondMetadataName(String secondMetadataName) {
        this.secondMetadataName = secondMetadataName;
    }

    public String getSecondMetadataValue() {
        return secondMetadataValue;
    }

    public void setSecondMetadataValue(String secondMetadataValue) {
        this.secondMetadataValue = secondMetadataValue;
    }
}
