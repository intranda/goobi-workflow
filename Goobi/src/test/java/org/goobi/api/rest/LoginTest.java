package org.goobi.api.rest;

import static org.junit.Assert.assertNotNull;

import javax.faces.push.PushContext;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;

import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.jglue.cdiunit.jaxrs.SupportJaxRs;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.net.HttpHeaders;

import de.sub.goobi.AbstractTest;

@RunWith(CdiRunner.class)
@SupportJaxRs
@AdditionalClasses(PushContext.class)
public class LoginTest extends AbstractTest {

    //    @Alternative
    //    class MockSessionForm extends SessionForm {
    //
    //    }

    @Inject
    Login webService;

    @Before
    public void setUp() {

        //        WeldInitiator weld = WeldInitiator.from(Login.class, LoginTest.class).build();

        //        Weld weld = WeldInitiator.createWeld()
        //                .addContainerLifecycleObserver(ContainerLifecycleObserver.processAnnotatedType()
        //                        .notify(pat -> pat.configureAnnotatedType()
        //                                .filterMethods(m -> m.isAnnotationPresent(Context.class))
        //                                .forEach(m -> m.add(javax.enterprise.inject.literal.InjectLiteral.INSTANCE))));

        //        Path template = Paths.get(ConfigProjectsTest.class.getClassLoader().getResource(".").getFile());
        //        Path goobiFolder = Paths.get(template.getParent().getParent().toString() + "/src/test/resources/config/goobi_config.properties"); // for junit tests in eclipse
        //        if (!Files.exists(goobiFolder)) {
        //            goobiFolder = Paths.get("target/test-classes/config/goobi_config.properties"); // to run mvn test from cli or in jenkins
        //        }
        //        ConfigurationHelper.resetConfigurationFile();
        //        ConfigurationHelper.getInstance().setParameter("EnableHeaderLogin", "true");
        //
        //        // mock HttpServletRequest
        //        HttpServletRequest servletRequest = EasyMock.createNiceMock(HttpServletRequest.class);
        //        EasyMock.expect(servletRequest.getAttribute(EasyMock.anyString())).andReturn("fixture").anyTimes();
        //        EasyMock.replay(servletRequest);
        //
        //        SessionForm sessionForm = EasyMock.createNiceMock(SessionForm.class);

        // mock response
        // mock sessionForm

    }

    @Test
    public void testConstructor() {
        Login login = new Login();
        assertNotNull(login);
    }

    //    @Test
    //    public void testApacheHeaderLogin() throws Exception {
    //
    //        Login login = new Login();
    //        login.apacheHeaderLogin();
    //
    //    }

    public static class ExampleWebService {
        @Context
        HttpServletRequest request;

        @Context
        HttpServletResponse response;

        @Context
        ServletContext context;

        @Context
        UriInfo uriInfo;

        @Context
        Request jaxRsRequest;

        @Context
        SecurityContext securityContext;

        @Context
        Providers providers;

        @Context
        HttpHeaders headers;

    }

}
