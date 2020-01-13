package org.goobi.managedbeans;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- https://goobi.io
 * 			- https://www.intranda.com
 * 			- https://github.com/intranda/goobi
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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.apache.commons.configuration.XMLConfiguration;
import org.goobi.beans.Docket;
import org.goobi.vocabulary.Definition;
import org.goobi.vocabulary.VocabRecord;
import org.goobi.vocabulary.Vocabulary;
import org.goobi.vocabulary.VocabularyManager;

import com.google.gson.Gson;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.DocketManager;
import de.sub.goobi.persistence.managers.ProcessManager;
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

        this.defTypes = new ArrayList<String>();
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
