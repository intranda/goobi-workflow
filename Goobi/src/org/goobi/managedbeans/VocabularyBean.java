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
import java.util.List;

import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
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

/**
 * 
 * This bean can be used to display the current state of the goobi_fast and goobi_slow queues. The bean provides methods to show all active tickets
 * and remove a ticket or clear the queue.
 *
 */

@javax.faces.bean.ManagedBean
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
    @Setter
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

    private List<VocabRecord> recordsToDelete;

    @Getter
    private List<MatchingField> headerOrder;

    public VocabularyBean() {
        uiStatus = "down";
        sortierung = "title";
    }

    public String FilterKein() {
        VocabularyManager vm = new VocabularyManager();
        paginator = new DatabasePaginator(sortierung, filter, vm, "vocabulary_all");
        return "vocabulary_all";
    }

    public String editVocabulary() {
        return "vocabulary_edit";
    }

    public String editRecords() {
        recordsToDelete = new ArrayList<>();
        // load records of selected vocabulary
        VocabularyManager.loadRecordsForVocabulary(currentVocabulary);
        if (!currentVocabulary.getRecords().isEmpty()) {
            currentVocabRecord = currentVocabulary.getRecords().get(0);
        } else {
            addRecord();
        }
        return "vocabulary_records";
    }

    public String newVocabulary() {
        currentVocabulary = new Vocabulary();
        return editVocabulary();
    }

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

        return cancelEdition();
    }

    public String deleteVocabulary() {
        if (currentVocabulary.getId() != null) {
            // TODO delete records as well?
            VocabularyManager.deleteVocabulary(currentVocabulary);
        }
        return cancelEdition();
    }

    public String cancelEdition() {
        return FilterKein();
    }

    public void deleteDefinition() {
        if (currentDefinition != null && currentVocabulary != null) {
            currentVocabulary.getStruct().remove(currentDefinition);
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
        recordsToDelete.add(currentVocabRecord);
        currentVocabulary.getRecords().remove(currentVocabRecord);
    }

    public String cancelRecordEdition() {
        recordsToDelete.clear();

        return cancelEdition();
    }

    public String saveRecordEdition() {
        boolean valid = true;
        for (VocabRecord vr : currentVocabulary.getRecords()) {
            vr.setValid(true);
            for (Field field : vr.getFields()) {
                field.setValidationMessage(null);
                if (field.getDefinition().isRequired()) {
                    if (StringUtils.isBlank(field.getValue())) {
                        valid = false;
                        vr.setValid(false);
                        field.setValidationMessage("vocabularyManager_validation_fieldIsRequired");
                    }
                }
                if (field.getDefinition().isUnique() && StringUtils.isNotBlank(field.getValue())) {
                    requiredCheck: for (VocabRecord other : currentVocabulary.getRecords()) {
                        if (!vr.equals(other)) {
                            for (Field f : other.getFields()) {
                                if (field.getDefinition().equals(f.getDefinition())) {
                                    if (field.getValue().equals(f.getValue())) {
                                        valid = false;
                                        vr.setValid(false);
                                        field.setValidationMessage("vocabularyManager_validation_fieldIsNotUnique");
                                        break requiredCheck;
                                    }
                                }
                            }
                        }
                    }

                }
            }
        }
        if (!valid) {
            return "";
        }
        for (VocabRecord vr : recordsToDelete) {
            VocabularyManager.deleteRecord(vr);
        }

        VocabularyManager.saveRecords(currentVocabulary);
        return cancelEdition();
    }

    public void Reload() {

    }

    public void downloadRecords() {

        VocabularyManager.loadRecordsForVocabulary(currentVocabulary);

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
            for (Field field : record.getFields()) {
                resultRow.createCell(columnCounter).setCellValue(field.getValue());
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

    public void handleFileUpload(FileUploadEvent event) {
        try {
            filename = event.getFile().getFileName();
            copyFile(filename, event.getFile().getInputstream());

        } catch (IOException e) {
            log.error(e);
        }

        loadUploadedFile();
    }

    @SuppressWarnings("deprecation")
    private void loadUploadedFile() {
        InputStream file = null;
        try {
            file = new FileInputStream(importFile.toFile());

            BOMInputStream in = new BOMInputStream(file, false);

            Workbook wb = WorkbookFactory.create(in);

            Sheet sheet = wb.getSheetAt(0);

            Iterator<Row> rowIterator = sheet.rowIterator();
            //  read and validate first row

            Row headerRow = rowIterator.next();

            int numberOfCells = headerRow.getLastCellNum();
            headerOrder = new ArrayList<>(numberOfCells);
            for (int i = 0; i < numberOfCells; i++) {
                Cell cell = headerRow.getCell(i);
                if (cell != null) {
                    cell.setCellType(Cell.CELL_TYPE_STRING);
                    String value = cell.getStringCellValue();
                    headerOrder.add(new MatchingField(value, i, CellReference.convertNumToColString(i)));
                }
            }

            for (MatchingField mf : headerOrder) {
                String excelTitle = mf.getColumnHeader();
                if (excelTitle.matches(".*\\(.*\\)")) {
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
                // try to detect correct field type
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

    public String uploadRecords() {
        headerOrder.clear();
        return "vocabulary_upload";
    }

    @Data
    @RequiredArgsConstructor
    public class MatchingField {
        @NonNull
        private String columnHeader;
        @NonNull
        private Integer columnOrderNumber;
        @NonNull
        private String columnLetter;

        private Definition assignedField;
    }

}