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

import java.util.Map;

import org.apache.logging.log4j.ThreadContext;

import lombok.extern.log4j.Log4j2;

/** Writes error reports to the log, one entry per report. */
@Log4j2
public final class IncidentLogger {

    private static final String DASH = "-";
    private static final String THREAD_CONTEXT_KEY = "incident";

    /** Details of the request that failed, any value may be null. */
    public record RequestInfo(String userLogin, Integer userId, String method, String uri, String viewId, boolean ajax, String sessionIdShort) {
    }

    private IncidentLogger() {
    }

    private static String orDash(Object value) {
        return value == null ? DASH : value.toString();
    }

    private static String headline(ErrorReport report) {
        Throwable cause = report.error().cause();
        return cause != null && cause.getMessage() != null ? cause.getMessage() : report.error().entry().key();
    }

    /**
     * @param report the report
     * @param info details of the request
     * @return the text of the log entry
     */
    public static String format(ErrorReport report, RequestInfo info) {
        ErrorEntry entry = report.error().entry();
        StringBuilder sb = new StringBuilder();
        sb.append("Incident ").append(orDash(report.incidentId())).append(" [").append(entry.key()).append("] ").append(headline(report));
        String user = info.userLogin() == null ? DASH : info.userLogin() + " (id " + orDash(info.userId()) + ")";
        sb.append("\n  user:     ").append(user);
        sb.append("\n  request:  ").append(orDash(info.method())).append(' ').append(orDash(info.uri())).append(" (view ")
                .append(orDash(info.viewId())).append(", ajax: ").append(info.ajax() ? "yes" : "no").append(')');
        sb.append("\n  session:  ").append(orDash(info.sessionIdShort()));
        for (Map.Entry<String, String> e : report.error().logContext().entrySet()) {
            sb.append("\n  ").append(e.getKey()).append(": ").append(orDash(e.getValue()));
        }
        return sb.toString();
    }

    /**
     * Logs the report: incidents as ERROR with the throwable, not found as DEBUG, forbidden and expired views as INFO without throwable.
     *
     * @param report the report
     * @param info details of the request
     */
    public static void log(ErrorReport report, RequestInfo info) {
        ErrorEntry entry = report.error().entry();
        String text = format(report, info);
        if (report.incidentId() != null) {
            ThreadContext.put(THREAD_CONTEXT_KEY, report.incidentId());
        }
        try {
            if (entry.incident()) {
                log.error(text, report.error().cause());
            } else if (entry == ErrorCatalogue.NOT_FOUND) {
                log.debug(text);
            } else {
                log.info(text);
            }
        } finally {
            ThreadContext.remove(THREAD_CONTEXT_KEY);
        }
    }

    /**
     * Logs that the error page itself failed.
     *
     * @param report the report of the first error
     * @param secondFailure the failure of the error page
     */
    public static void logFollowUp(ErrorReport report, Throwable secondFailure) {
        String id = report.incidentId() != null ? report.incidentId() : report.error().entry().key();
        if (report.incidentId() != null) {
            ThreadContext.put(THREAD_CONTEXT_KEY, report.incidentId());
        }
        try {
            log.error("Error page failed for incident {}", id, secondFailure);
        } finally {
            ThreadContext.remove(THREAD_CONTEXT_KEY);
        }
    }
}
