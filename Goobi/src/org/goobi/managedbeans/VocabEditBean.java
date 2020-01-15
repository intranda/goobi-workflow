package org.goobi.managedbeans;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.goobi.vocabulary.Definition;
import org.goobi.vocabulary.VocabularyManager;

import de.sub.goobi.helper.Helper;
import lombok.Data;

@ManagedBean(name = "VocabEditForm")
@SessionScoped
@Data
public class VocabEditBean extends BasicBean {

    private static final long serialVersionUID = 766068522036497980L;
    //    private Docket myDocket = new Docket();
    private VocabularyManager vm;
    private List<String> defTypes;
    private VocabularyBean vocabBean;

    public VocabEditBean() {

        //this gets the unique class currently in session scope. O_o
        this.vocabBean = (VocabularyBean) (Helper.getManagedBeanValue("#{vocabularyBean}"));
        this.vm = vocabBean.getVm();

        this.defTypes = new ArrayList<>();
        defTypes.add("input");
        defTypes.add("textarea");
        defTypes.add("select");
    }

    public String newDef() {

        Definition def = new Definition("", "input", "", "");

        vm.getDefinitions().add(def);
        vm.addDefToRecords(def);
        return "";
    }

    public String removeDef(Definition def) {

        vm.removeDefFromRecords(def);
        return "";
    }

    public String save() {

        try {

            vm.removeUndefinedDefsFromRecords();
            //save vocab
            vm.saveVocabulary();

            //load it up
            vocabBean.Reload(vm.getVocabulary().getTitle());
            this.vm = vocabBean.getVm();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "";
    }

    //cancel the current edit
    public String cancel() throws SQLException {

        //reload the vocab beam (and manager) : this will wthrow away changes to the vm.
        vocabBean.Reload(vm.getVocabulary().getTitle());
        this.vm = vocabBean.getVm();

        //and return to the main page:
        return "administration_vocabulary";
    }
}
