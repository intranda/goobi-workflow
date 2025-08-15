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
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
package de.sub.goobi.helper;

import de.sub.goobi.config.ConfigurationHelper;
import jakarta.inject.Singleton;

@Singleton
public final class StorageProvider {

    private StorageProvider() {
        // hide implicit public constructor
    }

    private static final String REGEX_AVI = "\\.[aA][vV][iI]";
    private static final String REGEX_DOCX = "\\.[dD][oO][cC][xX]?";
    private static final String REGEX_EPUB = "\\.[eE][pP][uU][bB]";
    private static final String REGEX_FBX = "\\.[fF][bB][xX]";
    private static final String REGEX_FLV = "\\.[fF][lL][vV]";
    private static final String REGEX_GIF = "\\.[gG][iI][fF]";
    private static final String REGEX_GLB = "\\.[Gg][lL][bB]";
    private static final String REGEX_GLTF = "\\.[gG][lL][tT][fF]";
    private static final String REGEX_JP2 = "\\.[jJ][pP][2]";
    private static final String REGEX_JPEG = "\\.[jJ][pP][eE]?[gG]";
    private static final String REGEX_MP3 = "\\.[mM][pP]3";
    private static final String REGEX_MP4 = "\\.[mM][pP]4";
    private static final String REGEX_MPEG = "\\.[mM][pP][eE]?[gG]";
    private static final String REGEX_MXF = "\\.[mM][xX][fF]";
    private static final String REGEX_OBJ = "\\.[oO][bB][jJ]";
    private static final String REGEX_OGG = "\\.[oO][gG][gG]";
    private static final String REGEX_PDF = "\\.[pP][dD][fF]";
    private static final String REGEX_PLY = "\\.[pP][lL][yY]";
    private static final String REGEX_PPTX = "\\.[pP][pP][tT][xX]?";
    private static final String REGEX_PNG = "\\.[pP][nN][gG]";
    private static final String REGEX_STL = "\\.[sS][tT][lL]";
    private static final String REGEX_TIFF = "\\.[tT][iI][fF][fF]?";
    private static final String REGEX_TXT = "\\.[tT][xX][tT]";
    private static final String REGEX_WAV = "\\.[wW][aA][vV]";
    private static final String REGEX_WMV = "\\.[wW][mM][vV]";
    private static final String REGEX_XLSX = "\\.[xX][lL][sS][xX]?";
    private static final String REGEX_X3D = "\\.[xX]3[dD]";
    private static final String REGEX_XML = "\\.[xX][mM][lL]";

    private static StorageProviderInterface instance;

    public enum StorageType {
        LOCAL,
        S3,
        BOTH
    }

    public static StorageProviderInterface getInstance() {
        if (instance == null) {
            if (ConfigurationHelper.getInstance().useS3()) {
                instance = new S3FileUtils();
            } else {
                instance = new NIOFileUtils();
            }
        }

        return instance;
    }

    public static boolean dataFilterString(String name) {
        String prefix = ConfigurationHelper.getInstance().getImagePrefix();
        boolean isAllowed = name.matches(prefix + REGEX_TIFF);
        isAllowed = isAllowed || name.matches(prefix + REGEX_JPEG);
        isAllowed = isAllowed || name.matches(prefix + REGEX_JP2);
        isAllowed = isAllowed || name.matches(prefix + REGEX_PNG);
        isAllowed = isAllowed || name.matches(prefix + REGEX_GIF);
        isAllowed = isAllowed || name.matches(prefix + REGEX_PDF);
        isAllowed = isAllowed || name.matches(prefix + REGEX_AVI);
        isAllowed = isAllowed || name.matches(prefix + REGEX_MPEG);
        isAllowed = isAllowed || name.matches(prefix + REGEX_MP4);
        isAllowed = isAllowed || name.matches(prefix + REGEX_MP3);
        isAllowed = isAllowed || name.matches(prefix + REGEX_WAV);
        isAllowed = isAllowed || name.matches(prefix + REGEX_WMV);
        isAllowed = isAllowed || name.matches(prefix + REGEX_FLV);
        isAllowed = isAllowed || name.matches(prefix + REGEX_OGG);
        isAllowed = isAllowed || name.matches(prefix + REGEX_DOCX);
        isAllowed = isAllowed || name.matches(prefix + REGEX_XLSX);
        isAllowed = isAllowed || name.matches(prefix + REGEX_PPTX);
        isAllowed = isAllowed || name.matches(prefix + REGEX_TXT);
        isAllowed = isAllowed || name.matches(prefix + REGEX_XML);
        isAllowed = isAllowed || name.matches(prefix + REGEX_OBJ);
        isAllowed = isAllowed || name.matches(prefix + REGEX_FBX);
        isAllowed = isAllowed || name.matches(prefix + REGEX_PLY);
        isAllowed = isAllowed || name.matches(prefix + REGEX_X3D);
        isAllowed = isAllowed || name.matches(prefix + REGEX_STL);
        isAllowed = isAllowed || name.matches(prefix + REGEX_GLTF);
        isAllowed = isAllowed || name.matches(prefix + REGEX_GLB);
        isAllowed = isAllowed || name.matches(prefix + REGEX_MXF);
        isAllowed = isAllowed || name.matches(prefix + REGEX_EPUB);
        return isAllowed;
    }
}
