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
package de.sub.goobi;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.sub.goobi.helper.exceptions.ExceptionTest;
import io.goobi.workflow.locking.LockingBeanTest;
import io.goobi.workflow.xslt.GeneratePdfFromXsltTest;

@RunWith(Suite.class)

@SuiteClasses({ OldTests.class, de.sub.goobi.config.TestAll.class, de.sub.goobi.export.dms.TestAll.class, de.sub.goobi.converter.TestAll.class,
    de.sub.goobi.forms.TestAll.class, org.goobi.api.mq.TestQueueType.class, de.sub.goobi.export.download.TestAll.class,
    de.sub.goobi.helper.TestAll.class, de.sub.goobi.helper.enums.TestAll.class, ExceptionTest.class, de.sub.goobi.helper.ldap.TestAll.class,
    de.sub.goobi.helper.servletfilter.TestAll.class, de.sub.goobi.metadaten.TestAll.class, org.goobi.api.rest.TestRestConfig.class,
    org.goobi.api.rest.TestAuthorizationFilter.class, LockingBeanTest.class, GeneratePdfFromXsltTest.class, org.goobi.api.display.TestAll.class,
    org.goobi.vocabulary.helper.ImportJsonVocabularyTest.class
})
public class TestAll {

}
