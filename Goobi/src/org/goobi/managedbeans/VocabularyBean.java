package org.goobi.managedbeans;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.SessionScoped;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.goobi.vocabulary.Definition;
import org.goobi.vocabulary.VocabularyManager;

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

    private static final String PLUGIN_NAME = "intranda_administration_vocabulary";
    private static final String GUI = "/uii/administration_vocabulary.xhtml";

    //details up or down
    private String uiStatus;

    public String getUiStatus() {
        return uiStatus;
    }

    public void setUiStatus(String uiStatus) {
        this.uiStatus = uiStatus;
    }

    private VocabularyManager vm;

    public VocabularyManager getVm() {
        return vm;
    }

    private List<String> allVocabularies;

    public List<String> getAllVocabularies() {
        return allVocabularies;
    }

    /**
     * Constructor for parameter initialisation from config file
     */
    public VocabularyBean() {

        Initialize(null);

        uiStatus = "down";
    }

    public void Reload() throws SQLException {

        //        Reload(null);
    }

    public void Reload(String strVocabTitle) throws SQLException {

        Initialize(strVocabTitle);

    }

    //called when a new vocab is defined.
    public String OpenNewVocabPage() {

        ArrayList<Definition> lstDefs = new ArrayList<>();
        lstDefs.add(new Definition("Title", "input", "", ""));
        this.vm.setDefinitions(lstDefs);

        this.vm.generateNewVocabulary("New vocabulary");

        return "vocab_edit";
    }

    //called when an existing vocab is to be edited.
    public String OpenEditVocabPage() {

        return "vocab_edit";
    }

    private void Initialize(String strVocabTitle) {

        try {
            vm = new VocabularyManager();

            //all the titles:
            allVocabularies = vm.getAllVocabulariesFromDB();

            // set first vocabulary as current one
            if (allVocabularies.size() > 0) {
                if (strVocabTitle != null) {
                    vm.loadVocabulary(strVocabTitle);
                } else {
                    vm.loadVocabulary(allVocabularies.get(0));
                }
            } else {
                vm.loadVocabulary(null);
            }

        } catch (SQLException e) {
            // For now, just ignore
            e.printStackTrace();
        }

    }

    public String FilterKein() throws ConfigurationException {

        // initialise the vocabulary
        String configfile = "plugin_intranda_administration_vocabulary.xml";
        XMLConfiguration config = new XMLConfiguration("/opt/digiverso/goobi/config/" + configfile);

        //        VocabularyManager vm = new VocabularyManager(config);
        //        paginator = new DatabasePaginator("titel", filter, vm, "ruleset_all");
        return "vocabulary";
    }

}
