package org.goobi.production.properties;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.apache.commons.lang3.StringUtils;
import org.goobi.beans.GoobiProperty;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.production.cli.helper.StringPair;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.MetadataManager;
import io.goobi.workflow.api.vocabulary.APIException;
import io.goobi.workflow.api.vocabulary.VocabularyAPIManager;
import jakarta.faces.model.SelectItem;
import lombok.extern.log4j.Log4j2;

@Log4j2
public final class PropertyParser {

    private static XMLConfiguration config = null;

    private static PropertyParser instance = null;

    /**
     * private constructor to read the configuration file
     */

    private PropertyParser() {
        try {
            config = new XMLConfiguration(); // NOSONAR this constructor is only called once, initializing a static field is safe
            config.setDelimiterParsingDisabled(true);
            config.load(ConfigurationHelper.getInstance().getConfigurationFolder() + "goobi_processProperties.xml");
            config.setReloadingStrategy(new FileChangedReloadingStrategy());
            config.setExpressionEngine(new XPathExpressionEngine());
        } catch (ConfigurationException e) {
            log.error(e);
            config = new XMLConfiguration(); // NOSONAR this constructor is only called once, initializing a static field is safe
        }
    }

    /**
     * get instance of the PropertyParser.
     * 
     * instance gets initialized, when getInstance() was called the first time
     * 
     * @return current instance
     */

    public static synchronized PropertyParser getInstance() {
        if (instance == null) {
            instance = new PropertyParser();
        }
        return instance;
    }

    /**
     * return the list of metadata names to display in a step.
     * 
     * @param step current step
     * @return names of metadata fields
     */

    public List<String> getDisplayableMetadataForStep(Step step) {
        return buildStringList(null, step);
    }

    /**
     * return the list of metadata names to display in process details.
     * 
     * @param process current process
     * @return names of metadata fields
     */

    public List<String> getDisplayableMetadataForProcess(Process process) {
        return buildStringList(process, null);
    }

    /**
     * Is needed by getDisplayableMetadataForStep() and getDisplayableMetadataForProcess(). It builds the String list with all needed text for the
     * current step or process
     * 
     * @param process The current process
     * @param step A current step of a process
     * @return The list of information strings
     */
    private static List<String> buildStringList(Process inProcess, Step step) {
        Process process = inProcess;

        if (process == null) {
            process = step.getProzess();
        }
        if (process.isIstTemplate()) {
            return Collections.emptyList();
        }

        String workflowTitle = "";
        for (GoobiProperty p : process.getEigenschaften()) {
            if ("Template".equals(p.getPropertyName())) {
                workflowTitle = p.getPropertyValue();
            }
        }
        StringBuilder xpath = new StringBuilder();
        // get all metadata
        xpath.append("/metadata");
        // limit by project
        xpath.append("[not(./project) or ./project='*' or ./project=");
        xpath.append(PropertyParser.getEscapedProperty(process.getProjekt().getTitel()));
        xpath.append("]");
        if (step != null) {
            // limit by step
            xpath.append("/showStep[@name=");
            xpath.append(PropertyParser.getEscapedProperty(step.getTitel()));
            xpath.append(" and (@template='*' or not(@template) ");
            if (StringUtils.isNotBlank(workflowTitle)) {
                xpath.append("or @template=");
                xpath.append(PropertyParser.getEscapedProperty(workflowTitle));
            }
            xpath.append(" )]/..");
        } else {
            xpath.append("[not(./showProcessGroup/@template) or ./showProcessGroup/@template='*' ");
            if (StringUtils.isNotBlank(workflowTitle)) {
                xpath.append("or ./showProcessGroup/@template=");
                xpath.append(PropertyParser.getEscapedProperty(workflowTitle));
            }
            xpath.append("]");
        }

        // get name attribute
        xpath.append("/@name");

        return Arrays.asList(config.getStringArray(xpath.toString()));
    }

    /**
     * Escapes single quotes in the text parameter. When there are no single quotes, the normal text is returned in quotes. Otherwise the concat()
     * function is used and returned with the escaped single quotes.
     * 
     * @param text The text where the single quotes may occur
     * @return The escaped text
     */
    public static String getEscapedProperty(String text) {
        if (!text.contains("'")) {
            return "'" + text + "'";
        } else {
            return "concat('" + text.replace("'", "',\"'\",'") + "')";
        }
    }

    public List<DisplayProperty> getPropertiesForStep(Step mySchritt) {

        String stepTitle = mySchritt.getTitel();
        String projectTitle = mySchritt.getProzess().getProjekt().getTitel();
        String workflowTitle = "";
        ArrayList<DisplayProperty> properties = new ArrayList<>();

        // find out original workflow template
        for (GoobiProperty p : mySchritt.getProzess().getEigenschaften()) {
            if ("Template".equals(p.getPropertyName())) {
                workflowTitle = p.getPropertyValue();
            }
        }

        if (mySchritt.getProzess().isIstTemplate()) {
            return properties;
        }

        // run though all properties
        int countProperties = config.getMaxIndex("/property");
        for (int i = 0; i <= countProperties; i++) {
            int position = i + 1;
            String property = "/property[" + position + "]";
            // general values for property
            DisplayProperty pp = new DisplayProperty();
            pp.setName(config.getString(property + "/@name"));
            pp.setContainer(config.getString(property + "/@container"));

            // workflows

            // project and workflows are configured correct?

            // projects
            int count = config.getMaxIndex(property + "/project");
            for (int j = 0; j <= count; j++) {
                pp.getProjects().add(config.getString(property + "/project[" + (j + 1) + "]"));
            }

            // project is configured correct?
            boolean projectOk = pp.getProjects().contains("*") || pp.getProjects().contains(projectTitle) || pp.getProjects().isEmpty();

            if (projectOk) {
                // showStep
                boolean containsCurrentStepTitle = false;
                count = config.getMaxIndex(property + "/showStep");
                for (int j = 0; j <= count; j++) {
                    ShowStepCondition ssc = new ShowStepCondition();
                    String showStep = property + "/showStep[" + (j + 1) + "]";
                    ssc.setName(config.getString(showStep + "/@name"));
                    String access = config.getString(showStep + "/@access");
                    String configuredTemplate = config.getString(showStep + "/@template", "*");
                    boolean duplicate = config.getBoolean(showStep + "/@duplicate", false);
                    ssc.setAccessCondition(AccessCondition.getAccessConditionByName(access));

                    boolean workflowMatches =
                            "*".equals(configuredTemplate) || (StringUtils.isNotBlank(workflowTitle) && configuredTemplate.equals(workflowTitle));

                    if ((ssc.getName().equals(stepTitle) || "*".equals(ssc.getName())) && workflowMatches) {
                        containsCurrentStepTitle = true;
                        pp.setDuplicationAllowed(duplicate);
                        pp.setCurrentStepAccessCondition(AccessCondition.getAccessConditionByName(access));
                    }

                    List<HierarchicalConfiguration> displayConditions = config.configurationsAt(showStep + "/display");

                    for (HierarchicalConfiguration hc : displayConditions) {
                        ssc.getDisplayCondition().add(new StringPair(hc.getString("@property"), hc.getString("@value")));
                    }

                    pp.getShowStepConditions().add(ssc);
                }

                // steptitle is configured
                if (containsCurrentStepTitle) {
                    // showProcessGroupAccessCondition
                    String groupAccess = config.getString(property + "/showProcessGroup/@access");
                    if (groupAccess != null) {
                        pp.setShowProcessGroupAccessCondition(AccessCondition.getAccessConditionByName(groupAccess));
                    } else {
                        pp.setShowProcessGroupAccessCondition(AccessCondition.WRITE);
                    }

                    // validation expression
                    pp.setValidation(config.getString(property + "/validation"));
                    // type
                    pp.setType(Type.getTypeByName(config.getString(property + "/type")));
                    // pattern
                    pp.setPattern(config.getString(property + "/pattern", "dd.MM.yyyy"));
                    // (default) value
                    String defaultValue = config.getString(property + "/defaultvalue");
                    if (Type.METADATA.equals(pp.getType())) {
                        String metadata = MetadataManager.getMetadataValue(mySchritt.getProzess().getId(), defaultValue);
                        pp.setValue(metadata);
                    } else {
                        pp.setValue(defaultValue);
                        pp.setReadValue("");
                    }

                    if (Type.VOCABULARYREFERENCE.equals(pp.getType()) || Type.VOCABULARYMULTIREFERENCE.equals(pp.getType())) {
                        populatePossibleValuesWithVocabulary(property, pp);
                    } else {
                        // possible values
                        count = config.getMaxIndex(property + "/value");
                        for (int j = 0; j <= count; j++) {
                            String label = config.getString(property + "/value[" + (j + 1) + "]/@label");
                            String value = config.getString(property + "/value[" + (j + 1) + "]");
                            if (StringUtils.isBlank(label)) {
                                label = value;
                            }

                            pp.getPossibleValues().add(new SelectItem(value, label));
                        }
                    }

                    properties.add(pp);
                }
            }
        }

        // add existing 'eigenschaften' to properties from config, so we have all properties from config and some of them with already existing
        // 'eigenschaften'
        ArrayList<DisplayProperty> listClone = new ArrayList<>(properties);
        mySchritt.getProzess().setEigenschaften(null);
        List<GoobiProperty> plist = mySchritt.getProzess().getEigenschaftenList();
        for (GoobiProperty pe : plist) {

            for (DisplayProperty pp : listClone) {
                if (pe.getPropertyName() != null && pe.getPropertyName().equals(pp.getName())) {

                    // pp has no pe assigned
                    if (pp.getProzesseigenschaft() == null) {
                        pp.setProzesseigenschaft(pe);
                        pp.setValue(pe.getPropertyValue());
                        pp.setContainer(pe.getContainer());
                    } else {
                        // clone pp
                        DisplayProperty pnew = pp.getClone(pe.getContainer());
                        pnew.setProzesseigenschaft(pe);
                        pnew.setValue(pe.getPropertyValue());
                        pnew.setContainer(pe.getContainer());
                        properties.add(pnew);
                    }
                }
            }
        }
        return properties;
    }

    public List<DisplayProperty> getPropertiesForProcess(Process process) {
        String projectTitle = process.getProjekt().getTitel();
        ArrayList<DisplayProperty> properties = new ArrayList<>();
        if (process.isIstTemplate()) {
            List<GoobiProperty> plist = process.getEigenschaftenList();
            for (GoobiProperty pe : plist) {
                DisplayProperty pp = new DisplayProperty();
                pp.setName(pe.getPropertyName());
                pp.setProzesseigenschaft(pe);
                pp.setType(Type.TEXT);
                pp.setValue(pe.getPropertyValue());
                pp.setContainer(pe.getContainer());
                properties.add(pp);
            }
            return properties;
        }
        String workflowTitle = "";
        for (GoobiProperty p : process.getEigenschaften()) {
            if ("Template".equals(p.getPropertyName())) {
                workflowTitle = p.getPropertyValue();
            }
        }
        // run though all properties
        int countProperties = config.getMaxIndex("/property");
        for (int i = 0; i <= countProperties; i++) {
            int position = i + 1;
            String property = "/property[" + position + "]";
            // general values for property
            DisplayProperty pp = new DisplayProperty();
            pp.setName(config.getString(property + "/@name"));
            pp.setContainer(config.getString(property + "/@container"));
            // workflows
            // project and workflows are configured correct?
            // projects
            int count = config.getMaxIndex(property + "/project");
            for (int j = 0; j <= count; j++) {
                pp.getProjects().add(config.getString(property + "/project[" + (j + 1) + "]"));
            }
            boolean templateMatches = false;
            String groupAccess = "write";
            if (StringUtils.isBlank(workflowTitle)) {
                templateMatches = true;
            } else {
                // get configured showProcessGroup elements
                count = config.getMaxIndex(property + "/showProcessGroup");
                // no element exists
                if (count == -1) {
                    templateMatches = true;
                } else {
                    // run through all, check if template matches
                    for (int j = 0; j <= count; j++) {
                        String configuredTempate = config.getString(property + "/showProcessGroup[" + (j + 1) + "]/@template");
                        if (StringUtils.isBlank(configuredTempate) || "*".equals(configuredTempate) || workflowTitle.equals(configuredTempate)) {
                            templateMatches = true;
                            groupAccess = config.getString(property + "/showProcessGroup[" + (j + 1) + "]/@access");
                            break;
                        }
                    }
                }
            }
            // project is configured
            if (templateMatches && (pp.getProjects().contains("*") || pp.getProjects().contains(projectTitle))) {
                if (groupAccess != null) {

                    pp.setShowProcessGroupAccessCondition(AccessCondition.getAccessConditionByName(groupAccess));
                } else {
                    pp.setShowProcessGroupAccessCondition(AccessCondition.WRITE);
                }
                // validation expression
                pp.setValidation(config.getString(property + "/validation"));
                // type
                pp.setType(Type.getTypeByName(config.getString(property + "/type")));
                // pattern
                pp.setPattern(config.getString(property + "/pattern", "dd.MM.yyyy"));
                // (default) value
                String defaultValue = config.getString(property + "/defaultvalue");
                if (Type.METADATA.equals(pp.getType())) {
                    String metadata = MetadataManager.getMetadataValue(process.getId(), defaultValue);
                    pp.setValue(metadata);
                } else {
                    pp.setValue(defaultValue);
                    pp.setReadValue("");
                }
                if (Type.VOCABULARYREFERENCE.equals(pp.getType()) || Type.VOCABULARYMULTIREFERENCE.equals(pp.getType())) {
                    populatePossibleValuesWithVocabulary(property, pp);
                } else {
                    // possible values
                    count = config.getMaxIndex(property + "/value");
                    if (count > 0 && pp.getPossibleValues().isEmpty() && !Type.LISTMULTISELECT.equals(pp.getType())) {
                        pp.getPossibleValues().add(new SelectItem("", Helper.getTranslation("bitteAuswaehlen")));
                    }
                    for (int j = 0; j <= count; j++) {
                        String value = config.getString(property + "/value[" + (j + 1) + "]");

                        String label = config.getString(property + "/value[" + (j + 1) + "]/@label");
                        if (StringUtils.isBlank(label)) {
                            label = value;
                        }
                        pp.getPossibleValues().add(new SelectItem(value, label));
                    }
                }
                log.trace("add property A " + pp.getName() + " - " + pp.getValue() + " - " + pp.getContainer());
                properties.add(pp);
            }
        } // add existing 'eigenschaften' to properties from config, so we have all properties from config and some of them with already existing
          // 'eigenschaften'
        List<DisplayProperty> listClone = new ArrayList<>(properties);
        process.setEigenschaften(null);
        List<GoobiProperty> plist = new ArrayList<>(process.getEigenschaftenList());
        for (GoobiProperty pe : plist) {
            if (pe.getPropertyName() != null) {
                for (DisplayProperty pp : listClone) {
                    if (pe.getPropertyName().equals(pp.getName())) {
                        // pp has no pe assigned
                        if (pp.getProzesseigenschaft() == null) {
                            pp.setProzesseigenschaft(pe);
                            pp.setValue(pe.getPropertyValue());
                            pp.setContainer(pe.getContainer());
                        } else {
                            // clone pp
                            DisplayProperty pnew = pp.getClone(pe.getContainer());
                            pnew.setProzesseigenschaft(pe);
                            pnew.setValue(pe.getPropertyValue());
                            pnew.setContainer(pe.getContainer());
                            log.trace("add property B " + pp.getName() + " - " + pp.getValue() + " - " + pp.getContainer());
                            properties.add(pnew);
                        }
                    }
                }
            }
        }
        // add 'eigenschaft' to all ProcessProperties
        for (DisplayProperty pp : properties) {
            if (pp.getProzesseigenschaft() != null) {
                plist.remove(pp.getProzesseigenschaft());
            }
        }
        // create ProcessProperties to remaining 'eigenschaften'
        if (!plist.isEmpty()) {
            for (GoobiProperty pe : plist) {
                DisplayProperty pp = new DisplayProperty();
                pp.setProzesseigenschaft(pe);
                pp.setName(pe.getPropertyName());
                pp.setValue(pe.getPropertyValue());
                pp.setContainer(pe.getContainer());
                pp.setType(Type.TEXT);
                log.trace("add property C " + pp.getName() + " - " + pp.getValue() + " - " + pp.getContainer());
                properties.add(pp);
            }
        }
        log.trace("all properties are " + properties.size());
        return properties;
    }

    public List<DisplayProperty> getProcessCreationProperties(Process process, String templateName) {
        List<DisplayProperty> properties = new ArrayList<>();

        List<HierarchicalConfiguration> propertyList = config.configurationsAt("/property");
        for (int i = 0; i < propertyList.size(); i++) {
            HierarchicalConfiguration prop = propertyList.get(i);
            String property = "/property[" + (i + 1) + "]";

            DisplayProperty pp = new DisplayProperty();
            // general values for property
            pp.setName(prop.getString("@name"));
            pp.setContainer(prop.getString("@container"));
            // projects
            pp.getProjects().addAll(Arrays.asList(prop.getStringArray("/project")));
            // project is configured
            if (!pp.getProjects().contains("*") && !pp.getProjects().contains(process.getProjekt().getTitel())) {
                //  current project is not configured for this property, skip it
                continue;
            }
            HierarchicalConfiguration templateDefinition = null;
            try {
                //  check if <showProcessCreation access="write" template="xxx" /> exists
                templateDefinition = prop.configurationAt("/showProcessCreation[@template='" + templateName + "']");
            } catch (NullPointerException | IllegalArgumentException e) {
                // no specific configuration for this template, check if <showProcessCreation access="write" template="*" /> exists
                try {
                    templateDefinition = prop.configurationAt("/showProcessCreation[@template='*']");
                } catch (NullPointerException | IllegalArgumentException e1) {
                    // no generic configuration for all templates
                    templateDefinition = null;
                }
            }
            if (templateDefinition != null) {
                // configuration for current template exists
                String groupAccess = templateDefinition.getString("@access", "write");
                pp.setShowProcessGroupAccessCondition(AccessCondition.getAccessConditionByName(groupAccess));

                // validation expression
                pp.setValidation(prop.getString("/validation"));
                // type
                pp.setType(Type.getTypeByName(prop.getString("/type")));
                // pattern
                pp.setPattern(prop.getString("/pattern", "dd.MM.yyyy"));
                // (default) value
                String defaultValue = prop.getString("/defaultvalue");
                if (Type.METADATA.equals(pp.getType())) {
                    String metadata = MetadataManager.getMetadataValue(process.getId(), defaultValue);
                    pp.setValue(metadata);
                } else {
                    pp.setValue(defaultValue);
                    pp.setReadValue("");
                }

                List<HierarchicalConfiguration> displayConditions = templateDefinition.configurationsAt("/display");

                for (HierarchicalConfiguration hc : displayConditions) {
                    pp.getProcessCreationConditions().add(new StringPair(hc.getString("@property"), hc.getString("@value")));
                }

                if (Type.VOCABULARYREFERENCE.equals(pp.getType()) || Type.VOCABULARYMULTIREFERENCE.equals(pp.getType())) {
                    populatePossibleValuesWithVocabulary(property, pp);
                } else {
                    // possible values
                    int count = config.getMaxIndex(property + "/value");
                    if (count > 0 && pp.getPossibleValues().isEmpty() && !Type.LISTMULTISELECT.equals(pp.getType())) {
                        pp.getPossibleValues().add(new SelectItem("", Helper.getTranslation("bitteAuswaehlen")));
                    }
                    for (int j = 0; j <= count; j++) {
                        String label = config.getString(property + "/value[" + (j + 1) + "]/@label");
                        String value = config.getString(property + "/value[" + (j + 1) + "]");
                        if (StringUtils.isBlank(label)) {
                            label = value;
                        }

                        pp.getPossibleValues().add(new SelectItem(value, label));

                    }
                }

                properties.add(pp);
            }
        }
        return properties;

    }

    private void populatePossibleValuesWithVocabulary(String property, DisplayProperty pp) {
        String vocabularyName = config.getString(property + "/vocabulary");
        try {
            long vocabularyId = VocabularyAPIManager.getInstance().vocabularies().findByName(vocabularyName).getId();
            pp.setPossibleValues(new LinkedList<>());
            // this "Please select" element is only required for non drop-down badge components, as this component handles it itself
            if (Type.VOCABULARYREFERENCE.equals(pp.getType())) {
                pp.getPossibleValues().add(new SelectItem("", Helper.getTranslation("bitteAuswaehlen")));
            }
            pp.getPossibleValues()
                    .addAll(VocabularyAPIManager.getInstance()
                            .vocabularyRecords()
                            .getRecordSelectItems(vocabularyId));
        } catch (APIException e) {
            log.warn("Unable to parse vocabulary (multi) reference property \"{}\"", property, e);
        }
    }
}
