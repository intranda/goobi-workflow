package io.goobi.workflow.api.vocabulary.helper;

import de.sub.goobi.helper.Helper;
import io.goobi.vocabulary.exception.VocabularyException;
import io.goobi.workflow.api.vocabulary.APIException;
import lombok.extern.log4j.Log4j2;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Log4j2
public class APIExceptionExtractor {
    private final static String MESSAGE_PREFIX = "vocabularyManager_exception_";
    private final APIException exception;

    public APIExceptionExtractor(APIException exception) {
        this.exception = exception;
    }

    public String getLocalizedMessage(Locale locale) {
        return Optional.ofNullable(exception.getVocabularyCause())
                .map(ex -> extractLocalizedVocabularyMessage(ex, locale)).orElse(exception.getMessage());
    }

    private String extractLocalizedVocabularyMessage(VocabularyException ex, Locale locale) {
        String currentLevelMessage = getLocalizedMessage(ex.getErrorType(), Optional.ofNullable(ex.getParams()).orElse(Collections.emptyMap()))
                .orElse("");
        List<String> causeLevelMessages = Optional.ofNullable(ex.getCauses())
                .map(Collection::stream)
                .map(s -> s.map(e -> extractLocalizedVocabularyMessage(e, locale))
                        .filter(m -> !m.isBlank())
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
        String message = currentLevelMessage;
        if (!causeLevelMessages.isEmpty()) {
            message += (message.isBlank() ? "" : "\n") + String.join("\n", causeLevelMessages);
        }
        return message;
    }

    private Optional<String> getLocalizedMessage(VocabularyException.ErrorCode errorType, Map<String, String> params) {
        String message = null;
        String messageKey = MESSAGE_PREFIX + errorType.toString();
        switch (errorType) {
            case FieldValuesDoNotMatchSpecifiedValidationRegex:
                message = Helper.getTranslation(messageKey, params.get("definitionName"), params.get("values"), params.get("regex"));
                break;
            case DeletionOfReferencedVocabularyRecord:
                if (params.containsKey("referencingRecordIds")) {
                    message = Helper.getTranslation(messageKey, params.get("recordId"), params.get("referencingRecordIds"));
                }
                break;
            case GenericValidation:
            case FieldInstanceIssues:
            case FieldInstanceValueIssues:
            default:
                log.warn("No translation for vocabulary exception type \"{}\" given, parameters: {}", errorType, params);
                break;
        }
        return Optional.ofNullable(message);
    }
}
