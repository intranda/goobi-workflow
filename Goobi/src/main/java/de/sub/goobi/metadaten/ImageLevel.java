package de.sub.goobi.metadaten;

/***************************************************************
 * Copyright notice
 *
 * (c) 2015 Robert Sehr <robert.sehr@intranda.com>
 *
 * All rights reserved
 *
 * This file is part of the Goobi project. The Goobi project is free software;
 * you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * The GNU General Public License can be found at
 * http://www.gnu.org/copyleft/gpl.html.
 *
 * This script is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * This copyright notice MUST APPEAR in all copies of this file!
 ***************************************************************/

import java.awt.Dimension;

import lombok.Getter;

public class ImageLevel implements Comparable<ImageLevel> {

    @Getter
    private String url;
    private Dimension size;

    public ImageLevel(String url, Dimension size) {
        super();
        this.url = url;
        this.size = size;
    }

    public ImageLevel(String url, int width, int height) {
        super();
        this.url = url;
        this.size = new Dimension(width, height);
    }

    public Dimension getSize() {
        return size;
    }

    public int getWidth() {
        return size.width;
    }

    public int getHeight() {
        return size.height;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{")
                .append("\"url\" : \"")
                .append(this.url)
                .append("\",")
                .append("\"width\" : ")
                .append(this.size.width)
                .append(",")
                .append("\"height\" : ")
                .append(this.size.height)
                .append("}");
        return sb.toString();
    }

    @Override
    public int compareTo(ImageLevel other) {
        return Integer.compare(size.width * size.height, other.size.width * other.size.height);
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || !(object.getClass().equals(this.getClass()))) {
            return false;
        } else if (object == this) {
            return true;
        }
        ImageLevel imageLevel = (ImageLevel) (object);
        return this.url.equals(imageLevel.url) && this.size.equals(imageLevel.size);
    }

    @Override
    public int hashCode() {
        int prime1 = 31;
        int prime2 = 37;
        return super.hashCode() + prime1 * this.url.hashCode() + prime2 * this.size.hashCode();
    }

}
