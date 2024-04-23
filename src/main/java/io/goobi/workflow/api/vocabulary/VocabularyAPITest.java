package io.goobi.workflow.api.vocabulary;

import io.goobi.vocabulary.exchange.FieldInstance;
import io.goobi.vocabulary.exchange.Language;
import io.goobi.workflow.api.vocabulary.hateoas.FieldTypePageResult;
import io.goobi.workflow.api.vocabulary.hateoas.LanguagePageResult;
import io.goobi.workflow.api.vocabulary.hateoas.VocabularySchemaPageResult;

import java.util.Comparator;
import java.util.stream.Collectors;

public class VocabularyAPITest {
    private static LanguageAPI languageAPI = new LanguageAPI("localhost", 8080);
    private static FieldTypeAPI typeAPI = new FieldTypeAPI("localhost", 8080);
    private static VocabularySchemaAPI schemaAPI = new VocabularySchemaAPI("localhost", 8080);
    private static VocabularyAPI vocabularyAPI = new VocabularyAPI("localhost", 8080);
    private static VocabularyRecordAPI recordAPI = new VocabularyRecordAPI("localhost", 8080);

    public static void main(String[] args) {
        FieldTypePageResult types = typeAPI.list();
        System.out.println(types);
        types.getContent().stream()
                .map(l -> l.getId() + ": " + l.getName())
                .forEach(System.out::println);

        LanguagePageResult result = languageAPI.list();
        System.out.println(result);
        result.getContent().stream()
                .map(l -> l.getId() + ": " + l.getName())
                .forEach(System.out::println);
        result.getContent().stream()
                .map(Language::get_links)
                .flatMap(e -> e.entrySet().stream())
                .map(e -> e.getKey() + " -> " + e.getValue())
                .forEach(System.out::println);
        result.get_links().entrySet().stream()
                .map(e -> e.getKey() + " -> " + e.getValue())
                .forEach(System.out::println);

        Language german = languageAPI.get(2);
        System.out.println(german.getAbbreviation());
        german.get_links().entrySet().stream()
                .map(e -> e.getKey() + " -> " + e.getValue())
                .forEach(System.out::println);

        german.setAbbreviation("ger");
        languageAPI.change(german);

        Language oldItalian = result.getContent().stream()
                .filter(l -> l.getAbbreviation().equals("ita"))
                .findAny().orElseThrow();
        languageAPI.delete(oldItalian);

        Language italian = new Language();
        italian.setName("Italian");
        italian.setAbbreviation("ita");

        try {
            italian = languageAPI.create(italian);
            System.out.println(italian.getAbbreviation());
            italian.get_links().entrySet().stream()
                    .map(e -> e.getKey() + " -> " + e.getValue())
                    .forEach(System.out::println);
        } catch (APIException e) {
            System.err.println(e.getReason());
        }

        VocabularySchemaPageResult schemas = schemaAPI.list();
        schemas.getContent().forEach(s -> {
            System.out.println(s.getId());
            s.getDefinitions().forEach(d -> System.out.println("\t" + d.getName()));
        });

        vocabularyAPI.list().getContent().stream()
                .map(v -> v.getName() + " (" + v.getDescription() + ")")
                .forEach(System.out::println);

        recordAPI.list(3).getContent().stream()
                .map(r -> r.getFields().stream()
                        .sorted(Comparator.comparing(FieldInstance::getDefinitionId))
                        .flatMap(f -> f.getValues().stream())
                        .flatMap(v -> v.getTranslations().values().stream())
                                .collect(Collectors.joining(", ")))
                .forEach(System.out::println);
    }
}
