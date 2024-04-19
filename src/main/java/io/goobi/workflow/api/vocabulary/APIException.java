package io.goobi.workflow.api.vocabulary;

import lombok.Getter;

@Getter
public class APIException extends RuntimeException {
    private final String url;
    private final String method;
    private final int statusCode;
    private final String reason;

    public APIException(String url, String method, int statusCode, String reason) {
        super("API call was not successful with status code [" + statusCode + "]: " + method + " -> " + url + ", Reason:\n" + reason);
        this.url = url;
        this.method = method;
        this.statusCode = statusCode;
        this.reason = reason;
    }
}
