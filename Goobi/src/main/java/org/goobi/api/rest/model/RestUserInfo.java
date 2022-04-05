package org.goobi.api.rest.model;

import lombok.Data;

@Data
public class RestUserInfo {
    private String user;
    private String address;
    private String browser;
    private String created;
    private String last;

}
