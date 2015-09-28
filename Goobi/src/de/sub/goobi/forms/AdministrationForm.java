package de.sub.goobi.forms;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *          - http://www.goobi.org
 *          - http://launchpad.net/goobi-production
 *          - http://gdz.sub.uni-goettingen.de
 *          - http://www.intranda.com
 *          - http://digiverso.com 
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
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.goobi.production.enums.PluginType;
import org.goobi.production.flow.jobs.HistoryAnalyserJob;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IAdministrationPlugin;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.Helper;
import dubious.sub.goobi.helper.encryption.MD5;

@ManagedBean(name = "AdministrationForm")
@SessionScoped
public class AdministrationForm implements Serializable {
    private static final long serialVersionUID = 5648439270064158243L;
    private String passwort;
    private boolean istPasswortRichtig = false;
    public final static String DIRECTORY_SUFFIX = "_tif";

    private List<String> possibleAdministrationPluginNames;

    private String currentAdministrationPluginName;

    private IAdministrationPlugin administrationPlugin;

    public AdministrationForm() {
        possibleAdministrationPluginNames = PluginLoader.getListOfPlugins(PluginType.Administration);
        Collections.sort(possibleAdministrationPluginNames);
    }

    /* =============================================================== */

    /**
     * Passwort eingeben
     */
    public String Weiter() {
        this.passwort = new MD5(this.passwort).getMD5();
        String adminMd5 = ConfigurationHelper.getInstance().getAdminPassword();
        this.istPasswortRichtig = (this.passwort.equals(adminMd5));
        if (!this.istPasswortRichtig) {
            Helper.setFehlerMeldung("wrong passworwd", "");
        }
        return "";
    }

    /* =============================================================== */

    public String getPasswort() {
        return this.passwort;
    }

    public void setPasswort(String passwort) {
        this.passwort = passwort;
    }

    public void startStorageCalculationForAllProcessesNow() {
        HistoryAnalyserJob job = new HistoryAnalyserJob();
        if (job.getIsRunning() == false) {
            job.execute();
            Helper.setMeldung("scheduler calculation executed");
        } else {
            Helper.setMeldung("Job is already running, try again in a few minutes");
        }
    }


    public boolean isIstPasswortRichtig() {
        return this.istPasswortRichtig;
    }

   

    @Deprecated
    public void test() {
        Helper.setFehlerMeldung("Fehlermeldung mit extrem langem Text, die sich über viele Zeilen erstreckt und so weiter geht bis ein Zeilenumbruch kommt der dann in einem Zeilenumbruch endet und damit die Zeile umgebrochen hat 1");
        Helper.setFehlerMeldung("Fehlermeldung mit extrem langem Text, die sich über viele Zeilen erstreckt und so weiter geht bis ein Zeilenumbruch kommt der dann in einem Zeilenumbruch endet und damit die Zeile umgebrochen hat 2");
        Helper.setFehlerMeldung("Fehlermeldung mit extrem langem Text, die sich über viele Zeilen erstreckt und so weiter geht bis ein Zeilenumbruch kommt der dann in einem Zeilenumbruch endet und damit die Zeile umgebrochen hat 3");
        Helper.setFehlerMeldung(
                "Fehlermeldung mit extrem langem Text, die sich über viele Zeilen erstreckt und so weiter geht bis ein Zeilenumbruch kommt der dann in einem Zeilenumbruch endet und damit die Zeile umgebrochen hat 4",
                new Exception("eine Exception die eine Exception ist und damit eine Exception geworfen hat."));

        Helper.setMeldung("Meldung mit extrem langem Text, die sich über viele Zeilen erstreckt und so weiter geht bis ein Zeilenumbruch kommt der dann in einem Zeilenumbruch endet und damit die Zeile umgebrochen hat 1");
        Helper.setMeldung("Meldung mit extrem langem Text, die sich über viele Zeilen erstreckt und so weiter geht bis ein Zeilenumbruch kommt der dann in einem Zeilenumbruch endet und damit die Zeile umgebrochen hat 2");
        Helper.setMeldung("Meldung mit extrem langem Text, die sich über viele Zeilen erstreckt und so weiter geht bis ein Zeilenumbruch kommt der dann in einem Zeilenumbruch endet und damit die Zeile umgebrochen hat 3");
    }


    public List<String> getPossibleAdministrationPluginNames() {
        return possibleAdministrationPluginNames;
    }

    public void setPossibleAdministrationPluginNames(List<String> possibleAdministrationPluginNames) {
        this.possibleAdministrationPluginNames = possibleAdministrationPluginNames;
    }

    public String getCurrentAdministrationPluginName() {
        return currentAdministrationPluginName;
    }

    public void setCurrentAdministrationPluginName(String currentAdministrationPluginName) {
        this.currentAdministrationPluginName = currentAdministrationPluginName;
    }

    public IAdministrationPlugin getAdministrationPlugin() {
        return administrationPlugin;
    }

    public void setAdministrationPlugin(IAdministrationPlugin administrationPlugin) {
        this.administrationPlugin = administrationPlugin;
    }

    public String setPlugin(String pluginName) {
        currentAdministrationPluginName = pluginName;
        administrationPlugin = (IAdministrationPlugin) PluginLoader.getPluginByTitle(PluginType.Administration, currentAdministrationPluginName);
        return "administration";
    }

}
