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
 *
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
package de.sub.goobi.persistence.managers;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.goobi.api.mail.SendMail;
import org.goobi.beans.Project;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import de.sub.goobi.AbstractTest;

public class UserMysqlHelperEmailConfigurationTest extends AbstractTest {

    private static final int USER_ID = 5;

    private static Project project(int id) {
        Project project = new Project();
        project.setId(id);
        project.setTitel("project " + id);
        return project;
    }

    private SqlCapture captureEmailConfiguration(boolean showAllItems) throws Exception {
        SendMail.MailConfiguration config = Mockito.mock(SendMail.MailConfiguration.class);
        Mockito.when(config.isEnableStatusChangeMail()).thenReturn(true);
        SendMail sendMail = Mockito.mock(SendMail.class);
        Mockito.when(sendMail.getConfig()).thenReturn(config);
        SqlCapture capture = new SqlCapture(new ArrayList<>());
        capture.mySQLHelper().when(MySQLHelper::isJsonCapable).thenReturn(true);
        try (MockedStatic<SendMail> mockedSendMail = Mockito.mockStatic(SendMail.class)) {
            mockedSendMail.when(SendMail::getInstance).thenReturn(sendMail);
            UserMysqlHelper.getEmailConfigurationForUser(Arrays.asList(project(1), project(2)), USER_ID, showAllItems);
        } finally {
            capture.close();
        }
        return capture;
    }

    @Test
    public void testAssignedStepsAreSearchedWithinTheProject() throws Exception {
        SqlCapture capture = captureEmailConfiguration(false);

        assertEquals(2, capture.statements.size());
        String sql = capture.statements.get(0).replaceAll("\\s+", " ");
        // the titles of all steps assigned to the user groups do not depend on the project, they must not be collected again for every project
        assertFalse(sql.contains("INTERSECT"), sql);
        assertTrue(sql.contains("JOIN schritteberechtigtegruppen sbg ON sbg.SchritteID = s.SchritteID"), sql);
        assertTrue(sql.contains("WHERE p.ProjekteID = ? AND bm.BenutzerID = ?"), sql);
    }

    @Test
    public void testAssignedStepsParameters() throws Exception {
        SqlCapture capture = captureEmailConfiguration(false);

        assertArrayEquals(new Object[] { 1, USER_ID, 1, USER_ID }, withoutHandler(capture.parameters.get(0)));
        assertArrayEquals(new Object[] { 2, USER_ID, 2, USER_ID }, withoutHandler(capture.parameters.get(1)));
    }

    @Test
    public void testAllStepsOfTheProjectWithoutGroups() throws Exception {
        SqlCapture capture = captureEmailConfiguration(true);

        String sql = capture.statements.get(0).replaceAll("\\s+", " ");
        assertFalse(sql.contains("schritteberechtigtegruppen"), sql);
        assertArrayEquals(new Object[] { 1, 1, USER_ID }, withoutHandler(capture.parameters.get(0)));
    }

    @Test
    public void testResultIsOrderedByStepTitle() throws Exception {
        SqlCapture capture = captureEmailConfiguration(false);

        // the order has to be defined on the outer query, an order inside the derived table is not kept by the join
        assertTrue(capture.statements.get(0).replaceAll("\\s+", " ").trim().endsWith("ORDER BY sub.titel"), capture.statements.get(0));
    }

    private static Object[] withoutHandler(Object[] parameters) {
        // first parameter after the sql is the result set handler
        List<Object> values = new ArrayList<>(Arrays.asList(parameters));
        values.remove(0);
        return values.toArray();
    }
}
