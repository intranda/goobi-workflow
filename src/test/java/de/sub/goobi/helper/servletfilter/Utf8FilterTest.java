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
package de.sub.goobi.helper.servletfilter;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.easymock.EasyMock;
import org.junit.Test;

import de.sub.goobi.AbstractTest;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class Utf8FilterTest extends AbstractTest {

    @Test
    public void testDoFilter() throws IOException, ServletException {
        Utf8Filter filter = new Utf8Filter();
        assertNotNull(filter);

        HttpServletRequest servletRequest = EasyMock.createMock(HttpServletRequest.class);
        HttpServletResponse servletResponse = EasyMock.createMock(HttpServletResponse.class);
        FilterChain filterChain = EasyMock.createMock(FilterChain.class);

        servletRequest.setCharacterEncoding(EasyMock.anyString());
        servletResponse.setCharacterEncoding(EasyMock.anyString());
        filterChain.doFilter(servletRequest, servletResponse);

        EasyMock.expectLastCall();

        filter.doFilter(servletRequest, servletResponse, filterChain);
    }

}
