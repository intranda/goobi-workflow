/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *             - https://goobi.io
 *             - https://www.intranda.com
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
package org.goobi.goobiScript;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.goobi.production.enums.GoobiScriptResultType;
import org.omnifaces.cdi.Push;
import org.omnifaces.cdi.PushContext;
import org.omnifaces.cdi.Startup;
import org.reflections.Reflections;

import de.sub.goobi.helper.FacesContextHelper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Startup
public class GoobiScriptManager {

    @Getter
    private List<GoobiScriptResult> goobiScriptResults = Collections.synchronizedList(new ArrayList<>());
    @Getter
    @Setter
    private int showMax = 100;

    @Getter
    @Setter
    private String sort = "";

    @Getter
    private boolean hasErrors;

    private Thread workerThread;
    private GoobiScriptWorker goobiScriptWorker;
    private List<GoobiScriptResult> workList = Collections.synchronizedList(new ArrayList<>());
    private int nextScriptPointer = 0;

    private Map<String, IGoobiScript> actionToScriptImplMap;

    @Inject
    @Push
    PushContext goobiscriptUpdateChannel;
    private LocalDateTime lastPush;

    @PostConstruct
    public void init() {
        populateActionToScriptImplMap();
    }

    private void populateActionToScriptImplMap() {
        actionToScriptImplMap = new HashMap<>();
        Set<Class<? extends IGoobiScript>> myset = new Reflections("org.goobi.goobiScript.*").getSubTypesOf(IGoobiScript.class);
        for (Class<? extends IGoobiScript> cl : myset) {
            if (!Modifier.isAbstract(cl.getModifiers())) {
                try {
                    IGoobiScript gs = cl.getDeclaredConstructor().newInstance();
                    actionToScriptImplMap.put(gs.getAction(), gs);
                } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                        | NoSuchMethodException | SecurityException exception) {
                    log.warn(exception);
                }
            }
        }
    }

    /**
     * enqueues new Scripts to the list of GoobiScripts
     * 
     * @param newScripts
     */
    public void enqueueScripts(List<GoobiScriptResult> newScripts) {
        synchronized (workList) {
            workList.addAll(newScripts);
        }
        synchronized (goobiScriptResults) {
            goobiScriptResults.addAll(newScripts);
        }
    }

    /**
     * starts (or if already started continues) working on GoobiScripts
     */
    public void startWork() {
        if (workerThread == null || !workerThread.isAlive()) {
            findNextScript();
            goobiScriptWorker = new GoobiScriptWorker(this);
            workerThread = new Thread(goobiScriptWorker);
            workerThread.setDaemon(true);
            workerThread.start();
        }
    }

    protected Optional<GoobiScriptResult> getNextScript() {
        if (nextScriptPointer < 0 || nextScriptPointer >= workList.size()) {
            findNextScript();
        }
        if (nextScriptPointer >= 0 && nextScriptPointer < workList.size()) {
            synchronized (workList) {
                GoobiScriptResult result = this.workList.get(nextScriptPointer);
                nextScriptPointer++;
                return Optional.of(result);
            }
        }
        return Optional.empty();
    }

    /**
     * this pushes an update command to the user interface of all users
     * 
     * @param force forces an update, otherwise an update is sent at most every three seconds
     */
    public void pushUpdateToUsers(boolean force) {
        //check if the last update was longer than three seconds ago. Some GoobiScripts are really fast,
        //so we could end up sending updates every 2ms or so, which would put high load on the server (which we don't want)
        if (force || lastPush == null || LocalDateTime.now().minus(3l, ChronoUnit.SECONDS).isAfter(lastPush)) {
            this.hasErrors = this.goobiScriptHasResults("ERROR");
            goobiscriptUpdateChannel.send("update");
            lastPush = LocalDateTime.now();
        }
    }

    /**
     * Determines whether a worker thread is not null and alive
     * 
     * @return if GoobiScript is running
     */
    public boolean isGoobiScriptRunning() {
        return workerThread != null && workerThread.isAlive();
    }

    /**
     * Gets the GoobiScript implementation for the action. If the cache does not have an implementation, reflection is used to find new
     * implementations.
     * 
     * @param action
     * @return
     */
    public Optional<IGoobiScript> getGoobiScriptForAction(String action) {
        IGoobiScript gs = this.actionToScriptImplMap.get(action);
        if (gs == null) {
            populateActionToScriptImplMap();
        }
        gs = this.actionToScriptImplMap.get(action);
        return Optional.ofNullable(gs);
    }

    /**
     * Gets all available GoobiScripts
     * 
     * @return Collection of all available GoobiScripts
     */
    public Collection<IGoobiScript> getAvailableGoobiScripts() {
        return this.actionToScriptImplMap.values();
    }

    /**
     * reset the list of all GoobiScriptResults
     */
    public void goobiScriptResultsReset() {
        goobiScriptWorker.setShouldStop(true);
        goobiScriptResults = Collections.synchronizedList(new ArrayList<>());
        workList = Collections.synchronizedList(new ArrayList<>());
        sort = "";
        showMax = 100;
        hasErrors = false;
    }

    /**
     * get just a limited number of results
     */
    public List<GoobiScriptResult> getShortGoobiScriptResults() {
        synchronized (goobiScriptResults) {
            if (showMax > goobiScriptResults.size()) {
                return goobiScriptResults;
            } else {
                return goobiScriptResults.subList(0, showMax);
            }
        }
    }

    /**
     * Check if there are currently GoobiScripts in the list with a specific status
     * 
     * @param status one of the {@link GoobiScriptResultType} values
     * @return boolean if elements with this status exist
     */
    public int getNumberOfFinishedScripts() {
        int count = 0;
        synchronized (goobiScriptResults) {
            for (GoobiScriptResult gsr : goobiScriptResults) {
                if (gsr.getResultType() != GoobiScriptResultType.WAITING) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Check if there are currently GoobiScripts in the list with a specific status
     * 
     * @param status one of the {@link GoobiScriptResultType} values
     * @return boolean if elements with this status exist
     */
    public boolean goobiScriptHasResults(String status) {
        synchronized (goobiScriptResults) {
            for (GoobiScriptResult gsr : goobiScriptResults) {
                if (gsr.getResultType().toString().equals(status)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Download current list of all results as Excel file
     */
    public void goobiScriptResultsExcel() {
        FacesContext facesContext = FacesContextHelper.getCurrentFacesContext();
        if (!facesContext.getResponseComplete()) {
            HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
            try {
                ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
                String contentType = servletContext.getMimeType("goobiScript.xlsx");
                response.setContentType(contentType);
                response.setHeader("Content-Disposition", "attachment;filename=\"goobiScript.xlsx\"");
                ServletOutputStream out = response.getOutputStream();

                try (XSSFWorkbook workbook = new XSSFWorkbook()) {
                    XSSFSheet sheet = workbook.createSheet("GoobiScript");

                    XSSFRow rowhead = sheet.createRow((short) 0);
                    rowhead.createCell(0).setCellValue("Process ID");
                    rowhead.createCell(1).setCellValue("Process title");
                    rowhead.createCell(2).setCellValue("Command");
                    rowhead.createCell(3).setCellValue("Username");
                    rowhead.createCell(4).setCellValue("Timestamp");
                    rowhead.createCell(5).setCellValue("Result");
                    rowhead.createCell(6).setCellValue("Description");
                    rowhead.createCell(7).setCellValue("Error");

                    int count = 1;
                    synchronized (goobiScriptResults) {
                        for (GoobiScriptResult gsr : goobiScriptResults) {
                            XSSFRow row = sheet.createRow(count);
                            row.createCell(0).setCellValue(gsr.getProcessId());
                            row.createCell(1).setCellValue(gsr.getProcessTitle());
                            row.createCell(2).setCellValue(gsr.getCommand());
                            row.createCell(3).setCellValue(gsr.getUsername());
                            row.createCell(4).setCellValue(gsr.getFormattedTimestamp());
                            row.createCell(5).setCellValue(gsr.getResultType().toString());
                            // reduce length to max 2000 char to avoid 'maximum length of cell contents (text) is 32767 characters' error in xls
                            row.createCell(6)
                            .setCellValue(gsr.getResultMessage() != null && gsr.getResultMessage().length() > 2000
                            ? gsr.getResultMessage().substring(0, 2000) : gsr.getResultMessage());
                            row.createCell(7).setCellValue(gsr.getErrorText() != null && gsr.getErrorText().length() > 2000
                                    ? gsr.getErrorText().substring(0, 2000) : gsr.getErrorText());
                            count++;
                        }
                    }

                    workbook.write(out);
                    out.flush();
                    facesContext.responseComplete();
                }
            } catch (IOException exception) {
                log.error(exception);
            }
        }
    }

    /**
     * sort the list by specific value
     */
    public void goobiScriptSort() {
        synchronized (goobiScriptResults) {
            if (sort.equals("id")) {
                Collections.sort(goobiScriptResults, new SortByID(false));
            } else if (sort.equals("id desc")) {
                Collections.sort(goobiScriptResults, new SortByID(true));
            } else if (sort.equals("title")) {
                Collections.sort(goobiScriptResults, new SortByTitle(false));
            } else if (sort.equals("title desc")) {
                Collections.sort(goobiScriptResults, new SortByTitle(true));
            } else if (sort.equals("status")) {
                Collections.sort(goobiScriptResults, new SortByStatus(false));
            } else if (sort.equals("status desc")) {
                Collections.sort(goobiScriptResults, new SortByStatus(true));
            } else if (sort.equals("command")) {
                Collections.sort(goobiScriptResults, new SortByCommand(false));
            } else if (sort.equals("command desc")) {
                Collections.sort(goobiScriptResults, new SortByCommand(true));

            } else if (sort.equals("user")) {
                Collections.sort(goobiScriptResults, new SortByUser(false));
            } else if (sort.equals("user desc")) {
                Collections.sort(goobiScriptResults, new SortByUser(true));

            } else if (sort.equals("timestamp")) {
                Collections.sort(goobiScriptResults, new SortByTimestamp(false));
            } else if (sort.equals("timestamp desc")) {
                Collections.sort(goobiScriptResults, new SortByTimestamp(true));

            } else if (sort.equals("description")) {
                Collections.sort(goobiScriptResults, new SortByDescription(false));
            } else if (sort.equals("description desc")) {
                Collections.sort(goobiScriptResults, new SortByDescription(true));
            }
        }
    }

    private void findNextScript() {
        synchronized (workList) {
            nextScriptPointer = -1;
            for (int i = 0; i < workList.size(); i++) {
                if (workList.get(i).getResultType() == GoobiScriptResultType.WAITING) {
                    nextScriptPointer = i;
                    break;
                }
            }
        }
    }

    private class SortByStatus implements Comparator<GoobiScriptResult> {

        private boolean reverse = false;

        public SortByStatus(boolean reverse) {
            this.reverse = reverse;
        }

        @Override
        public int compare(GoobiScriptResult g1, GoobiScriptResult g2) {
            if (reverse) {
                return g1.getResultType().compareTo(g2.getResultType());
            } else {
                return g2.getResultType().compareTo(g1.getResultType());
            }
        }
    }

    private class SortByTitle implements Comparator<GoobiScriptResult> {
        private boolean reverse = false;

        public SortByTitle(boolean reverse) {
            this.reverse = reverse;
        }

        @Override
        public int compare(GoobiScriptResult g1, GoobiScriptResult g2) {
            if (reverse) {
                return g1.getProcessTitle().compareTo(g2.getProcessTitle());
            } else {
                return g2.getProcessTitle().compareTo(g1.getProcessTitle());
            }
        }
    }

    private class SortByID implements Comparator<GoobiScriptResult> {
        private boolean reverse = false;

        public SortByID(boolean reverse) {
            this.reverse = reverse;
        }

        @Override
        public int compare(GoobiScriptResult g1, GoobiScriptResult g2) {
            if (reverse) {
                return g1.getProcessId().compareTo(g2.getProcessId());
            } else {
                return g2.getProcessId().compareTo(g1.getProcessId());
            }
        }
    }

    private class SortByCommand implements Comparator<GoobiScriptResult> {
        private boolean reverse = false;

        public SortByCommand(boolean reverse) {
            this.reverse = reverse;
        }

        @Override
        public int compare(GoobiScriptResult g1, GoobiScriptResult g2) {
            if (reverse) {
                return g1.getCommand().compareTo(g2.getCommand());
            } else {
                return g2.getCommand().compareTo(g1.getCommand());
            }
        }
    }

    private class SortByUser implements Comparator<GoobiScriptResult> {
        private boolean reverse = false;

        public SortByUser(boolean reverse) {
            this.reverse = reverse;
        }

        @Override
        public int compare(GoobiScriptResult g1, GoobiScriptResult g2) {
            if (reverse) {
                return g1.getUsername().compareTo(g2.getUsername());
            } else {
                return g2.getUsername().compareTo(g1.getUsername());
            }
        }
    }

    private class SortByTimestamp implements Comparator<GoobiScriptResult> {
        private boolean reverse = false;

        public SortByTimestamp(boolean reverse) {
            this.reverse = reverse;
        }

        @Override
        public int compare(GoobiScriptResult g1, GoobiScriptResult g2) {
            if (reverse) {
                return g1.getTimestamp().compareTo(g2.getTimestamp());
            } else {
                return g2.getTimestamp().compareTo(g1.getTimestamp());
            }
        }
    }

    private class SortByDescription implements Comparator<GoobiScriptResult> {
        private boolean reverse = false;

        public SortByDescription(boolean reverse) {
            this.reverse = reverse;
        }

        @Override
        public int compare(GoobiScriptResult g1, GoobiScriptResult g2) {
            if (reverse) {
                return g1.getResultMessage().compareTo(g2.getResultMessage());
            } else {
                return g2.getResultMessage().compareTo(g1.getResultMessage());
            }
        }
    }

    public int getGoobiScriptResultSize() {
        synchronized (goobiScriptResults) {
            return goobiScriptResults.size();
        }
    }

}
