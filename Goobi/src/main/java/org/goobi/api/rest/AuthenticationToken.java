package org.goobi.api.rest;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class AuthenticationToken {

    @Getter
    @Setter
    private Integer userId;

    @Getter
    @Setter
    private Integer tokenId;

    @Getter
    @Setter
    private String tokenName;

    @Getter
    @Setter
    private String description;

    @Getter
    @Setter
    private List<AuthenticationMethodDescription> methods = new ArrayList<>();

    public AuthenticationToken(String tokenName, Integer userId) {
        this.tokenName = tokenName;
        this.userId = userId;
        // use reflection to find all configured MethodDescriptions
        Set<Class<? extends IRestAuthentication>> clazzes = new Reflections().getSubTypesOf(IRestAuthentication.class);
        for (Class<? extends IRestAuthentication> clazz : clazzes) {
            try {
                IRestAuthentication impl = clazz.getDeclaredConstructor().newInstance();
                methods.addAll(impl.getAuthenticationMethods());
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | IllegalArgumentException | InvocationTargetException
                    | SecurityException e) {
                log.error(e);
            }
        }
    }
}
