package org.goobi.api.rest;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

@XmlRootElement
public @Data class ErrorResponse {

    private String result; // error

    private String errorText;

}
