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
package org.goobi.beans;

import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.XmlTools;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import software.amazon.awssdk.utils.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Log4j2
public class SimpleAlto {
    private static XPathFactory xFactory = XPathFactory.instance();

    private XPathExpression<Element> linesXpath;
    private XPathExpression<Element> wordXpath;
    private XPathExpression<Element> namedEntityXpath;

    private List<SimpleAltoLine> lines = new ArrayList<>();
    private Map<String, SimpleAltoLine> lineMap = new HashMap<>();
    private Map<String, SimpleAltoWord> wordMap = new HashMap<>();

    private List<NamedEntity> namedEntities = new ArrayList<>();
    private Map<String, List<String>> namedEntityMap = new HashMap<>();

    public SimpleAlto() {
        super();
    }

    /**
     * This constructor should only be used to display error messages, it creates a single line with a single word which holds the error message
     * 
     * @param errorMessage
     */
    public SimpleAlto(String errorMessage) {
        SimpleAltoWord word = new SimpleAltoWord();
        word.setValue(errorMessage);
        List<SimpleAltoWord> words = new ArrayList<>();
        words.add(word);
        SimpleAltoLine line = new SimpleAltoLine();
        line.setWords(words);
        lines = new ArrayList<>();
        lines.add(line);
    }

    private void initializePatterns(Document doc) {
        Namespace namespace = doc.getRootElement().getNamespace();
        Namespace prefixNamespace = Namespace.getNamespace("alto", namespace.getURI());
        compilePatternForNamespace(prefixNamespace);
    }

    private void compilePatternForNamespace(Namespace namespace) {
        linesXpath = xFactory.compile("//alto:TextLine", Filters.element(), null, namespace);
        wordXpath = xFactory.compile("./alto:String", Filters.element(), null, namespace);
        namedEntityXpath = xFactory.compile("//alto:NamedEntityTag", Filters.element(), null, namespace);
    }

    public void readAlto(Path altoPath) throws IOException, JDOMException {
        SAXBuilder sax = XmlTools.getSAXBuilder();
        Document doc = null;
        try (InputStream in = StorageProvider.getInstance().newInputStream(altoPath)) {
            doc = sax.build(in);
        }

        initializePatterns(doc);

        List<Element> xmlLines = linesXpath.evaluate(doc);

        createNamedEntities(doc, this);

        for (Element xmlLine : xmlLines) {
            SimpleAltoLine line = new SimpleAltoLine();
            line.setId(xmlLine.getAttributeValue("ID"));

            line.setX((int) Double.parseDouble(xmlLine.getAttributeValue("HPOS", "0")));
            line.setY((int) Double.parseDouble(xmlLine.getAttributeValue("VPOS", "0")));
            line.setWidth((int) Double.parseDouble(xmlLine.getAttributeValue("WIDTH", "0")));
            line.setHeight((int) Double.parseDouble(xmlLine.getAttributeValue("HEIGHT", "0")));

            List<SimpleAltoWord> words = new ArrayList<>();

            List<Element> xmlWords = wordXpath.evaluate(xmlLine);
            for (Element xmlWord : xmlWords) {
                SimpleAltoWord word = new SimpleAltoWord();
                word.setId(xmlWord.getAttributeValue("ID"));
                word.setValue(xmlWord.getAttributeValue("CONTENT"));
                word.setLineId(line.getId());

                word.setX((int) Double.parseDouble(xmlWord.getAttributeValue("HPOS", "0")));
                word.setY((int) Double.parseDouble(xmlWord.getAttributeValue("VPOS", "0")));
                word.setWidth((int) Double.parseDouble(xmlWord.getAttributeValue("WIDTH", "0")));
                word.setHeight((int) Double.parseDouble(xmlWord.getAttributeValue("HEIGHT", "0")));

                parseNamedEntityTagRefs(this, xmlWord);

                words.add(word);
                wordMap.put(word.getId(), word);
            }
            line.setWords(words);

            lines.add(line);
            lineMap.put(line.getId(), line);
        }
    }

    public static void parseNamedEntityTagRefs(SimpleAlto alto, Element xmlWord) {
        String tagrefString = xmlWord.getAttributeValue("TAGREFS", "");
        String[] tagrefs = tagrefString.split("[\\s,;]+");
        for (String tagref : tagrefs) {
            if (StringUtils.isNotBlank(tagref)) {
                List<String> tagWordList = alto.namedEntityMap.getOrDefault((tagref), new ArrayList<>());
                tagWordList.add(xmlWord.getAttributeValue("ID"));
            }
        }
    }

    public void createNamedEntities(Document doc, SimpleAlto alto) {
        List<Element> namedEntityElements = namedEntityXpath.evaluate(doc);
        for (Element entityElement : namedEntityElements) {
            String id = entityElement.getAttributeValue("ID");
            String label = entityElement.getAttributeValue("LABEL");
            String type = entityElement.getAttributeValue("TYPE");
            String uri = entityElement.getAttributeValue("URI");
            if (StringUtils.isNotBlank(id)) {
                NamedEntity entity = new NamedEntity(id, label, type, uri);
                alto.namedEntities.add(entity);
                alto.namedEntityMap.put(id, new ArrayList<>());
            } else {
                log.warn("Found named entity without id. Entity cannot be imported and will be ignored");
            }
        }
    }
}
