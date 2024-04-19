package io.goobi.workflow.api.vocabulary;

import io.goobi.vocabulary.exchange.Language;
import io.goobi.workflow.api.vocabulary.hateoas.LanguagePageResult;

public class VocabularyAPITest {
    private static VocabularyAPI api = new VocabularyAPI("localhost", 8080);
    public static void main(String[] args) {
        LanguagePageResult result = api.listLanguages();

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

        Language german = api.getLanguage(2);
        System.out.println(german.getAbbreviation());
        german.get_links().entrySet().stream()
                .map(e -> e.getKey() + " -> " + e.getValue())
                .forEach(System.out::println);

        german.setAbbreviation("deu");
        api.changeLanguage(german);

        Language italian = new Language();
        italian.setName("Italian");
        italian.setAbbreviation("ita");

        try {
            italian = api.createLanguage(italian);
            System.out.println(italian.getAbbreviation());
            italian.get_links().entrySet().stream()
                    .map(e -> e.getKey() + " -> " + e.getValue())
                    .forEach(System.out::println);
        } catch (APIException e) {
            System.err.println(e.getReason());
        }
    }
}
