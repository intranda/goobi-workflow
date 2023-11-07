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
package org.goobi.production.plugin;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data

public class PluginInstallConflict implements Serializable {
    private static final long serialVersionUID = -7844460933499082596L;

    protected static final String SHOW_OLD_AND_NEW_FILE = "show_old_and_new_file";
    protected static final String SHOW_DEFAULT_AND_CUSTOM_FILE = "show_default_and_custom_file";
    protected static final String EDIT_EXISTING_FILE = "edit_existing_file";

    private String path;
    private ResolveTactic resolveTactic;
    private String archivedVersion;
    private String existingVersion;
    private String uploadedVersion;
    private String editedExistingVersion;
    private String editedUploadedVersion;
    private List<List<SpanTag>> spanTagsOldNew;
    private List<String> lineNumbersOldNew;
    private List<String> lineTypesOldNew;
    private List<List<SpanTag>> spanTagsOldOld;
    private List<String> lineNumbersOldOld;
    private List<String> lineTypesOldOld;
    private String diffMode = SHOW_OLD_AND_NEW_FILE;
    private String conflictsMode = EDIT_EXISTING_FILE;
    private boolean fixed;
    private int number;

    public PluginInstallConflict(String path, ResolveTactic tactic, String existing, String uploaded, String archived) {
        this.path = path;
        this.resolveTactic = tactic;
        this.existingVersion = existing;
        this.uploadedVersion = uploaded;
        this.archivedVersion = archived;
        this.resetTextEditor();
        this.fixed = false;
    }

    public String getFileName() {
        if (this.path.contains("/")) {
            return this.path.substring(this.path.lastIndexOf("/") + 1, this.path.length());
        } else {
            return this.path;
        }
    }

    public void resetTextEditor() {
        this.editedExistingVersion = this.existingVersion;
        this.editedUploadedVersion = this.uploadedVersion;
    }

    public void setCurrentVersion(String content) {
        if (EDIT_EXISTING_FILE.equals(this.conflictsMode)) {
            this.editedExistingVersion = content;
        } else {
            this.editedUploadedVersion = content;
        }
    }

    public String getCurrentVersion() {
        if (EDIT_EXISTING_FILE.equals(this.conflictsMode)) {
            return this.editedExistingVersion;
        } else {
            return this.editedUploadedVersion;
        }
    }

    public void fixConflict() {
        this.fixed = true;
    }

    public boolean isFixed() {
        return this.fixed;
    }

    public enum ResolveTactic {
        unknown,
        useMaintainer,
        useCurrent,
        editedVersion
    }
}
