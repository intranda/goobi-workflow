package io.goobi.workflow.api.vocabulary.hateoas;

import io.goobi.vocabulary.exception.VocabularyException;
import io.goobi.vocabulary.exchange.Identifiable;
import io.goobi.workflow.api.vocabulary.APIException;
import lombok.Data;
import lombok.Setter;
import org.apache.commons.lang3.NotImplementedException;
import org.goobi.managedbeans.Paginator;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.goobi.workflow.api.vocabulary.VocabularyAPIManager.setupBearerTokenAuthenticationIfPresent;

public class HATEOASPaginator<T extends Identifiable, PageT extends BasePageResult<T>> implements Paginator<T> {
    public static final String NAVIGATE_PREVIOUS = "prev";
    public static final String NAVIGATE_NEXT = "next";
    public static final String NAVIGATE_FIRST = "first";
    public static final String NAVIGATE_LAST = "last";

    @Data
    class Node {
        private Long id;
        private T data;
        private List<Node> children;
        private boolean visible = false;

        public void load() {
            if (data != null) {
                return;
            }

            data = postLoader.apply(getId());
            prepareChildren();
        }

        private void prepareChildren() {
            if (getData() != null) {
                childrenExtractor.ifPresent(e -> {
                    Collection<Long> extractedChildren = e.apply(getData());
                    if (extractedChildren != null) {
                        setChildren(new LinkedList<>(
                                extractedChildren.stream().map(c -> {
                                    Node child = new Node();
                                    child.setId(c);
                                    return child;
                                }).collect(Collectors.toList())));
                    }
                });
            }
            if (getChildren() == null) {
                setChildren(new LinkedList<>());
            }
        }
    }

    @Setter
    private Client client = ClientBuilder.newClient();
    private final Optional<Function<T, Collection<Long>>> childrenExtractor;
    private final Optional<Function<T, Long>> parentExtractor;
    private final Function<Long, T> postLoader;
    private Optional<String> searchParameter = Optional.empty();
    private Optional<String> sortField = Optional.empty();

    private Class<PageT> pageClass;
    private PageT currentPage;
    private List<Node> tree = new LinkedList<>();
    private Map<Long, Node> treeMap = new HashMap<>();
    private List<T> items = new LinkedList<>();

    public HATEOASPaginator(Class<PageT> pageClass, PageT initialPage, Function<T, Collection<Long>> childrenExtractor, Function<T, Long> parentExtractor, Function<Long, T> postLoader) {
        this.pageClass = pageClass;
        this.childrenExtractor = Optional.ofNullable(childrenExtractor);
        this.parentExtractor = Optional.ofNullable(parentExtractor);
        this.postLoader = postLoader;
        setCurrentPage(initialPage);
    }

    private void setCurrentPage(PageT page) {
        this.currentPage = page;
        this.tree.clear();
        this.currentPage.getContent().forEach(this::insertElement);
        this.tree.forEach(this::recursiveShow);
        rebuildTree();
    }

    private void recursiveShow(Node node) {
        if (node.getData() == null) {
            return;
        }
        node.setVisible(true);
        if (node.getChildren() != null) {
            node.getChildren().forEach(this::recursiveShow);
        }
    }

    private void rebuildTree() {
        this.items.clear();
        this.treeMap.clear();
        this.tree.stream()
                .filter(Node::isVisible)
                .forEachOrdered(this::addToFlatList);
    }

    private void addToFlatList(Node node) {
        if (node.isVisible() && node.getData() != null) {
            this.items.add(node.getData());
        }
        this.treeMap.put(node.getId(), node);
        if (node.getData() != null) {
            node.getChildren().forEach(this::addToFlatList);
        }
    }

    public void expand(T entry) {
        Node node = this.treeMap.get(entry.getId());
        node.getChildren().forEach(child -> {
            child.load();
            child.setVisible(true);
        });
        rebuildTree();
    }

    public void collapse(T entry) {
        Node node = this.treeMap.get(entry.getId());
        this.recursiveCollapseChildren(node);
        rebuildTree();
    }

    private void recursiveCollapseChildren(Node node) {
        if (node.getChildren() == null) {
            return;
        }
        node.getChildren().forEach(child -> {
            child.setVisible(false);
            this.recursiveCollapseChildren(child);
        });
    }

    private void recursiveParentExpanding(Node node) {
        node.load();
        node.setVisible(true);
        this.parentExtractor.ifPresent(e -> {
            Long parentId = e.apply(node.getData());
            if (parentId == null) {
                return;
            }
            Node parent = getOrCreateParent(parentId);
            parent.setVisible(true);
            recursiveParentExpanding(parent);
        });
    }

    public boolean isExpanded(T entry) {
        List<Node> children = this.treeMap.get(entry.getId()).getChildren();
        if (children == null) {
            return false;
        }
        return children.stream().anyMatch(Node::isVisible);
    }

    public void setSearchParameter(String searchParameter) {
        if (searchParameter.isBlank()) {
            searchParameter = null;
        }
        this.searchParameter = Optional.ofNullable(searchParameter);
    }

    public String getSearchParameter() {
        return searchParameter.orElse(null);
    }

    public void setSortField(String sortField) {
        if (sortField.isBlank()) {
            sortField = null;
        }
        this.sortField = Optional.ofNullable(sortField);
    }

    public String getSortField() {
        return sortField.orElse(null);
    }

    private void request(String url) {
        request(url, Optional.empty(), Optional.empty());
    }

    private void request(String url, Optional<Long> pageSize, Optional<Long> pageNumber) {
        url = updatePageAndSizeUrlParameters(url, pageSize, pageNumber, sortField, searchParameter);
        Invocation.Builder builder = client
                .target(url)
                .request(MediaType.APPLICATION_JSON);
        builder = setupBearerTokenAuthenticationIfPresent(builder);
        try (Response response = builder.get()) {
            if (response.getStatus() / 100 != 2) {
                throw new APIException(url, "GET", response.getStatus(), "Vocabulary server error", response.readEntity(VocabularyException.class), null);
            }
            setCurrentPage(response.readEntity(pageClass));
        }
    }

    private static String updatePageAndSizeUrlParameters(String url, Optional<Long> pageSize, Optional<Long> pageNumber, Optional<String> sortField, Optional<String> searchParameter) {
        Map<String, String> parameters = new HashMap<>();
        int questionMarkIndex = url.indexOf('?');
        if (questionMarkIndex > 0) {
            String[] parts = url.substring(questionMarkIndex + 1).split("&");
            for (String part : parts) {
                String[] keyValue = part.split("=");
                if (keyValue.length == 2) {
                    parameters.put(keyValue[0], keyValue[1]);
                }
            }
        }
        pageSize.ifPresent(value -> parameters.put("size", String.valueOf(value)));
        pageNumber.ifPresent(value -> parameters.put("page", String.valueOf(value)));
        sortField.ifPresent(s -> parameters.put("sort", s));
        searchParameter.ifPresent(s -> parameters.put("search", s));
        if (searchParameter.isEmpty()) {
            parameters.remove("search");
        }
        if (questionMarkIndex < 0) {
            url += "?";
            questionMarkIndex = url.length() - 1;
        }
        url = url.substring(0, questionMarkIndex + 1) + parameters.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));
        return url;
    }

    @Override
    public void reload() {
        request(currentPage.get_links().get("self").getHref(), Optional.empty(), Optional.of(0L));
    }

    @Override
    public void postLoad(T item) {
        this.innerPostLoad(item);
        this.rebuildTree();
    }

    private void innerPostLoad(T item) {
        if (!this.treeMap.containsKey(item.getId())) {
            this.insertElement(item);
        }

        Node node = this.treeMap.get(item.getId());
        node.load();
        node.setVisible(true);
        recursiveParentExpanding(node);
    }

    private void insertElement(T item) {
        Long parentId = null;
        if (this.parentExtractor.isPresent()) {
            parentId = this.parentExtractor.get().apply(item);
        }

        Node node = new Node();
        node.setId(item.getId());
        node.setData(item);
        node.prepareChildren();

        if (parentId == null) {
            this.tree.add(node);
        } else {
            getOrCreateParent(parentId).getChildren().add(node);
        }
        this.treeMap.putIfAbsent(item.getId(), node);
    }

    private Node getOrCreateParent(Long parentId) {
        if (!this.treeMap.containsKey(parentId)) {
            T parentData = postLoader.apply(parentId);
            insertElement(parentData);
        }
        Node result = this.treeMap.get(parentId);
        result.load();
        return result;
    }

    @Override
    public long getCurrentPage() {
        return currentPage.getPage().getNumber() + 1;
    }

    @Override
    public void setCurrentPage(long page) {
        if (page != getCurrentPage()) {
            if (page > getNumberOfPages()) {
                page = getNumberOfPages();
            }
            if (page < 1) {
                page = 1;
            }
            request(currentPage.get_links().get(NAVIGATE_FIRST).getHref(), Optional.of(currentPage.getPage().getSize()), Optional.of(page - 1));
        }
    }

    @Override
    public List<T> getItems() {
        return this.items;
    }

    @Override
    public long getPageSize() {
        return currentPage.getPage().getSize();
    }

    @Override
    public void setPageSize(long pageSize) {
        throw new NotImplementedException("to be done");
    }

    @Override
    public long getTotalResults() {
        return currentPage.getPage().getTotalElements();
    }

    @Override
    public long getNumberOfPages() {
        return currentPage.getPage().getTotalPages();
    }

    @Override
    public boolean hasPreviousPage() {
        return currentPage.get_links().containsKey(NAVIGATE_PREVIOUS);
    }

    @Override
    public boolean hasNextPage() {
        return currentPage.get_links().containsKey(NAVIGATE_NEXT);
    }

    @Override
    public void cmdMoveFirst() {
        request(currentPage.get_links().get(NAVIGATE_FIRST).getHref());
    }

    @Override
    public void cmdMoveLast() {
        request(currentPage.get_links().get(NAVIGATE_LAST).getHref());
    }

    @Override
    public void cmdMoveNext() {
        if (!hasNextPage()) {
            return;
        }
        request(currentPage.get_links().get(NAVIGATE_NEXT).getHref());
    }

    @Override
    public void cmdMovePrevious() {
        if (!hasPreviousPage()) {
            return;
        }
        request(currentPage.get_links().get(NAVIGATE_PREVIOUS).getHref());
    }
}
