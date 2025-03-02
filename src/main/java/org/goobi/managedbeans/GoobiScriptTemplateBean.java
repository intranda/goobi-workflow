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
 */

package org.goobi.managedbeans;

import java.io.Serializable;

import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.goobi.goobiScript.GoobiScriptTemplate;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.GoobiScriptTemplateManager;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;

@Named
@WindowScoped
public class GoobiScriptTemplateBean extends BasicBean implements Serializable {

    private static final long serialVersionUID = -7675038257191897566L;

    private static final String RETURN_PAGE = "template_all";

    @Getter
    @Setter
    private GoobiScriptTemplate template = new GoobiScriptTemplate();

    public String Neu() {
        this.template = new GoobiScriptTemplate();
        return "template_edit";
    }

    public String Speichern() {
        try {
            GoobiScriptTemplateManager.saveGoobiScriptTemplate(template);
            return FilterKein();

        } catch (DAOException e) {
            Helper.setFehlerMeldung("fehlerNichtSpeicherbar", e.getMessage());
            return "";
        }
    }

    public String Loeschen() {
        try {
            GoobiScriptTemplateManager.deleteGoobiScriptTemplate(template);
        } catch (DAOException e) {
            Helper.setFehlerMeldung("fehlerNichtLoeschbar", e.getMessage());
            return "";
        }
        return FilterKein();
    }

    public String Cancel() {
        return RETURN_PAGE;
    }

    public String FilterKein() {
        GoobiScriptTemplateManager m = new GoobiScriptTemplateManager();
        paginator = new DatabasePaginator(sortField, filter, m, RETURN_PAGE);
        return RETURN_PAGE;
    }

    public String FilterKeinMitZurueck() {
        FilterKein();
        return this.zurueck;
    }

}
