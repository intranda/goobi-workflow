package org.goobi.api.display.helper;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.goobi.beans.Process;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import de.sub.goobi.helper.VariableReplacer;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import ugh.dl.DigitalDocument;
import ugh.dl.Prefs;
import ugh.fileformats.mets.ModsHelper;

@Getter
@Setter
@Log4j2
public class MetadataGeneration {

    // metadata name, e.g. TitleDocMain
    private String metadataName;

    // xpath condition, if filled it must result to true
    private String condition;

    // configured template value, e.g. "Letter from [ACTOR_FROM] to [ACTOR_TO] at [PLACE], [DATE]"
    private String defaultValue;

    // list of configured parameter to replace variables in template value, see below
    List<MetadataGenerationParameter> parameter = new ArrayList<>();

    public void addParameter(MetadataGenerationParameter param) {
        parameter.add(param);
    }

    @Getter
    @Setter
    public class MetadataGenerationParameter {

        // parameter name, e.g. ACTOR_FROM
        private String parameterName;

        // parameter type, e.g. xpath or variable
        private String type;

        // where to take the value from, xpath expression or variable name
        private String field;

        // manipulate the found value. e.g.
        // ([0-9]{4})-([0-9]{2})-([(0-9]{2}) $1 to get a date
        // (.+), .* $1 to get the name from the string Jean Baptiste Laborde, master of the 'Amphitrion' in 1744
        private String regularExpression;
        private String replacement;
    }

    /**
     * Generate metadata value based on configured rule.
     *
     * @param process
     * @param prefs
     * @param dd
     * @param xpfac
     * @param metadataSection
     * @return
     */

    public String generateValue(Process process, Prefs prefs, DigitalDocument dd, XPathFactory xpfac, Element metadataSection) {
        String currentValue = getDefaultValue();

        // prepare variable replacer
        VariableReplacer replacer = new VariableReplacer(dd, prefs, process, null);
        // compute parameter
        for (MetadataGenerationParameter param : getParameter()) {
            String parameterValue = null;
            if ("xpath".equals(param.getType())) {
                try {
                    XPathExpression<Element> xp =
                            xpfac.compile(param.getField(), Filters.element(), null, ModsHelper.MODS_NAMESPACE, ModsHelper.GOOBI_NAMESPACE);
                    Element test = xp.evaluateFirst(metadataSection);
                    if (test != null) {
                        parameterValue = test.getValue();
                    }
                } catch (IllegalArgumentException | IllegalStateException e) {
                    log.error(e);
                    return e.getMessage();

                }
            } else if ("variable".equals(param.getType())) {
                parameterValue = replacer.replace(param.getField());
            }
            if (StringUtils.isNotBlank(parameterValue)) {
                if (StringUtils.isNotBlank(param.getRegularExpression())) {
                    parameterValue = parameterValue.replaceAll(param.getRegularExpression(), param.getReplacement());
                }
                // finally replace the field with the content
                currentValue = currentValue.replace("[" + param.getParameterName() + "]", parameterValue);
            }
        }
        return currentValue;
    }
}
