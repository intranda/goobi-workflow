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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.goobi.api.display.helper.NormDatabase;
import org.goobi.beans.Process;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.LogType;

import de.sub.goobi.config.ConfigNormdata;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.VariableReplacer;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.managers.ProcessManager;
import lombok.extern.log4j.Log4j2;
import ugh.dl.DocStruct;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.MetadataGroup;
import ugh.dl.Prefs;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.UGHException;

@Log4j2
public class GoobiScriptMetadataAdd extends AbstractIGoobiScript implements IGoobiScript {

    private static final String FIELD = "field";
    private static final String VALUE = "value";
    private static final String POSITION = "position";
    private static final String IGNORE_ERRORS = "ignoreErrors";
    private static final String TYPE = "type";
    private static final String GROUP = "group";

    private static final String POSITION_TOP = "top";
    private static final String POSITION_CHILD = "child";
    private static final String POSITION_ANY = "any";
    private static final String POSITION_PHYSICAL = "physical";

    @Override
    public String getAction() {
        return "metadataAdd";
    }

    @Override
    public String getSampleCall() {
        StringBuilder sb = new StringBuilder();
        addNewActionToSampleCall(sb, "This GoobiScript allows to add a new metadata to a METS file.");
        addParameterToSampleCall(sb, FIELD, "Description",
                "Internal name of the metadata field to be used. Use the internal name here (e.g. `TitleDocMain`), "
                        + "not the translated display name (e.g. `Main title`).");
        addParameterToSampleCall(sb, VALUE, "This is my content.",
                "This is used to define the value that shall be stored inside of the newly created metadata field.");
        addParameterToSampleCall(sb, "authorityName", "", "Name of the authority file, e.g. viaf or gnd.");
        addParameterToSampleCall(sb, "authorityValue", "", "Define the authority data value for this metadata field.");
        addParameterToSampleCall(sb, POSITION, "work",
                "Define where in the hierarchy of the METS file the searched term shall be replaced. Possible values are: "
                        + "`work` `top` `child` `any` `physical`");
        addParameterToSampleCall(sb, IGNORE_ERRORS, "true",
                "Define if the further processing shall be cancelled for a Goobi process if an error occures (`false`) or "
                        + "if the processing should skip errors and move on (`true`).\\n# This is especially useful if the the"
                        + " value `any` was selected for the position.");
        addParameterToSampleCall(sb, TYPE, "metadata",
                "Define what type of metadata you would like to add. Possible values are `metadata` and `group`. Default is metadata.");
        addParameterToSampleCall(sb, GROUP, "",
                "Internal name of the group. Use it when the metadata to add is located within a group or if a new group should be added.");
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
            // first get the top element
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

            // check if errors shall be ignored
            boolean ignoreErrors = getParameterAsBoolean(IGNORE_ERRORS);

            // get the content to be set and pipe it through the variable replacer
            String newvalue = parameters.get(VALUE);
            VariableReplacer replacer = new VariableReplacer(ff.getDigitalDocument(), p.getRegelsatz().getPreferences(), p, null);
            newvalue = replacer.replace(newvalue);
            String type = parameters.get(TYPE);
            String group = parameters.get(GROUP);

            String authorityName = parameters.get("authorityName");
            String authorityValue = parameters.get("authorityValue");

            // now add the new metadata and save the file
            addMetadata(dsList, type, group, parameters.get(FIELD), newvalue, p.getRegelsatz().getPreferences(), ignoreErrors, authorityName,
                    authorityValue);
            p.writeMetadataFile(ff);
            Thread.sleep(2000);
            Helper.addMessageToProcessJournal(p.getId(), LogType.DEBUG,
                    "Metadata added using GoobiScript: " + parameters.get(FIELD) + " - " + parameters.get(VALUE), username);
            log.info("Metadata added using GoobiScript for process with ID " + p.getId());
            gsr.setResultMessage("Metadata added successfully.");
            gsr.setResultType(GoobiScriptResultType.OK);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (UGHException | IOException | SwapException e1) {
            String message = "Problem while adding the metadata using GoobiScript for process with id: " + p.getId();
            Helper.addMessageToProcessJournal(p.getId(), LogType.ERROR, message, username);
            log.error(message, e1);
            gsr.setResultMessage("Error while adding metadata: " + e1.getMessage());
            gsr.setResultType(GoobiScriptResultType.ERROR);
            gsr.setErrorText(e1.getMessage());
        }
        gsr.updateTimestamp();
    }

    /**
     * Method to add a specific metadata to a given structural element
     *
     * @param dsList as List of structural elements to use
     * @param field the metadata field to create
     * @param prefs the {@link Preferences} to use
     * @param value the information the shall be stored as metadata in the given field
     * @throws MetadataTypeNotAllowedException
     */
    @SuppressWarnings("unchecked")
    private void addMetadata(List<DocStruct> dsList, String addType, String groupName, String field, String value, Prefs prefs, boolean ignoreErrors,
            String authorityName, String authorityValue)
            throws MetadataTypeNotAllowedException {
        String authorityUri = null;
        if (StringUtils.isNotBlank(authorityValue) && StringUtils.isNotBlank(authorityName)) {
            List<NormDatabase> dblist = ConfigNormdata.getConfiguredNormdatabases();
            for (NormDatabase db : dblist) {
                if (db.getAbbreviation().equalsIgnoreCase(authorityName)) {
                    authorityUri = db.getPath();
                }
            }
        }

        if (StringUtils.isNotBlank(addType) && GROUP.equals(addType)) {
            for (DocStruct ds : dsList) {
                MetadataGroup mg = new MetadataGroup(prefs.getMetadataGroupTypeByName(groupName));
                ds.addMetadataGroup(mg);
                Metadata mdColl = new Metadata(prefs.getMetadataTypeByName(field));
                mdColl.setValue(value);
                if (StringUtils.isNotBlank(authorityValue) && StringUtils.isNotBlank(authorityName)) {
                    mdColl.setAuthorityFile(authorityName, authorityUri, authorityValue);
                }
                mg.addMetadata(mdColl);
            }
        } else {
            outer: for (DocStruct ds : dsList) {
                List<Metadata> metadatalist = new ArrayList<>();
                String tmpValue = value;
                if (tmpValue.contains("metadata.")) {
                    for (Matcher m = VariableReplacer.getMetadataPattern().matcher(tmpValue); m.find();) {
                        String metadataName = m.group(1);
                        if (StringUtils.isNotBlank(groupName)) {
                            for (MetadataGroup grp : ds.getAllMetadataGroupsByType(prefs.getMetadataGroupTypeByName(field))) {
                                metadatalist.addAll(grp.getMetadataByType(metadataName));
                            }
                        } else {
                            metadatalist = (List<Metadata>) ds.getAllMetadataByType(prefs.getMetadataTypeByName(metadataName));
                        }

                        if (metadatalist != null && !metadatalist.isEmpty()) {
                            Metadata md = metadatalist.get(0);
                            tmpValue = tmpValue.replace(m.group(), md.getValue());
                        } else {
                            continue outer;
                        }
                    }
                }
                if (StringUtils.isNotBlank(groupName)) {
                    try {
                        for (MetadataGroup grp : ds.getAllMetadataGroupsByType(prefs.getMetadataGroupTypeByName(field))) {
                            Metadata mdColl = new Metadata(prefs.getMetadataTypeByName(field));
                            mdColl.setValue(tmpValue);
                            if (StringUtils.isNotBlank(authorityValue) && StringUtils.isNotBlank(authorityName)) {
                                mdColl.setAuthorityFile(authorityName, authorityUri, authorityValue);
                            }
                            grp.addMetadata(mdColl);
                        }
                    } catch (MetadataTypeNotAllowedException e) {
                        if (!ignoreErrors) {
                            throw e;
                        }
                    }
                } else {
                    Metadata mdColl = new Metadata(prefs.getMetadataTypeByName(field));
                    mdColl.setValue(tmpValue);
                    if (StringUtils.isNotBlank(authorityValue) && StringUtils.isNotBlank(authorityName)) {
                        mdColl.setAuthorityFile(authorityName, authorityUri, authorityValue);
                    }
                    try {
                        ds.addMetadata(mdColl);
                    } catch (MetadataTypeNotAllowedException e) {
                        if (!ignoreErrors) {
                            throw e;
                        }
                    }
                }
            }
        }
    }

    public static Iterable<MatchResult> findRegexMatches(String pattern, CharSequence s) {
        List<MatchResult> results = new ArrayList<>();
        for (Matcher m = Pattern.compile(pattern).matcher(s); m.find();) {
            results.add(m.toMatchResult());
        }
        return results;
    }

}
