package org.goobi.api.mq;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.RMIServerSocketFactory;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *          - https://goobi.io
 *          - https://www.intranda.com
 *          - https://github.com/intranda/goobi-workflow
 *          - http://digiverso.com
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnectorServer;

import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;

import de.sub.goobi.config.ConfigurationHelper;
import jakarta.jms.JMSException;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import lombok.extern.log4j.Log4j2;

@WebListener
@Log4j2
public class StartQueueBrokerListener implements ServletContextListener {

    private RMIConnectorServer rmiServer;
    private BrokerService broker;
    private List<GoobiDefaultQueueListener> listeners = new ArrayList<>();
    private GoobiInternalDLQListener dlqListener;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ConfigurationHelper config = ConfigurationHelper.getInstance();
        if (config.isStartInternalMessageBroker()) {
            Thread.ofVirtual().name("GoobiInternalMessageBroker").start(() -> {
                log.debug("Initializing internal RMI server...");
                // JMX/RMI part taken from: https://vafer.org/blog/20061010091658/
                String address = "localhost";
                int namingPort = 1099;
                int protocolPort = 0;
                try {
                    RMIServerSocketFactory serverFactory = new RMIServerSocketFactoryImpl(InetAddress.getByName(address));

                    LocateRegistry.createRegistry(namingPort, null, serverFactory);

                    StringBuilder url = new StringBuilder();
                    url.append("service:jmx:");
                    url.append("rmi://").append(address).append(':').append(protocolPort).append("/jndi/");
                    url.append("rmi://").append(address).append(':').append(namingPort).append("/connector");

                    Map<String, Object> env = new HashMap<>();
                    env.put(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE, serverFactory);

                    rmiServer = new RMIConnectorServer(
                            new JMXServiceURL(url.toString()),
                            env,
                            ManagementFactory.getPlatformMBeanServer());

                    log.debug("Starting internal RMI server...");
                    rmiServer.start();
                    log.debug("Internal RMI server started");
                } catch (IOException e1) {
                    log.error("error starting JMX connector. Will not start internal MessageBroker. Exception: {}", e1);
                    return;
                }
                String activeMqConfig = config.getActiveMQConfigPath();
                try {
                    log.debug("Starting internal MessageBroker...");
                    broker = BrokerFactory.createBroker("xbean:file:" + activeMqConfig, false);
                    broker.setUseJmx(true);
                    broker.start();
                    log.debug("Internal MessageBroker started");
                } catch (Exception e) {
                    log.error(e);
                }

                try {
                    log.debug("Registering internal MessageBroker listeners...");
                    for (int i = 0; i < config.getNumberOfParallelMessages(); i++) {
                        GoobiDefaultQueueListener listener = new GoobiDefaultQueueListener();
                        listener.register(config.getMessageBrokerUsername(), config.getMessageBrokerPassword(), QueueType.SLOW_QUEUE);
                        this.listeners.add(listener);
                    }
                    GoobiDefaultQueueListener listener = new GoobiDefaultQueueListener();
                    listener.register(config.getMessageBrokerUsername(), config.getMessageBrokerPassword(), QueueType.FAST_QUEUE);
                    this.listeners.add(listener);

                    dlqListener = new GoobiInternalDLQListener();
                    dlqListener.register(config.getMessageBrokerUsername(), config.getMessageBrokerPassword(), QueueType.DEAD_LETTER_QUEUE);

                    GoobiCommandListener commandListener = new GoobiCommandListener();
                    commandListener.register(config.getMessageBrokerUsername(), config.getMessageBrokerPassword());

                    if (config.isAllowExternalQueue() && "SQS".equalsIgnoreCase(config.getExternalQueueType())) {
                        GoobiExternalJobQueueDLQListener externalDlqListener = new GoobiExternalJobQueueDLQListener();
                        externalDlqListener.register(config.getMessageBrokerUsername(), config.getMessageBrokerPassword());
                    }
                    log.debug("Internal MessageBroker listeners registered");
                } catch (JMSException e) {
                    log.error(e);
                }
            });
        } else {
            log.info("MessageBroker will not be started. Set 'MessageBrokerStart=true' in the 'goobi_config.properties' to start it.");
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            if (rmiServer != null) {
                rmiServer.stop();
            }
            for (GoobiDefaultQueueListener l : listeners) {
                l.close();
            }

            dlqListener.close();

            if (broker != null) {
                broker.stop();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            log.error(e);
        }
    }

}
