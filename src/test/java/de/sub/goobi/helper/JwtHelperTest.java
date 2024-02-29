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

import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.LongSupplier;

import org.junit.Test;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.SignatureVerificationException;

import de.sub.goobi.AbstractTest;

public class JwtHelperTest extends AbstractTest {
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
