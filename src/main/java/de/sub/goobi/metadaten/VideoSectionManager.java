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

package de.sub.goobi.metadaten;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import de.sub.goobi.helper.Helper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import ugh.dl.DigitalDocument;
import ugh.dl.DocStruct;
import ugh.dl.DocStructType;
import ugh.dl.Metadata;
import ugh.dl.Prefs;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.TypeNotAllowedAsChildException;
import ugh.exceptions.TypeNotAllowedForParentException;

@Log4j2
public class VideoSectionManager {

    private static final String LOGICAL_PAGE_NUMBER = "logicalPageNumber";
    private static final String PHYSICAL_PAGE_NUMBER = "physPageNumber";
    private static final String UNCOUNTED = "uncounted";
    private static final String BEGIN_METADATA = "_BEGIN";
    private static final String END_METADARA = "_END";
    private static final String BETYPE_METADATA = "_BETYPE";

    @Getter
    @Setter
    private DocStruct newPageArea = null;
    private final Prefs prefs;
    private final DigitalDocument document;

    VideoSectionManager(Prefs prefs, DigitalDocument document) {
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

    public String getNewPageAreaLabel() {
        return Optional.ofNullable(getNewPageArea())
                .map(area -> Helper.getTranslation("mets_pageArea", MetadatenHelper.getSingleMetadataValue(area, LOGICAL_PAGE_NUMBER).orElse("")))
                .orElse("");
    }

    public DocStruct createVideoSection(DocStruct page, String begin, String end)
            throws TypeNotAllowedForParentException, MetadataTypeNotAllowedException, TypeNotAllowedAsChildException {
        DocStructType dst = prefs.getDocStrctTypeByName("area");
        DocStruct pageArea = document.createDocStruct(dst);
        Metadata logicalPageNumber = new Metadata(prefs.getMetadataTypeByName(LOGICAL_PAGE_NUMBER));
        logicalPageNumber.setValue(MetadatenHelper.getSingleMetadataValue(page, LOGICAL_PAGE_NUMBER).orElse(""));
        pageArea.addMetadata(logicalPageNumber);

        Metadata physPageNumber = new Metadata(prefs.getMetadataTypeByName(PHYSICAL_PAGE_NUMBER));
        physPageNumber.setValue(MetadatenHelper.getSingleMetadataValue(page, PHYSICAL_PAGE_NUMBER).orElse(""));
        pageArea.addMetadata(physPageNumber);
        Metadata mdBegin = new Metadata(prefs.getMetadataTypeByName(BEGIN_METADATA));
        mdBegin.setValue(begin);
        pageArea.addMetadata(mdBegin);
        if (StringUtils.isNotBlank(end)) {
            Metadata mdEnd = new Metadata(prefs.getMetadataTypeByName(END_METADARA));
            mdEnd.setValue(end);
            pageArea.addMetadata(mdEnd);
        }

        Metadata mdTime = new Metadata(prefs.getMetadataTypeByName(BETYPE_METADATA));
        mdTime.setValue("TIME");
        pageArea.addMetadata(mdTime);

        pageArea.setDocstructType("area");
        pageArea.setIdentifier(createPhysicalPageNumberForArea(pageArea, page));
        return pageArea;
    }

    public void assignToPhysicalDocStruct(DocStruct pageArea, DocStruct page) {
        try {
            page.addChild(pageArea);
            // order children by BEGIN time
            Collections.sort(page.getAllChildren(), new Comparator<DocStruct>() {

                @Override
                public int compare(DocStruct page1, DocStruct page2) {

                    String beginTimestamp1 = "";
                    String beginTimestamp2 = "";
                    for (Metadata md : page1.getAllMetadata()) {
                        if ("_BEGIN".equals(md.getType().getName())) {
                            beginTimestamp1 = md.getValue();
                            break;
                        }
                    }
                    for (Metadata md : page2.getAllMetadata()) {
                        if ("_BEGIN".equals(md.getType().getName())) {
                            beginTimestamp2 = md.getValue();
                            break;
                        }
                    }

                    return beginTimestamp1.compareTo(beginTimestamp2);
                }
            });

        } catch (TypeNotAllowedAsChildException e) {
            log.error("Could not add area to page ", e);
        }
    }

    public void assignToLogicalDocStruct(DocStruct pageArea, DocStruct logical) {
        logical.removeReferenceTo(pageArea.getParent());
        logical.addReferenceTo(pageArea, "logical_physical");
    }

    private String createPhysicalPageNumberForArea(DocStruct pageAreaStruct, DocStruct page) {
        String physicalPageNumber = page.getAllMetadata()
                .stream()
                .filter(md -> PHYSICAL_PAGE_NUMBER.equalsIgnoreCase(md.getType().getName()))
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
        if ("area".equalsIgnoreCase(pageAreaStruct.getDocstructType()) && pageAreaStruct.getParent() != null) {
            return pageAreaStruct.getAllMetadata()
                    .stream()
                    .filter(md -> BEGIN_METADATA.equalsIgnoreCase(md.getType().getName()))
                    .findAny()
                    .map(Metadata::getValue)
                    .orElse(" - ");
        } else {
            throw new IllegalArgumentException("given docStruct is not page area or has no parent");
        }
    }
}
