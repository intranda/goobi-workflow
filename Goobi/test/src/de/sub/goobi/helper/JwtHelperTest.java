package de.sub.goobi.helper;

import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.LongSupplier;

import org.junit.Test;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.SignatureVerificationException;

public class JwtHelperTest {
    @Test
    public void testValidTokenValidation()
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        String secret = "secret";
        String token = createTestToken(secret);

        Method verifyToken = JwtHelper.class.getDeclaredMethod("verifyToken", String.class, String.class, LongSupplier.class);
        verifyToken.setAccessible(true);

        //if this fails, an exception is thrown - so the test will fail too
        verifyToken.invoke(null, token, secret, (LongSupplier) System::currentTimeMillis);
    }

    @Test
    public void testTooOldTokenValidation()
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException,
            NoSuchFieldException {
        String secret = "secret";
        String token = createTestToken(secret);

        Class<JwtHelper> clazz = JwtHelper.class;

        Method verifyToken = clazz.getDeclaredMethod("verifyToken", String.class, String.class, LongSupplier.class);
        verifyToken.setAccessible(true);

        try {
            Field rotationTimeField = clazz.getDeclaredField("rotationDuration");
            rotationTimeField.setAccessible(true);
            long rotationTime = rotationTimeField.getLong(null);
            verifyToken.invoke(null, token, secret, (LongSupplier) () -> System.currentTimeMillis() + rotationTime * 3);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof SignatureVerificationException) {
                //this is expected, so we return and the test passed
                return;
            }
            throw e;
        }
        //there was no exception, so we fail the test
        fail("Too old token should have thrown an error");
    }

    private String createTestToken(String secret) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class<JwtHelper> clazz = JwtHelper.class;
        Method createSigningAlgorithm = clazz.getDeclaredMethod("createSigningAlgorithm", String.class);
        createSigningAlgorithm.setAccessible(true);

        Algorithm algo = (Algorithm) createSigningAlgorithm.invoke(null, secret);

        String token = JWT.create()
                .withIssuer("Goobi")
                .sign(algo);
        return token;
    }
}
