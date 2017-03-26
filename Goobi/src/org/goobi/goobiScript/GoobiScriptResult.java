package org.goobi.goobiScript;

import org.goobi.production.enums.GoobiScriptResultType;

import lombok.Data;

@Data
public class GoobiScriptResult {
	
	private Integer processId;
	private String processTitle;
	private String command;
	private GoobiScriptResultType resultType;
	private String resultMessage;
	
	public GoobiScriptResult(Integer id, String command){
		this.processId = id;
		this.command = command;
		this.processTitle = "";
		this.resultMessage = "";
		this.resultType = GoobiScriptResultType.WAITING;
	}

}
