package de.sub.goobi.metadaten;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

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
    private static SAXBuilder sax = new SAXBuilder();
    private static XPathFactory xFactory = XPathFactory.instance();

    public static void saveAltoChanges(Path altoFile, AltoChange[] changes) throws JDOMException, IOException {
        if (changes == null || changes.length == 0) {
            return;
        }
        Document doc = sax.build(altoFile.toFile());
        Namespace namespace = Namespace.getNamespace("alto", doc.getRootElement().getNamespaceURI());

        for (AltoChange change : changes) {
            String xpath = String.format("//alto:String[@ID='%s']", change.getWordId());
            XPathExpression<Element> compXpath = xFactory.compile(xpath, Filters.element(), null, namespace);
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
