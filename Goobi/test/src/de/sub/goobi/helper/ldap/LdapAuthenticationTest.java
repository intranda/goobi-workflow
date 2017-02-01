package de.sub.goobi.helper.ldap;

import static org.junit.Assert.*;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.naming.NamingException;

import javax.naming.directory.InitialDirContext;


import org.goobi.beans.Ldap;
import org.goobi.beans.User;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.mock.Ldapserver;
@RunWith(PowerMockRunner.class)
@PrepareForTest(InitialDirContext.class)

public class LdapAuthenticationTest {

    private User currentUser;
    private Ldapserver server;
    @Before
    public void setUp() throws Exception {
        currentUser = new User();
        currentUser.setLogin("login");
        currentUser.setVorname("firstname");
        currentUser.setNachname("lastname");
        currentUser.setLdaplogin("login");
        currentUser.setEncryptedPassword("password");
        
        Ldap ldap = new Ldap();
        ldap.setUserDN("cn=testuser,dc=ldap,dc=intranda,dc=com");
        ldap.setObjectClasses("inetOrgPerson");
        ldap.setHomeDirectory("home/{login}");
        
        currentUser.setLdapGruppe(ldap);
        server = new Ldapserver();
        server.start();
    }
    
    @After
    public void tearDown() {
        server.shutdown();
    }
    
    
   


//    @Test
    public void testLdapAuthentication() {
        LdapAuthentication auth = new LdapAuthentication();
        assertNotNull(auth);
    }

//    @Test
    public void testCreateNewUser() throws NoSuchAlgorithmException, NamingException, IOException, InterruptedException {
        LdapAuthentication auth = new LdapAuthentication();
        // ldap is read only
        auth.createNewUser(currentUser, "password");
        
        ConfigurationHelper.getInstance().setParameter("ldap_readonly", "false");
//        ConfigurationHelper.getInstance().setParameter("ldap_sslconnection", "true");
        
        auth.createNewUser(currentUser, "password");
    }

//    @Test
//    public void testIsUserPasswordCorrect() {
//        fail("Not yet implemented");
//    }
//
//    @Test
//    public void testGetUserHomeDirectory() {
//        fail("Not yet implemented");
//    }
//
//    @Test
//    public void testIsUserAlreadyExists() {
//        fail("Not yet implemented");
//    }
//
//    @Test
//    public void testDeleteUser() {
//        fail("Not yet implemented");
//    }
//
//    @Test
//    public void testChangeUserPassword() {
//        fail("Not yet implemented");
//    }

}
