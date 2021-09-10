package de.sub.goobi.metadaten;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.goobi.beans.AltoChange;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

public class AltoSaver {
    private static Namespace altoNamespace = Namespace.getNamespace("alto", "http://www.loc.gov/standards/alto/ns-v2#");
    private static SAXBuilder sax = new SAXBuilder();
    private static XPathFactory xFactory = XPathFactory.instance();

    private static final Map<String, Object> wordXpathVariables = new HashMap<>();
    static {
        wordXpathVariables.put("wordid", "String_12290");
    }

    public static void saveAltoChanges(Path altoFile, AltoChange[] changes) throws JDOMException, IOException {
        if (changes == null || changes.length == 0) {
            return;
        }
        Document doc = sax.build(altoFile.toFile());
        for (AltoChange change : changes) {
            String xpath = String.format("//alto:String[@ID='%s']", change.getWordId());
            XPathExpression<Element> compXpath = xFactory.compile(xpath, Filters.element(), wordXpathVariables, altoNamespace);
            compXpath.setVariable("wordid", change.getWordId());
            Element stringEl = compXpath.evaluateFirst(doc);
            if (stringEl != null) {
                stringEl.setAttribute("CONTENT", change.getValue());
            }
        }
        try (OutputStream out = Files.newOutputStream(altoFile)) {
            XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
            xmlOut.output(doc, out);
        }
    }
}
