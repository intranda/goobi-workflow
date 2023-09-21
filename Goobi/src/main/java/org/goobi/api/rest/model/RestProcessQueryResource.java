package org.goobi.api.rest.model;

import java.util.Arrays;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

@XmlRootElement(name = "query")
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class RestProcessQueryResource {
    private String filter;

    public RestProcessQueryResource() {

    }

    public String[] getConditions() {
        if (StringUtils.isBlank(filter)) {
            return new String[] {};
        }

        String[] conditionsRaw = filter.split("'");
        return Arrays.stream(conditionsRaw)
                .filter(s -> StringUtils.isNotBlank(s))
                .toArray(String[]::new);
    }

}
