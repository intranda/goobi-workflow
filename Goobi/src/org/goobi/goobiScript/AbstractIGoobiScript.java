package org.goobi.goobiScript;

import java.util.HashMap;
import java.util.List;

import de.sub.goobi.forms.SessionForm;
import de.sub.goobi.helper.Helper;

public abstract class AbstractIGoobiScript implements IGoobiScript{
	protected List<Integer> processes;
	protected HashMap<String, String> parameters;
	protected List<GoobiScriptResult> resultList;
	protected String command;
	
    public AbstractIGoobiScript() {
        super();
    }
    
    @Override
	public void prepare(List<Integer> processes, String command, HashMap<String, String> parameters) {
		this.processes = processes;
		this.parameters = parameters;
		this.command = command;
		SessionForm sf = (SessionForm) Helper.getManagedBeanValue("#{SessionForm}");
		resultList = sf.getGsm().getGoobiScriptResults();
	}
	
	@Override
	public String execute() {
		return "";
	}

}
