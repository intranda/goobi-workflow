package org.goobi.api.rest.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.goobi.api.rest.model.RestMetadata;
import org.goobi.api.rest.model.RestProcess;
import org.goobi.api.rest.request.SearchRequest;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import de.sub.goobi.config.ConfigurationHelper;
import lombok.extern.log4j.Log4j;

@Log4j
public class MetadataUtils {
    private static final Namespace goobiNamespace = Namespace.getNamespace("goobi", "http://meta.goobi.org/v1.5.1/");
    private static final Namespace mets = Namespace.getNamespace("mets", "http://www.loc.gov/METS/");
    private static final Namespace mods = Namespace.getNamespace("mods", "http://www.loc.gov/mods/v3");
    private static final XPathFactory xFactory = XPathFactory.instance();
    private static final XPathExpression<Element> authorityMetaXpath = xFactory.compile(
            "//mets:xmlData/mods:mods/mods:extension/goobi:goobi/goobi:metadata",
            Filters.element(), null, mods, mets, goobiNamespace);
    private static final XPathExpression<Element> metadataTypeXpath = xFactory.compile("//MetadataType", Filters.element());
    private static final SAXBuilder builder = new SAXBuilder();

    public static void addMetadataToRestProcesses(List<RestProcess> processes, SearchRequest req) {
        String metadataFolder = ConfigurationHelper.getInstance().getMetadataFolder();
        Map<String, Map<String, Map<String, String>>> ruleset2labelMapsMap = new HashMap<>();
        for (RestProcess p : processes) {
            Map<String, Map<String, String>> labelMap = ruleset2labelMapsMap.get(p.getRuleset());
            if (labelMap == null) {
                labelMap = readLabelMap(p.getRuleset());
                ruleset2labelMapsMap.put(p.getRuleset(), labelMap);
            }
            addMetadataToRestProcess(p, metadataFolder, req, labelMap);
        }
    }

    private static Map<String, Map<String, String>> readLabelMap(String ruleset) {
        Map<String, Map<String, String>> metaName2LanguagesMap = new HashMap<>();
        Path rulesetPath = Paths.get(ConfigurationHelper.getInstance().getRulesetFolder(), ruleset);
        Document doc;
        try (InputStream in = Files.newInputStream(rulesetPath)) {
            doc = builder.build(in);
        } catch (JDOMException | IOException e) {
            log.error(e);
            return null;
        }
        for (Element el : metadataTypeXpath.evaluate(doc)) {
            String name = el.getChildText("Name");
            Map<String, String> languages = new HashMap<>();
            for (Element langEl : el.getChildren("language")) {
                languages.put(langEl.getAttributeValue("name"), langEl.getText());
            }
            metaName2LanguagesMap.put(name, languages);
        }
        return metaName2LanguagesMap;
    }

    private static RestProcess addMetadataToRestProcess(RestProcess p, String metadataFolder, SearchRequest req,
            Map<String, Map<String, String>> labelMap) {
        Path metsPath = Paths.get(metadataFolder, Integer.toString(p.getId()), "meta.xml");
        Path anchorPath = metsPath.resolveSibling("meta_anchor.xml");

        extractMetadataFromFile(metsPath, p, labelMap, req);
        extractMetadataFromFile(anchorPath, p, labelMap, req);
        return p;
    }

    private static void extractMetadataFromFile(Path metsPath, RestProcess p, Map<String, Map<String, String>> labelMap, SearchRequest req) {
        if (!Files.exists(metsPath)) {
            return;
        }
        Document doc;
        try (InputStream in = Files.newInputStream(metsPath)) {
            doc = builder.build(in);
        } catch (JDOMException | IOException e) {
            log.error(e);
            return;
        }

        for (Element el : authorityMetaXpath.evaluate(doc)) {
            RestMetadata meta = new RestMetadata();
            String name = el.getAttributeValue("name");

            if (req.getWantedFields() != null && !req.getWantedFields().contains(name)) {
                continue;
            }
            String type = el.getAttributeValue("type");
            if ("person".equals(type)) {
                meta.setValue(el.getChildText("displayName", goobiNamespace));
            } else {
                meta.setValue(el.getText());
            }
            meta.setAuthorityID(el.getChildText("authorityID", goobiNamespace));
            meta.setAuthorityURI(el.getChildText("authorityURI", goobiNamespace));
            meta.setAuthorityValue(el.getChildText("authorityValue", goobiNamespace));
            meta.setLabels(labelMap.get(name));
            p.addMetadata(name, meta);
        }
    }
}
