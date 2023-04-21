package org.goobi.api.rest;

import java.util.List;

public interface IRestAuthentication {

    public List<AuthenticationMethodDescription> getAuthenticationMethods();
}
