package org.goobi.production.plugin.interfaces;

public interface IDelayPlugin extends IStepPlugin {

    public void setDelay(long millis);
    
    public long getRemainingDelay();
    
    public boolean delayIsExhausted();
    
}
