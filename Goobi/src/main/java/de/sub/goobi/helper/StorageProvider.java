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

import javax.inject.Singleton;

import de.sub.goobi.config.ConfigurationHelper;

@Singleton
public class StorageProvider {
    private static StorageProviderInterface instance;

    public static enum StorageType {
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
        boolean fileOk = false;
        String prefix = ConfigurationHelper.getInstance().getImagePrefix();
        if (name.matches(prefix + "\\.[Tt][Ii][Ff][Ff]?")) {
            fileOk = true;
        } else if (name.matches(prefix + "\\.[jJ][pP][eE]?[gG]")) {
            fileOk = true;
        } else if (name.matches(prefix + "\\.[jJ][pP][2]")) {
            fileOk = true;
        } else if (name.matches(prefix + "\\.[pP][nN][gG]")) {
            fileOk = true;
        } else if (name.matches(prefix + "\\.[gG][iI][fF]")) {
            fileOk = true;
        } else if (name.matches(prefix + "\\.[pP][dD][fF]")) {
            fileOk = true;
        } else if (name.matches(prefix + "\\.[aA][vV][iI]")) {
            fileOk = true;
        } else if (name.matches(prefix + "\\.[mM][pP][eE]?[gG]")) {
            fileOk = true;
        } else if (name.matches(prefix + "\\.[mM][pP]4")) {
            fileOk = true;
        } else if (name.matches(prefix + "\\.[mM][pP]3")) {
            fileOk = true;
        } else if (name.matches(prefix + "\\.[wW][aA][vV]")) {
            fileOk = true;
        } else if (name.matches(prefix + "\\.[wW][mM][vV]")) {
            fileOk = true;
        } else if (name.matches(prefix + "\\.[fF][lL][vV]")) {
            fileOk = true;
        } else if (name.matches(prefix + "\\.[oO][gG][gG]")) {
            fileOk = true;
        } else if (name.matches(prefix + "\\.[dD][oO][cC][xX]?")) {
            fileOk = true;
        } else if (name.matches(prefix + "\\.[xX][lL][sS][xX]?")) {
            fileOk = true;
        } else if (name.matches(prefix + "\\.[pP][pP][tT][xX]?")) {
            fileOk = true;
        } else if (name.matches(prefix + "\\.[tT][xX][tT]")) {
            fileOk = true;
        } else if (name.matches(prefix + "\\.[xX][mM][lL]")) {
            fileOk = true;
        } else if (name.matches(prefix + "\\.[oO][bB][jJ]")) {
            fileOk = true;
        } else if (name.matches(prefix + "\\.[fF][bB][xX]")) {
            fileOk = true;
        } else if (name.matches(prefix + "\\.[pP][lL][yY]")) {
            fileOk = true;
        } else if (name.matches(prefix + "\\.[xX]3[dD]")) {
            fileOk = true;
        } else if (name.matches(prefix + "\\.[sS][tT][lL]")) {
            fileOk = true;
        } else if (name.matches(prefix + "\\.[Gg][Ll][Tt][Ff]")) {
            fileOk = true;
        } else if (name.matches(prefix + "\\.[Gg][Ll][bB]")) {
            fileOk = true;
        } else if (name.matches(prefix + "\\.[Mm][Xx][Ff]")) {
            fileOk = true;
        }

        return fileOk;
    }

}
