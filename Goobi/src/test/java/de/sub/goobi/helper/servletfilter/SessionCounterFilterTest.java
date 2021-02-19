package de.sub.goobi.helper.servletfilter;

import static org.junit.Assert.*;

import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.helper.FacesContextHelper;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ FacesContext.class })
public class SessionCounterFilterTest {

    @Test
    public void testInit() throws ServletException {
        SessionCounterFilter filter = new SessionCounterFilter();
        assertNotNull(filter);

        FilterConfig conf = EasyMock.createMock(FilterConfig.class);
        ServletContext context = EasyMock.createMock(ServletContext.class);
        EasyMock.expect(conf.getServletContext()).andReturn(context).anyTimes();

        EasyMock.expectLastCall();
        filter.init(conf);

    }

    @Test
    public void testDoFilter() throws IOException, ServletException {
        HttpServletRequest servletRequest = EasyMock.createMock(HttpServletRequest.class);
        HttpServletResponse servletResponse = EasyMock.createMock(HttpServletResponse.class);
        FilterChain filterChain = EasyMock.createMock(FilterChain.class);
        PowerMock.mockStatic(FacesContext.class);

        FacesContext facesContext = EasyMock.createMock(FacesContext.class);
        FacesContextHelper.setFacesContext(facesContext);

        SessionCounterFilter filter = new SessionCounterFilter();
        filter.doFilter(servletRequest, servletResponse, filterChain);
    }

}
