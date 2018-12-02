package org.goobi.production.properties;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- https://goobi.io
 * 			- https://www.intranda.com
 * 			- https://github.com/intranda/goobi
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

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.goobi.beans.Processproperty;
import org.goobi.beans.Step;

import lombok.Getter;
import lombok.Setter;


public class ProcessProperty implements IProperty, Serializable {

	private static final long serialVersionUID = 6413183995622426678L;
	@Getter @Setter
	private String name;
	@Getter @Setter
	private int container;
	@Getter @Setter
	private String validation;
	@Getter @Setter
	private Type type;
	@Getter
	private String value;
	@Getter @Setter
	private String readValue;
	@Getter @Setter
	private List<String> possibleValues;
	@Getter @Setter
	private List<String> projects;
	@Getter @Setter
	private List<String> workflows;
	@Getter @Setter
	private List<ShowStepCondition> showStepConditions;
	@Getter @Setter
	private AccessCondition showProcessGroupAccessCondition;
	@Getter @Setter
	private Processproperty prozesseigenschaft;
	@Getter @Setter
	private AccessCondition currentStepAccessCondition;
	@Getter @Setter
	private boolean duplicationAllowed = false;
	
	public ProcessProperty() {
		this.possibleValues = new ArrayList<String>();
		this.projects = new ArrayList<String>();
		this.workflows = new ArrayList<String>();
		this.showStepConditions = new ArrayList<ShowStepCondition>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.properties.IProperty#setValue(java.lang.String)
	 */
	@Override
	public void setValue(String value) {
		this.value = value;
		this.readValue = value;
	}
	
	public void setDateValue(Date inDate) {
		SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
		value= format.format(inDate);
		this.readValue = value;
	}

	
	public Date getDateValue() {
		SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
		try {
			Calendar cal = Calendar.getInstance();
			cal.setTime(format.parse(value));
			cal.set(Calendar.HOUR, 12);
			return cal.getTime();
		} catch (ParseException e) {
			return new Date();
		} catch (NullPointerException e) {
			return new Date();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.properties.IProperty#isValid()
	 */
	@Override
	public boolean isValid() {
		if (this.validation != null && this.validation.length() > 0) {
			Pattern pattern = Pattern.compile(this.validation);
			Matcher matcher = pattern.matcher(this.value);
			return matcher.matches();
		} else {
			return true;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.properties.IProperty#save(de.sub.goobi.Beans.Schritt)
	 */

	public void save(Step step) {
		if (this.prozesseigenschaft != null) {

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.properties.IProperty#getClone()
	 */
	@Override
	public ProcessProperty getClone(int containerNumber) {
		ProcessProperty p = new ProcessProperty();
		p.setContainer(containerNumber);
		p.setName(this.name);
		p.setValidation(this.validation);
		p.setType(this.type);
		p.setValue(this.value);
		p.setShowProcessGroupAccessCondition(this.showProcessGroupAccessCondition);
		p.setDuplicationAllowed(this.isDuplicationAllowed());
		p.setShowStepConditions(new ArrayList<ShowStepCondition>(getShowStepConditions()));
		p.setPossibleValues(new ArrayList<String>(getPossibleValues()));
		p.setProjects(new ArrayList<String>(getProjects()));
		p.setCurrentStepAccessCondition(currentStepAccessCondition);
		return p;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.properties.IProperty#transfer()
	 */
	@Override
	public void transfer() {
			this.prozesseigenschaft.setWert(this.value);
			this.prozesseigenschaft.setTitel(this.name);
			this.prozesseigenschaft.setContainer(this.container);
	}

	public List<String> getValueList() {
		List<String> answer = new ArrayList<String>();
		if (this.value != null && this.value.contains("; ")){
			String[] values = this.value.split("; ");
			for (String val : values) {
				answer.add(val);
			}
		}
		return answer;
	}

	public void setValueList(List<String> valueList) {
		this.value = "";
		for (String val : valueList) {
			this.value = this.value + val + "; ";
		}
		this.readValue = value;
	}

	public boolean getBooleanValue() {
		if (this.value != null && this.value.equalsIgnoreCase("true")) {
			return true;
		} else {
			return false;
		}
	}

	public void setBooleanValue(boolean val) {
		if (val) {
			this.value = "true";
		} else {
			this.value = "false";
		}
		this.readValue = value;
	}

	public static class CompareProperties implements Comparator<ProcessProperty>, Serializable {
		private static final long serialVersionUID = 8047374873015931547L;

		@Override
		public int compare(ProcessProperty o1, ProcessProperty o2) {
			return new Integer(o1.getContainer()).compareTo(new Integer(o2.getContainer()));
		}

	}

	public boolean getIsNew() {
		if (this.name == null || this.name.length() == 0) {
			return true;
		}
		return false;
	}

}
