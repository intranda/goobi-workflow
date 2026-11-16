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

/**
 * One kind of error page. All texts are message keys derived from {@link #key()}.
 *
 * @param key key part of the messages, for example {@code notFound} for {@code error_notFound_title}
 * @param severity severity of the error
 * @param icon icon name shown in a tinted circle if no illustration is set
 * @param illustration illustration name, may be null
 * @param incident whether an incident number is created and shown
 * @param httpStatus HTTP status of the response
 * @param howSteps number of "what you can do" steps
 * @param adminSteps number of steps for administrators
 * @param standalone whether the error is rendered only as static fallback page
 */
public record ErrorEntry(String key, ErrorSeverity severity, String icon, String illustration, boolean incident, int httpStatus,
        int howSteps, int adminSteps, boolean standalone) {

    private String prefix() {
        return "error_" + key;
    }

    public String titleKey() {
        return prefix() + "_title";
    }

    public String pageTitleKey() {
        return prefix() + "_pageTitle";
    }

    public String whatKey() {
        return prefix() + "_what";
    }

    public String whyKey() {
        return prefix() + "_why";
    }

    /** @param n step number, 1-based */
    public String howKey(int n) {
        return prefix() + "_how_" + n;
    }

    /** @param n step number, 1-based */
    public String adminKey(int n) {
        return prefix() + "_admin_" + n;
    }

    /** Optional key, the page falls back to {@code error_back}. */
    public String backLabelKey() {
        return prefix() + "_back";
    }

    /**
     * Optional key for the steps shown to logged-out visitors, the page falls back to {@link #howKey(int)}.
     *
     * @param n step number, 1-based
     */
    public String anonHowKey(int n) {
        return prefix() + "_anonHow_" + n;
    }

    /** Optional key for the description shown to logged-out visitors, the page falls back to {@link #whatKey()}. */
    public String anonWhatKey() {
        return prefix() + "_anonWhat";
    }
}
