package de.sub.goobi.metadaten;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.Gson;

import lombok.Getter;
import lombok.Setter;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.DocStructType;
import ugh.dl.Metadata;
import ugh.dl.Prefs;
import ugh.dl.Reference;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.TypeNotAllowedAsChildException;
import ugh.exceptions.TypeNotAllowedForParentException;

public class PageAreaManager {

    private static final Logger logger = LogManager.getLogger(Metadaten.class);

    /**
     * Internal field to keep newly created page areas to be attached to a DocStruct yet to be created
     */
    @Getter
    @Setter
    private DocStruct newPageArea = null;
    private final Prefs prefs;
    private final DigitalDocument document;

    PageAreaManager(Prefs prefs, DigitalDocument document) {
        this.prefs = prefs;
        this.document = document;
    }

    public void resetNewPageArea() {
        this.newPageArea = null;
    }

    public boolean hasNewPageArea() {
        return this.newPageArea != null;
    }

    public PhysicalObject createPhysicalObject(DocStruct docStruct) {
        PhysicalObject pi = new PhysicalObject();
        pi.setDocStruct(docStruct);
        pi.setPhysicalPageNo(createPhysicalPageNumberForArea(docStruct));
        pi.setLogicalPageNo(createLogicalPageNumberForArea(docStruct));
        return pi;
    }

    public void setRectangle(String id, int x, int y, int w, int h, DocStruct page) {

        if (StringUtils.isNotBlank(id)) {
            if (hasNewPageArea() && id.equals(getNewPageArea().getIdentifier())) {
                setCoords(this.getNewPageArea(), x, y, w, h);
            } else {
                List<DocStruct> pageAreas = new ArrayList<>(page.getAllChildren());
                DocStruct area = pageAreas.stream().filter(a -> id.equals(a.getIdentifier())).findAny().orElse(null);
                if (area != null) {
                    setCoords(area, x, y, w, h);
                }
            }
        }
    }

    private void setCoords(DocStruct area, int x, int y, int w, int h) {
        for (Metadata md : area.getAllMetadataByType(prefs.getMetadataTypeByName("_COORDS"))) {
            md.setValue(x + "," + y + "," + w + "," + h);
        }
    }

    public String getRectangles(DocStruct page, DocStruct currentLogicalDocStruct) {
        JSONArray rectangles = new JSONArray();
        if (page == null || page.getAllChildren() == null) {
            return "";
        }
        List<DocStruct> pageAreas = new ArrayList<>(page.getAllChildren());
        for (DocStruct area : pageAreas) {
            String coordinates = MetadatenHelper.getSingleMetadataValue(area, "_COORDS").orElse(null);
            DocStruct logDocStruct = Optional.ofNullable(area.getAllFromReferences())
                    .flatMap(refs -> refs.stream().findFirst())
                    .map(Reference::getSource)
                    .orElse(null);

            JSONObject json = new JSONObject();
            String id = createPhysicalPageNumberForArea(area);
            area.setIdentifier(id);
            json.put("areaId", id);
            json.put("logId", logDocStruct.getIdentifier());
            if (logDocStruct != null && Objects.equals(logDocStruct, currentLogicalDocStruct)) {
                json.put("highlight", true);
            }

            String x = "";
            String y = "";
            String w = "";
            String h = "";
            if (StringUtils.isNotBlank(coordinates)) {

                Pattern pattern = Pattern.compile("(\\d+),(\\d+),(\\d+),(\\d+)");
                Matcher matcher = pattern.matcher(coordinates);

                if (matcher.matches()) {
                    x = matcher.group(1);
                    y = matcher.group(2);
                    w = matcher.group(3);
                    h = matcher.group(4);
                }

                json.put("x", x);
                json.put("y", y);
                json.put("w", w);
                json.put("h", h);

            }

            rectangles.put(json);
        }
        return rectangles.toString();
    }

    public DocStruct createPageArea(DocStruct page, Integer x, Integer y, Integer w, Integer h)
            throws TypeNotAllowedForParentException, MetadataTypeNotAllowedException, TypeNotAllowedAsChildException {
        DocStructType dst = prefs.getDocStrctTypeByName("area");
        DocStruct pageArea = document.createDocStruct(dst);
        Metadata logicalPageNumber = new Metadata(prefs.getMetadataTypeByName("logicalPageNumber"));
        logicalPageNumber.setValue(MetadatenHelper.getSingleMetadataValue(page, "logicalPageNumber").orElse(""));
        pageArea.addMetadata(logicalPageNumber);

        Metadata physPageNumber = new Metadata(prefs.getMetadataTypeByName("physPageNumber"));
        physPageNumber.setValue(MetadatenHelper.getSingleMetadataValue(page, "physPageNumber").orElse(""));
        pageArea.addMetadata(physPageNumber);
        Metadata md = new Metadata(prefs.getMetadataTypeByName("_COORDS"));
        md.setValue(x + "," + y + "," + w + "," + h);
        pageArea.addMetadata(md);
        pageArea.setDocstructType("area");
        pageArea.setIdentifier(createPhysicalPageNumberForArea(pageArea));
        return pageArea;
    }

    public void assignToPhysicalDocStruct(DocStruct pageArea, DocStruct page) {
        try {
            page.addChild(pageArea);
        } catch (TypeNotAllowedAsChildException e) {
            logger.error("Could not add area to page ", e);
        }
    }

    public void assignToLogicalDocStruct(DocStruct pageArea, DocStruct logical) {
        logical.removeReferenceTo(pageArea.getParent());
        logical.addReferenceTo(pageArea, "logical_physical");
    }

    private String createPhysicalPageNumberForArea(DocStruct pageAreaStruct) {
        if (pageAreaStruct.getDocstructType().equalsIgnoreCase("area") && pageAreaStruct.getParent() != null) {
            DocStruct page = pageAreaStruct.getParent();
            String physicalPageNumber = page.getAllMetadata()
                    .stream()
                    .filter(md -> md.getType().getName().equalsIgnoreCase("physPageNumber"))
                    .findAny()
                    .map(Metadata::getValue)
                    .orElse("uncounted");
            int indexOfArea = page.getAllChildren().indexOf(pageAreaStruct);
            if (indexOfArea < 0) {
                indexOfArea = page.getAllChildren().size();
            }
            return physicalPageNumber + "_" + Integer.toString(indexOfArea + 1);
        } else {
            throw new IllegalArgumentException("given docStruct is not page area or has no parent");
        }
    }

    private String createLogicalPageNumberForArea(DocStruct pageAreaStruct) {
        if (pageAreaStruct.getDocstructType().equalsIgnoreCase("area") && pageAreaStruct.getParent() != null) {
            DocStruct page = pageAreaStruct.getParent();
            String logicalPageNumber = page.getAllMetadata()
                    .stream()
                    .filter(md -> md.getType().getName().equalsIgnoreCase("logicalPageNumber"))
                    .findAny()
                    .map(Metadata::getValue)
                    .orElse("uncounted");
            return logicalPageNumber;
        } else {
            throw new IllegalArgumentException("given docStruct is not page area or has no parent");
        }
    }

}
