package io.goobi.workflow.api.vocabulary.beans;

import io.goobi.vocabulary.exchange.VocabularyRecord;
import io.goobi.workflow.api.vocabulary.helper.ExtendedVocabularyRecord;

import javax.faces.component.FacesComponent;
import javax.faces.component.UINamingContainer;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@FacesComponent("io.goobi.workflow.api.vocabulary.beans.VocabularyRecordBean")
public class VocabularyRecordBean extends UINamingContainer {
    private enum PropertyKeys {
        hasChildren,
        isExpanded,
        level,
        titleValues
    }

    @Override
    public String getFamily() {
        return UINamingContainer.COMPONENT_FAMILY;
    }

    private Optional<ExtendedVocabularyRecord> extendedVocabularyRecord = Optional.empty();

    public void init() {
        VocabularyRecord r = (VocabularyRecord) getAttributes().get("record");
        if (r != null) {
            extendedVocabularyRecord =  Optional.of(new ExtendedVocabularyRecord(r));
        }
    }

    public Boolean getHasChildren() {
        return extendedVocabularyRecord.map(ExtendedVocabularyRecord::getChildren)
                .map(children -> !children.isEmpty())
                .orElse(false);
    }

    public Boolean getIsExpanded() {
        return (Boolean) getStateHelper().eval(PropertyKeys.isExpanded, Boolean.FALSE);
    }

    public Integer getLevel() {
        return extendedVocabularyRecord.map(ExtendedVocabularyRecord::getLevel).orElse(0);
    }

    public List<String> getTitleValues() {
        return extendedVocabularyRecord.map(ExtendedVocabularyRecord::getTitleValues).orElse(Collections.emptyList());
    }
}
