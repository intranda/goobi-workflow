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

package org.goobi.api.sru;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import lombok.extern.log4j.Log4j2;

/**
 * <p>
 * A trivial wrapper for response Objects, allowing access to common information without having to parse continually.
 * </p>
 * 
 * Idea taken from <a href="https://github.com/redbox-mint/mint/tree/master/plugins/sru/sruclient">SRUClient</a>
 * 
 */

@Log4j2
public class SRUResponse {

    /** Record counts **/
    private int totalRecords = 0;
    private int recordsReturned = 0;

    XPathFactory xpfac = XPathFactory.instance();

    /** Results **/
    private List<Element> resultsList;

    private Namespace srw = Namespace.getNamespace("srw", "http://www.loc.gov/zing/srw/");

    /**
     * <p>
     * Default Constructor. Extract some basic information.
     * </p>
     * 
     * @param searchResponse A parsed jdom2 Document
     * @throws SRUException If any of the XML structure does not look like expected
     */

    public SRUResponse(Document searchResponse) throws JDOMException {
        // Results total

        XPathExpression<Element> xp = xpfac.compile("//srw:numberOfRecords", Filters.element(), null, srw);
        Element number = xp.evaluateFirst(searchResponse.getRootElement());

        if (number == null) {
            throw new JDOMException("Unable to get result numbers from response XML.");
        }
        totalRecords = Integer.parseInt(number.getText());
        log.debug("SRU Search found {} results(s)", totalRecords);

        // Results List
        if (totalRecords == 0) {
            resultsList = new ArrayList<>();
        } else {
            xp = xpfac.compile("//srw:recordData", Filters.element(), null, srw);

            resultsList = xp.evaluate(searchResponse);
        }
        recordsReturned = resultsList.size();
    }

    /**
     * <p>
     * Get the number of rows returned by this search. Not the total results that match the search.
     * </p>
     * 
     * @return int The number of rows returned from this search.
     */
    public int getRows() {
        return recordsReturned;
    }

    /**
     * <p>
     * Get the number of records that match this search. A subset of this will be returned if the total is higher then the number of rows requested
     * (or defaulted).
     * </p>
     * 
     * @return int The number of records that match this search.
     */
    public int getTotalResults() {
        return totalRecords;
    }

    /**
     * <p>
     * Return the List of DOM4J Nodes extracted from the SRU XML wrapping it.
     * </p>
     * 
     * @return int The number of records that match this search.
     */
    public List<Element> getResults() {
        return resultsList;
    }
}
