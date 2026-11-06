package io.goobi.workflow.api.vocabulary.helper;

import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.DataIntegrityViolation;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.DeletionOfReferencedVocabulary;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.DeletionOfReferencedVocabularyRecord;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.EntityNotFound;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.FieldDefinitionFallbackNotRequired;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.FieldDefinitionNeitherReferenceVocabularyNorTypeNotPossible;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.FieldDefinitionNonUniqueFallbackLanguageSpecified;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.FieldDefinitionReferenceVocabularyAndTypeNotPossible;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.FieldInstanceBelongsToWrongSchema;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.FieldInstanceIsEmpty;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.FieldInstanceIssues;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.FieldInstanceMultipleValuesNotAllowed;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.FieldInstanceValueIssues;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.FieldTypeInvalidRegexSyntax;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.FieldTypeSelectableValuesDoNotMatchSpecifiedValidationRegex;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.FieldValueContainsNonTranslatedValue;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.FieldValueHasNonAllowedTranslations;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.FieldValueIsBlank;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.FieldValueIsNotUnique;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.FieldValueMissingNonUniqueTranslations;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.FieldValueMissingRequiredTranslations;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.FieldValueReferencedRecordBelongsToWrongVocabulary;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.FieldValueReferencedRecordIssues;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.FieldValueUnspecifiedTranslations;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.FieldValuesAreNonSelectableValues;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.FieldValuesDoNotMatchSpecifiedValidationRegex;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.GenericValidation;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.IllegalAttributeProvided;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.InsertingNonExistingItem;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.Mapping;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.MissingRequiredAttribute;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.RecordImport;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.RecordImportFieldCountIssue;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.RecordImportHeaderIssues;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.RecordImportParsingIssues;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.RecordImportUnsupportedExcelCellType;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.RecordValidation;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.RecordValidationChildrenReferencesNotAllowed;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.RecordValidationHierarchyIssues;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.RecordValidationMissingRequiredFields;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.RecordValidationParentReferenceNotAllowed;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.RecordValidationSingleRootElementVocabularyAlreadyContainsRecord;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.RecordValidationUndefinedFieldsGiven;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.SchemaValidationDefinitionIssues;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.SchemaValidationMainFieldIsNotRequired;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.SchemaValidationMissingMainField;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.SchemaValidationNoDefinitions;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.SchemaValidationTitleFieldsAreNotRequired;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.SchemaValidationTooManyMainFields;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.VocabularyValidationMetadataSchemaIsHierarchical;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.VocabularyValidationMetadataSchemaIsNotSetToSingleRootElement;
import static io.goobi.vocabulary.exception.VocabularyException.ErrorCode.VocabularyValidationMetadataSchemaIssues;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.sub.goobi.helper.Helper;
import io.goobi.vocabulary.exception.VocabularyException;
import io.goobi.vocabulary.exception.VocabularyException.ErrorCode;
import io.goobi.workflow.api.vocabulary.APIException;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class APIExceptionExtractor {
    private static final String MESSAGE_PREFIX = "vocabularyManager_exception_";
    private static final String LABEL_PREFIX = MESSAGE_PREFIX + "label_";
    private static final String UNKNOWN_KEY = MESSAGE_PREFIX + "Unknown";
    private static final String UNKNOWN_FALLBACK = "Unknown vocabulary server error";
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{(\\d+)}");

    /**
     * A message text variant: the suffix of the message key and the parameters that fill its placeholders {0}, {1}, ... in this order.
     */
    private record Variant(String keySuffix, List<String> paramKeys) {
    }

    /** Variants per error type, most specific first. */
    private static final Map<ErrorCode, List<Variant>> VARIANTS = new EnumMap<>(ErrorCode.class);

    static {
        put(DataIntegrityViolation, variant("DataIntegrityViolation", "reason"));
        put(Mapping, variant("Mapping", "from", "to", "reason"), variant("MappingExport", "vocabularyId", "format", "reason"));
        put(EntityNotFound, variant("EntityNotFound", "type", "id"), variant("EntityNotFoundSearchTerm", "type", "searchTerm"),
                variant("EntityNotFoundType", "type"));
        put(MissingRequiredAttribute, variant("MissingRequiredAttribute", "attributes", "type"));
        put(InsertingNonExistingItem, variant("InsertingNonExistingItem", "type", "id"));
        put(IllegalAttributeProvided, variant("IllegalAttributeProvided", "attribute"));
        put(DeletionOfReferencedVocabulary, variant("DeletionOfReferencedVocabulary", "vocabularyId", "referencingVocabularyIds"));
        put(DeletionOfReferencedVocabularyRecord, variant("DeletionOfReferencedVocabularyRecord", "recordId", "referencingRecordIds"),
                variant("DeletionOfReferencedVocabularyRecordSubtree", "recordId"),
                variant("DeletionOfReferencedVocabularyRecordAll", "vocabularyId"));
        putWithoutParams(GenericValidation, SchemaValidationNoDefinitions, SchemaValidationMissingMainField, SchemaValidationDefinitionIssues,
                SchemaValidationTitleFieldsAreNotRequired, VocabularyValidationMetadataSchemaIsNotSetToSingleRootElement,
                VocabularyValidationMetadataSchemaIsHierarchical, RecordValidationHierarchyIssues, RecordImportParsingIssues,
                FieldInstanceIssues, FieldInstanceIsEmpty, FieldInstanceValueIssues, FieldValueReferencedRecordIssues);
        put(SchemaValidationTooManyMainFields, variant("SchemaValidationTooManyMainFields", "mainFieldNames"));
        put(SchemaValidationMainFieldIsNotRequired, variant("SchemaValidationMainFieldIsNotRequired", "mainFieldNames"));
        put(FieldDefinitionNonUniqueFallbackLanguageSpecified,
                variant("FieldDefinitionNonUniqueFallbackLanguageSpecified", "name", "numberOfSpecifiedFallbackLanguages"));
        put(FieldDefinitionFallbackNotRequired, variant("FieldDefinitionFallbackNotRequired", "name"));
        put(FieldDefinitionNeitherReferenceVocabularyNorTypeNotPossible,
                variant("FieldDefinitionNeitherReferenceVocabularyNorTypeNotPossible", "name"));
        put(FieldDefinitionReferenceVocabularyAndTypeNotPossible, variant("FieldDefinitionReferenceVocabularyAndTypeNotPossible", "name"));
        put(FieldTypeInvalidRegexSyntax, variant("FieldTypeInvalidRegexSyntax", "regex"));
        put(FieldTypeSelectableValuesDoNotMatchSpecifiedValidationRegex,
                variant("FieldTypeSelectableValuesDoNotMatchSpecifiedValidationRegex", "regex", "selectableValues"));
        put(VocabularyValidationMetadataSchemaIssues, variant("VocabularyValidationMetadataSchemaIssues", "metadataSchemaId"));
        put(RecordValidation, variant("RecordValidation", "recordId"), variant("RecordValidationWithoutId"));
        put(RecordValidationMissingRequiredFields, variant("RecordValidationMissingRequiredFields", "missingFieldNames"));
        put(RecordValidationUndefinedFieldsGiven, variant("RecordValidationUndefinedFieldsGiven", "providedFieldNames"),
                variant("RecordValidationUndefinedFieldsGiven", "providedFieldIds"));
        put(RecordValidationParentReferenceNotAllowed, variant("RecordValidationParentReferenceNotAllowed", "parentId"));
        put(RecordValidationChildrenReferencesNotAllowed, variant("RecordValidationChildrenReferencesNotAllowed", "childrenRecordIds"));
        put(RecordValidationSingleRootElementVocabularyAlreadyContainsRecord,
                variant("RecordValidationSingleRootElementVocabularyAlreadyContainsRecord", "vocabularyName"));
        put(RecordImport, variant("RecordImportUndefinedParent", "parentId"), variant("RecordImport"));
        put(RecordImportHeaderIssues, variant("RecordImportHeaderIssues", "undefinedFields", "missingFields"));
        put(RecordImportFieldCountIssue, variant("RecordImportFieldCountIssue", "expectedSize", "realSize"));
        put(RecordImportUnsupportedExcelCellType, variant("RecordImportUnsupportedExcelCellType", "cellType"));
        put(FieldInstanceBelongsToWrongSchema,
                variant("FieldInstanceBelongsToWrongSchema", "definitionName", "definitionId", "specifiedSchemaId", "requiredSchemaId"));
        put(FieldInstanceMultipleValuesNotAllowed,
                variant("FieldInstanceMultipleValuesNotAllowed", "definitionName", "definitionId", "numberOfValues"));
        put(FieldValuesDoNotMatchSpecifiedValidationRegex,
                variant("FieldValuesDoNotMatchSpecifiedValidationRegex", "definitionName", "values", "regex"));
        put(FieldValuesAreNonSelectableValues,
                variant("FieldValuesAreNonSelectableValues", "definitionName", "values", "selectableValues"));
        put(FieldValueIsBlank, variant("FieldValueIsBlank", "definitionName"));
        put(FieldValueContainsNonTranslatedValue, variant("FieldValueContainsNonTranslatedValue", "definitionName"));
        put(FieldValueIsNotUnique, variant("FieldValueIsNotUnique", "definitionName", "duplicateValues"));
        put(FieldValueHasNonAllowedTranslations, variant("FieldValueHasNonAllowedTranslations", "definitionName", "translationLanguages"));
        put(FieldValueUnspecifiedTranslations,
                variant("FieldValueUnspecifiedTranslations", "definitionName", "unspecifiedTranslationLanguages"));
        put(FieldValueMissingRequiredTranslations,
                variant("FieldValueMissingRequiredTranslations", "definitionName", "requiredTranslationLanguages"));
        put(FieldValueMissingNonUniqueTranslations,
                variant("FieldValueMissingNonUniqueTranslations", "definitionName", "givenTranslationLanguages"));
        put(FieldValueReferencedRecordBelongsToWrongVocabulary,
                variant("FieldValueReferencedRecordBelongsToWrongVocabulary", "recordId", "referencedVocabulary",
                        "specifiedReferenceVocabulary"));
    }

    private static Variant variant(String keySuffix, String... paramKeys) {
        return new Variant(keySuffix, List.of(paramKeys));
    }

    private static void put(ErrorCode errorType, Variant... variants) {
        VARIANTS.put(errorType, List.of(variants));
    }

    private static void putWithoutParams(ErrorCode... errorTypes) {
        for (ErrorCode errorType : errorTypes) {
            put(errorType, variant(errorType.name()));
        }
    }

    private final APIException exception;
    private final BiFunction<Locale, String, String> rawText;

    public APIExceptionExtractor(APIException exception) {
        this(exception, Helper::getString);
    }

    /**
     * @param rawText looks up the untranslated message template for a locale and key; returns the key itself if there is none
     */
    APIExceptionExtractor(APIException exception, BiFunction<Locale, String, String> rawText) {
        this.exception = exception;
        this.rawText = rawText;
    }

    public String getLocalizedMessage(Locale locale) {
        return Optional.ofNullable(exception.getVocabularyCause())
                .map(ex -> render(ex, locale == null ? Locale.ENGLISH : locale))
                .orElse(exception.getMessage());
    }

    /**
     * Renders the own text of the top exception as header, followed by one breadcrumb line per leaf cause: the labels of the wrapping levels
     * and the text of the leaf, joined by " \u203A ". Identical lines are shown once, prefixed with their number of occurrences ("3\u00D7 ").
     */
    private String render(VocabularyException top, Locale locale) {
        List<String> lines = new ArrayList<>();
        collectLines(top, List.of(), locale, lines);
        Map<String, Integer> counts = new LinkedHashMap<>();
        lines.forEach(line -> counts.merge(line, 1, Integer::sum));
        List<String> result = new ArrayList<>();
        result.add(ownText(top, locale));
        counts.forEach((line, count) -> result.add(count > 1 ? count + "\u00D7 " + line : line));
        return String.join("\n", result);
    }

    private void collectLines(VocabularyException node, List<String> breadcrumb, Locale locale, List<String> lines) {
        for (VocabularyException cause : causes(node)) {
            List<String> path = new ArrayList<>(breadcrumb);
            if (causes(cause).isEmpty()) {
                path.add(ownText(cause, locale));
                lines.add(String.join(" \u203A ", path));
            } else {
                String segment = segment(cause, locale);
                if (!segment.isBlank()) {
                    path.add(segment);
                }
                collectLines(cause, path, locale, lines);
            }
        }
    }

    private List<VocabularyException> causes(VocabularyException node) {
        return Optional.ofNullable(node.getCauses()).orElse(Collections.emptyList()).stream().filter(Objects::nonNull).toList();
    }

    private Map<String, String> params(VocabularyException node) {
        return Optional.ofNullable(node.getParams()).orElse(Collections.emptyMap());
    }

    /**
     * The breadcrumb segment of a wrapping level: a label, nothing for purely technical levels, else the own text.
     */
    private String segment(VocabularyException node, Locale locale) {
        ErrorCode errorType = node.getErrorType();
        if (errorType == null) {
            return ownText(node, locale);
        }
        Map<String, String> params = params(node);
        Optional<String> label = switch (errorType) {
            case GenericValidation, FieldInstanceIssues -> Optional.of("");
            case FieldInstanceValueIssues -> findDefinitionName(node)
                    .map(name -> label("FieldNamed", locale, name))
                    .orElseGet(() -> label("Field", locale));
            case FieldValueReferencedRecordIssues -> label("FieldValueReferencedRecordIssues", locale);
            case RecordValidation -> isPresent(params.get("recordId"))
                    ? label("RecordValidation", locale, params.get("recordId"))
                    : label("RecordValidationWithoutId", locale);
            case RecordValidationHierarchyIssues -> label("RecordValidationHierarchyIssues", locale);
            case SchemaValidationDefinitionIssues -> label("SchemaValidationDefinitionIssues", locale);
            case VocabularyValidationMetadataSchemaIssues -> isPresent(params.get("metadataSchemaId"))
                    ? label("VocabularyValidationMetadataSchemaIssues", locale, params.get("metadataSchemaId"))
                    : Optional.empty();
            default -> Optional.empty();
        };
        return label.orElseGet(() -> ownText(node, locale));
    }

    private Optional<String> label(String keySuffix, Locale locale, String... values) {
        return template(LABEL_PREFIX + keySuffix, locale).map(template -> fillPlaceholders(template, List.of(values)));
    }

    /** The definition name of the first leaf below the node that has one. */
    private Optional<String> findDefinitionName(VocabularyException node) {
        for (VocabularyException cause : causes(node)) {
            Optional<String> name = causes(cause).isEmpty() ? Optional.ofNullable(params(cause).get("definitionName")).filter(this::isPresent)
                    : findDefinitionName(cause);
            if (name.isPresent()) {
                return name;
            }
        }
        return Optional.empty();
    }

    private String ownText(VocabularyException ex, Locale locale) {
        Map<String, String> params = params(ex);
        return translate(ex.getErrorType(), params, locale).orElseGet(() -> fallbackText(ex, params, locale));
    }

    private Optional<String> translate(ErrorCode errorType, Map<String, String> params, Locale locale) {
        if (errorType == null) {
            return Optional.empty();
        }
        return VARIANTS.getOrDefault(errorType, Collections.emptyList())
                .stream()
                .filter(variant -> variant.paramKeys().stream().allMatch(key -> isPresent(params.get(key))))
                .findFirst()
                .flatMap(variant -> template(MESSAGE_PREFIX + variant.keySuffix(), locale)
                        .map(template -> fillPlaceholders(template, variant.paramKeys().stream().map(params::get).toList())));
    }

    private boolean isPresent(String value) {
        return value != null && !value.isBlank() && !"null".equals(value);
    }

    private Optional<String> template(String key, Locale locale) {
        return List.of(locale, Locale.ENGLISH).stream()
                .distinct()
                .map(l -> rawText.apply(l, key))
                .filter(text -> text != null && !text.isBlank() && !text.equals(key))
                .findFirst();
    }

    private String fillPlaceholders(String template, List<String> values) {
        Matcher matcher = PLACEHOLDER.matcher(template);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            int index = Integer.parseInt(matcher.group(1));
            String replacement = index < values.size() ? values.get(index) : matcher.group();
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private String fallbackText(VocabularyException ex, Map<String, String> params, Locale locale) {
        if (ex.getErrorType() != null) {
            log.warn("No translation for vocabulary exception type \"{}\" given, parameters: {}", ex.getErrorType(), params);
        }
        return firstNonBlankLine(ex.getMessage())
                .or(() -> Optional.ofNullable(ex.getErrorType()).map(ErrorCode::name))
                .orElseGet(() -> template(UNKNOWN_KEY, locale).orElse(UNKNOWN_FALLBACK));
    }

    private Optional<String> firstNonBlankLine(String message) {
        if (message == null) {
            return Optional.empty();
        }
        return message.lines().map(String::trim).filter(line -> !line.isEmpty()).findFirst();
    }
}
