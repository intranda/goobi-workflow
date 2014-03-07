package org.goobi.production.flow.helper;

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
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
//import org.goobi.production.flow.statistics.hibernate.IEvaluableFilter;
//import org.goobi.production.flow.statistics.hibernate.UserDefinedFilter;
//import org.hibernate.Criteria;
//import org.hibernate.criterion.Order;
//import org.hibernate.criterion.Restrictions;

import org.goobi.beans.Process;
import org.goobi.beans.Processproperty;
import org.goobi.production.flow.statistics.hibernate.FilterHelper;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.ProcessManager;

public class ExtendedSearchResultGeneration {

    private String filter = "";
    private boolean showClosedProcesses = false;
    private boolean showArchivedProjects = false;

    public ExtendedSearchResultGeneration(String filter, boolean showClosedProcesses, boolean showArchivedProjects) {
        this.filter = filter;
        this.showClosedProcesses = showClosedProcesses;
        this.showArchivedProjects = showArchivedProjects;
    }

    @SuppressWarnings("deprecation")
    public HSSFWorkbook getResult(List<String> selectedColumnList) {

        boolean renderColumnTitle = false;
        boolean renderColumnId = false;
        boolean renderColumnDate = false;
        boolean renderColumnCountImages = false;
        boolean renderColumnCountMetadata = false;
        boolean renderColumnProject = false;
        boolean renderColumnStatus = false;
        boolean renderColumnAltRefNo = false;
        boolean renderColumnBnumber = false;

        int columnTitle = 0;
        int columnId = 0;
        int columnDate = 0;
        int columnCountImages = 0;
        int columnCountMetadata = 0;
        int columnProject = 0;
        int columnStatus = 0;
        int columnAltRefNo = 0;
        int columnBnumber = 0;

        int columnCounter = 0;
        for (String selectedColumn : selectedColumnList) {
            if (selectedColumn.equals(SearchColumnName.COLUMN_NAME_TITLE)) {
                columnTitle = columnCounter++;
                renderColumnTitle = true;
            } else if (selectedColumn.equals(SearchColumnName.COLUMN_NAME_ID)) {
                columnId = columnCounter++;
                renderColumnId = true;
            } else if (selectedColumn.equals(SearchColumnName.COLUMN_NAME_DATE)) {
                columnDate = columnCounter++;
                renderColumnDate = true;
            } else if (selectedColumn.equals(SearchColumnName.COLUMN_NAME_COUNT_IMAGES)) {
                columnCountImages = columnCounter++;
                renderColumnCountImages = true;
            } else if (selectedColumn.equals(SearchColumnName.COLUMN_NAME_COUNT_METADATA)) {
                columnCountMetadata = columnCounter++;
                renderColumnCountMetadata = true;
            } else if (selectedColumn.equals(SearchColumnName.COLUMN_NAME_PROJECT)) {
                columnProject = columnCounter++;
                renderColumnProject = true;
            } else if (selectedColumn.equals(SearchColumnName.COLUMN_NAME_STATUS)) {
                columnStatus = columnCounter++;
                renderColumnStatus = true;
            } else if (selectedColumn.equals(SearchColumnName.COLUMN_NAME_ALTREFNO)) {
                columnAltRefNo = columnCounter++;
                renderColumnAltRefNo = true;
            } else if (selectedColumn.equals(SearchColumnName.COLUMN_NAME_BNUMBER)) {
                columnBnumber = columnCounter++;
                renderColumnBnumber = true;
            }

        }

        String sql = FilterHelper.criteriaBuilder(filter, false, null, null, null, true, false);

        if (!sql.isEmpty()) {
            sql = sql + " AND ";
        }
        sql = sql + " prozesse.istTemplate = false ";

        if (!this.showClosedProcesses) {
            if (!sql.isEmpty()) {
                sql = sql + " AND ";
            }
            sql = sql + " prozesse.sortHelperStatus <> '100000000' ";
        }
        if (!this.showArchivedProjects) {
            if (!sql.isEmpty()) {
                sql = sql + " AND ";
            }
            sql = sql + " prozesse.ProjekteID not in (select ProjekteID from projekte where projectIsArchived = true) ";
        }

        List<Process> pl = ProcessManager.getProcesses("prozesse.titel", sql);

        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("Search results");

        HSSFRow title = sheet.createRow(0);
        HSSFCell titleCell1 = title.createCell(0);
        titleCell1.setCellValue(this.filter);
        HSSFCell titleCell2 = title.createCell(1);
        titleCell2.setCellValue("");
        HSSFCell titleCell3 = title.createCell(2);
        titleCell3.setCellValue("");
        HSSFCell titleCell4 = title.createCell(3);
        titleCell4.setCellValue("");
        HSSFCell titleCell5 = title.createCell(4);
        titleCell5.setCellValue("");
        HSSFCell titleCell6 = title.createCell(5);
        titleCell6.setCellValue("");

        HSSFCell titleCell7 = title.createCell(6);
        titleCell7.setCellValue("");
        HSSFCell titleCell8 = title.createCell(7);
        titleCell8.setCellValue("");

        HSSFCell titleCell9 = title.createCell(8);
        titleCell9.setCellValue("");

        HSSFRow row0 = sheet.createRow(1);

        if (renderColumnTitle) {
            HSSFCell headercell0 = row0.createCell(columnTitle);
            headercell0.setCellValue(Helper.getTranslation(SearchColumnName.COLUMN_NAME_TITLE));
        }
        if (renderColumnId) {
            HSSFCell headercell1 = row0.createCell(columnId);
            headercell1.setCellValue(Helper.getTranslation(SearchColumnName.COLUMN_NAME_ID));
        }
        if (renderColumnDate) {
            HSSFCell headercell2 = row0.createCell(columnDate);
            headercell2.setCellValue(Helper.getTranslation(SearchColumnName.COLUMN_NAME_DATE));
        }
        if (renderColumnCountImages) {
            HSSFCell headercell3 = row0.createCell(columnCountImages);
            headercell3.setCellValue(Helper.getTranslation(SearchColumnName.COLUMN_NAME_COUNT_IMAGES));
        }
        if (renderColumnCountMetadata) {
            HSSFCell headercell4 = row0.createCell(columnCountMetadata);
            headercell4.setCellValue(Helper.getTranslation(SearchColumnName.COLUMN_NAME_COUNT_METADATA));
        }
        if (renderColumnProject) {
            HSSFCell headercell5 = row0.createCell(columnProject);
            headercell5.setCellValue(Helper.getTranslation(SearchColumnName.COLUMN_NAME_PROJECT));
        }
        if (renderColumnStatus) {
            HSSFCell headercell6 = row0.createCell(columnStatus);
            headercell6.setCellValue(Helper.getTranslation(SearchColumnName.COLUMN_NAME_STATUS));
        }
        if (renderColumnAltRefNo) {
            HSSFCell headercell7 = row0.createCell(columnAltRefNo);
            headercell7.setCellValue(Helper.getTranslation(SearchColumnName.COLUMN_NAME_ALTREFNO));
        }
        if (renderColumnBnumber) {
            HSSFCell headercell8 = row0.createCell(columnBnumber);
            headercell8.setCellValue(Helper.getTranslation(SearchColumnName.COLUMN_NAME_BNUMBER));
        }
        int rowcounter = 2;
        for (Process p : pl) {
            HSSFRow row = sheet.createRow(rowcounter);
            if (renderColumnTitle) {
                HSSFCell cell0 = row.createCell(columnTitle);
                cell0.setCellValue(p.getTitel());
            }
            if (renderColumnId) {
                HSSFCell cell1 = row.createCell(columnId);
                cell1.setCellValue(p.getId());
            }
            if (renderColumnDate) {
                HSSFCell cell2 = row.createCell(columnDate);
                cell2.setCellValue(p.getErstellungsdatum().toGMTString());
            }
            if (renderColumnCountImages) {
                HSSFCell cell3 = row.createCell(columnCountImages);
                cell3.setCellValue(p.getSortHelperImages());
            }
            if (renderColumnCountMetadata) {
                HSSFCell cell4 = row.createCell(columnCountMetadata);
                cell4.setCellValue(p.getSortHelperDocstructs());
            }
            if (renderColumnProject) {
                HSSFCell cell5 = row.createCell(columnProject);
                cell5.setCellValue(p.getProjekt().getTitel());
            }
            if (renderColumnStatus) {
                HSSFCell cell6 = row.createCell(columnStatus);
                cell6.setCellValue(p.getSortHelperStatus().substring(0, 3) + " / " + p.getSortHelperStatus().substring(3, 6) + " / "
                        + p.getSortHelperStatus().substring(6));
            }
            HSSFCell cell7 = null;
            HSSFCell cell8 = null;
            if (renderColumnAltRefNo) {
                cell7 = row.createCell(columnAltRefNo);
                cell7.setCellValue("");
            }
            if (renderColumnBnumber) {
                cell8 = row.createCell(columnBnumber);
                cell8.setCellValue("");
            }
            if (p.getEigenschaftenList().size() > 0) {
                for (Processproperty pe : p.getEigenschaftenList()) {
                    if (pe.getTitel().equals("AltRefNo") && cell7 != null) {
                        cell7.setCellValue(pe.getWert());
                    } else if (pe.getTitel().equals("b-number") && cell8 != null) {
                        cell8.setCellValue(pe.getWert());
                    }
                }
            }

            rowcounter++;
        }

        return wb;
    }
}
