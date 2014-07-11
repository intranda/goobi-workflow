package de.sub.goobi.helper.ldap;

import static org.junit.Assert.*;

import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapName;

import org.goobi.beans.Ldap;
import org.goobi.beans.User;
import org.junit.Test;

import de.sub.goobi.config.ConfigurationHelper;

public class LdapUserTest {

    @Test
    public void testConstructor() {
        LdapUser fixture = new LdapUser();
        assertNotNull(fixture);
    }

    @Test(expected = NamingException.class)
    public void testConfigureException() throws Exception {
        ConfigurationHelper.getInstance().setParameter("ldap_readonly", "false");
        User user = new User();
        user.setLogin("login");

        Ldap ldap = new Ldap();
        user.setLdapGruppe(ldap);

        LdapUser fixture = new LdapUser();

        fixture.configure(user, "test", "666");
    }

    @Test
    public void testConfigure() throws Exception {
        ConfigurationHelper.getInstance().setParameter("ldap_readonly", "false");
        User user = new User();
        user.setLogin("login");

        Ldap ldap = new Ldap();
        ldap.setObjectClasses("inetOrgPerson");
        user.setLdapGruppe(ldap);
        ldap.setHomeDirectory("home/{login}");
        LdapUser fixture = new LdapUser();

        fixture.configure(user, "test", "666");

    }

    @Test
    public void testGetAttributes() throws Exception {
        ConfigurationHelper.getInstance().setParameter("ldap_readonly", "false");
        User user = new User();
        user.setLogin("login");

        Ldap ldap = new Ldap();
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

        ConfigurationHelper.getInstance().setParameter("ldap_readonly", "false");
        User user = new User();
        user.setLogin("login");

        Ldap ldap = new Ldap();
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
        fixture.search("", "", null, null);    }
    
    @Test(expected = OperationNotSupportedException.class)
    public void testgetNameInNamespace() throws Exception {
        LdapUser fixture = new LdapUser();
        fixture.getNameInNamespace();
    }

 
}
