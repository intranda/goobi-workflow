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
 */
package io.goobi.workflow.harvester.export;

import java.io.File;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.core.util.Loader;
import org.jdom2.Document;
import org.jdom2.Namespace;

import de.sub.goobi.config.ConfigHarvester;
import lombok.extern.log4j.Log4j2;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.MetadataType;
import ugh.dl.Prefs;
import ugh.dl.Reference;
import ugh.dl.VirtualFileGroup;
import ugh.exceptions.PreferencesException;

@Log4j2
public abstract class AbstractConverter implements IConverter {

    protected static DecimalFormat imageNameFormat = new DecimalFormat("00000000");

    protected Prefs myPrefs;
    protected Document doc;
    protected ExportMode mode;

    protected static Namespace nsOaiDc = Namespace.getNamespace("oai_dc", "http://www.openarchives.org/OAI/2.0/oai_dc/");
    protected static Namespace nsDc = Namespace.getNamespace("dc", "http://purl.org/dc/elements/1.1/");

    protected static final String DEFAULT_COLLECTION = "Varia";

    protected static String oaiPrefix;
    protected static String ughRulesetFile;
    protected static String hotfolderViewer;
    protected static String hotfolderGoobi;
    protected static String imageTempFolder;
    protected static String imageUrlLocal;
    protected static String imageUrlRemote;
    protected static String imageRoot;
    protected static String rightsOwner;
    protected static String rightsOwnerLogo;
    protected static String rightsOwnerSiteUrl;

    protected String repositoryType;

    public AbstractConverter(Document doc, ExportMode mode, String repositoryType) {
        this.doc = doc;
        this.mode = mode;
        this.repositoryType = repositoryType;
        if (StringUtils.isEmpty(repositoryType)) {
            repositoryType = "_DEFAULT";
        }
        loadProperties();

        this.myPrefs = getPreferences(ughRulesetFile);
    }

    /**
     * Loads the settings from config.properties.
     * 
     * @return
     */
    @SuppressWarnings("rawtypes")
    protected final void loadProperties() {
        oaiPrefix = ConfigHarvester.getInstance().getOaiPrefix();
        ughRulesetFile = ConfigHarvester.getInstance().getUghRulesetFile();
        hotfolderViewer = ConfigHarvester.getInstance().getViewerHotfolder();
        List<String> hotfolderList = ConfigHarvester.getInstance().getGoobiHotfolders();
        if (hotfolderList != null) {
            String defaultHotfolder = null;
            for (Object element : hotfolderList) {
                HierarchicalConfiguration sub = (HierarchicalConfiguration) element;
                if (repositoryType.equals(sub.getString("[@name]"))) {
                    hotfolderGoobi = sub.getString(".");
                } else if ("_DEFAULT".equals(sub.getString("[@name]"))) {
                    defaultHotfolder = sub.getString(".");
                }
            }
            if (StringUtils.isEmpty(hotfolderGoobi)) {
                hotfolderGoobi = defaultHotfolder;
            }
        }
        imageTempFolder = ConfigHarvester.getInstance().getImageTempFolder();
        imageUrlLocal = ConfigHarvester.getInstance().getImageUrlLocal();
        imageUrlRemote = ConfigHarvester.getInstance().getImageUrlRemote();
        imageRoot = ConfigHarvester.getInstance().getImageRoot();
        rightsOwner = ConfigHarvester.getInstance().getRightsOwner();
        rightsOwnerLogo = ConfigHarvester.getInstance().getRightsOwnerLogo();
        rightsOwnerSiteUrl = ConfigHarvester.getInstance().getRightsOwnerSiteUrl();
    }

    private static Prefs getPreferences(String filename) {
        Prefs mypreferences = new Prefs();
        try {
            File prefFile = new File(ConfigHarvester.getInstance().getConfigFolder() + File.separator + filename);
            if (!prefFile.exists()) {
                // Load from internal resources package if no external file found
                prefFile = new File(Loader.getResource("resources" + File.separator + filename, Loader.getThreadContextClassLoader()).toURI());
            }
            mypreferences.loadPrefs(prefFile.getAbsolutePath());
        } catch (PreferencesException | URISyntaxException e) {
            log.error(e.getMessage(), e);
        }
        return mypreferences;
    }

    /**
     * Recursive bottom-up re-population of the physical mappings, so that each docstruct has all mappings of any of its subelements.
     * 
     * @param myDocStruct The root DocStruct of the recursion.
     */
    @SuppressWarnings("rawtypes")
    protected void attachChildPages(DocStruct myDocStruct) {
        if (myDocStruct.getAllChildren() != null) {
            for (Object element : myDocStruct.getAllChildren()) {
                DocStruct child = (DocStruct) element;
                attachChildPages(child);
                for (Reference ref : child.getAllToReferences()) {
                    // Since Reference.equals() isn't properly implemented, the contains check must by done manually
                    boolean alreadyReferenced = false;
                    for (Reference myRef : myDocStruct.getAllToReferences()) {
                        if (myRef.getTarget().equals(ref.getTarget())) {
                            alreadyReferenced = true;
                            break;
                        }
                    }
                    if (!alreadyReferenced) {
                        myDocStruct.getAllToReferences().add(ref);
                    }
                }
            }
        }
    }

    /**
     * Convenience method to check whether a <code>MetadataType</code> is allowed in the given <code>DocStruct</code>. Cannot do that with
     * <code>ds.getaddableMetadataTypes().contains(mdt)</code> because it always returns false.
     * 
     * @param ds
     * @param mdt
     * @return true if allowed; false otherwise.
     */
    protected boolean isMetadataTypeAllowed(DocStruct ds, MetadataType mdt) {
        if (ds != null) {
            List<MetadataType> addableTypes = ds.getAddableMetadataTypes(false);
            if (addableTypes != null) {
                for (MetadataType t : addableTypes) {
                    if (t.equals(mdt)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    protected void addVirtualFileGroup(String fileNamePI, DigitalDocument dd, String suffix, String mimeType, String name) {
        VirtualFileGroup vfgLocal = new VirtualFileGroup();
        vfgLocal.setName(name);
        vfgLocal.setPathToFiles(fileNamePI + "/");
        vfgLocal.setMimetype(mimeType);
        vfgLocal.setFileSuffix(suffix);
        dd.getFileSet().addVirtualFileGroup(vfgLocal);
    }
}
