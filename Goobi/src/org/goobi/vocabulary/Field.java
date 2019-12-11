package org.goobi.vocabulary;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Field {
	private String label;
	private String value;
	private transient Definition definition;
	
	/**
	 * Simple getter to allow reading the current value as multi select field
	 * @return
	 */
	public String[] getValueMultiSelect() {
		return value.split("\\|");
	}

	/**
	 * simple setter to allow writing information as multi select field
	 * @param data
	 */
	public void setValueMultiSelect(String[] data) {
		if (data!=null && data.length>0) {
			value = "";
			for (int i = 0; i < data.length; i++) {
				value+=data[i] + "|";
			}
		}
	}
}
