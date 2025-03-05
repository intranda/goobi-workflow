package org.goobi.production.plugin.interfaces;

import java.util.ArrayList;
import java.util.List;

import de.unigoettingen.sub.search.opac.ConfigOpac;
import de.unigoettingen.sub.search.opac.ConfigOpacCatalogue;
import de.unigoettingen.sub.search.opac.ConfigOpacDoctype;
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
 */
import ugh.dl.Fileformat;
import ugh.dl.Prefs;

public interface IOpacPlugin extends IPlugin {

    public Fileformat search(String inSuchfeld, String inSuchbegriff, ConfigOpacCatalogue coc, Prefs inPrefs) throws Exception;

    public int getHitcount();

    public String getAtstsl();

    public ConfigOpacDoctype getOpacDocType();

    public String createAtstsl(String value, String value2);

    public void setAtstsl(String createAtstsl);

    public String getGattung();

    /**
     * Set the name of the selected process template
     *
     * The default implementation does nothing with it, but it can be overwritten in the individual plugin implementation
     */

    public default void setTemplateName(String template) {
    }

    /**
     * Set the name of the selected project
     *
     * The default implementation does nothing with it, but it can be overwritten in the individual plugin implementation
     */

    public default void setProjectName(String projectName) {
    }

    /**
     * Get the url to the xhtml include to display the search options
     *
     * @return
     */

    public default String getGui() {
        return "/uii/template/includes/process/process_new_opac.xhtml";
    }

    default List<ConfigOpacCatalogue> getOpacConfiguration(String title) {
        List<ConfigOpacCatalogue> catalogues = new ArrayList<>();
        ConfigOpacCatalogue coc = ConfigOpac.getInstance().getCatalogueByName(title);
        coc.setOpacPlugin(this);
        catalogues.add(coc);
        return catalogues;
    }

    default List<ConfigOpacCatalogue> getOpacConfiguration(String workflowName, String title) {
        return getOpacConfiguration(title);
    }

}