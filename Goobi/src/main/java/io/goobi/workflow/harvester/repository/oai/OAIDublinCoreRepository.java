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
package io.goobi.workflow.harvester.repository.oai;

import java.sql.Timestamp;

import org.joda.time.MutableDateTime;

import de.sub.goobi.helper.exceptions.HarvestException;
import de.sub.goobi.persistence.managers.HarvesterRepositoryManager;
import io.goobi.workflow.harvester.beans.Record;
import io.goobi.workflow.harvester.export.ExportOutcome;
import io.goobi.workflow.harvester.export.IConverter.ExportMode;
import io.goobi.workflow.harvester.helper.Utils;

/**
 * Abstract class for work with repository. All the logic about harvestering from repository is here. If you have some kind of "strange" Repository,
 * you have to extend this class.
 * 
 * @see MetsRepository
 * @see UnimatrixRepository
 * @see GeogreifRepository
 * 
 * @author Igor Toker
 * 
 */
public class OAIDublinCoreRepository extends OAIRepository {

    public static final String TYPE = "OAI_DC";
    private static final String METADATA_PREFIX = "oai_dc";

    /**
     * @param id {@link String}
     * @param name
     * @param url {@link String}
     * @param exportFolder
     * @param scriptPath
     * @param lastHarvest {@link Timestamp}
     * @param delay int
     * @param enabled int
     */
    public OAIDublinCoreRepository(Integer id, String name, String url, String exportFolder, String scriptPath, Timestamp lastHarvest, int frequency,
            int delay, boolean enabled) {
        super(id, name, url, exportFolder, scriptPath, lastHarvest, frequency, delay, enabled);
        getParameter().put("metadataPrefix", METADATA_PREFIX);
    }

    public OAIDublinCoreRepository() {
        getParameter().put("metadataPrefix", METADATA_PREFIX);
    }

    @Override
    public String getRepositoryType() {
        return TYPE;
    }

    @Override
    public boolean isAutoExport() {
        return false;
    }

    /**
     * Harvest the latest records from the repository and stores their metadata in the DB. The default implementation queries an OAI interface.
     * 
     * @param jobId
     * @throws DBException
     * @throws ParserException
     * @throws HarvestException
     */

    public int harvest(String jobId) {
        /* Get last harvestering timestamp */
        Timestamp lastHarvest = HarvesterRepositoryManager.getLastHarvest(getId());
        String url = getUrl().trim();
        String query;
        if (url.contains("?")) {
            query = url + "&metadataPrefix=oai_dc&verb=ListRecords";
        } else {
            query = url + "?metadataPrefix=oai_dc&verb=ListRecords";
        }
        //        if (lastHarvest != null) {
        //            String timeString = lastHarvest.toString();
        //            int i = timeString.indexOf(" ");
        //            timeString = timeString.substring(0, i);
        //            query += "&from=" + timeString;
        //        }
        if (lastHarvest != null) {
            MutableDateTime timestamp = Utils.formatterISO8601DateTimeMS.parseMutableDateTime(lastHarvest.toString());
            query += "&from=" + Utils.formatterISO8601DateTimeFullWithTimeZone.withZoneUTC().print(timestamp);
        }
        /* Harvest records to DB */
        try {
            return queryOAItoDB(query, jobId);
        } catch (HarvestException e) {
            return 0;
        }
    }

    @Override
    public ExportOutcome exportRecord(Record record, ExportMode mode) {
        // record.setExported(mode.toString());
        // record.setExportedDatestamp(new Date());

        return new ExportOutcome();
    }
}
