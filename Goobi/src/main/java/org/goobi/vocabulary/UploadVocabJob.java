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
        return "UploadVocabJob";
    }

    @Override
    public void execute(JobExecutionContext context) {

        log.debug("Execute job: " + context.getJobDetail().getName() + " - " + context.getRefireCount());

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
