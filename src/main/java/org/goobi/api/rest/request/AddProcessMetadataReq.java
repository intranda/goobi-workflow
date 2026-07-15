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
package org.goobi.api.rest.request;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.goobi.api.rest.model.RestMetadata;
import org.goobi.api.rest.response.UpdateMetadataResponse;
import org.goobi.beans.Process;

import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import lombok.Data;
import ugh.dl.DigitalDocument;
import ugh.dl.Fileformat;
import ugh.dl.Metadata;
import ugh.dl.MetadataGroup;
import ugh.dl.MetadataGroupType;
import ugh.dl.Prefs;
import ugh.exceptions.MetadataTypeNotAllowedException;
import ugh.exceptions.PreferencesException;
import ugh.exceptions.ReadException;
import ugh.exceptions.WriteException;

@Data
public class AddProcessMetadataReq {

    private Map<String, List<RestMetadata>> addMetadata;

    public UpdateMetadataResponse apply(Process p)
            throws IOException, InterruptedException, SwapException, DAOException, ReadException, PreferencesException, WriteException {
        UpdateMetadataResponse resp = new UpdateMetadataResponse();

        Prefs prefs = p.getRegelsatz().getPreferences();
        Fileformat ff = p.readMetadataFile();
        DigitalDocument dd = ff.getDigitalDocument();

        addMetadata(prefs, dd, resp);
        p.writeMetadataFile(ff);

        return resp;
    }

    private void addMetadata(Prefs prefs, DigitalDocument dd, UpdateMetadataResponse resp)
            throws IOException, InterruptedException, SwapException, DAOException, WriteException, PreferencesException {
        if (addMetadata != null) {
            for (String name : addMetadata.keySet()) {
                try {
                    for (RestMetadata rmd : addMetadata.get(name)) {
                        if (!rmd.anyValue()) {
                            continue;
                        }
                        if (name.contains("/")) {
                            //handle group
                            String[] split = name.split("/");
                            String group = split[0];
                            String metaName = split[1];
                            MetadataGroupType mgt = prefs.getMetadataGroupTypeByName(group);
                            MetadataGroup addGroup = null;
                            List<MetadataGroup> groups = dd.getLogicalDocStruct().getAllMetadataGroupsByType(mgt);
                            if (groups.isEmpty()) {
                                addGroup = new MetadataGroup(mgt);
                                dd.getLogicalDocStruct().addMetadataGroup(addGroup);
                            } else {
                                addGroup = groups.get(0);
                            }
                            Metadata md = new Metadata(prefs.getMetadataTypeByName(metaName));
                            addGroup.addMetadata(md);
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
                            dd.getLogicalDocStruct().addMetadata(md);
                        }
                    }
                } catch (MetadataTypeNotAllowedException e) {
                    resp.setError(true);
                    resp.addErrorMessage("Metadata not allowed: " + name);
                }
            }
        }
    }
}
