package org.goobi.goobiScript;

import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import org.goobi.managedbeans.LoginBean;

import de.sub.goobi.forms.SessionForm;

public abstract class AbstractIGoobiScript implements IGoobiScript{
    protected List<Integer> processes;
    protected HashMap<String, String> parameters;
    protected List<GoobiScriptResult> resultList;
    protected String command;
    protected String username;
    protected GoobiScriptManager gsm;
    protected long starttime;

    @Inject
    private SessionForm sf;
    @Inject
    private LoginBean login;


    public AbstractIGoobiScript() {
        super();
    }

    @Override
    public boolean prepare(List<Integer> processes, String command, HashMap<String, String> parameters) {
        this.processes = processes;
        this.parameters = parameters;
        this.command = command;
        gsm = sf.getGsm();
        resultList = sf.getGsm().getGoobiScriptResults();
        username = login.getMyBenutzer().getNachVorname();
        starttime = System.currentTimeMillis();
        return true;
    }

    @Override
    public void execute() {
    }

}
