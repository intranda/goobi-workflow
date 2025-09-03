package de.unigoettingen.sub.search.opac;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.goobi.production.plugin.interfaces.IOpacPlugin;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.DOMBuilder;
import org.jdom2.output.DOMOutputter;
import org.jdom2.output.XMLOutputter;
import org.w3c.dom.Node;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.StorageProvider;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Getter
@Log4j2
public class ConfigOpacCatalogue {
    @Setter
    private String title = "";
    private String description = "";
    private String address = "";
    private String database = "";
    private String iktlist = "";
    private int port = 80;
    private String cbs;
    private String charset = "iso-8859-1";
    private List<ConfigOpacCatalogueBeautifier> beautifySetList;
    private String opacType;
    private String protocol = "http://";
    private Map<String, String> searchFields;

    @Setter
    private IOpacPlugin opacPlugin;

    public ConfigOpacCatalogue(String title, String desciption, String address, String database, String iktlist, int port,
            List<ConfigOpacCatalogueBeautifier> inBeautifySetList, String opacType, Map<String, String> searchFields) {
        this.title = title;
        this.description = desciption;
        this.address = address;
        this.database = database;
        this.iktlist = iktlist;
        this.port = port;
        this.beautifySetList = inBeautifySetList;
        this.opacType = opacType;
        this.searchFields = searchFields;
    }

    // Constructor that also takes a charset, a quick hack for DPD-81
    public ConfigOpacCatalogue(String title, String desciption, String address, String database, String iktlist, int port, String charset, String cbs,
            List<ConfigOpacCatalogueBeautifier> inBeautifySetList, String opacType, String protocol, Map<String, String> searchFields) {
        // Call the contructor above
        this(title, desciption, address, database, iktlist, port, inBeautifySetList, opacType, searchFields);
        this.charset = charset;
        this.cbs = cbs;
        this.protocol = protocol;
    }

    public Node executeBeautifier(Node myHitlist) {

        Node node = myHitlist;

        /* Ausgabe des Opac-Ergebnissen in Datei */

        /*
         * --------------------- aus dem Dom-Node ein JDom-Object machen -------------------
         */
        Document doc = new DOMBuilder().build(node.getOwnerDocument());

        /*
         * --------------------- Im JDom-Object alle Felder durchlaufen und die notwendigen Ersetzungen vornehmen -------------------
         */
        /* alle Records durchlaufen */
        List<Element> elements = doc.getRootElement().getChildren();
        for (Element el : elements) {
            /* in jedem Record den Beautifier anwenden */
            executeBeautifierForElement(el);
        }

        /*
         * --------------------- aus dem JDom-Object wieder ein Dom-Node machen -------------------
         */
        DOMOutputter doutputter = new DOMOutputter();
        try {
            node = doutputter.output(doc);
            node = node.getFirstChild();
        } catch (JDOMException e) {
            log.error("JDOMException in executeBeautifier(Node)", e);
        }

        /* Ausgabe des überarbeiteten Opac-Ergebnisses */
        if (!"".equals(ConfigurationHelper.getInstance().getDebugFolder())
                && StorageProvider.getInstance().isWritable(Paths.get(ConfigurationHelper.getInstance().getDebugFolder()))) {
            debugMyNode(node, ConfigurationHelper.getInstance().getDebugFolder() + "/opacBeautifyAfter.xml");
        }
        return node;
    }

    /**
     * Beautifier für ein JDom-Object durchführen.
     */

    private void executeBeautifierForElement(Element el) {
        String matchedValue = "";
        for (ConfigOpacCatalogueBeautifier beautifier : this.beautifySetList) {
            Element subfieldToChange = null;
            Element mainFieldToChange = null;
            /* eine Kopie der zu prüfenden Elemente anlegen (damit man darin löschen kann */

            ArrayList<ConfigOpacCatalogueBeautifierElement> prooflist =
                    new ArrayList<>(beautifier.getTagElementsToProof());
            /* von jedem Record jedes Field durchlaufen */
            List<Element> elements = el.getChildren("field");
            boolean foundValue = false;
            for (Element field : elements) {
                String tag = field.getAttributeValue("tag");

                if (beautifier.getTagElementToChange().getTag().equals(tag)) {
                    mainFieldToChange = field;
                }

                /* von jedem Field alle Subfelder durchlaufen */
                List<Element> subelements = field.getChildren("subfield");
                for (Element subfield : subelements) {
                    String subtag = subfield.getAttributeValue("code");
                    String value = subfield.getText();

                    if (beautifier.getTagElementToChange().getTag().equals(tag) && beautifier.getTagElementToChange().getSubtag().equals(subtag)) {
                        subfieldToChange = subfield;
                    }
                    /*
                     * wenn die Werte des Subfeldes in der Liste der zu prüfenden Beutifier-Felder stehen, dieses aus der Liste der Beautifier
                     * entfernen
                     */
                    if (!prooflist.isEmpty()) {
                        for (ConfigOpacCatalogueBeautifierElement cocbe : beautifier.getTagElementsToProof()) {
                            if ("*".equals(cocbe.getValue())) {
                                if (cocbe.getTag().equals(tag) && cocbe.getSubtag().equals(subtag)) {
                                    if (!foundValue) {
                                        matchedValue = value;
                                        foundValue = true;
                                    }
                                    prooflist.remove(cocbe);
                                }
                            } else if (cocbe.getTag().equals(tag) && cocbe.getSubtag().equals(subtag) && value.matches(cocbe.getValue())) {
                                if (!foundValue) {
                                    matchedValue = value;
                                }
                                prooflist.remove(cocbe);
                            }
                        }
                    }
                }
            }
            /*
             * --------------------- wenn in der Kopie der zu prüfenden Elemente keine Elemente mehr enthalten sind, kann der zu ändernde Wert
             * wirklich geändert werden -------------------
             */

            // check main field
            if (prooflist.isEmpty() && mainFieldToChange == null) {
                mainFieldToChange = new Element("field");
                mainFieldToChange.setAttribute("tag", beautifier.getTagElementToChange().getTag());
                elements.add(mainFieldToChange);
            }

            // check subfield
            if (mainFieldToChange != null && prooflist.isEmpty() && subfieldToChange == null) {

                subfieldToChange = new Element("subfield");
                subfieldToChange.setAttribute("code", beautifier.getTagElementToChange().getSubtag());
                mainFieldToChange.addContent(subfieldToChange);
            }

            if (subfieldToChange != null && prooflist.isEmpty()) {
                if ("*".equals(beautifier.getTagElementToChange().getValue())) {
                    subfieldToChange.setText(matchedValue);
                } else {
                    subfieldToChange.setText(beautifier.getTagElementToChange().getValue());
                }
            }

        }

    }

    /**
     * Print given DomNode to defined File ================================================================
     */
    private void debugMyNode(Node inNode, String fileName) {
        try {
            XMLOutputter outputter = new XMLOutputter();
            Document tempDoc = new DOMBuilder().build(inNode.getOwnerDocument());
            FileOutputStream output = new FileOutputStream(fileName);
            outputter.output(tempDoc.getRootElement(), output);
        } catch (IOException e) {
            log.error("debugMyNode(Node, String)", e);
        }

    }

}
