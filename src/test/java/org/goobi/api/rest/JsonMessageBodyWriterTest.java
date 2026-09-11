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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.sub.goobi.AbstractTest;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.In;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import jakarta.ws.rs.core.MediaType;

/**
 * The OpenAPI document is consumed by the API documentation UI on restApi.xhtml, which resolves security schemes by the literal values defined in the
 * OpenAPI specification. Serializing the swagger enums with their Java names instead leaves the UI unable to render the authorization form.
 */
public class JsonMessageBodyWriterTest extends AbstractTest {

    private JsonNode write(OpenAPI openApi) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new JsonMessageBodyWriter().writeTo(openApi, OpenAPI.class, null, null, MediaType.APPLICATION_JSON_TYPE, null, out);
        return new ObjectMapper().readTree(out.toByteArray());
    }

    private OpenAPI openApiWithSecurity() {
        OpenAPI openApi = new OpenAPI();
        openApi.setComponents(new Components());
        openApi.getComponents().addSecuritySchemes("query", new SecurityScheme().type(Type.APIKEY).in(In.QUERY).name("token"));
        openApi.getComponents().addSecuritySchemes("header", new SecurityScheme().type(Type.APIKEY).in(In.HEADER).name("token"));
        openApi.setSecurity(Collections.singletonList(new SecurityRequirement().addList("query").addList("header")));
        return openApi;
    }

    @Test
    public void testSecuritySchemeTypeIsWrittenAsSpecifiedByOpenApi() throws Exception {
        JsonNode schemes = write(openApiWithSecurity()).path("components").path("securitySchemes");

        assertEquals("apiKey", schemes.path("query").path("type").asText());
        assertEquals("apiKey", schemes.path("header").path("type").asText());
    }

    @Test
    public void testSecuritySchemeLocationIsWrittenAsSpecifiedByOpenApi() throws Exception {
        JsonNode schemes = write(openApiWithSecurity()).path("components").path("securitySchemes");

        assertEquals("query", schemes.path("query").path("in").asText());
        assertEquals("header", schemes.path("header").path("in").asText());
    }

    @Test
    public void testSecurityRequirementKeepsItsSchemesWhenTheirScopeListIsEmpty() throws Exception {
        JsonNode requirement = write(openApiWithSecurity()).path("security").path(0);

        assertTrue(requirement.has("query"), "global security requirement lost the query scheme");
        assertTrue(requirement.has("header"), "global security requirement lost the header scheme");
    }

    @Test
    public void testSchemasAreWrittenWithoutSwaggerInternalFields() throws Exception {
        OpenAPI openApi = new OpenAPI();
        openApi.setComponents(new Components());
        openApi.getComponents().addSchemas("Thing", new ObjectSchema().addProperty("id", new IntegerSchema()));

        String json = write(openApi).toString();

        assertFalse(json.contains("exampleSetFlag"), "internal field exampleSetFlag leaked into the OpenAPI document");
        assertFalse(json.contains("\"types\""), "internal field types leaked into the OpenAPI document");
    }
}
