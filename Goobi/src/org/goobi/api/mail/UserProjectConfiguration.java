package org.goobi.api.mail;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
public class UserProjectConfiguration {


    private String projectName;

    private Integer projectId;


    private List<StepConfiguration> stepList = new ArrayList<>();


    @AllArgsConstructor
    @Getter
    public class StepConfiguration {

        private boolean activated;
        private String stepName;

    }

    public StepConfiguration newStepConfiguration(boolean activated, String stepName) {

        StepConfiguration config = new StepConfiguration(activated, stepName);
        stepList.add(config);
        return config;
    }

}
