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

package io.goobi.workflow.ruleseteditor;

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.goobi.beans.Process;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.metadaten.MetadatenHelper;
import de.sub.goobi.persistence.managers.ProcessManager;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ugh.dl.Fileformat;
import ugh.dl.Prefs;

/**
 * Checks which processes can no longer be read with a ruleset, e.g. the unsaved content of the ruleset editor. Used by the ruleset editor, runs in
 * its own thread and keeps the first error of every process. Processes the user cannot access are checked too, but only their ruleset error is
 * kept (with the metadata folder masked), no title, project or link.
 */
public class RulesetProcessCheck implements Runnable, Serializable {

    private static final long serialVersionUID = 4425781395718023764L;

    /**
     * A process that could not be read with the ruleset.
     */
    @Getter
    @AllArgsConstructor
    public static class ProcessError implements Serializable {
        private static final long serialVersionUID = -2873625240152342587L;
        private int processId;
        private String title;
        private String project;
        private String message;
        // true if the metadata editor can open the process, i.e. the saved ruleset still fits
        private boolean openable;
        private boolean accessible;
    }

    private final List<Integer> processIds;
    private final Set<Integer> accessibleIds;
    private final transient Prefs prefs;

    @Getter
    private final int total;

    @Getter
    private volatile int done = 0;

    @Getter
    private volatile boolean running = false;

    @Getter
    private volatile boolean cancelled = false;

    private final List<ProcessError> errors = Collections.synchronizedList(new ArrayList<>());

    /**
     * @param processIds the processes to check
     * @param accessibleIds the processes the user can access, the others only show the ruleset error
     * @param prefs the ruleset to check with
     */
    public RulesetProcessCheck(List<Integer> processIds, Set<Integer> accessibleIds, Prefs prefs) {
        this.processIds = processIds;
        // read by the check thread
        this.accessibleIds = Set.copyOf(accessibleIds);
        this.prefs = prefs;
        this.total = processIds.size();
    }

    public void start() {
        running = true;
        Thread thread = new Thread(this, "ruleset-process-check");
        thread.setDaemon(true);
        thread.start();
    }

    public void cancel() {
        cancelled = true;
    }

    /**
     * @return a copy of the errors found so far, in the order they were found
     */
    public List<ProcessError> getErrors() {
        synchronized (errors) {
            return new ArrayList<>(errors);
        }
    }

    /**
     * @return a copy of the errors of the processes the user can access, in the order they were found
     */
    public List<ProcessError> getAccessibleErrors() {
        return getErrors().stream().filter(ProcessError::isAccessible).toList();
    }

    @Override
    public void run() {
        try {
            for (Integer processId : processIds) {
                if (cancelled) {
                    break;
                }
                Process process = ProcessManager.getProcessById(processId);
                if (process != null) {
                    checkProcess(processId, process);
                }
                done++;
            }
        } finally {
            running = false;
        }
    }

    private void checkProcess(int processId, Process process) {
        boolean accessible = accessibleIds.contains(processId);
        String path;
        try {
            path = process.getMetadataFilePath();
        } catch (Exception e) {
            // every reason why the file cannot be read is a result here, not an error of the check
            addError(processId, process, accessible, e.getMessage() != null ? e.getMessage() : e.toString(), null);
            return;
        }
        String message = readWithRuleset(path, prefs);
        if (message == null) {
            return;
        }
        addError(processId, process, accessible, message, path);
    }

    private void addError(int processId, Process process, boolean accessible, String message, String path) {
        if (accessible) {
            // the metadata editor reads with the saved ruleset, so it can open the process only if that one still fits
            boolean openable = path != null && readWithRuleset(path, process.getRegelsatz().getPreferences()) == null;
            errors.add(new ProcessError(processId, process.getTitel(), process.getProjekt().getTitel(), message, openable, true));
        } else {
            // nothing about the process is shown, and the message must not reveal where its metadata is stored
            errors.add(new ProcessError(processId, null, null, maskFolder(message, path), false, false));
        }
    }

    private static String maskFolder(String message, String path) {
        if (path == null) {
            // the path itself could not be determined, so there is nothing to mask and the message may name the process folder
            return Helper.getTranslation("ruleset_process_metadata_unreadable");
        }
        Path folder = Paths.get(path).getParent();
        return folder == null ? message : message.replace(folder.toString(), "\u2026");
    }

    /**
     * Reads the metadata file of a process with the ruleset, the same way the metadata editor does.
     *
     * @param path the metadata file to read
     * @param rulesetPrefs the ruleset to read with
     * @return null if the file can be read, otherwise the error message
     */
    private String readWithRuleset(String path, Prefs rulesetPrefs) {
        try {
            String type = MetadatenHelper.getMetaFileType(path);
            // not getFileformatByName: it loads the saved ruleset, which may be the broken one that is being repaired in the editor
            Fileformat fileformat = MetadatenHelper.getFileformatForPrefs(type, rulesetPrefs);
            if (fileformat == null) {
                return "Unknown metadata format: " + type;
            }
            fileformat.read(path);
            return null;
        } catch (Exception e) {
            // every reason why the file cannot be read is a result here, not an error of the check
            return e.getMessage() != null ? e.getMessage() : e.toString();
        }
    }
}
