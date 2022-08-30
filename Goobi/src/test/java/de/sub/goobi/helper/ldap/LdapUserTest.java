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
package de.sub.goobi.helper.ldap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;
import javax.naming.directory.Attributes;
import javax.naming.ldap.LdapName;

import org.goobi.beans.Ldap;
import org.goobi.beans.User;
import org.junit.Test;

import de.sub.goobi.AbstractTest;

public class LdapUserTest extends AbstractTest {

    @Test
    public void testConstructor() {
        LdapUser fixture = new LdapUser();
        assertNotNull(fixture);
    }

    @Test(expected = NamingException.class)
    public void testConfigureException() throws Exception {

        User user = new User();
        user.setLogin("login");

        Ldap ldap = new Ldap();
        ldap.setAuthenticationType("ldap");
        ldap.setObjectClasses(null);
        ldap.setReadonly(false);
        user.setLdapGruppe(ldap);

        LdapUser fixture = new LdapUser();

        fixture.configure(user, "test", "666");
    }

    @Test
    public void testConfigure() throws Exception {
        User user = new User();
        user.setLogin("login");

        Ldap ldap = new Ldap();
        ldap.setAuthenticationType("ldap");
        ldap.setReadonly(false);
        ldap.setObjectClasses("inetOrgPerson");
        user.setLdapGruppe(ldap);
        ldap.setHomeDirectory("home/{login}");
        LdapUser fixture = new LdapUser();

        fixture.configure(user, "test", "666");

    }

    @Test
    public void testGetAttributes() throws Exception {
        User user = new User();
        user.setLogin("login");

        Ldap ldap = new Ldap();
        ldap.setAuthenticationType("ldap");
        ldap.setReadonly(false);
        ldap.setObjectClasses("inetOrgPerson");
        user.setLdapGruppe(ldap);
        ldap.setHomeDirectory("home/{login}");
        LdapUser fixture = new LdapUser();

        fixture.configure(user, "test", "666");

        Attributes attrs = fixture.getAttributes("");
        assertNotNull(attrs);
        Name name = new LdapName("");
        attrs = fixture.getAttributes(name);
        assertNotNull(attrs);

        String[] idList = { "uid" };
        attrs = fixture.getAttributes("", idList);
        assertNotNull(attrs);

        attrs = fixture.getAttributes(name, idList);
        assertNotNull(attrs);

    }

    @Test
    public void testToString() throws Exception {

        User user = new User();
        user.setLogin("login");

        Ldap ldap = new Ldap();
        ldap.setAuthenticationType("ldap");
        ldap.setAuthenticationType("ldap");
        ldap.setReadonly(false);
        ldap.setObjectClasses("inetOrgPerson");
        user.setLdapGruppe(ldap);
        ldap.setHomeDirectory("home/{login}");
        LdapUser fixture = new LdapUser();

        fixture.configure(user, "test", "666");
        assertEquals("login", fixture.toString());
    }

    @Test(expected = OperationNotSupportedException.class)
    public void testLockupName() throws Exception {
        LdapUser fixture = new LdapUser();
        Name name = new LdapName("");
        fixture.lookup(name);
    }

    @Test(expected = OperationNotSupportedException.class)
    public void testLockupString() throws Exception {
        LdapUser fixture = new LdapUser();
        fixture.lookup("");
    }

    @Test(expected = OperationNotSupportedException.class)
    public void testBindName() throws Exception {
        LdapUser fixture = new LdapUser();
        Name name = new LdapName("");
        fixture.bind(name, "");
    }

    @Test(expected = OperationNotSupportedException.class)
    public void testBindString() throws Exception {
        LdapUser fixture = new LdapUser();
        fixture.bind("", "");
    }

    @Test(expected = OperationNotSupportedException.class)
    public void testReBindName() throws Exception {
        LdapUser fixture = new LdapUser();
        Name name = new LdapName("");
        fixture.rebind(name, "");
    }

    @Test(expected = OperationNotSupportedException.class)
    public void testReBindString() throws Exception {
        LdapUser fixture = new LdapUser();
        fixture.rebind("", "");
    }

    @Test(expected = OperationNotSupportedException.class)
    public void testUnBindName() throws Exception {
        LdapUser fixture = new LdapUser();
        Name name = new LdapName("");
        fixture.unbind(name);
    }

    @Test(expected = OperationNotSupportedException.class)
    public void testUnBindString() throws Exception {
        LdapUser fixture = new LdapUser();
        fixture.unbind("");
    }

    @Test(expected = OperationNotSupportedException.class)
    public void testRenameName() throws Exception {
        LdapUser fixture = new LdapUser();
        Name name = new LdapName("");
        fixture.rename(name, name);
    }

    @Test(expected = OperationNotSupportedException.class)
    public void testRenameString() throws Exception {
        LdapUser fixture = new LdapUser();
        fixture.rename("", "");
    }

    @Test(expected = OperationNotSupportedException.class)
    public void testListName() throws Exception {
        LdapUser fixture = new LdapUser();
        Name name = new LdapName("");
        fixture.list(name);
    }

    @Test(expected = OperationNotSupportedException.class)
    public void testListString() throws Exception {
        LdapUser fixture = new LdapUser();
        fixture.list("");
    }

    @Test(expected = OperationNotSupportedException.class)
    public void testListBindingsName() throws Exception {
        LdapUser fixture = new LdapUser();
        Name name = new LdapName("");
        fixture.listBindings(name);
    }

    @Test(expected = OperationNotSupportedException.class)
    public void testListBindingsString() throws Exception {
        LdapUser fixture = new LdapUser();
        fixture.listBindings("");
    }

    @Test(expected = OperationNotSupportedException.class)
    public void testDestroySubcontextName() throws Exception {
        LdapUser fixture = new LdapUser();
        Name name = new LdapName("");
        fixture.destroySubcontext(name);
    }

    @Test(expected = OperationNotSupportedException.class)
    public void testDestroySubcontextString() throws Exception {
        LdapUser fixture = new LdapUser();
        fixture.destroySubcontext("");
    }

    @Test(expected = OperationNotSupportedException.class)
    public void testcreateSubcontextName() throws Exception {
        LdapUser fixture = new LdapUser();
        Name name = new LdapName("");
        fixture.createSubcontext(name);
    }

    @Test(expected = OperationNotSupportedException.class)
    public void testCreateSubcontextString() throws Exception {
        LdapUser fixture = new LdapUser();
        fixture.createSubcontext("");
    }

    @Test(expected = OperationNotSupportedException.class)
    public void testLookupLinkName() throws Exception {
        LdapUser fixture = new LdapUser();
        Name name = new LdapName("");
        fixture.lookupLink(name);
    }

    @Test(expected = OperationNotSupportedException.class)
    public void testLookupLinkString() throws Exception {
        LdapUser fixture = new LdapUser();
        fixture.lookupLink("");
    }

    @Test(expected = OperationNotSupportedException.class)
    public void testGetNameParserName() throws Exception {
        LdapUser fixture = new LdapUser();
        Name name = new LdapName("");
        fixture.getNameParser(name);
    }

    @Test(expected = OperationNotSupportedException.class)
    public void testGetNameParserString() throws Exception {
        LdapUser fixture = new LdapUser();
        fixture.getNameParser("");
    }

    @Test(expected = OperationNotSupportedException.class)
    public void testComposeNameName() throws Exception {
        LdapUser fixture = new LdapUser();
        Name name = new LdapName("");
        fixture.composeName(name, name);
    }

    @Test(expected = OperationNotSupportedException.class)
    public void testComposeNameString() throws Exception {
        LdapUser fixture = new LdapUser();
        fixture.composeName("", "");
    }

    @Test(expected = OperationNotSupportedException.class)
    public void testAddToEnvironmentString() throws Exception {
        LdapUser fixture = new LdapUser();
        fixture.addToEnvironment("", "");
    }

    @Test(expected = OperationNotSupportedException.class)
    public void testRemoveFromEnvironmentName() throws Exception {
        LdapUser fixture = new LdapUser();
        fixture.removeFromEnvironment("");
    }

    @Test(expected = NamingException.class)
    public void testGetEnvironment() throws NamingException {
        LdapUser fixture = new LdapUser();
        fixture.getEnvironment();
    }

    @Test(expected = NamingException.class)
    public void testClose() throws NamingException {
        LdapUser fixture = new LdapUser();
        fixture.close();
    }

    @Test(expected = OperationNotSupportedException.class)
    public void testModifyAttributesName() throws Exception {
        LdapUser fixture = new LdapUser();
        Name name = new LdapName("");
        fixture.modifyAttributes(name, 1, null);
    }

    @Test(expected = OperationNotSupportedException.class)
    public void testModifyAttributesString() throws Exception {
        LdapUser fixture = new LdapUser();
        fixture.modifyAttributes("", 1, null);
    }

    @Test(expected = OperationNotSupportedException.class)
    public void testModifyAttributesName2() throws Exception {
        LdapUser fixture = new LdapUser();
        Name name = new LdapName("");
        fixture.modifyAttributes(name, null);
    }

    @Test(expected = OperationNotSupportedException.class)
    public void testModifyAttributesString2() throws Exception {
        LdapUser fixture = new LdapUser();
        fixture.modifyAttributes("", null);
    }

    @Test(expected = OperationNotSupportedException.class)
    public void testBindAttributesName() throws Exception {
        LdapUser fixture = new LdapUser();
        Name name = new LdapName("");
        fixture.bind(name, null, null);
    }

    @Test(expected = OperationNotSupportedException.class)
    public void testBindAttributesString() throws Exception {
        LdapUser fixture = new LdapUser();
        fixture.bind("", null, null);
    }

    @Test(expected = OperationNotSupportedException.class)
    public void testReBindAttributesName() throws Exception {
        LdapUser fixture = new LdapUser();
        Name name = new LdapName("");
        fixture.rebind(name, null, null);
    }

    @Test(expected = OperationNotSupportedException.class)
    public void testReBindAttributesString() throws Exception {
        LdapUser fixture = new LdapUser();
        fixture.rebind("", null, null);
    }

    @Test(expected = OperationNotSupportedException.class)
    public void testCreateSubcontextAttributesName() throws Exception {
        LdapUser fixture = new LdapUser();
        Name name = new LdapName("");
        fixture.createSubcontext(name, null);
    }

    @Test(expected = OperationNotSupportedException.class)
    public void testCreateSubcontextAttributesString() throws Exception {
        LdapUser fixture = new LdapUser();
        fixture.createSubcontext("", null);
    }

    @Test(expected = OperationNotSupportedException.class)
    public void testgetSchemaName() throws Exception {
        LdapUser fixture = new LdapUser();
        Name name = new LdapName("");
        fixture.getSchema(name);
    }

    @Test(expected = OperationNotSupportedException.class)
    public void testgetSchemaString() throws Exception {
        LdapUser fixture = new LdapUser();
        fixture.getSchema("");
    }

    @Test(expected = OperationNotSupportedException.class)
    public void testgetSchemaClassDefinitionName() throws Exception {
        LdapUser fixture = new LdapUser();
        Name name = new LdapName("");
        fixture.getSchemaClassDefinition(name);
    }

    @Test(expected = OperationNotSupportedException.class)
    public void testgetSchemaClassDefinitionString() throws Exception {
        LdapUser fixture = new LdapUser();
        fixture.getSchemaClassDefinition("");
    }

    //public NamingEnumeration<SearchResult> search(Name name, Attributes matchingAttributes, String[] attributesToReturn) throws NamingException {
    //    public NamingEnumeration<SearchResult> search(String name, Attributes matchingAttributes, String[] attributesToReturn) throws NamingException {
    @Test(expected = OperationNotSupportedException.class)
    public void testSearchName() throws Exception {
        LdapUser fixture = new LdapUser();
        Name name = new LdapName("");
        String[] attr = {};
        fixture.search(name, null, attr);
    }

    @Test(expected = OperationNotSupportedException.class)
    public void testSearchString() throws Exception {
        LdapUser fixture = new LdapUser();
        String[] attr = {};
        fixture.search("", null, attr);
    }

    @Test(expected = OperationNotSupportedException.class)
    public void testSearchName2() throws Exception {
        LdapUser fixture = new LdapUser();
        Name name = new LdapName("");
        fixture.search(name, null);
    }

    @Test(expected = OperationNotSupportedException.class)
    public void testSearchString2() throws Exception {
        LdapUser fixture = new LdapUser();
        fixture.search("", null);
    }

    @Test(expected = OperationNotSupportedException.class)
    public void testSearchName3() throws Exception {
        LdapUser fixture = new LdapUser();
        Name name = new LdapName("");
        fixture.search(name, "", null);

    }

    @Test(expected = OperationNotSupportedException.class)
    public void testSearchString3() throws Exception {
        LdapUser fixture = new LdapUser();
        fixture.search("", "", null);
    }

    @Test(expected = OperationNotSupportedException.class)
    public void testSearchName4() throws Exception {
        LdapUser fixture = new LdapUser();
        Name name = new LdapName("");
        fixture.search(name, "", null, null);
    }

    @Test(expected = OperationNotSupportedException.class)
    public void testSearchString4() throws Exception {
        LdapUser fixture = new LdapUser();
        fixture.search("", "", null, null);
    }

    @Test(expected = OperationNotSupportedException.class)
    public void testgetNameInNamespace() throws Exception {
        LdapUser fixture = new LdapUser();
        fixture.getNameInNamespace();
    }

}
