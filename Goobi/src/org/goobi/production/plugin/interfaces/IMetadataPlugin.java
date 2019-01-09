package org.goobi.production.plugin.interfaces;

import java.util.List;

import javax.faces.model.SelectItem;

import org.geonames.Toponym;

import de.intranda.digiverso.normdataimporter.model.NormData;
import de.sub.goobi.metadaten.Metadaten;
import ugh.dl.Metadata;

public interface IMetadataPlugin extends IPlugin {

    public void setMetadata(Metadata metadata);

    public Metadata getMetadata();

    public String getPagePath();

    public String copy();

    public String delete();

    public void setBean(Metadaten bean);

    public void setDefaultValue(String defaultValue);

    public void setPossibleItems(List<SelectItem> items);

    public void setDefaultItems(List<String> selectedItems);

    @Override
    public String getTitle();

    public String search();

    public String getData();

    public String getUrl();

    public List<List<NormData>> getDataList();

    public  List<NormData> getCurrentData();

    public String getSearchValue();

    public String getSearchOption();

    public void setSelectedItems(List<String> selectedItems);

    public List<String> getSelectedItems();

    public void setSelectedItem(String selectedItem);

    public String getSelectedItem() ;

    public List<Toponym> getResultList();

    public int getTotalResults();

    public void setSearchValue(String value);

    public void setSearchOption(String value);

    public boolean isDisableMetadataField();

    public boolean isDisableIdentifierField();

    public void clearResults();

    /**
     * sets the source or vocabulary
     */

    public void setSource(String source);

    /**
     * define the main field to import as metadata value
     */
    public void setField(String field);

}
