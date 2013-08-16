package org.goobi.production.plugin.interfaces;

public interface IDelayPlugin extends IStepPlugin {

    public void setDelay(long seconds);
    
    public long getRemainingDelay();
    
    public boolean delayIsExhausted();
    
}
