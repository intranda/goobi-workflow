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
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;

import org.goobi.beans.Process;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.XmlTools;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.ProcessManager;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ProcessSwapInTask extends LongRunningTask {

    @Override
    public void initialize(org.goobi.beans.Process inProzess) {
        super.initialize(inProzess);
        setTitle("Einlagerung: " + inProzess.getTitel());
    }

    /**
     * Aufruf als Thread ================================================================
     */
    @SuppressWarnings("deprecation")
    @Override
    public void run() {
        setStatusProgress(5);
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
            // TODO: Don't catch Exception (the super class)
        } catch (Exception e) {
            log.warn("Exception:", e);
            setStatusMessage("Error while getting process data folder: " + e.getClass().getName() + " - " + e.getMessage());
            setStatusProgress(-1);
            return;
        }

        Path fileIn = Paths.get(processDirectory);
        Path fileOut = Paths.get(swapPath + getProzess().getId() + FileSystems.getDefault().getSeparator());

        if (!StorageProvider.getInstance().isFileExists(fileOut)) {
            setStatusMessage(getProzess().getTitel() + ": swappingOutTarget does not exist");
            setStatusProgress(-1);
            return;
        }
        if (!StorageProvider.getInstance().isFileExists(fileIn)) {
            setStatusMessage(getProzess().getTitel() + ": process data folder does not exist");
            setStatusProgress(-1);
            return;
        }

        SAXBuilder builder = XmlTools.getSAXBuilder();
        Document docOld;
        try {
            Path swapLogFile = Paths.get(processDirectory, "swapped.xml");
            docOld = builder.build(swapLogFile.toFile());
            // TODO: Don't catch Exception (the super class)
        } catch (Exception e) {
            log.warn("Exception:", e);
            setStatusMessage("Error while reading swapped.xml in process data folder: " + e.getClass().getName() + " - " + e.getMessage());
            setStatusProgress(-1);
            return;
        }

        /*
         * --------------------- alte Checksummen in HashMap schreiben -------------------
         */
        setStatusMessage("reading checksums");
        Element rootOld = docOld.getRootElement();

        HashMap<String, String> crcMap = new HashMap<>();

        // TODO: Don't use Iterators
        for (Iterator<Element> it = rootOld.getChildren("file").iterator(); it.hasNext();) {
            Element el = it.next();
            crcMap.put(el.getAttribute("path").getValue(), el.getAttribute("crc32").getValue());
        }
        StorageProvider.getInstance().deleteDataInDir(fileIn);

        /*
         * --------------------- Dateien kopieren und Checksummen ermitteln -------------------
         */
        Document doc = new Document();
        Element root = new Element("goobiArchive");
        doc.setRootElement(root);

        /*
         * --------------------- Verzeichnisse und Dateien kopieren und anschliessend den Ordner leeren -------------------
         */
        setStatusProgress(50);
        try {
            setStatusMessage("copying process files");
            Helper.copyDirectoryWithCrc32Check(fileOut, fileIn, swapPath.length(), root);
        } catch (IOException e) {
            log.warn("IOException:", e);
            setStatusMessage("IOException in copyDirectory: " + e.getMessage());
            setStatusProgress(-1);
            return;
        }
        setStatusProgress(80);

        /*
         * --------------------- Checksummen vergleichen -------------------
         */
        setStatusMessage("checking checksums");
        // TODO: Don't use Iterators
        for (Iterator<Element> it = root.getChildren("file").iterator(); it.hasNext();) {
            Element el = it.next();
            String newPath = el.getAttribute("path").getValue();
            String newCrc = el.getAttribute("crc32").getValue();
            if (crcMap.containsKey(newPath)) {
                if (!crcMap.get(newPath).equals(newCrc)) {
                    setLongMessage(getLongMessage() + "File " + newPath + " has different checksum<br/>");
                }
                crcMap.remove(newPath);
            }
        }

        setStatusProgress(85);
        /*
         * --------------------- prüfen, ob noch Dateien fehlen -------------------
         */
        setStatusMessage("checking missing files");
        if (crcMap.size() > 0) {
            for (String myFile : crcMap.keySet()) {
                setLongMessage(getLongMessage() + "File " + myFile + " is missing<br/>");
            }
        }

        setStatusProgress(90);

        /* in Prozess speichern */
        StorageProvider.getInstance().deleteDir(fileOut);
        try {
            setStatusMessage("saving process");
            Process myProzess = ProcessManager.getProcessById(getProzess().getId());
            myProzess.setSwappedOutGui(false);
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
