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
 */
package org.goobi.beans;

import java.io.Serializable;
import java.util.List;
import jakarta.inject.Named;

import org.apache.deltaspike.core.api.scope.WindowScoped;

import de.sub.goobi.metadaten.Image;

@Named("ImageList")
@WindowScoped
public class ImageList implements Serializable {

    private static final long serialVersionUID = 123L;

    private List<Image> images;
    private int currentIndex = 0;
    private boolean viewerOpen = false;

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
        this.currentIndex = 0;
    }

    public int getTotalCount() {
        return images != null ? images.size() : 0;
    }

    public boolean isFirst() {
        return currentIndex == 0;
    }

    public boolean isLast() {
        return currentIndex == images.size() - 1;
    }

    public Image getCurrentImage() {
        if (images == null || images.isEmpty() || !viewerOpen) {
            return null;
        }
        return images.get(currentIndex);
    }

    public Image getPreviousImage() {
        if (isFirst()) {
            return null;
        }
        return images.get(currentIndex - 1);
    }

    public Image getNextImage() {
        if (isLast()) {
            return null;
        }
        return images.get(currentIndex + 1);
    }

    public void previous() {
        if (!isFirst()) {
            currentIndex--;
        }
    }

    public void next() {
        if (!isLast()) {
            currentIndex++;
        }
    }

    public Image getImageAt(int index) {
        if (images == null || index < 0 || index >= images.size()) {
            return null;
        }
        return images.get(index);
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(int currentIndex) {
        if (images != null && currentIndex >= 0 && currentIndex < images.size()) {
            this.currentIndex = currentIndex;
            this.viewerOpen = true;
        }
    }

    public boolean isViewerOpen() {
        return viewerOpen;
    }

    public void setViewerOpen() {
        this.viewerOpen = true;
    }

    public void close() {
        this.viewerOpen = false;
    }

    public void clear() {
        if (images != null) {
            images.clear();
        }
        currentIndex = 0;
        viewerOpen = false;
    }

}
