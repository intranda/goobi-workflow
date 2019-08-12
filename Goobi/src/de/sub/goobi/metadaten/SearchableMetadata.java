package de.sub.goobi.metadaten;

import org.goobi.api.display.enums.DisplayType;

public interface SearchableMetadata {

    /**
     * defines the type of the current metadata field
     * 
     * @return {@link DisplayType}
     */
    public DisplayType getMetadataDisplaytype();

    /**
     * set the default search value, some implementations like viaf use own fields
     * 
     * @param value
     */

    public void setSearchValue(String value);

    /**
     * set the default field to perform the search in. Some implementations like geonames don't need this
     * 
     * @param value
     */

    public void setSearchOption(String value);

    /**
     * performs the search
     * 
     * @return
     */

    public String search();

    /**
     * clear results of older search requests
     */

    public void clearResults();

    /**
     * defines if the search should be performed in gnd or viaf
     */
    public void setSearchInViaf(boolean serachInViaf);

}
