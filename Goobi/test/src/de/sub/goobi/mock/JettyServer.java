package de.sub.goobi.mock;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Map.Entry;

import javax.naming.NamingException;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.resource.ResourceCollection;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JettyServer {

    private Server server;

    @Before
    public void startServer() throws Exception {
        Server server = new Server(8080);

        WebAppContext wac = new AliasEnhancedWebAppContext();
        wac.setContextPath("/Goobi");

        wac.setBaseResource(new ResourceCollection(new String[] { "./webapp" }));
        wac.setResourceAlias("/webapp/WEB-INF/classes", "/classes/");
        server.setHandler(wac);

        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost/goobi");
        dataSource.setUsername("goobi");
        dataSource.setPassword("goobi");
        dataSource.setJmxName("goobi");

//        wac.setConfigurationClasses(new String[] { "org.eclipse.jetty.plus.webapp.PlusConfiguration",
//                "org.eclipse.jetty.webapp.FragmentConfiguration" });
        try {
            org.eclipse.jetty.plus.jndi.Resource mydatasource = new org.eclipse.jetty.plus.jndi.Resource(wac, "java:comp/env/goobi", dataSource);
//            server.setAttribute("java:comp/env/goobi", mydatasource);
        } catch (NamingException e) {
            e.printStackTrace();
        }

        server.setStopAtShutdown(true);
        try {
            server.start();
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void shouldRun() throws Exception {
        @SuppressWarnings("deprecation")
        HttpClient client = new DefaultHttpClient();
        HttpGet mockRequest = new HttpGet("http://localhost:8080/Goobi");
        HttpResponse mockResponse = client.execute(mockRequest);

    }

    @After
    public void shutdownServer() throws Exception {
        server.stop();
    }

}
