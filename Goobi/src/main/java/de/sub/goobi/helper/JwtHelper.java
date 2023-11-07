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
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
package de.sub.goobi.helper;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.Map;
import java.util.function.LongSupplier;

import javax.naming.ConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.goobi.beans.Step;
import org.joda.time.DateTime;

import com.auth0.jwk.InvalidPublicKeyException;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator.Builder;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.RSAKeyProvider;

import de.sub.goobi.config.ConfigurationHelper;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class JwtHelper {

    private static final long rotationDuration = 1000l * 60l * 60l * 24l; //24 hours

    private static final String JWT_NOT_DEFINED =
            "Could not get JWT secret from configuration. Please configure the key 'jwtSecret' in the file goobi_config.properties";
    private static final String DEAULT_ISSUER = "Goobi";

    /**
     * creates a rotated token. Rotation is done by appending a timestamp
     * 
     * @param secret
     * @return
     */
    private static Algorithm createSigningAlgorithm(String secret) {
        long currentTime = System.currentTimeMillis();
        long rotationTime = (currentTime / rotationDuration) * rotationDuration;
        return Algorithm.HMAC256(secret + rotationTime);
    }

    /**
     * Verifies tokens with rotated keys. Also checks if the last rotated key is valid
     * 
     * @param token
     * @param secret
     * @return
     */
    private static DecodedJWT verifyToken(String token, String secret, LongSupplier currentMillisSupplier) {
        long currentTime = currentMillisSupplier.getAsLong();
        int maxRotations = 3;
        for (int currentRotation = 0; currentRotation < maxRotations; currentRotation++) {
            long rotationTime = ((currentTime - (rotationDuration * currentRotation)) / rotationDuration) * rotationDuration;
            try {
                return verifyTokenWithRotationTime(token, secret, rotationTime);
            } catch (JWTVerificationException e) {
                if (currentRotation == maxRotations - 1) {
                    throw e;
                }
            }
        }
        return null;
    }

    private static DecodedJWT verifyTokenWithRotationTime(String token, String secret, long lastRotationTime) {
        Algorithm algorithm = Algorithm.HMAC256(secret + lastRotationTime);
        JWTVerifier verifier = JWT.require(algorithm).withIssuer(DEAULT_ISSUER).build();
        return verifier.verify(token);
    }

    public static String createToken(Map<String, String> map, Date expiryDate) throws ConfigurationException {
        String secret = ConfigurationHelper.getInstance().getJwtSecret();
        if (secret == null) {
            throw new ConfigurationException(JWT_NOT_DEFINED);
        }
        if (map == null || map.isEmpty()) {
            throw new ConfigurationException("Could not generate token from an empty map.");
        }

        Algorithm algorithm = createSigningAlgorithm(secret);

        Builder tokenBuilder = JWT.create().withIssuer(DEAULT_ISSUER);
        for (Map.Entry<String, String> entry : map.entrySet()) {
            tokenBuilder = tokenBuilder.withClaim(entry.getKey(), entry.getValue());
        }
        return tokenBuilder.withExpiresAt(expiryDate).sign(algorithm);
    }

    public static String createToken(Map<String, String> map) throws ConfigurationException {
        Date expiryDate = new DateTime().plusHours(37).toDate();
        return createToken(map, expiryDate);
    }

    public static boolean validateToken(String token, Map<String, String> map) throws ConfigurationException {
        String secret = ConfigurationHelper.getInstance().getJwtSecret();
        if (secret == null) {
            throw new ConfigurationException(JWT_NOT_DEFINED);
        }

        if (map == null || map.isEmpty()) {
            throw new ConfigurationException("Could not validate token from an empty map.");
        }
        try {
            DecodedJWT jwt = verifyToken(token, secret, System::currentTimeMillis);
            if (jwt != null) {
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    String value = jwt.getClaim(entry.getKey()).asString();
                    if (StringUtils.isBlank(value) || !value.equals(entry.getValue())) {
                        log.debug("token rejected: parameter " + entry.getKey() + " with value " + value + " does not match " + entry.getValue());
                        return false;
                    }
                }
            } else {
                return false;
            }
        } catch (JWTVerificationException exception) {
            //Invalid signature/claims
            return false;
        }
        return true;
    }

    /**
     * Verifies the String token and returns a decoded JWT
     * 
     * @param token
     * @return
     * @throws ConfigurationException
     */
    public static DecodedJWT verifyTokenAndReturnClaims(String token) throws ConfigurationException {
        String secret = ConfigurationHelper.getInstance().getJwtSecret();
        if (secret == null) {
            throw new ConfigurationException(JWT_NOT_DEFINED);
        }
        return verifyToken(token, secret, System::currentTimeMillis);
    }

    /**
     * Creates a JSON web token that has the claims "changeStepAllowed"=true, "stepId"=step.getId() and is valid for 37 hours
     * 
     * @param step
     * @return
     * @throws ConfigurationException
     */
    public static String createChangeStepToken(Step step) throws ConfigurationException {
        String secret = ConfigurationHelper.getInstance().getJwtSecret();
        if (secret == null) {
            throw new ConfigurationException(JWT_NOT_DEFINED);
        }
        Algorithm algorithm = createSigningAlgorithm(secret);
        Date expiryDate = new DateTime().plusHours(37).toDate();
        return JWT.create()
                .withIssuer(DEAULT_ISSUER)
                .withClaim("stepId", step.getId())
                .withClaim("changeStepAllowed", true)
                .withExpiresAt(expiryDate)
                .sign(algorithm);
    }

    /**
     * Creates an API token that allows using the Goobi REST API when the pathRegex matches the API path and the used HTTP Method <br>
     * is in the methods array passed to this function.
     * 
     * @param pathRegex
     * @param methods
     * @return A signed JWT
     * @throws ConfigurationException
     */
    public static String createApiToken(String pathRegex, String[] methods) throws ConfigurationException {
        String secret = ConfigurationHelper.getInstance().getJwtSecret();
        if (secret == null) {
            throw new ConfigurationException(JWT_NOT_DEFINED);
        }
        Algorithm algorithm = createSigningAlgorithm(secret);
        Date expiryDate = new DateTime().plusHours(37).toDate();
        return JWT.create()
                .withIssuer(DEAULT_ISSUER)
                .withClaim("api_path", pathRegex)
                .withArrayClaim("api_methods", methods)
                .withExpiresAt(expiryDate)
                .sign(algorithm);
    }

    public static boolean verifyChangeStepToken(String token, Integer stepId) throws ConfigurationException {
        String secret = ConfigurationHelper.getInstance().getJwtSecret();
        if (secret == null) {
            throw new ConfigurationException(JWT_NOT_DEFINED);
        }
        try {
            DecodedJWT jwt = verifyToken(token, secret, System::currentTimeMillis);
            if (jwt == null) {
                return false;
            }

            Integer claimId = jwt.getClaim("stepId").asInt();
            if (claimId == null || !stepId.equals(claimId)) {
                log.debug("token rejected: step IDs do not match");
                return false;
            }
            Boolean changeStepAllowed = jwt.getClaim("changeStepAllowed").asBoolean();
            if (changeStepAllowed == null || !changeStepAllowed) {
                log.debug("token rejected: changing the step not allowed");
                return false;
            }
        } catch (JWTVerificationException exception) {
            //Invalid signature/claims
            return false;
        }
        return true;
    }

    public static DecodedJWT verifyOpenIdToken(String token) {
        RSAKeyProvider keyProvider = null;
        final ConfigurationHelper config = ConfigurationHelper.getInstance();
        try {
            final JwkProvider provider = new UrlJwkProvider(new URL(config.getOIDCJWKSet()));

            keyProvider = new RSAKeyProvider() {
                @Override
                public RSAPublicKey getPublicKeyById(String kid) {
                    //Received 'kid' value might be null if it wasn't defined in the Token's header
                    PublicKey publicKey;
                    try {
                        publicKey = provider.get(kid).getPublicKey();
                        return (RSAPublicKey) publicKey;
                    } catch (InvalidPublicKeyException e) {
                        log.error(e);
                    } catch (JwkException e) {
                        log.error(e);
                    }
                    return null;
                }

                @Override
                public RSAPrivateKey getPrivateKey() {
                    return null;
                }

                @Override
                public String getPrivateKeyId() {
                    return null;
                }
            };
        } catch (MalformedURLException e1) {
            // TODO Auto-generated catch block
            log.error(e1);
        }

        DecodedJWT decodedJwt = JWT.decode(token);
        String strAlgorithm = decodedJwt.getAlgorithm();

        Algorithm algorithm = null;
        if ("RS256".equals(strAlgorithm)) {
            algorithm = Algorithm.RSA256(keyProvider);
        } else {
            log.error("JWT algorithm not supported: \"" + strAlgorithm + "\"");
            return null;
        }

        try {
            JWTVerifier verifier = JWT.require(algorithm).withIssuer(config.getOIDCIssuer()).build();
            return verifier.verify(decodedJwt);
        } catch (JWTVerificationException exception) {
            log.error(exception);
            return null;
        }
    }
}
