package org.goobi.production.plugin.barcode;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.goobi.production.plugin.barcode.config.BarcodeFormat;
import org.goobi.production.plugin.barcode.config.BarcodeScannerPluginConfiguration;
import org.goobi.production.plugin.interfaces.AbstractDockablePlugin;

import javax.annotation.ManagedBean;
import javax.enterprise.context.RequestScoped;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Data
@ManagedBean
@RequestScoped
public class BarcodeScannerPlugin extends AbstractDockablePlugin {
    @Override
    public String getId() {
        return getTitle().replaceAll("\\s+", "");
    }

    @Override
    public String getIcon() {
        return "fa-barcode";
    }

    @Override
    public boolean isMenuBarDockable() {
        return true;
    }

    @Override
    public boolean isFooterDockable () {
        return true;
    }

    @Override
    public void execute() throws Exception {
        success("Barcode plugin \"" + getTitle() + "\" executed!");
    }

    @Override
    public String getTitle() {
        return "Barcode Scanner";
    }

    @Override
    public String getModalPath() {
        return "../includes/barcodePlugin/barcodeModal.xhtml";
    }

    private String code;
    private BarcodeScannerPluginConfiguration config;
    private List<BarcodeFormat> activeFormats = Collections.emptyList();
    private boolean showConfig;
    private BarcodeFormat selectedConfigFormat;
    private List<BarcodeField> barcodeFields;
    private String barcode;

    @Data
    public class BarcodeField {
        private String label;
        private String value;

        public void setValue(String value) {
            this.value = value;
            updateBarcode();
        }
    }

    public void setSelectedConfigFormat(BarcodeFormat selectedConfigFormat) {
        this.selectedConfigFormat = selectedConfigFormat;
        updateBarcodeFields();
        updateBarcode();
    }

    private void updateBarcodeFields() {
        if (this.selectedConfigFormat == null) {
            return;
        }
        String p = this.selectedConfigFormat.getPattern();
        int numberOfFields = StringUtils.countMatches(p, "(");
        barcodeFields = new ArrayList<>(numberOfFields);
        List<String> sampleValues = this.selectedConfigFormat.getSampleValues();
        for (int i = 0; i < numberOfFields; i++) {
            BarcodeField field = new BarcodeField();
            field.setLabel("Value " + (i+1));
            field.setValue(sampleValues.get(i));
            barcodeFields.add(field);
        }
    }

    private void updateBarcode() {
        if (this.selectedConfigFormat == null) {
            return;
        }
        this.barcode = this.selectedConfigFormat.getPattern();
        for (int i = 0; i < this.barcodeFields.size(); i++) {
            this.barcode = this.barcode.replaceFirst("\\(.*?\\)", this.barcodeFields.get(i).getValue());
        }
    }

    public static void main(String[] args) throws Exception {
        BarcodeScannerPlugin plugin = new BarcodeScannerPlugin();
        plugin.initialize();
        plugin.config.getBarcode().get(0).activate("propertyset_Standort_54");
    }

    @Override
    public void initialize() throws Exception {
        XmlMapper mapper = new XmlMapper();
        this.config = mapper.readValue(new File("/opt/digiverso/goobi/config/plugin_intranda_footer_barcodeScanner.xml"), BarcodeScannerPluginConfiguration.class);
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
}