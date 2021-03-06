package de.sub.goobi.metadaten.search;

import de.intranda.digiverso.normdataimporter.model.NormDataRecord;

import java.util.List;

/***
 * A service to import data from external services.
 *
 * @ author Hemed Al Ruwehy
 * 2020-03-03
 */
public interface NormDataImporter {

    /**
     * Imports data from a given endpoint URL and return a list of norm data records. The implementation on how data
     * are imported is left to concrete classes.
     *
     * @param url an endpoint URL to import data from
     * @return a list of records of type NormDataRecord
     * @see NormDataRecord
     */
    List<NormDataRecord> importNormData(String url);
}
