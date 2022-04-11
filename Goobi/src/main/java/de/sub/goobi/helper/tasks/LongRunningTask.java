package de.sub.goobi.helper.tasks;

import org.goobi.beans.Process;

import de.sub.goobi.helper.Helper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class LongRunningTask extends Thread {
    
    @Getter
    @Setter
    private int statusProgress = 0;
    @Getter
    private String statusMessage = "";
    @Getter
    @Setter
    private String longMessage = "";
    @Getter
    @Setter
    private String title = "MasterTask";
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private Process prozess;
    private boolean isSingleThread = true;

    public void initialize(Process inProzess) {
        this.prozess = inProzess;
    }

    public void execute() {
        this.statusProgress = 1;
        this.statusMessage = "running";
        this.isSingleThread = false;
        run();
    }

    public void cancel() {
        this.statusMessage = "stopping";
        this.interrupt();
    }

    protected void stopped() {
        this.statusMessage = "stopped";
        this.statusProgress = -1;
    }

    @Override
    public void run() {
        /*
         * --------------------- Simulierung einer lang laufenden Aufgabe
         * -------------------
         */
        for (int i = 0; i < 100; i++) {
            /*
             * prüfen, ob der Thread unterbrochen wurde, wenn ja, stopped()
             */
            if (this.isInterrupted()) {
                stopped();
                return;
            }
            /* lang dauernde Schleife zur Simulierung einer langen Aufgabe */
            for (double j = 0; j < 10000000; j++) {
            }
            setStatusProgress(i);
        }
        setStatusMessage("done");
        setStatusProgress(100);
    }

    /**
     * Setter für Statusmeldung nur für vererbte Klassen ================================================================
     */
    protected void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
        if (!this.isSingleThread) {
            Helper.setMeldung(statusMessage);
            if (log.isDebugEnabled()) {
                log.debug(statusMessage);
            }
        }
    }

}
