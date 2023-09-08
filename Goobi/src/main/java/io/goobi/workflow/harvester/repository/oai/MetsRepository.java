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
 */
package io.goobi.workflow.harvester.repository.oai;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import de.sub.goobi.config.ConfigHarvester;
import de.sub.goobi.helper.exceptions.HarvestException;
import io.goobi.workflow.harvester.beans.Record;
import io.goobi.workflow.harvester.export.ExportOutcome;
import io.goobi.workflow.harvester.export.ExportOutcome.ExportOutcomeStatus;
import io.goobi.workflow.harvester.export.IConverter.ExportMode;
import lombok.extern.log4j.Log4j2;

/**
 * This is implementation of {@link OAIDublinCoreRepository} for default OAI Repository.
 * 
 * @author Igor Toker
 * 
 */
@Log4j2
public class MetsRepository extends OAIDublinCoreRepository {

    public static final String TYPE = "METS";

    public MetsRepository(String id, String name, String url, String exportFolder, String scriptPath, Timestamp lastHarvest, int frequency, int delay,
            boolean enabled) {
        super(id, name, url, exportFolder, scriptPath, lastHarvest, frequency, delay, enabled);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public ExportOutcome exportRecord(Record rec, ExportMode mode) {
        ExportOutcome outcome = new ExportOutcome();
        String url = getUrl().trim();
        String query;

        if (url.contains("?")) {
            url = url.substring(0, url.indexOf("?"));
        }
        query = url + "?verb=GetRecord&metadataPrefix=mets&identifier=" + rec.getIdentifier();

        File oaiFile = null;
        File metsFile = null;
        try {
            oaiFile = queryOAItoFile(query);

            // Extract the METS part of the XML file
            Document docOai = new SAXBuilder().build(oaiFile);

            List<Namespace> namespaces = new ArrayList<>();

            namespaces.add(Namespace.getNamespace("mets", "http://www.loc.gov/METS/"));
            namespaces.add(Namespace.getNamespace("dv", "http://dfg-viewer.de/"));
            namespaces.add(Namespace.getNamespace("xlink", "http://www.w3.org/1999/xlink"));
            namespaces.add(Namespace.getNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance"));
            namespaces.add(Namespace.getNamespace("mods", "http://www.loc.gov/mods/v3"));

            XPathFactory xFactory = XPathFactory.instance();

            XPathExpression<Element> xp = xFactory.compile("//mets:mets", Filters.element(), null, namespaces);
            Element eleMetsRoot = xp.evaluateFirst(docOai);
            if (eleMetsRoot == null) {
                log.error("No valid METS document found.");
                outcome.status = ExportOutcomeStatus.ERROR;
                outcome.message = "No valid METS document found";
                return outcome;
            }

            Element eleNewMetsRoot = eleMetsRoot.clone();
            for (Namespace namespace : namespaces) {
                eleNewMetsRoot.addNamespaceDeclaration(namespace);
            }
            Document docMets = new Document();

            docMets.setRootElement(eleNewMetsRoot);

            // Generate ATSTSL

            String fileName = oaiFile.getName();
            if (fileName.contains("/")) {
                fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
            }
            if (!fileName.endsWith(".xml")) {
                fileName = fileName + ".xml";
            }
            log.trace("Export mode: {}", mode);
            switch (mode) {
                case VIEWER:
                    metsFile = new File(ConfigHarvester.getInstance().getViewerHotfolder() + File.separator + fileName);
                    break;
                case GOOBI:
                case GOOBIPROCESS:
                    List<String> goobiHotfolders = ConfigHarvester.getInstance().getGoobiHotfolders();
                    if (goobiHotfolders != null && !goobiHotfolders.isEmpty()) {
                        metsFile = new File(ConfigHarvester.getInstance().getGoobiHotfolders() + File.separator + fileName);
                    } else {
                        log.error("No Goobi hotfolder configured, cannot continue");
                        outcome.status = ExportOutcomeStatus.ERROR;
                        outcome.message = "No Goobi hotfolder configured, cannot continue";
                        return outcome;
                    }
                    break;
                case FOLDER:
                    if (exportFolderPath != null) {
                        File exportFolder = checkAndCreateDownloadFolder(ConfigHarvester.getInstance().getExportFolder());
                        metsFile = new File(exportFolder, fileName);
                    } else {
                        log.error("No export folder configured, cannot continue");
                        outcome.status = ExportOutcomeStatus.ERROR;
                        outcome.message = "No export folder configured, cannot continue";
                        return outcome;
                    }
                    break;
                default:
                    log.error("Unknown export mode: {}", mode);
                    outcome.status = ExportOutcomeStatus.ERROR;
                    outcome.message = "Unknown export mode: " + mode;
                    return outcome;
            }

            try (FileWriter writer = new FileWriter(metsFile);) {
                new XMLOutputter().output(docMets, writer);
            }
        } catch (HarvestException | JDOMException | IOException e) {
            outcome.status = ExportOutcomeStatus.ERROR;
            outcome.message = e.getMessage();
            log.error(e.getMessage(), e);
        } finally {
            if (oaiFile != null) {
                if (!oaiFile.delete()) {
                    log.warn("Could not delete temporary file '" + oaiFile.getAbsolutePath() + "'!");
                }
            }
        }

        return outcome;
    }

    @Override
    public boolean isAutoExport() {
        return true;
    }
}
