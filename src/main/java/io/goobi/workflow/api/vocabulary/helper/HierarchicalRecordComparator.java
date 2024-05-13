package io.goobi.workflow.api.vocabulary.helper;

import io.goobi.workflow.api.vocabulary.jsfwrapper.JSFVocabularyRecord;
import lombok.Data;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class HierarchicalRecordComparator implements Comparator<JSFVocabularyRecord> {
    @Data
    static class Node {
        private JSFVocabularyRecord record;
        private List<Node> children = new LinkedList<>();
    }

    private Node root = new Node();
    private Map<Long, Node> map = new HashMap<>();
    private List<Long> orderedIds = new LinkedList<>();

    public void clear() {
        root.children.clear();
        map.clear();
    }

    public void add(JSFVocabularyRecord item) {
        if (map.containsKey(item.getId())) {
            return;
        }
        Node node = new Node();
        node.setRecord(item);
        if (item.getParentId() == null) {
            root.children.add(node);
        } else if (map.containsKey(item.getParentId())) {
            map.get(item.getParentId()).children.add(node);
        } else {
            throw new IllegalArgumentException("Parent is not loaded yet!");
        }
        map.put(item.getId(), node);
        node.getRecord().setLevel(calculateLevel(node.getRecord()));
        recalculateOrder();
    }

    private void recalculateOrder() {
        orderedIds.clear();
        for (Node child : root.getChildren()) {
            recalculateOrder(child);
        }
    }

    private void recalculateOrder(Node node) {
        orderedIds.add(node.getRecord().getId());
        for (Node child : node.getChildren()) {
            recalculateOrder(child);
        }
    }

    private int calculateLevel(JSFVocabularyRecord record) {
        if (record.getParentId() == null) {
            return 0;
        }
        return calculateLevel(map.get(record.getParentId()).getRecord()) + 1;
    }

    @Override
    public int compare(JSFVocabularyRecord o1, JSFVocabularyRecord o2) {
        return orderedIds.indexOf(o1.getId()) - orderedIds.indexOf(o2.getId());
    }
}
