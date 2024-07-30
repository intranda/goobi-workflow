package io.goobi.workflow.api.vocabulary.helper;

import io.goobi.vocabulary.exchange.Vocabulary;
import lombok.Getter;

import java.util.Optional;

public class ExtendedVocabulary extends Vocabulary {
    @Getter
    private boolean skosExportPossible;

    public ExtendedVocabulary(Vocabulary orig) {
        setId(orig.getId());
        setSchemaId(orig.getSchemaId());
        setMetadataSchemaId(orig.getMetadataSchemaId());
        setName(orig.getName());
        setDescription(orig.getDescription());
        set_links(orig.get_links());

        postInit();
    }

    private void postInit() {
        this.skosExportPossible = Optional.ofNullable(get_links())
                .map(m -> m.keySet().stream().anyMatch(link -> link.startsWith("export_rdf")))
                .orElse(false);
    }

    public String getURI() {
        return get_links().get("self").getHref();
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
