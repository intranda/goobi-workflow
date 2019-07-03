package org.goobi.api.mail;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class UserProjectConfiguration {


    private String projectName;

    private Integer projectId;


    private List<StepConfiguration> stepList = new ArrayList<>();


    //    @RequiredArgsConstructor
    //    @Getter
    //    public class StepConfiguration {
    //
    //        private Integer id;
    //        @NonNull
    //        private String stepName;
    //
    //        private boolean activated;
    //    }
    //
    //    public StepConfiguration newStepConfiguration( String stepName) {
    //
    //        StepConfiguration config = new StepConfiguration(stepName);
    //        stepList.add(config);
    //        return config;
    //    }

}
