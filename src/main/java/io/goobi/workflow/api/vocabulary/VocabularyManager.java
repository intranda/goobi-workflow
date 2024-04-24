package io.goobi.workflow.api.vocabulary;

import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.IManager;
import io.goobi.vocabulary.exchange.Vocabulary;
import org.goobi.beans.DatabaseObject;
import org.goobi.beans.Institution;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class VocabularyManager implements IManager {
    private final VocabularyAPI api;

    public VocabularyManager(VocabularyAPI api) {
        this.api = api;
    }

    @Override
    public int getHitSize(String order, String filter, Institution institution) throws DAOException {
        return (int) api.list().getPage().getTotalElements();
    }

    @Override
    public List<? extends DatabaseObject> getList(String order, String filter, Integer start, Integer count, Institution institution) throws DAOException {
        return api.list(Optional.of(count), Optional.of(start/count)).getContent();
    }

    // TODO: This does not contain ALL ids, but still paged ones
    @Override
    public List<Integer> getIdList(String order, String filter, Institution institution) {
        return api.list().getContent().stream()
                .map(Vocabulary::getId)
                .map(Long::intValue)
                .collect(Collectors.toList());
    }
}
