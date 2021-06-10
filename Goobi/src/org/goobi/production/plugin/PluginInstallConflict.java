package org.goobi.production.plugin;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
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
        this.resetEditedVersion();
        this.fixed = false;
    }

    public void resetEditedVersion() {
        this.editedExistingVersion = this.existingVersion;
        this.editedUploadedVersion = this.uploadedVersion;
    }

    public void setCurrentVersion() {
        if (this.conflictsMode.equals("edit_existing_file")) {
            this.editedExistingVersion = this.currentVersion;
        } else {
            this.editedUploadedVersion = this.currentVersion;
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
