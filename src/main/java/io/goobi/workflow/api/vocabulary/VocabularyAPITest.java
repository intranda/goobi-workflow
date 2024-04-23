package io.goobi.workflow.api.vocabulary;

import io.goobi.vocabulary.exchange.Language;
import io.goobi.workflow.api.vocabulary.hateoas.FieldTypePageResult;
import io.goobi.workflow.api.vocabulary.hateoas.LanguagePageResult;

public class VocabularyAPITest {
    private static LanguageAPI languageAPI = new LanguageAPI("localhost", 8080);
    private static FieldTypeAPI typeAPI = new FieldTypeAPI("localhost", 8080);

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
    }
}
