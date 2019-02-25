package org.goobi.api.mq;

import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.qpid.server.SystemLauncher;

import de.sub.goobi.config.ConfigurationHelper;
import lombok.extern.log4j.Log4j;

@WebListener
@Log4j
public class QpidBrokerListener implements ServletContextListener {

    private SystemLauncher systemLauncher = null;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        if (ConfigurationHelper.getInstance().isStartInternalMessageBroker()) {
            // if configured, start message broker
            try {
                systemLauncher = new SystemLauncher();
                systemLauncher.startup(createSystemConfig());

            } catch (Exception e) {
                log.error(e);
            }
        }
        // register number of parallel message listener to configured broker
        if (ConfigurationHelper.getInstance().getNumberOfParallelMessages() > 0) {
            String server = ConfigurationHelper.getInstance().getMessageBrokerUrl();
            int port = ConfigurationHelper.getInstance().getMessageBrokerPort();
            String username = ConfigurationHelper.getInstance().getMessageBrokerUsername();
            String password = ConfigurationHelper.getInstance().getMessageBrokerPassword();

            for (int i = 0; i < ConfigurationHelper.getInstance().getNumberOfParallelMessages(); i++) {
                MessageListener listener = new GoobiDefaultQueueListener();
                listener.register(server, port, username, password);
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (systemLauncher == null) {
            systemLauncher.shutdown();
        }
    }

    private Map<String, Object> createSystemConfig() throws Exception {
        Map<String, Object> attributes = new HashMap<>();
        URL initialConfig = Paths.get(ConfigurationHelper.getInstance().getConfigurationFolder(), "qpid.json").toUri().toURL();
        attributes.put("type", "Memory");
        attributes.put("initialConfigurationLocation", initialConfig.toExternalForm());
        attributes.put("startupLoggedToSystemOut", true);
        return attributes;
    }

}
