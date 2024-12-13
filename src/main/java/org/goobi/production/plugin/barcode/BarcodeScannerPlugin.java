package org.goobi.production.plugin.barcode;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.goobi.production.plugin.barcode.config.BarcodeFormat;
import org.goobi.production.plugin.interfaces.IFooterPlugin;

import java.util.Collections;
import java.util.List;

@Slf4j
@Data
public class BarcodeScannerPlugin implements IFooterPlugin {
    @Override
    public String getIcon() {
        return "fa-barcode";
    }

    @Override
    public void execute() throws Exception {
        System.err.println("Footer plugin \"" + getTitle() + "\" executed!");
    }

    @Override
    public String getTitle() {
        return "Barcode Scanner";
    }

    @Override
    public String getModal() {
        return "includes/footerPluginModal.xhtml";
    }

    private String code;
    private List<BarcodeFormat> barcodeFormats = Collections.emptyList();
    private List<BarcodeFormat> activeFormats = Collections.emptyList();

    @Override
    public void initialize() throws Exception {
        barcodeFormats = List.of(new BarcodeFormat("place_(.*)", """
---
action: propertySet
name: {{1}}
value: {{?}}
"""));
    }

    public void scan() {
        log.debug("Processing barcode \"{}\"", code);

        List<BarcodeFormat> applicable = findMatchingBarcodeFormats(code);

        if (!applicable.isEmpty()) {
            applicable.forEach(bf -> bf.process(code));
            activeFormats = applicable;
        } else {
            if (!activeFormats.isEmpty()) {
                activeFormats.forEach(bf -> bf.execute(code));
            } else {
                log.warn("No active barcode formats found");
            }
        }

//        PropertyManager.getProcessPropertiesForProcess(Integer.parseInt(code)).stream()
//                .filter(p -> p.getTitel().equals("Standort"))
//                .findFirst()
//                .ifPresent(p -> {
//                    p.setWert("54");
//                    PropertyManager.saveProcessProperty(p);
//                    Helper.addMessageToProcessJournal(Integer.parseInt(code), LogType.DEBUG, "Process property \"Standort\" was set by barcode to new value \"54\".");
//                });
    }

    private List<BarcodeFormat> findMatchingBarcodeFormats(String code) {
        return barcodeFormats.stream()
                .filter(bf -> bf.patternMatches(code))
                .toList();
    }
}