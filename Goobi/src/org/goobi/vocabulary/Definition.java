package org.goobi.vocabulary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.faces.model.SelectItem;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Definition {
	private String label;
	private String type;
	private String validation;
	private String select;
	
	/**
	 * create a selectable list of items for select list
	 * @return List of select items
	 */
	public List<SelectItem> getSelectList() {
        List<SelectItem> list = new ArrayList<SelectItem>();
        List<String> items = Arrays.asList(select.split("\\|"));
        for (String s : items) {
            list.add(new SelectItem(s, s, null));
        }
        return list;
    }
}
