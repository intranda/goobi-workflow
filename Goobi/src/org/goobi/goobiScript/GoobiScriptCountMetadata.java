package org.goobi.goobiScript;

import java.util.HashMap;
import java.util.List;

import org.goobi.beans.Process;
import org.goobi.production.enums.GoobiScriptResultType;
import org.goobi.production.enums.LogType;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.XmlArtikelZaehlen;
import de.sub.goobi.helper.XmlArtikelZaehlen.CountType;
import de.sub.goobi.persistence.managers.ProcessManager;
import lombok.extern.log4j.Log4j;
import ugh.dl.DocStruct;

@Deprecated
@Log4j
public class GoobiScriptCountMetadata extends AbstractIGoobiScript implements IGoobiScript {

    @Override
    public boolean prepare(List<Integer> processes, String command, HashMap<String, String> parameters) {
        super.prepare(processes, command, parameters);

        // add all valid commands to list
        for (Integer i : processes) {
            GoobiScriptResult gsr = new GoobiScriptResult(i, command, username, starttime);
            resultList.add(gsr);
        }

        return true;
    }

    @Override
    public void execute() {
        CountMetadataThread et = new CountMetadataThread();
        et.start();
    }

    class CountMetadataThread extends Thread {

        @Override
        public void run() {

            // wait until there is no earlier script to be executed first
            while (gsm.getAreEarlierScriptsWaiting(starttime)) {
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    log.error("Problem while waiting for running GoobiScripts", e);
                }
            }

            XmlArtikelZaehlen zaehlen = new XmlArtikelZaehlen();

            // execute all jobs that are still in waiting state
            synchronized (resultList) {
                for (GoobiScriptResult gsr : resultList) {
                    if (gsm.getAreScriptsWaiting(command) && gsr.getResultType() == GoobiScriptResultType.WAITING
                            && gsr.getCommand().equals(command)) {
                        Process p = ProcessManager.getProcessById(gsr.getProcessId());
                        gsr.setProcessTitle(p.getTitel());
                        gsr.setResultType(GoobiScriptResultType.RUNNING);
                        gsr.updateTimestamp();

                        try {
                            DocStruct logical = p.readMetadataFile().getDigitalDocument().getLogicalDocStruct();
                            p.setSortHelperDocstructs(zaehlen.getNumberOfUghElements(logical, CountType.DOCSTRUCT));
                            p.setSortHelperMetadata(zaehlen.getNumberOfUghElements(logical, CountType.METADATA));
                            ProcessManager.saveProcess(p);
                            Helper.addMessageToProcessLog(p.getId(), LogType.DEBUG, "Metadata fields counted using GoobiScript.", username);
                            log.info("Metadata fields counted using GoobiScript for process with ID " + p.getId());
                            gsr.setResultMessage("Metadata fields counted successfully.");
                            gsr.setResultType(GoobiScriptResultType.OK);
                        } catch (Exception e) {
                            log.error(e);
                            gsr.setResultMessage("Error while counting the metadata: " + e.getMessage());
                            gsr.setResultType(GoobiScriptResultType.ERROR);
                            gsr.setErrorText(e.getMessage());
                        }
                        gsr.updateTimestamp();
                    }
                }
            }
        }
    }
}
