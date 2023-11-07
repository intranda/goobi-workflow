/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *          - https://goobi.io
 *          - https://www.intranda.com
 *          - https://github.com/intranda/goobi-workflow
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
package org.goobi.vocabulary;

import org.apache.commons.lang3.StringUtils;

public class VocabularyFieldValidator {

    /**
     * This method validates a field of the currently edited vocabulary record. If fields are required, but are empty, the currentVocabRecord is set
     * to 'invalid=false'. If there are data fields that have to be distinctive and are equal to the contents of fields of other vocabulary records,
     * all of them are set to invalid too. If the currentVocabRecord is invalid, false is returned. If it is valid and can be stored in the database,
     * true is returned. The given field and the value to set are the data that should be set in the frontend and must be validated. The
     * validationMessage of all invalid fields is set to the key that can be translated (to get a readable validation error message). Otherwise, the
     * validation message is set to an empty string.
     *
     * @param vocabulary The current vocabulary to validate
     * @param vocabRecord The current vocabulary record to validate
     * @param field The field to validate
     * @param fieldValue The value that should be set to that field
     * @return true If currentVocabRecord is valid and its fields are valid or false if they are invalid or distinctive fields have the same content
     *         as fields of other vocabulary records
     */
    public static boolean validateFieldInRecords(Vocabulary vocabulary, VocabRecord vocabRecord, Field field, String fieldValue) {

        // If field is required and empty, it gets marked as invalid
        if (field.getDefinition().isRequired() && StringUtils.isBlank(fieldValue)) {
            field.setValidationMessage("vocabularyManager_validation_fieldIsRequired");
            vocabRecord.setValid(false);
            return false;
        }

        field.setValidationMessage("");
        boolean valid = true;

        if (field.getDefinition().isDistinctive() && StringUtils.isNotBlank(fieldValue)) {
            for (VocabRecord other : vocabulary.getRecords()) {

                if (vocabRecord.equals(other)) {
                    continue;
                }

                String otherFieldValue = other.getFieldValue(field.getDefinition());
                if (fieldValue.equals(otherFieldValue)) {
                    field.setValidationMessage("vocabularyManager_validation_fieldIsNotUnique");
                    other.setValid(false);
                    vocabRecord.setValid(false);
                    valid = false;
                }
            }
        }
        return valid;
    }

}
