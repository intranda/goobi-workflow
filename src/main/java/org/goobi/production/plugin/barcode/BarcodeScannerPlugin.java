package org.goobi.production.plugin.barcode;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import de.sub.goobi.helper.Helper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.goobi.production.plugin.barcode.config.BarcodeFormat;
import org.goobi.production.plugin.barcode.config.BarcodeScannerPluginConfiguration;
import org.goobi.production.plugin.interfaces.IFooterPlugin;

import javax.annotation.ManagedBean;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.Application;
import javax.faces.application.Resource;
import javax.faces.component.UIComponent;
import javax.faces.component.UIPanel;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.context.FacesContext;
import javax.faces.view.facelets.FaceletContext;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Slf4j
@Data
@ManagedBean
@RequestScoped
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
    public String getModalPath() {
        return "../includes/barcodePlugin/barcodeModal.xhtml";
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

    public void setModal(UIComponent c) {

    }

    public UIComponent getModal() {
        FacesContext context = FacesContext.getCurrentInstance();
        Application application = context.getApplication();
        FaceletContext faceletContext = (FaceletContext) context.getAttributes().get(FaceletContext.FACELET_CONTEXT_KEY);

        UIComponent parent = FacesContext.getCurrentInstance().getApplication().createComponent(HtmlPanelGroup.COMPONENT_TYPE);
        parent.setId("modal" + getId());

        Resource componentResource = context.getApplication().getResourceHandler().createResource("barcodeModal.xhtml", "plugins");
        UIComponent composite = application.createComponent(context, componentResource);
        composite.setId("modalComposite" + getId());

        // This basically creates <composite:implementation>.
        UIComponent implementation = application.createComponent(UIPanel.COMPONENT_TYPE);
        implementation.setRendererType("javax.faces.Group");
        composite.getFacets().put(UIComponent.COMPOSITE_FACET_NAME, implementation);

        parent.getChildren().add(composite);
        parent.pushComponentToEL(context, composite); // This makes #{cc} available.
        try {
            faceletContext.includeFacelet(implementation, componentResource.getURL());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        parent.popComponentFromEL(context);

        //add some attributes...
//        parent.getAttributes().put("plugin", this);

        return parent;
    }
}