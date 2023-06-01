package org.goobi.production.flow.helper;

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
import java.util.HashMap;
import java.util.List;

import org.goobi.beans.Step;

import de.sub.goobi.helper.enums.StepStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BatchDisplayItem implements Comparable<BatchDisplayItem> {
    private String stepTitle = "";
    private Integer stepOrder = null;
    private StepStatus stepStatus = StepStatus.DONE;
    private HashMap<String, String> scripts = new HashMap<>();
    private boolean exportDMS = false;

    public BatchDisplayItem(Step s) {
        this.stepTitle = s.getTitel();
        this.stepOrder = s.getReihenfolge();
        this.stepStatus = s.getBearbeitungsstatusEnum();
        this.scripts.putAll(s.getAllScripts());
        this.exportDMS = s.isTypExportDMS();
    }

    @Override
    public int compareTo(BatchDisplayItem o) {
        return this.getStepOrder().compareTo(o.getStepOrder());
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || !(object.getClass().equals(this.getClass()))) {
            return false;
        } else if (object == this) {
            return true;
        }
        BatchDisplayItem item = (BatchDisplayItem) (object);
        boolean equal = item.stepTitle.equals(this.stepTitle);
        equal = equal && item.stepOrder.equals(this.stepOrder);
        equal = equal && item.stepStatus.equals(this.stepStatus);
        equal = equal && item.scripts.equals(this.scripts);
        equal = equal && item.exportDMS == this.exportDMS;
        return equal;
    }

    @Override
    public int hashCode() {
        int hashCode = super.hashCode();
        hashCode = hashCode * 71 + this.stepTitle.hashCode();
        hashCode = hashCode * 73 + this.stepOrder.hashCode();
        hashCode = hashCode * 79 + this.stepStatus.hashCode();
        hashCode = hashCode * 83 + this.scripts.hashCode();
        hashCode = hashCode * (this.exportDMS ? 89 : 97);
        return hashCode;
    }

    public int getScriptSize() {
        return this.scripts.size();
    }

    public List<String> getScriptnames() {
        List<String> answer = new ArrayList<>();
        answer.addAll(this.scripts.keySet());
        return answer;
    }
}
