package org.goobi.goobiScript;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.goobi.beans.Process;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.LogType;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.VariableReplacer;
import de.sub.goobi.persistence.managers.ProcessManager;
import lombok.extern.log4j.Log4j2;
import ugh.dl.Fileformat;

@Log4j2
public class GoobiScriptCloneProcess extends AbstractIGoobiScript implements IGoobiScript {

    private static final String CONTENT_ALL = "all"; // all data completely
    private static final String CONTENT_EMPTY = "empty"; // just the metadata
    private static final String CONTENT_PART = "part"; // still unused
    private static final String CONTENT_MORE = "more"; // still unused
    
    @Override
    public String getAction() {
        return "cloneProcess";
    }

    @Override
    public String getSampleCall() {
        StringBuilder sb = new StringBuilder();
        addNewActionToSampleCall(sb, "This GoobiScript allows to clone an existing process and store it under a new name.");
        addParameterToSampleCall(sb, "title", "\"{processtitle}_copy\"",
                "Title for the new process that is created. Please notice that typically no special characters and no blanks are allowed.");
        addParameterToSampleCall(sb, "content", "all",
                "You can define here now much of the content shall be cloned. Possible values are `all` (all data, the database entry and the METS file), `empty` (no data, just the database entry and the corresponding METS file)");
        return sb.toString();
    }

    @Override
    public List<GoobiScriptResult> prepare(List<Integer> processes, String command, Map<String, String> parameters) {
        super.prepare(processes, command, parameters);

        if (parameters.get("title") == null || parameters.get("title").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "title");
            return new ArrayList<>();
        }
        if (parameters.get("content") == null || parameters.get("content").equals("")) {
            Helper.setFehlerMeldung("goobiScriptfield", "Missing parameter: ", "content");
            return new ArrayList<>();
        }

        String c = parameters.get("content");
        // if (!c.equals(CONTENT_ALL) && !c.equals(CONTENT_EMPTY) && !c.equals(CONTENT_PART) && !c.equals(CONTENT_MORE)) {
        if (!c.equals(CONTENT_ALL) && !c.equals(CONTENT_EMPTY)) {
            Helper.setFehlerMeldung("goobiScriptfield", "Wrong content parameter", "(only limited values are allowed)");
            return new ArrayList<>();
        }

        // add all valid commands to list
        List<GoobiScriptResult> newList = new ArrayList<>();
        for (Integer i : processes) {
            GoobiScriptResult gsr = new GoobiScriptResult(i, command, parameters, username, starttime);
            newList.add(gsr);
        }
        return newList;
    }

    @Override
    public void execute(GoobiScriptResult gsr) {
        Map<String, String> parameters = gsr.getParameters();
        Process p = ProcessManager.getProcessById(gsr.getProcessId());
        gsr.setProcessTitle(p.getTitel());
        gsr.setResultType(GoobiScriptResultType.RUNNING);
        gsr.updateTimestamp();
        
        String title = parameters.get("title");
        try {

            // first duplicate the process inside of the database
            Process newprocess = p.clone();
            
            // copy the files and other content over to the new process
            String c = parameters.get("content");
            if (c.equals(CONTENT_ALL)) {
                // all content
                StorageProvider.getInstance().copyDirectory(Paths.get(p.getProcessDataDirectory()), Paths.get(newprocess.getProcessDataDirectory()));
            } else {
                // just the metadata files
                StorageProvider.getInstance().copyFile(Paths.get(p.getMetadataFilePath()), Paths.get(newprocess.getMetadataFilePath()));
                if (StorageProvider.getInstance().isFileExists(Paths.get(p.getProcessDataDirectory(), "meta_anchor.xml"))) {
                    StorageProvider.getInstance().copyFile(Paths.get(p.getProcessDataDirectory(), "meta_anchor.xml"), Paths.get(newprocess.getProcessDataDirectory(), "meta_anchor.xml"));
                }
            }
            
            // get the title to be set and pipe it through the variable replacer
            Fileformat ff = p.readMetadataFile();
            VariableReplacer replacer = new VariableReplacer(ff.getDigitalDocument(), p.getRegelsatz().getPreferences(), p, null);
            title = replacer.replace(title);
            
            // assign the old process title to be able to change that and all folders correctly
            newprocess.setTitel(p.getTitel());
            newprocess.changeProcessTitle(title);
            ProcessManager.saveProcess(newprocess);
            
            // all successfull, yeah
            gsr.setResultMessage("Process '" + p.getTitel() + "' successfully cloned under the new name '" + parameters.get("title") + "' with id: " + newprocess.getId());
            gsr.setResultType(GoobiScriptResultType.OK);
        
        } catch (Exception e) {
            Helper.addMessageToProcessLog(p.getId(), LogType.ERROR,
                    "Problem while cloning the process '" + p.getTitel() + "' under the new name '" + title + "'", username);
            log.error("Problem while cloning the process '" + p.getTitel() + "' under the new name '" + title + "'", e);
            gsr.setResultMessage("Error while cloning a process: " + e.getMessage());
            gsr.setResultType(GoobiScriptResultType.ERROR);
            gsr.setErrorText(e.getMessage());
        }
        
        gsr.updateTimestamp();
    }

}
