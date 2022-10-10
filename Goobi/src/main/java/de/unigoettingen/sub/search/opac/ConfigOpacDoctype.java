package de.unigoettingen.sub.search.opac;

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
import java.util.List;
import java.util.Map;

import de.sub.goobi.helper.FacesContextHelper;
import lombok.Getter;
import lombok.Setter;

public class ConfigOpacDoctype {
    @Getter
    private String title = "";
    @Getter
    private String rulesetType = "";
    @Getter
    private String tifHeaderType = "";
    @Getter
    private boolean periodical = false;
    @Getter
    private boolean multiVolume = false;
    @Getter
    private boolean containedWork = false;
    @Getter
    private Map<String, String> labels;
    @Getter
    @Setter
    private List<String> mappings;
    @Getter
    private String rulesetChildType;

    public ConfigOpacDoctype(String inTitle, String inRulesetType, String inTifHeaderType, boolean inPeriodical, boolean inMultiVolume,
            boolean inContainedWork, Map<String, String> inLabels, List<String> inMappings, String rulesetChildType) {
        this.title = inTitle;
        this.rulesetType = inRulesetType;
        this.tifHeaderType = inTifHeaderType;
        this.periodical = inPeriodical;
        this.multiVolume = inMultiVolume;
        this.containedWork = inContainedWork;
        this.labels = inLabels;
        this.mappings = inMappings;
        this.rulesetChildType = rulesetChildType;
    }

    public String getLocalizedLabel() {
        String currentLocale = FacesContextHelper.getCurrentFacesContext().getViewRoot().getLocale().getLanguage();
        if (currentLocale != null && !currentLocale.equals("")) {
            String answer = this.labels.get(currentLocale);
            if (answer != null && !answer.equals("")) {
                return answer;
            }
        }
        return this.labels.get(this.labels.keySet().iterator().next());
    }

}
