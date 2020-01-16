package org.goobi.managedbeans;

import java.io.Serializable;

import javax.faces.bean.SessionScoped;

import org.goobi.vocabulary.Definition;
import org.goobi.vocabulary.Vocabulary;

import de.sub.goobi.helper.Helper;
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

    @Getter
    @Setter
    private Definition currentDefinition;

    //details up or down
    @Getter
    @Setter
    private String uiStatus;

    @Getter
    private String[] possibleDefinitionTypes = { "input", "textarea", "select" };

    public VocabularyBean() {
        uiStatus = "down";
        sortierung = "title";
    }

    public String FilterKein() {
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

    public String saveVocabulary() {
        // check if title is unique
        if (VocabularyManager.isTitleUnique(currentVocabulary)) {
            VocabularyManager.saveVocabulary(currentVocabulary);
        } else {
            Helper.setFehlerMeldung(Helper.getTranslation("plugin_admin_vocabulary_titleNotUnique"));
            return "";
        }
        return cancelEdition();
    }

    public String deleteVocabulary() {
        if (currentVocabulary.getId() != null) {
            // TODO delete records as well?
            VocabularyManager.deleteVocabulary(currentVocabulary);
        }
        return cancelEdition();
    }

    public String cancelEdition() {
        return FilterKein();
    }

    public void deleteDefinition() {
        if (currentDefinition != null && currentVocabulary != null) {
            currentVocabulary.getStruct().remove(currentDefinition);
        }
    }

    public void addDefinition() {
        currentVocabulary.getStruct().add(new Definition("", "", "", ""));
    }
}
