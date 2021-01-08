/**
 * This file is part of the Goobi viewer - a content presentation and management application for digitized objects.
 *
 * Visit these websites for more information.
 *          - https://www.intranda.com
 *          - http://digiverso.com
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
package org.goobi.api.rest.object;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * @author Florian Alpers
 *
 */
public class ObjectInfo {

    private ObjectFormat format;
    private URI uri;
    private List<URI> resources;
    private Point3D center = new Point3D(0, 0, 0);
    private Point3D rotation = new Point3D(0, 0, 0);

    public ObjectInfo(URI uri) {
        this.uri = uri;
        this.format = ObjectFormat.getByFileExtension(uri.toString().substring(uri.toString().lastIndexOf("/")));

    }

    /**
     * @param objectURI
     * @throws URISyntaxException
     */
    public ObjectInfo(String uri) throws URISyntaxException {
        this.uri = new URI(uri);
        this.format = ObjectFormat.getByFileExtension(uri.toString().substring(uri.toString().lastIndexOf("/")));

    }

    /**
     * @return the type
     */
    public ObjectFormat getFormat() {
        return format;
    }

    /**
     * @param type the type to set
     */
    public void setFormat(ObjectFormat format) {
        this.format = format;
    }

    /**
     * @return the uri
     */
    public URI getUri() {
        return uri;
    }

    /**
     * @param uri the uri to set
     */
    public void setUri(URI uri) {
        this.uri = uri;
    }

    /**
     * @return the center
     */
    public Point3D getCenter() {
        return center;
    }

    /**
     * @param center the center to set
     */
    public void setCenter(Point3D center) {
        this.center = center;
    }

    /**
     * @return the rotation
     */
    public Point3D getRotation() {
        return rotation;
    }

    /**
     * @param rotation the rotation to set
     */
    public void setRotation(Point3D rotation) {
        this.rotation = rotation;
    }

    public List<URI> getResources() {
        return resources;
    }

    public void setResources(List<URI> resources) {
        this.resources = resources;
    }

}
