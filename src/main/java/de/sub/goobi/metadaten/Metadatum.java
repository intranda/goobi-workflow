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
 * 
 */

package de.sub.goobi.metadaten;

import java.util.List;

import org.goobi.api.display.Item;

import jakarta.faces.model.SelectItem;
import ugh.dl.Metadata;

public interface Metadatum {

    List<Item> getWert();

    void setWert(String inWert);

    String getTyp();

    void setTyp(String inTyp);

    int getIdentifier();

    void setIdentifier(int identifier);

    Metadata getMd();

    void setMd(Metadata md);

    /*
     * 
     * new functions for use of display configuration whithin xml files
     * 
     */

    String getOutputType();

    List<SelectItem> getItems();

    void setItems(List<SelectItem> items);

    List<String> getSelectedItems();

    void setSelectedItems(List<String> selectedItems);

    String getSelectedItem();

    void setSelectedItem(String selectedItem);

    void setValue(String value);

    String getValue();

    boolean isDisplayRestrictions();

    boolean isRestricted();

    void setRestricted(boolean restricted);
}
