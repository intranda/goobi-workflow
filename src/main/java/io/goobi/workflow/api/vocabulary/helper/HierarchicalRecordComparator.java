package io.goobi.workflow.api.vocabulary.helper;

import lombok.Data;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class HierarchicalRecordComparator implements Comparator<ExtendedVocabularyRecord> {
    @Data
    static class Node {
        private ExtendedVocabularyRecord record;
        private List<Node> children = new LinkedList<>();
    }

    private Node root = new Node();
    private Map<Long, Node> map = new HashMap<>();
    private List<Long> orderedIds = new LinkedList<>();

    public void clear() {
        root.children.clear();
        map.clear();
    }

    public void add(ExtendedVocabularyRecord item) {
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
//        node.getRecord().setLevel(calculateLevel(node.getRecord()));
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

    private int calculateLevel(ExtendedVocabularyRecord record) {
        if (record.getParentId() == null) {
            return 0;
        }
        return calculateLevel(map.get(record.getParentId()).getRecord()) + 1;
    }

    @Override
    public int compare(ExtendedVocabularyRecord o1, ExtendedVocabularyRecord o2) {
        return orderedIds.indexOf(o1.getId()) - orderedIds.indexOf(o2.getId());
    }
}
