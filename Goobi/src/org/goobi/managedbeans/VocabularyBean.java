package org.goobi.managedbeans;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.goobi.vocabulary.Definition;
import org.goobi.vocabulary.Field;
import org.goobi.vocabulary.VocabRecord;
import org.goobi.vocabulary.Vocabulary;
import org.primefaces.event.FileUploadEvent;

import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.VocabularyManager;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

@Named
@SessionScoped
@Log4j
public class VocabularyBean extends BasicBean implements Serializable {

    private static final long serialVersionUID = -4591427229251805665L;

    @Getter
    @Setter
    private Vocabulary currentVocabulary;

    @Getter
    @Setter
    private Definition currentDefinition;

    @Getter
    private VocabRecord currentVocabRecord;

    //details up or down
    @Getter
    @Setter
    private String uiStatus;

    @Getter
    private String[] possibleDefinitionTypes = { "input", "textarea", "select", "select1", "html" };

    @Getter
    @Setter
    private Path importFile;
    @Getter
    private String filename;

    @Getter
    private List<MatchingField> headerOrder;

    @Getter
    @Setter
    private MatchingField selectedMatchingField;

    @Getter
    private List<SelectItem> allDefinitionNames;

    private List<Row> rowsToImport;

    @Getter
    @Setter
    private String importType = "merge";

    private List<Definition> removedDefinitions = null;

    /**
     * Constructor for class
     */
    public VocabularyBean() {
        uiStatus = "down";
        sortierung = "title";
    }

    /**
     * main method to start searching the records
     * 
     * @return path to list records
     */
    public String FilterKein() {
        VocabularyManager vm = new VocabularyManager();
        paginator = new DatabasePaginator(sortierung, filter, vm, "vocabulary_all");
        return "vocabulary_all";
    }

    /**
     * method to go to the vocabulary edition area
     * 
     * @return path to vocabulary edition arey
     */
    public String editVocabulary() {
        removedDefinitions = new ArrayList<>();
        return "vocabulary_edit";
    }

    /**
     * method to start editing the records
     * 
     * @return path to list and edit records
     */
    public String editRecords() {
        // load records of selected vocabulary
        for (Definition def : currentVocabulary.getStruct()) {
            if (def.isMainEntry()) {
                currentVocabulary.setMainFieldName(def.getLabel());
            }
        }
        // initial first page
        //        VocabularyManager.getPaginatedRecords(currentVocabulary);
        VocabularyManager.getAllRecords(currentVocabulary);
        currentVocabulary.runFilter();
        currentVocabulary.setTotalNumberOfRecords(currentVocabulary.getRecords().size());
        if (!currentVocabulary.getRecords().isEmpty()) {
            currentVocabRecord = currentVocabulary.getRecords().get(0);
        } else {
            addRecord();
        }
        return "vocabulary_records";
    }

    /**
     * start the edition of a new vocabulary
     * 
     * @return path to vocabulary edition area
     */
    public String newVocabulary() {
        currentVocabulary = new Vocabulary();
        return editVocabulary();
    }

    /**
     * method to save the vocabulary definitions
     * 
     * @return path to the vocabulary listing
     */
    public String saveVocabulary() {
        int numberOfMainEntries = 0;
        for (Definition def : currentVocabulary.getStruct()) {
            if (def.isMainEntry()) {
                numberOfMainEntries++;
            }
        }

        // check if one field is marked as main entry
        if (numberOfMainEntries == 0) {
            Helper.setFehlerMeldung(Helper.getTranslation("vocabularyManager_noMainEntry"));
            return "";
        } else if (numberOfMainEntries > 1) {
            Helper.setFehlerMeldung(Helper.getTranslation("vocabularyManager_wrongNumberOfMainEntries"));
            return "";
        }
        // check if title is unique
        if (VocabularyManager.isTitleUnique(currentVocabulary)) {
            VocabularyManager.saveVocabulary(currentVocabulary);
        } else {
            Helper.setFehlerMeldung(Helper.getTranslation("vocabularyManager_titleNotUnique"));
            return "";
        }
        for (Definition def : removedDefinitions) {
            VocabularyManager.deleteDefinition(def);
        }

        return cancelEdition();
    }

    /**
     * method to to delete an existing vocabulay
     * 
     * @return path to the vocabulary listing
     */
    public String deleteVocabulary() {
        if (currentVocabulary.getId() != null) {
            VocabularyManager.deleteVocabulary(currentVocabulary);
        }
        return cancelEdition();
    }

    /**
     * some cleanup and then go to overview page again
     * 
     * @return path to vocabulary listing
     */
    public String cancelEdition() {
        if (removedDefinitions != null) {
            removedDefinitions.clear();
        }
        return FilterKein();
    }

    public void deleteDefinition() {
        if (currentDefinition != null && currentVocabulary != null) {
            currentVocabulary.getStruct().remove(currentDefinition);
            removedDefinitions.add(currentDefinition);
        }
    }

    public void addDefinition() {
        currentVocabulary.getStruct().add(new Definition());
    }

    public void addRecord() {
        VocabRecord rec = new VocabRecord();
        List<Field> fieldList = new ArrayList<>();
        for (Definition definition : currentVocabulary.getStruct()) {
            Field field = new Field(definition.getLabel(), definition.getLanguage(), "", definition);
            fieldList.add(field);
        }
        rec.setFields(fieldList);
        currentVocabulary.getRecords().add(rec);
        currentVocabRecord = rec;
    }

    public void deleteRecord() {
        currentVocabulary.getRecords().remove(currentVocabRecord);
        VocabularyManager.deleteRecord(currentVocabRecord);
        editRecords();
    }

    public String cancelRecordEdition() {
        return cancelEdition();
    }

    /**
     * Save the current records. First it gets validated, if all required fields are filled and if the unique fields are unique. If this is not the
     * case, the records and fields are marked for the user and the saving is aborted. Otherwise the records get saved
     * 
     * @return
     */
    public void saveRecordEdition() {
        currentVocabRecord.setValid(true);
        for (Field field : currentVocabRecord.getFields()) {
            field.setValidationMessage(null);
            if (field.getDefinition().isRequired()) {
                if (StringUtils.isBlank(field.getValue())) {
                    currentVocabRecord.setValid(false);
                    field.setValidationMessage("vocabularyManager_validation_fieldIsRequired");
                }
            }
            if (field.getDefinition().isDistinctive() && StringUtils.isNotBlank(field.getValue())) {
                requiredCheck: for (VocabRecord other : currentVocabulary.getRecords()) {
                    if (!currentVocabRecord.equals(other)) {
                        for (Field f : other.getFields()) {
                            if (field.getDefinition().equals(f.getDefinition())) {
                                if (field.getValue().equals(f.getValue())) {
                                    currentVocabRecord.setValid(false);
                                    field.setValidationMessage("vocabularyManager_validation_fieldIsNotUnique");
                                    break requiredCheck;
                                }
                            }
                        }
                    }
                }

            }
        }

        VocabularyManager.saveRecord(currentVocabulary.getId(), currentVocabRecord);
        VocabRecord temp = currentVocabRecord;
        editRecords();
        setCurrentVocabRecord(temp);
    }

    /**
     * probably unneeded reload method to stay on the same page
     */
    public void Reload() {

    }

    /**
     * create an excel result and send it to the response output stream
     */
    public void downloadRecords() {
        VocabularyManager.getAllRecords(currentVocabulary);
        String title = currentVocabulary.getTitle();
        String description = currentVocabulary.getDescription();
        List<Definition> definitionList = currentVocabulary.getStruct();
        List<VocabRecord> recordList = currentVocabulary.getRecords();

        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet(StringUtils.isBlank(description) ? title : title + " - " + description);

        // create header
        Row headerRow = sheet.createRow(0);
        int columnCounter = 0;
        for (Definition definition : definitionList) {
            headerRow.createCell(columnCounter)
            .setCellValue(StringUtils.isNotBlank(definition.getLanguage()) ? definition.getLabel() + " (" + definition.getLanguage() + ")"
                    : definition.getLabel());
            columnCounter = columnCounter + 1;
        }

        int rowCounter = 1;
        // add records
        for (VocabRecord record : recordList) {
            Row resultRow = sheet.createRow(rowCounter);
            columnCounter = 0;
            for (Definition definition : definitionList) {
                resultRow.createCell(columnCounter).setCellValue(record.getFieldValue(definition));
                columnCounter = columnCounter + 1;
            }
            rowCounter = rowCounter + 1;
        }

        // write result into output stream
        FacesContext facesContext = FacesContextHelper.getCurrentFacesContext();

        HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
        OutputStream out;
        try {
            out = response.getOutputStream();
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment;filename=\"" + title + ".xlsx\"");
            wb.write(out);
            out.flush();

            facesContext.responseComplete();
        } catch (IOException e) {
            log.error(e);
        }
        try {
            wb.close();
        } catch (IOException e) {
            log.error(e);
        }
    }

    /**
     * allow file upload for vocabulary records
     * 
     * @param event
     */
    public void handleFileUpload(FileUploadEvent event) {
        try {
            filename = event.getFile().getFileName();
            copyFile(filename, event.getFile().getInputstream());

        } catch (IOException e) {
            log.error(e);
        }
        loadUploadedFile();
    }

    /**
     * internal method to manage the file upload for vocabulary records
     */
    private void loadUploadedFile() {
        InputStream file = null;
        try {
            file = new FileInputStream(importFile.toFile());
            BOMInputStream in = new BOMInputStream(file, false);
            Workbook wb = WorkbookFactory.create(in);
            Sheet sheet = wb.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.rowIterator();
            Row headerRow = rowIterator.next();
            int numberOfCells = headerRow.getLastCellNum();
            headerOrder = new ArrayList<>(numberOfCells);
            rowsToImport = new LinkedList<>();
            for (int i = 0; i < numberOfCells; i++) {
                Cell cell = headerRow.getCell(i);
                if (cell != null) {
                    cell.setCellType(CellType.STRING);
                    String value = cell.getStringCellValue();
                    headerOrder.add(new MatchingField(value, i, CellReference.convertNumToColString(i), this));
                }
            }
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                rowsToImport.add(row);
            }

            for (MatchingField mf : headerOrder) {
                String excelTitle = mf.getColumnHeader();
                if (excelTitle.matches(".*\\(.{3}\\)")) {
                    String titlePart = excelTitle.substring(0, excelTitle.lastIndexOf("(")).trim();
                    String languagePart = excelTitle.substring(excelTitle.lastIndexOf("(") + 1, excelTitle.lastIndexOf(")")).trim();
                    for (Definition def : currentVocabulary.getStruct()) {
                        if (def.getLabel().equals(titlePart) && def.getLanguage().equals(languagePart)) {
                            mf.setAssignedField(def);
                        }
                    }
                } else {
                    String titlePart = excelTitle.trim();
                    for (Definition def : currentVocabulary.getStruct()) {
                        if (def.getLabel().equals(titlePart) && StringUtils.isBlank(def.getLanguage())) {
                            mf.setAssignedField(def);
                        }
                    }
                }
            }

        } catch (Exception e) {
            Helper.setFehlerMeldung("file not readable", e);
            log.error(e);
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (IOException e) {
                    log.error(e);
                }
            }
        }

    }

    public void copyFile(String fileName, InputStream in) {
        OutputStream out = null;
        try {
            String extension = fileName.substring(fileName.indexOf("."));
            importFile = Files.createTempFile(fileName, extension);
            out = new FileOutputStream(importFile.toFile());
            int read = 0;
            byte[] bytes = new byte[1024];
            while ((read = in.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.flush();
        } catch (IOException e) {
            log.error(e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.error(e);
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    log.error(e);
                }
            }
        }
    }

    /**
     * navigate to the excel updoad area
     * 
     * @return path to the excel upload area
     */
    public String uploadRecords() {
        VocabularyManager.getAllRecords(currentVocabulary);
        allDefinitionNames = new ArrayList<>();
        allDefinitionNames.add(new SelectItem("", "-"));
        for (Definition definition : currentVocabulary.getStruct()) {
            String definitionName;
            if (StringUtils.isNotBlank(definition.getLanguage())) {
                definitionName = definition.getLabel() + " (" + definition.getLanguage() + ")";
            } else {
                definitionName = definition.getLabel();
            }
            allDefinitionNames.add(new SelectItem(definitionName, definitionName));
        }

        headerOrder = null;
        filename = null;
        importFile = null;
        return "vocabulary_upload";
    }

    /**
     * Checks if the assigned field is used in a different {@link MatchingField}. If this is the case, the other assignment is removed
     * 
     * @param currentField
     */
    private void updateFieldList(MatchingField currentField) {
        for (MatchingField other : headerOrder) {
            if (!other.equals(currentField) && other.getAssignedField() != null) {
                if (other.getCurrentDefinition().equals(currentField.getCurrentDefinition())) {
                    other.setAssignedField(null);
                }
            }
        }
    }

    /**
     * Import the records from the excel file. First all old records are deleted, then for each row a new record is created. The fields are filled
     * based on the configured {@link MatchingField}s
     * 
     * @return
     */
    public String importRecords() {
        if (importType.equals("remove")) {
            // if selected, remove existing entries of this vocabulary
            VocabularyManager.deleteAllRecords(currentVocabulary);
            currentVocabulary.setRecords(new ArrayList<>());
        }
        if (importType.equals("remove") || importType.equals("add")) {
            for (Row row : rowsToImport) {
                VocabRecord record = new VocabRecord();
                List<Field> fieldList = new ArrayList<>();
                for (MatchingField mf : headerOrder) {
                    if (mf.getAssignedField() != null) {
                        String cellValue = getCellValue(row.getCell(mf.getColumnOrderNumber()));
                        if (StringUtils.isNotBlank(cellValue)) {
                            Field field = new Field(mf.getAssignedField().getLabel(), mf.getAssignedField().getLanguage(), cellValue,
                                    mf.getAssignedField());
                            fieldList.add(field);
                        }
                    }
                }
                if (!fieldList.isEmpty()) {
                    addFieldToRecord(record, fieldList);
                }

            }
            VocabularyManager.insertNewRecords(currentVocabulary.getRecords(), currentVocabulary.getId());
        }

        if (importType.equals("merge")) {
            List<VocabRecord> newRecords = new ArrayList<>();
            List<VocabRecord> updateRecords = new ArrayList<>();
            // get main entry row
            Integer mainEntryColumnNumber = null;
            for (MatchingField mf : headerOrder) {
                if (mf.getAssignedField() != null && mf.getAssignedField().isMainEntry()) {
                    mainEntryColumnNumber = mf.getColumnOrderNumber();
                }
            }
            for (Row row : rowsToImport) {
                // search for existing records based on the value of the main entry
                VocabRecord recordToUpdate = null;
                if (mainEntryColumnNumber != null) {
                    String uniqueIdentifierEntry = getCellValue(row.getCell(mainEntryColumnNumber));
                    outerloop: for (VocabRecord vr : currentVocabulary.getRecords()) {
                        for (Field field : vr.getFields()) {
                            if (field.getDefinition().isMainEntry() && uniqueIdentifierEntry.equals(field.getValue())) {
                                recordToUpdate = vr;
                                break outerloop;
                            }
                        }
                    }
                    if (recordToUpdate != null) {
                        updateRecords.add(recordToUpdate);
                        // update existing record
                        for (MatchingField mf : headerOrder) {
                            Field fieldToUpdate = null;
                            for (Field field : recordToUpdate.getFields()) {
                                if (mf.getAssignedField().equals(field.getDefinition())) {
                                    fieldToUpdate = field;
                                    break;
                                }
                            }
                            String cellValue = getCellValue(row.getCell(mf.getColumnOrderNumber()));
                            if (fieldToUpdate == null) {
                                fieldToUpdate = new Field(mf.getAssignedField().getLabel(), mf.getAssignedField().getLanguage(), cellValue,
                                        mf.getAssignedField());
                                recordToUpdate.getFields().add(fieldToUpdate);
                            } else {
                                fieldToUpdate.setValue(cellValue);
                            }
                        }
                        continue;
                    } else {
                        // create new record
                        VocabRecord record = new VocabRecord();
                        List<Field> fieldList = new ArrayList<>();
                        for (MatchingField mf : headerOrder) {
                            if (mf.getAssignedField() != null) {
                                String cellValue = getCellValue(row.getCell(mf.getColumnOrderNumber()));
                                if (StringUtils.isNotBlank(cellValue)) {
                                    Field field = new Field(mf.getAssignedField().getLabel(), mf.getAssignedField().getLanguage(), cellValue,
                                            mf.getAssignedField());
                                    fieldList.add(field);
                                }

                            }
                        }
                        if (!fieldList.isEmpty()) {
                            addFieldToRecord(record, fieldList);
                            newRecords.add(record);
                        }
                    }
                }
            }
            if (!newRecords.isEmpty()) {
                VocabularyManager.insertNewRecords(newRecords, currentVocabulary.getId());
            }
            if (!updateRecords.isEmpty()) {
                VocabularyManager.batchUpdateRecords(updateRecords, currentVocabulary.getId());
            }
        }
        //  VocabularyManager.saveRecords(currentVocabulary);
        return FilterKein();
    }

    /**
     * method to add a field to the existing record
     * 
     * @param record Record to use
     * @param fieldList List of fields to add to the record
     */
    private void addFieldToRecord(VocabRecord record, List<Field> fieldList) {
        for (Definition def : currentVocabulary.getStruct()) {
            boolean fieldExists = false;
            for (Field f : fieldList) {
                if (def.getId().equals(f.getDefinition().getId())) {
                    fieldExists = true;
                    break;
                }
            }
            if (!fieldExists) {
                Field emptyField = new Field(def.getLabel(), def.getLanguage(), "", def);
                fieldList.add(emptyField);
            }
        }
        record.setFields(fieldList);
        currentVocabulary.getRecords().add(record);
    }

    /**
     * returns the value of the current cell as string
     */
    @SuppressWarnings("deprecation")
    private String getCellValue(Cell cell) {
        String value = "";
        if (cell != null) {
            cell.setCellType(CellType.STRING);
            value = cell.getStringCellValue();
        }
        return value;
    }

    /**
     * This class is used to match the excel columns and the vocabulary fields
     */
    @Data
    @RequiredArgsConstructor
    public class MatchingField {

        /**
         * Name of the header of the current column within the excel file
         */
        @NonNull
        private String columnHeader;

        /**
         * Internal order number of the current column within the excel file
         */
        @NonNull
        private Integer columnOrderNumber;

        /**
         * Displayed label the current column within the excel file (1=A, 2=B, 3=C, ...)
         */
        @NonNull
        private String columnLetter;

        /**
         * Reference to the managed bean
         */
        @NonNull
        private VocabularyBean bean;

        /**
         * field in which the current data is imported
         */
        private Definition assignedField;

        /**
         * Creates a label to identify the assigned field
         * 
         * @return
         */
        public String getCurrentDefinition() {
            if (assignedField == null) {
                return "-";
            }
            String definitionName;
            if (StringUtils.isNotBlank(assignedField.getLanguage())) {
                definitionName = assignedField.getLabel() + " (" + assignedField.getLanguage() + ")";
            } else {
                definitionName = assignedField.getLabel();
            }
            return definitionName;
        }

        /**
         * Set the assigned field based on the selected label. If a new field is set, all other fields are checked if the current field was already
         * selected. If this is the case, the selection is removed from the other field
         * 
         * @param value
         */
        public void setCurrentDefinition(String value) {

            if (StringUtils.isNotBlank(value) && !"-".equals(value)) {
                if (value.matches(".*\\(.*\\)")) {
                    String titlePart = value.substring(0, value.lastIndexOf("(")).trim();
                    String languagePart = value.substring(value.lastIndexOf("(") + 1, value.lastIndexOf(")")).trim();
                    for (Definition def : currentVocabulary.getStruct()) {
                        if (def.getLabel().equals(titlePart) && def.getLanguage().equals(languagePart)) {
                            assignedField = def;
                            break;
                        }
                    }
                } else {
                    for (Definition def : currentVocabulary.getStruct()) {
                        if (def.getLabel().equals(value) && StringUtils.isBlank(def.getLanguage())) {
                            assignedField = def;
                        }
                    }
                }
                bean.updateFieldList(this);
            } else {
                assignedField = null;
            }
        }
    }

    /**
     * method to set the current record to use
     * 
     * @param currentVocabRecord the record to use
     */
    public void setCurrentVocabRecord(VocabRecord currentVocabRecord) {
        if (this.currentVocabRecord == null || !this.currentVocabRecord.equals(currentVocabRecord)) {
            this.currentVocabRecord = currentVocabRecord;
        }
    }

}