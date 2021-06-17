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
    private String existingVersion;
    private String uploadedVersion;
    private String currentVersion;
    private String editedExistingVersion;
    private String editedUploadedVersion;
    private List<List<SpanTag>> spanTags;
    private List<String> lineNumbers;
    private List<String> lineTypes;
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
        //this.setCurrentVersion();
    }

    public void resetTextEditor() {
        this.editedExistingVersion = this.existingVersion;
        this.editedUploadedVersion = this.uploadedVersion;
    }

    public void setCurrentVersion(String content) {
        this.currentVersion = content;
        if (this.conflictsMode.equals("edit_existing_file")) {
            this.editedExistingVersion = content;
            //log.error(this.editedExistingVersion);
        } else {
            this.editedUploadedVersion = content;
            //log.error(this.editedUploadedVersion);
        }
    }

    public String getCurrentVersion() {
        if (this.conflictsMode.equals("edit_existing_file")) {
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

    public static enum ResolveTactic {
        unknown,
        useMaintainer,
        useCurrent,
        editedVersion
    }
}
