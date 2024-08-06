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
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.apache.commons.lang3.StringUtils;
import org.goobi.beans.Process;
import org.goobi.beans.Processproperty;
import org.goobi.beans.Step;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.persistence.managers.MetadataManager;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class PropertyParser {

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
     * get instance of the PropertyParser
     * 
     * instance gets initialized, when getInstance() was called the first time
     * 
     * @return
     */

    public static synchronized PropertyParser getInstance() {
        if (instance == null) {
            instance = new PropertyParser();
        }
        return instance;
    }

    /**
     * return the list of metadata names to display in a step
     * 
     * @param step current step
     * @return names of metadata fields
     */

    public List<String> getDisplayableMetadataForStep(Step step) {
        return buildStringList(null, step);
    }

    /**
     * return the list of metadata names to display in process details
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
    private static List<String> buildStringList(Process process, Step step) {
        if (process == null) {
            process = step.getProzess();
        }
        if (process.isIstTemplate()) {
            return Collections.emptyList();
        }

        String workflowTitle = "";
        for (Processproperty p : process.getEigenschaften()) {
            if ("Template".equals(p.getTitel())) {
                workflowTitle = p.getWert();
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

    public List<ProcessProperty> getPropertiesForStep(Step mySchritt) {

        String stepTitle = mySchritt.getTitel();
        String projectTitle = mySchritt.getProzess().getProjekt().getTitel();
        String workflowTitle = "";
        ArrayList<ProcessProperty> properties = new ArrayList<>();

        // find out original workflow template
        for (Processproperty p : mySchritt.getProzess().getEigenschaften()) {
            if ("Template".equals(p.getTitel())) {
                workflowTitle = p.getWert();
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
            ProcessProperty pp = new ProcessProperty();
            pp.setName(config.getString(property + "/@name"));
            pp.setContainer(config.getInt(property + "/@container"));

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

                    // possible values
                    count = config.getMaxIndex(property + "/value");
                    for (int j = 0; j <= count; j++) {
                        pp.getPossibleValues().add(config.getString(property + "/value[" + (j + 1) + "]"));
                    }
                    properties.add(pp);
                }
            }
        }

        // add existing 'eigenschaften' to properties from config, so we have all properties from config and some of them with already existing
        // 'eigenschaften'
        ArrayList<ProcessProperty> listClone = new ArrayList<>(properties);
        mySchritt.getProzess().setEigenschaften(null);
        List<Processproperty> plist = mySchritt.getProzess().getEigenschaftenList();
        for (Processproperty pe : plist) {

            for (ProcessProperty pp : listClone) {
                if (pe.getTitel() != null && pe.getTitel().equals(pp.getName())) {

                    // pp has no pe assigned
                    if (pp.getProzesseigenschaft() == null) {
                        pp.setProzesseigenschaft(pe);
                        pp.setValue(pe.getWert());
                        pp.setContainer(pe.getContainer());
                    } else {
                        // clone pp
                        ProcessProperty pnew = pp.getClone(pe.getContainer());
                        pnew.setProzesseigenschaft(pe);
                        pnew.setValue(pe.getWert());
                        pnew.setContainer(pe.getContainer());
                        properties.add(pnew);
                    }
                }
            }
        }
        return properties;
    }

    public List<ProcessProperty> getPropertiesForProcess(Process process) {
        String projectTitle = process.getProjekt().getTitel();
        ArrayList<ProcessProperty> properties = new ArrayList<>();
        if (process.isIstTemplate()) {
            List<Processproperty> plist = process.getEigenschaftenList();
            for (Processproperty pe : plist) {
                ProcessProperty pp = new ProcessProperty();
                pp.setName(pe.getTitel());
                pp.setProzesseigenschaft(pe);
                pp.setType(Type.TEXT);
                pp.setValue(pe.getWert());
                pp.setContainer(pe.getContainer());
                properties.add(pp);
            }
            return properties;
        }

        String workflowTitle = "";
        for (Processproperty p : process.getEigenschaften()) {
            if ("Template".equals(p.getTitel())) {
                workflowTitle = p.getWert();
            }
        }

        // run though all properties
        int countProperties = config.getMaxIndex("/property");
        for (int i = 0; i <= countProperties; i++) {
            int position = i + 1;
            String property = "/property[" + position + "]";
            // general values for property
            ProcessProperty pp = new ProcessProperty();
            pp.setName(config.getString(property + "/@name"));
            pp.setContainer(config.getInt(property + "/@container"));

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

                // possible values
                count = config.getMaxIndex(property + "/value");
                for (int j = 0; j <= count; j++) {
                    pp.getPossibleValues().add(config.getString(property + "/value[" + (j + 1) + "]"));
                }
                if (log.isDebugEnabled()) {
                    log.debug("add property A " + pp.getName() + " - " + pp.getValue() + " - " + pp.getContainer());
                }
                properties.add(pp);

            }
        } // add existing 'eigenschaften' to properties from config, so we have all properties from config and some of them with already existing
          // 'eigenschaften'
        List<ProcessProperty> listClone = new ArrayList<>(properties);
        process.setEigenschaften(null);
        List<Processproperty> plist = new ArrayList<>(process.getEigenschaftenList());
        for (Processproperty pe : plist) {

            if (pe.getTitel() != null) {

                for (ProcessProperty pp : listClone) {
                    if (pe.getTitel().equals(pp.getName())) {
                        // pp has no pe assigned
                        if (pp.getProzesseigenschaft() == null) {
                            pp.setProzesseigenschaft(pe);
                            pp.setValue(pe.getWert());
                            pp.setContainer(pe.getContainer());
                        } else {
                            // clone pp
                            ProcessProperty pnew = pp.getClone(pe.getContainer());
                            pnew.setProzesseigenschaft(pe);
                            pnew.setValue(pe.getWert());
                            pnew.setContainer(pe.getContainer());
                            if (log.isDebugEnabled()) {
                                log.debug("add property B " + pp.getName() + " - " + pp.getValue() + " - " + pp.getContainer());
                            }
                            properties.add(pnew);
                        }
                    }
                }
            }
        }

        // add 'eigenschaft' to all ProcessProperties
        for (ProcessProperty pp : properties) {
            if (pp.getProzesseigenschaft() != null) {
                plist.remove(pp.getProzesseigenschaft());
            }
        }
        // create ProcessProperties to remaining 'eigenschaften'
        if (!plist.isEmpty()) {
            for (Processproperty pe : plist) {
                ProcessProperty pp = new ProcessProperty();
                pp.setProzesseigenschaft(pe);
                pp.setName(pe.getTitel());
                pp.setValue(pe.getWert());
                pp.setContainer(pe.getContainer());
                pp.setType(Type.TEXT);
                if (log.isDebugEnabled()) {
                    log.debug("add property C " + pp.getName() + " - " + pp.getValue() + " - " + pp.getContainer());
                }
                properties.add(pp);

            }
        }
        if (log.isDebugEnabled()) {
            log.debug("all properties are " + properties.size());
        }

        return properties;
    }

    public List<ProcessProperty> getProcessCreationProperties(Process process, String templateName) {
        List<ProcessProperty> properties = new ArrayList<>();

        List<HierarchicalConfiguration> propertyList = config.configurationsAt("/property");
        for (HierarchicalConfiguration prop : propertyList) {
            ProcessProperty pp = new ProcessProperty();
            // general values for property
            pp.setName(prop.getString("@name"));
            pp.setContainer(prop.getInt("@container"));
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
            } catch (Exception e) {
                // no specific configuration for this template, check if <showProcessCreation access="write" template="*" /> exists
                try {
                    templateDefinition = prop.configurationAt("/showProcessCreation[@template='*']");
                } catch (Exception e1) {
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
                // (default) value
                String defaultValue = prop.getString("/defaultvalue");
                if (Type.METADATA.equals(pp.getType())) {
                    String metadata = MetadataManager.getMetadataValue(process.getId(), defaultValue);
                    pp.setValue(metadata);
                } else {
                    pp.setValue(defaultValue);
                    pp.setReadValue("");
                }

                // possible values
                pp.getPossibleValues().addAll(Arrays.asList(prop.getStringArray("/value")));
                properties.add(pp);
            }
        }
        return properties;

    }

}
