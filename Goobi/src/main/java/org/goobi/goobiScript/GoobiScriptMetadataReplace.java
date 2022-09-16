/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *             - https://goobi.io
 *             - https://www.intranda.com
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
package org.goobi.goobiScript;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.goobi.beans.Process;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.LogType;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.VariableReplacer;
import de.sub.goobi.persistence.managers.ProcessManager;
import lombok.extern.log4j.Log4j2;
import ugh.dl.DocStruct;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.Prefs;

@Log4j2
public class GoobiScriptMetadataReplace extends AbstractIGoobiScript implements IGoobiScript {

    // action:metadataReplace field:DocLanguage search:deutschTop replace:deutschNewTop position:top
    // action:metadataReplace field:DocLanguage search:deutschChild replace:deutschNewChild position:child

    @Override
    public String getAction() {
        return "metadataReplace";
    }

    @Override
    public String getSampleCall() {
        StringBuilder sb = new StringBuilder();
        addNewActionToSampleCall(sb, "This GoobiScript allows to replace an existing metadata within the METS file.");
        addParameterToSampleCall(sb, "field", "Description",
                "Internal name of the metadata field to be used. Use the internal name here (e.g. `TitleDocMain`), not the translated display name (e.g. `Main title`).");
        addParameterToSampleCall(sb, "search", "Phone", "Term to be searched for");
        addParameterToSampleCall(sb, "replace", "Telephone", "Term that shall replace the searched term");
        addParameterToSampleCall(sb, "regularExpression", "false",
                "If the search term shall used used as regular expression set this value to `true` here.");
        addParameterToSampleCall(sb, "position", "work",
                "Define where in the hierarchy of the METS file the searched term shall be replaced. Possible values are: `work` `top` `child` `any` `physical`");
        return sb.toString();
    }

    @Override
    public List<GoobiScriptResult> prepare(List<Integer> processes, String command, Map<String, String> parameters) {
        super.prepare(processes, command, parameters);

        if (parameters.get("field") == null || parameters.get("field").equals("")) {
            Helper.setFehlerMeldungUntranslated("goobiScriptfield", "Missing parameter: ", "field");
            return new ArrayList<>();
        }

        if (parameters.get("search") == null || parameters.get("search").equals("")) {
            Helper.setFehlerMeldungUntranslated("goobiScriptfield", "Missing parameter: ", "search");
            return new ArrayList<>();
        }

        if (parameters.get("position") == null || parameters.get("position").equals("")) {
            Helper.setFehlerMeldungUntranslated("goobiScriptfield", "Missing parameter: ", "position");
            return new ArrayList<>();
        }

        // add all valid commands to list
        List<GoobiScriptResult> newList = new ArrayList<>();
        for (Integer i : processes) {
            GoobiScriptResult gsr = new GoobiScriptResult(i, command, parameters, username, starttime);
            newList.add(gsr);
        }
        return newList;
    }

    @Override
    public void execute(GoobiScriptResult gsr) {
        Map<String, String> parameters = gsr.getParameters();
        Process p = ProcessManager.getProcessById(gsr.getProcessId());
        gsr.setProcessTitle(p.getTitel());
        gsr.setResultType(GoobiScriptResultType.RUNNING);
        gsr.updateTimestamp();
        try {
            Fileformat ff = p.readMetadataFile();
            // first get the top element
            DocStruct ds = ff.getDigitalDocument().getLogicalDocStruct();
            DocStruct physical = ff.getDigitalDocument().getPhysicalDocStruct();
            // find the right elements to adapt
            List<DocStruct> dsList = new ArrayList<>();
            switch (parameters.get("position")) {

                // just the anchor element
                case "top":
                    if (ds.getType().isAnchor()) {
                        dsList.add(ds);
                    } else {
                        gsr.setResultMessage("Error while adapting metadata for top element, as top element is no anchor");
                        gsr.setResultType(GoobiScriptResultType.ERROR);
                        return;
                    }
                    break;

                    // fist the first child element
                case "child":
                    if (ds.getType().isAnchor()) {
                        dsList.add(ds.getAllChildren().get(0));
                    } else {
                        gsr.setResultMessage("Error while adapting metadata for child, as top element is no anchor");
                        gsr.setResultType(GoobiScriptResultType.ERROR);
                        return;
                    }
                    break;

                    // any element in the hierarchy
                case "any":
                    dsList.add(ds);
                    dsList.addAll(ds.getAllChildrenAsFlatList());
                    if (physical!= null) {
                        dsList.add(physical);
                    }
                    break;
                case "physical":
                    if (physical!= null) {
                        dsList.add(physical);
                    }
                    break;

                    // default "work", which is the first child or the main top element if it is not an anchor
                default:
                    if (ds.getType().isAnchor()) {
                        dsList.add(ds.getAllChildren().get(0));
                    } else {
                        dsList.add(ds);
                    }
                    break;
            }

            // check if regular expressions shall be interpreted
            boolean searchFieldIsRegularExpression = getParameterAsBoolean("regularExpression");

            String replace = parameters.get("replace");
            if (replace == null) {
                replace = "";
            }


            // get the content to be set and pipe it through the variable replacer
            VariableReplacer replacer = new VariableReplacer(ff.getDigitalDocument(), p.getRegelsatz().getPreferences(), p, null);
            String field = parameters.get("field");
            String search = parameters.get("search");
            field = replacer.replace(field);
            search = replacer.replace(search);
            replace = replacer.replace(replace);

            // now change the searched metadata and save the file
            replaceMetadata(dsList, field, search, replace, p.getRegelsatz().getPreferences(),
                    searchFieldIsRegularExpression);
            p.writeMetadataFile(ff);
            Thread.sleep(2000);
            Helper.addMessageToProcessJournal(p.getId(), LogType.DEBUG,
                    "Metadata changed using GoobiScript: " + parameters.get("field") + " - " + parameters.get("search") + " - " + parameters.get("replace"), username);
            log.info("Metadata changed using GoobiScript for process with ID " + p.getId());
            gsr.setResultMessage("Metadata changed successfully.");
            gsr.setResultType(GoobiScriptResultType.OK);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e1) {
            log.error("Problem while changing the metadata using GoobiScript for process with id: " + p.getId(), e1);
            gsr.setResultMessage("Error while changing metadata: " + e1.getMessage());
            gsr.setResultType(GoobiScriptResultType.ERROR);
            gsr.setErrorText(e1.getMessage());
        }
        gsr.updateTimestamp();
    }

    /**
     * Method to replace a string in a given metadata from a {@link DocStruct}
     * 
     * @param dsList as List of structural elements to use
     * @param field the metadata field that is used
     * @param search a partial string to be replaced
     * @param replace the replacement to be used
     * @param prefs the {@link Preferences} to use
     * @param searchFieldIsRegularExpression interpret the search field as regular expression or as string
     */
    private void replaceMetadata(List<DocStruct> dsList, String field, String search, String replace, Prefs prefs,
            boolean searchFieldIsRegularExpression) {
        for (DocStruct ds : dsList) {
            List<? extends Metadata> mdlist = ds.getAllMetadataByType(prefs.getMetadataTypeByName(field));
            if (mdlist != null && ! mdlist.isEmpty()) {
                for (Metadata md : mdlist) {
                    if (searchFieldIsRegularExpression) {
                        for (Matcher m = Pattern.compile(search).matcher(md.getValue()); m.find();) {
                            md.setValue(md.getValue().replace(m.group(), replace));
                        }
                    } else {
                        if (md.getValue().contains(search)) {
                            md.setValue(md.getValue().replace(search, replace));
                        }
                    }
                }
            }
        }
    }

}
