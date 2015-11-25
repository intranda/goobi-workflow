package de.sub.goobi.metadaten;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

public @Data class Image {
    private String imageName;
    private int order;
    private String thumbnailUrl;
    private List<ImageLevel> imageLevels = new ArrayList<ImageLevel>();
    private String tooltip;
    private Dimension size = null;

    public Image(String imageName, int order, String thumbnailUrl, String tooltip) {
        this.imageName = imageName;
        this.order = order;
        this.thumbnailUrl = thumbnailUrl;
        this.tooltip = tooltip;
    }

    public void addImageLevel(String imageUrl, int size) {

        double scale = size / (double) (Math.max(getSize().height, getSize().width));
        Dimension dim = new Dimension((int) (getSize().width * scale), (int) (getSize().height * scale));
        ImageLevel layer = new ImageLevel(imageUrl, dim);
        imageLevels.add(layer);
    }

}
