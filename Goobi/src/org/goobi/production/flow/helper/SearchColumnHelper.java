package org.goobi.production.flow.helper;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.goobi.production.flow.statistics.hibernate.FilterHelper;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.MetadataManager;
import de.sub.goobi.persistence.managers.PropertyManager;

public class SearchColumnHelper {

    private List<SelectItem> possibleColumns = new ArrayList<SelectItem>();

    public List<SelectItem> getPossibleColumns() {
        return possibleColumns;
    }

    public SearchColumnHelper() {
        List<String> columnWhiteList = ConfigurationHelper.getInstance().getDownloadColumnWhitelist();

        SelectItem processData = new SelectItem("processData", Helper.getTranslation("processData"), Helper.getTranslation("processData"), true);
        // static data
        possibleColumns.add(processData);

        {
            SelectItem item = new SelectItem("prozesse.Titel", Helper.getTranslation("title"));
            possibleColumns.add(item);
        }
        {
            SelectItem item = new SelectItem("prozesse.ProzesseID", Helper.getTranslation("ID"));
            possibleColumns.add(item);
        }
        {
            SelectItem item = new SelectItem("prozesse.erstellungsdatum", Helper.getTranslation("Datum"));
            possibleColumns.add(item);
        }
        {
            SelectItem item = new SelectItem("prozesse.sortHelperImages", Helper.getTranslation("CountImages"));
            possibleColumns.add(item);
        }
        {
            SelectItem item = new SelectItem("prozesse.sortHelperMetadata", Helper.getTranslation("CountMetadata"));
            possibleColumns.add(item);
        }
        {
            SelectItem item = new SelectItem("projekte.Titel", Helper.getTranslation("Project"));
            possibleColumns.add(item);
        }

        if (columnWhiteList == null || columnWhiteList.isEmpty()) {
            return;
        }

        // data from configuration

        List<String> processTitles = PropertyManager.getDistinctProcessPropertyTitles();
        if (!processTitles.isEmpty()) {

            for (String title : processTitles) {
                if (columnWhiteList.contains(title)) {
                    SelectItem item = new SelectItem("prozesseeigenschaften." + title, Helper.getTranslation(title));
                    possibleColumns.add(item);
                }
            }
        }

        List<String> templateTitles = PropertyManager.getDistinctTemplatePropertyTitles();
        if (!templateTitles.isEmpty()) {
            List<SelectItem> subList = new ArrayList<>();

            for (String title : templateTitles) {
                if (columnWhiteList.contains(title)) {
                    SelectItem item = new SelectItem("vorlageneigenschaften." + title, Helper.getTranslation(title));
                    subList.add(item);
                }
            }
            if (!subList.isEmpty()) {
                SelectItem templateData =
                        new SelectItem("templateData", Helper.getTranslation("templateData"), Helper.getTranslation("templateData"), true);
                possibleColumns.add(templateData);
                possibleColumns.addAll(subList);
            }
        }

        List<String> masterpiecePropertyTitles = PropertyManager.getDistinctMasterpiecePropertyTitles();
        if (!masterpiecePropertyTitles.isEmpty()) {
            List<SelectItem> subList = new ArrayList<>();

            for (String title : masterpiecePropertyTitles) {
                if (columnWhiteList.contains(title)) {
                    SelectItem item = new SelectItem("werkstueckeeigenschaften." + title, Helper.getTranslation(title));
                    subList.add(item);
                }
            }
            if (!subList.isEmpty()) {
                SelectItem masterpieceData =
                        new SelectItem("masterpieceData", Helper.getTranslation("masterpieceData"), Helper.getTranslation("masterpieceData"), true);
                possibleColumns.add(masterpieceData);
                possibleColumns.addAll(subList);
            }
        }

        List<String> metadataTitles = MetadataManager.getDistinctMetadataNames();
        if (!metadataTitles.isEmpty()) {
            List<SelectItem> subList = new ArrayList<>();

            for (String title : metadataTitles) {
                if (columnWhiteList.contains(title)) {
                    SelectItem item = new SelectItem("metadata." + title, Helper.getTranslation(title));
                    subList.add(item);
                }
            }

            if (!subList.isEmpty()) {
                SelectItem metadataData =
                        new SelectItem("metadataData", Helper.getTranslation("metadataData"), Helper.getTranslation("metadataData"), true);
                possibleColumns.add(metadataData);
                possibleColumns.addAll(subList);
            }
        }
    }

    public HSSFWorkbook getResult(List<SearchColumn> columnList, String filter, boolean showClosedProcesses, boolean showArchivedProjects) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ");

        // add column labels to query
        for (SearchColumn sc : columnList) {
            sb.append(sc.getTableName() + "." + sc.getColumnName() + ", ");
        }
        int length = sb.length();
        sb = sb.replace(length - 2, length, "");

        sb.append(" FROM prozesse ");

        // add table names
        for (SearchColumn sc : columnList) {
            String table = sc.getTableType();
            if (!table.isEmpty()) {
                sb.append(", ");
                sb.append(table);
                sb.append(" ");
                sb.append(sc.getTableName());
                String additional = sc.getAdditionalTable();
                if (!additional.isEmpty()) {
                    sb.append(", ");
                    sb.append(additional);
                    sb.append(" ");
                }
            }
        }

        sb.append(" WHERE ");
        // add 

        for (SearchColumn sc : columnList) {
            String clause = sc.getWhereClause();
            if (!clause.isEmpty()) {
                sb.append(clause + " AND ");
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
            sql = sql + " prozesse.ProjekteID not in (select ProjekteID from projekte where projectIsArchived = true) ";
        }
        return null;
    }
}
