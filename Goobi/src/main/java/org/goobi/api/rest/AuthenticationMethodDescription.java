package org.goobi.api.rest;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
@Getter
@Setter
public class AuthenticationMethodDescription {
    private Integer methodID;
    private Integer apiTokenId;
    @NonNull
    private String methodType;
    @NonNull
    private String description;
    @NonNull
    private String url;
    private boolean selected;
}
