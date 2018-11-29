package de.intranda.goobi.plugins;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.goobi.api.rest.model.RestMetadata;
import org.goobi.api.rest.model.RestProcess;
import org.goobi.api.rest.request.SearchGroup;
import org.goobi.api.rest.request.SearchQuery;
import org.goobi.api.rest.request.SearchQuery.RelationalOperator;
import org.goobi.api.rest.request.SearchRequest;
import org.goobi.beans.Project;
import org.goobi.production.plugin.interfaces.AbstractMetadataPlugin;
import org.goobi.production.plugin.interfaces.IMetadataPlugin;

import de.sub.goobi.config.ConfigPlugins;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.MetadataManager;
import lombok.EqualsAndHashCode;
import lombok.extern.log4j.Log4j;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import ugh.dl.DocStruct;
import ugh.dl.Metadata;
import ugh.dl.MetadataGroup;
import ugh.dl.MetadataGroupType;
import ugh.dl.Prefs;
import ugh.exceptions.MetadataTypeNotAllowedException;

@Log4j
@PluginImplementation
@EqualsAndHashCode(callSuper = false)
public class ProcessPlugin extends AbstractMetadataPlugin implements IMetadataPlugin {

    private SearchRequest searchRequest;
    private List<RestProcess> results;
    private List<String> possibleFields;

    public ProcessPlugin() {
        super();
        initSearch();
    }

    @Override
    public String getTitle() {
        return "ProcessPlugin";
    }

    @Override
    public String search() {
        // set our wanted fields
        configureRequest(searchRequest);
        try {
            this.results = searchRequest.search();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            log.error(e);
        }
        return "";
    }

    @Override
    public String getData() {

        this.initSearch();
        return "";
    }

    public SearchRequest getSearchRequest() {
        return this.searchRequest;
    }

    public List<RestProcess> getResults() {
        return this.results;
    }

    private void initSearch() {
        log.debug("init search!");
        this.searchRequest = new SearchRequest();
        List<String> possibleFields = this.getPossibleFields();
        SearchQuery query = new SearchQuery(possibleFields.get(0), "", RelationalOperator.EQUAL);
        SearchGroup group = new SearchGroup();
        group.addFilter(query);
        this.searchRequest.addSearchGroup(group);
    }

    public void linkProcess(RestProcess rp) {
        Project p = this.getBean().getMyProzess().getProjekt();
        XMLConfiguration xmlConf = ConfigPlugins.getPluginConfig(getTitle());
        if (xmlConf == null) {
            return;
        }
        HierarchicalConfiguration use = xmlConf.configurationAt("globalConfig");
        List<HierarchicalConfiguration> projectConfs = xmlConf.configurationsAt("projectConfig");
        for (HierarchicalConfiguration projectConf : projectConfs) {
            if (p.getTitel().equals(projectConf.getString("[@project]"))) {
                use = projectConf;
                break;
            }
        }
        Map<String, List<RestMetadata>> addMetadata = new HashMap<>();
        List<HierarchicalConfiguration> mappings = use.configurationsAt("mapping");
        for (HierarchicalConfiguration mapping : mappings) {
            String from = mapping.getString("[@from]");
            String to = mapping.getString("[@to]");
            List<RestMetadata> fromMeta = rp.getMetadata().get(from);
            if (fromMeta != null) {
                addMetadata.put(to, fromMeta);
            }
        }
        Prefs prefs = this.getBean().getMyPrefs();
        DocStruct ds = this.getBean().getMyDocStruct();
        for (String name : addMetadata.keySet()) {
            try {
                for (RestMetadata rmd : addMetadata.get(name)) {
                    if (!rmd.anyValue()) {
                        continue;
                    }
                    if (name.contains("/")) {
                        String[] split = name.split("/");
                        String group = split[0];
                        String metaName = split[1];
                        MetadataGroupType mgt = prefs.getMetadataGroupTypeByName(group);
                        MetadataGroup addGroup = null;
                        addGroup = new MetadataGroup(mgt);
                        List<Metadata> metaList = addGroup.getMetadataByType(metaName);
                        Metadata md;
                        if (metaList.isEmpty()) {
                            md = new Metadata(prefs.getMetadataTypeByName(metaName));
                            addGroup.addMetadata(md);
                        } else {
                            md = metaList.get(0);
                        }
                        if (rmd.getValue() != null) {
                            md.setValue(rmd.getValue());
                        }
                        if (rmd.getAuthorityID() != null) {
                            md.setAuthorityID(rmd.getAuthorityID());
                        }
                        if (rmd.getAuthorityURI() != null) {
                            md.setAuthorityURI(rmd.getAuthorityURI());
                        }
                        if (rmd.getAuthorityValue() != null) {
                            md.setAuthorityValue(rmd.getAuthorityValue());
                        }
                        ds.addMetadataGroup(addGroup);
                    } else {
                        Metadata md = new Metadata(prefs.getMetadataTypeByName(name));
                        if (rmd.getValue() != null) {
                            md.setValue(rmd.getValue());
                        }
                        if (rmd.getAuthorityID() != null) {
                            md.setAuthorityID(rmd.getAuthorityID());
                        }
                        if (rmd.getAuthorityURI() != null) {
                            md.setAuthorityURI(rmd.getAuthorityURI());
                        }
                        if (rmd.getAuthorityValue() != null) {
                            md.setAuthorityValue(rmd.getAuthorityValue());
                        }
                        ds.addMetadata(md);
                    }
                }
            } catch (MetadataTypeNotAllowedException e) {
                Helper.setFehlerMeldung("Metadata " + name + " not allowed in preferences.", e);
            }
        }
        this.getBean().reloadMetadataList();
    }

    private void configureRequest(SearchRequest req) {

        if (this.getBean() == null) {
            log.error("Metadaten bean null");
            return;
        }
        Project p = this.getBean().getMyProzess().getProjekt();
        XMLConfiguration xmlConf = ConfigPlugins.getPluginConfig(getTitle());
        if (xmlConf == null) {
            return;
        }
        List<HierarchicalConfiguration> projectConfs = xmlConf.configurationsAt("projectConfig");
        for (HierarchicalConfiguration projectConf : projectConfs) {
            if (p.getTitel().equals(projectConf.getString("[@project]"))) {
                req.setWantedFields(loadWantedFields(projectConf));
                req.setFilterProjects(loadFilterProjects(projectConf));
                return;
            }
        }
        HierarchicalConfiguration global = xmlConf.configurationAt("globalConfig");
        req.setWantedFields(loadWantedFields(global));
        req.setFilterProjects(loadFilterProjects(global));
    }

    private Set<String> loadWantedFields(HierarchicalConfiguration projectConf) {
        Set<String> wantedFields = new HashSet<>();
        String[] wfArr = projectConf.getStringArray("wantedField");
        for (String wf : wfArr) {
            wantedFields.add(wf);
        }
        return wantedFields;
    }

    private List<String> loadFilterProjects(HierarchicalConfiguration projectConf) {
        List<String> projects = new ArrayList<>();
        String[] projArr = projectConf.getStringArray("searchableProject");
        for (String proj : projArr) {
            projects.add(proj);
        }
        return projects;
    }

    public List<String> getPossibleFields() {
        if (this.possibleFields == null) {
            this.possibleFields = MetadataManager.getDistinctMetadataNames();
        }
        return this.possibleFields;
    }

}
