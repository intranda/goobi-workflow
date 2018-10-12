package org.goobi.api.rest;

import java.util.List;

import lombok.Data;

@Data
public class RestEndpointConfig {
    private String path;
    private List<RestMethodConfig> methodConfigs;
    private List<String> corsOrigins;
    private List<String> corsMethods;
}
