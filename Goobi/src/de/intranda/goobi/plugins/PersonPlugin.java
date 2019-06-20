package de.intranda.goobi.plugins;

import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import org.apache.commons.lang.StringUtils;
import org.goobi.api.display.helper.NormDatabase;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.interfaces.IPersonPlugin;

import de.intranda.digiverso.normdataimporter.NormDataImporter;
import de.intranda.digiverso.normdataimporter.model.MarcRecord;
import de.intranda.digiverso.normdataimporter.model.NormData;
import de.intranda.digiverso.normdataimporter.model.TagDescription;
import de.sub.goobi.metadaten.Metadaten;
import de.sub.goobi.metadaten.MetadatenHelper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import ugh.dl.DocStruct;
import ugh.dl.Metadata;
import ugh.dl.NamePart;
import ugh.dl.Person;

@PluginImplementation
@EqualsAndHashCode(callSuper = false)
public @Data class PersonPlugin extends ViafInputPlugin implements IPersonPlugin {

    private Person person;
    protected Metadaten bean;

    private String searchValue;
    private String searchOption;
    private List<List<NormData>> dataList;
    private List<NormData> currentData;

    private DocStruct docStruct;
    private MetadatenHelper metadatenHelper;
    private boolean showNotHits = false;

    private boolean isSearchInViaf;

    public PersonPlugin() {
        super();
        mainTagList = new ArrayList<>();
        mainTagList.add(new TagDescription("100", "_", "_", "a", null));
        mainTagList.add(new TagDescription("110", "_", "_", "a", null));
        mainTagList.add(new TagDescription("150", "_", "_", "a", null));
        mainTagList.add(new TagDescription("151", "_", "_", "a", null));
        mainTagList.add(new TagDescription("200", "_", "_", "a", null));
        mainTagList.add(new TagDescription("210", "_", "_", "a", null));
        mainTagList.add(new TagDescription("400", "_", "_", "a", null));
        mainTagList.add(new TagDescription("410", "_", "_", "a", null));
        mainTagList.add(new TagDescription("450", "_", "_", "a", null));
    }

    public ArrayList<SelectItem> getAddableRollen() {
        return this.metadatenHelper.getAddablePersonRoles(docStruct, person.getRole());
    }

    @Override
    public PluginType getType() {
        return PluginType.Metadata;
    }

    @Override
    public String getTitle() {
        return "PersonPlugin";
    }

    @Override
    public void setMetadata(Metadata metadata) {
        person = (Person) metadata;

    }

    @Override
    public Metadata getMetadata() {
        return person;
    }

    @Override
    public String getPagePath() {
        return "/uii/includes/metseditor/extension/formMetsPerson.xhtml";
    }

    @Override
    public String copy() {
        bean.setCurrentPerson(person);
        bean.CopyPerson();
        return "";
    }

    @Override
    public String delete() {
        bean.setCurrentPerson(person);
        bean.deletePerson();
        return "";
    }

    @Override
    public void setDefaultValue(String defaultValue) {
    }

    @Override
    public void setPossibleItems(List<SelectItem> items) {
    }

    @Override
    public void setDefaultItems(List<String> selectedItems) {
    }

    @Override
    public String getFirstname() {
        return person.getFirstname();
    }

    @Override
    public void setFirstname(String firstname) {
        person.setFirstname(firstname);
    }

    @Override
    public String getLastname() {

        return person.getLastname();
    }

    @Override
    public void setLastname(String lastname) {
        person.setLastname(lastname);

    }

    @Override
    public String getRole() {
        return person.getRole();
    }

    @Override
    public void setRole(String role) {
        person.setRole(role);
    }

    public void setNormDatabase(String abbrev) {
        NormDatabase database = NormDatabase.getByAbbreviation(abbrev);
        person.setAuthorityID(database.getAbbreviation());
        person.setAuthorityURI(database.getPath());
    }

    public String getNormDatabase() {
        return person.getAuthorityID();
    }

    public void addNamePart() {
        List<NamePart> parts = person.getAdditionalNameParts();
        if (parts == null) {
            parts = new ArrayList<>();
        }
        NamePart part = new NamePart();
        part.setType("date");
        parts.add(part);
        person.setAdditionalNameParts(parts);
    }

    @Override
    public String search() {
        if (isSearchInViaf) {
            return super.search();
        }

        String val = "";
        if (StringUtils.isBlank(searchOption) && StringUtils.isBlank(searchValue)) {
            showNotHits = true;
            return "";
        }
        if (StringUtils.isBlank(searchOption)) {
            val = "dnb.nid=" + searchValue;
        } else {
            val = searchValue + " and BBG=" + searchOption;
        }
        URL url = convertToURLEscapingIllegalCharacters("http://normdata.intranda.com/normdata/gnd/woe/" + val);
        String string = url.toString().replace("Ä", "%C3%84").replace("Ö", "%C3%96").replace("Ü", "%C3%9C").replace("ä", "%C3%A4").replace("ö",
                "%C3%B6").replace("ü", "%C3%BC").replace("ß", "%C3%9F");
        dataList = NormDataImporter.importNormDataList(string, 3);

        if (dataList == null || dataList.isEmpty()) {
            showNotHits = true;
        } else {
            showNotHits = false;
        }

        return "";
    }

    private URL convertToURLEscapingIllegalCharacters(String string) {
        try {
            String decodedURL = URLDecoder.decode(string, "UTF-8");
            URL url = new URL(decodedURL);
            URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
            return uri.toURL();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public String getData() {
        String mainValue = null;

        if (isSearchInViaf) {
            if (currentDatabase != null) {
                MarcRecord recordToImport = NormDataImporter.getSingleMarcRecord(currentDatabase.getMarcRecordUrl());

                List<String> names = new ArrayList<>();
                for (TagDescription tag : mainTagList) {
                    if (tag.getSubfieldCode() == null) {
                        String value = recordToImport.getControlfieldValue(tag.getDatafieldTag());
                        if (StringUtils.isNotBlank(value)) {
                            names.add(value);
                        }
                    } else {
                        List<String> list = recordToImport.getFieldValues(tag.getDatafieldTag(), tag.getInd1(), tag.getInd2(), tag.getSubfieldCode());
                        if (list != null) {
                            names.addAll(list);
                        }
                    }
                }

                if (!names.isEmpty()) {
                    person.setAutorityFile("viaf", "http://www.viaf.org/viaf/", currentDatabase.getMarcRecordUrl());
                    person.addAuthorityUriToMap("viaf-cluster", currentCluster);
                    person.addAuthorityUriToMap("viaf", currentDatabase.getMarcRecordUrl());
                    mainValue = names.get(0);
                }
            }
        }else {

            for (NormData normdata : currentData) {
                if (normdata.getKey().equals("NORM_IDENTIFIER")) {
                    person.setAutorityFile("gnd", "http://d-nb.info/gnd/", normdata.getValues().get(0).getText());
                } else if (normdata.getKey().equals("NORM_NAME")) {
                    mainValue = normdata.getValues().get(0).getText().replaceAll("\\x152", "").replaceAll("\\x156", "");
                }
            }
        }
        if (mainValue != null) {
            mainValue = filter(mainValue);
            if (mainValue.contains(",")) {
                person.setLastname(mainValue.substring(0, mainValue.lastIndexOf(",")).trim());
                person.setFirstname(mainValue.substring(mainValue.lastIndexOf(",") + 1).trim());
            } else if (mainValue.contains(" ")) {
                String[] nameParts = mainValue.split(" ");
                String first = "";
                String last = "";
                if (nameParts.length == 1) {
                    last = nameParts[0];
                } else if (nameParts.length == 2) {
                    first = nameParts[0];
                    last = nameParts[1];
                } else {
                    int counter = nameParts.length;
                    for (int i = 0; i < counter; i++) {
                        if (i == counter - 1) {
                            last = nameParts[i];
                        } else {
                            first += " " + nameParts[i];
                        }
                    }
                }
                person.setLastname(last);
                person.setFirstname(first);
            } else {
                person.setLastname(mainValue);
            }


            dataList = new ArrayList<>();

        }
        return "";

    }

    @Override
    public boolean isShowNoHitFound() {
        return showNotHits;
    }
}
