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
import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiFunction;

import de.sub.goobi.helper.Helper;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;

/** View model of the error page {@code uii/error.xhtml}. */
@Log4j2
@Named("ErrorPageBean")
@RequestScoped
public class ErrorPageBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String ATTRIBUTE_EXCEPTION = "jakarta.servlet.error.exception";
    private static final String ATTRIBUTE_STATUS = "jakarta.servlet.error.status_code";
    private static final String START_PAGE = "/uii/index.xhtml";
    private static final String TIME_PATTERN = "dd MMM yyyy, HH:mm:ss";

    private transient ErrorReport report;
    private boolean admin;
    private boolean loggedIn;
    private String contextPath = "";
    private Locale locale = Locale.ENGLISH;
    private ZoneId zone = ZoneId.systemDefault();
    private transient BiFunction<String, String[], String> translate = Helper::getTranslation;

    public ErrorPageBean() {
        // initialised in init()
    }

    ErrorPageBean(ErrorReport report, boolean admin, boolean loggedIn, String contextPath, Locale locale, ZoneId zone,
            BiFunction<String, String[], String> translate) {
        this.report = report;
        this.admin = admin;
        this.loggedIn = loggedIn;
        this.contextPath = contextPath;
        this.locale = locale;
        this.zone = zone;
        this.translate = translate;
    }

    @PostConstruct
    public void init() {
        FacesContext context = FacesContext.getCurrentInstance();
        ExternalContext external = context.getExternalContext();
        HttpServletRequest request = (HttpServletRequest) external.getRequest();
        contextPath = external.getRequestContextPath();
        Throwable exception = (Throwable) request.getAttribute(ATTRIBUTE_EXCEPTION);
        Integer status = (Integer) request.getAttribute(ATTRIBUTE_STATUS);
        if (exception == null && status == null && request.getAttribute(ErrorReporting.REQUEST_ATTRIBUTE) == null) {
            // direct visit or reload: nothing happened, nothing to log
            try {
                external.redirect(contextPath + START_PAGE);
            } catch (IOException e) {
                log.debug("Could not redirect from the error page to the start page", e);
            }
            return;
        }
        loggedIn = Helper.getCurrentUser() != null;
        admin = Helper.getLoginBean() != null && Helper.getLoginBean().hasRole("Admin_Menu");
        report = ErrorReporting.report(request, exception, status, Helper.getCurrentUser());
        if (context.getViewRoot() != null && context.getViewRoot().getLocale() != null) {
            locale = context.getViewRoot().getLocale();
        }
    }

    private ErrorEntry entry() {
        return report().error().entry();
    }

    private ErrorReport report() {
        if (report == null) {
            // only reached when the page is rendered without a request that caused it
            report = new ErrorReport(new ResolvedError(ErrorCatalogue.UNEXPECTED, List.of(), Map.of(), null), null,
                    Instant.now(), null);
        }
        return report;
    }

    private String translate(String key, String... params) {
        if (translate == null) {
            translate = Helper::getTranslation;
        }
        return translate.apply(key, params);
    }

    private boolean isTranslated(String key) {
        return !key.equals(translate(key));
    }

    public String getPageTitle() {
        return translate(entry().pageTitleKey());
    }

    public String getTitle() {
        return translate(entry().titleKey());
    }

    /** @return the affected object, for example the process, or null */
    public String getContext() {
        List<String> params = report().error().params();
        if (ErrorCatalogue.METADATA_UNREADABLE.key().equals(entry().key()) && params != null && params.size() >= 2) {
            return translate("process") + " " + params.get(0) + " (ID " + params.get(1) + ")";
        }
        return null;
    }

    public String getWhat() {
        if (!loggedIn && isTranslated(entry().anonWhatKey())) {
            return translate(entry().anonWhatKey());
        }
        return translate(entry().whatKey());
    }

    public String getWhy() {
        return translate(entry().whyKey());
    }

    public List<String> getHowSteps() {
        List<String> steps = new ArrayList<>();
        for (int i = 1; i <= entry().howSteps(); i++) {
            String key = entry().howKey(i);
            if (!loggedIn && isTranslated(entry().anonHowKey(i))) {
                key = entry().anonHowKey(i);
            }
            steps.add(translate(key));
        }
        return steps;
    }

    public List<String> getAdminSteps() {
        List<String> steps = new ArrayList<>();
        if (isShowAdmin()) {
            for (int i = 1; i <= entry().adminSteps(); i++) {
                steps.add(translate(entry().adminKey(i)));
            }
        }
        return steps;
    }

    public boolean isShowAdmin() {
        return admin && loggedIn && entry().adminSteps() > 0;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public String getSeverityCss() {
        return entry().severity().cssModifier();
    }

    public String getIcon() {
        return entry().icon();
    }

    public String getIllustrationUrl() {
        String illustration = entry().illustration();
        return illustration == null ? null : contextPath + "/uii/template/img/errors/" + illustration + ".svg";
    }

    public String getIncidentId() {
        return report().incidentId();
    }

    public String getIncidentTime() {
        return DateTimeFormatter.ofPattern(TIME_PATTERN, locale).withZone(zone).format(report().time());
    }

    /** @return text for support, only incident number, time and title and never technical details */
    public String getCopyText() {
        return "Incident " + getIncidentId() + ", " + getIncidentTime() + ", " + getTitle();
    }

    public String getBackLabel() {
        String key = entry().backLabelKey();
        return translate(isTranslated(key) ? key : "error_back");
    }

    public boolean isShowBack() {
        return loggedIn && !ErrorCatalogue.VIEW_EXPIRED.key().equals(entry().key());
    }

    public String getOpenAgainUrl() {
        if (ErrorCatalogue.VIEW_EXPIRED.key().equals(entry().key()) && report().expiredViewId() != null) {
            return contextPath + report().expiredViewId();
        }
        return null;
    }

    public String getStartUrl() {
        return contextPath + START_PAGE;
    }

    public String getStartLabel() {
        return translate(loggedIn ? "error_startPage" : "error_goToLogin");
    }

    public String getTemplate() {
        return loggedIn ? "/uii/template/template.html" : "/uii/template/template_blank.html";
    }
}
