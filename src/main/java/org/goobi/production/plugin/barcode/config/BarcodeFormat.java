package org.goobi.production.plugin.barcode.config;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class BarcodeFormat {
    private Pattern pattern;
    private String goobiScriptTemplate;
    private String goobiScript;

    public BarcodeFormat(String pattern, String goobiScript) {
        this.pattern = Pattern.compile(pattern);
        this.goobiScriptTemplate = goobiScript;
    }

    public boolean patternMatches(String barcode) {
        return this.pattern.matcher(barcode).matches();
    }

    public void process(String barcode) {
        List<String> parameters = determineParameters(barcode);
        parameters.forEach(p -> System.err.println("Param: " + p));
        String newGoobiScript = goobiScriptTemplate;
        for (int i = 0; i < parameters.size(); i++) {
            newGoobiScript = newGoobiScript.replaceAll("\\{\\{" + (i+1) + "\\}\\}", parameters.get(i));
        }
        goobiScript = newGoobiScript;
    }

    public void execute(String barcode) {
        System.err.println(goobiScript.replaceAll("\\{\\{\\?\\}\\}", barcode));
    }

    private List<String> determineParameters(String barcode) {
        Matcher m = this.pattern.matcher(barcode);
        if (!m.matches()) {
            throw new IllegalStateException("Should not reach this code if barcode format is not applicable");
        }

        List<String> parameters = new ArrayList<>(m.groupCount());
        for (int i = 1; i <= m.groupCount(); i++) {
            parameters.add(m.group(i));
        }
        return parameters;
    }
}
