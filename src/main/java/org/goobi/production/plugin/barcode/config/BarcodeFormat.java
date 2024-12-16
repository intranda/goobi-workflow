package org.goobi.production.plugin.barcode.config;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import de.sub.goobi.helper.GoobiScript;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Getter
@Setter
public class BarcodeFormat {
    @JacksonXmlProperty(isAttribute = true)
    private String pattern;
    @JacksonXmlText
    private String goobiScriptTemplate;

    /*
     * Custom setter to remove whitespaces at line start. The YAML parser will be angry otherwise.
     */
    public void setGoobiScriptTemplate(String template) {
        this.goobiScriptTemplate = template.replaceAll("(?m)^\\s+", "");
    }

    private transient Pattern regexPattern;
    private transient String goobiScript;

    public boolean patternMatches(String barcode) {
        return regexPattern().matcher(barcode).matches();
    }

    public void activate(String barcode) {
        List<String> parameters = determineParameters(barcode);
//        parameters.forEach(p -> System.err.println("Param: " + p));
        this.goobiScript = this.goobiScriptTemplate;
        for (int i = 0; i < parameters.size(); i++) {
            this.goobiScript = this.goobiScript.replaceAll("\\{\\{" + (i+1) + "\\}\\}", parameters.get(i));
        }
    }

    public void execute(String barcode) {
        GoobiScript gs = new GoobiScript();
        try {
            int processId = Integer.parseInt(barcode);
            String concreteGoobiScript = this.goobiScript.replaceAll("\\{\\{\\?\\}\\}", barcode);
            gs.execute(List.of(processId), concreteGoobiScript);
        } catch (NumberFormatException e) {
            log.error("Invalid process id \"{}\"", barcode);
        }
    }

    private List<String> determineParameters(String barcode) {
        Matcher m = regexPattern().matcher(barcode);
        if (!m.matches()) {
            throw new IllegalStateException("Should not reach this code if barcode format is not applicable");
        }

        List<String> parameters = new ArrayList<>(m.groupCount());
        for (int i = 1; i <= m.groupCount(); i++) {
            parameters.add(m.group(i));
        }
        return parameters;
    }

    private Pattern regexPattern() {
        if (this.regexPattern == null) {
            this.regexPattern = Pattern.compile(this.pattern);
        }
        return this.regexPattern;
    }
}
