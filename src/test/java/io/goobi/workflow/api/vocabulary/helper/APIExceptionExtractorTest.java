package io.goobi.workflow.api.vocabulary.helper;

import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.DataIntegrityViolation;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.EntityNotFound;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.FieldInstanceIsEmpty;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.FieldTypeSelectableValuesDoNotMatchSpecifiedValidationRegex;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.FieldValueIsBlank;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.GenericValidation;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.RecordImport;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.RecordValidation;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import io.goobi.vocabulary.exception.VocabularyException;
import io.goobi.vocabulary.exception.VocabularyException.ErrorCode;
import io.goobi.workflow.api.vocabulary.APIException;

public class APIExceptionExtractorTest {

    private static final String SERVER_FALLBACK = "SERVER_FALLBACK";
    private static final String ENGLISH_FALLBACK = "ENGLISH_FALLBACK";
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\d+}");
    private static final Map<Locale, Properties> BUNDLES = new HashMap<>();

    /**
     * Param key sets the vocabulary server sends per error type, one entry per construction site variant (goobi-vocabulary-server, module-core).
     */
    private static final Map<String, List<List<String>>> SERVER_PARAM_SETS = new LinkedHashMap<>();

    static {
        sets("DataIntegrityViolation", List.of("reason"));
        sets("Mapping", List.of("from", "to", "reason"), List.of("format", "vocabularyId", "reason"));
        sets("EntityNotFound", List.of("type", "id"), List.of("type", "searchTerm"), List.of("type"));
        sets("MissingRequiredAttribute", List.of("type", "attributes"));
        sets("InsertingNonExistingItem", List.of("type", "id"));
        sets("IllegalAttributeProvided", List.of("attribute"));
        sets("DeletionOfReferencedVocabulary", List.of("vocabularyId", "referencingVocabularyIds", "referencingVocabularyNames"));
        sets("DeletionOfReferencedVocabularyRecord", List.of("recordId", "referencingRecordIds"), List.of("recordId"), List.of("vocabularyId"));
        sets("GenericValidation", List.of());
        sets("SchemaValidationNoDefinitions", List.of());
        sets("SchemaValidationMissingMainField", List.of());
        sets("SchemaValidationTooManyMainFields", List.of("mainFieldNames"));
        sets("SchemaValidationMainFieldIsNotRequired", List.of("mainFieldNames"));
        sets("SchemaValidationTitleFieldsAreNotRequired", List.of());
        sets("SchemaValidationDefinitionIssues", List.of());
        sets("VocabularyValidationMetadataSchemaIssues", List.of("metadataSchemaId"));
        sets("VocabularyValidationMetadataSchemaIsNotSetToSingleRootElement", List.of());
        sets("VocabularyValidationMetadataSchemaIsHierarchical", List.of());
        sets("RecordValidation", List.of("recordId"));
        sets("RecordValidationMissingRequiredFields", List.of("missingFieldIds", "missingFieldNames", "providedFieldIds", "providedFieldNames"));
        sets("RecordValidationUndefinedFieldsGiven", List.of("providedFieldIds", "providedFieldNames"));
        sets("RecordValidationHierarchyIssues", List.of());
        sets("RecordValidationParentReferenceNotAllowed", List.of("parentId"));
        sets("RecordValidationChildrenReferencesNotAllowed", List.of("vocabularyId", "vocabularyName", "childrenRecordIds"));
        sets("RecordValidationSingleRootElementVocabularyAlreadyContainsRecord", List.of("vocabularyId", "vocabularyName"));
        sets("RecordImport", List.of("parentId"), List.of());
        sets("RecordImportHeaderIssues", List.of("undefinedFields", "missingFields"));
        sets("RecordImportParsingIssues", List.of());
        sets("RecordImportFieldCountIssue", List.of("expectedSize", "realSize"));
        sets("RecordImportUnsupportedExcelCellType", List.of("cellType"));
        sets("FieldInstanceIssues", List.of("vocabularyId", "vocabularyName"));
        sets("FieldInstanceIsEmpty", List.of());
        sets("FieldInstanceBelongsToWrongSchema", List.of("definitionId", "definitionName", "specifiedSchemaId", "requiredSchemaId"));
        sets("FieldInstanceValueIssues", List.of());
        sets("FieldInstanceMultipleValuesNotAllowed", List.of("definitionId", "definitionName", "numberOfValues"));
        sets("FieldDefinitionNonUniqueFallbackLanguageSpecified", List.of("name", "numberOfSpecifiedFallbackLanguages"));
        sets("FieldDefinitionFallbackNotRequired", List.of("name"));
        sets("FieldDefinitionReferenceVocabularyAndTypeNotPossible", List.of("name", "typeNotAllowed", "translationDefinitionsNotAllowed"));
        sets("FieldDefinitionNeitherReferenceVocabularyNorTypeNotPossible", List.of("name"));
        sets("FieldTypeInvalidRegexSyntax", List.of("regex"));
        sets("FieldTypeSelectableValuesDoNotMatchSpecifiedValidationRegex", List.of("selectableValues", "regex"));
        sets("FieldValuesDoNotMatchSpecifiedValidationRegex", List.of("values", "definitionId", "definitionName", "regex"));
        sets("FieldValuesAreNonSelectableValues", List.of("values", "selectableValues", "definitionId", "definitionName"));
        sets("FieldValueIsBlank", List.of("definitionId", "definitionName"));
        sets("FieldValueIsNotUnique", List.of("definitionId", "definitionName", "duplicateValues"));
        sets("FieldValueHasNonAllowedTranslations", List.of("definitionId", "definitionName", "translationLanguages"));
        sets("FieldValueContainsNonTranslatedValue", List.of("definitionId", "definitionName"));
        sets("FieldValueUnspecifiedTranslations", List.of("definitionId", "definitionName", "unspecifiedTranslationLanguages"));
        sets("FieldValueMissingRequiredTranslations", List.of("definitionId", "definitionName", "requiredTranslationLanguages"));
        sets("FieldValueMissingNonUniqueTranslations", List.of("definitionId", "definitionName", "givenTranslationLanguages"));
        sets("FieldValueReferencedRecordBelongsToWrongVocabulary", List.of("recordId", "referencedVocabulary", "specifiedReferenceVocabulary"));
        sets("FieldValueReferencedRecordIssues", List.of());
    }

    @SafeVarargs
    private static void sets(String errorType, List<String>... paramSets) {
        SERVER_PARAM_SETS.put(errorType, Arrays.asList(paramSets));
    }

    /**
     * Reads the real message files from src/main/resources. The test classpath has its own messages_en.properties which hides the main one.
     */
    private static synchronized Properties bundle(Locale locale) {
        return BUNDLES.computeIfAbsent(locale, l -> {
            Properties properties = new Properties();
            try (Reader reader = Files.newBufferedReader(Path.of("src/main/resources/messages_" + l.getLanguage() + ".properties"),
                    StandardCharsets.UTF_8)) {
                properties.load(reader);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            return properties;
        });
    }

    /**
     * Same contract as Helper.getString: strips a trailing "zzz" and returns the key if there is no text.
     */
    private static String rawText(Locale locale, String key) {
        String value = bundle(locale).getProperty(key);
        if (value == null) {
            return key;
        }
        if (value.endsWith("zzz")) {
            value = value.replace("zzz", "").trim();
        }
        return value.isEmpty() ? key : value;
    }

    private static VocabularyException ex(ErrorCode type, Map<String, String> params, String message, VocabularyException... causes) {
        VocabularyException exception = new VocabularyException();
        exception.setErrorType(type);
        exception.setParams(params);
        exception.setMessage(message);
        exception.setCauses(causes.length == 0 ? null : List.of(causes));
        return exception;
    }

    private static VocabularyException ex(ErrorCode type, Map<String, String> params, VocabularyException... causes) {
        return ex(type, params, SERVER_FALLBACK + " " + type, causes);
    }

    private static String render(Locale locale, VocabularyException vocabularyException) {
        return render(locale, vocabularyException, APIExceptionExtractorTest::rawText);
    }

    private static String render(Locale locale, VocabularyException vocabularyException, BiFunction<Locale, String, String> texts) {
        APIException exception = new APIException("http://localhost/api/v1/vocabularies/1/import/csv", "POST", 400, "Bad Request",
                vocabularyException, null);
        return new APIExceptionExtractor(exception, texts).getLocalizedMessage(locale);
    }

    // a. Fallback to the server message

    @Test
    public void leafWithoutMatchingVariantShowsFirstNonBlankLineOfServerMessage() {
        String message = render(Locale.ENGLISH, ex(EntityNotFound, Map.of(), "\n\t  Record not found anywhere  \n\tsecond line"));
        assertEquals("Record not found anywhere", message);
    }

    @Test
    public void leafWithoutMatchingVariantAndWithoutServerMessageShowsErrorType() {
        String message = render(Locale.ENGLISH, ex(EntityNotFound, Map.of(), (String) null));
        assertEquals("EntityNotFound", message);
    }

    // b. Param variants

    @Test
    public void entityNotFoundBySearchTermUsesItsOwnText() {
        String message = render(Locale.ENGLISH, ex(EntityNotFound, Map.of("type", "VocabularyEntity", "searchTerm", "Places")));
        assertEquals("VocabularyEntity for search term \"Places\" not found", message);
    }

    @Test
    public void recordImportWithUndefinedParentShowsTheParentId() {
        String message = render(Locale.ENGLISH, ex(RecordImport, Map.of("parentId", "17")));
        assertEquals("Unknown parent record ID \"17\"", message);
    }

    @Test
    public void nullStringParamCountsAsAbsent() {
        String message = render(Locale.ENGLISH, ex(RecordValidation, Map.of("recordId", "null")));
        assertEquals("Error validating a new record", message);
    }

    @Test
    public void blankParamCountsAsAbsent() {
        String message = render(Locale.ENGLISH, ex(RecordValidation, Map.of("recordId", " ")));
        assertEquals("Error validating a new record", message);
    }

    // c. Placeholders are replaced in one pass

    @Test
    public void paramValuesContainingPlaceholdersStayIntact() {
        String message = render(Locale.ENGLISH,
                ex(FieldTypeSelectableValuesDoNotMatchSpecifiedValidationRegex, Map.of("regex", "^\\d{1}$", "selectableValues", "a, b")));
        assertEquals("Selectable value(s) \"a, b\" do not match the validation regex \"^\\d{1}$\"", message);
    }

    // d. Breadcrumbs: one line per distinct error path, header = top level, count for repeated lines

    private static final VocabularyException NON_SELECTABLE = ex(ErrorCode.FieldValuesAreNonSelectableValues,
            Map.of("definitionId", "5", "definitionName", "authority", "values", "VIAF", "selectableValues", "geonames,viaf"));
    private static final VocabularyException TITLE_BLANK = ex(FieldValueIsBlank, Map.of("definitionId", "3", "definitionName", "title"));
    private static final VocabularyException REFERENCE_MISSING = ex(ErrorCode.FieldValueReferencedRecordIssues, null,
            ex(EntityNotFound, Map.of("type", "VocabularyRecordEntity", "id", "7")));

    /** Per field: field instance validator > FieldInstanceValueIssues > per value: field value validator > leaf. */
    private static VocabularyException fieldWithValueErrors(VocabularyException... valueErrors) {
        VocabularyException[] perValue = Arrays.stream(valueErrors).map(e -> ex(GenericValidation, null, e)).toArray(VocabularyException[]::new);
        return ex(GenericValidation, null, ex(ErrorCode.FieldInstanceValueIssues, null, perValue));
    }

    /** Per record: record validator > FieldInstanceIssues > fields. */
    private static VocabularyException recordWithFields(VocabularyException... fields) {
        return ex(GenericValidation, null, ex(ErrorCode.FieldInstanceIssues, Map.of("vocabularyId", "1", "vocabularyName", "Places"), fields));
    }

    private static VocabularyException recordImport(VocabularyException... records) {
        return ex(RecordImport, null, records);
    }

    private static String lines(String... lines) {
        return String.join("\n", lines);
    }

    @Test
    public void importWithIdenticalErrorsShowsOneBreadcrumbLineWithCount() {
        VocabularyException[] records = new VocabularyException[59];
        Arrays.fill(records, recordWithFields(fieldWithValueErrors(NON_SELECTABLE)));

        assertEquals(lines("Error(s) during record import",
                "59× Field \"authority\" › Value(s) \"VIAF\" not among the selectable values: geonames,viaf"),
                render(Locale.ENGLISH, recordImport(records)));
    }

    @Test
    public void importWithMixedErrorsShowsDistinctLinesInFirstOccurrenceOrder() {
        VocabularyException nonSelectable = recordWithFields(fieldWithValueErrors(NON_SELECTABLE));
        VocabularyException missingTitle = ex(GenericValidation, null, ex(ErrorCode.RecordValidationMissingRequiredFields,
                Map.of("missingFieldIds", "3", "missingFieldNames", "title", "providedFieldIds", "5", "providedFieldNames", "authority")));
        VocabularyException undefinedParent = ex(RecordImport, Map.of("parentId", "17"));

        assertEquals(lines("Error(s) during record import",
                "3× Field \"authority\" › Value(s) \"VIAF\" not among the selectable values: geonames,viaf",
                "2× The following required fields were not provided: title",
                "Unknown parent record ID \"17\""),
                render(Locale.ENGLISH, recordImport(nonSelectable, missingTitle, nonSelectable, undefinedParent, missingTitle, nonSelectable)));
    }

    @Test
    public void subRecordImportNamesTheParentRecordInclChildren() {
        VocabularyException subRecord = ex(RecordValidation, Map.of("recordId", "12"), recordWithFields(fieldWithValueErrors(TITLE_BLANK)));

        assertEquals(lines("Error(s) during record import",
                "Record [12] incl. child records › Field \"title\" › The value must not be blank"),
                render(Locale.ENGLISH, recordImport(subRecord)));
    }

    @Test
    public void subRecordImportWithoutIdUsesNewRecordLabel() {
        VocabularyException subRecord = ex(RecordValidation, Map.of("recordId", "null"), recordWithFields(fieldWithValueErrors(TITLE_BLANK)));

        assertEquals(lines("Error(s) during record import",
                "New record incl. child records › Field \"title\" › The value must not be blank"),
                render(Locale.ENGLISH, recordImport(subRecord)));
    }

    @Test
    public void savingASingleRecordShowsGenericHeaderAndFieldBreadcrumb() {
        assertEquals(lines("Some issues occurred during the validation phase",
                "Field \"title\" › The value must not be blank"),
                render(Locale.ENGLISH, recordWithFields(fieldWithValueErrors(TITLE_BLANK))));
    }

    @Test
    public void parsingErrorsAreTheTopLevel() {
        VocabularyException rowError = ex(ErrorCode.RecordImportFieldCountIssue, Map.of("expectedSize", "3", "realSize", "2"));

        assertEquals(lines("Error(s) during record parsing",
                "2× Malformed tabular data, number of fields does not match the header: Header size \"3\", data row size \"2\""),
                render(Locale.ENGLISH, ex(ErrorCode.RecordImportParsingIssues, null, rowError, rowError)));
    }

    @Test
    public void fieldWithOnlyReferenceErrorsHasUnnamedFieldLabel() {
        assertEquals(lines("Error(s) during record import",
                "Field › Referenced records › VocabularyRecordEntity with ID \"7\" not found"),
                render(Locale.ENGLISH, recordImport(recordWithFields(fieldWithValueErrors(REFERENCE_MISSING)))));
    }

    @Test
    public void fieldNameIsTakenFromAnyLeafOfTheField() {
        assertEquals(lines("Error(s) during record import",
                "Field \"title\" › Referenced records › VocabularyRecordEntity with ID \"7\" not found",
                "Field \"title\" › The value must not be blank"),
                render(Locale.ENGLISH, recordImport(recordWithFields(fieldWithValueErrors(REFERENCE_MISSING, TITLE_BLANK)))));
    }

    @Test
    public void wrapperWithEmptyCausesPrintsItsOwnText() {
        VocabularyException emptyWrapper = ex(GenericValidation, null);
        emptyWrapper.setCauses(List.of());

        assertEquals(lines("Error(s) during record import", "Some issues occurred during the validation phase"),
                render(Locale.ENGLISH, recordImport(emptyWrapper)));
    }

    @Test
    public void wrapperWithoutTypeUsesServerMessageAsLabel() {
        VocabularyException mystery = ex(null, null, "Mystery level\nsecond line", TITLE_BLANK);

        assertEquals(lines("Error(s) during record import", "Mystery level › The value must not be blank"),
                render(Locale.ENGLISH, recordImport(mystery)));
    }

    @Test
    public void wrapperWithoutLabelKeepsItsOwnText() {
        VocabularyException unusual = ex(DataIntegrityViolation, Map.of("reason", "duplicate key"), TITLE_BLANK);

        assertEquals(lines("Error(s) during record import",
                "A data integrity violation has occurred: duplicate key › The value must not be blank"),
                render(Locale.ENGLISH, recordImport(unusual)));
    }

    @Test
    public void nullCauseElementsAreSkipped() {
        VocabularyException top = ex(RecordImport, null);
        top.setCauses(Arrays.asList(null, TITLE_BLANK));

        assertEquals(lines("Error(s) during record import", "The value must not be blank"), render(Locale.ENGLISH, top));
    }

    @Test
    public void everyLabelHasAnEnglishText() {
        assertEveryLabelIsTranslated(Locale.ENGLISH, APIExceptionExtractorTest::rawText);
    }

    @Test
    public void everyLabelHasAGermanText() {
        assertEveryLabelIsTranslated(Locale.GERMAN, (locale, key) -> Locale.GERMAN.equals(locale) ? rawText(locale, key) : ENGLISH_FALLBACK);
    }

    /**
     * The own text of a labelled wrapper is replaced by a sentinel, so a missing label (which falls back to the own text) is detected.
     */
    private void assertEveryLabelIsTranslated(Locale locale, BiFunction<Locale, String, String> texts) {
        String ownTextSentinel = "OWN_TEXT_INSTEAD_OF_LABEL";
        List<VocabularyException> wrappers = List.of(
                ex(ErrorCode.FieldInstanceValueIssues, null, TITLE_BLANK),
                ex(ErrorCode.FieldInstanceValueIssues, null, ex(FieldInstanceIsEmpty, null)),
                ex(ErrorCode.FieldValueReferencedRecordIssues, null, TITLE_BLANK),
                ex(RecordValidation, Map.of("recordId", "12"), TITLE_BLANK),
                ex(RecordValidation, Map.of("recordId", "null"), TITLE_BLANK),
                ex(ErrorCode.RecordValidationHierarchyIssues, null, TITLE_BLANK),
                ex(ErrorCode.SchemaValidationDefinitionIssues, null, TITLE_BLANK),
                ex(ErrorCode.VocabularyValidationMetadataSchemaIssues, Map.of("metadataSchemaId", "4"), TITLE_BLANK));
        List<String> problems = new ArrayList<>();
        for (VocabularyException wrapper : wrappers) {
            String ownKeyPrefix = "vocabularyManager_exception_" + wrapper.getErrorType();
            BiFunction<Locale, String, String> withoutOwnText = (l, key) -> key.startsWith(ownKeyPrefix) ? ownTextSentinel : texts.apply(l, key);
            String message = render(locale, recordImport(wrapper), withoutOwnText);
            if (message.contains(ownTextSentinel) || message.contains(ENGLISH_FALLBACK) || message.contains(SERVER_FALLBACK)
                    || message.contains("vocabularyManager_exception_") || PLACEHOLDER.matcher(message).find()) {
                problems.add(wrapper.getErrorType() + " " + wrapper.getParams() + " -> " + message);
            }
        }
        if (!problems.isEmpty()) {
            fail(problems.size() + " untranslated label(s) in " + locale + ":\n" + String.join("\n", problems));
        }
    }

    // e. Missing data never breaks rendering

    @Test
    public void nullErrorTypeWithServerMessageShowsServerMessage() {
        assertEquals("Something broke", render(Locale.ENGLISH, ex(null, null, "Something broke")));
    }

    @Test
    public void nullErrorTypeWithoutServerMessageShowsUnknownError() {
        assertEquals("Unknown vocabulary server error", render(Locale.ENGLISH, ex(null, null, (String) null)));
    }

    @Test
    public void leafWithNullParamsAndNullCausesIsTranslated() {
        assertEquals("The field is empty", render(Locale.ENGLISH, ex(FieldInstanceIsEmpty, null)));
    }

    @Test
    public void missingVocabularyCauseShowsExceptionMessage() {
        APIException exception = new APIException("http://localhost", "GET", 500, "boom", null, null);
        String message = new APIExceptionExtractor(exception, APIExceptionExtractorTest::rawText).getLocalizedMessage(Locale.ENGLISH);
        assertEquals(exception.getMessage(), message);
    }

    // f. Locale handling

    @Test
    public void germanLocaleUsesGermanTexts() {
        String message = render(Locale.GERMAN, ex(FieldValueIsBlank, Map.of("definitionId", "3", "definitionName", "Titel")));
        assertEquals("Der Wert darf nicht leer sein", message);
    }

    @Test
    public void textMissingInRequestedLocaleFallsBackToEnglish() {
        BiFunction<Locale, String, String> noFrench = (locale, key) -> Locale.FRENCH.equals(locale) ? key : rawText(locale, key);
        String message = render(Locale.FRENCH, ex(FieldValueIsBlank, Map.of("definitionId", "3", "definitionName", "Titre")), noFrench);
        assertEquals("The value must not be blank", message);
    }

    // g. Contract: every error type and every param set the server sends has a real text in English and German

    @Test
    public void everyServerErrorHasAnEnglishText() {
        assertEveryServerErrorIsTranslated(Locale.ENGLISH, APIExceptionExtractorTest::rawText);
    }

    @Test
    public void everyServerErrorHasAGermanText() {
        BiFunction<Locale, String, String> germanOnly = (locale, key) -> Locale.GERMAN.equals(locale) ? rawText(locale, key) : ENGLISH_FALLBACK;
        assertEveryServerErrorIsTranslated(Locale.GERMAN, germanOnly);
    }

    private void assertEveryServerErrorIsTranslated(Locale locale, BiFunction<Locale, String, String> texts) {
        List<String> problems = new ArrayList<>();
        for (ErrorCode type : ErrorCode.values()) {
            List<List<String>> paramSets = SERVER_PARAM_SETS.get(type.name());
            if (paramSets == null) {
                problems.add(type + ": no server param sets known to this test");
                continue;
            }
            for (List<String> keys : paramSets) {
                Map<String, String> params = new HashMap<>();
                keys.forEach(k -> params.put(k, "v-" + k));
                String message = render(locale, ex(type, keys.isEmpty() ? null : params), texts);
                if (message == null || message.isBlank() || message.contains(SERVER_FALLBACK) || message.contains(ENGLISH_FALLBACK)
                        || message.contains("vocabularyManager_exception_") || message.contains("Vocabulary manager error")
                        || PLACEHOLDER.matcher(message).find()) {
                    problems.add(type + " " + keys + " -> " + message);
                }
            }
        }
        if (!problems.isEmpty()) {
            fail(problems.size() + " untranslated error(s) in " + locale + ":\n" + String.join("\n", problems));
        }
    }

    @Test
    public void messageFilesHaveNoPlaceholderTexts() {
        for (Locale locale : List.of(Locale.ENGLISH, Locale.GERMAN, Locale.FRENCH)) {
            bundle(locale).forEach((key, value) -> {
                if (key.toString().startsWith("vocabularyManager_exception_")) {
                    String text = value.toString();
                    assertFalse(text.contains("Vocabulary manager error") || text.contains("gestionnaire de vocabulaire {0}"),
                            locale + " " + key + "=" + text);
                }
            });
        }
    }
}
