package de.sub.goobi.helper;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ShellScriptReturnValue {

    private int returnCode;
    private String outputText;
    private String errorText;



}
