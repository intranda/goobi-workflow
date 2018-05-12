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
	private int showMax = 100;
	
	@Getter	@Setter
	private String sort = "";

	/**
	 * reset the list of all GoobiScriptResults
	 */
	public void goobiScriptResultsReset() {
		goobiScriptResults = new ArrayList<>();
		sort = "";
		showMax = 100;
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
	public int getNumberOfFinishedScripts() {
		int count = 0;
		for (GoobiScriptResult gsr : goobiScriptResults) {
			if (gsr.getResultType() != GoobiScriptResultType.WAITING) {
				count++;
			}
		}
		return count;
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
				workbook.close();
			} catch (IOException e) {

			}
		}
	}

	/**
	 * sort the list by specific value
	 */
	public void goobiScriptSort() {
		if (sort.equals("id")) {
			Collections.sort(goobiScriptResults, new SortByID(false));
		} else if (sort.equals("id desc")) {
			Collections.sort(goobiScriptResults, new SortByID(true));
		} else if (sort.equals("title")) {
			Collections.sort(goobiScriptResults, new SortByTitle(false));
		} else if (sort.equals("title desc")) {
			Collections.sort(goobiScriptResults, new SortByTitle(true));
		} else if (sort.equals("status")) {
			Collections.sort(goobiScriptResults, new SortByStatus(false));
		} else if (sort.equals("status desc")) {
			Collections.sort(goobiScriptResults, new SortByStatus(true));
		} else if (sort.equals("command")) {
			Collections.sort(goobiScriptResults, new SortByCommand(false));
		} else if (sort.equals("command desc")) {
			Collections.sort(goobiScriptResults, new SortByCommand(true));
		
		} else if (sort.equals("user")) {
			Collections.sort(goobiScriptResults, new SortByUser(false));
		} else if (sort.equals("user desc")) {
			Collections.sort(goobiScriptResults, new SortByUser(true));
		
		} else if (sort.equals("timestamp")) {
			Collections.sort(goobiScriptResults, new SortByTimestamp(false));
		} else if (sort.equals("timestamp desc")) {
			Collections.sort(goobiScriptResults, new SortByTimestamp(true));
		
		} else if (sort.equals("description")) {
			Collections.sort(goobiScriptResults, new SortByDescription(false));
		} else if (sort.equals("description desc")) {
			Collections.sort(goobiScriptResults, new SortByDescription(true));
		}
	}

	private class SortByStatus implements Comparator<GoobiScriptResult> {
		
		private boolean reverse = false;
		
		public SortByStatus(boolean reverse) {
			this.reverse = reverse;
		}
		
		@Override
		public int compare(GoobiScriptResult g1, GoobiScriptResult g2) {
			if (reverse){
				return g1.getResultType().compareTo(g2.getResultType());
			} else{
				return g2.getResultType().compareTo(g1.getResultType());
			}
		}
	}

	private class SortByTitle implements Comparator<GoobiScriptResult> {
		private boolean reverse = false;
		
		public SortByTitle(boolean reverse) {
			this.reverse = reverse;
		}
		@Override
		public int compare(GoobiScriptResult g1, GoobiScriptResult g2) {
			if (reverse){
				return g1.getProcessTitle().compareTo(g2.getProcessTitle());
			} else{
				return g2.getProcessTitle().compareTo(g1.getProcessTitle());
			}
		}
	}

	private class SortByID implements Comparator<GoobiScriptResult> {
		private boolean reverse = false;
		
		public SortByID(boolean reverse) {
			this.reverse = reverse;
		}
		@Override
		public int compare(GoobiScriptResult g1, GoobiScriptResult g2) {
			if (reverse){
				return g1.getProcessId().compareTo(g2.getProcessId());
			} else{
				return g2.getProcessId().compareTo(g1.getProcessId());
			}
		}
	}
	
	private class SortByCommand implements Comparator<GoobiScriptResult> {
		private boolean reverse = false;
		
		public SortByCommand(boolean reverse) {
			this.reverse = reverse;
		}
		@Override
		public int compare(GoobiScriptResult g1, GoobiScriptResult g2) {
			if (reverse){
				return g1.getCommand().compareTo(g2.getCommand());
			} else{
				return g2.getCommand().compareTo(g1.getCommand());
			}
		}
	}
	
	private class SortByUser implements Comparator<GoobiScriptResult> {
		private boolean reverse = false;
		
		public SortByUser(boolean reverse) {
			this.reverse = reverse;
		}
		@Override
		public int compare(GoobiScriptResult g1, GoobiScriptResult g2) {
			if (reverse){
				return g1.getUsername().compareTo(g2.getUsername());
			} else{
				return g2.getUsername().compareTo(g1.getUsername());
			}
		}
	}
	
	private class SortByTimestamp implements Comparator<GoobiScriptResult> {
		private boolean reverse = false;
		
		public SortByTimestamp(boolean reverse) {
			this.reverse = reverse;
		}
		@Override
		public int compare(GoobiScriptResult g1, GoobiScriptResult g2) {
			if (reverse){
				return g1.getTimestamp().compareTo(g2.getTimestamp());
			} else{
				return g2.getTimestamp().compareTo(g1.getTimestamp());
			}
		}
	}
	
	private class SortByDescription implements Comparator<GoobiScriptResult> {
		private boolean reverse = false;
		
		public SortByDescription(boolean reverse) {
			this.reverse = reverse;
		}
		@Override
		public int compare(GoobiScriptResult g1, GoobiScriptResult g2) {
			if (reverse){
				return g1.getResultMessage().compareTo(g2.getResultMessage());
			} else{
				return g2.getResultMessage().compareTo(g1.getResultMessage());
			}
		}
	}

    public boolean getAreScriptsWaiting(String command) {
        boolean keepRunning = false;
        for (GoobiScriptResult gsr : goobiScriptResults) {
            if (gsr.getResultType() == GoobiScriptResultType.WAITING && gsr.getCommand().equals(command)) {
                keepRunning = true;
                break;
            }
        }
        return keepRunning;
    }
	
	
}
