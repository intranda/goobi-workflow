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

package io.goobi.workflow.configeditor;

import java.nio.file.Path;

import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.StorageProviderInterface;
import lombok.Getter;
import lombok.Setter;

public class ConfigFile {

    @Getter
    @Setter
    private ConfigDirectory configDirectory;

    @Getter
    private String fileName;

    @Getter
    @Setter
    private Type type;

    @Getter
    @Setter
    private String lastModified;

    @Getter
    private boolean writable;

    public ConfigFile(Path path) {
        this(path, null);
    }

    public ConfigFile(Path path, Type type) {
        this.fileName = path.getFileName().toString();
        this.type = type;
        StorageProviderInterface provider = StorageProvider.getInstance();
        this.writable = provider.isWritable(path);
    }

    public enum Type {
        XML,
        PROPERTIES;

        /**
         * These strings are the mime types for the code mirror text editor
         */
        @Override
        public String toString() {
            switch (this) {
                case XML:
                    return "xml";
                case PROPERTIES:
                    return "text/x-properties";
                default:
                    return "";
            }
        }
    }

}