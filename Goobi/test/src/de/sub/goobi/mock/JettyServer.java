package de.sub.goobi.mock;

import org.eclipse.jetty.plus.jndi.Resource;
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

import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;

@SuppressWarnings("deprecation")
public class JettyServer {

    private Server server;

    @Before
    public void startServer() throws Exception {
        Server server = new Server(8080);

        WebAppContext wac = new AliasEnhancedWebAppContext();
        wac.setContextPath("/Goobi");

        wac.setBaseResource(new ResourceCollection(new String[] { "webapp" }));
        wac.setResourceAlias("webapp/WEB-INF/classes", "/classes/");
        wac.setDescriptor("webapp/WEB-INF/web.xml");

        //        wac.setClassLoader(getClass().getClassLoader());
        server.setHandler(wac);

        MysqlConnectionPoolDataSource dataSource = new MysqlConnectionPoolDataSource();
        dataSource.setUrl("jdbc:mysql://localhost/goobi");
        dataSource.setUser("goobi");
        dataSource.setPassword("goobi");
        new Resource("java:comp/env/goobi", dataSource);

        server.setStopAtShutdown(true);
        try {
            server.start();

            server.join();
            wac.getServletContext().addListener(com.sun.faces.config.ConfigureListener.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void shouldRun() throws Exception {
        HttpClient client = new DefaultHttpClient();
        HttpGet mockRequest = new HttpGet("http://localhost:8090/Goobi");
        HttpResponse mockResponse = client.execute(mockRequest);

    }

    @After
    public void shutdownServer() throws Exception {
        server.stop();
    }

}
