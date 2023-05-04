package org.goobi.api.display;

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
import java.util.ArrayList;
import java.util.List;

import org.goobi.api.display.enums.DisplayType;
import org.goobi.api.display.helper.ConfigDisplayRules;
import org.goobi.beans.Process;

import lombok.Getter;
import lombok.Setter;
import ugh.dl.MetadataType;

public class DisplayCase {
    @Getter
    private DisplayType displayType = null;
    @Getter
    @Setter
    private List<Item> itemList;
    private ConfigDisplayRules configDisplay;
    private Process myProcess;
    private String metaName;

    /**
     * Initializes a display case object and sets all member fields.
     *
     * @param process The process that should be assigned to this display case
     * @param metaType The meta type that should be assigned to this display case
     */
    public DisplayCase(Process process, MetadataType metaType) {
        if (metaType.getIsPerson()) {
            this.displayType = DisplayType.person;
        } else {
            this.initializeDisplayCase(process, metaType.getName());
        }
    }

    /**
     * Initializes a display case object and sets all member fields.
     *
     * @deprecated This constructor should not be used anymore because the bind value is not used.
     *
     * @param process The process that should be assigned to this display case
     * @param bind The bind value - is not used anymore
     * @param metaType The meta type that should be assigned to this display case
     */
    @Deprecated(since = "23.05", forRemoval = true)
    public DisplayCase(Process process, String bind, String metaType) {
        this.initializeDisplayCase(process, metaType);
    }

    /**
     * Initializes all member fields of this display case object. This method is used by the DisplayCase constructors.
     *
     * @param process The process that should be assigned to this display case
     * @param metaType The meta type that should be assigned to this display case
     */
    private void initializeDisplayCase(Process process, String metaType) {
        this.myProcess = process;
        this.metaName = metaType;
        this.itemList = new ArrayList<>();
        try {
            this.configDisplay = ConfigDisplayRules.getInstance();
            if (this.configDisplay != null) {
                String projectTitle = this.myProcess.getProjekt().getTitel();
                this.displayType = this.configDisplay.getElementTypeByName(projectTitle, this.metaName);
                this.itemList = this.configDisplay.getItemsByNameAndType(projectTitle, this.metaName, this.displayType);
            } else {
                // no ruleset file
                this.setDefaultValues();
            }
        } catch (Exception e) {
            // incorrect ruleset file
            this.setDefaultValues();
        }
    }

    /**
     * Initializes the display type and the item list with default values.
     */
    private void setDefaultValues() {
        this.displayType = DisplayType.getByTitle("input");
        this.itemList.add(new Item(this.metaName, "", false, "", ""));
    }

    public void overwriteConfiguredElement(Process process, MetadataType metaType) {
        this.configDisplay.overwriteConfiguredElement(this.myProcess.getProjekt().getTitel(), metaType.getName());
    }

}
