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

public class JettyServer {

    private Server server;

    @Before
    public void startServer() throws Exception {
        Server server = new Server(8080);
        
        WebAppContext wac = new AliasEnhancedWebAppContext();
        wac.setContextPath("/Goobi");

        wac.setBaseResource(new ResourceCollection(new String[] { "./webapp" }));
        wac.setResourceAlias("/webapp/WEB-INF/classes", "/classes/");
        wac.setDescriptor("./webapp/WEB-INF/web.xml");
        server.setHandler(wac);
        // TODO check javax.faces
//            wac.setConfigurationClasses(new String[] { 
//                    "org.eclipse.jetty.plus.webapp.EnvConfiguration",
//                "org.eclipse.jetty.plus.webapp.PlusConfiguration",
//               
//              
//                });
//            "org.eclipse.jetty.webapp.WebInfConfiguration", 
//            "org.eclipse.jetty.webapp.WebXmlConfiguration",
//            "org.eclipse.jetty.webapp.MetaInfConfiguration",
//            "org.eclipse.jetty.webapp.FragmentConfiguration",
//            "org.eclipse.jetty.plus.webapp.EnvConfiguration",
//            "org.eclipse.jetty.plus.webapp.PlusConfiguration",
//            "org.eclipse.jetty.webapp.JettyWebXmlConfiguration",
//            "org.eclipse.jetty.webapp.TagLibConfiguration" 

//        InputStream jettyConfFile = JettyServer.class.getResourceAsStream("jetty-env.xml");
//        XmlConfiguration config = new XmlConfiguration(jettyConfFile);
//        config.configure(server);
        MysqlConnectionPoolDataSource dataSource=new MysqlConnectionPoolDataSource();
        dataSource.setUrl("jdbc:mysql://localhost/goobi");
        dataSource.setUser("goobi");
        dataSource.setPassword("goobi");
        new Resource( "java:comp/env/goobi",dataSource);
//        
        
//        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.eclipse.jetty.jndi.InitialContextFactory");
//        System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");
//        InitialContext ic = new InitialContext();
//
//        ic.createSubcontext("java:");
//        ic.createSubcontext("java:/comp");
//        ic.createSubcontext("java:/comp/env");
//        ic.bind("java:/goobi", dataSource);
//        BasicDataSource dataSource = new BasicDataSource();
//        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
//        dataSource.setUrl("jdbc:mysql://localhost/goobi");
//        dataSource.setUsername("goobi");
//        dataSource.setPassword("goobi");

//        dataSource.setJmxName("goobi");



        
//        try {
//            org.eclipse.jetty.plus.jndi.Resource mydatasource = new org.eclipse.jetty.plus.jndi.Resource(wac, "goobi", dataSource);
////            server.setAttribute("goobi", mydatasource);
//        } catch (NamingException e) {
//            e.printStackTrace();
//        }

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
