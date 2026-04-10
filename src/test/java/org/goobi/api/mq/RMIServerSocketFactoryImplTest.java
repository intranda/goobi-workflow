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
package org.goobi.api.mq;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;

import org.junit.Test;

public class RMIServerSocketFactoryImplTest {

    @Test
    public void testConstructor() throws Exception {
        InetAddress addr = InetAddress.getLoopbackAddress();
        RMIServerSocketFactoryImpl factory = new RMIServerSocketFactoryImpl(addr);
        assertNotNull(factory);
    }

    @Test
    public void testEqualsSameInstance() throws Exception {
        InetAddress addr = InetAddress.getLoopbackAddress();
        RMIServerSocketFactoryImpl factory = new RMIServerSocketFactoryImpl(addr);
        assertTrue(factory.equals(factory));
    }

    @Test
    public void testEqualsSameClass() throws Exception {
        InetAddress addr = InetAddress.getLoopbackAddress();
        RMIServerSocketFactoryImpl factory1 = new RMIServerSocketFactoryImpl(addr);
        RMIServerSocketFactoryImpl factory2 = new RMIServerSocketFactoryImpl(addr);
        assertTrue(factory1.equals(factory2));
    }

    @Test
    public void testEqualsNull() throws Exception {
        InetAddress addr = InetAddress.getLoopbackAddress();
        RMIServerSocketFactoryImpl factory = new RMIServerSocketFactoryImpl(addr);
        assertFalse(factory.equals(null));
    }

    @Test
    public void testEqualsDifferentClass() throws Exception {
        InetAddress addr = InetAddress.getLoopbackAddress();
        RMIServerSocketFactoryImpl factory = new RMIServerSocketFactoryImpl(addr);
        assertFalse("not a factory".equals(factory));
    }

    @Test
    public void testHashCode() throws Exception {
        InetAddress addr = InetAddress.getLoopbackAddress();
        RMIServerSocketFactoryImpl factory = new RMIServerSocketFactoryImpl(addr);
        assertEquals(RMIServerSocketFactoryImpl.class.hashCode(), factory.hashCode());
    }
}
