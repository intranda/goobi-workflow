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
import java.util.List;

import lombok.Data;

public @Data class Image {
    private String imageName;
    private int order;
    private String thumbnailUrl;
    private String largeThumbnailUrl;
    private String mainImageUrl;
    private String bookmarkUrl;
    private String tooltip;
    private Dimension size = null;

    public Image(String imageName, int order, String thumbnailUrl, String largeThumbnailUrl, String tooltip) {
        this.imageName = imageName;
        this.order = order;
        this.thumbnailUrl = thumbnailUrl;
        this.largeThumbnailUrl = largeThumbnailUrl;
        this.tooltip = tooltip;
    }

    public void addImageLevel(String imageUrl, int size) {

        this.mainImageUrl = imageUrl;
    }

}
