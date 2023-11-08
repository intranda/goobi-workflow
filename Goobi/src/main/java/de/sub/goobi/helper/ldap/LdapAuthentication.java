package de.sub.goobi.helper.ldap;

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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.StartTlsRequest;
import javax.naming.ldap.StartTlsResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.goobi.beans.User;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.FilesystemHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.encryption.MD4;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class LdapAuthentication {

    private static final String CONTEXT_FACTORY_CLASS = "com.sun.jndi.ldap.LdapCtxFactory";
    private static final String LDAP_VERSION = "java.naming.ldap.version";
    private static final String AUTHENTICATION_MODE_NONE = "none";
    private static final String AUTHENTICATION_MODE_SIMPLE = "simple";
    private static final String UID_NUMBER = "uidNumber";
    private static final String PROPERTY_KEYSTORE = "javax.net.ssl.keyStore";
    private static final String PROPERTY_TRUSTSTORE = "javax.net.ssl.trustStore";
    private static final String PROPERTY_KEYSTORE_PASSWORD = "javax.net.ssl.keyStorePassword";

    private static final String TLS_ERROR = "TLS negotiation error: ";
    private static final String JNDI_ERROR = "JNDI error: ";

    private String getUserDN(User inBenutzer) {
        String userDN = inBenutzer.getLdapGruppe().getUserDN();
        userDN = userDN.replace("{login}", inBenutzer.getLogin());
        if (inBenutzer.getLdaplogin() != null) {
            userDN = userDN.replace("{ldaplogin}", inBenutzer.getLdaplogin());
        }
        userDN = userDN.replace("{firstname}", inBenutzer.getVorname());
        userDN = userDN.replace("{lastname}", inBenutzer.getNachname());
        return userDN;
    }

    /**
     * create new user in LDAP-directory
     * 
     * @param inBenutzer
     * @param inPasswort
     * @throws NamingException
     * @throws NoSuchAlgorithmException
     * @throws InterruptedException
     * @throws IOException
     */
    public void createNewUser(User inBenutzer, String inPasswort)
            throws NamingException, NoSuchAlgorithmException, IOException, InterruptedException {

        if (!inBenutzer.getLdapGruppe().isReadonly()) {
            Hashtable<String, String> env = LdapConnectionSettings(inBenutzer);
            env.put(Context.SECURITY_PRINCIPAL, inBenutzer.getLdapGruppe().getAdminLogin());
            env.put(Context.SECURITY_CREDENTIALS, inBenutzer.getLdapGruppe().getAdminPassword());

            LdapUser dr = new LdapUser();
            dr.configure(inBenutzer, inPasswort, getNextUidNumber(inBenutzer));
            DirContext ctx = new InitialDirContext(env);
            ctx.bind(getUserDN(inBenutzer), dr);
            ctx.close();
            setNextUidNumber(inBenutzer);
            Helper.setMeldung(null, Helper.getTranslation("ldapWritten") + " " + inBenutzer.getNachVorname(), "");
            /*
             * -------------------------------- check if HomeDir exists, else create it --------------------------------
             */
            if (log.isDebugEnabled()) {
                log.debug("HomeVerzeichnis pruefen");
            }
            String homePath = getUserHomeDirectory(inBenutzer);
            if (!StorageProvider.getInstance().isFileExists(Paths.get(homePath))) {
                if (log.isDebugEnabled()) {
                    log.debug("HomeVerzeichnis existiert noch nicht");
                }
                if (FilesystemHelper.createDirectoryForUser(homePath, inBenutzer.getLogin())) {
                    if (log.isDebugEnabled()) {
                        log.debug("HomeVerzeichnis angelegt");
                    }
                } else {
                    Helper.setFehlerMeldung("ldap_error_user_home_creation");
                    log.error("Could not create user home " + homePath);
                }
            } else if (log.isDebugEnabled()) {
                log.debug("HomeVerzeichnis existiert schon");
            }
        } else {
            Helper.setMeldung(Helper.getTranslation("ldapIsReadOnly"));
        }
    }

    /**
     * Check if connection with login and password possible
     * 
     * @param inBenutzer
     * @param inPasswort
     * @return Login correct or not
     */
    public boolean isUserPasswordCorrect(User inBenutzer, String inPasswort) {
        if (log.isDebugEnabled()) {
            log.debug("start login session with ldap");
        }
        Hashtable<String, String> env = LdapConnectionSettings(inBenutzer);

        // Start TLS
        if (inBenutzer.getLdapGruppe().isUseTLS()) {
            if (log.isDebugEnabled()) {
                log.debug("use TLS for auth");
            }
            env = new Hashtable<>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, CONTEXT_FACTORY_CLASS);
            env.put(Context.PROVIDER_URL, inBenutzer.getLdapGruppe().getLdapUrl());
            env.put(LDAP_VERSION, "3");
            LdapContext ctx = null;
            StartTlsResponse tls = null;
            try {
                ctx = new InitialLdapContext(env, null);

                // Authentication must be performed over a secure channel
                tls = (StartTlsResponse) ctx.extendedOperation(new StartTlsRequest());
                tls.negotiate();

                // Authenticate via SASL EXTERNAL mechanism using client X.509
                // certificate contained in JVM keystore
                ctx.addToEnvironment(Context.SECURITY_AUTHENTICATION, AUTHENTICATION_MODE_SIMPLE);
                ctx.addToEnvironment(Context.SECURITY_PRINCIPAL, getUserDN(inBenutzer));
                ctx.addToEnvironment(Context.SECURITY_CREDENTIALS, inPasswort);
                ctx.reconnect(null);
                return true;
                // Perform search for privileged attributes under authenticated context

            } catch (IOException e) {
                log.error(TLS_ERROR, e);
                return false;
            } catch (NamingException e) {
                log.error(JNDI_ERROR, e);
                return false;
            } finally {
                if (tls != null) {
                    try {
                        // Tear down TLS connection
                        tls.close();
                    } catch (IOException exception) {
                        log.warn(exception);
                    }
                }
                if (ctx != null) {
                    try {
                        // Close LDAP connection
                        ctx.close();
                    } catch (NamingException exception) {
                        log.warn(exception);
                    }
                }
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("don't use TLS for auth");
            }
            env.put(Context.SECURITY_PRINCIPAL, getUserDN(inBenutzer));
            env.put(Context.SECURITY_CREDENTIALS, inPasswort);
            if (log.isDebugEnabled()) {
                log.debug("ldap environment set");
            }
            try {
                if (log.isDebugEnabled()) {
                    log.debug("start classic ldap authentification");
                    log.debug("user DN is " + getUserDN(inBenutzer));
                }
                if (inBenutzer.getLdapGruppe().getAttributeToTest() == null) {
                    if (log.isDebugEnabled()) {
                        log.debug("ldap attribute to test is null");
                    }
                    new InitialDirContext(env);
                    return true;
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("ldap attribute to test is not null");
                    }
                    DirContext ctx = new InitialDirContext(env);

                    Attributes attrs = ctx.getAttributes(getUserDN(inBenutzer));
                    Attribute la = attrs.get(inBenutzer.getLdapGruppe().getAttributeToTest());
                    if (log.isDebugEnabled()) {
                        log.debug("ldap attributes set");
                    }
                    String test = (String) la.get(0);
                    if (test.equals(inBenutzer.getLdapGruppe().getValueOfAttribute())) {
                        if (log.isDebugEnabled()) {
                            log.debug("ldap ok");
                        }
                        ctx.close();
                        return true;
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("ldap not ok");
                        }
                        ctx.close();
                        return false;
                    }
                }
            } catch (NamingException e) {
                if (log.isDebugEnabled()) {
                    log.debug("login not allowed for " + inBenutzer.getLogin(), e);
                }
                return false;
            }
        }
    }

    /**
     * retrieve home directory of given user
     * 
     * @param inBenutzer
     * @return path as string
     */
    public String getUserHomeDirectory(User inBenutzer) {
        // skip ldap check, when user is inactive or deleted
        if (inBenutzer.getStatus() != User.UserStatus.ACTIVE) {
            return "";
        }
        if (inBenutzer.getLdapGruppe().isUseLocalDirectoryConfiguration()) {
            return ConfigurationHelper.getInstance().getUserFolder() + inBenutzer.getLogin();
        }
        // use local directory, when user has no ldap group assigned
        if (inBenutzer.getLdapGruppe() == null) {
            return ConfigurationHelper.getInstance().getUserFolder() + inBenutzer.getLogin();
        }

        Hashtable<String, String> env = LdapConnectionSettings(inBenutzer);
        if (inBenutzer.getLdapGruppe().isUseTLS()) {

            env = new Hashtable<>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, CONTEXT_FACTORY_CLASS);
            env.put(Context.PROVIDER_URL, inBenutzer.getLdapGruppe().getLdapUrl());
            env.put(LDAP_VERSION, "3");
            LdapContext ctx = null;
            StartTlsResponse tls = null;
            try {
                ctx = new InitialLdapContext(env, null);

                // Authentication must be performed over a secure channel
                tls = (StartTlsResponse) ctx.extendedOperation(new StartTlsRequest());
                tls.negotiate();

                // Authenticate via SASL EXTERNAL mechanism using client X.509
                // certificate contained in JVM keystore
                ctx.addToEnvironment(Context.SECURITY_AUTHENTICATION, AUTHENTICATION_MODE_SIMPLE);
                ctx.addToEnvironment(Context.SECURITY_PRINCIPAL, inBenutzer.getLdapGruppe().getAdminLogin());
                ctx.addToEnvironment(Context.SECURITY_CREDENTIALS, inBenutzer.getLdapGruppe().getAdminPassword());

                ctx.reconnect(null);

                Attributes attrs = ctx.getAttributes(getUserDN(inBenutzer));
                Attribute la = attrs.get(inBenutzer.getLdapGruppe().getLdapHomeDirectoryAttributeName());
                return (String) la.get(0);

                // Perform search for privileged attributes under authenticated context

            } catch (IOException e) {
                log.error(TLS_ERROR, e);

                return ConfigurationHelper.getInstance().getUserFolder() + inBenutzer.getLogin();
            } catch (NamingException e) {

                log.error(JNDI_ERROR, e);

                return ConfigurationHelper.getInstance().getUserFolder() + inBenutzer.getLogin();
            } finally {
                if (tls != null) {
                    try {
                        // Tear down TLS connection
                        tls.close();
                    } catch (IOException exception) {
                        log.warn(exception);
                    }
                }
                if (ctx != null) {
                    try {
                        // Close LDAP connection
                        ctx.close();
                    } catch (NamingException exception) {
                        log.warn(exception);
                    }
                }
            }
        } else if (inBenutzer.getLdapGruppe().isReadDirectoryAnonymous()) {
            env.put(Context.SECURITY_AUTHENTICATION, AUTHENTICATION_MODE_NONE);
        } else {
            env.put(Context.SECURITY_AUTHENTICATION, AUTHENTICATION_MODE_SIMPLE);
            env.put(Context.SECURITY_PRINCIPAL, inBenutzer.getLdapGruppe().getAdminLogin());
            env.put(Context.SECURITY_CREDENTIALS, inBenutzer.getLdapGruppe().getAdminPassword());

        }
        DirContext ctx;
        String rueckgabe = "";
        try {
            ctx = new InitialDirContext(env);
            Attributes attrs = ctx.getAttributes(getUserDN(inBenutzer));
            Attribute la = attrs.get(inBenutzer.getLdapGruppe().getLdapHomeDirectoryAttributeName());
            rueckgabe = (String) la.get(0);
            ctx.close();
        } catch (NamingException e) {
            log.error(e);
        }
        return rueckgabe;
    }

    /**
     * check if User already exists on system
     * 
     * @param inBenutzer
     * @return path as string
     */
    public boolean isUserAlreadyExists(User inBenutzer) {
        Hashtable<String, String> env = LdapConnectionSettings(inBenutzer);
        env.put(Context.SECURITY_PRINCIPAL, inBenutzer.getLdapGruppe().getAdminLogin());
        env.put(Context.SECURITY_CREDENTIALS, inBenutzer.getLdapGruppe().getAdminPassword());
        DirContext ctx;
        boolean rueckgabe = false;
        try {
            ctx = new InitialDirContext(env);
            Attributes matchAttrs = new BasicAttributes(true);
            NamingEnumeration<SearchResult> answer = ctx.search("ou=users,dc=gdz,dc=sub,dc=uni-goettingen,dc=de", matchAttrs);
            rueckgabe = answer.hasMoreElements();

            while (answer.hasMore()) {
                SearchResult sr = answer.next();
                if (log.isDebugEnabled()) {
                    log.debug(">>>" + sr.getName());
                }
                Attributes attrs = sr.getAttributes();
                String givenName;
                String surName;
                String mail;
                String cn;
                String hd;
                try {
                    givenName = attrs.get("givenName").toString();
                } catch (Exception err) {
                    givenName = " ";
                }
                try {
                    surName = attrs.get("sn").toString();
                } catch (Exception e2) {
                    surName = " ";
                }
                try {
                    mail = attrs.get("mail").toString();
                } catch (Exception e3) {
                    mail = " ";
                }
                try {
                    cn = attrs.get("cn").toString();
                } catch (Exception e4) {
                    cn = " ";
                }
                try {
                    hd = attrs.get(inBenutzer.getLdapGruppe().getLdapHomeDirectoryAttributeName()).toString();
                } catch (Exception e4) {
                    hd = " ";
                }
                if (log.isDebugEnabled()) {
                    log.debug(givenName);
                    log.debug(surName);
                    log.debug(mail);
                    log.debug(cn);
                    log.debug(hd);
                }
            }

            ctx.close();
        } catch (NamingException e) {
            log.error(e);
        }
        return rueckgabe;
    }

    /**
     * Get next free uidNumber
     * 
     * @return next free uidNumber
     * @throws NamingException
     */
    private String getNextUidNumber(User inBenutzer) {
        Hashtable<String, String> env = LdapConnectionSettings(inBenutzer);
        env.put(Context.SECURITY_PRINCIPAL, inBenutzer.getLdapGruppe().getAdminLogin());
        env.put(Context.SECURITY_CREDENTIALS, inBenutzer.getLdapGruppe().getAdminPassword());
        DirContext ctx;
        String rueckgabe = "";
        try {
            ctx = new InitialDirContext(env);
            Attributes attrs = ctx.getAttributes(inBenutzer.getLdapGruppe().getNextFreeUnixId());
            Attribute la = attrs.get(UID_NUMBER);
            rueckgabe = (String) la.get(0);
            ctx.close();
        } catch (NamingException e) {
            log.error(e);
            Helper.setFehlerMeldung(e.getMessage());
        }
        return rueckgabe;
    }

    /**
     * Set next free uidNumber
     * 
     * @throws NamingException
     */
    private void setNextUidNumber(User inBenutzer) {
        Hashtable<String, String> env = LdapConnectionSettings(inBenutzer);
        env.put(Context.SECURITY_PRINCIPAL, inBenutzer.getLdapGruppe().getAdminLogin());
        env.put(Context.SECURITY_CREDENTIALS, inBenutzer.getLdapGruppe().getAdminPassword());
        DirContext ctx;

        try {
            ctx = new InitialDirContext(env);
            Attributes attrs = ctx.getAttributes(inBenutzer.getLdapGruppe().getNextFreeUnixId());
            Attribute la = attrs.get(UID_NUMBER);
            String oldValue = (String) la.get(0);
            int bla = Integer.parseInt(oldValue) + 1;

            BasicAttribute attrNeu = new BasicAttribute(UID_NUMBER, String.valueOf(bla));
            ModificationItem[] mods = new ModificationItem[1];
            mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attrNeu);
            ctx.modifyAttributes(inBenutzer.getLdapGruppe().getNextFreeUnixId(), mods);

            ctx.close();
        } catch (NamingException e) {
            log.error(e);
        }
    }

    public void deleteUser(User inBenutzer) {

        Hashtable<String, String> env = LdapConnectionSettings(inBenutzer);
        if (inBenutzer.getLdapGruppe().isUseTLS()) {
            env = new Hashtable<>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, CONTEXT_FACTORY_CLASS);
            env.put(Context.PROVIDER_URL, inBenutzer.getLdapGruppe().getLdapUrl());
            env.put(LDAP_VERSION, "3");
            LdapContext ctx = null;
            StartTlsResponse tls = null;
            try {
                ctx = new InitialLdapContext(env, null);

                // Authentication must be performed over a secure channel
                tls = (StartTlsResponse) ctx.extendedOperation(new StartTlsRequest());
                tls.negotiate();

                // Authenticate via SASL EXTERNAL mechanism using client X.509
                // certificate contained in JVM keystore
                ctx.addToEnvironment(Context.SECURITY_AUTHENTICATION, AUTHENTICATION_MODE_SIMPLE);
                ctx.addToEnvironment(Context.SECURITY_PRINCIPAL, inBenutzer.getLdapGruppe().getAdminLogin());
                ctx.addToEnvironment(Context.SECURITY_CREDENTIALS, inBenutzer.getLdapGruppe().getAdminPassword());
                ctx.reconnect(null);
                ctx.unbind(getUserDN(inBenutzer));
            } catch (IOException e) {
                log.error(TLS_ERROR, e);

            } catch (NamingException e) {

                log.error(JNDI_ERROR, e);

            } finally {
                if (tls != null) {
                    try {
                        // Tear down TLS connection
                        tls.close();
                    } catch (IOException exception) {
                        log.warn(exception);
                    }
                }
                if (ctx != null) {
                    try {
                        // Close LDAP connection
                        ctx.close();
                    } catch (NamingException exception) {
                        log.warn(exception);
                    }
                }
            }
        } else if (inBenutzer.getLdapGruppe().isReadDirectoryAnonymous()) {
            env.put(Context.SECURITY_AUTHENTICATION, AUTHENTICATION_MODE_NONE);
        } else {
            env.put(Context.SECURITY_AUTHENTICATION, AUTHENTICATION_MODE_SIMPLE);
            env.put(Context.SECURITY_PRINCIPAL, inBenutzer.getLdapGruppe().getAdminLogin());
            env.put(Context.SECURITY_CREDENTIALS, inBenutzer.getLdapGruppe().getAdminPassword());

        }
        DirContext ctx;
        try {
            ctx = new InitialDirContext(env);
            ctx.unbind(getUserDN(inBenutzer));

            ctx.close();
        } catch (NamingException e) {
            log.error(e);
        }
    }

    /**
     * change password of given user, needs old password for authentification
     * 
     * @param inUser
     * @param inOldPassword
     * @param inNewPassword
     * @return boolean about result of change
     * @throws NoSuchAlgorithmException
     */
    public boolean changeUserPassword(User inBenutzer, String inOldPassword, String inNewPassword) throws NoSuchAlgorithmException {
        Hashtable<String, String> env = LdapConnectionSettings(inBenutzer);
        if (!inBenutzer.getLdapGruppe().isReadonly()) {
            env.put(Context.SECURITY_PRINCIPAL, inBenutzer.getLdapGruppe().getAdminLogin());
            env.put(Context.SECURITY_CREDENTIALS, inBenutzer.getLdapGruppe().getAdminPassword());

            try {
                DirContext ctx = new InitialDirContext(env);

                /*
                 * -------------------------------- Encryption of password and Base64-Encoding --------------------------------
                 */
                MessageDigest md = MessageDigest.getInstance(inBenutzer.getLdapGruppe().getEncryptionType());
                md.update(inNewPassword.getBytes());
                String digestBase64 = new String(Base64.encodeBase64(md.digest()));
                ModificationItem[] mods = new ModificationItem[4];

                /*
                 * -------------------------------- UserPasswort-Attribut ändern --------------------------------
                 */
                BasicAttribute userpassword =
                        new BasicAttribute("userPassword", "{" + inBenutzer.getLdapGruppe().getEncryptionType() + "}" + digestBase64);

                /*
                 * -------------------------------- LanMgr-Passwort-Attribut ändern --------------------------------
                 */
                BasicAttribute lanmgrpassword = null;
                try {
                    lanmgrpassword = new BasicAttribute("sambaLMPassword", LdapUser.toHexString(LdapUser.lmHash(inNewPassword)));
                    // TODO: Don't catch super class exception, make sure that the password isn't logged here
                } catch (Exception e) {
                    log.error(e);
                }

                /*
                 * -------------------------------- NTLM-Passwort-Attribut ändern --------------------------------
                 */
                BasicAttribute ntlmpassword = null;
                byte[] hmm = MD4.mdfour(inNewPassword.getBytes(StandardCharsets.UTF_16LE));
                ntlmpassword = new BasicAttribute("sambaNTPassword", LdapUser.toHexString(hmm));
                BasicAttribute sambaPwdLastSet = new BasicAttribute("sambaPwdLastSet", String.valueOf(System.currentTimeMillis() / 1000l));
                mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, userpassword);
                mods[1] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, lanmgrpassword);
                mods[2] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, ntlmpassword);
                mods[3] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, sambaPwdLastSet);
                ctx.modifyAttributes(getUserDN(inBenutzer), mods);

                // Close the context when we're done
                ctx.close();
                return true;
            } catch (NamingException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Benutzeranmeldung nicht korrekt oder Passwortänderung nicht möglich", e);
                }
                return false;
            }
        }
        return false;
    }

    private Hashtable<String, String> LdapConnectionSettings(User inBenutzer) {
        // Set up environment for creating initial context
        Hashtable<String, String> env = new Hashtable<>(11);
        env.put(Context.INITIAL_CONTEXT_FACTORY, CONTEXT_FACTORY_CLASS);
        env.put(Context.PROVIDER_URL, inBenutzer.getLdapGruppe().getLdapUrl());
        env.put(Context.SECURITY_AUTHENTICATION, AUTHENTICATION_MODE_SIMPLE);
        /* wenn die Verbindung über ssl laufen soll */
        if (inBenutzer.getLdapGruppe().isUseSsl()) {
            String keystorepath = ConfigurationHelper.getInstance().getTruststore();
            String keystorepasswd = ConfigurationHelper.getInstance().getTruststoreToken();
            if (StringUtils.isNotBlank(keystorepath) && StringUtils.isNotBlank(keystorepasswd)) {
                // add all necessary certificates first
                loadCertificates(inBenutzer, keystorepath, keystorepasswd);

                // set properties, so that the current keystore is used for SSL
                System.setProperty(PROPERTY_KEYSTORE, keystorepath);
                System.setProperty(PROPERTY_TRUSTSTORE, keystorepath);
                System.setProperty(PROPERTY_KEYSTORE_PASSWORD, keystorepasswd);
            }
            env.put(Context.SECURITY_PROTOCOL, "ssl");
        }
        return env;
    }

    private void loadCertificates(User inBenutzer, String path, String passwd) {
        /* wenn die Zertifikate noch nicht im Keystore sind, jetzt einlesen */
        Path myPfad = Paths.get(path);
        if (!StorageProvider.getInstance().isFileExists(myPfad)) {
            try {
                // TODO: Rename parameters to something more meaningful, this is quite specific for the GDZ
                try (FileOutputStream ksos = new FileOutputStream(path);
                        FileInputStream cacertFile = new FileInputStream(inBenutzer.getLdapGruppe().getPathToRootCertificate());
                        FileInputStream certFile2 = new FileInputStream(inBenutzer.getLdapGruppe().getPathToPdcCertificate())) {

                    CertificateFactory cf = CertificateFactory.getInstance("X.509");
                    X509Certificate cacert = (X509Certificate) cf.generateCertificate(cacertFile);
                    X509Certificate servercert = (X509Certificate) cf.generateCertificate(certFile2);

                    KeyStore ks = KeyStore.getInstance("jks");
                    char[] password = passwd.toCharArray();

                    // TODO: Let this method really load a keystore if configured
                    // initalize the keystore, if file is available, load the keystore
                    ks.load(null);

                    ks.setCertificateEntry("ROOTCERT", cacert);
                    ks.setCertificateEntry("PDC", servercert);

                    ks.store(ksos, password);
                }
            } catch (Exception e) {
                log.error(e);
            }

        }
    }

}