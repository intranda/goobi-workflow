package de.sub.goobi.helper;

import java.io.IOException;
import java.io.Serializable;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.goobi.api.mail.SendMail;
import org.goobi.beans.GoobiProperty;
import org.goobi.beans.GoobiProperty.PropertyOwnerType;
import org.goobi.beans.JournalEntry;
import org.goobi.beans.JournalEntry.EntryType;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.beans.User;
import org.goobi.managedbeans.StepBean;
import org.goobi.production.enums.LogType;
import org.goobi.production.enums.PluginType;
import org.goobi.production.flow.jobs.HistoryAnalyserJob;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IExportPlugin;
import org.goobi.production.plugin.interfaces.IValidatorPlugin;
import org.goobi.production.properties.AccessCondition;
import org.goobi.production.properties.DisplayProperty;
import org.goobi.production.properties.PropertyParser;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.export.dms.ExportDms;
import de.sub.goobi.helper.enums.HistoryEventType;
import de.sub.goobi.helper.enums.PropertyType;
import de.sub.goobi.helper.enums.StepEditType;
import de.sub.goobi.helper.enums.StepStatus;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.ExportFileException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.helper.exceptions.UghHelperException;
import de.sub.goobi.metadaten.MetadatenImagesHelper;
import de.sub.goobi.metadaten.MetadatenVerifizierung;
import de.sub.goobi.persistence.managers.HistoryManager;
import de.sub.goobi.persistence.managers.JournalManager;
import de.sub.goobi.persistence.managers.MetadataManager;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.PropertyManager;
import de.sub.goobi.persistence.managers.StepManager;
import jakarta.faces.model.SelectItem;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import ugh.exceptions.DocStructHasNoTypeException;
import ugh.exceptions.UGHException;

@Log4j2
public class BatchStepHelper implements Serializable {

    private static final long serialVersionUID = -4104323465193019618L;

    @Getter
    @Setter
    private List<Step> steps;
    @Getter
    @Setter
    private Step currentStep;
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Setter
    private List<DisplayProperty> processPropertyList;
    @Getter
    @Setter
    private DisplayProperty processProperty;
    @Getter
    private Map<String, PropertyListObject> containers = new TreeMap<>();
    @Getter
    private String container;
    @Getter
    @Setter
    private String myProblemStep;
    @Getter
    @Setter
    private String mySolutionStep;
    @Getter
    @Setter
    private String problemMessage;
    @Getter
    @Setter
    private String solutionMessage;

    @Getter
    @Setter
    private String selectedErrorPropertyType;

    @Getter
    @Setter
    private String selectedSolutionPropertyType;

    @Getter
    private String processName = "";
    @Getter
    @Setter
    private String content = "";

    @Getter
    private HashMap<String, Boolean> containerAccess;

    @Getter
    @Setter
    private String script;
    private WebDav myDav = new WebDav();
    @Getter
    @Setter
    private List<String> processNameList = new ArrayList<>();
    @Getter
    private Map<String, List<String>> displayableMetadataMap;

    private StepBean sb = Helper.getBeanByClass(StepBean.class);

    public BatchStepHelper(List<Step> steps) {
        this.steps = steps;
        for (Step s : steps) {

            this.processNameList.add(s.getProzess().getTitel());
        }
        if (!steps.isEmpty()) {
            this.currentStep = steps.get(0);
            this.processName = this.currentStep.getProzess().getTitel();
            loadProcessProperties(this.currentStep);
            loadDisplayableMetadata(currentStep);
        }
    }

    public BatchStepHelper(List<Step> steps, Step inStep) {
        this.steps = steps;
        for (Step s : steps) {
            this.processNameList.add(s.getProzess().getTitel());
        }
        this.currentStep = inStep;
        this.processName = inStep.getProzess().getTitel();
        loadProcessProperties(this.currentStep);
        loadDisplayableMetadata(currentStep);
    }

    /*
     * properties
     */

    public List<DisplayProperty> getProcessProperties() {
        return this.processPropertyList;
    }

    public int getPropertyListSize() {
        return this.processPropertyList.size();
    }

    public void setProcessName(String processName) {
        this.processName = processName;
        for (Step s : this.steps) {
            if (s.getProzess().getTitel().equals(processName)) {
                this.currentStep = s;
                loadProcessProperties(this.currentStep);
                loadDisplayableMetadata(currentStep);
                //try to load the same step in step-managed-bean
                sb.setMySchritt(s);
                break;
            }
        }
    }

    public void saveCurrentProperty() {
        List<DisplayProperty> ppList = getContainerProperties();
        for (DisplayProperty pp : ppList) {
            this.processProperty = pp;
            if (!this.processProperty.isValid()) {
                Helper.setFehlerMeldung("Property " + this.processProperty.getName() + " is not valid");
                return;
            }
            if (this.processProperty.getProzesseigenschaft() == null) {
                GoobiProperty pe = new GoobiProperty(PropertyOwnerType.PROCESS);
                pe.setOwner(this.currentStep.getProzess());
                this.processProperty.setProzesseigenschaft(pe);
                this.currentStep.getProzess().getEigenschaften().add(pe);
            }
            this.processProperty.transfer();

            Process p = this.currentStep.getProzess();
            List<GoobiProperty> props = p.getEigenschaftenList();
            for (GoobiProperty pe : props) {
                if (pe.getPropertyName() == null) {
                    p.getEigenschaften().remove(pe);
                }
            }
            if (!this.processProperty.getProzesseigenschaft()
                    .getOwner()
                    .getProperties()
                    .contains(this.processProperty.getProzesseigenschaft())) {
                this.processProperty.getProzesseigenschaft().getOwner().getProperties().add(this.processProperty.getProzesseigenschaft());
            }
            PropertyManager.saveProperty(processProperty.getProzesseigenschaft());
        }
        Helper.setMeldung("propertiesSaved");
    }

    public void saveCurrentPropertyForAll() {
        List<DisplayProperty> ppList = getContainerProperties();
        for (DisplayProperty pp : ppList) {
            this.processProperty = pp;
            if (!this.processProperty.isValid()) {
                Helper.setFehlerMeldung("Property " + this.processProperty.getName() + " is not valid");
                return;
            }
            if (this.processProperty.getProzesseigenschaft() == null) {
                GoobiProperty pe = new GoobiProperty(PropertyOwnerType.PROCESS);
                pe.setOwner(this.currentStep.getProzess());
                this.processProperty.setProzesseigenschaft(pe);
                this.currentStep.getProzess().getEigenschaften().add(pe);
            }
            this.processProperty.transfer();

            GoobiProperty prop = processProperty.getProzesseigenschaft();
            for (Step s : this.steps) {
                Process process = s.getProzess();
                boolean match = false;
                for (GoobiProperty prpr : process.getEigenschaftenList()) {
                    if (prpr.getPropertyName() != null && prop.getPropertyName().equals(prpr.getPropertyName())
                            && prop.getContainer().equals(prpr.getContainer())) {
                        prpr.setPropertyValue(prop.getPropertyValue());
                        PropertyManager.saveProperty(prpr);
                        match = true;
                        break;
                    }
                }
                if (!match) {
                    GoobiProperty p = new GoobiProperty(PropertyOwnerType.PROCESS);
                    p.setPropertyName(prop.getPropertyName());
                    p.setPropertyValue(prop.getPropertyValue());
                    p.setContainer(prop.getContainer());
                    p.setType(prop.getType());
                    p.setOwner(process);
                    process.getEigenschaften().add(p);
                    PropertyManager.saveProperty(p);
                }
            }
        }
        Helper.setMeldung("propertiesSaved");
    }

    public int getSizeOfDisplayableMetadata() {
        return displayableMetadataMap.size();
    }

    private void loadDisplayableMetadata(Step s) {

        displayableMetadataMap = new LinkedHashMap<>();
        List<String> possibleMetadataNames = PropertyParser.getInstance().getDisplayableMetadataForStep(s);
        if (possibleMetadataNames.isEmpty()) {
            return;
        }

        for (String metadataName : possibleMetadataNames) {
            List<String> values = MetadataManager.getAllMetadataValues(s.getProzess().getId(), metadataName);
            if (!values.isEmpty()) {
                displayableMetadataMap.put(metadataName, values);
            }
        }
    }

    private void loadProcessProperties(Step s) {
        containerAccess = new HashMap<>();
        this.containers = new TreeMap<>();
        this.processPropertyList = PropertyParser.getInstance().getPropertiesForStep(s);
        List<Process> pList = new ArrayList<>();
        for (Step step : this.steps) {
            pList.add(step.getProzess());
        }
        for (DisplayProperty pt : this.processPropertyList) {
            if (!"0".equals(pt.getContainer()) && pt.getCurrentStepAccessCondition() != AccessCondition.READ) {
                containerAccess.put(pt.getContainer(), true);
            }
            if (pt.getProzesseigenschaft() == null) {
                GoobiProperty pe = new GoobiProperty(PropertyOwnerType.PROCESS);
                pe.setOwner(s.getProzess());
                pt.setProzesseigenschaft(pe);
                s.getProzess().getEigenschaften().add(pe);
                pt.transfer();
            }
            if (!this.containers.keySet().contains(pt.getContainer())) {
                PropertyListObject plo = new PropertyListObject(pt.getContainer());
                plo.addToList(pt);
                this.containers.put(pt.getContainer(), plo);
            } else {
                PropertyListObject plo = this.containers.get(pt.getContainer());
                plo.addToList(pt);
                this.containers.put(pt.getContainer(), plo);
            }
        }

        for (Process p : pList) {
            for (GoobiProperty pe : p.getEigenschaftenList()) {
                if (!this.containers.keySet().contains(pe.getContainer())) {
                    this.containers.put(pe.getContainer(), null);
                }
            }
        }
    }

    public int getContainersSize() {
        if (this.containers == null) {
            return 0;
        }
        return this.containers.size();
    }

    public List<DisplayProperty> getSortedProperties() {
        Comparator<DisplayProperty> comp = new DisplayProperty.CompareProperties();
        Collections.sort(this.processPropertyList, comp);
        return this.processPropertyList;
    }

    public List<DisplayProperty> getContainerlessProperties() {
        List<DisplayProperty> answer = new ArrayList<>();
        for (DisplayProperty pp : this.processPropertyList) {
            if ("0".equals(pp.getContainer()) && pp.getName() != null) {
                answer.add(pp);
            }
        }
        return answer;
    }

    public void setContainer(String container) {
        this.container = container;
        if (container != null && !"0".equals(container)) {
            this.processProperty = getContainerProperties().get(0);
        }
    }

    public List<DisplayProperty> getContainerProperties() {
        List<DisplayProperty> answer = new ArrayList<>();

        if (this.container != null && !"0".equals(this.container)) {
            for (DisplayProperty pp : this.processPropertyList) {
                if (this.container.equals(pp.getContainer()) && pp.getName() != null) {
                    answer.add(pp);
                }
            }
        } else {
            answer.add(this.processProperty);
        }

        return answer;
    }

    public String duplicateContainerForSingle() {
        String currentContainer = this.processProperty.getContainer();
        List<DisplayProperty> plist = new ArrayList<>();
        // search for all properties in container
        for (DisplayProperty pt : this.processPropertyList) {
            if (pt.getContainer().equals(currentContainer)) {
                plist.add(pt);
            }
        }
        int counter = 1;
        currentContainer = currentContainer.replaceAll(" - \\d+", "");
        String newContainerNumber = currentContainer;
        if (!"0".equals(currentContainer)) {
            // find new unused container number
            boolean search = true;
            while (search) {
                newContainerNumber = currentContainer + " - " + counter;
                if (!this.containers.containsKey(newContainerNumber)) {
                    search = false;
                } else {
                    counter++;
                }
            }
        }
        // clone properties
        for (DisplayProperty pt : plist) {
            DisplayProperty newProp = pt.getClone(newContainerNumber);
            this.processPropertyList.add(newProp);
            this.processProperty = newProp;
            saveCurrentProperty();
        }
        loadProcessProperties(this.currentStep);

        return "";
    }

    private void saveStep() {
        Process p = this.currentStep.getProzess();
        List<GoobiProperty> props = p.getEigenschaftenList();
        for (GoobiProperty pe : props) {
            if (pe.getPropertyName() == null) {
                p.getEigenschaften().remove(pe);
            }
        }
        try {
            ProcessManager.saveProcessInformation(this.currentStep.getProzess());
            StepManager.saveStep(currentStep);
        } catch (DAOException e) {
            log.error(e);
        }
    }

    public String duplicateContainerForAll() {
        String currentContainer = this.processProperty.getContainer();
        List<DisplayProperty> plist = new ArrayList<>();
        // search for all properties in container
        for (DisplayProperty pt : this.processPropertyList) {
            if (pt.getContainer().equals(currentContainer)) {
                plist.add(pt);
            }
        }

        int counter = 1;
        currentContainer = currentContainer.replaceAll(" - \\d+", "");
        String newContainerNumber = currentContainer;
        if (!"0".equals(currentContainer)) {
            boolean search = true;
            while (search) {
                newContainerNumber = currentContainer + " - " + counter;
                if (!this.containers.containsKey(newContainerNumber)) {
                    search = false;
                } else {
                    counter++;
                }
            }
        }
        // clone properties
        for (DisplayProperty pt : plist) {
            DisplayProperty newProp = pt.getClone(newContainerNumber);
            this.processPropertyList.add(newProp);
            this.processProperty = newProp;
            saveCurrentPropertyForAll();
        }
        loadProcessProperties(this.currentStep);
        return "";
    }

    /*
     * Error management
     */

    public String reportProblemForSingle() {

        this.myDav.uploadFromHome(this.currentStep.getProzess());
        reportProblem();
        saveStep();
        this.problemMessage = "";
        this.myProblemStep = "";
        return sb.FilterAlleStart();
    }

    public String reportProblemForAll() {
        for (Step s : this.steps) {
            this.currentStep = s;
            this.myDav.uploadFromHome(this.currentStep.getProzess());
            reportProblem();
            saveStep();
        }
        this.problemMessage = "";
        this.myProblemStep = "";
        return sb.FilterAlleStart();
    }

    private void reportProblem() {
        Date myDate = new Date();
        this.currentStep.setBearbeitungsstatusEnum(StepStatus.LOCKED);
        this.currentStep.setEditTypeEnum(StepEditType.MANUAL_SINGLE);
        this.currentStep.setPrioritaet(Integer.valueOf(10));
        currentStep.setBearbeitungszeitpunkt(new Date());
        User ben = Helper.getCurrentUser();
        if (ben != null) {
            currentStep.setBearbeitungsbenutzer(ben);
        }
        this.currentStep.setBearbeitungsbeginn(null);

        try {
            Step temp = null;
            for (Step s : this.currentStep.getProzess().getSchritteList()) {
                if (s.getTitel().equals(this.myProblemStep)) {
                    temp = s;
                }
            }
            if (temp != null) {
                SendMail.getInstance().sendMailToAssignedUser(temp, StepStatus.ERROR);
                temp.setBearbeitungsstatusEnum(StepStatus.ERROR);
                temp.setCorrectionStep();
                temp.setBearbeitungsende(new Date());
                GoobiProperty se = new GoobiProperty(PropertyOwnerType.ERROR);

                String messageText;
                if (StringUtils.isNotBlank(selectedErrorPropertyType)) {
                    messageText = sb.getErrorPropertyTypes().get(selectedErrorPropertyType).replace("{}", problemMessage);
                } else {
                    messageText = problemMessage;
                }

                se.setPropertyName(Helper.getTranslation("Korrektur notwendig"));
                if (ben == null) {
                    se.setPropertyValue("[" + this.formatter.format(new Date()) + "] " + messageText);
                } else {
                    se.setPropertyValue("[" + this.formatter.format(new Date()) + ", " + ben.getNachVorname() + "] " + messageText);
                }
                se.setType(PropertyType.MESSAGE_ERROR);
                se.setCreationDate(myDate);
                se.setOwner(temp);
                String message = Helper.getTranslation("KorrekturFuer") + " " + temp.getTitel() + ": " + messageText;
                String username;
                if (ben != null) {
                    username = ben.getNachVorname();
                } else {
                    username = "-";
                }
                JournalEntry logEntry =
                        new JournalEntry(currentStep.getProzess().getId(), new Date(), username, LogType.ERROR, message, EntryType.PROCESS);
                JournalManager.saveJournalEntry(logEntry);

                temp.getEigenschaften().add(se);
                StepManager.saveStep(temp);
                HistoryManager.addHistory(myDate, temp.getReihenfolge().doubleValue(), temp.getTitel(), HistoryEventType.stepError.getValue(),
                        temp.getProzess().getId());

                /*
                 * alle Schritte zwischen dem aktuellen und dem Korrekturschritt wieder schliessen
                 */

                List<Step> alleSchritteDazwischen =
                        StepManager.getSteps("Reihenfolge desc", " schritte.prozesseID = " + currentStep.getProzess().getId() + " AND Reihenfolge <= "
                                + currentStep.getReihenfolge() + "  AND Reihenfolge > " + temp.getReihenfolge(), 0, Integer.MAX_VALUE, null);

                for (Step step : alleSchritteDazwischen) {
                    if (!StepStatus.DEACTIVATED.equals(step.getBearbeitungsstatusEnum())) {
                        step.setBearbeitungsstatusEnum(StepStatus.LOCKED);
                    }
                    step.setCorrectionStep();
                    step.setBearbeitungsende(null);
                    GoobiProperty seg = new GoobiProperty(PropertyOwnerType.ERROR);
                    seg.setPropertyName(Helper.getTranslation("Korrektur notwendig"));
                    seg.setPropertyValue(Helper.getTranslation("KorrekturFuer") + " " + temp.getTitel() + ": " + messageText);
                    seg.setOwner(step);
                    seg.setType(PropertyType.MESSAGE_IMPORTANT);
                    seg.setCreationDate(new Date());
                    step.getEigenschaften().add(seg);
                }
            }

            /*
             * den Prozess aktualisieren, so dass der Sortierungshelper gespeichert wird
             */
            ProcessManager.saveProcessInformation(currentStep.getProzess());
        } catch (DAOException e) {
            log.error(e);
        }
    }

    public List<SelectItem> getPreviousStepsForProblemReporting() {
        List<SelectItem> answer = new ArrayList<>();
        List<Step> alleVorherigenSchritte = StepManager.getSteps("Reihenfolge desc",
                " schritte.prozesseID = " + this.currentStep.getProzess().getId() + " AND Reihenfolge < " + this.currentStep.getReihenfolge(), 0,
                Integer.MAX_VALUE, null);

        for (Step s : alleVorherigenSchritte) {
            answer.add(new SelectItem(s.getTitel(), s.getTitelMitBenutzername()));
        }
        return answer;
    }

    public int getSizeOfPreviousStepsForProblemReporting() {
        return getPreviousStepsForProblemReporting().size();
    }

    public List<SelectItem> getNextStepsForProblemSolution() {
        List<SelectItem> answer = new ArrayList<>();
        List<Step> alleNachfolgendenSchritte = StepManager.getSteps("Reihenfolge", " schritte.prozesseID = " + this.currentStep.getProzess().getId()
                + " AND Reihenfolge > " + this.currentStep.getReihenfolge() + " AND prioritaet = 10", 0, Integer.MAX_VALUE, null);

        for (Step s : alleNachfolgendenSchritte) {
            answer.add(new SelectItem(s.getTitel(), s.getTitelMitBenutzername()));
        }
        return answer;
    }

    public int getSizeOfNextStepsForProblemSolution() {
        return getNextStepsForProblemSolution().size();
    }

    public String solveProblemForSingle() {
        solveProblem();
        saveStep();
        this.solutionMessage = "";
        this.mySolutionStep = "";
        return sb.FilterAlleStart();
    }

    public String solveProblemForAll() {
        for (Step s : this.steps) {
            this.currentStep = s;
            solveProblem();
            saveStep();
        }
        this.solutionMessage = "";
        this.mySolutionStep = "";

        return sb.FilterAlleStart();
    }

    private void solveProblem() {
        Date now = new Date();
        this.myDav.uploadFromHome(this.currentStep.getProzess());
        SendMail.getInstance().sendMailToAssignedUser(currentStep, StepStatus.DONE);
        this.currentStep.setBearbeitungsstatusEnum(StepStatus.DONE);
        this.currentStep.setBearbeitungsende(now);
        this.currentStep.setEditTypeEnum(StepEditType.MANUAL_SINGLE);
        currentStep.setBearbeitungszeitpunkt(new Date());
        User ben = Helper.getCurrentUser();
        if (ben != null) {
            currentStep.setBearbeitungsbenutzer(ben);
        }

        try {
            Step temp = null;
            for (Step s : this.currentStep.getProzess().getSchritteList()) {
                if (s.getTitel().equals(this.mySolutionStep)) {
                    temp = s;
                }
            }
            String messageText;
            if (StringUtils.isNotBlank(selectedSolutionPropertyType)) {
                messageText = sb.getSolutionPropertyTypes().get(selectedSolutionPropertyType).replace("{}", solutionMessage);
            } else {
                messageText = solutionMessage;
            }

            if (temp != null) {
                /*
                 * alle Schritte zwischen dem aktuellen und dem Korrekturschritt wieder schliessen
                 */
                List<Step> alleSchritteDazwischen =
                        StepManager.getSteps("Reihenfolge", " schritte.prozesseID = " + this.currentStep.getProzess().getId() + " AND Reihenfolge >= "
                                + this.currentStep.getReihenfolge() + "  AND Reihenfolge <= " + temp.getReihenfolge(), 0, Integer.MAX_VALUE, null);

                for (Step step : alleSchritteDazwischen) {
                    if (!StepStatus.DEACTIVATED.equals(step.getBearbeitungsstatusEnum())) {
                        step.setBearbeitungsstatusEnum(StepStatus.DONE);
                    }
                    step.setBearbeitungsende(now);
                    step.setPrioritaet(Integer.valueOf(0));
                    if (step.getId().intValue() == temp.getId().intValue()) {
                        step.setBearbeitungsstatusEnum(StepStatus.OPEN);
                        step.setCorrectionStep();
                        step.setBearbeitungsende(null);
                        step.setBearbeitungszeitpunkt(now);
                    }
                    GoobiProperty seg = new GoobiProperty(PropertyOwnerType.ERROR);
                    seg.setPropertyName(Helper.getTranslation("Korrektur durchgefuehrt"));
                    seg.setPropertyValue("[" + this.formatter.format(new Date()) + ", " + ben.getNachVorname() + "] "
                            + Helper.getTranslation("KorrekturloesungFuer") + " " + temp.getTitel() + ": " + messageText);
                    seg.setOwner(step);
                    seg.setType(PropertyType.MESSAGE_IMPORTANT);
                    seg.setCreationDate(new Date());
                    step.getEigenschaften().add(seg);
                    StepManager.saveStep(step);
                }
            }
            String message = Helper.getTranslation("KorrekturloesungFuer") + " " + temp.getTitel() + ": " + messageText;

            String username;
            if (ben != null) {
                username = ben.getNachVorname();
            } else {
                username = "-";
            }
            JournalEntry logEntry =
                    new JournalEntry(currentStep.getProzess().getId(), new Date(), username, LogType.INFO, message, EntryType.PROCESS);
            JournalManager.saveJournalEntry(logEntry);

            /*
             * den Prozess aktualisieren, so dass der Sortierungshelper gespeichert wird
             */
            ProcessManager.saveProcessInformation(currentStep.getProzess());
        } catch (DAOException e) {
            log.error(e);
        }
    }

    public void addLogEntry() {
        if (StringUtils.isNotBlank(content)) {
            User user = Helper.getCurrentUser();
            JournalEntry logEntry =
                    new JournalEntry(currentStep.getProzess().getId(), new Date(), user.getNachVorname(), LogType.USER, content, EntryType.PROCESS);
            JournalManager.saveJournalEntry(logEntry);
            currentStep.getProzess().getJournal().add(logEntry);
            this.content = "";
        }
    }

    public void addLogEntryForAll() {
        if (StringUtils.isNotBlank(content)) {
            User user = Helper.getCurrentUser();
            for (Step s : this.steps) {
                JournalEntry logEntry =
                        new JournalEntry(s.getProzess().getId(), new Date(), user.getNachVorname(), LogType.USER, content, EntryType.PROCESS);
                s.getProzess().getJournal().add(logEntry);
                JournalManager.saveJournalEntry(logEntry);
            }
            this.content = "";
        }
    }

    /*
     * actions
     */

    public void executeScript() {
        for (Step step : this.steps) {

            if (step.getAllScripts().containsKey(this.script)) {
                Step so = StepManager.getStepById(step.getId());
                String scriptPath = step.getAllScripts().get(this.script);

                new HelperSchritte().executeScriptForStepObject(so, scriptPath, false);

            }
        }
    }

    public void exportDMS() {
        for (Step step : this.steps) {
            IExportPlugin dms = null;
            if (StringUtils.isNotBlank(step.getStepPlugin())) {
                dms = (IExportPlugin) PluginLoader.getPluginByTitle(PluginType.Export, step.getStepPlugin());
                if (dms == null) {
                    log.error("Can't load export plugin, use default export");
                    Helper.setFehlerMeldung("Can't load export plugin, use default export");
                    dms = new ExportDms();
                }
            }
            if (dms == null) {
                dms = new ExportDms();
            }
            try {
                dms.startExport(step.getProzess());
            } catch (UGHException | IOException | DocStructHasNoTypeException | InterruptedException | ExportFileException | UghHelperException
                    | SwapException | DAOException e) { //NOSONAR InterruptedException must not be re-thrown as it is not running in a separate thread
                Helper.setFehlerMeldung("Error on export", e.getMessage());
                log.error(e);
            }
        }

    }

    public String abortBatchEdition() {

        for (Step s : this.steps) {

            this.myDav.uploadFromHome(s.getProzess());
            s.setBearbeitungsstatusEnum(StepStatus.OPEN);
            if (Boolean.TRUE.equals(s.isCorrectionStep())) {
                s.setBearbeitungsbeginn(null);
            }
            s.setEditTypeEnum(StepEditType.MANUAL_MULTI);
            currentStep.setBearbeitungszeitpunkt(new Date());
            User ben = Helper.getCurrentUser();
            if (ben != null) {
                currentStep.setBearbeitungsbenutzer(ben);
            }
            SendMail.getInstance().sendMailToAssignedUser(currentStep, StepStatus.OPEN);

            try {
                StepManager.saveStep(s);
            } catch (DAOException e) {
                log.error(e);
            }
        }
        return sb.FilterAlleStart();
    }

    public String finishBatchEdition() {
        HelperSchritte helper = new HelperSchritte();
        for (Step s : this.steps) {
            boolean error = false;
            if (s.getValidationPlugin() != null && s.getValidationPlugin().length() > 0) {
                IValidatorPlugin ivp = (IValidatorPlugin) PluginLoader.getPluginByTitle(PluginType.Validation, s.getValidationPlugin());
                if (ivp != null) {
                    ivp.setStep(s);
                    if (!ivp.validate()) {
                        error = true;
                    }
                } else {
                    Helper.setFehlerMeldung("ErrorLoadingValidationPlugin");
                }
            }

            if (s.isTypImagesSchreiben()) {
                try {
                    HistoryAnalyserJob.updateHistory(s.getProzess());
                } catch (IOException | SwapException | DAOException e) {
                    Helper.setFehlerMeldung("Error while calculation of storage and images", e);
                }
            }

            if (s.isTypBeimAbschliessenVerifizieren()) {
                if (s.isTypMetadaten() && ConfigurationHelper.getInstance().isUseMetadataValidation()) {
                    MetadatenVerifizierung mv = new MetadatenVerifizierung();
                    mv.setAutoSave(true);
                    if (!mv.validate(s.getProzess())) {
                        error = true;
                    }
                }
                if (s.isTypImagesSchreiben()) {
                    MetadatenImagesHelper mih = new MetadatenImagesHelper(null, null);
                    try {
                        if (!mih.checkIfImagesValid(s.getProzess().getTitel(), s.getProzess().getImagesOrigDirectory(false))) {
                            error = true;
                        }
                    } catch (IOException | SwapException | DAOException e) {
                        Helper.setFehlerMeldung("Error on image validation: ", e);
                    }
                }

                loadProcessProperties(s);

                for (DisplayProperty prop : processPropertyList) {

                    if (AccessCondition.WRITEREQUIRED.equals(prop.getCurrentStepAccessCondition())
                            && (prop.getValue() == null || "".equals(prop.getValue()))) {
                        String[] parameter = { prop.getName(), s.getProzess().getTitel() };
                        Helper.setFehlerMeldung(Helper.getTranslation("BatchPropertyEmpty", parameter));
                        error = true;
                    } else if (!prop.isValid()) {
                        String[] parameter = { prop.getName(), s.getProzess().getTitel() };
                        Helper.setFehlerMeldung(Helper.getTranslation("BatchPropertyValidation", parameter));
                        error = true;
                    }
                }
            }
            if (!error) {
                this.myDav.uploadFromHome(s.getProzess());
                Step so = StepManager.getStepById(s.getId());
                so.setEditTypeEnum(StepEditType.MANUAL_MULTI);
                helper.CloseStepObjectAutomatic(so);
            }
        }
        return sb.FilterAlleStart();
    }

    public List<String> getScriptnames() {
        List<String> answer = new ArrayList<>();
        answer.addAll(getCurrentStep().getAllScripts().keySet());
        return answer;
    }

    public List<String> getContainerList() {
        return new ArrayList<>(this.containers.keySet());
    }
}
