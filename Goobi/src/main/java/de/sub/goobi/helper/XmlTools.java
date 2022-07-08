package de.sub.goobi.helper;

import org.jdom2.input.SAXBuilder;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class XmlTools {

    public static SAXBuilder getSAXBuilder() {
        SAXBuilder builder = new SAXBuilder();
        // Disable access to external entities
        builder.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        builder.setFeature("http://xml.org/sax/features/external-general-entities", false);
        builder.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

        return builder;
    }


}
