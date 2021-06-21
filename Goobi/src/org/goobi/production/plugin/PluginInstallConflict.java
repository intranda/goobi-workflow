package org.goobi.production.plugin;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.log4j.Log4j2;

@AllArgsConstructor
@Data
@Log4j2
public class PluginInstallConflict {
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
    private String diffMode = "show_old_and_new_file";
    private String conflictsMode = "edit_existing_file";
    private boolean fixed;

    public PluginInstallConflict(String path, ResolveTactic tactic, String existing, String uploaded) {
        this.path = path;
        this.resolveTactic = tactic;
        this.existingVersion = existing;
        this.uploadedVersion = uploaded;
        this.resetTextEditor();
        this.fixed = false;
    }

    public void setConflictsMode(String mode) {
        this.conflictsMode = mode;
    }


    public void setDiffMode(String mode) {
        this.diffMode = mode;
        log.error(this.diffMode);
    }

    public void resetTextEditor() {
        this.editedExistingVersion = this.existingVersion;
        this.editedUploadedVersion = this.uploadedVersion;
    }

    public void setCurrentVersion(String content) {
        if (this.conflictsMode.equals("edit_existing_file")) {
            this.editedExistingVersion = content;
        } else {
            this.editedUploadedVersion = content;
        }
    }

    public String getCurrentVersion() {
        if (this.conflictsMode.equals("edit_existing_file")) {
            return this.editedExistingVersion;
        } else {
            return this.editedUploadedVersion;
        }
    }

    /* THIS IS NON-PRODUCTIVE CODE */
    /* IT IS ONLY FOR TESTING PURPOSES */
    public void setExistingVersion(String content) {
        this.existingVersion = content;
        /**/this.archivedVersion = content;
    }

    public void fixConflict() {
        this.fixed = true;
    }

    public boolean isFixed() {
        return this.fixed;
    }

    public static enum ResolveTactic {
        unknown,
        useMaintainer,
        useCurrent,
        editedVersion
    }
}
