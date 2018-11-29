package de.intranda.goobi.plugins;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.geonames.Style;
import org.geonames.Toponym;
import org.geonames.ToponymSearchCriteria;
import org.geonames.ToponymSearchResult;
import org.geonames.WebService;
import org.goobi.production.plugin.interfaces.AbstractMetadataPlugin;
import org.goobi.production.plugin.interfaces.IMetadataPlugin;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.Helper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.log4j.Log4j;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
@EqualsAndHashCode(callSuper = false)
@Log4j
public @Data class GeonamesPlugin extends AbstractMetadataPlugin implements IMetadataPlugin {

    private static final String TITLE = "GeonamesPlugin";

    private String searchValue;
    private List<Toponym> resultList;
    private int totalResults;

    private Toponym currentToponym;
    private boolean showNotHits = false;

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public String getPagePath() {
        return "/uii/includes/metseditor/extension/formMetsGeonamesInput.xhtml";
    }

    public String search() {
        String credentials = ConfigurationHelper.getInstance().getGeonamesCredentials();
        if (credentials != null) {
            WebService.setUserName(credentials);
            ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria();
            searchCriteria.setNameEquals(searchValue);
            searchCriteria.setStyle(Style.FULL);
            try {
                ToponymSearchResult searchResult = WebService.search(searchCriteria);
                resultList = searchResult.getToponyms();
                totalResults = searchResult.getTotalResultsCount();
            } catch (Exception e) {
                log.error(e);
            }

            if (totalResults == 0) {
                showNotHits = true;
            } else {
                showNotHits = false;
            }
        } else {
            // deaktiviert 
            Helper.setFehlerMeldung("geonamesList", "Missing data", "mets_geoname_account_inactive");
        }
        return "";
    }

    public String getData() {
        metadata.setValue(currentToponym.getName());
        metadata.setAutorityFile("geonames", "http://www.geonames.org/", "" + currentToponym.getGeoNameId());
        resultList = new ArrayList<>();
        return "";
    }

    public String getUrl() {

        if (StringUtils.isBlank(metadata.getAuthorityValue())) {
            return null;
        } else {
            return metadata.getAuthorityURI() + metadata.getAuthorityValue();
        }
    }
    
    
    public boolean isShowNoHitFound() {
        return showNotHits;
    }

}
