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

import static org.easymock.EasyMock.createMock;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.sub.goobi.metadaten.Image;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
@ExtendWith(MockitoExtension.class)
public class ImageListTest {

    private ImageList imageList;

    private Image img1;
    private Image img2;
    private Image img3;

    private List<Image> images;

    @BeforeEach
    public void setUp() {
        imageList = new ImageList();

        img1 = createMock(Image.class);
        img2 = createMock(Image.class);
        img3 = createMock(Image.class);

        images = new ArrayList<>();
        images.add(img1);
        images.add(img2);
        images.add(img3);
    }

    @Test
    public void testSetImagesResetsIndex() {
        imageList.setImages(images);

        org.junit.Assert.assertEquals(0, imageList.getCurrentIndex());
        org.junit.Assert.assertEquals(3, imageList.getTotalCount());
    }

    @Test
    public void testGetTotalCountOnNull() {
        org.junit.Assert.assertEquals(0, new ImageList().getTotalCount());
    }

    @Test
    public void testIsFirst() {
        imageList.setImages(images);

        org.junit.Assert.assertTrue(imageList.isFirst());

        imageList.next();
        org.junit.Assert.assertFalse(imageList.isFirst());
    }

    @Test
    public void testIsLast() {
        imageList.setImages(images);

        org.junit.Assert.assertFalse(imageList.isLast());

        imageList.setCurrentIndex(2);
        org.junit.Assert.assertTrue(imageList.isLast());
    }

    @Test
    public void testGetCurrentImageViewerClosed() {
        imageList.setImages(images);

        // viewerOpen == false → should return null
        org.junit.Assert.assertNull(imageList.getCurrentImage());
    }

    @Test
    public void testGetCurrentImageViewerOpen() {
        imageList.setImages(images);
        imageList.setViewerOpen();

        org.junit.Assert.assertEquals(img1, imageList.getCurrentImage());
    }

    @Test
    public void testGetPreviousImage() {
        imageList.setImages(images);
        imageList.setCurrentIndex(1);

        org.junit.Assert.assertEquals(img1, imageList.getPreviousImage());
    }

    @Test
    public void testGetPreviousImageAtFirst() {
        imageList.setImages(images);
        imageList.setCurrentIndex(0);

        org.junit.Assert.assertNull(imageList.getPreviousImage());
    }

    @Test
    public void testGetNextImage() {
        imageList.setImages(images);
        imageList.setCurrentIndex(1);

        org.junit.Assert.assertEquals(img3, imageList.getNextImage());
    }

    @Test
    public void testGetNextImageAtLast() {
        imageList.setImages(images);
        imageList.setCurrentIndex(2);

        org.junit.Assert.assertNull(imageList.getNextImage());
    }

    @Test
    public void testPrevious() {
        imageList.setImages(images);
        imageList.setCurrentIndex(2);

        imageList.previous();
        org.junit.Assert.assertEquals(1, imageList.getCurrentIndex());
    }

    @Test
    public void testPreviousAtFirstDoesNothing() {
        imageList.setImages(images);
        imageList.setCurrentIndex(0);

        imageList.previous();
        org.junit.Assert.assertEquals(0, imageList.getCurrentIndex());
    }

    @Test
    public void testNext() {
        imageList.setImages(images);
        imageList.setCurrentIndex(0);

        imageList.next();
        org.junit.Assert.assertEquals(1, imageList.getCurrentIndex());
    }

    @Test
    public void testNextAtLastDoesNothing() {
        imageList.setImages(images);
        imageList.setCurrentIndex(2);

        imageList.next();
        org.junit.Assert.assertEquals(2, imageList.getCurrentIndex());
    }

    @Test
    public void testGetImageAtValidIndex() {
        imageList.setImages(images);
        org.junit.Assert.assertEquals(img2, imageList.getImageAt(1));
    }

    @Test
    public void testGetImageAtInvalidIndex() {
        imageList.setImages(images);

        org.junit.Assert.assertNull(imageList.getImageAt(-1));
        org.junit.Assert.assertNull(imageList.getImageAt(10));
    }

    @Test
    public void testSetCurrentIndexValidEnablesViewer() {
        imageList.setImages(images);
        org.junit.Assert.assertFalse(imageList.isViewerOpen());

        imageList.setCurrentIndex(1);

        org.junit.Assert.assertEquals(1, imageList.getCurrentIndex());
        org.junit.Assert.assertTrue(imageList.isViewerOpen());
    }

    @Test
    public void testSetCurrentIndexInvalid() {
        imageList.setImages(images);
        imageList.setCurrentIndex(100); // ignore

        org.junit.Assert.assertEquals(0, imageList.getCurrentIndex());
        org.junit.Assert.assertFalse(imageList.isViewerOpen());
    }

    @Test
    public void testSetViewerOpen() {
        imageList.setViewerOpen();
        org.junit.Assert.assertTrue(imageList.isViewerOpen());
    }

    @Test
    public void testClose() {
        imageList.setViewerOpen();
        imageList.close();

        org.junit.Assert.assertFalse(imageList.isViewerOpen());
    }

    @Test
    public void testClear() {
        imageList.setImages(images);
        imageList.setViewerOpen();
        imageList.setCurrentIndex(2);

        imageList.clear();

        org.junit.Assert.assertEquals(0, imageList.getCurrentIndex());
        org.junit.Assert.assertFalse(imageList.isViewerOpen());
        org.junit.Assert.assertEquals(0, imageList.getTotalCount());
    }

}
