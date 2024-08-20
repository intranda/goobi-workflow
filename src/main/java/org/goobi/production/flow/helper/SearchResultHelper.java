package org.goobi.production.flow.helper;

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

import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.faces.model.SelectItem;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.goobi.production.flow.statistics.hibernate.FilterHelper;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblWidth;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Cell;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Table;
import com.lowagie.text.rtf.RtfWriter2;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.MetadataManager;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.PropertyManager;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class SearchResultHelper {

    private static final String METADATA = "metadata";

    @Getter
    private List<SelectItem> possibleColumns = new ArrayList<>();

    public SearchResultHelper() {

        possibleColumns.add(new SelectItem("all", Helper.getTranslation("selectAllFields")));

        String processData = "processData";
        possibleColumns.add(new SelectItem(processData, Helper.getTranslation(processData), Helper.getTranslation(processData), true));

        String processTitle = "prozesse.Titel";
        possibleColumns.add(new SelectItem(processTitle, Helper.getTranslation(processTitle)));

        String processId = "prozesse.ProzesseID";
        possibleColumns.add(new SelectItem(processId, Helper.getTranslation(processId)));

        String processCreationDate = "prozesse.erstellungsdatum";
        possibleColumns.add(new SelectItem(processCreationDate, Helper.getTranslation(processCreationDate)));

        String processSortHelperImages = "prozesse.sortHelperImages";
        possibleColumns.add(new SelectItem(processSortHelperImages, Helper.getTranslation(processSortHelperImages)));

        String processSortHelperMetadata = "prozesse.sortHelperMetadata";
        possibleColumns.add(new SelectItem(processSortHelperMetadata, Helper.getTranslation(processSortHelperMetadata)));

        String processSortHelperDocstructs = "prozesse.sortHelperDocstructs";
        possibleColumns.add(new SelectItem(processSortHelperDocstructs, Helper.getTranslation(processSortHelperDocstructs)));

        String projectTitle = "projekte.Titel";
        possibleColumns.add(new SelectItem(projectTitle, Helper.getTranslation(projectTitle)));

        possibleColumns.add(new SelectItem("log.lastError", Helper.getTranslation("SearchResultField_lastError")));

        List<String> columnWhiteList = ConfigurationHelper.getInstance().getDownloadColumnWhitelist();
        if (columnWhiteList == null || columnWhiteList.isEmpty()) {
            return;
        }

        // data from configuration

        List<String> processTitles = PropertyManager.getDistinctProcessPropertyTitles();
        if (!processTitles.isEmpty()) {

            for (String title : processTitles) {
                String key = "prozesseeigenschaften." + title;
                if (columnWhiteList.contains(title)) {
                    possibleColumns.add(new SelectItem(key, Helper.getTranslation(key)));
                }
            }
        }

        List<String> templateTitles = PropertyManager.getDistinctTemplatePropertyTitles();
        if (!templateTitles.isEmpty()) {
            List<SelectItem> subList = new ArrayList<>();

            for (String title : templateTitles) {
                if (columnWhiteList.contains(title)) {
                    String key = "vorlageneigenschaften." + title;
                    subList.add(new SelectItem(key, Helper.getTranslation(key)));
                }
            }
            if (!subList.isEmpty()) {
                String key = "templateData";
                String translation = Helper.getTranslation(key);
                possibleColumns.add(new SelectItem(key, translation, translation, true));
                possibleColumns.addAll(subList);
            }
        }

        List<String> masterpiecePropertyTitles = PropertyManager.getDistinctMasterpiecePropertyTitles();
        if (!masterpiecePropertyTitles.isEmpty()) {
            List<SelectItem> subList = new ArrayList<>();

            for (String title : masterpiecePropertyTitles) {
                if (columnWhiteList.contains(title)) {
                    String key = "werkstueckeeigenschaften." + title;
                    subList.add(new SelectItem(key, Helper.getTranslation(key)));
                }
            }
            if (!subList.isEmpty()) {
                String key = "masterpieceData";
                String translation = Helper.getTranslation(key);
                possibleColumns.add(new SelectItem(key, translation, translation, true));
                possibleColumns.addAll(subList);
            }
        }

        List<String> metadataTitles = MetadataManager.getDistinctMetadataNames();
        if (!metadataTitles.isEmpty()) {
            List<SelectItem> subList = new ArrayList<>();

            for (String title : metadataTitles) {
                if (columnWhiteList.contains(title)) {
                    String key = "metadata." + title;
                    subList.add(new SelectItem(key, Helper.getTranslation(key)));
                }
            }

            if (!subList.isEmpty()) {
                String key = "metadataData";
                String translation = Helper.getTranslation(key);
                possibleColumns.add(new SelectItem(key, translation, translation, true));
                possibleColumns.addAll(subList);
            }
        }
    }

    public XWPFDocument getResultAsWord(List<SearchColumn> columnList, String filter, String order, boolean showClosedProcesses,
            boolean showArchivedProjects) {
        @SuppressWarnings("rawtypes")
        List list = search(columnList, filter, order, showClosedProcesses, showArchivedProjects);

        XWPFDocument doc = new XWPFDocument();

        // create header row
        int colNum = columnList.size();
        int rowNum = list.size() + 1;

        XWPFTable table = doc.createTable(rowNum, colNum);
        CTTblWidth width = table.getCTTbl().addNewTblPr().addNewTblW();
        width.setType(STTblWidth.DXA);
        width.setW(BigInteger.valueOf(10000));

        int currentRow = 0;
        int currentCol = 0;
        XWPFTableRow headerRow = table.getRow(currentRow++);
        for (SearchColumn sc : columnList) {
            XWPFTableCell cell = headerRow.getCell(currentCol++);
            cell.setText(Helper.getTranslation(sc.getValue()));
        }

        for (Object obj : list) {
            currentCol = 0;
            Object[] objArr = (Object[]) obj;
            XWPFTableRow row = table.getRow(currentRow++);
            for (Object entry : objArr) {
                XWPFTableCell cell = row.getCell(currentCol++);
                cell.setText((String) entry);
            }
        }

        return doc;
    }

    public void getResultAsRtf(List<SearchColumn> columnList, String filter, String order, boolean showClosedProcesses, boolean showArchivedProjects,
            OutputStream out) {
        Document document = new Document();

        RtfWriter2.getInstance(document, out);

        @SuppressWarnings("rawtypes")
        List list = search(columnList, filter, order, showClosedProcesses, showArchivedProjects);

        document.open();

        Table table = null;
        try {
            table = new Table(columnList.size());
        } catch (BadElementException exception) {
            log.error(exception);
        }
        if (table != null) {
            table.setBorderWidth(1);

            for (SearchColumn sc : columnList) {
                Cell cell = new Cell(Helper.getTranslation(sc.getValue()));
                cell.setHeader(true);
                table.addCell(cell);
            }
            table.endHeaders();

            for (Object obj : list) {
                Object[] objArr = (Object[]) obj;
                for (Object entry : objArr) {
                    Cell cell = new Cell((String) entry);
                    table.addCell(cell);
                }
            }
        }
        try {
            document.add(table);
        } catch (DocumentException exception) {
            log.error(exception);
        }

        document.close();
    }

    public XSSFWorkbook getResult(List<SearchColumn> columnList, String filter, String order, boolean showClosedProcesses,
            boolean showArchivedProjects) {
        List<SearchColumn> sortedList = new ArrayList<>(columnList.size());
        for (SearchColumn sc : columnList) {
            if (!sc.getTableName().startsWith(METADATA)) {
                sortedList.add(sc);
            }
        }
        for (SearchColumn sc : columnList) {
            if (sc.getTableName().startsWith(METADATA)) {
                sortedList.add(sc);
            }
        }
        columnList = sortedList;

        @SuppressWarnings("rawtypes")
        List list = search(columnList, filter, order, showClosedProcesses, showArchivedProjects);

        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet("Search results");

        // create title row
        int titleColumnNumber = 0;
        XSSFRow title = sheet.createRow(0);
        int columnNumber = 0;
        for (SearchColumn sc : columnList) {
            XSSFCell titleCell = title.createCell(titleColumnNumber++);
            titleCell.setCellValue(Helper.getTranslation(sc.getValue()));
            XSSFCellStyle cellStyle = wb.createCellStyle();
            XSSFFont cellFont = wb.createFont();
            cellFont.setBold(true);
            cellStyle.setFont(cellFont);
            titleCell.setCellStyle(cellStyle);
        }

        int rowNumber = 1;
        for (Object obj : list) {
            Object[] objArr = (Object[]) obj;
            XSSFRow row = sheet.createRow(rowNumber++);
            columnNumber = 0;
            for (Object entry : objArr) {
                XSSFCell cell = row.createCell(columnNumber++);
                if (entry != null) {
                    cell.setCellValue(((String) entry).replace("\"", ""));
                } else {
                    cell.setCellValue("");
                }
            }
        }

        sheet.createFreezePane(0, 1);
        for (int i = 0; i < columnList.size(); i++) {
            sheet.autoSizeColumn(i);
            if (sheet.getColumnWidth(i) > 15000) {
                sheet.setColumnWidth(i, 15000);
            }
        }

        return wb;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private List search(List<SearchColumn> columnList, String filter, String order, boolean showClosedProcesses, boolean showArchivedProjects) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT distinct prozesse.ProzesseID, ");

        if (StringUtils.isNotBlank(order)) {
            sb.append(order.replace(" desc", "").replace(" asc", "") + ", ");
        }

        boolean includeLog = false;

        // add column labels to query
        for (SearchColumn sc : columnList) {
            if (sc.getTableName().startsWith("log")) {
                // reduce length to max 2000 chararcters to avoid 'maximum length of cell contents (text) is 32767 characters' error in xls
                sb.append("if (LENGTH(log.content) > 2000, SUBSTRING(log.content, 1, 2000), log.content), ");
                includeLog = true;
            }

            else if (!sc.getTableName().startsWith(METADATA)) {
                sb.append(sc.getTableName() + "." + sc.getColumnName() + ", ");
            }
        }

        int length = sb.length();
        sb = sb.replace(length - 2, length, "");
        sb.append(" FROM prozesse LEFT JOIN projekte on projekte.ProjekteID = prozesse.ProjekteID ");
        sb.append("left join batches on prozesse.batchId = batches.id ");

        if (includeLog) {
            sb.append(" left join journal log on log.objectID = prozesse.ProzesseID and log.entrytype = 'process' and log.id = ");
            sb.append("(select max(id) from journal where objectID = prozesse.ProzesseID and log.entrytype = 'process' and type  = 'error') ");
        }

        boolean leftJoin = false;

        for (SearchColumn sc : columnList) {
            if (sc.getTableName().startsWith("log.")) {
                sb.append(" log.content ");
            } else if (!sc.getTableName().startsWith(METADATA) && !sc.getTableName().startsWith("projekte")) {
                String clause = sc.getJoinClause();
                if (!clause.isEmpty()) {
                    if (!leftJoin) {
                        sb.append(" LEFT JOIN ");
                    } else {
                        sb.append(" JOIN ");
                    }
                    sb.append(clause);
                }
            }
        }

        String sql = FilterHelper.criteriaBuilder(filter, false, null, null, null, true, false);
        if (!sql.isEmpty()) {
            sql = sql + " AND ";
        }
        sql = sql + " prozesse.istTemplate = false ";

        if (!showClosedProcesses) {
            if (!sql.isEmpty()) {
                sql = sql + " AND ";
            }
            sql = sql + " prozesse.sortHelperStatus <> '100000000' ";
        }
        if (!showArchivedProjects) {
            if (!sql.isEmpty()) {
                sql = sql + " AND ";
            }
            sql = sql + " projekte.projectIsArchived = false ";
        }
        sb.append(" WHERE ");
        sb.append(sql);

        if (order != null && !order.isEmpty()) {
            sb.append(" ORDER BY " + order);
        }
        List list = ProcessManager.runSQL(sb.toString());

        for (int i = 0; i < list.size(); i++) {
            // get metadata for each id
            Object[] o = (Object[]) list.get(i);
            String s = (String) o[0];

            String metadataQuery = "select name, print from metadata where processid = " + s;
            List<Object[]> metadataList = ProcessManager.runSQL(metadataQuery);

            List<String> additionalColumns = new ArrayList<>();
            for (SearchColumn sc : columnList) {
                if (sc.getTableName().startsWith(METADATA)) {
                    String value = "";
                    for (Object[] metadataRow : metadataList) {
                        String metadataName = (String) metadataRow[0];
                        String metadataValue = (String) metadataRow[1];
                        if ((METADATA + "." + metadataName).equalsIgnoreCase(sc.getValue())) {

                            value = metadataValue;
                            break;
                        }
                    }
                    additionalColumns.add(value);
                }
            }

            Object[] currentEntry = (Object[]) list.get(i);
            List<Object> values = Arrays.asList(currentEntry);
            List newList = new ArrayList<>();
            newList.addAll(values);
            newList.addAll(additionalColumns);
            newList.remove(0);
            if (StringUtils.isNotBlank(order)) {
                newList.remove(0);
            }
            list.set(i, newList.toArray());
        }
        return list;
    }
}
