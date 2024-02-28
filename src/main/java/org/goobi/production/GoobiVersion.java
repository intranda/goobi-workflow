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

package org.goobi.production;

import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.joda.time.DateTime;

import lombok.Getter;

public class GoobiVersion {

    private static DateTime now = DateTime.now();

    @Getter
    private static String version = "N/A";
    @Getter
    private static String buildversion = "N/A";
    // after a long discussion about earth climate change we decided that a subtraction of 2000 years is probably the easiest way
    // to get a two character year number. Just remember to change that value in 81 years from now on :)
    @Getter
    private static String publicVersion = String.format("%02d.%02d-dev", now.getYear() - 2000, now.getMonthOfYear());
    @Getter
    private static String builddate = "N/A";

    public static void setupFromManifest(Manifest manifest) throws IllegalArgumentException {
        Attributes mainAttributes = manifest.getMainAttributes();

        version = getOptionalValue(mainAttributes, "Implementation-Version").orElse(version);
        buildversion = version;
        builddate = getOptionalValue(mainAttributes, "Implementation-Build-Date").orElse(builddate);
        publicVersion = getOptionalValue(mainAttributes, "version").orElse(publicVersion);
    }

    private static Optional<String> getOptionalValue(Attributes attributes, String attributeName) throws IllegalArgumentException {
        String result = attributes.getValue(attributeName);
        return Optional.ofNullable(result);
    }
}
