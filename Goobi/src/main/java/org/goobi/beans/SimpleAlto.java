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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.XmlTools;
import lombok.Data;

@Data
public class SimpleAlto {
    private static Namespace altoNamespace = Namespace.getNamespace("alto", "http://www.loc.gov/standards/alto/ns-v2#");
    private static Namespace altov4Namespace = Namespace.getNamespace("alto", "http://www.loc.gov/standards/alto/ns-v4#");
    private static XPathFactory xFactory = XPathFactory.instance();
    private static XPathExpression<Element> linesXpath = xFactory.compile("//alto:TextLine", Filters.element(), null, altoNamespace);
    private static XPathExpression<Element> wordXpath = xFactory.compile("./alto:String", Filters.element(), null, altoNamespace);

    private static XPathExpression<Element> linesV4Xpath = xFactory.compile("//alto:TextLine", Filters.element(), null, altov4Namespace);
    private static XPathExpression<Element> wordV4Xpath = xFactory.compile("./alto:String", Filters.element(), null, altov4Namespace);

    private List<SimpleAltoLine> lines;
    private Map<String, SimpleAltoLine> lineMap = new HashMap<>();
    private Map<String, SimpleAltoWord> wordMap = new HashMap<>();

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

    public static SimpleAlto readAlto(Path altoPath) throws IOException, JDOMException {
        SAXBuilder sax = XmlTools.getSAXBuilder();
        Document doc = null;
        try (InputStream in = StorageProvider.getInstance().newInputStream(altoPath)) {
            doc = sax.build(in);
        }

        SimpleAlto alto = new SimpleAlto();
        alto.lines = new ArrayList<>();

        boolean useV4 = false;
        List<Element> xmlLines = linesXpath.evaluate(doc);
        if (xmlLines.isEmpty()) {
            xmlLines = linesV4Xpath.evaluate(doc);
            useV4 = true;
        }
        for (Element xmlLine : xmlLines) {
            SimpleAltoLine line = new SimpleAltoLine();
            line.setId(xmlLine.getAttributeValue("ID"));

            line.setX((int) Double.parseDouble(xmlLine.getAttributeValue("HPOS", "0")));
            line.setY((int) Double.parseDouble(xmlLine.getAttributeValue("VPOS", "0")));
            line.setWidth((int) Double.parseDouble(xmlLine.getAttributeValue("WIDTH", "0")));
            line.setHeight((int) Double.parseDouble(xmlLine.getAttributeValue("HEIGHT", "0")));

            List<SimpleAltoWord> words = new ArrayList<>();

            List<Element> xmlWords = useV4 ? wordV4Xpath.evaluate(xmlLine) : wordXpath.evaluate(xmlLine);
            for (Element xmlWord : xmlWords) {
                SimpleAltoWord word = new SimpleAltoWord();
                word.setId(xmlWord.getAttributeValue("ID"));
                word.setValue(xmlWord.getAttributeValue("CONTENT"));
                word.setLineId(line.getId());

                word.setX((int) Double.parseDouble(xmlWord.getAttributeValue("HPOS", "0")));
                word.setY((int) Double.parseDouble(xmlWord.getAttributeValue("VPOS", "0")));
                word.setWidth((int) Double.parseDouble(xmlWord.getAttributeValue("WIDTH", "0")));
                word.setHeight((int) Double.parseDouble(xmlWord.getAttributeValue("HEIGHT", "0")));

                words.add(word);
                alto.wordMap.put(word.getId(), word);
            }
            line.setWords(words);

            alto.lines.add(line);
            alto.lineMap.put(line.getId(), line);
        }

        return alto;
    }
}
