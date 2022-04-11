package de.sub.goobi.persistence.managers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.sub.goobi.AbstractTest;

public class MySQLHelperTest extends AbstractTest {
    @Test
    public void testCheckMariadbVersion() {
        assertTrue(MySQLHelper.checkMariadbVersion("5.5.5-10.3.8-MariaDB-1:10.3.8+maria~xenial"));
        assertTrue(MySQLHelper.checkMariadbVersion("5.5.5-10.2.13-MariaDB-10.2.13+maria~stretch"));
        assertTrue(MySQLHelper.checkMariadbVersion("5.5.5-10.2.3-MariaDB-10.2.3+maria~stretch"));
        assertFalse(MySQLHelper.checkMariadbVersion("5.5.5-10.2.2-MariaDB-10.2.2+maria~stretch"));
        assertTrue(MySQLHelper.checkMariadbVersion("5.5.5-10.3.14-MariaDB-1"));
        assertFalse(MySQLHelper.checkMariadbVersion("5.7.21-0ubuntu0.16.04.1"));
        assertFalse(MySQLHelper.checkMariadbVersion("10.0.34-MariaDB-0ubuntu0.16.04.1"));
        assertFalse(MySQLHelper.checkMariadbVersion("10.2.2-MariaDB-0ubuntu0.16.04.1"));
        assertTrue(MySQLHelper.checkMariadbVersion("10.3.12-MariaDB-0ubuntu0.20.04.1"));
    }
}
