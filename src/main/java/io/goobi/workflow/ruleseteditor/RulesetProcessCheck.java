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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.goobi.beans.Process;

import de.sub.goobi.metadaten.MetadatenHelper;
import de.sub.goobi.persistence.managers.ProcessManager;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ugh.dl.Fileformat;
import ugh.dl.Prefs;

/**
 * Checks which processes can no longer be read with a ruleset, e.g. the unsaved content of the ruleset editor. Used by the ruleset editor, runs in
 * its own thread and keeps the first error of every process.
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
    }

    private final List<Integer> processIds;
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

    public RulesetProcessCheck(List<Integer> processIds, Prefs prefs) {
        this.processIds = processIds;
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

    @Override
    public void run() {
        try {
            for (Integer processId : processIds) {
                if (cancelled) {
                    break;
                }
                Process process = ProcessManager.getProcessById(processId);
                if (process != null) {
                    String message = readWithRuleset(process);
                    if (message != null) {
                        errors.add(new ProcessError(processId, process.getTitel(), process.getProjekt().getTitel(), message));
                    }
                }
                done++;
            }
        } finally {
            running = false;
        }
    }

    /**
     * Reads the metadata file of a process with the ruleset, the same way the metadata editor does.
     *
     * @param process the process to read
     * @return null if the file can be read, otherwise the error message
     */
    private String readWithRuleset(Process process) {
        try {
            String path = process.getMetadataFilePath();
            String type = MetadatenHelper.getMetaFileType(path);
            Fileformat fileformat = MetadatenHelper.getFileformatByName(type, process.getRegelsatz());
            if (fileformat == null) {
                return "Unknown metadata format: " + type;
            }
            fileformat.setPrefs(prefs);
            fileformat.read(path);
            return null;
        } catch (Exception e) {
            // every reason why the file cannot be read is a result here, not an error of the check
            return e.getMessage() != null ? e.getMessage() : e.toString();
        }
    }
}
