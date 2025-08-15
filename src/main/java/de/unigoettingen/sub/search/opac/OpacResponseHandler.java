package de.unigoettingen.sub.search.opac;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *          - https://goobi.io
 *          - https://www.intranda.com
 *          - https://github.com/intranda/goobi-workflow
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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import lombok.Getter;

public class OpacResponseHandler extends DefaultHandler {

    private boolean readTitle = false;
    private boolean readSessionVar = false;
    private String sessionVar = "";
    private String title = "";
    private String sessionId = "";
    private String cookie = "";
    @Getter
    private String set = "";
    @Getter
    private int numberOfHits = 0;

    @Getter
    private ArrayList<String> opacResponseItemPpns = new ArrayList<>();
    @Getter
    private ArrayList<String> opacResponseItemTitles = new ArrayList<>();

    public OpacResponseHandler() {
        super();
    }

    /**
     * SAX parser callback method.
     * 
     * @throws SAXException
     */
    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        //Eingefügt cm 8.5.2007
        if ("RESULT".equals(localName) && atts.getValue("error") != null && "ILLEGAL".equalsIgnoreCase(atts.getValue("error"))) {
            throw new SAXException(new IllegalQueryException());
        }

        if ("SESSIONVAR".equals(localName)) {
            this.sessionVar = atts.getValue("name");
            this.readSessionVar = true;
        }

        if ("SET".equals(localName)) {
            this.numberOfHits = Integer.parseInt(atts.getValue("hits"));
        }

        if ("SHORTTITLE".equals(localName)) {
            this.readTitle = true;
            this.title = "";
            this.opacResponseItemPpns.add(atts.getValue("PPN"));
        }
    }

    /**
     * SAX parser callback method.
     */
    @Override
    public void characters(char[] ch, int start, int length) {
        if (this.readTitle) {
            this.title += new String(ch, start, length);
        }

        if (this.readSessionVar) {
            if ("SID".equals(this.sessionVar)) {
                this.sessionId = new String(ch, start, length);
            }
            if ("SET".equals(this.sessionVar)) {
                this.set = new String(ch, start, length);
            }
            if ("COOKIE".equals(this.sessionVar)) {
                this.cookie = new String(ch, start, length);
            }
        }
    }

    /**
     * SAX parser callback method.
     */
    @Override
    public void endElement(String namespaceURI, String localName, String qName) {
        if ("SHORTTITLE".equals(localName)) {
            this.readTitle = false;
            this.opacResponseItemTitles.add(this.title);
        }

        if ("SESSIONVAR".equals(localName)) {
            this.readSessionVar = false;
        }
    }

    public String getSessionId(String encoding) throws UnsupportedEncodingException {
        if (!"".equals(this.cookie)) {
            return URLEncoder.encode(this.sessionId, encoding) + "/COOKIE=" + URLEncoder.encode(this.cookie, encoding);
        }
        return this.sessionId;
    }
}
