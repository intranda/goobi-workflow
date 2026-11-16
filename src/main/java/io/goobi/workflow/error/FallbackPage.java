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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.log4j.Log4j2;

/**
 * The static error page for errors where the application cannot be trusted to render anything (database down, error page failed). Independent of
 * JSF, the database and the translation helper, so it works while everything else is broken.
 */
@Log4j2
public final class FallbackPage {

    private static final String TEMPLATE_RESOURCE = "/error-fallback.html";
    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{(\\w+)}");
    private static final String PARTIAL_START =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?><partial-response><changes><update id=\"jakarta.faces.ViewRoot\"><![CDATA[";
    private static final String PARTIAL_END = "]]></update></changes></partial-response>";

    private static final String COPY_ICON =
            "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 24 24\" fill=\"none\" stroke=\"currentColor\" stroke-width=\"2\" "
                    + "stroke-linecap=\"round\" stroke-linejoin=\"round\" aria-hidden=\"true\">"
                    + "<path d=\"M7 9.667A2.667 2.667 0 0 1 9.667 7h8.666A2.667 2.667 0 0 1 "
                    + "21 9.667v8.666A2.667 2.667 0 0 1 18.333 21H9.667A2.667 2.667 0 0 1 7 18.333z\"/><path d=\"M4.012 16.737A2.005 "
                    + "2.005 0 0 1 3 15V5c0-1.1.9-2 2-2h10c.75 0 1.158.385 1.5 1\"/></svg>";
    private static final String COPY_SCRIPT = "<script>function copyIncident(b){var t=b.getAttribute('data-copy-text');"
            + "var f=document.getElementById('fallback-feedback');var d=function(){f.textContent='';"
            + "window.setTimeout(function(){f.textContent=b.getAttribute('data-copied-label');},50);};"
            + "if(navigator.clipboard&&navigator.clipboard.writeText){navigator.clipboard.writeText(t).then(d,function(){});}}</script>";

    private static final Map<String, Map<String, String>> TEXTS = Map.of(
            "en", Map.of(
                    "title", "Goobi workflow is not available right now",
                    "what", "Goobi cannot reach its database, so no page can be shown at the moment.",
                    "whyHeading", "Why did this happen?",
                    "why", "The database is not responding. This is often caused by maintenance, a restart or a network problem between the servers.",
                    "howHeading", "What you can do",
                    "how1", "Wait a few minutes, then reload this page.",
                    "how2", "If the problem persists, tell an administrator and give them the incident number below.",
                    "reload", "Reload page",
                    "incident", "Incident"),
            "de", Map.of(
                    "title", "Goobi workflow ist momentan nicht verfügbar",
                    "what", "Goobi kann die Datenbank nicht erreichen, daher kann momentan keine Seite angezeigt werden.",
                    "whyHeading", "Warum ist das passiert?",
                    "why", "Die Datenbank antwortet nicht. Das liegt oft an Wartungsarbeiten, einem Neustart oder einem Netzwerkproblem "
                            + "zwischen den Servern.",
                    "howHeading", "Was Sie tun können",
                    "how1", "Warten Sie einige Minuten und laden Sie diese Seite dann neu.",
                    "how2", "Wenn das Problem weiterhin besteht, informieren Sie einen Administrator und nennen Sie dabei die unten "
                            + "angezeigte Vorfallnummer.",
                    "reload", "Seite neu laden",
                    "incident", "Vorfall"));

    private static final String COPIED_EN = "Copied";
    private static final String COPIED_DE = "Kopiert";

    private static String template;

    private FallbackPage() {
    }

    /**
     * @param report the error report
     * @param locale the language of the visitor, English if not supported
     * @return the complete HTML page
     */
    public static String render(ErrorReport report, Locale locale) {
        String lang = locale != null && "de".equals(locale.getLanguage()) ? "de" : "en";
        Map<String, String> texts = TEXTS.get(lang);
        Map<String, String> values = new java.util.HashMap<>();
        values.put("lang", lang);
        for (String key : new String[] { "title", "what", "whyHeading", "why", "howHeading", "reload" }) {
            values.put(key, escape(texts.get(key)));
        }
        values.put("how", "<li><span>" + escape(texts.get("how1")) + "</span></li>\n<li><span>" + escape(texts.get("how2")) + "</span></li>");
        values.put("incident", incidentChip(report, texts, "de".equals(lang) ? COPIED_DE : COPIED_EN));

        Matcher matcher = PLACEHOLDER.matcher(template());
        StringBuilder page = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(page, Matcher.quoteReplacement(values.getOrDefault(matcher.group(1), "")));
        }
        matcher.appendTail(page);
        return page.toString();
    }

    /**
     * @param report the error report
     * @param locale the language of the visitor
     * @return a JSF partial response that replaces the whole view with the page
     */
    public static String renderPartial(ErrorReport report, Locale locale) {
        // a CDATA section ends at the first "]]>", which the page can contain only through the incident number (escaped) - split to be safe
        return PARTIAL_START + render(report, locale).replace("]]>", "]]]]><![CDATA[>") + PARTIAL_END;
    }

    private static String incidentChip(ErrorReport report, Map<String, String> texts, String copied) {
        if (report == null || report.incidentId() == null) {
            return "";
        }
        String id = escape(report.incidentId());
        return "<div class=\"foot\"><button type=\"button\" class=\"chip\" onclick=\"copyIncident(this)\" data-copy-text=\"" + id
                + "\" data-copied-label=\"" + escape(copied) + "\"><span class=\"chip-label\">" + escape(texts.get("incident"))
                + "</span><span class=\"chip-id\">" + id + "</span>" + COPY_ICON + "</button>"
                + "<div class=\"feedback\" id=\"fallback-feedback\" aria-live=\"polite\"></div></div>" + COPY_SCRIPT;
    }

    static String escape(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder out = new StringBuilder(value.length() + 16);
        for (char c : value.toCharArray()) {
            switch (c) {
                case '&' -> out.append("&amp;");
                case '<' -> out.append("&lt;");
                case '>' -> out.append("&gt;");
                case '"' -> out.append("&quot;");
                case '\'' -> out.append("&#39;");
                default -> out.append(c);
            }
        }
        return out.toString();
    }

    private static synchronized String template() {
        if (template == null) {
            try (InputStream in = FallbackPage.class.getResourceAsStream(TEMPLATE_RESOURCE)) {
                if (in == null) {
                    throw new IOException("resource not found: " + TEMPLATE_RESOURCE);
                }
                template = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                log.error("Could not load the fallback error page template", e);
                template = "<!DOCTYPE html><html lang=\"${lang}\"><head><meta charset=\"UTF-8\"><title>${title}</title></head>"
                        + "<body class=\"login\"><h1>${title}</h1><p>${what}</p>${incident}</body></html>";
            }
        }
        return template;
    }
}
