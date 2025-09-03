/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 *
 * Visit the websites for more information.
 *          - https://www.intranda.com
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 *
 */

package org.goobi.beans;

import java.io.Serializable;
import java.nio.file.Path;
import java.text.DateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.goobi.production.enums.LogType;

import de.sub.goobi.config.ConfigurationHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.JournalManager;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class JournalEntry implements Serializable {

    private static final long serialVersionUID = -5624248615174083906L;

    private Integer id;
    @NonNull
    private Integer objectId;
    @NonNull
    private Date creationDate;
    @NonNull
    private String userName;
    @NonNull
    private LogType type;
    @NonNull
    private String content;

    private String filename;

    @NonNull
    private EntryType entryType;

    // used only for LogType.File
    private transient Path file;

    public String getFormattedCreationDate() {
        return Helper.getDateAsFormattedString(creationDate);
    }

    public String getFormattedDate() {
        return DateFormat.getDateInstance().format(creationDate);
    }

    public String getCreationTime() {
        return DateFormat.getTimeInstance(DateFormat.MEDIUM).format(creationDate);
    }

    public void persist() {
        JournalManager.saveJournalEntry(this);
    }

    /**
     * Return the base name of a file. The basename is the name part of the file without the path
     *
     * @return basename of a file
     */

    public String getBasename() {
        String basename = filename;
        if (type == LogType.FILE && StringUtils.isNotBlank(filename)) {
            if (basename.contains("/")) {
                basename = basename.substring(basename.lastIndexOf("/") + 1);
            }
            if (basename.contains("\\")) {
                basename = basename.substring(basename.lastIndexOf("\\") + 1);
            }
        }
        return basename;
    }

    public boolean isExternalFile() {
        return StringUtils.isNotBlank(filename) && !filename.contains(ConfigurationHelper.getInstance().getFolderForInternalJournalFiles());
    }

    public String getFormattedContent() {
        if (content == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        String[] lines = content.split("\\n|<br\\/>");
        for (String line : lines) {
            if (sb.length() > 0) {
                sb.append("<br/>");
            }
            sb.append(StringEscapeUtils.escapeHtml4(line));
        }

        return sb.toString();
    }

    @AllArgsConstructor
    public enum EntryType {

        PROCESS("process"),
        INSTITUTION("institution"),
        USER("user"),
        PROJECT("project");

        @Getter
        private String title;

        public static EntryType getByTitle(String title) {
            for (EntryType type : values()) {
                if (type.getTitle().equals(title)) {
                    return type;
                }
            }
            return EntryType.PROCESS;
        }
    }

}
