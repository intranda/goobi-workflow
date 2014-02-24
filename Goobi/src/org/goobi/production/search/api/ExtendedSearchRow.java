package org.goobi.production.search.api;

public class ExtendedSearchRow {

    private String firstFieldName;

    private String firstFieldOperand;

    private String firstFieldValue;

    private String fieldOperand;

    private String secondFieldName;

    private String secondFieldOperand;

    private String secondFieldValue;

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

    @Override
    public String toString() {
        return "ExtendedSearchRow [firstFieldName=" + firstFieldName + ", firstFieldOperand=" + firstFieldOperand + ", firstFieldValue="
                + firstFieldValue + ", fieldOperand=" + fieldOperand + ", secondFieldName=" + secondFieldName + ", secondFieldOperand="
                + secondFieldOperand + ", secondFieldValue=" + secondFieldValue + "]";
    }
    

}
