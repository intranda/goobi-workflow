/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * <p>
 * Visit the websites for more information.
 * - https://goobi.io
 * - https://www.intranda.com
 * - https://github.com/intranda/goobi-workflow
 * <p>
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * <p>
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
package org.goobi.managedbeans;

import de.sub.goobi.helper.Helper;
import io.goobi.vocabulary.exchange.Vocabulary;
import io.goobi.vocabulary.exchange.VocabularySchema;
import io.goobi.workflow.api.vocabulary.APIException;
import io.goobi.workflow.api.vocabulary.VocabularyAPIManager;
import io.goobi.workflow.api.vocabulary.helper.ExtendedVocabularyRecord;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.deltaspike.core.api.scope.WindowScoped;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

@Named
@WindowScoped
@Log4j2
public class VocabularyEditBean implements Serializable {
    private static final long serialVersionUID = 5672948572345L;

    private static final String RETURN_PAGE_EDIT = "vocabulary_edit";

    private static final VocabularyAPIManager api = VocabularyAPIManager.getInstance();

    @Getter
    private Vocabulary vocabulary;
    @Getter
    private VocabularySchema metadataSchema;
    @Getter
    private ExtendedVocabularyRecord metadataRecord;

    @Inject
    private VocabularyBean vocabularyBean;


    public String load(Vocabulary vocabulary) {
        this.vocabulary = vocabulary;

        loadMetadata();

        return RETURN_PAGE_EDIT;
    }

    private void loadMetadata() {
        if (vocabulary.getMetadataSchemaId() == null) {
            this.metadataSchema = null;
            this.metadataRecord = null;
            return;
        }

        this.metadataSchema = api.vocabularySchemas().get(vocabulary.getMetadataSchemaId());
        this.metadataRecord = api.vocabularyRecords().getMetadata(vocabulary.getId());
    }

    public String saveVocabulary() {
        api.vocabularies().change(vocabulary);
        if (metadataRecord != null) {
            Long recordId = metadataRecord.getId();
            try {
                api.vocabularyRecords().save(metadataRecord);
            } catch (APIException e) {
                Helper.setFehlerMeldung(e);
                metadataRecord.setId(recordId);
                metadataRecord.setVocabularyId(vocabulary.getId());
                metadataRecord.setMetadata(true);
                metadataRecord = new ExtendedVocabularyRecord(metadataRecord);
                return "";
            }
        }
        return cancel();
    }

    public String deleteVocabulary() {
        api.vocabularies().delete(vocabulary);
        return cancel();
    }

    public String cancel() {
        return vocabularyBean.load();
    }
}
