package io.goobi.workflow.api.vocabulary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import io.goobi.vocabulary.exchange.HateoasHref;
import io.goobi.vocabulary.exchange.Identifiable;
import io.goobi.workflow.api.vocabulary.hateoas.BasePageResult;
import io.goobi.workflow.api.vocabulary.hateoas.HATEOASPaginator;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class HATEOASPaginatorTest {
    @Data
    @AllArgsConstructor
    class TestItem implements Identifiable {
        private Long id;
        private Long parent;
        private List<Long> children;
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    class TestItemResultPage extends BasePageResult<TestItem> {
        private List<TestItem> content;
    }

    private HATEOASPaginator<TestItem, TestItemResultPage> paginator;

    private void setupPaginator(List<TestItem> items) {
        List<TestItem> rootItems = items.stream()
                .filter(i -> i.getParent() == null)
                .collect(Collectors.toList());
        TestItemResultPage resultPage = new TestItemResultPage();
        HateoasHref selfLink = new HateoasHref();
        selfLink.setHref("http://localhost:8081/test/1");
        resultPage.set_links(Map.of("self", selfLink));
        resultPage.setContent(rootItems);
        this.paginator = new HATEOASPaginator<>(TestItemResultPage.class, resultPage, TestItem::getChildren, TestItem::getParent,
                id -> items.stream().filter(i -> i.getId().equals(id)).findFirst().orElseThrow());

        // Client mocking
        Response response = Mockito.mock(Response.class);
        Mockito.when(response.getStatus()).thenReturn(200);
        Mockito.when(response.readEntity(TestItemResultPage.class)).thenReturn(resultPage);
        response.close();

        Invocation.Builder builder = Mockito.mock(Invocation.Builder.class);
        Mockito.when(builder.header(Mockito.anyString(), Mockito.anyString())).thenReturn(builder);
        Mockito.when(builder.get()).thenReturn(response);

        WebTarget target = Mockito.mock(WebTarget.class);
        Mockito.when(target.request(Mockito.anyString())).thenReturn(builder);

        Client client = Mockito.mock(Client.class);
        Mockito.when(client.target((String) Mockito.any())).thenReturn(target);

        this.paginator.setClient(client);
    }

    private TestItem createFlat(Long id) {
        return new TestItem(id, null, null);
    }

    @Test
    public void givenFlatItemPageWhenGetItemsThenReturnAllItems() {
        List<TestItem> items = Stream.of(1L, 2L, 3L)
                .map(this::createFlat)
                .collect(Collectors.toList());

        setupPaginator(items);

        assertEquals(items, this.paginator.getItems());
    }

    @Test
    public void givenSimpleHierarchyItemPageWhenGetItemsThenReturnOnlyRootItems() {
        List<TestItem> items = List.of(
                new TestItem(1L, null, List.of(11L)),
                new TestItem(2L, null, null),
                new TestItem(3L, null, null),
                new TestItem(11L, 1L, null));
        List<TestItem> rootItems = items.stream()
                .filter(i -> i.getParent() == null)
                .collect(Collectors.toList());

        setupPaginator(items);

        assertEquals(rootItems, this.paginator.getItems());
    }

    @Test
    public void givenSimpleHierarchyItemPageWhenExpandAndGetItemsThenReturnAllItems() {
        List<TestItem> items = List.of(
                new TestItem(1L, null, List.of(11L)),
                new TestItem(11L, 1L, null),
                new TestItem(2L, null, null),
                new TestItem(3L, null, null));

        setupPaginator(items);
        this.paginator.expand(items.get(0));

        assertEquals(items, this.paginator.getItems());
    }

    @Test
    public void givenSimpleHierarchyItemPageWhenIsExpandedChildThenReturnFalse() {
        List<TestItem> items = List.of(
                new TestItem(1L, null, List.of(11L)),
                new TestItem(11L, 1L, null),
                new TestItem(2L, null, null),
                new TestItem(3L, null, null));

        setupPaginator(items);

        assertFalse(this.paginator.isExpanded(items.get(1)));
    }

    @Test
    public void givenSimpleHierarchyItemPageWhenIsExpandedParentThenReturnFalse() {
        List<TestItem> items = List.of(
                new TestItem(1L, null, List.of(11L)),
                new TestItem(11L, 1L, null),
                new TestItem(2L, null, null),
                new TestItem(3L, null, null));

        setupPaginator(items);

        assertFalse(this.paginator.isExpanded(items.get(0)));
    }

    @Test
    public void givenEmptyPageWhenSettingAndGettingSortFieldAndSearchParameterThenEverythingWorks() {
        setupPaginator(Collections.emptyList());

        this.paginator.setSortField("sort");
        this.paginator.setSearchParameter("search");

        assertEquals("sort", this.paginator.getSortField());
        assertEquals("search", this.paginator.getSearchParameter());
    }

    @Test
    public void givenComplexHierarchyItemPageWhenExpandDeepChildAndGetItemsThenReturnAllRootsAndIntermediateParentsWithChild() {
        List<TestItem> items = List.of(
                new TestItem(1L, null, List.of(11L)),
                new TestItem(11L, 1L, List.of(111L)),
                new TestItem(111L, 11L, List.of(1111L)),
                new TestItem(1111L, 111L, null),
                new TestItem(2L, null, List.of(21L)),
                new TestItem(21L, 2L, null),
                new TestItem(3L, null, null));

        setupPaginator(items);
        this.paginator.postLoad(items.get(3));

        assertEquals(items.stream().filter(i -> !i.getId().equals(21L)).collect(Collectors.toList()), this.paginator.getItems());
    }

    @Test
    public void givenEmptyPageWhenReloadingThenNoErrors() {
        setupPaginator(Collections.emptyList());

        this.paginator.reload();
    }
}
