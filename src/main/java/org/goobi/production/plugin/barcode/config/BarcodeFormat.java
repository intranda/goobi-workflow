package org.goobi.production.plugin.barcode.config;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import de.sub.goobi.helper.GoobiScript;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.persistence.managers.ProcessManager;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.goobi.beans.Batch;
import org.goobi.beans.Institution;
import org.goobi.beans.Process;
import org.goobi.beans.User;
import org.goobi.production.plugin.barcode.BarcodeScannerPlugin;
import org.goobi.production.plugin.interfaces.AbstractDockablePlugin;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Getter
@Setter
public class BarcodeFormat {
    private static final String BATCH_PREFIX = "batch:";

    @JacksonXmlProperty(isAttribute = true)
    private String pattern;
    @JacksonXmlProperty(isAttribute = true)
    private String sample;
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
        return getRegexPattern().matcher(barcode).matches();
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
        Optional<Batch> batch = findBatchForBarcode(barcode);
        Optional<Process> process = Optional.ofNullable(ProcessManager.getProcessByTitle(barcode.strip()));

        if (batch.isPresent()) {
            executeCurrentBarcodeForBatch(batch.get());
        } else {
            process.ifPresentOrElse(this::executeCurrentBarcodeForProcess, () -> AbstractDockablePlugin.error("Process with title \"" + barcode.strip() + "\" not found!"));
        }
    }

    private Optional<Batch> findBatchForBarcode(String barcode) {
        if (!barcode.startsWith(BATCH_PREFIX)) {
            return Optional.empty();
        }
        try {
            int batchId = Integer.parseInt(barcode.substring(BATCH_PREFIX.length()));
            return Optional.ofNullable(ProcessManager.getBatchById(batchId));
        } catch (NumberFormatException e) {
            log.error("Wrong batch id format: \"{}\"", barcode, e);
            return Optional.empty();
        }
    }

    private List<String> determineParameters(String barcode) {
        Matcher m = getRegexPattern().matcher(barcode);
        if (!m.matches()) {
            throw new IllegalStateException("Should not reach this code if barcode format is not applicable");
        }

        List<String> parameters = new ArrayList<>(m.groupCount());
        for (int i = 1; i <= m.groupCount(); i++) {
            parameters.add(m.group(i));
        }
        return parameters;
    }

    public Pattern getRegexPattern() {
        if (this.regexPattern == null) {
            this.regexPattern = Pattern.compile(this.pattern);
        }
        return this.regexPattern;
    }

    public List<String> getSampleValues() {
        Matcher sampleMatcher = getRegexPattern().matcher(this.sample);
        if (sampleMatcher.find()) {
            List<String> result = new LinkedList<>();
            for (int i = 0; i < sampleMatcher.groupCount(); i++) {
                result.add(sampleMatcher.group(i + 1));
            }
            return result;
        } else {
            return Collections.emptyList();
        }
    }

    // TODO: Move into lib
    private void executeCurrentBarcodeForBatch(Batch batch) {
        // TODO:  Don't duplicate processes per batch logic! (copied from BatchBean)
        Institution inst = null;
        User user = Helper.getCurrentUser();
        if (user != null && !user.isSuperAdmin()) {
            // limit result to institution of current user
            inst = user.getInstitution();
        }
        ProcessManager.getProcesses(null, " istTemplate = false AND batchID = " + batch.getBatchId(), inst)
                .forEach(this::executeCurrentBarcodeForProcess);
    }

    private void executeCurrentBarcodeForProcess(Process process) {
        GoobiScript gs = new GoobiScript();
        String concreteGoobiScript = this.goobiScript.replaceAll("\\{\\{\\?\\}\\}", process.getId().toString());
        gs.execute(List.of(process.getId()), concreteGoobiScript);
        BarcodeScannerPlugin.success("Barcode action \"" + this.description + "\" executed for process \"" + process.getTitel() + "\" [" + process.getId() + "].");
    }
}
