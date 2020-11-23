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
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.goobi.beans.Process;
import org.goobi.beans.Processproperty;
import org.goobi.beans.Step;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.persistence.managers.MetadataManager;

public class PropertyParser {
    private static final Logger logger = LogManager.getLogger(PropertyParser.class);

    private static XMLConfiguration config = null;

    private static PropertyParser instance = null;

    /**
     * private constructor to read the configuration file
     */

    private PropertyParser() {
        try {
            config = new XMLConfiguration();
            config.setDelimiterParsingDisabled(true);
            config.load(ConfigurationHelper.getInstance().getConfigurationFolder() + "goobi_processProperties.xml");
            config.setReloadingStrategy(new FileChangedReloadingStrategy());
            config.setExpressionEngine(new XPathExpressionEngine());
        } catch (ConfigurationException e) {
            logger.error(e);
            config = new XMLConfiguration();
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

    @SuppressWarnings("unchecked")
    public List<String> getDisplayableMetadataForStep(Step step) {
        return buildStringList(null, step);
    }

    /**
     * return the list of metadata names to display in process details
     * 
     * @param process current process
     * @return names of metadata fields
     */

    @SuppressWarnings("unchecked")
    public List<String> getDisplayableMetadataForProcess(Process process) {
        return buildStringList(process, null);
    }

    /**
     * Is needed by getDisplayableMetadataForStep() and getDisplayableMetadataForProcess().
     * It builds the String list with all needed text for the current step or process
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
            if (p.getTitel().equals("Template")) {
                workflowTitle = p.getWert();
            }
        }
        StringBuilder xpath = new StringBuilder();
        // get all metadata
        xpath.append("/metadata");
        // limit by project
        xpath.append("[not(./project) or ./project='*' or ./project='");
        //xpath.append(convertToASCII(process.getProjekt().getTitel()));
        xpath.append(process.getProjekt().getTitel().replace("'", ""));
        xpath.append("']");
        if (step != null) {
            // limit by step
            xpath.append("[./showStep/@name='");
            xpath.append(step.getTitel());
            xpath.append("']");
        }
        // limit by workflow
        if (StringUtils.isNotBlank(workflowTitle)) {
            xpath.append("[not(./workflow) or ./workflow='*' or ./workflow='");
            xpath.append(workflowTitle);
            xpath.append("']");
        } else {
            xpath.append("[not(./workflow) or ./workflow='*']");
        }
        // get name attribute
        xpath.append("/@name");

        return Arrays.asList(config.getStringArray(xpath.toString()));
    }

    /**
     * This is maybe needed to avoid unknown characters and translate them into ascii-chars.
     * 
     * @param text The text where the unknown characters should be replaced
     * @return The normalized text
     */
    public static String convertToASCII(String text) {
        String APOSTROPHES = "\u02BC";
        String SPACES = "\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007\u2008\u2009\u200A";
        String QUOTES = "\u201C\u201D";

        String apostrophes = "";
        String spaces = "";
        String quotes = "";
        // Builds sanitized versions based on length of unsanitary entries.
        for (int i = 0; i < APOSTROPHES.length(); i++) {
            apostrophes += "'";
        }
        for (int i = 0; i < SPACES.length(); i++) {
            spaces += " ";
        }
        for (int i = 0; i < QUOTES.length(); i++) {
            quotes += "\"";
        }
        String src = "\"" + APOSTROPHES + SPACES + QUOTES + "\"";
        String dest = "concat(\"" + apostrophes + spaces + "\",'" + quotes + "')";
        return "translate(" + text + ", " + src + ", " + dest + ")";
    }

    public List<ProcessProperty> getPropertiesForStep(Step mySchritt) {

        String stepTitle = mySchritt.getTitel();
        String projectTitle = mySchritt.getProzess().getProjekt().getTitel();
        String workflowTitle = "";
        ArrayList<ProcessProperty> properties = new ArrayList<>();

        // find out original workflow template
        for (Processproperty p : mySchritt.getProzess().getEigenschaften()) {
            if (p.getTitel().equals("Template")) {
                workflowTitle = p.getWert();
            }
        }

        if (mySchritt.getProzess().isIstTemplate()) {
            return properties;
        }

        //            String path = ConfigurationHelper.getInstance().getConfigurationFolder() + "goobi_processProperties.xml";
        //            XMLConfiguration config;
        //            try {
        //                config = new XMLConfiguration(path);
        //            } catch (ConfigurationException e) {
        //                logger.error(e);
        //                config = new XMLConfiguration();
        //            }
        //            config.setListDelimiter('&');
        //            config.setReloadingStrategy(new FileChangedReloadingStrategy());

        // run though all properties
        int countProperties = config.getMaxIndex("/property");
        for (int i = 0; i <= countProperties; i++) {
            int position = i + 1;
            // general values for property
            ProcessProperty pp = new ProcessProperty();
            pp.setName(config.getString("/property[" + position + "]/@name"));
            pp.setContainer(config.getInt("/property[" + position + "]/@container"));

            // projects
            int count = config.getMaxIndex("/property[" + position + "]/project");
            for (int j = 0; j <= count; j++) {

                pp.getProjects().add(config.getString("/property[" + position + "]/project[" + (j + 1) + "]"));
            }

            // workflows
            count = config.getMaxIndex("/property[" + position + "]/workflow");
            for (int j = 0; j <= count; j++) {
                pp.getWorkflows().add(config.getString("/property[" + position + "]/workflow[" + (j + 1) + "]"));
            }

            // project and workflows are configured correct?
            boolean projectOk = pp.getProjects().contains("*") || pp.getProjects().contains(projectTitle) || pp.getProjects().size() == 0;
            boolean workflowOk = pp.getWorkflows().contains("*") || pp.getWorkflows().contains(workflowTitle) || pp.getWorkflows().size() == 0;

            if (projectOk && workflowOk) {

                // showStep
                boolean containsCurrentStepTitle = false;
                count = config.getMaxIndex("/property[" + position + "]/showStep");
                for (int j = 0; j <= count; j++) {
                    ShowStepCondition ssc = new ShowStepCondition();
                    ssc.setName(config.getString("/property[" + position + "]/showStep[" + (j + 1) + "]/@name"));
                    String access = config.getString("/property[" + position + "]/showStep[" + (j + 1) + "]/@access");
                    boolean duplicate = config.getBoolean("/property[" + position + "]/showStep[" + (j + 1) + "]/@duplicate", false);
                    ssc.setAccessCondition(AccessCondition.getAccessConditionByName(access));
                    if (ssc.getName().equals(stepTitle) || ssc.getName().equals("*")) {
                        containsCurrentStepTitle = true;
                        pp.setDuplicationAllowed(duplicate);
                        pp.setCurrentStepAccessCondition(AccessCondition.getAccessConditionByName(access));
                    }

                    pp.getShowStepConditions().add(ssc);
                }

                // steptitle is configured
                if (containsCurrentStepTitle) {
                    // showProcessGroupAccessCondition
                    String groupAccess = config.getString("/property[" + position + "]/showProcessGroup/@access");
                    if (groupAccess != null) {
                        pp.setShowProcessGroupAccessCondition(AccessCondition.getAccessConditionByName(groupAccess));
                    } else {
                        pp.setShowProcessGroupAccessCondition(AccessCondition.WRITE);
                    }

                    // validation expression
                    pp.setValidation(config.getString("/property[" + position + "]/validation"));
                    // type
                    pp.setType(Type.getTypeByName(config.getString("/property[" + position + "]/type")));
                    // (default) value
                    String defaultValue = config.getString("/property[" + position + "]/defaultvalue");
                    if (pp.getType().equals(Type.METADATA)) {
                        String metadata = MetadataManager.getMetadataValue(mySchritt.getProzess().getId(), defaultValue);
                        pp.setValue(metadata);
                    } else {
                        pp.setValue(defaultValue);
                        pp.setReadValue("");
                    }

                    // possible values
                    count = config.getMaxIndex("/property[" + position + "]/value");
                    for (int j = 0; j <= count; j++) {
                        pp.getPossibleValues().add(config.getString("/property[" + position + "]/value[" + (j + 1) + "]"));
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
                if (pe.getTitel() != null) {

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
                            properties.add(pnew);
                        }
                    }
                }
            }
        }
        return properties;
    }

    public List<ProcessProperty> getPropertiesForProcess(Process process) {
        //      Hibernate.initialize(process.getProjekt());
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

        // run though all properties
        int countProperties = config.getMaxIndex("/property");
        for (int i = 0; i <= countProperties; i++) {
            int position = i + 1;
            // general values for property
            ProcessProperty pp = new ProcessProperty();
            pp.setName(config.getString("/property[" + position + "]/@name"));
            pp.setContainer(config.getInt("/property[" + position + "]/@container"));

            // projects
            int count = config.getMaxIndex("/property[" + position + "]/project");
            for (int j = 0; j <= count; j++) {
                pp.getProjects().add(config.getString("/property[" + position + "]/project[" + (j + 1) + "]"));
            }

            // project is configured
            if (pp.getProjects().contains("*") || pp.getProjects().contains(projectTitle)) {
                String groupAccess = config.getString("/property[" + position + "]/showProcessGroup[@access]");
                if (groupAccess != null) {
                    pp.setShowProcessGroupAccessCondition(AccessCondition.getAccessConditionByName(groupAccess));
                } else {
                    pp.setShowProcessGroupAccessCondition(AccessCondition.WRITE);
                }
                // validation expression
                pp.setValidation(config.getString("/property[" + position + "]/validation"));
                // type
                pp.setType(Type.getTypeByName(config.getString("/property[" + position + "]/type")));
                // (default) value
                String defaultValue = config.getString("/property[" + position + "]/defaultvalue");
                if (pp.getType().equals(Type.METADATA)) {
                    String metadata = MetadataManager.getMetadataValue(process.getId(), defaultValue);
                    pp.setValue(metadata);
                } else {
                    pp.setValue(defaultValue);
                    pp.setReadValue("");
                }

                // possible values
                count = config.getMaxIndex("/property[" + position + "]/value");
                for (int j = 0; j <= count; j++) {
                    pp.getPossibleValues().add(config.getString("/property[" + position + "]/value[" + (j + 1) + "]"));
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("add property A " + pp.getName() + " - " + pp.getValue() + " - " + pp.getContainer());
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
                            if (logger.isDebugEnabled()) {
                                logger.debug("add property B " + pp.getName() + " - " + pp.getValue() + " - " + pp.getContainer());
                            }
                            properties.add(pnew);
                        }
                    }
                }
            }
        }

        // add 'eigenschaft' to all ProcessProperties
        for (ProcessProperty pp : properties) {
            if (pp.getProzesseigenschaft() == null) {
            } else {
                plist.remove(pp.getProzesseigenschaft());
            }
        }
        // create ProcessProperties to remaining 'eigenschaften'
        if (plist.size() > 0) {
            for (Processproperty pe : plist) {
                ProcessProperty pp = new ProcessProperty();
                pp.setProzesseigenschaft(pe);
                pp.setName(pe.getTitel());
                pp.setValue(pe.getWert());
                pp.setContainer(pe.getContainer());
                pp.setType(Type.TEXT);
                if (logger.isDebugEnabled()) {
                    logger.debug("add property C " + pp.getName() + " - " + pp.getValue() + " - " + pp.getContainer());
                }
                properties.add(pp);

            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("all properties are " + properties.size());
        }

        return properties;
    }

}
