package org.goobi.api.display.enums;

import java.util.Arrays;
import java.util.List;

public enum NormDatabase {

    GND("http://www.dnb.de/gnd", "gnd"),
    REFGEO("http://normdata.intranda.com/normdata/refgeo", "intranda Geo Datenbank"),
    REFBIO("http://normdata.intranda.com/normdata/refbio", "intranda PND");

    private String path;

    private String abbreviation;

    private NormDatabase(String path, String abbreviation) {
        this.path = path;
        this.abbreviation = abbreviation;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public String getPath() {
        return path;
    }

    public static NormDatabase getByAbbreviation(String abbreviation) {
        for (NormDatabase ndb : NormDatabase.values()) {
            if (ndb.getAbbreviation().equals(abbreviation)) {
                return ndb;
            }
        }
        return GND;
    }

    public static List<NormDatabase> getAllDatabases() {
        return Arrays.asList(NormDatabase.values());
    }

}
