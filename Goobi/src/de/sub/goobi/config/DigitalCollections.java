/**
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information. 
 *             - https://goobi.io
 *             - https://www.intranda.com
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package de.sub.goobi.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.apache.commons.lang.StringUtils;
import org.goobi.beans.Process;

public class DigitalCollections {

	public static List<String> possibleDigitalCollectionsForProcess(
			Process process) throws JDOMException, IOException {
		
		List<String> result = new ArrayList<String>();
		String filename = ConfigurationHelper.getInstance().getConfigurationFolder() + "goobi_digitalCollections.xml";
		if (!Files.exists(Paths.get(filename))) {
			throw new FileNotFoundException("File not found: " + filename);
		}
		
		/* Datei einlesen und Root ermitteln */
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(filename);
		Element root = doc.getRootElement();
		/* alle Projekte durchlaufen */
		List<Element> projekte = root.getChildren();
		for (Iterator<Element> iter = projekte.iterator(); iter.hasNext();) {
			Element projekt = iter.next();
			List<Element> projektnamen = projekt.getChildren("name");
			for (Iterator<Element> iterator = projektnamen.iterator(); iterator.hasNext();) {
				Element projektname = iterator.next();

				/*
				 * wenn der Projektname aufgeführt wird, dann alle Digitalen Collectionen in die Liste
				 */
				if (projektname.getText().equalsIgnoreCase(process.getProjekt().getTitel())) {
					List<Element> myCols = projekt.getChildren("DigitalCollection");
					for (Iterator<Element> it2 = myCols.iterator(); it2.hasNext();) {
						Element col = it2.next();
						result.add(col.getText());
					}
				}
			}
		}
		return result;
	}
	
	public static String getDefaultDigitalCollectionForProcess(
			Process process) throws JDOMException, IOException {
		
		String filename = ConfigurationHelper.getInstance().getConfigurationFolder() + "goobi_digitalCollections.xml";
		if (!Files.exists(Paths.get(filename))) {
			throw new FileNotFoundException("File not found: " + filename);
		}
		
		String firstCollection = "";
		
		/* Datei einlesen und Root ermitteln */
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(filename);
		Element root = doc.getRootElement();
		/* alle Projekte durchlaufen */
		List<Element> projekte = root.getChildren();
		for (Iterator<Element> iter = projekte.iterator(); iter.hasNext();) {
			Element projekt = iter.next();
			List<Element> projektnamen = projekt.getChildren("name");
			for (Iterator<Element> iterator = projektnamen.iterator(); iterator.hasNext();) {
				Element projektname = iterator.next();

				/*
				 * wenn der Projektname aufgeführt wird, dann alle Digitalen Collectionen in die Liste
				 */
				if (projektname.getText().equalsIgnoreCase(process.getProjekt().getTitel())) {
					List<Element> myCols = projekt.getChildren("DigitalCollection");
					for (Iterator<Element> it2 = myCols.iterator(); it2.hasNext();) {
						Element col = it2.next();
						String collectionName = col.getText();
						String defaultCollection = col.getAttributeValue("default");
						if(defaultCollection.equalsIgnoreCase("true")) {
							return collectionName;
						}
						if(StringUtils.isBlank(firstCollection)) {
							firstCollection = collectionName;
						}
					}
				}
			}
		}
		return firstCollection;
	}
}
