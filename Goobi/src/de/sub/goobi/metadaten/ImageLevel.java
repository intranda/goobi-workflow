package de.sub.goobi.metadaten;

import java.awt.Dimension;

public class ImageLevel implements Comparable<ImageLevel>{
    
    private String url;
    private Dimension size;
    private int rotation;
    
    public ImageLevel(String url, Dimension size) {
        super();
        this.url = url;
        this.size = size;
        this.rotation = 0;
    }
    
    public ImageLevel(String url, int width, int height) {
        super();
        this.url = url;
        this.size = new Dimension(width, height);
        this.rotation = 0;
    }

    public ImageLevel(String url, Dimension size, int currentRotate) {
        super();
        this.url = url;
        this.size = size;
        this.rotation = currentRotate;
    }

    public String getUrl() {
        return url;
    }

    public Dimension getSize() {
        if(rotation % 180 == 0) {            
            return size;
        } else {
            return new Dimension(size.height, size.width);
        }
    }
    
    public int getWidth() {
        return rotation%180 == 90 ? size.height : size.width;
    }
    
    public int getHeight() {
        return rotation%180 == 90 ? size.width : size.height;
    }

    @Override
    public String toString() {
        return "[\"" + url + "\"," + getWidth() + "," + getHeight() + "]";
    }

    @Override
    public int compareTo(ImageLevel other) {
        return Integer.compare(size.width*size.height, other.size.width*other.size.height);
    }

}
