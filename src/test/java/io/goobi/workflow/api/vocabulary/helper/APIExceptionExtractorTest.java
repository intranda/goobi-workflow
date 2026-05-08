package io.goobi.workflow.api.vocabulary.helper;

import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.DataIntegrityViolation;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.DeletionOfReferencedVocabulary;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.DeletionOfReferencedVocabularyRecord;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.EntityNotFound;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.FieldValueIsNotUnique;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.FieldValuesDoNotMatchSpecifiedValidationRegex;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.GenericValidation;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.RecordImportUnsupportedExcelCellType;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.RecordValidationMissingRequiredFields;
import static org.junit.Assert.assertNotNull;

import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.goobi.vocabulary.exception.VocabularyException;
import io.goobi.workflow.api.vocabulary.APIException;

public class APIExceptionExtractorTest {

    private void testForNonEmptyness(VocabularyException.ErrorCode code) {
        VocabularyException vocabularyException = new VocabularyException(code, null, Map.of("reason", "nothing"), params -> "");
        APIException exception = new APIException("localhost", "GET", 200, "some reason", vocabularyException, null);
        APIExceptionExtractor extractor = new APIExceptionExtractor(exception);
        String message = extractor.getLocalizedMessage(Locale.ENGLISH);
        assertNotNull(message);
    }

    @Test
    public void testAllImplementedCases() {
        testForNonEmptyness(DataIntegrityViolation);
        testForNonEmptyness(EntityNotFound);
        testForNonEmptyness(FieldValueIsNotUnique);
        testForNonEmptyness(FieldValuesDoNotMatchSpecifiedValidationRegex);
        testForNonEmptyness(RecordValidationMissingRequiredFields);
        testForNonEmptyness(RecordImportUnsupportedExcelCellType);
        testForNonEmptyness(DeletionOfReferencedVocabulary);
        testForNonEmptyness(DeletionOfReferencedVocabularyRecord);
        testForNonEmptyness(GenericValidation);
    }
}
