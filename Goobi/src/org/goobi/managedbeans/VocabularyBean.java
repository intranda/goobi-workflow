package org.goobi.managedbeans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.SessionScoped;
import javax.inject.Named;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.jmx.QueueViewMBean;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.goobi.api.mq.TaskTicket;
import org.goobi.production.enums.PluginType;

import com.google.gson.Gson;

import org.goobi.vocabulary.VocabularyManager;
import de.sub.goobi.config.ConfigPlugins;
import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.persistence.managers.MQResultManager;
import de.sub.goobi.persistence.managers.RulesetManager;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j;

/**
 * 
 * This bean can be used to display the current state of the goobi_fast and goobi_slow queues. The bean provides methods to show all active tickets
 * and remove a ticket or clear the queue.
 *
 */

@javax.faces.bean.ManagedBean
@SessionScoped
@Log4j
public class VocabularyBean extends BasicBean implements Serializable {

    private static final long serialVersionUID = -4591427229251805665L;

    private static final String PLUGIN_NAME = "intranda_administration_vocabulary";
    private static final String GUI = "/uii/administration_vocabulary.xhtml";

    //details up or down
    private String uiStatus;

    public String getUiStatus() {
        return uiStatus;
    }

    public void setUiStatus(String uiStatus) {
        this.uiStatus = uiStatus;
    }
    
    private VocabularyManager vm;

    public VocabularyManager getVm() {
        return vm;
    }

    private List<String> allVocabularies;

    public List<String> getAllVocabularies() {
        return allVocabularies;
    }

    /**
     * Constructor for parameter initialisation from config file
     */
    public VocabularyBean() {

        Initialize();
        
        uiStatus = "down";
    }
    
    
    public void Reload() {
        
       // Initialize();
    }
    
    private void Initialize() {
        
        System.out.println("constructor start");

        // initialise configuration file
        XMLConfiguration config = ConfigPlugins.getPluginConfig(PLUGIN_NAME);
        config.setExpressionEngine(new XPathExpressionEngine());

        // save all vocabulary names
        List<HierarchicalConfiguration> vocabularyList = config.configurationsAt("vocabulary");
        allVocabularies = new ArrayList<>();
        for (HierarchicalConfiguration voc : vocabularyList) {
            allVocabularies.add(voc.getString("@title"));
        }

        // set first vocabulary as current one
        vm = new VocabularyManager(config);

        if (allVocabularies.size() > 0)
            vm.loadVocabulary(allVocabularies.get(0));
        else
            vm.loadVocabulary(null);
    }

    public String FilterKein() throws ConfigurationException {

        // initialise the vocabulary
        String configfile = "plugin_intranda_administration_vocabulary.xml";
        XMLConfiguration config = new XMLConfiguration("/opt/digiverso/goobi/config/" + configfile);

        //        VocabularyManager vm = new VocabularyManager(config);
        //        paginator = new DatabasePaginator("titel", filter, vm, "ruleset_all");
        return "vocabulary";
    }

    
}
