package de.sub.goobi.helper.tasks;

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
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import org.goobi.beans.Process;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.ProcessManager;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ProcessSwapOutTask extends LongRunningTask {

    @Override
    public void initialize(Process inProzess) {
        super.initialize(inProzess);
        setTitle("Auslagerung: " + inProzess.getTitel());
    }

    /**
     * Aufruf als Thread ================================================================
     */
    @SuppressWarnings("deprecation")
    @Override
    public void run() {
        setStatusProgress(5);
        Helper help = new Helper();
        String swapPath = null;
        String processDirectory = "";

        if (ConfigurationHelper.getInstance().isUseSwapping()) {
            swapPath = ConfigurationHelper.getInstance().getSwapPath();
        } else {
            setStatusMessage("swapping not activated");
            setStatusProgress(-1);
            return;
        }
        if (swapPath == null || swapPath.length() == 0) {
            setStatusMessage("no swappingPath defined");
            setStatusProgress(-1);
            return;
        }
        Path swapFile = Paths.get(swapPath);
        if (!StorageProvider.getInstance().isFileExists(swapFile)) {
            setStatusMessage("Swap folder does not exist or is not mounted");
            setStatusProgress(-1);
            return;
        }
        try {
            processDirectory = getProzess().getProcessDataDirectoryIgnoreSwapping();
            //TODO: Don't catch Exception (the super class)
        } catch (Exception e) {
            log.warn("Exception:", e);
            setStatusMessage("Error while getting process data folder: " + e.getClass().getName() + " - " + e.getMessage());
            setStatusProgress(-1);
            return;
        }

        Path fileIn = Paths.get(processDirectory);
        Path fileOut = Paths.get(swapPath + getProzess().getId() + FileSystems.getDefault().getSeparator());
        if (StorageProvider.getInstance().isFileExists(fileOut)) {
            setStatusMessage(getProzess().getTitel() + ": swappingOutTarget already exists");
            setStatusProgress(-1);
            return;
        }
        try {
            StorageProvider.getInstance().createDirectories(fileOut);
        } catch (IOException e1) {
            log.error(e1);
        }

        /* ---------------------
         * Xml-Datei vorbereiten
         * -------------------*/
        Document doc = new Document();
        Element root = new Element("goobiArchive");
        doc.setRootElement(root);
        Element source = new Element("source").setText(fileIn.toString());
        Element target = new Element("target").setText(fileOut.toString());
        Element title = new Element("title").setText(getProzess().getTitel());
        Element mydate = new Element("date").setText(new Date().toString());
        root.addContent(source);
        root.addContent(target);
        root.addContent(title);
        root.addContent(mydate);

        /* ---------------------
         * Verzeichnisse und Dateien kopieren und anschliessend den Ordner leeren
         * -------------------*/
        setStatusProgress(50);
        try {
            setStatusMessage("copying process folder");
            Helper.copyDirectoryWithCrc32Check(fileIn, fileOut, help.getGoobiDataDirectory().length(), root);
        } catch (IOException e) {
            log.warn("IOException:", e);
            setStatusMessage("IOException in copyDirectory: " + e.getMessage());
            setStatusProgress(-1);
            return;
        }
        setStatusProgress(80);
        StorageProvider.getInstance().deleteDataInDir(fileIn);

        /* ---------------------
         * xml-Datei schreiben
         * -------------------*/
        Format format = Format.getPrettyFormat();
        format.setEncoding("UTF-8");
        try {
            setStatusMessage("writing swapped.xml");
            XMLOutputter xmlOut = new XMLOutputter(format);
            FileOutputStream fos = new FileOutputStream(processDirectory + FileSystems.getDefault().getSeparator() + "swapped.xml");
            xmlOut.output(doc, fos);
            fos.close();
            //TODO: Don't catch Exception (the super class)
        } catch (Exception e) {
            log.warn("Exception:", e);
            setStatusMessage(e.getClass().getName() + " in xmlOut.output: " + e.getMessage());
            setStatusProgress(-1);
            return;
        }
        setStatusProgress(90);

        /* in Prozess speichern */
        try {
            setStatusMessage("saving process");
            Process myProzess = ProcessManager.getProcessById(getProzess().getId());
            myProzess.setSwappedOutGui(true);
            ProcessManager.saveProcess(myProzess);
        } catch (DAOException e) {
            setStatusMessage("DAOException while saving process: " + e.getMessage());
            log.warn("DAOException:", e);
            setStatusProgress(-1);
            return;
        }
        setStatusMessage("done");
        setStatusProgress(100);
    }

}
