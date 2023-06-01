/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *          - https://goobi.io
 *          - https://www.intranda.com
 *          - https://github.com/intranda/goobi-workflow
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.goobi.api.rest;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.shiro.crypto.hash.Sha256Hash;
import org.reflections.Reflections;

import com.rometools.rome.io.impl.Base64;

import de.sub.goobi.config.ConfigurationHelper;
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

    private String tokenName;

    @Getter
    @Setter
    private String tokenHash;

    @Getter
    @Setter
    private String description;

    @Getter
    @Setter
    private List<AuthenticationMethodDescription> methods = new ArrayList<>();

    public AuthenticationToken() {
        // used in database requests
    }

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
        tokenHash = new Sha256Hash(tokenName, ConfigurationHelper.getInstance().getApiTokenSalt(), 10000).toBase64();
    }

    public String getEncodedTokenName() {
        return Base64.encode(tokenName);
    }
}
