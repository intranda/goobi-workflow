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

import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.apache.commons.lang.StringUtils;
import org.goobi.managedbeans.VocabularyBean;

import de.sub.goobi.helper.Helper;

@FacesValidator("org.goobi.vocabulary.VocabularyFieldValidator")
public class VocabularyFieldValidator implements Validator<Integer> {

    @Override
    public void validate(FacesContext context, UIComponent component, Integer time) throws ValidatorException {

        // TODO: What is with the time parameter? Can it be replaced by object? Or does it depend on attributes in JSF?
        // TODO: The validation message should return or set an error description (for the case of different possible messages)
        /*
        if (time != 0) {
        if (time < 10 || time > 30) {
            FacesMessage message =
                    new FacesMessage(Helper.getTranslation("metadataSaveTimeError"), Helper.getTranslation("metadataSaveTimeError"));
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(message);
        }
        }
        */
        VocabularyBean bean = VocabularyFieldValidator.extractVocabularyBean(context);
        Vocabulary vocabulary = bean.getCurrentVocabulary();
        VocabRecord record = bean.getCurrentVocabRecord();
        boolean success = VocabularyFieldValidator.validateRecords(vocabulary, record);
        if (!success) {
            FacesMessage message = new FacesMessage(Helper.getTranslation("metadataSaveTimeError"), Helper.getTranslation("metadataSaveTimeError"));
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(message);
        }
    }

    /**
     * This method validates the currently edited vocabulary record. If fields are required, but are empty, the currentVocabRecord is set to
     * 'invalid=false'. If there are data fields that have to be distinctive and are equal to the contents of fields of other vocabulary records, all
     * of them are set to invalid too. If the currentVocabRecord is invalid, false is returned. If it is valid and can be stored in the database, true
     * is returned.
     *
     * @param vocabulary The current vocabulary to validate
     * @param record The current vocabulary record to validate
     * @return true If currentVocabRecord is valid and false if its fields are invalid or distinctive fields have the same content as fields of other
     *         vocabulary records
     */
    public static boolean validateRecords(Vocabulary vocabulary, VocabRecord record) {

        // Only the invalid records should be set to 'valid=false' later
        for (VocabRecord currentRecord : vocabulary.getRecords()) {
            currentRecord.setValid(true);
        }

        boolean valid = true;

        for (Field field : record.getFields()) {

            // If the field is a text field, the value is trimmed to avoid leading or trailing whitespaces
            String type = field.getDefinition().getType();
            if (type.equals("input") || type.equals("textarea") || type.equals("html")) {
                field.setValue(field.getValue().trim());
            }

            field.setValidationMessage(null);

            // If field is required and empty, it gets marked as invalid
            if (field.getDefinition().isRequired() && StringUtils.isBlank(field.getValue())) {
                field.setValidationMessage("vocabularyManager_validation_fieldIsRequired");
                record.setValid(false);
                valid = false;
            }

            if (field.getDefinition().isDistinctive() && StringUtils.isNotBlank(field.getValue())) {
                requiredCheck: for (VocabRecord other : vocabulary.getRecords()) {
                    if (record.equals(other)) {
                        continue;
                    }
                    for (Field f : other.getFields()) {
                        if (field.getDefinition().equals(f.getDefinition())) {
                            if (field.getValue().equals(f.getValue())) {
                                field.setValidationMessage("vocabularyManager_validation_fieldIsNotUnique");
                                other.setValid(false);
                                record.setValid(false);
                                valid = false;
                                break requiredCheck;
                            }
                        }
                    }
                }
            }
        }
        return valid;
    }

    /**
     * Extracts and returns the vocabulary bean from the given faces context object.
     *
     * @param context The faces context object to get the bean from
     * @return The vocabulary bean
     */
    private static VocabularyBean extractVocabularyBean(FacesContext context) {
        Application application = context.getApplication();
        ExpressionFactory expressionFactory = application.getExpressionFactory();
        ValueExpression expression = expressionFactory.createValueExpression(context.getELContext(), "#{vocabularyBean}", Object.class);
        return (VocabularyBean) expression.getValue(context.getELContext());
    }

}
