package de.sub.goobi.helper.ldap;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.extensions.StartTLSExtendedRequest;
import com.unboundid.util.LDAPTestUtils;
import com.unboundid.util.ssl.KeyStoreKeyManager;
import com.unboundid.util.ssl.SSLUtil;
import com.unboundid.util.ssl.TrustStoreTrustManager;

import de.sub.goobi.config.ConfigurationHelper;

public class Ldapserver {

    private static String serverKeyStorePath;
    private static char[] serverKeyStorePIN;
    private static String serverTrustStorePath;
    private static String clientTrustStorePath;

    private static String ldifFilePath;

    private InMemoryDirectoryServer server;

    public static void main(String[] args) throws Exception {
        Ldapserver s = new Ldapserver();
        s.start();
    }
    
    public void start() throws Exception {

        InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig("dc=ldap,dc=intranda,dc=com");
        config.addAdditionalBindCredentials("cn=Manager", "CHANGEME");

        // Update the configuration to support LDAP (with StartTLS) and LDAPS
        // listeners.
        //        final SSLUtil serverSSLUtil =
        //                new SSLUtil(new KeyStoreKeyManager(serverKeyStorePath, serverKeyStorePIN, "JKS", "server-cert"), new TrustStoreTrustManager(
        //                        serverTrustStorePath));
        //        final SSLUtil clientSSLUtil = new SSLUtil(new TrustStoreTrustManager(clientTrustStorePath));
                config.setListenerConfigs(InMemoryListenerConfig.createLDAPConfig("LDAP", 0));
                        
//                        .createLDAPConfig("LDAP", // Listener name
//                        null, // Listen address. (null = listen on all interfaces)
//                        0, // Listen port (0 = automatically choose an available port)
        //                serverSSLUtil.createSSLSocketFactory()), // StartTLS factory
        //                InMemoryListenerConfig.createLDAPSConfig("LDAPS", // Listener name
        //                        null, // Listen address. (null = listen on all interfaces)
        //                        0, // Listen port (0 = automatically choose an available port)
        //                        serverSSLUtil.createSSLServerSocketFactory(), // Server factory
        //                        clientSSLUtil.createSSLSocketFactory())); // Client factory

        // Create and start the server instance and populate it with an initial set
        // of data from an LDIF file.
        server = new InMemoryDirectoryServer(config);

        server.importFromLDIF(true, ldifFilePath);

        // Start the server so it will accept client connections.
        server.startListening();

        // Get an unencrypted connection to the server's LDAP listener, then use
        // StartTLS to secure that connection.  Make sure the connection is usable
        // by retrieving the server root DSE.
                LDAPConnection connection = server.getConnection("LDAP");
//                System.out.println(connection.getConnectedAddress());
//                System.out.println( connection.getConnectedPort());
        //        connection.processExtendedOperation(new StartTLSExtendedRequest(clientSSLUtil.createSSLContext()));
        //        LDAPTestUtils.assertEntryExists(connection, "");
ConfigurationHelper.getInstance().setParameter("ldap_url", "ldap://" + connection.getConnectedAddress() + ":"+connection.getConnectedPort());
connection.close();
        // Establish an SSL-based connection to the LDAPS listener, and make sure
        // that connection is also usable.
        //        connection = server.getConnection("LDAPS");
        //        LDAPTestUtils.assertEntryExists(connection, "");
        //        connection.close();

        // Shut down the server so that it will no longer accept client
        // connections, and close all existing connections.

    }

    public void shutdown() {
        if (server != null) {
            server.shutDown(true);
        }
    }
}
