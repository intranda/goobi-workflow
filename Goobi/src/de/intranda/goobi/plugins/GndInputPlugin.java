package de.intranda.goobi.plugins;

import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import org.goobi.production.plugin.interfaces.AbstractMetadataPlugin;
import org.goobi.production.plugin.interfaces.IMetadataPlugin;

import de.intranda.digiverso.normdataimporter.NormDataImporter;
import de.intranda.digiverso.normdataimporter.model.NormData;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@PluginImplementation
@EqualsAndHashCode(callSuper = false)
public @Data class GndInputPlugin extends AbstractMetadataPlugin implements IMetadataPlugin {

    private List<List<NormData>> dataList;

    private List<NormData> currentData;
    private boolean showNotHits = false;



    @Override
    public String getTitle() {
        return "GndInputPlugin";
    }

    @Override
    public String getPagePath() {
        return "/uii/includes/metseditor/extension/formMetsGndInput.xhtml";
    }

    private String searchValue;
    private String searchOption;

    @Override
    public String search() {
        String val = "";
        if (searchOption.isEmpty()) {
            val = "dnb.nid=" + searchValue;
        } else {
            val = searchValue + " and BBG=" + searchOption;
        }
        URL url = convertToURLEscapingIllegalCharacters("http://normdata.intranda.com/normdata/gnd/woe/" + val);
        String string =
                url.toString().replace("Ä", "%C3%84").replace("Ö", "%C3%96").replace("Ü", "%C3%9C").replace("ä", "%C3%A4").replace("ö", "%C3%B6")
                .replace("ü", "%C3%BC").replace("ß", "%C3%9F");
        dataList = NormDataImporter.importNormDataList(string, 3);

        if (dataList == null || dataList.isEmpty()) {
            showNotHits = true;
        } else {
            showNotHits = false;
        }

        return "";
    }

    private URL convertToURLEscapingIllegalCharacters(String string) {
        try {
            String decodedURL = URLDecoder.decode(string, "UTF-8");
            URL url = new URL(decodedURL);
            URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
            return uri.toURL();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public String getData() {

        for (NormData normdata : currentData) {
            if (normdata.getKey().equals("NORM_IDENTIFIER")) {
                metadata.setAutorityFile("gnd", "http://d-nb.info/gnd/", normdata.getValues().get(0).getText());
            } else if (normdata.getKey().equals("NORM_NAME")) {
                String value = normdata.getValues().get(0).getText();
                metadata.setValue(filter(value));
            }
        }
        dataList = new ArrayList<>();
        return "";
    }


    @Override
    public boolean isShowNoHitFound() {
        return showNotHits;
    }

}
