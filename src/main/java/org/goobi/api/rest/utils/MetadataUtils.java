/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *             - https://goobi.io
 *             - https://www.intranda.com
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
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
import de.sub.goobi.helper.XmlTools;
import lombok.extern.log4j.Log4j2;

@Log4j2
public final class MetadataUtils {
    private MetadataUtils() {
        // hide implicit public constructor
    }

    private static final Namespace GOOBI_NS = Namespace.getNamespace("goobi", "http://meta.goobi.org/v1.5.1/");
    private static final Namespace METS_NS = Namespace.getNamespace("mets", "http://www.loc.gov/METS/");
    private static final Namespace MODS_NS = Namespace.getNamespace("mods", "http://www.loc.gov/mods/v3");
    private static final XPathFactory XFACTORY = XPathFactory.instance();
    private static final XPathExpression<Element> AUTHORITY_DATA_XPATH =
            XFACTORY.compile("/mets:mets/mets:dmdSec[1]/mets:mdWrap/mets:xmlData/mods:mods/mods:extension/goobi:goobi/goobi:metadata",
                    Filters.element(), null, MODS_NS, METS_NS, GOOBI_NS);
    private static final XPathExpression<Element> METADATDA_TYPE_XPATH = XFACTORY.compile("//MetadataType", Filters.element());
    private static final SAXBuilder BUILDER = XmlTools.getSAXBuilder();

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
            doc = BUILDER.build(in);
        } catch (JDOMException | IOException e) {
            log.error(e);
            return null;
        }
        for (Element el : METADATDA_TYPE_XPATH.evaluate(doc)) {
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
            doc = BUILDER.build(in);
        } catch (JDOMException | IOException e) {
            log.error(e);
            return;
        }

        for (Element el : AUTHORITY_DATA_XPATH.evaluate(doc)) {
            RestMetadata meta = new RestMetadata();
            String name = el.getAttributeValue("name");

            if (req.getWantedFields() != null && !req.getWantedFields().contains(name)) {
                continue;
            }
            String type = el.getAttributeValue("type");
            if ("person".equals(type)) {
                meta.setValue(el.getChildText("displayName", GOOBI_NS));
            } else {
                meta.setValue(el.getText());
            }
            meta.setAuthorityID(el.getChildText("authorityID", GOOBI_NS));
            meta.setAuthorityURI(el.getChildText("authorityURI", GOOBI_NS));
            meta.setAuthorityValue(el.getChildText("authorityValue", GOOBI_NS));
            meta.setLabels(labelMap.get(name));
            p.addMetadata(name, meta);
        }
    }
}
