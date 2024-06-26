package io.goobi.workflow.api.vocabulary.jsfwrapper;

import io.goobi.vocabulary.exchange.Vocabulary;
import lombok.Getter;

public class JSFVocabulary extends Vocabulary {
    @Getter
    private boolean skosExportPossible;

    public void setSkosExportPossible(boolean skosExportPossible) {
    }

    public void load() {
        this.skosExportPossible = get_links().keySet().stream().anyMatch(link -> link.startsWith("export_rdf"));
    }

    public String rdfXmlExport() {
        return get_links().get("export_rdf_xml").getHref();
    }

    public String rdfTurtleExport() {
        return get_links().get("export_rdf_turtle").getHref();
    }

    public String jsonExport() {
        return get_links().get("export_json").getHref();
    }

    public String csvExport() {
        return get_links().get("export_csv").getHref();
    }

    public String excelExport() {
        return get_links().get("export_excel").getHref();
    }
}
