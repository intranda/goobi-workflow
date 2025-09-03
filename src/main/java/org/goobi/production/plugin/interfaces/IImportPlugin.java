package org.goobi.production.plugin.interfaces;

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
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.goobi.production.enums.ImportType;
import org.goobi.production.importer.DocstructElement;
import org.goobi.production.importer.ImportObject;
import org.goobi.production.importer.Record;
import org.goobi.production.properties.ImportProperty;

import de.sub.goobi.forms.MassImportForm;
import de.sub.goobi.helper.exceptions.ImportPluginException;
import ugh.dl.Fileformat;
import ugh.dl.Prefs;

public interface IImportPlugin extends IPlugin, Serializable {

    void setPrefs(Prefs prefs);

    void setData(Record r);

    String getImportFolder();

    String getProcessTitle();

    List<ImportObject> generateFiles(List<Record> records);

    void setForm(MassImportForm form);

    void setImportFolder(String folder);

    List<Record> splitRecords(String records);

    List<Record> generateRecordsFromFile();

    /**
     * Create records out of the selected files. Typically the record identifiers are the same as the file names and they can be used to request a
     * catalogue
     * 
     * @param filenames List of all the files that where selected in the mass impoart mask
     * @return record list
     */
    List<Record> generateRecordsFromFilenames(List<String> filenames);

    void setFile(File importFile);

    List<String> splitIds(String ids);

    List<ImportType> getImportTypes();

    List<ImportProperty> getProperties();

    /**
     * Returns a list of all the files / objects that the plugin offers in the file multiselect box to pick from for an import. Each import decides
     * what shall be listed here (files, folders etc.)
     * 
     * @return file list
     */
    List<String> getAllFilenames();

    void deleteFiles(List<String> selectedFilenames);

    List<? extends DocstructElement> getCurrentDocStructs();

    String deleteDocstruct();

    String addDocstruct();

    List<String> getPossibleDocstructs();

    DocstructElement getDocstruct();

    void setDocstruct(DocstructElement dse);

    /**
     * should be an internal method for each plugin. Can very likely be removed from this interface. It is usually used to generate FileFormats and to
     * put these into the ImportObjects then
     * 
     * @return fileformat
     * @throws ImportPluginException
     */
    Fileformat convertData() throws ImportPluginException;

    /**
     * Option to set the selected workflow name to the plugin. This might be used to select the correct configuration.
     * 
     * @param workflowTitle
     */
    default void setWorkflowTitle(String workflowTitle) {
        // the default implementation ignores this call
    }

    /**
     * Get all plugin instances. The default implementation returns the plugin name only-
     * 
     * @return plugin list
     */

    default List<String> getPluginNames() {
        List<String> list = new ArrayList<>();
        list.add(getTitle());
        return list;
    }

    /**
     * Option to set a special configuration name. This might be used to load a special configuration for the import
     * 
     * @param configuration
     */

    default void setConfigurationName(String configuration) {
        // the default implementation ignores this call
    }
}
