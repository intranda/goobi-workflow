package de.sub.goobi.config;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.log4j.Logger;
import org.goobi.api.display.helper.NormDatabase;

public class ConfigNormdata {

    private static final Logger logger = Logger.getLogger(ConfigNormdata.class);
    
    
    public static List<NormDatabase> getConfiguredNormdatabases() {
        List<NormDatabase> answer = new ArrayList<NormDatabase>();
        XMLConfiguration config = getNormdataConfiguration();
        int numberOfNormdatabases = config.getMaxIndex("normdatabase");
        for (int i = 0; i <= numberOfNormdatabases; i++) {
            String url = config.getString("normdatabase(" + i + ")[@url]");
            String abbreviation = config.getString("normdatabase(" + i + ")[@abbreviation]");
            answer.add(new NormDatabase(url, abbreviation));
        }
        if (answer.isEmpty()) {
            answer.add(new NormDatabase("http://d-nb.info/gnd/", "gnd"));
        }
        
        return answer;
    }
    
    
    private static  XMLConfiguration getNormdataConfiguration() {
        String configurationFile = ConfigurationHelper.getInstance().getConfigurationFolder() + "goobi_normdata.xml";
        XMLConfiguration config;
        try {
            config = new XMLConfiguration(configurationFile);
        } catch (ConfigurationException e) {
            logger.error(e);
            config = new XMLConfiguration();
        }
        config.setListDelimiter('&');
        config.setReloadingStrategy(new FileChangedReloadingStrategy());
        return config;
    }
}
