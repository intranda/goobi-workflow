package org.goobi.production.plugin.interfaces;

import de.sub.goobi.metadaten.MetadatenHelper;
import ugh.dl.DocStruct;
import ugh.dl.Person;

public interface IPersonPlugin extends IMetadataPlugin {

    public void setPerson(Person person);

    public void setDocStruct(DocStruct docStruct);

    public void setMetadatenHelper(MetadatenHelper mdh);

    public String getFirstname();

    public void setFirstname(String firstname);

    public String getLastname();

    public void setLastname(String lastname);

    public String getRole();

    public void setRole(String role);

}
