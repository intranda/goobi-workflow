package org.goobi.goobiScript;

import java.util.List;
import java.util.Map;

public interface IGoobiScript {

    public abstract List<GoobiScriptResult> prepare(List<Integer> processes, String command, Map<String, String> parameters);

    public abstract void execute(GoobiScriptResult gsr);

    public abstract String getSampleCall();

    public boolean isVisible();

    public String getAction();

}