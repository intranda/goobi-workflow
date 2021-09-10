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
import lombok.Data;

@Data
public class SimpleAlto {
    private static Namespace altoNamespace = Namespace.getNamespace("alto", "http://www.loc.gov/standards/alto/ns-v2#");
    private static XPathFactory xFactory = XPathFactory.instance();
    private static XPathExpression<Element> linesXpath = xFactory.compile("//alto:TextLine", Filters.element(), null, altoNamespace);
    private static XPathExpression<Element> wordXpath = xFactory.compile("./alto:String", Filters.element(), null, altoNamespace);

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
        List<SimpleAltoWord> words = new ArrayList<SimpleAltoWord>();
        words.add(word);
        SimpleAltoLine line = new SimpleAltoLine();
        line.setWords(words);
        lines = new ArrayList<SimpleAltoLine>();
        lines.add(line);
    }

    public static SimpleAlto readAlto(Path altoPath) throws IOException, JDOMException {
        SAXBuilder sax = new SAXBuilder();
        Document doc = null;
        try (InputStream in = StorageProvider.getInstance().newInputStream(altoPath)) {
            doc = sax.build(in);
        }

        SimpleAlto alto = new SimpleAlto();
        alto.lines = new ArrayList<>();

        List<Element> xmlLines = linesXpath.evaluate(doc);
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
