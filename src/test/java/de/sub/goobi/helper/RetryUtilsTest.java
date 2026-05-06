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
package de.sub.goobi.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

public class RetryUtilsTest {

    @Test
    public void testSuccessOnFirstCall() throws Exception {
        String result = RetryUtils.retry(new RuntimeException("should not throw"), Duration.ZERO, 3, () -> "success");
        assertEquals("success", result);
    }

    @Test
    public void testSuccessAfterFailures() throws Exception {
        AtomicInteger attempts = new AtomicInteger(0);
        String result = RetryUtils.retry(new RuntimeException("exhausted"), Duration.ZERO, 3, () -> {
            if (attempts.incrementAndGet() < 3) {
                throw new RuntimeException("transient failure");
            }
            return "recovered";
        });
        assertEquals("recovered", result);
        assertEquals(3, attempts.get());
    }

    @Test
    public void testThrowsWhenMaxRetriesExhausted() {
        assertThrows(RuntimeException.class, () -> RetryUtils.retry(new RuntimeException("exhausted"), Duration.ZERO, 3, () -> {
            throw new RuntimeException("always fails");
        }));
    }

    @Test
    public void testReturnsCorrectValue() throws Exception {
        Integer result = RetryUtils.retry(new RuntimeException("should not throw"), Duration.ZERO, 1, () -> 42);
        assertEquals(Integer.valueOf(42), result);
    }
}
