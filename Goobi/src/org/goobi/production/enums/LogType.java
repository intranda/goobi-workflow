package org.goobi.production.enums;

import lombok.Getter;

public enum LogType {

    ERROR("error"),
    WARN("warn"),
    DEBUG("debug"),
    USER("user");

    @Getter
    private String title;

    private LogType(String title) {
        this.title = title;
    }

    public static LogType getByTitle(String title) {
        for (LogType t : LogType.values()) {
            if (t.getTitle().equals(title)) {
                return t;
            }
        }
        return null;
    }
}
