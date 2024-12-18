package org.goobi.production.plugin.barcode.config;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import de.sub.goobi.helper.GoobiScript;
import de.sub.goobi.persistence.managers.ProcessManager;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.goobi.beans.Process;
import org.goobi.production.plugin.barcode.BarcodeScannerPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Getter
@Setter
public class BarcodeFormat {
    @JacksonXmlProperty(isAttribute = true)
    private String pattern;
    @JacksonXmlProperty(localName = "description", isAttribute = true)
    private String descriptionTemplate;
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
    @JacksonXmlProperty(localName = "_description", isAttribute = true)
    private transient String description;

    public boolean patternMatches(String barcode) {
        return regexPattern().matcher(barcode).matches();
    }

    public void activate(String barcode) {
        List<String> parameters = determineParameters(barcode);
//        parameters.forEach(p -> System.err.println("Param: " + p));
        this.description = fillParameters(this.descriptionTemplate, parameters);
        this.goobiScript = fillParameters(this.goobiScriptTemplate, parameters);
        BarcodeScannerPlugin.success("Barcode action \"" + this.description + "\" activated.");
    }

    private String fillParameters(String template, List<String> parameters) {
        String result = template;
        for (int i = 0; i < parameters.size(); i++) {
            result = result.replaceAll("\\{\\{" + (i+1) + "\\}\\}", parameters.get(i));
        }
        return result;
    }

    public void execute(String barcode) {
        GoobiScript gs = new GoobiScript();
        try {
            Optional<Process> process = Optional.ofNullable(ProcessManager.getProcessById(Integer.parseInt(barcode.strip())));

            process.ifPresentOrElse(p -> {
                String concreteGoobiScript = this.goobiScript.replaceAll("\\{\\{\\?\\}\\}", p.getId().toString());
                gs.execute(List.of(p.getId()), concreteGoobiScript);
                BarcodeScannerPlugin.success("Barcode action \"" + this.description + "\" executed for process \"" + p.getTitel() + "\" [" + p.getId() + "].");
            }, () -> BarcodeScannerPlugin.error("Process with ID \"" + barcode.strip() + "\" not found!"));
        } catch (NumberFormatException e) {
            BarcodeScannerPlugin.error("Invalid process id \"" + barcode.strip() + "\"!");
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
