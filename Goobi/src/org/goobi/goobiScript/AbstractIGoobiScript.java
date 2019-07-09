package org.goobi.goobiScript;

import java.util.HashMap;
import java.util.List;

import org.goobi.managedbeans.LoginBean;

import de.sub.goobi.forms.SessionForm;
import de.sub.goobi.helper.Helper;

public abstract class AbstractIGoobiScript implements IGoobiScript{
	protected List<Integer> processes;
	protected HashMap<String, String> parameters;
	protected List<GoobiScriptResult> resultList;
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
		SessionForm sf = (SessionForm) Helper.getManagedBeanValue("#{SessionForm}");
		gsm = sf.getGsm();
		resultList = sf.getGsm().getGoobiScriptResults();
		LoginBean login = (LoginBean) Helper.getManagedBeanValue("#{LoginForm}");
		username = login.getMyBenutzer().getNachVorname();
		starttime = System.currentTimeMillis();
		return true;
	}
	
	@Override
	public void execute() {
	}

}
