/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *             - https://goobi.io
 *             - https://www.intranda.com
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
package org.goobi.api.rest;

import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.goobi.production.GoobiVersion;

import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.In;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.servlet.ServletConfig;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;

/**
 * @author florian
 *
 */
@Path("/swagger/openapi.json")
public class OpenApiResource {

    @Context
    Application application;
    @Context
    ServletConfig servletConfig;

    @GET
    @Operation(summary = "Returns the API description", description = "Returns the description about all services in the API")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "500", description = "Internal error")
    @Produces(MediaType.APPLICATION_JSON)
    public OpenAPI getOpenApi() {
        String contextPath = servletConfig.getServletContext().getContextPath();
        return initSwagger(servletConfig, application, contextPath);
    }

    private OpenAPI initSwagger(ServletConfig servletConfig, Application application, String apiUrl) {

        try {
            SwaggerConfiguration oasConfig = new SwaggerConfiguration()
                    .prettyPrint(true)
                    .readAllResources(false)

                    .resourcePackages(Stream.of("org.goobi.api.rest").collect(Collectors.toSet()));

            OpenAPI openApi = new JaxrsOpenApiContextBuilder<>()
                    .servletConfig(servletConfig)
                    .application(application)
                    .openApiConfiguration(oasConfig)
                    .buildContext(true)
                    .read();

            //authentication
            SecurityScheme queryScheme = new SecurityScheme().type(Type.APIKEY).in(In.QUERY).name("token");
            SecurityScheme headerScheme = new SecurityScheme().type(Type.APIKEY).in(In.HEADER).name("token");
            SecurityScheme basicSchema = new SecurityScheme().type(Type.APIKEY).in(In.HEADER).name("Authorization");
            openApi.getComponents().addSecuritySchemes("query", queryScheme);
            openApi.getComponents().addSecuritySchemes("header", headerScheme);
            openApi.getComponents().addSecuritySchemes("basic", basicSchema);

            SecurityRequirement securityItem = new SecurityRequirement().addList("query").addList("header");
            openApi.setSecurity(Collections.singletonList(securityItem));

            Server server = new Server();
            server.setUrl(apiUrl);
            openApi.setServers(Collections.singletonList(server));

            openApi.setInfo(getInfo());

            return openApi;
        } catch (OpenApiConfigurationException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /**
     * @return
     */
    public Info getInfo() {
        return new Info()
                .title("Goobi workflow REST API.")
                .description("This documentation describes the Goobi workflow REST API.")
                .contact(new Contact()
                        .email("info@intranda.com"))
                .license(new License()
                        .name("GPL2 or later")
                        .url("https://github.com/intranda/goobi-workflow/blob/master/LICENSE"))
                .version(GoobiVersion.getVersion());
    }

}
