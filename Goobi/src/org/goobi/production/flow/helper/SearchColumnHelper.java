package org.goobi.production.flow.helper;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.MetadataManager;
import de.sub.goobi.persistence.managers.PropertyManager;

public class SearchColumnHelper {

    public static final String COLUMN_NAME_TITLE = "title";
    public static final String COLUMN_NAME_ID = "ID";
    public static final String COLUMN_NAME_DATE = "Datum";
    public static final String COLUMN_NAME_COUNT_IMAGES = "CountImages";
    public static final String COLUMN_NAME_COUNT_METADATA = "CountMetadata";
    public static final String COLUMN_NAME_PROJECT = "Project";
    public static final String COLUMN_NAME_STATUS = "Status";
    public static final String COLUMN_NAME_ALTREFNO = "AltRefNo";
    public static final String COLUMN_NAME_BNUMBER = "b-number";

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
}
