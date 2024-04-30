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

package org.goobi.api.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.goobi.beans.Process;
import org.goobi.beans.Project;
import org.goobi.beans.Step;
import org.xml.sax.SAXException;

import de.sub.goobi.helper.ScriptThreadWithoutHibernate;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.ProjectManager;
import de.sub.goobi.persistence.managers.StepManager;
import lombok.extern.log4j.Log4j2;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.DocStructType;
import ugh.dl.Fileformat;
import ugh.dl.Prefs;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.TypeNotAllowedForParentException;
import ugh.exceptions.UGHException;
import ugh.fileformats.mets.XStream;

@Log4j2
public abstract class MetadataService {

    public abstract Fileformat readMetadataFile(InputStream inputStream, Prefs prefs)
            throws ParserConfigurationException, SAXException, IOException, ReadException, PreferencesException, TypeNotAllowedForParentException;

    protected Response createRecord(String projectName, String templateName, String processTitle, InputStream inputStream) {
        Project project = null;
        try {
            project = ProjectManager.getProjectByName(projectName);
        } catch (DAOException e) {
            log.error(e);
            return Response.status(500).entity("Cannot read project").build();
        }

        if (project == null) {
            return Response.status(404).entity("Project not found").build();
        }

        Process template = ProcessManager.getProcessByExactTitle(templateName);
        if (template == null) {
            return Response.status(404).entity("Process template not found").build();
        }
        // generate temporary process title if missing
        if (StringUtils.isBlank(processTitle) || "-".equals(processTitle)) {
            processTitle = String.valueOf(System.currentTimeMillis());
        }

        // check for duplicates
        Process other = ProcessManager.getProcessByExactTitle(processTitle);
        if (other != null) {
            return Response.status(Status.CONFLICT).entity("The process title is already used.").build();
        }

        Process process = ProcessService.prepareProcess(processTitle, template);
        process.setProjekt(project);
        Prefs prefs = process.getRegelsatz().getPreferences();

        try {
            // save process to create id and directories
            ProcessManager.saveProcess(process);
            // save metadata file
            Fileformat fileformat = readMetadataFile(inputStream, prefs);

            DigitalDocument dd = fileformat.getDigitalDocument();
            Fileformat ff = new XStream(prefs);
            ff.setDigitalDocument(dd);
            /* add physical docstruct */
            DocStructType dst = prefs.getDocStrctTypeByName("BoundBook");
            DocStruct dsBoundBook = dd.createDocStruct(dst);
            dd.setPhysicalDocStruct(dsBoundBook);

            if (dd.getLogicalDocStruct() == null || dd.getLogicalDocStruct().getType() == null) {
                log.error("Cannot parse record for {}", processTitle);
            } else {
                process.writeMetadataFile(ff);
            }
            log.debug("Generated process {} using marc upload", processTitle);
        } catch (DAOException | UGHException | ParserConfigurationException | SAXException | IOException | SwapException e) {
            log.error(e);
        }

        List<Step> steps = StepManager.getStepsForProcess(process.getId());
        for (Step s : steps) {
            if (StepStatus.OPEN.equals(s.getBearbeitungsstatusEnum()) && s.isTypAutomatisch()) {
                ScriptThreadWithoutHibernate myThread = new ScriptThreadWithoutHibernate(s);
                myThread.startOrPutToQueue();
            }
        }

        return Response.status(204).build();
    }

    protected Response replaceMetadataInProcess(Integer processid, InputStream inputStream) {

        Process process = ProcessManager.getProcessById(processid);

        if (process == null) {
            return Response.status(404).entity("Process not found").build();
        }
        Prefs prefs = process.getRegelsatz().getPreferences();

        try {
            Fileformat fileformat = readMetadataFile(inputStream, prefs);
            DigitalDocument dd = fileformat.getDigitalDocument();
            Fileformat ff = new XStream(prefs);
            ff.setDigitalDocument(dd);
            /* add physical docstruct */
            DocStructType dst = prefs.getDocStrctTypeByName("BoundBook");
            DocStruct dsBoundBook = dd.createDocStruct(dst);
            dd.setPhysicalDocStruct(dsBoundBook);

            process.writeMetadataFile(ff);
        } catch (ParserConfigurationException | UGHException | SAXException | IOException | SwapException e) {
            log.error(e);
            return Response.status(500).entity("Error during metadata creation").build();
        }

        return Response.status(200).build();
    }
}
