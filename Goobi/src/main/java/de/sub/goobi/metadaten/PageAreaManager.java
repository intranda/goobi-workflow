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
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
package de.sub.goobi.metadaten;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import de.sub.goobi.helper.Helper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.DocStructType;
import ugh.dl.Metadata;
import ugh.dl.Prefs;
import ugh.dl.Reference;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.TypeNotAllowedAsChildException;
import ugh.exceptions.TypeNotAllowedForParentException;

@Log4j2
public class PageAreaManager {

    private static final String LOGICAL_PAGE_NUMBER = "logicalPageNumber";
    private static final String PHYSICAL_PAGE_NUMBER = "physPageNumber";
    private static final String UNCOUNTED = "uncounted";
    private static final String COORDS = "_COORDS";

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
        pi.setPhysicalPageNo(createPhysicalPageNumberForArea(docStruct, docStruct.getParent()));
        pi.setLogicalPageNo(createLogicalPageNumberForArea(docStruct));
        return pi;
    }

    public void setRectangle(String id, int x, int y, int w, int h, DocStruct page) {

        if (StringUtils.isNotBlank(id)) {
            if (hasNewPageArea() && id.equals(getNewPageArea().getIdentifier())) {
                setCoords(this.getNewPageArea(), x, y, w, h);
            } else {
                List<DocStruct> pageAreas = new ArrayList<>(Optional.ofNullable(page.getAllChildren()).orElse(Collections.emptyList()));
                DocStruct area = pageAreas.stream().filter(a -> id.equals(a.getIdentifier())).findAny().orElse(null);
                if (area != null) {
                    setCoords(area, x, y, w, h);
                }
            }
        }
    }

    private void setCoords(DocStruct area, int x, int y, int w, int h) {
        for (Metadata md : area.getAllMetadataByType(prefs.getMetadataTypeByName(COORDS))) {
            md.setValue(x + "," + y + "," + w + "," + h);
        }
    }

    public String getRectangles(DocStruct page, DocStruct currentLogicalDocStruct) {
        JSONArray rectangles = new JSONArray();
        if (page == null || page.getAllChildren() == null) {
            return "";
        }
        List<DocStruct> pageAreas = new ArrayList<>(page.getAllChildren());
        if (this.newPageArea != null) {
            pageAreas.add(newPageArea);
        }
        for (DocStruct area : pageAreas) {
            String coordinates = MetadatenHelper.getSingleMetadataValue(area, COORDS).orElse(null);

            List<DocStruct> referencedLogStructs = Optional.ofNullable(area.getAllFromReferences())
                    .orElse(Collections.emptyList())
                    .stream()
                    .map(Reference::getSource)
                    .collect(Collectors.toList());
            DocStruct logDocStruct = referencedLogStructs.isEmpty() ? null : referencedLogStructs.get(referencedLogStructs.size() - 1);

            JSONObject json = new JSONObject();
            String id = createPhysicalPageNumberForArea(area, page);
            area.setIdentifier(id);
            json.put("areaId", id);
            if (logDocStruct != null) {
                json.put("logId", logDocStruct.getIdentifier());
                if (logDocStruct != null && Objects.equals(logDocStruct, currentLogicalDocStruct)) {
                    json.put("highlight", true);
                }
            } else {
                json.put("label", getNewPageAreaLabel());
            }
            String x = "";
            String y = "";
            String w = "";
            String h = "";
            if (StringUtils.isNotBlank(coordinates)) {

                Pattern pattern = Pattern.compile("([0-9-]+),([0-9-]+),([0-9-]+),([0-9-]+)");
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

    public String getNewPageAreaLabel() {
        return Optional.ofNullable(getNewPageArea())
                .map(area -> Helper.getTranslation("mets_pageArea", MetadatenHelper.getSingleMetadataValue(area, LOGICAL_PAGE_NUMBER).orElse("")))
                .orElse("");
    }

    public DocStruct createPageArea(DocStruct page, Integer x, Integer y, Integer w, Integer h)
            throws TypeNotAllowedForParentException, MetadataTypeNotAllowedException, TypeNotAllowedAsChildException {
        DocStructType dst = prefs.getDocStrctTypeByName("area");
        DocStruct pageArea = document.createDocStruct(dst);
        Metadata logicalPageNumber = new Metadata(prefs.getMetadataTypeByName(LOGICAL_PAGE_NUMBER));
        logicalPageNumber.setValue(MetadatenHelper.getSingleMetadataValue(page, LOGICAL_PAGE_NUMBER).orElse(""));
        pageArea.addMetadata(logicalPageNumber);

        Metadata physPageNumber = new Metadata(prefs.getMetadataTypeByName(PHYSICAL_PAGE_NUMBER));
        physPageNumber.setValue(MetadatenHelper.getSingleMetadataValue(page, PHYSICAL_PAGE_NUMBER).orElse(""));
        pageArea.addMetadata(physPageNumber);
        Metadata md = new Metadata(prefs.getMetadataTypeByName(COORDS));
        md.setValue(x + "," + y + "," + w + "," + h);
        pageArea.addMetadata(md);
        pageArea.setDocstructType("area");
        pageArea.setIdentifier(createPhysicalPageNumberForArea(pageArea, page));
        return pageArea;
    }

    public void assignToPhysicalDocStruct(DocStruct pageArea, DocStruct page) {
        try {
            page.addChild(pageArea);
        } catch (TypeNotAllowedAsChildException e) {
            log.error("Could not add area to page ", e);
        }
    }

    public void assignToLogicalDocStruct(DocStruct pageArea, DocStruct logical) {
        logical.removeReferenceTo(pageArea.getParent());
        logical.addReferenceTo(pageArea, "logical_physical");
    }

    String createPhysicalPageNumberForArea(DocStruct pageAreaStruct, DocStruct page) {
        String physicalPageNumber = page.getAllMetadata()
                .stream()
                .filter(md -> md.getType().getName().equalsIgnoreCase(PHYSICAL_PAGE_NUMBER))
                .findAny()
                .map(Metadata::getValue)
                .orElse(UNCOUNTED);
        List<DocStruct> pageAreas = new ArrayList<>(Optional.ofNullable(page.getAllChildren()).orElse(Collections.emptyList()));
        int indexOfArea = pageAreas.size();
        if (pageAreaStruct != null && pageAreas.contains(pageAreaStruct)) {
            indexOfArea = pageAreas.indexOf(pageAreaStruct);
        }
        return physicalPageNumber + "_" + Integer.toString(indexOfArea + 1);
    }

    private String createLogicalPageNumberForArea(DocStruct pageAreaStruct) {
        if (pageAreaStruct.getDocstructType().equalsIgnoreCase("area") && pageAreaStruct.getParent() != null) {
            DocStruct page = pageAreaStruct.getParent();
            return page.getAllMetadata()
                    .stream()
                    .filter(md -> md.getType().getName().equalsIgnoreCase(LOGICAL_PAGE_NUMBER))
                    .findAny()
                    .map(Metadata::getValue)
                    .orElse(UNCOUNTED);
        } else {
            throw new IllegalArgumentException("given docStruct is not page area or has no parent");
        }
    }

}
