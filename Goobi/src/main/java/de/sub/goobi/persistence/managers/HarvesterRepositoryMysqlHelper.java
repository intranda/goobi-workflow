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
package de.sub.goobi.persistence.managers;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

import io.goobi.workflow.harvester.repository.Repository;
import io.goobi.workflow.harvester.repository.internetarchive.InternetArchiveCliRepository;
import io.goobi.workflow.harvester.repository.internetarchive.InternetArchiveRepository;
import io.goobi.workflow.harvester.repository.oai.GeogreifRepository;
import io.goobi.workflow.harvester.repository.oai.MetsModsRepository;
import io.goobi.workflow.harvester.repository.oai.MetsRepository;
import io.goobi.workflow.harvester.repository.oai.OAIDublinCoreRepository;
import io.goobi.workflow.harvester.repository.oai.UnimatrixRepository;
import io.goobi.workflow.harvester.repository.oai.viewer.IntrandaViewerCrowdsourcingRepository;
import io.goobi.workflow.harvester.repository.oai.viewer.IntrandaViewerOverviewPageRepository;
import lombok.extern.log4j.Log4j2;

@Log4j2
class HarvesterRepositoryMysqlHelper implements Serializable {

    private static final long serialVersionUID = -8160933323894230856L;

    public static Repository getRepository(String repositoryId) throws SQLException {
        Connection connection = null;
        try {
            connection = MySQLHelper.getInstance().getConnection();
            String sql = "SELECT * FROM repository WHERE id=?";
            List<Repository> retList = new QueryRunner().query(connection, sql, resultSetToRepositoriesList, repositoryId);
            if (!retList.isEmpty()) {
                return retList.get(0);
            } else {
                return null;
            }
        } finally {
            if (connection != null) {
                MySQLHelper.closeConnection(connection);
            }
        }
    }

    //    /**
    //     * Converts {@link ResultSet} to {@link ArrayList} of {@link Job}
    //     */
    //    public static ResultSetHandler<List<Job>> resultSetToJobList = new ResultSetHandler<List<Job>>() {
    //        @Override
    //        public List<Job> handle(ResultSet rs) throws SQLException {
    //            List<Job> retList = new ArrayList<>(rs.getFetchSize());
    //            while (rs.next()) {
    //                Job j = new Job(rs.getString("id"), rs.getString("status"), rs.getString("repository_id"), rs.getString("repository_name"),
    //                        rs.getString("message"), rs.getTimestamp("timestamp"));
    //                retList.add(j);
    //            }
    //            return retList;
    //        }
    //    };

    /**
     * Converts {@link ResultSet} to {@link ArrayList} of {@link OAIDublinCoreRepository}
     */
    public static ResultSetHandler<List<Repository>> resultSetToRepositoriesList = new ResultSetHandler<List<Repository>>() {
        @Override
        public List<Repository> handle(ResultSet rs) throws SQLException {
            List<Repository> retList = new ArrayList<>(rs.getFetchSize());
            while (rs.next()) {
                Repository r = null;
                String repositoryType = rs.getString("type");
                if (repositoryType != null) {
                    switch (repositoryType) {
                        case InternetArchiveRepository.TYPE:
                            // The Internet Archive
                            r = new InternetArchiveRepository(rs.getString("id"), rs.getString("name"), rs.getString("base_url"),
                                    rs.getString("export_folder"), rs.getString("script_path"), rs.getTimestamp("last_harvest"), rs.getInt("freq"),
                                    rs.getInt("delay"), rs.getBoolean("enabled"));
                            break;
                        case GeogreifRepository.TYPE:
                            // Geogreif
                            r = new GeogreifRepository(rs.getString("id"), rs.getString("name"), rs.getString("base_url"),
                                    rs.getString("script_path"), rs.getTimestamp("last_harvest"), rs.getInt("freq"), rs.getInt("delay"),
                                    rs.getBoolean("enabled"));
                            break;
                        case UnimatrixRepository.TYPE:
                            // Unimatrix
                            r = new UnimatrixRepository(rs.getString("id"), rs.getString("name"), rs.getString("base_url"),
                                    rs.getString("script_path"), rs.getTimestamp("last_harvest"), rs.getInt("freq"), rs.getInt("delay"),
                                    rs.getBoolean("enabled"));
                            break;
                        case IntrandaViewerOverviewPageRepository.TYPE:
                            // intranda viewer overview pages
                            r = new IntrandaViewerOverviewPageRepository(rs.getString("id"), rs.getString("name"), rs.getString("base_url"),
                                    rs.getString("script_path"), rs.getTimestamp("last_harvest"), rs.getInt("freq"), rs.getInt("delay"),
                                    rs.getBoolean("enabled"));
                            break;
                        case IntrandaViewerCrowdsourcingRepository.TYPE:
                            // intranda viewer crowsourcing updates
                            r = new IntrandaViewerCrowdsourcingRepository(rs.getString("id"), rs.getString("name"), rs.getString("base_url"),
                                    rs.getString("script_path"), rs.getTimestamp("last_harvest"), rs.getInt("freq"), rs.getInt("delay"),
                                    rs.getBoolean("enabled"));
                            break;
                        case MetsRepository.TYPE:
                            // METS
                            r = new MetsRepository(rs.getString("id"), rs.getString("name"), rs.getString("base_url"), rs.getString("export_folder"),
                                    rs.getString("script_path"), rs.getTimestamp("last_harvest"), rs.getInt("freq"), rs.getInt("delay"),
                                    rs.getBoolean("enabled"));
                            break;
                        case MetsModsRepository.TYPE:
                            r = new MetsModsRepository(rs.getString("id"), rs.getString("name"), rs.getString("base_url"),
                                    rs.getString("export_folder"), rs.getString("script_path"), rs.getTimestamp("last_harvest"), rs.getInt("freq"),
                                    rs.getInt("delay"), rs.getBoolean("enabled"));
                            break;
                        case InternetArchiveCliRepository.TYPE:
                            r = new InternetArchiveCliRepository(rs.getString("id"), rs.getString("name"), rs.getString("base_url"),
                                    rs.getString("export_folder"), rs.getString("script_path"), rs.getTimestamp("last_harvest"), rs.getInt("freq"),
                                    rs.getInt("delay"), rs.getBoolean("enabled"));
                            break;

                        default:
                            log.error("Cannot instantiate unknown repository type: {}", repositoryType);
                            break;
                    }
                }

                if (r != null) {
                    retList.add(r);
                }
                // TODO implement other repositories types
                // Repository r = new Repository(rs.getString("id"),
                // rs.getString("base_url"), rs.getTimestamp("last_harvest"),
                // rs.getInt("freq"),
                // rs.getBoolean("enabled"));
            }
            return retList;
        }
    };

    //    /**
    //     * Converts {@link ResultSet} to {@link ArrayList} of {@link Record}
    //     */
    //    public static ResultSetHandler<List<Record>> resultSetToRecordList = new ResultSetHandler<List<Record>>() {
    //        @Override
    //        public List<Record> handle(ResultSet rs) throws SQLException {
    //            List<Record> retList = new ArrayList<>(rs.getFetchSize());
    //            while (rs.next()) {
    //                Record r = new Record(rs.getString("id"), rs.getTimestamp("timestamp"), rs.getString("identifier"),
    //                        rs.getDate("repository_datestamp"), rs.getString("title"), rs.getString("creator"), rs.getString("repository_id"),
    //                        rs.getString("setSpec"), rs.getString("job_id"), rs.getString("source"), rs.getString("exported"),
    //                        rs.getTimestamp("exported_datestamp"), rs.getString("subquery"));
    //                retList.add(r);
    //            }
    //            return retList;
    //        }
    //    };

    //    /**
    //     * Converts {@link ResultSet} to {@link ArrayList} of {@link ExportHistoryEntry}
    //     */
    //    public static ResultSetHandler<List<ExportHistoryEntry>> resultSetToExportHistoryEntryList = new ResultSetHandler<List<ExportHistoryEntry>>() {
    //        @Override
    //        public List<ExportHistoryEntry> handle(ResultSet rs) throws SQLException {
    //            List<ExportHistoryEntry> retList = new ArrayList<>();
    //            while (rs.next()) {
    //                ExportHistoryEntry r = new ExportHistoryEntry(rs.getString("id"), rs.getTimestamp("timestamp"), rs.getString("record_id"),
    //                        rs.getString("record_identifier"), rs.getString("record_title"), rs.getString("repository_id"), rs.getString("status"),
    //                        rs.getString("message"));
    //                retList.add(r);
    //            }
    //            return retList;
    //        }
    //    };
}
