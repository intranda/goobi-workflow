package org.goobi.goobiScript;

import java.util.HashMap;
import java.util.List;

import de.sub.goobi.forms.SessionForm;
import de.sub.goobi.helper.Helper;

public abstract class AbstractIGoobiScript implements IGoobiScript {

    protected List<Integer> processes;
    protected HashMap<String, String> parameters;
    protected String command;
    protected String username;
    protected GoobiScriptManager gsm;
    protected long starttime;

    public AbstractIGoobiScript() {
        super();
    }

    @Override
    public boolean prepare(List<Integer> processes, String command, HashMap<String, String> parameters) {
        this.processes = processes;
        this.parameters = parameters;
        this.command = command;
        SessionForm sf =  Helper.getSessionBean();
        gsm =sf.getGsm();
        username = Helper.getCurrentUser().getNachVorname();
        starttime = System.currentTimeMillis();
        return true;
    }

    @Override
    public void execute() {
    }

}
