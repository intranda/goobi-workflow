package org.goobi.api.rest;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("api")
public class WebApi extends Application {

    //    public WebApi() {
    //        register(MultiPartFeature.class);
    //        packages(true, "org.goobi.api.rest");
    //        packages(true, "io.swagger");
    //    }



    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put("jersey.config.server.provider.classnames", "org.glassfish.jersey.media.multipart.MultiPartFeature");
        return props;
    }
}
