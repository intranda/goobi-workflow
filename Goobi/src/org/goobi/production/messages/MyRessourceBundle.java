package org.goobi.production.messages;

import java.util.Enumeration;
import java.util.ResourceBundle;

import de.sub.goobi.helper.Helper;

public class MyRessourceBundle extends ResourceBundle {

	@Override
	protected Object handleGetObject(String key) {
		return Helper.getTranslation(key);
	}

	@Override
	public Enumeration<String> getKeys() {
		return null;
	}

}
