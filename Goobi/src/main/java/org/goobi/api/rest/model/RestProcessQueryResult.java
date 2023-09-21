package org.goobi.api.rest.model;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.goobi.beans.Process;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

@XmlRootElement(name = "query")
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class RestProcessQueryResult {
    private int results;
    private Integer[] ids;

    public RestProcessQueryResult(List<Process> processes) {
        if (processes == null) {
            results = 0;
            ids = new Integer[] {};
        } else {
            results = processes.size();
            //            ids = new int[results];
            ids = processes.stream()
                    .map(p -> p.getId())
                    .toArray(Integer[]::new);
        }
    }
}
