package org.goobi.goobiScript;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.goobi.beans.Process;
import org.goobi.production.enums.GoobiScriptResultType;
import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import de.sub.goobi.persistence.managers.ProcessManager;
import io.goobi.workflow.xslt.XsltPreparatorDocket;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GoobiScriptExportDatabaseInformation extends AbstractIGoobiScript implements IGoobiScript {

    @Override
    public String getAction() {
        return "exportDatabaseInformation";
    }

    @Override
    public String getSampleCall() {
        StringBuilder sb = new StringBuilder();
        addNewActionToSampleCall(sb,
                "This GoobiScript exports all database contents of the selected Goobi process to an internal XML file that is stored in the process folder.");
        return sb.toString();
    }

    @Override
    public List<GoobiScriptResult> prepare(List<Integer> processes, String command, Map<String, String> parameters) {
        super.prepare(processes, command, parameters);

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
        Process p = ProcessManager.getProcessById(gsr.getProcessId());
        try {
            gsr.setProcessTitle(p.getTitel());
            gsr.setResultType(GoobiScriptResultType.RUNNING);
            gsr.updateTimestamp();

            Path dest = null;
            try {
                dest = Paths.get(p.getProcessDataDirectoryIgnoreSwapping(), p.getId() + "_db_export.xml");
            } catch (IOException | InterruptedException | SwapException | DAOException e) {
                log.error(e);
                gsr.setResultMessage("Cannot read process folder " + e.getMessage());
                gsr.setResultType(GoobiScriptResultType.ERROR);
            }
            OutputStream os = null;

            try {
                os = Files.newOutputStream(dest);
            } catch (IOException e) {
                log.error(e);
                gsr.setResultMessage("Cannot write into export file " + e.getMessage());
                gsr.setResultType(GoobiScriptResultType.ERROR);
            }
            try {
                Document doc = new XsltPreparatorDocket().createExtendedDocument(p);
                XMLOutputter outp = new XMLOutputter();
                outp.setFormat(Format.getPrettyFormat());
                outp.output(doc, os);
            } catch (IOException e) {
                log.error(e);
                gsr.setResultMessage("Cannot write into export file " + e.getMessage());
                gsr.setResultType(GoobiScriptResultType.ERROR);
            } finally {
                if (os != null) {
                    os.close();
                }
            }
            if (gsr.getResultType() != GoobiScriptResultType.ERROR) {
                gsr.setResultMessage("Export done successfully");
                gsr.setResultType(GoobiScriptResultType.OK);
            }

        } catch (NoSuchMethodError | Exception e) {
            gsr.setResultMessage(e.getMessage());
            gsr.setResultType(GoobiScriptResultType.ERROR);
            gsr.setErrorText(e.getMessage());
            log.error("Exception during the export of database information for id " + p.getId(), e);
        }
        gsr.updateTimestamp();
    }

}
