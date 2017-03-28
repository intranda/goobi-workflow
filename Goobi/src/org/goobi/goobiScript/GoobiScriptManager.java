package org.goobi.goobiScript;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.goobi.production.enums.GoobiScriptResultType;

import de.sub.goobi.helper.FacesContextHelper;
import lombok.Getter;
import lombok.Setter;

public class GoobiScriptManager {

	@Getter
	private List<GoobiScriptResult> goobiScriptResults = new ArrayList<>();
	@Getter @Setter
	private int showMax = 1000;
	
	@Getter
	@Setter
	private String sort = "";

	/**
	 * reset the list of all GoobiScriptResults
	 */
	public void goobiScriptResultsReset() {
		goobiScriptResults = new ArrayList<>();
		sort = "";
		showMax = 1000;
	}

	/**
	 * get just a limited number of results
	 */
	public List<GoobiScriptResult> getShortGoobiScriptResults(){
		if (showMax>goobiScriptResults.size()){
			return goobiScriptResults;
		} else {
			return goobiScriptResults.subList(0, showMax);
		}
	}
	
	/**
	 * Check if there are currently GoobiScripts in the list with a specific
	 * status
	 * 
	 * @param status
	 *            one of the {@link GoobiScriptResultType} values
	 * @return boolean if elements with this status exist
	 */
	public boolean goobiScriptHasResults(String status) {
		for (GoobiScriptResult gsr : goobiScriptResults) {
			if (gsr.getResultType().toString().equals(status)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Download current list of all results as Excel file
	 */
	public void goobiScriptResultsExcel() {
		FacesContext facesContext = FacesContextHelper.getCurrentFacesContext();
		if (!facesContext.getResponseComplete()) {
			HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();
			try {
				ServletContext servletContext = (ServletContext) facesContext.getExternalContext().getContext();
				String contentType = servletContext.getMimeType("goobiScript.xlsx");
				response.setContentType(contentType);
				response.setHeader("Content-Disposition", "attachment;filename=\"goobiScript.xlsx\"");
				ServletOutputStream out = response.getOutputStream();

				XSSFWorkbook workbook = new XSSFWorkbook();
				XSSFSheet sheet = workbook.createSheet("GoobiScript");

				XSSFRow rowhead = sheet.createRow((short) 0);
				rowhead.createCell(0).setCellValue("Process ID");
				rowhead.createCell(1).setCellValue("Process title");
				rowhead.createCell(2).setCellValue("Command");
				rowhead.createCell(3).setCellValue("Result");
				rowhead.createCell(4).setCellValue("Description");

				int count = 1;
				for (GoobiScriptResult gsr : goobiScriptResults) {
					XSSFRow row = sheet.createRow((short) count++);
					row.createCell(0).setCellValue(gsr.getProcessId());
					row.createCell(1).setCellValue(gsr.getProcessTitle());
					row.createCell(2).setCellValue(gsr.getCommand());
					row.createCell(3).setCellValue(gsr.getResultType().toString());
					row.createCell(4).setCellValue(gsr.getResultMessage());
				}

				workbook.write(out);
				out.flush();
				facesContext.responseComplete();
			} catch (IOException e) {

			}
		}
	}

	/**
	 * sort the list by specific value
	 */
	public void goobiScriptSort() {
		if (sort.equals("id")) {
			Collections.sort(goobiScriptResults, new SortByID());
		} else if (sort.equals("title")) {
			Collections.sort(goobiScriptResults, new SortByTitle());
		} else if (sort.equals("status")) {
			Collections.sort(goobiScriptResults, new SortByStatus());
		}else if (sort.equals("command")) {
			Collections.sort(goobiScriptResults, new SortByCommand());
		}else if (sort.equals("description")) {
			Collections.sort(goobiScriptResults, new SortByDescription());
		}
	}

	public class SortByStatus implements Comparator<GoobiScriptResult> {
		@Override
		public int compare(GoobiScriptResult g1, GoobiScriptResult g2) {
			return g1.getResultType().compareTo(g2.getResultType());
		}
	}

	public class SortByTitle implements Comparator<GoobiScriptResult> {
		@Override
		public int compare(GoobiScriptResult g1, GoobiScriptResult g2) {
			return g1.getProcessTitle().compareTo(g2.getProcessTitle());
		}
	}

	public class SortByID implements Comparator<GoobiScriptResult> {
		@Override
		public int compare(GoobiScriptResult g1, GoobiScriptResult g2) {
			return g1.getProcessId().compareTo(g2.getProcessId());
		}
	}
	
	public class SortByCommand implements Comparator<GoobiScriptResult> {
		@Override
		public int compare(GoobiScriptResult g1, GoobiScriptResult g2) {
			return g1.getCommand().compareTo(g2.getCommand());
		}
	}
	
	public class SortByDescription implements Comparator<GoobiScriptResult> {
		@Override
		public int compare(GoobiScriptResult g1, GoobiScriptResult g2) {
			return g1.getResultMessage().compareTo(g2.getResultMessage());
		}
	}
}
