package org.goobi.api.mq;

import org.goobi.beans.DatabaseObject;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExternalCommandResult implements DatabaseObject {
    private int processId;
    private int stepId;
    private String scriptName;
    
    
    @Override
    public void lazyLoad() {
        // TODO Auto-generated method stub
        
    }
}
