/**
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information.
 *             - https://goobi.io
 *             - https://www.intranda.com
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package de.sub.goobi.mock;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import org.goobi.api.display.Item;

import de.sub.goobi.metadaten.Metadatum;
import ugh.dl.Metadata;

public class MockMetadatum implements Metadatum {

    private String value;

    public MockMetadatum() {
    }

    public MockMetadatum(String value) {
        this.value = value;
    }

    @Override
    public int getIdentifier() {
        return 0;
    }

    @Override
    public List<SelectItem> getItems() {
        return null;
    }

    @Override
    public Metadata getMd() {
        return null;
    }

    @Override
    public String getOutputType() {
        return null;
    }

    @Override
    public String getSelectedItem() {
        return null;
    }

    @Override
    public List<String> getSelectedItems() {
        return null;
    }

    @Override
    public String getTyp() {
        return null;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public ArrayList<Item> getWert() {
        return null;
    }

    @Override
    public void setIdentifier(int identifier) {
    }

    @Override
    public void setItems(List<SelectItem> items) {
    }

    @Override
    public void setMd(Metadata md) {
    }

    @Override
    public void setSelectedItem(String selectedItem) {
    }

    @Override
    public void setSelectedItems(List<String> selectedItems) {
    }

    @Override
    public void setTyp(String inTyp) {
    }

    @Override
    public void setValue(String value) {
    }

    @Override
    public void setWert(String inWert) {
        value = inWert;
    }

    @Override
    public boolean isDisplayRestrictions() {
        return false;
    }

    @Override
    public boolean isRestricted() {
        return false;
    }

    @Override
    public void setRestricted(boolean restricted) {

    }

}
