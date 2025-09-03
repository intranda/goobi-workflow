package io.goobi.workflow.api.vocabulary;

import io.goobi.vocabulary.exception.VocabularyException;
import lombok.Getter;

@Getter
public class APIException extends RuntimeException {
    private static final long serialVersionUID = -8019383979068126463L;
    private final String url;
    private final String method;
    private final int statusCode;
    private final String reason;
    private final VocabularyException vocabularyCause;
    private final Exception cause;

    public APIException(String url, String method, int statusCode, String reason, VocabularyException vocabularyCause, Exception cause) {
        super("API call was not successful" + (statusCode >= 0 ? " with status code [" + statusCode + "]" : "")
                + ": " + method + " -> " + url + ", Reason:\n" + reason);
        this.url = url;
        this.method = method;
        this.statusCode = statusCode;
        this.reason = reason;
        this.vocabularyCause = vocabularyCause;
        this.cause = cause;
    }
}
