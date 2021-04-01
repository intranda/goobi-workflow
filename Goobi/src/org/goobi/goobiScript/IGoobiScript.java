package org.goobi.goobiScript;

import java.util.Map;
import java.util.List;

public interface IGoobiScript {

    public abstract boolean prepare(List<Integer> processes, String command, Map<String, String> parameters);

    public abstract void execute();
    
    public abstract String getSampleCall();
    
    public boolean isVisible();
    
    public String getAction();

}