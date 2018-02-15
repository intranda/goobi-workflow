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
	
    public AbstractIGoobiScript() {
        super();
    }
    
    @Override
	public boolean prepare(List<Integer> processes, String command, HashMap<String, String> parameters) {
		this.processes = processes;
		this.parameters = parameters;
		this.command = command;
		SessionForm sf = (SessionForm) Helper.getManagedBeanValue("SessionForm", SessionForm.class);
		resultList = sf.getGsm().getGoobiScriptResults();
		LoginBean login = (LoginBean)  Helper.getManagedBeanValue("LoginBean", LoginBean.class);
		username = login.getMyBenutzer().getNachVorname();
		return true;
	}
	
	@Override
	public void execute() {
	}

}
