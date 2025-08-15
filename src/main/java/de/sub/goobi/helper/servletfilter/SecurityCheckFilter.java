package de.sub.goobi.helper.servletfilter;

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
import java.io.IOException;

import org.goobi.managedbeans.LoginBean;

import de.sub.goobi.config.ConfigurationHelper;
import jakarta.inject.Inject;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class SecurityCheckFilter implements Filter {

    @Inject // NOSONAR needs to be a field injection, as the been constructor does not allow arguments
    private LoginBean userBean;

    public SecurityCheckFilter() { //called once. no method arguments allowed here!
    }

    @Override
    public void init(FilterConfig conf) throws ServletException {
        // do nothing
    }

    @Override
    public void destroy() {
        // do nothing
    }

    /** Creates a new instance of SecurityCheckFilter. */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest hreq = (HttpServletRequest) request;
        String url = hreq.getRequestURI();
        String destination = "index.xhtml";
        if (url.contains("external_index.xhtml") && ConfigurationHelper.getInstance().isEnableExternalUserLogin()) {
            chain.doFilter(request, response);
        } else if ((userBean == null || userBean.getMyBenutzer() == null) && !url.contains("jakarta.faces.resource") && !url.contains("wi?")
                && !url.contains("currentUsers.xhtml") && !url.contains("logout.xhtml") && !url.contains("technicalBackground.xhtml")
                && !url.contains("mailNotificationDisabled.xhtml") && !url.contains(destination)) {
            ((HttpServletResponse) response).sendRedirect(destination);
        } else {
            chain.doFilter(request, response);
        }
    }
}
