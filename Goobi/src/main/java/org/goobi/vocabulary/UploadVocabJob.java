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
package org.goobi.vocabulary;

import java.util.List;

import org.goobi.beans.DatabaseObject;
import org.goobi.production.flow.jobs.AbstractGoobiJob;
import org.quartz.JobExecutionContext;

import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.VocabularyManager;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class UploadVocabJob extends AbstractGoobiJob {

    /*
     * (non-Javadoc)
     * 
     * @see org.goobi.production.flow.jobs.SimpleGoobiJob#initialize()
     */
    @Override
    public String getJobName() {
        return "dailyVocabJob";
    }

    @Override
    public void execute(JobExecutionContext context) {

        log.debug("Execute job: " + context.getJobDetail().getKey() + " - " + context.getRefireCount());

        VocabularyManager vocabMan = new VocabularyManager();

        try {

            //get list of vocabularies
            List<? extends DatabaseObject> lstVocabs = vocabMan.getList("title", null, 0, 1000, null);

            for (DatabaseObject dbObject : lstVocabs) {

                Vocabulary vocab = (Vocabulary) dbObject;
                //for each, check if altered since last save
                if (VocabularyManager.getVocabularyLastUploaded(vocab).after(VocabularyManager.getVocabularyLastAltered(vocab))) {
                    continue;
                }

                //if so, update authority server
                VocabularyManager.getAllRecords(vocab);
                VocabularyUploader.upload(vocab);

                log.debug(String.format("Vocabulary Uploader: %s uploaded.", vocab.getTitle()));
            }

        } catch (DAOException e) {
            log.error(e);
        }

        log.debug("Vocabulary Uploader completed");
    }

}
