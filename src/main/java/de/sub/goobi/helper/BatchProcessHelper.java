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
package de.sub.goobi.helper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.goobi.beans.Batch;
import org.goobi.beans.GoobiProperty;
import org.goobi.beans.GoobiProperty.PropertyOwnerType;
import org.goobi.beans.Process;
import org.goobi.production.properties.DisplayProperty;
import org.goobi.production.properties.PropertyParser;

import de.sub.goobi.persistence.managers.MetadataManager;
import de.sub.goobi.persistence.managers.ProcessManager;
import de.sub.goobi.persistence.managers.PropertyManager;
import lombok.Data;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Data
@Log4j2
public class BatchProcessHelper implements Serializable {

    private static final long serialVersionUID = 8313940977308996834L;

    private List<Process> processes;
    private Process currentProcess;
    private List<DisplayProperty> processPropertyList;
    private DisplayProperty processProperty;
    private Map<String, PropertyListObject> containers = new TreeMap<>();
    private String container;
    private List<String> processNameList = new ArrayList<>();
    private String processName = "";
    private Batch batch;

    @Getter
    private Map<String, List<String>> displayableMetadataMap;

    public BatchProcessHelper(List<Process> processes, Batch batch) {
        this.batch = batch;
        this.processes = processes;
        for (Process p : processes) {

            this.processNameList.add(p.getTitel());
        }
        if (log.isDebugEnabled()) {
            log.debug("loaded batch with " + this.processes.size() + " processes.");
        }
        this.currentProcess = processes.get(0);
        this.processName = this.currentProcess.getTitel();
        loadProcessProperties(this.currentProcess);
        loadDisplayableMetadata(currentProcess);
    }

    public int getPropertyListSize() {
        return this.processPropertyList.size();
    }

    public List<DisplayProperty> getProcessProperties() {
        return this.processPropertyList;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
        for (Process s : this.processes) {
            if (s.getTitel().equals(processName)) {
                this.currentProcess = s;
                loadProcessProperties(this.currentProcess);
                loadDisplayableMetadata(currentProcess);
                break;
            }
        }
    }

    public void saveCurrentProperty() {
        List<DisplayProperty> ppList = getContainerProperties();
        for (DisplayProperty pp : ppList) {
            this.processProperty = pp;
            if (!this.processProperty.isValid()) {
                String value = Helper.getTranslation("propertyNotValid", processProperty.getName());
                Helper.setFehlerMeldung(value);
                return;
            }
            if (this.processProperty.getProzesseigenschaft() == null) {
                GoobiProperty pe = new GoobiProperty(PropertyOwnerType.PROCESS);
                pe.setOwner(this.currentProcess);
                this.processProperty.setProzesseigenschaft(pe);
                this.currentProcess.getEigenschaften().add(pe);
            }
            this.processProperty.transfer();

            Process p = this.currentProcess;
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
                pe.setOwner(this.currentProcess);
                this.processProperty.setProzesseigenschaft(pe);
                currentProcess.getEigenschaften().add(pe);
            }
            this.processProperty.transfer();

            GoobiProperty prop = processProperty.getProzesseigenschaft();
            for (Process process : this.processes) {
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
                    p.setOwnerObject(process);
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

    private void loadDisplayableMetadata(Process process) {

        displayableMetadataMap = new LinkedHashMap<>();
        List<String> possibleMetadataNames = PropertyParser.getInstance().getDisplayableMetadataForProcess(process);
        if (possibleMetadataNames.isEmpty()) {
            return;
        }

        for (String metadataName : possibleMetadataNames) {
            List<String> values = MetadataManager.getAllMetadataValues(process.getId(), metadataName);
            if (!values.isEmpty()) {
                displayableMetadataMap.put(metadataName, values);
            }
        }
    }

    private void loadProcessProperties(Process process) {
        this.containers = new TreeMap<>();
        this.processPropertyList = PropertyParser.getInstance().getPropertiesForProcess(this.currentProcess);

        for (DisplayProperty pt : this.processPropertyList) {
            if (pt.getProzesseigenschaft() == null) {
                GoobiProperty pe = new GoobiProperty(PropertyOwnerType.PROCESS);
                pe.setOwner(process);
                pt.setProzesseigenschaft(pe);
                process.getEigenschaften().add(pe);
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
        for (Process p : this.processes) {
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
            if (!"0".equals(pp.getContainer()) && pp.getName() != null) {
                answer.add(pp);
            }
        }
        return answer;
    }

    public List<String> getContainerList() {
        return new ArrayList<>(this.containers.keySet());
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
        loadProcessProperties(this.currentProcess);

        return "";
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
        loadProcessProperties(this.currentProcess);
        return "";
    }

    /**
     * store the current state of the batch in the database.
     */

    public void saveBatchDetails() {
        ProcessManager.saveBatch(batch);
    }

}
