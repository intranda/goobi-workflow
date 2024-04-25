package io.goobi.workflow.api.vocabulary.objects;

import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.managers.IManager;
import io.goobi.vocabulary.exchange.Vocabulary;
import io.goobi.workflow.api.vocabulary.VocabularyRecordAPI;
import org.goobi.beans.DatabaseObject;
import org.goobi.beans.Institution;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class VocabularyRecordManager implements IManager {
    private final VocabularyRecordAPI api;
    private final Vocabulary vocabulary;

    public VocabularyRecordManager(VocabularyRecordAPI api, Vocabulary vocabulary) {
        this.api = api;
        this.vocabulary = vocabulary;
    }

    @Override
    public int getHitSize(String order, String filter, Institution institution) throws DAOException {
        return (int) api.list(vocabulary.getId()).getPage().getTotalElements();
    }

    @Override
    public List<? extends DatabaseObject> getList(String order, String filter, Integer start, Integer count, Institution institution) throws DAOException {
        return api.list(vocabulary.getId(), Optional.of(count), Optional.of(start/count)).getContent();
    }

    // TODO: This does not contain ALL ids, but still paged ones
    @Override
    public List<Integer> getIdList(String order, String filter, Institution institution) {
        return api.list(vocabulary.getId()).getContent().stream()
                .map(VocabularyRecordDO::getId)
                .map(Long::intValue)
                .collect(Collectors.toList());
    }
}
