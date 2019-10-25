package de.sub.goobi.helper;

import java.util.Date;
import java.util.Map;

import javax.naming.ConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.goobi.beans.Step;
import org.joda.time.DateTime;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator.Builder;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import de.sub.goobi.config.ConfigurationHelper;
import lombok.extern.log4j.Log4j;

@Log4j
public class JwtHelper {

    public static String createToken(Map<String, String> map, Date expiryDate) throws ConfigurationException {
        String secret = ConfigurationHelper.getInstance().getJwtSecret();
        if (secret == null) {
            throw new ConfigurationException(
                    "Could not get JWT secret from configuration. Please configure the key 'jwtSecret' in the file goobi_config.properties");
        }
        if (map == null || map.isEmpty()) {
            throw new ConfigurationException("Could not generate token from an empty map.");
        }

        Algorithm algorithm = Algorithm.HMAC256(secret);

        Builder tokenBuilder = JWT.create().withIssuer("Goobi");
        for (String key : map.keySet()) {
            tokenBuilder = tokenBuilder.withClaim(key, map.get(key));
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
            throw new ConfigurationException(
                    "Could not get JWT secret from configuration. Please configure the key 'jwtSecret' in the file goobi_config.properties");
        }

        if (map == null || map.isEmpty()) {
            throw new ConfigurationException("Could not validate token from an empty map.");
        }
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm).withIssuer("Goobi").build();
            DecodedJWT jwt = verifier.verify(token);

            for (String key : map.keySet()) {
                String tokenValue = jwt.getClaim(key).asString();
                if (StringUtils.isBlank(tokenValue) || !tokenValue.equals(map.get(key))) {
                    log.debug("token rejected: parameter " + key + " with value " + tokenValue + " does not match " + map.get(key));
                    return false;
                }
            }
        } catch (JWTVerificationException exception) {
            //Invalid signature/claims
            return false;
        }
        return true;
    }

    public static String createChangeStepToken(Step step) throws ConfigurationException {
        String secret = ConfigurationHelper.getInstance().getJwtSecret();
        if (secret == null) {
            throw new ConfigurationException(
                    "Could not get JWT secret from configuration. Please configure the key 'jwtSecret' in the file goobi_config.properties");
        }
        Algorithm algorithm = Algorithm.HMAC256(secret);
        Date expiryDate = new DateTime().plusHours(37).toDate();
        String token = JWT.create()
                .withIssuer("Goobi")
                .withClaim("stepId", step.getId())
                .withClaim("changeStepAllowed", true)
                .withExpiresAt(
                        expiryDate)
                .sign(algorithm);
        return token;
    }

    public static boolean verifyChangeStepToken(String token, Integer stepId) throws ConfigurationException {
        String secret = ConfigurationHelper.getInstance().getJwtSecret();
        if (secret == null) {
            throw new ConfigurationException(
                    "Could not get JWT secret from configuration. Please configure the key 'jwtSecret' in the file goobi_config.properties");
        }
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm).withIssuer("Goobi").build();
            DecodedJWT jwt = verifier.verify(token);
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
}
