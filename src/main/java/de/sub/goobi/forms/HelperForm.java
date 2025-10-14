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
package de.sub.goobi.forms;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.apache.deltaspike.core.api.scope.WindowScoped;
import org.goobi.beans.Docket;
import org.goobi.beans.Ruleset;
import org.goobi.beans.Step;
import org.goobi.production.GoobiVersion;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.PluginLoader;
import org.reflections.Reflections;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.FacesContextHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.HelperSchritte;
import de.sub.goobi.helper.ScriptThreadWithoutHibernate;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.DocketManager;
import de.sub.goobi.persistence.managers.RulesetManager;
import de.sub.goobi.persistence.managers.StepManager;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import ugh.dl.Fileformat;

@Named("HelperForm")
@WindowScoped
public class HelperForm implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -3225651472111393183L;

    private Boolean massImportAllowed = null;

    @Getter
    @Setter
    private boolean showError = false;

    public static final String MAIN_JSF_PATH = "/newpages";

    /**
     * @Deprecated Use {@link HelperForm#getVersion()} instead.
     * @return version
     */
    @Deprecated(forRemoval = false)
    public String getBuildVersion() {
        return getVersion();
    }

    public String getVersion() {
        return GoobiVersion.getVersion();
    }

    public String getRevision() {
        return GoobiVersion.getRevision();
    }

    public String getApplicationHeaderTitle() {

        return ConfigurationHelper.getInstance().getApplicationHeaderTitle();
    }

    public String getApplicationTitle() {
        return ConfigurationHelper.getInstance().getApplicationTitle();
    }

    public String getApplicationWebsiteUrl() {
        return getServletPathAsUrl();
    }

    public String getApplicationWebsiteMsg() {
        String rueck = ConfigurationHelper.getInstance().getApplicationWebsiteMsg();
        return Helper.getTranslation(rueck);
    }

    public String getApplicationHomepageMsg() {
        String rueck = ConfigurationHelper.getInstance().getApplicationHomepageMsg();
        return Helper.getTranslation(rueck);
    }

    public boolean getAnonymized() {
        return ConfigurationHelper.getInstance().isAnonymizeData();
    }

    public List<SelectItem> getRegelsaetze() throws DAOException {
        List<SelectItem> myPrefs = new ArrayList<>();
        List<Ruleset> temp = RulesetManager.getRulesets("titel", null, null, null,
                Helper.getCurrentUser().isSuperAdmin() ? null : Helper.getCurrentUser().getInstitution());
        for (Ruleset an : temp) {
            myPrefs.add(new SelectItem(an, an.getTitel(), null));
        }
        return myPrefs;
    }

    public List<SelectItem> getDockets() {
        List<SelectItem> answer = new ArrayList<>();
        try {
            List<Docket> temp = DocketManager.getDockets("name", null, null, null,
                    Helper.getCurrentUser().isSuperAdmin() ? null : Helper.getCurrentUser().getInstitution());
            for (Docket d : temp) {
                answer.add(new SelectItem(d, d.getName(), null));
            }
        } catch (DAOException e) {
            // ignore this and do nothing
        }

        return answer;
    }

    public List<SelectItem> getFileFormats() {
        ArrayList<SelectItem> ffs = new ArrayList<>();

        Set<Class<? extends Fileformat>> formatSet = new Reflections("ugh.fileformats.*").getSubTypesOf(Fileformat.class);
        for (Class<? extends Fileformat> cl : formatSet) {
            try {
                Fileformat ff = cl.getDeclaredConstructor().newInstance();
                if (ff.isExportable()) {
                    ffs.add(new SelectItem(ff.getDisplayName(), null));
                }
            } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | InvocationTargetException | NoSuchMethodException
                    | SecurityException e) {
                // ignore this and do nothing
            }
        }
        return ffs;
    }

    public List<SelectItem> getFileFormatsInternalOnly() {
        ArrayList<SelectItem> ffs = new ArrayList<>();

        Set<Class<? extends Fileformat>> formatSet = new Reflections("ugh.fileformats.*").getSubTypesOf(Fileformat.class);
        for (Class<? extends Fileformat> cl : formatSet) {
            try {
                Fileformat ff = cl.getDeclaredConstructor().newInstance();
                if (ff.isWritable()) {
                    ffs.add(new SelectItem(ff.getDisplayName(), null));
                }
            } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | InvocationTargetException | NoSuchMethodException
                    | SecurityException e) {
                // ignore this and do nothing
            }

        }
        return ffs;
    }

    public List<SelectItem> getStepStatusList() {
        List<SelectItem> ssl = new ArrayList<>();

        SelectItem locked = new SelectItem("0", Helper.getTranslation("statusGesperrt"));
        ssl.add(locked);

        SelectItem open = new SelectItem("1", Helper.getTranslation("statusOffen"));
        ssl.add(open);

        SelectItem inWork = new SelectItem("2", Helper.getTranslation("statusInBearbeitung"));
        ssl.add(inWork);

        ssl.add(new SelectItem("6", Helper.getTranslation("statusInFlight")));

        SelectItem finished = new SelectItem("3", Helper.getTranslation("statusAbgeschlossen"));
        ssl.add(finished);

        SelectItem error = new SelectItem("4", Helper.getTranslation("statusError"));
        ssl.add(error);

        SelectItem deactivated = new SelectItem("5", Helper.getTranslation("statusDeactivated"));
        ssl.add(deactivated);

        return ssl;
    }

    public List<SelectItem> getStepPriorityList() {
        List<SelectItem> ssl = new ArrayList<>();
        SelectItem s1 = new SelectItem("0", Helper.getTranslation("normalePrioritaet"));
        ssl.add(s1);
        SelectItem s2 = new SelectItem("1", Helper.getTranslation("badgePriority1"));
        ssl.add(s2);
        SelectItem s3 = new SelectItem("2", Helper.getTranslation("badgePriority2"));
        ssl.add(s3);
        SelectItem s4 = new SelectItem("3", Helper.getTranslation("badgePriority3"));
        ssl.add(s4);
        SelectItem s5 = new SelectItem("10", Helper.getTranslation("badgeCorrection"));
        ssl.add(s5);
        return ssl;
    }

    public String getServletPathAsUrl() {
        FacesContext context = FacesContextHelper.getCurrentFacesContext();
        ExternalContext external = context.getExternalContext();
        return external.getRequestContextPath() + "/";
    }

    public String getItmPathAsUrl() {
        FacesContext context = FacesContextHelper.getCurrentFacesContext();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        String scheme = request.getScheme(); // http
        String serverName = request.getServerName(); // hostname.com
        int serverPort = request.getServerPort(); // 80
        return scheme + "://" + serverName + ":" + serverPort + "/itm/";
    }

    public String getServletPathWithHostAsUrl() {
        FacesContext context = FacesContextHelper.getCurrentFacesContext();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        String scheme = request.getScheme(); // http
        String serverName = request.getServerName(); // hostname.com
        int serverPort = request.getServerPort(); // 80
        String contextPath = request.getContextPath(); // /mywebapp
        return scheme + "://" + serverName + ":" + serverPort + contextPath;
    }

    public String getContextPath() {
        FacesContext context = FacesContextHelper.getCurrentFacesContext();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        return request.getContextPath();
    }

    public boolean getMessagesExist() {
        return FacesContextHelper.getCurrentFacesContext().getMessages().hasNext();
    }

    public TimeZone getTimeZone() {
        return TimeZone.getDefault();
    }

    public boolean getMassImportAllowed() {
        if (massImportAllowed == null) {
            if (ConfigurationHelper.getInstance().isMassImportAllowed()) {

                massImportAllowed = !PluginLoader.getPluginList(PluginType.Import).isEmpty();
            } else {
                massImportAllowed = false;
            }
        }
        return massImportAllowed;
    }

    public boolean getIsIE() {
        FacesContext context = FacesContextHelper.getCurrentFacesContext();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        return request.getHeader("User-Agent").contains("MSIE");
    }

    public String getUserAgent() {
        FacesContext context = FacesContextHelper.getCurrentFacesContext();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();

        return request.getHeader("User-Agent");
    }

    public boolean isPasswordIsChangable() {
        return !Helper.getCurrentUser().getLdapGruppe().isReadonly();
    }

    public boolean isUseUii() {
        return ConfigurationHelper.getInstance().isUseIntrandaUi();
    }

    public boolean isRenderAccessibilityCss() {
        return ConfigurationHelper.getInstance().isRenderAccessibilityCss();
    }

    public void executeScriptsForStep(int id) {
        Step step = StepManager.getStepById(id);
        ScriptThreadWithoutHibernate thread = new ScriptThreadWithoutHibernate(step);
        thread.startOrPutToQueue();
    }

    public void executeHttpCallForStep(int id) {
        HelperSchritte hs = new HelperSchritte();
        Step s = StepManager.getStepById(id);
        s.setHttpCloseStep(false);
        hs.runHttpStep(s);
    }

    @Deprecated(since = "25.10", forRemoval = true)
    public List<SelectItem> getPossibleShortcuts() {
        List<SelectItem> ret = new ArrayList<>();
        ret.add(new SelectItem("ctrl", Helper.getTranslation("mets_key_ctrl")));
        ret.add(new SelectItem("alt", Helper.getTranslation("mets_key_alt")));
        ret.add(new SelectItem("ctrl+shift", Helper.getTranslation("mets_key_ctrlShift")));
        ret.add(new SelectItem("alt+shift", Helper.getTranslation("mets_key_altShift")));
        ret.add(new SelectItem("ctrl+alt", Helper.getTranslation("mets_key_ctrlAlt")));
        return ret;
    }

    /**
     *
     * @return build date written by the ant script
     */
    public String getBuildDate() {
        return GoobiVersion.getBuildDate();
    }

    /**
     * Receive a specific translation for a key including a prefix. And if it is missing respond the original key back again
     *
     * @param prefix
     * @param key
     *
     * @return translated value
     */
    public String getTranslation(String prefix, String key) {
        String result = Helper.getTranslation(prefix + key);
        if (result.startsWith(prefix)) {
            return key;
        } else {
            return result;
        }
    }

    public List<SelectItem> getTaskListColumnNames() {
        List<SelectItem> taskList = new ArrayList<>();
        taskList.add(new SelectItem("id", Helper.getTranslation("id")));
        taskList.add(new SelectItem("schritt", Helper.getTranslation("arbeitsschritt")));
        taskList.add(new SelectItem("prozess", Helper.getTranslation("prozessTitel")));
        taskList.add(new SelectItem("prozessdate", Helper.getTranslation("vorgangsdatum")));
        taskList.add(new SelectItem("projekt", Helper.getTranslation("projekt")));
        taskList.add(new SelectItem("institution", Helper.getTranslation("institution")));
        taskList.add(new SelectItem("sperrungen", Helper.getTranslation("sperrungen")));
        taskList.add(new SelectItem("batch", Helper.getTranslation("batch")));
        return taskList;
    }

    public boolean isShowEditionDataEnabled() {
        return ConfigurationHelper.getInstance().isProcesslistShowEditionData();
    }

    public List<FacesMessage> getFacesMessageList() {
        return getFacesMessageList(null);
    }

    public List<FacesMessage> getFacesMessageList(String clientId) {
        FacesContext context = FacesContextHelper.getCurrentFacesContext();
        if (StringUtils.isBlank(clientId)) {
            return context.getMessageList(null);
        } else {
            return context.getMessageList(clientId);
        }
    }

}
