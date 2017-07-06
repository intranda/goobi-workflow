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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import lombok.Data;

public @Data class Image {
    private String imageName;
    private int order;
    private String thumbnailUrl;
    private String largeThumbnailUrl;
    private String objectUrl;
    private List<ImageLevel> imageLevels = new ArrayList<ImageLevel>();
    private String bookmarkUrl;
    private String tooltip;
    private Dimension size = null;
    private Type type;

    public Image(String imageName, int order, String thumbnailUrl, String largeThumbnailUrl, String tooltip) {
        this.imageName = imageName;
        this.type = getType(imageName);
        this.order = order;
        this.thumbnailUrl = thumbnailUrl;
        this.largeThumbnailUrl = largeThumbnailUrl;
        this.tooltip = tooltip;
    }

    private Type getType(String filename) {
        String extension = FilenameUtils.getExtension(filename);
        return Type.getFromFilenameExtension(extension);
    }

    public void addImageLevel(String imageUrl, int size) {

        double scale = size / (double) (Math.max(getSize().height, getSize().width));
        Dimension dim = new Dimension((int) (getSize().width * scale), (int) (getSize().height * scale));
        ImageLevel layer = new ImageLevel(imageUrl, dim);
        imageLevels.add(layer);
    }

    public boolean hasImageLevels() {
        return !imageLevels.isEmpty();
    }

    public enum Type {
        image("jpg", "tif", "png", "jp2"),
        object("obj", "stl", "ply", "fbx", "3ds"),
        unknown("");

        private final List<String> extensions;

        private Type(String... extensions) {
            this.extensions = Arrays.asList(extensions);
        }

        public static Type getFromFilenameExtension(String extension) {
            for (Type type : Type.values()) {
                if(type.extensions.contains(extension.toLowerCase())) {
                    return type;
                }
            }
            return unknown;
        }
    }

    public void setObjectUrl(String string) {
        this.objectUrl = string;
        
    }
    
    public String getObjectUrl() {
        return this.objectUrl;
    }

}
