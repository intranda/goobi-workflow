package de.sub.goobi.helper.servletfilter;

import static org.junit.Assert.*;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.easymock.EasyMock;
import org.junit.Test;

public class Utf8FilterTest {

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
