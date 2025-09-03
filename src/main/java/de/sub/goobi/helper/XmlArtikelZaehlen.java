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
package de.sub.goobi.helper;

import java.io.IOException;

import org.goobi.beans.Process;

import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.managers.ProcessManager;
import lombok.extern.log4j.Log4j2;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.MetadataGroup;
import ugh.dl.Person;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.UGHException;

@Log4j2
public class XmlArtikelZaehlen {

    public enum CountType {
        METADATA,
        DOCSTRUCT;
    }

    /**
     * Anzahl der Strukturelemente ermitteln.
     * 
     * @param myProzess
     * @param inType
     * @return number
     */
    public int getNumberOfUghElements(Process myProzess, CountType inType) {
        int rueckgabe = 0;

        /* --------------------------------
         * Dokument einlesen
         * --------------------------------*/
        Fileformat gdzfile;
        try {
            gdzfile = myProzess.readMetadataFile();
        } catch (UGHException | IOException | SwapException e) {
            Helper.setFehlerMeldung("xml error", e.getMessage());
            return -1;
        }

        /* --------------------------------
         * DocStruct rukursiv durchlaufen
         * --------------------------------*/
        DigitalDocument mydocument = null;
        try {
            mydocument = gdzfile.getDigitalDocument();
            DocStruct logicalTopstruct = mydocument.getLogicalDocStruct();
            rueckgabe += getNumberOfUghElements(logicalTopstruct, inType);
        } catch (PreferencesException e1) {
            Helper.setFehlerMeldung("[" + myProzess.getId() + "] Can not get DigitalDocument: ", e1.getMessage());
            log.error(e1);
            rueckgabe = 0;
        }

        /* --------------------------------
         * die ermittelte Zahl im Prozess speichern
         * --------------------------------*/
        myProzess.setSortHelperArticles(Integer.valueOf(rueckgabe));
        try {
            ProcessManager.saveProcess(myProzess);
        } catch (DAOException e) {
            log.error(e);
        }
        return rueckgabe;
    }

    /**
     * Anzahl der Strukturelemente oder der Metadaten ermitteln, die ein Band hat, rekursiv durchlaufen.
     * 
     * @param inStruct
     * @param inType
     * @return number
     */
    public int getNumberOfUghElements(DocStruct inStruct, CountType inType) {
        int rueckgabe = 0;
        if (inStruct != null) {
            /* --------------------------------
             * increment number of docstructs, or add number of metadata elements
             * --------------------------------*/
            if (inType == CountType.DOCSTRUCT) {
                rueckgabe++;
            } else {
                /* count non-empty persons */
                if (inStruct.getAllPersons() != null) {
                    for (Person p : inStruct.getAllPersons()) {
                        if (p.getLastname() != null && p.getLastname().trim().length() > 0) {
                            rueckgabe++;
                        }
                    }
                }
                /* count non-empty metadata */
                if (inStruct.getAllMetadata() != null) {
                    for (Metadata md : inStruct.getAllMetadata()) {
                        if (md.getValue() != null && md.getValue().trim().length() > 0) {
                            rueckgabe++;
                        }
                    }
                }
                /* count metadata in groups */
                if (inStruct.getAllMetadataGroups() != null) {
                    for (MetadataGroup mg : inStruct.getAllMetadataGroups()) {
                        for (Metadata md : mg.getMetadataList()) {
                            if (md.getValue() != null && md.getValue().trim().length() > 0) {
                                rueckgabe++;
                            }
                        }
                    }

                }
            }

            /* --------------------------------
             * call children recursive
             * --------------------------------*/
            if (inStruct.getAllChildren() != null) {
                for (DocStruct struct : inStruct.getAllChildren()) {
                    rueckgabe += getNumberOfUghElements(struct, inType);
                }
            }
        }
        return rueckgabe;
    }

}