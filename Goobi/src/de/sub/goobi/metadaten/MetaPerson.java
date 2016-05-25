package de.sub.goobi.metadaten;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- http://launchpad.net/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
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
import java.util.List;

import javax.faces.model.SelectItem;

import org.goobi.api.display.DisplayCase;
import org.goobi.api.display.helper.NormDatabase;
import org.goobi.beans.Process;
import org.goobi.production.plugin.interfaces.IPersonPlugin;

import de.sub.goobi.forms.NavigationForm.Theme;
import ugh.dl.DocStruct;
import ugh.dl.MetadataType;
import ugh.dl.NamePart;
import ugh.dl.Person;
import ugh.dl.Prefs;

/**
 * Die Klasse Schritt ist ein Bean für einen einzelnen Schritt mit dessen Eigenschaften und erlaubt die Bearbeitung der Schrittdetails
 * 
 * @author Steffen Hankiewicz
 * @version 1.00 - 10.01.2005
 */
public class MetaPerson {
    private Person p;
    private int identifier;
    private Prefs myPrefs;
    private DocStruct myDocStruct;
    private MetadatenHelper mdh;
    private Theme theme;
    private IPersonPlugin plugin;
    private DisplayCase myValues;

    /**
     * Allgemeiner Konstruktor ()
     */
    public MetaPerson(Person p, int inID, Prefs inPrefs, DocStruct inStruct, Process inProcess, Theme theme, Metadaten bean) {
        this.myPrefs = inPrefs;
        this.p = p;
        this.identifier = inID;
        this.myDocStruct = inStruct;
        this.mdh = new MetadatenHelper(inPrefs, null);
        this.theme = theme;
        myValues = new DisplayCase(inProcess, p.getType());

        if (this.theme == Theme.uii) {
            try {
                plugin = (IPersonPlugin) Class.forName("de.intranda.goobi.plugins." + myValues.getDisplayType().getPluginName()).newInstance();
                if (plugin != null) {
                    plugin.setPerson(p);
                    plugin.setBean(bean);
                    plugin.setDocStruct(myDocStruct);
                    plugin.setMetadatenHelper(mdh);
                    //                    initializeValues();
                }
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                plugin = null;
            }
        }
    }

    /*#####################################################
     #####################################################
     ##																															 
     ##																Getter und Setter									
     ##                                                   															    
     #####################################################
     ####################################################*/
   
    public int getIdentifier() {
        return this.identifier;
    }

   
    public void setIdentifier(int identifier) {
        this.identifier = identifier;
    }

   
    public Person getP() {
        return this.p;
    }

   
    public void setP(Person p) {
        this.p = p;
    }

   
    public String getVorname() {
        if (this.p.getFirstname() == null) {
            return "";
        }
        return this.p.getFirstname();
    }

   
    public void setVorname(String inVorname) {
        if (inVorname == null) {
            inVorname = "";
        }
        this.p.setFirstname(inVorname);
        this.p.setDisplayname(getNachname() + ", " + getVorname());
    }

   
    public String getNachname() {
        if (this.p.getLastname() == null) {
            return "";
        }
        return this.p.getLastname();
    }

   
    public void setNachname(String inNachname) {
        if (inNachname == null) {
            inNachname = "";
        }
        this.p.setLastname(inNachname);
        this.p.setDisplayname(getNachname() + ", " + getVorname());
    }

   
    public String getRolle() {
        return this.p.getRole();
    }

   
    public void setRolle(String inRolle) {
        this.p.setRole(inRolle);
        MetadataType mdt = this.myPrefs.getMetadataTypeByName(this.p.getRole());
        this.p.setType(mdt);

    }

   
    public ArrayList<SelectItem> getAddableRollen() {
        return this.mdh.getAddablePersonRoles(this.myDocStruct, this.p.getRole());
    }

   
    public List<NamePart> getAdditionalNameParts() {
        return p.getAdditionalNameParts();
    }

   
    public void setAdditionalNameParts(List<NamePart> nameParts) {
        p.setAdditionalNameParts(nameParts);
    }

   
    public void addNamePart() {
        List<NamePart> parts = p.getAdditionalNameParts();
        if (parts == null) {
            parts = new ArrayList<NamePart>();
        }
        NamePart part = new NamePart();
        part.setType("date");
        parts.add(part);
        p.setAdditionalNameParts(parts);
    }

   
    public List<String> getPossibleDatabases() {
        List<NormDatabase> databaseList = NormDatabase.getAllDatabases();
        List<String> abbrev = new ArrayList<String>();
        for (NormDatabase norm : databaseList) {
            abbrev.add(norm.getAbbreviation());
        }
        return abbrev;
    }

   
    public List<String> getPossibleNamePartTypes() {
        // TODO configurable?
        List<String> possibleNamePartTypes = new ArrayList<String>();
        possibleNamePartTypes.add("date");
        possibleNamePartTypes.add("termsOfAddress");
        return possibleNamePartTypes;
    }

   
    public String getNormdataValue() {
        return p.getAuthorityValue();
    }

   
    public void setNormdataValue(String normdata) {
        p.setAuthorityValue(normdata);
    }

   
    public void setNormDatabase(String abbrev) {
        NormDatabase database = NormDatabase.getByAbbreviation(abbrev);
        p.setAuthorityID(database.getAbbreviation());
        p.setAuthorityURI(database.getPath());
    }

   
    public String getNormDatabase() {
        if (p.getAuthorityURI() != null && p.getAuthorityID() != null) {
            NormDatabase ndb = NormDatabase.getByAbbreviation(p.getAuthorityID());
            return ndb.getAbbreviation();
        } else {
            return null;
        }
    }

   
    public boolean isAdditionalParts() {
        return p.getType().isAllowNameParts();
    }

   
    public boolean isNormdata() {
        return p.getType().isAllowNormdata();
    }

    
    public IPersonPlugin getPlugin() {
        return plugin;
    }
    
    public void setPlugin(IPersonPlugin plugin) {
        this.plugin = plugin;
    }
}
