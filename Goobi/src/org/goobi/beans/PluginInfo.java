package org.goobi.beans;

import java.util.Date;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PluginInfo {
    private String filename;
    private Date buildDate;
    private String gitHash;
}
