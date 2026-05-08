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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;
import javax.naming.directory.Attributes;
import javax.naming.ldap.LdapName;

import org.goobi.beans.Ldap;
import org.goobi.beans.User;
import org.junit.jupiter.api.Test;

import de.sub.goobi.AbstractTest;

public class LdapUserTest extends AbstractTest {

    @Test
    public void testConstructor() {
        LdapUser fixture = new LdapUser();
        assertNotNull(fixture);
    }

    @Test
    public void testConfigureException() {
        assertThrows(NamingException.class, () -> {
            User user = new User();
            user.setLogin("login");

            Ldap ldap = new Ldap();
            ldap.setAuthenticationType("ldap");
            ldap.setObjectClasses(null);
            ldap.setReadonly(false);
            user.setLdapGruppe(ldap);

            LdapUser fixture = new LdapUser();

            fixture.configure(user, "test", "666");
        });
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

    @Test
    public void testLockupName() {
        assertThrows(OperationNotSupportedException.class, () -> {
            LdapUser fixture = new LdapUser();
            Name name = new LdapName("");
            fixture.lookup(name);
        });
    }

    @Test
    public void testLockupString() {
        assertThrows(OperationNotSupportedException.class, () -> {
            LdapUser fixture = new LdapUser();
            fixture.lookup("");
        });
    }

    @Test
    public void testBindName() {
        assertThrows(OperationNotSupportedException.class, () -> {
            LdapUser fixture = new LdapUser();
            Name name = new LdapName("");
            fixture.bind(name, "");
        });
    }

    @Test
    public void testBindString() {
        assertThrows(OperationNotSupportedException.class, () -> {
            LdapUser fixture = new LdapUser();
            fixture.bind("", "");
        });
    }

    @Test
    public void testReBindName() {
        assertThrows(OperationNotSupportedException.class, () -> {
            LdapUser fixture = new LdapUser();
            Name name = new LdapName("");
            fixture.rebind(name, "");
        });
    }

    @Test
    public void testReBindString() {
        assertThrows(OperationNotSupportedException.class, () -> {
            LdapUser fixture = new LdapUser();
            fixture.rebind("", "");
        });
    }

    @Test
    public void testUnBindName() {
        assertThrows(OperationNotSupportedException.class, () -> {
            LdapUser fixture = new LdapUser();
            Name name = new LdapName("");
            fixture.unbind(name);
        });
    }

    @Test
    public void testUnBindString() {
        assertThrows(OperationNotSupportedException.class, () -> {
            LdapUser fixture = new LdapUser();
            fixture.unbind("");
        });
    }

    @Test
    public void testRenameName() {
        assertThrows(OperationNotSupportedException.class, () -> {
            LdapUser fixture = new LdapUser();
            Name name = new LdapName("");
            fixture.rename(name, name);
        });
    }

    @Test
    public void testRenameString() {
        assertThrows(OperationNotSupportedException.class, () -> {
            LdapUser fixture = new LdapUser();
            fixture.rename("", "");
        });
    }

    @Test
    public void testListName() {
        assertThrows(OperationNotSupportedException.class, () -> {
            LdapUser fixture = new LdapUser();
            Name name = new LdapName("");
            fixture.list(name);
        });
    }

    @Test
    public void testListString() {
        assertThrows(OperationNotSupportedException.class, () -> {
            LdapUser fixture = new LdapUser();
            fixture.list("");
        });
    }

    @Test
    public void testListBindingsName() {
        assertThrows(OperationNotSupportedException.class, () -> {
            LdapUser fixture = new LdapUser();
            Name name = new LdapName("");
            fixture.listBindings(name);
        });
    }

    @Test
    public void testListBindingsString() {
        assertThrows(OperationNotSupportedException.class, () -> {
            LdapUser fixture = new LdapUser();
            fixture.listBindings("");
        });
    }

    @Test
    public void testDestroySubcontextName() {
        assertThrows(OperationNotSupportedException.class, () -> {
            LdapUser fixture = new LdapUser();
            Name name = new LdapName("");
            fixture.destroySubcontext(name);
        });
    }

    @Test
    public void testDestroySubcontextString() {
        assertThrows(OperationNotSupportedException.class, () -> {
            LdapUser fixture = new LdapUser();
            fixture.destroySubcontext("");
        });
    }

    @Test
    public void testcreateSubcontextName() {
        assertThrows(OperationNotSupportedException.class, () -> {
            LdapUser fixture = new LdapUser();
            Name name = new LdapName("");
            fixture.createSubcontext(name);
        });
    }

    @Test
    public void testCreateSubcontextString() {
        assertThrows(OperationNotSupportedException.class, () -> {
            LdapUser fixture = new LdapUser();
            fixture.createSubcontext("");
        });
    }

    @Test
    public void testLookupLinkName() {
        assertThrows(OperationNotSupportedException.class, () -> {
            LdapUser fixture = new LdapUser();
            Name name = new LdapName("");
            fixture.lookupLink(name);
        });
    }

    @Test
    public void testLookupLinkString() {
        assertThrows(OperationNotSupportedException.class, () -> {
            LdapUser fixture = new LdapUser();
            fixture.lookupLink("");
        });
    }

    @Test
    public void testGetNameParserName() {
        assertThrows(OperationNotSupportedException.class, () -> {
            LdapUser fixture = new LdapUser();
            Name name = new LdapName("");
            fixture.getNameParser(name);
        });
    }

    @Test
    public void testGetNameParserString() {
        assertThrows(OperationNotSupportedException.class, () -> {
            LdapUser fixture = new LdapUser();
            fixture.getNameParser("");
        });
    }

    @Test
    public void testComposeNameName() {
        assertThrows(OperationNotSupportedException.class, () -> {
            LdapUser fixture = new LdapUser();
            Name name = new LdapName("");
            fixture.composeName(name, name);
        });
    }

    @Test
    public void testComposeNameString() {
        assertThrows(OperationNotSupportedException.class, () -> {
            LdapUser fixture = new LdapUser();
            fixture.composeName("", "");
        });
    }

    @Test
    public void testAddToEnvironmentString() {
        assertThrows(OperationNotSupportedException.class, () -> {
            LdapUser fixture = new LdapUser();
            fixture.addToEnvironment("", "");
        });
    }

    @Test
    public void testRemoveFromEnvironmentName() {
        assertThrows(OperationNotSupportedException.class, () -> {
            LdapUser fixture = new LdapUser();
            fixture.removeFromEnvironment("");
        });
    }

    @Test
    public void testGetEnvironment() {
        assertThrows(NamingException.class, () -> {
            LdapUser fixture = new LdapUser();
            fixture.getEnvironment();
        });
    }

    @Test
    public void testClose() {
        assertThrows(NamingException.class, () -> {
            LdapUser fixture = new LdapUser();
            fixture.close();
        });
    }

    @Test
    public void testModifyAttributesName() {
        assertThrows(OperationNotSupportedException.class, () -> {
            LdapUser fixture = new LdapUser();
            Name name = new LdapName("");
            fixture.modifyAttributes(name, 1, null);
        });
    }

    @Test
    public void testModifyAttributesString() {
        assertThrows(OperationNotSupportedException.class, () -> {
            LdapUser fixture = new LdapUser();
            fixture.modifyAttributes("", 1, null);
        });
    }

    @Test
    public void testModifyAttributesName2() {
        assertThrows(OperationNotSupportedException.class, () -> {
            LdapUser fixture = new LdapUser();
            Name name = new LdapName("");
            fixture.modifyAttributes(name, null);
        });
    }

    @Test
    public void testModifyAttributesString2() {
        assertThrows(OperationNotSupportedException.class, () -> {
            LdapUser fixture = new LdapUser();
            fixture.modifyAttributes("", null);
        });
    }

    @Test
    public void testBindAttributesName() {
        assertThrows(OperationNotSupportedException.class, () -> {
            LdapUser fixture = new LdapUser();
            Name name = new LdapName("");
            fixture.bind(name, null, null);
        });
    }

    @Test
    public void testBindAttributesString() {
        assertThrows(OperationNotSupportedException.class, () -> {
            LdapUser fixture = new LdapUser();
            fixture.bind("", null, null);
        });
    }

    @Test
    public void testReBindAttributesName() {
        assertThrows(OperationNotSupportedException.class, () -> {
            LdapUser fixture = new LdapUser();
            Name name = new LdapName("");
            fixture.rebind(name, null, null);
        });
    }

    @Test
    public void testReBindAttributesString() {
        assertThrows(OperationNotSupportedException.class, () -> {
            LdapUser fixture = new LdapUser();
            fixture.rebind("", null, null);
        });
    }

    @Test
    public void testCreateSubcontextAttributesName() {
        assertThrows(OperationNotSupportedException.class, () -> {
            LdapUser fixture = new LdapUser();
            Name name = new LdapName("");
            fixture.createSubcontext(name, null);
        });
    }

    @Test
    public void testCreateSubcontextAttributesString() {
        assertThrows(OperationNotSupportedException.class, () -> {
            LdapUser fixture = new LdapUser();
            fixture.createSubcontext("", null);
        });
    }

    @Test
    public void testgetSchemaName() {
        assertThrows(OperationNotSupportedException.class, () -> {
            LdapUser fixture = new LdapUser();
            Name name = new LdapName("");
            fixture.getSchema(name);
        });
    }

    @Test
    public void testgetSchemaString() {
        assertThrows(OperationNotSupportedException.class, () -> {
            LdapUser fixture = new LdapUser();
            fixture.getSchema("");
        });
    }

    @Test
    public void testgetSchemaClassDefinitionName() {
        assertThrows(OperationNotSupportedException.class, () -> {
            LdapUser fixture = new LdapUser();
            Name name = new LdapName("");
            fixture.getSchemaClassDefinition(name);
        });
    }

    @Test
    public void testgetSchemaClassDefinitionString() {
        assertThrows(OperationNotSupportedException.class, () -> {
            LdapUser fixture = new LdapUser();
            fixture.getSchemaClassDefinition("");
        });
    }

    //public NamingEnumeration<SearchResult> search(Name name, Attributes matchingAttributes, String[] attributesToReturn) throws NamingException {
    //public NamingEnumeration<SearchResult> search(String name, Attributes matchingAttributes, String[] attributesToReturn) throws NamingException {
    @Test
    public void testSearchName() {
        assertThrows(OperationNotSupportedException.class, () -> {
            LdapUser fixture = new LdapUser();
            Name name = new LdapName("");
            String[] attr = {};
            fixture.search(name, null, attr);
        });
    }

    @Test
    public void testSearchString() {
        assertThrows(OperationNotSupportedException.class, () -> {
            LdapUser fixture = new LdapUser();
            String[] attr = {};
            fixture.search("", null, attr);
        });
    }

    @Test
    public void testSearchName2() {
        assertThrows(OperationNotSupportedException.class, () -> {
            LdapUser fixture = new LdapUser();
            Name name = new LdapName("");
            fixture.search(name, null);
        });
    }

    @Test
    public void testSearchString2() {
        assertThrows(OperationNotSupportedException.class, () -> {
            LdapUser fixture = new LdapUser();
            fixture.search("", null);
        });
    }

    @Test
    public void testSearchName3() {
        assertThrows(OperationNotSupportedException.class, () -> {
            LdapUser fixture = new LdapUser();
            Name name = new LdapName("");
            fixture.search(name, "", null);
        });
    }

    @Test
    public void testSearchString3() {
        assertThrows(OperationNotSupportedException.class, () -> {
            LdapUser fixture = new LdapUser();
            fixture.search("", "", null);
        });
    }

    @Test
    public void testSearchName4() {
        assertThrows(OperationNotSupportedException.class, () -> {
            LdapUser fixture = new LdapUser();
            Name name = new LdapName("");
            fixture.search(name, "", null, null);
        });
    }

    @Test
    public void testSearchString4() {
        assertThrows(OperationNotSupportedException.class, () -> {
            LdapUser fixture = new LdapUser();
            fixture.search("", "", null, null);
        });
    }

    @Test
    public void testgetNameInNamespace() {
        assertThrows(OperationNotSupportedException.class, () -> {
            LdapUser fixture = new LdapUser();
            fixture.getNameInNamespace();
        });
    }

}
