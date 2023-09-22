package org.goobi.api.rest.model;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

@XmlRootElement(name = "query")
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class RestStepQueryResource {
    private String stepname;

    public RestStepQueryResource() {
        // nothing needs to be done here
    }

    public RestStepQueryResource(String name) {
        stepname = name;
    }

}
