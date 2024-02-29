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
package org.goobi.managedbeans;

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.goobi.beans.Ruleset;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.StorageProviderInterface;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.RulesetManager;
import lombok.Getter;
import lombok.Setter;

@Named("RegelsaetzeForm")
@WindowScoped
public class RulesetBean extends BasicBean implements Serializable {

    private static final long serialVersionUID = -8994941188718721705L;

    private static final String RETURN_PAGE_ALL = "ruleset_all";
    private static final String RETURN_PAGE_EDIT = "ruleset_edit";

    @Getter
    @Setter
    private Ruleset myRegelsatz = new Ruleset();

    public String Neu() {
        this.myRegelsatz = new Ruleset();
        return RETURN_PAGE_EDIT;
    }

    public String Speichern() {

        // JSF returns this.myRegelsatz.datei = "true" (as non-null string) if no file is selected in the drop down menu
        boolean isNoFileSelected = this.myRegelsatz.getDatei() == null || this.myRegelsatz.getDatei().equals("null");

        if (isNoFileSelected) {
            Helper.setFehlerMeldung("RulesetNotSpecified");
            return "";
        }

        try {
            if (hasValidRulesetFilePath(myRegelsatz, ConfigurationHelper.getInstance().getRulesetFolder())) {
                RulesetManager.saveRuleset(myRegelsatz);
                paginator.load();
                return FilterKein();
            } else {
                Helper.setFehlerMeldung("RulesetNotFound");
                return "";
            }
        } catch (DAOException e) {
            Helper.setFehlerMeldung("fehlerNichtSpeicherbar", e.getMessage());
            return "";
        }
    }

    private boolean hasValidRulesetFilePath(Ruleset r, String pathToRulesets) {
        Path rulesetFile = Paths.get(pathToRulesets + r.getDatei());
        return StorageProvider.getInstance().isFileExists(rulesetFile);
    }

    public String Loeschen() {
        try {
            if (hasAssignedProcesses(myRegelsatz)) {
                Helper.setFehlerMeldung("RulesetInUse");
                return "";
            } else {
                RulesetManager.deleteRuleset(myRegelsatz);
                paginator.load();
            }
        } catch (DAOException e) {
            Helper.setFehlerMeldung("fehlerNichtLoeschbar", e.getMessage());
            return "";
        }
        return FilterKein();
    }

    public String Cancel() {
        return RETURN_PAGE_ALL;
    }

    private boolean hasAssignedProcesses(Ruleset r) {
        Integer number = ProcessManager.getNumberOfProcessesWithRuleset(r.getId());
        return number != null && number > 0;
    }

    public String FilterKein() {
        RulesetManager rm = new RulesetManager();
        paginator = new DatabasePaginator("titel", filter, rm, RETURN_PAGE_ALL);
        return RETURN_PAGE_ALL;
    }

    public String FilterKeinMitZurueck() {
        FilterKein();
        return this.zurueck;
    }

    public List<String> getAvailableRulesetFiles() {
        String rulesetPath = ConfigurationHelper.getInstance().getRulesetFolder();
        StorageProviderInterface storage = StorageProvider.getInstance();
        List<Path> files = storage.listFiles(rulesetPath);
        List<String> fileNames = new ArrayList<>();
        for (int index = 0; index < files.size(); index++) {
            String name = files.get(index).getFileName().toString();
            if (storage.isFileExists(files.get(index)) && name.endsWith(".xml")) {
                fileNames.add(name);
            }
        }
        return fileNames;
    }
}
