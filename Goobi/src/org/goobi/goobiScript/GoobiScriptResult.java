package org.goobi.goobiScript;

import java.util.Date;

import org.goobi.production.enums.GoobiScriptResultType;

import de.sub.goobi.helper.Helper;
import lombok.Data;

@Data
public class GoobiScriptResult {

    private Integer processId;
    private String processTitle;
    private String command;
    private GoobiScriptResultType resultType;
    private String resultMessage;
    private Date timestamp;
    private String username;
    private String errorText;
    private long starttime;

    public GoobiScriptResult(Integer id, String command, String username, long starttime){
        this.processId = id;
        this.command = command;
        this.processTitle = "";
        this.resultMessage = "";
        this.timestamp = new Date();
        this.username = username;
        this.resultType = GoobiScriptResultType.WAITING;
        this.starttime = starttime;
    }

    public GoobiScriptResult(Integer id, String command, String username){
        this.processId = id;
        this.command = command;
        this.processTitle = "";
        this.resultMessage = "";
        this.timestamp = new Date();
        this.username = username;
        this.resultType = GoobiScriptResultType.WAITING;
        this.starttime = System.currentTimeMillis();
    }

    public void updateTimestamp(){
        timestamp = new Date();
    }

    public String getFormattedTimestamp() {
        return Helper.getDateAsFormattedString(timestamp);
    }

}
