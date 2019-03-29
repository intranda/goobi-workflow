package org.goobi.api.mq;

import java.util.ArrayList;
import java.util.List;

import javax.jms.JMSException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;

import de.sub.goobi.config.ConfigurationHelper;
import lombok.extern.log4j.Log4j;

@WebListener
@Log4j
public class StartQueueBrokerListener implements ServletContextListener {

    public static final String FAST_QUEUE = "goobi_fast";
    public static final String SLOW_QUEUE = "goobi_slow";

    private BrokerService broker;
    private List<GoobiDefaultQueueListener> listeners = new ArrayList<>();

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ConfigurationHelper config = ConfigurationHelper.getInstance();
        String activeMqConfig = config.getActiveMQConfigPath();
        try {
            broker = BrokerFactory.createBroker("xbean:file://" + activeMqConfig, true);
        } catch (Exception e) {
            log.error(e);
        }

        try {
            for (int i = 0; i < config.getNumberOfParallelMessages(); i++) {
                GoobiDefaultQueueListener listener = new GoobiDefaultQueueListener();
                listener.register(config.getMessageBrokerUsername(), config.getMessageBrokerPassword(), SLOW_QUEUE);
                this.listeners.add(listener);
            }
            GoobiDefaultQueueListener listener = new GoobiDefaultQueueListener();
            listener.register(config.getMessageBrokerUsername(), config.getMessageBrokerPassword(), FAST_QUEUE);
            this.listeners.add(listener);
        } catch (JMSException e) {
            log.error(e);
        }

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        try {
            broker.stop();
            for (GoobiDefaultQueueListener l : listeners) {
                l.close();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            log.error(e);
        }
    }

}
