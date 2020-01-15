package org.goobi.managedbeans;

import java.io.Serializable;
import java.sql.SQLException;

import javax.faces.bean.SessionScoped;

import org.apache.commons.configuration.ConfigurationException;
import org.goobi.vocabulary.Vocabulary;

import de.sub.goobi.persistence.managers.VocabularyManager;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

/**
 * 
 * This bean can be used to display the current state of the goobi_fast and goobi_slow queues. The bean provides methods to show all active tickets
 * and remove a ticket or clear the queue.
 *
 */

@javax.faces.bean.ManagedBean
@SessionScoped
@Log4j
public class VocabularyBean extends BasicBean implements Serializable {

    private static final long serialVersionUID = -4591427229251805665L;

    @Getter
    @Setter
    private Vocabulary currentVocabulary;

    //details up or down
    @Getter
    @Setter
    private String uiStatus;

    /**
     * Constructor for parameter initialisation from config file
     */
    public VocabularyBean() {

        uiStatus = "down";
        sortierung = "title";
    }

    public void Reload() throws SQLException {

        //        Reload(null);
    }

    public void Reload(String strVocabTitle) throws SQLException {

    }

    public String FilterKein() throws ConfigurationException {
        VocabularyManager vm = new VocabularyManager();
        paginator = new DatabasePaginator(sortierung, filter, vm, "vocabulary_all");
        return "administration_vocabulary";
    }

    public String editVocabulary() {
        return "vocab_edit";
    }

    public String newVocabulary() {
        currentVocabulary = new Vocabulary();
        return editVocabulary();
    }

}
