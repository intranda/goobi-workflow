package org.goobi.production.plugin.barcode;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import de.sub.goobi.helper.Helper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.goobi.production.plugin.barcode.config.BarcodeFormat;
import org.goobi.production.plugin.barcode.config.BarcodeScannerPluginConfiguration;
import org.goobi.production.plugin.interfaces.IFooterPlugin;

import java.io.File;
import java.util.Collections;
import java.util.List;

@Slf4j
@Data
public class BarcodeScannerPlugin implements IFooterPlugin {
    @Override
    public String getId() {
        return getTitle().replaceAll("\\s+", "");
    }

    @Override
    public String getIcon() {
        return "fa-barcode";
    }

    @Override
    public void execute() throws Exception {
        success("Footer plugin \"" + getTitle() + "\" executed!");
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
    private BarcodeScannerPluginConfiguration config;
    private List<BarcodeFormat> activeFormats = Collections.emptyList();

    public static void main(String[] args) throws Exception {
        BarcodeScannerPlugin plugin = new BarcodeScannerPlugin();
        plugin.initialize();
        plugin.config.getBarcode().get(0).activate("propertyset_Standort_54");
    }

    @Override
    public void initialize() throws Exception {
        XmlMapper mapper = new XmlMapper();
        this.config = mapper.readValue(new File("/opt/digiverso/goobi/config/plugin_intranda_footer_barcodeScanner.xml"), BarcodeScannerPluginConfiguration.class);
//        this.config = new BarcodeScannerPluginConfiguration();
//        this.config.setBarcode(List.of(new BarcodeFormat("place_(.*)_(\\d+)", """
//---
//action: propertySet
//name: {{1}}
//value: {{2}}
//"""),
//                new BarcodeFormat("restauration_(.*)_(\\d+)", """
//---
//action: propertySet
//name: {{1}}
//value: {{2}}
//""")));
//        mapper.writeValue(System.err, this.config);
    }

    public void scan() {
        log.debug("Processing barcode \"{}\"", code);
        String barcode = code;
        this.code = "";

        try {
            List<BarcodeFormat> applicable = findMatchingBarcodeFormats(barcode);

            if (!applicable.isEmpty()) {
                applicable.forEach(bf -> bf.activate(barcode));
                activeFormats = applicable;
            } else {
                if (!activeFormats.isEmpty()) {
                    activeFormats.forEach(bf -> bf.execute(barcode));
                } else {
                    warn("No active barcode actions found!");
                }
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            error("An error occurred while scanning barcode \"" + barcode + "\"!");
        }
    }

    private List<BarcodeFormat> findMatchingBarcodeFormats(String code) {
        return this.config.getBarcode().stream()
                .filter(bf -> bf.patternMatches(code))
                .toList();
    }

    public static void success(String message) {
        log.debug(message);
        Helper.setMeldung(message);
    }

    public static void warn(String message) {
        log.warn(message);
        Helper.setFehlerMeldung(message);
    }

    public static void error(String message) {
        log.error(message);
        Helper.setFehlerMeldung(message);
    }
}