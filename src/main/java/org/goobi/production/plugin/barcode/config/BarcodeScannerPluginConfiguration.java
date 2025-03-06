package org.goobi.production.plugin.barcode.config;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;
import org.goobi.production.plugin.DockAnchor;

import java.util.List;

@Data
@JacksonXmlRootElement(localName = "config")
public class BarcodeScannerPluginConfiguration {
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<DockAnchor> docking;
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<BarcodeFormat> barcode;
}
