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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import org.apache.commons.lang3.StringUtils;
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
import ugh.dl.MetadataGroup;
import ugh.dl.Prefs;

@Log4j2
public class GoobiScriptMetadataDelete extends AbstractIGoobiScript implements IGoobiScript {

    private static final String FIELD = "field";
    private static final String VALUE = "value";
    private static final String POSITION = "position";
    private static final String IGNORE_VALUE = "ignoreValue";
    private static final String TYPE = "type";
    private static final String GROUP = "group";

    private static final String POSITION_TOP = "top";
    private static final String POSITION_CHILD = "child";
    private static final String POSITION_ANY = "any";
    private static final String POSITION_PHYSICAL = "physical";

    @Override
    public String getAction() {
        return "metadataDelete";
    }

    @Override
    public String getSampleCall() {
        StringBuilder sb = new StringBuilder();
        addNewActionToSampleCall(sb, "This GoobiScript allows to delete an existing metadata from the METS file.");
        addParameterToSampleCall(sb, FIELD, "Classification",
                "Internal name of the metadata field to be used. Use the internal name here (e.g. `TitleDocMain`), not the translated display name (e.g. `Main title`) here.");
        addParameterToSampleCall(sb, VALUE, "Animals",
                "Define the value that the metadata shall have. Only if the value is the same the metadata will be deleted.");
        addParameterToSampleCall(sb, POSITION, "work",
                "Define where in the hierarchy of the METS file the searched term shall be replaced. Possible values are: `work` `top` `child` `any` `physical`");
        addParameterToSampleCall(sb, IGNORE_VALUE, "false",
                "Set this parameter to `true` if the deletion of the metadata shall take place independent of the current metadata value. In this case all metadata that match the defined `field` will be deleted.");
        addParameterToSampleCall(sb, TYPE, "metadata",
                "Define what type of metadata you would like to change. Possible values are `metadata` and `group`. Default is metadata.");
        addParameterToSampleCall(sb, GROUP, "", "Internal name of the group. Use it when the metadata to change is located within a group.");
        return sb.toString();
    }

    @Override
    public List<GoobiScriptResult> prepare(List<Integer> processes, String command, Map<String, String> parameters) {
        super.prepare(processes, command, parameters);

        String missingParameter = "Missing parameter: ";
        if (StringUtils.isBlank(parameters.get(FIELD))) {
            Helper.setFehlerMeldungUntranslated(missingParameter, FIELD);
            return Collections.emptyList();
        }

        if (StringUtils.isBlank(parameters.get(VALUE))) {
            Helper.setFehlerMeldungUntranslated(missingParameter, VALUE);
            return Collections.emptyList();
        }

        if (StringUtils.isBlank(parameters.get(POSITION))) {
            Helper.setFehlerMeldungUntranslated(missingParameter, POSITION);
            return Collections.emptyList();
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
            DocStruct ds = ff.getDigitalDocument().getLogicalDocStruct();
            DocStruct physical = ff.getDigitalDocument().getPhysicalDocStruct();
            // find the right elements to adapt
            List<DocStruct> dsList = new ArrayList<>();
            switch (parameters.get(POSITION)) {

                // just the anchor element
                case POSITION_TOP:
                    if (ds.getType().isAnchor()) {
                        dsList.add(ds);
                    } else {
                        gsr.setResultMessage("Error while adapting metadata for top element, as top element is no anchor");
                        gsr.setResultType(GoobiScriptResultType.ERROR);
                        return;
                    }
                    break;

                // fist the first child element
                case POSITION_CHILD:
                    if (ds.getType().isAnchor()) {
                        dsList.add(ds.getAllChildren().get(0));
                    } else {
                        gsr.setResultMessage("Error while adapting metadata for child, as top element is no anchor");
                        gsr.setResultType(GoobiScriptResultType.ERROR);
                        return;
                    }
                    break;

                // any element in the hierarchy
                case POSITION_ANY:
                    dsList.add(ds);
                    dsList.addAll(ds.getAllChildrenAsFlatList());
                    if (physical != null) {
                        dsList.add(physical);
                    }
                    break;
                case POSITION_PHYSICAL:
                    if (physical != null) {
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

            // check if values shall be ignored
            boolean ignoreValue = getParameterAsBoolean(IGNORE_VALUE);

            // get the content to be set and pipe it through the variable replacer
            VariableReplacer replacer = new VariableReplacer(ff.getDigitalDocument(), p.getRegelsatz().getPreferences(), p, null);
            String field = parameters.get(FIELD);
            String value = parameters.get(VALUE);
            field = replacer.replace(field);
            value = replacer.replace(value);
            String type = parameters.get(TYPE);
            String group = parameters.get(GROUP);
            // now find the metadata field to delete
            deleteMetadata(dsList, type, group, field, value, ignoreValue, p.getRegelsatz().getPreferences());
            p.writeMetadataFile(ff);
            Thread.sleep(2000);
            Helper.addMessageToProcessJournal(p.getId(), LogType.DEBUG,
                    "Metadata deleted using GoobiScript: " + parameters.get(FIELD) + " - " + parameters.get(VALUE), username);
            log.info("Metadata deleted using GoobiScript for process with ID " + p.getId());
            gsr.setResultMessage("Metadata deleted successfully.");
            gsr.setResultType(GoobiScriptResultType.OK);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e1) {
            log.error("Problem while deleting the metadata using GoobiScript for process with id: " + p.getId(), e1);
            gsr.setResultMessage("Error while deleting metadata: " + e1.getMessage());
            gsr.setResultType(GoobiScriptResultType.ERROR);
            gsr.setErrorText(e1.getMessage());
        }
        gsr.updateTimestamp();
    }

    /**
     * Method to delete a given metadata from a {@link DocStruct}
     *
     * @param dsList as List of structural elements to use
     * @param field the metadata field that is used
     * @param value the metadata value to be deleted
     * @param ignoreValue a boolean that defines if the value of the metadata shall not be checked before deletion
     * @param prefs the {@link Preferences} to use
     */
    @SuppressWarnings("unchecked")
    private void deleteMetadata(List<DocStruct> dsList, String deletionType, String groupName, String metadataName, String value, boolean ignoreValue,
            Prefs prefs) {
        if (StringUtils.isNotBlank(deletionType) && GROUP.equals(deletionType)) {
            for (DocStruct ds : dsList) {
                List<MetadataGroup> groups = new ArrayList<>(ds.getAllMetadataGroupsByType(prefs.getMetadataGroupTypeByName(metadataName)));
                for (MetadataGroup grp : groups) {
                    ds.removeMetadataGroup(grp, true);
                }
            }
        } else {
            for (DocStruct ds : dsList) {
                List<Metadata> mdlist = new ArrayList<>();
                if (StringUtils.isNotBlank(groupName)) {
                    List<MetadataGroup> groups = ds.getAllMetadataGroupsByType(prefs.getMetadataGroupTypeByName(groupName));
                    for (MetadataGroup mg : groups) {
                        mdlist.addAll(mg.getMetadataByType(metadataName));
                    }
                } else {
                    mdlist = (List<Metadata>) ds.getAllMetadataByType(prefs.getMetadataTypeByName(metadataName));
                }

                if (mdlist != null && !mdlist.isEmpty()) {
                    for (Metadata md : mdlist) {
                        if (ignoreValue || md.getValue().equals(value)) {
                            md.getParent().removeMetadata(md, true);
                        }
                    }
                }
            }
        }
    }

}
