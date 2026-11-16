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
package io.goobi.workflow.error;

import java.util.List;

/** The known kinds of error pages. */
public final class ErrorCatalogue {

    public static final ErrorEntry UNEXPECTED =
            new ErrorEntry("unexpected", ErrorSeverity.ERROR, "alert-octagon", null, true, 500, 2, 2, false);
    public static final ErrorEntry METADATA_UNREADABLE =
            new ErrorEntry("metadataUnreadable", ErrorSeverity.ERROR, "file-broken", "metadata-unreadable", true, 500, 3, 3, false);
    public static final ErrorEntry NOT_FOUND =
            new ErrorEntry("notFound", ErrorSeverity.INFO, "search", null, false, 404, 2, 0, false);
    public static final ErrorEntry FORBIDDEN =
            new ErrorEntry("forbidden", ErrorSeverity.WARNING, "lock", null, false, 403, 1, 0, false);
    public static final ErrorEntry VIEW_EXPIRED =
            new ErrorEntry("viewExpired", ErrorSeverity.INFO, "clock", null, false, 200, 2, 0, false);
    public static final ErrorEntry DATABASE_UNAVAILABLE =
            new ErrorEntry("databaseUnavailable", ErrorSeverity.ERROR, "database-off", null, true, 503, 2, 2, true);

    private static final List<ErrorEntry> ALL =
            List.of(UNEXPECTED, METADATA_UNREADABLE, NOT_FOUND, FORBIDDEN, VIEW_EXPIRED, DATABASE_UNAVAILABLE);

    private ErrorCatalogue() {
    }

    public static List<ErrorEntry> all() {
        return ALL;
    }
}
