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
 */
package org.goobi.api.rest;

import org.goobi.beans.Docket;
import org.goobi.beans.Institution;
import org.goobi.beans.Process;
import org.goobi.beans.Project;
import org.goobi.beans.Ruleset;
import org.mockito.Mockito;

import de.sub.goobi.AbstractTest;
import jakarta.servlet.http.HttpServletRequest;

public abstract class AbstractProcessResourceTest extends AbstractTest {

    protected Process buildProcess(int projectId) {
        Project project = new Project();
        project.setId(projectId);
        project.setTitel("testProject");
        Institution inst = new Institution();
        inst.setShortName("inst");
        project.setInstitution(inst);

        Ruleset ruleset = new Ruleset();
        ruleset.setTitel("testRuleset");

        Docket docket = new Docket();
        docket.setName("testDocket");

        Process process = new Process();
        process.setId(5);
        process.setTitel("testProcess");
        process.setProjekt(project);
        process.setRegelsatz(ruleset);
        process.setDocket(docket);
        return process;
    }

    protected AuthenticationToken buildToken(int userId) {
        AuthenticationToken token = new AuthenticationToken();
        token.setUserId(userId);
        return token;
    }

    protected HttpServletRequest mockRequestWithToken(AuthenticationToken token) {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        Mockito.when(req.getAttribute("authToken")).thenReturn(token);
        return req;
    }

    protected HttpServletRequest mockRequestWithoutToken() {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        Mockito.when(req.getAttribute("authToken")).thenReturn(null);
        return req;
    }
}
