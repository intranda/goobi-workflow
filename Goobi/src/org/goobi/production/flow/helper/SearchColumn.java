package org.goobi.production.flow.helper;

public class SearchColumn {

    private String value = "";

    private int order;

    public SearchColumn(int order) {
        this.order = order;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getTableName() {
        if (value == null || value.isEmpty()) {
            return "";
        } else if (value.startsWith("prozesse.")) {
            return "prozesse" + order;
        } else if (value.startsWith("projekte.")) {
            return "projekte" + order;
        } else if (value.startsWith("prozesseeigenschaften.")) {
            return "prozesseeigenschaften" + order;
        } else if (value.startsWith("vorlageneigenschaften.")) {
            return "vorlageneigenschaften" + order;
        } else if (value.startsWith("werkstueckeeigenschaften.")) {
            return "werkstueckeeigenschaften" + order;
        } else if (value.startsWith("metadata.")) {
            return "metadata" + order;
        }
        return "";
    }

    public String getColumnName() {
        if (value == null || value.isEmpty() || !value.contains(".")) {
            return "";
        } else {
            return value.substring(value.indexOf(".") + 1);
        }
    }
}
