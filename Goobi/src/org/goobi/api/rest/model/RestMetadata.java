package org.goobi.api.rest.model;

import java.util.Map;

import lombok.Data;

@Data
public class RestMetadata {
	private String value;
	private Map<String, String> labels;
	private String authorityID;
	private String authorityValue;
	private String authorityURI;

	public boolean anyValue() {
		return value != null || authorityID != null || authorityURI != null || authorityValue != null;
	}
}
