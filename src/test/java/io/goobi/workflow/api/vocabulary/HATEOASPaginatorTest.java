package io.goobi.workflow.api.vocabulary;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.easymock.EasyMock;
import org.junit.Test;

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

public class HATEOASPaginatorTest {
    @Data
    @AllArgsConstructor
    class TestItem implements Identifiable {
        private Long id;
        private Long parent;
        private List<Long> children;
    }

    @Data
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
        Response response = EasyMock.createMock(Response.class);
        EasyMock.expect(response.getStatus()).andReturn(200).anyTimes();
        EasyMock.expect(response.readEntity(TestItemResultPage.class)).andReturn(resultPage).anyTimes();
        response.close();
        EasyMock.expectLastCall();
        EasyMock.replay(response);

        Invocation.Builder builder = EasyMock.createMock(Invocation.Builder.class);
        EasyMock.expect(builder.header(EasyMock.anyString(), EasyMock.anyString())).andReturn(builder).anyTimes();
        EasyMock.expect(builder.get()).andReturn(response).anyTimes();
        EasyMock.replay(builder);

        WebTarget target = EasyMock.createMock(WebTarget.class);
        EasyMock.expect(target.request(EasyMock.anyString())).andReturn(builder).anyTimes();
        EasyMock.replay(target);

        Client client = EasyMock.createMock(Client.class);
        EasyMock.expect(client.target((String) EasyMock.anyObject())).andReturn(target).anyTimes();
        EasyMock.replay(client);

        this.paginator.setClient(client);
    }

    private TestItem createFlat(Long id) {
        return new TestItem(id, null, null);
    }

    @Test
    public void givenFlatItemPage_whenGetItems_thenReturnAllItems() {
        List<TestItem> items = Stream.of(1L, 2L, 3L)
                .map(this::createFlat)
                .collect(Collectors.toList());

        setupPaginator(items);

        assertEquals(items, this.paginator.getItems());
    }

    @Test
    public void givenSimpleHierarchyItemPage_whenGetItems_thenReturnOnlyRootItems() {
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
    public void givenSimpleHierarchyItemPage_whenExpandAndGetItems_thenReturnAllItems() {
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
    public void givenSimpleHierarchyItemPage_whenIsExpandedChild_thenReturnFalse() {
        List<TestItem> items = List.of(
                new TestItem(1L, null, List.of(11L)),
                new TestItem(11L, 1L, null),
                new TestItem(2L, null, null),
                new TestItem(3L, null, null));

        setupPaginator(items);

        assertFalse(this.paginator.isExpanded(items.get(1)));
    }

    @Test
    public void givenSimpleHierarchyItemPage_whenIsExpandedParent_thenReturnFalse() {
        List<TestItem> items = List.of(
                new TestItem(1L, null, List.of(11L)),
                new TestItem(11L, 1L, null),
                new TestItem(2L, null, null),
                new TestItem(3L, null, null));

        setupPaginator(items);

        assertFalse(this.paginator.isExpanded(items.get(0)));
    }

    @Test
    public void givenEmptyPage_whenSettingAndGettingSortFieldAndSearchParameter_thenEverythingWorks() {
        setupPaginator(Collections.emptyList());

        this.paginator.setSortField("sort");
        this.paginator.setSearchParameter("search");

        assertEquals("sort", this.paginator.getSortField());
        assertEquals("search", this.paginator.getSearchParameter());
    }

    @Test
    public void givenComplexHierarchyItemPage_whenExpandDeepChildAndGetItems_thenReturnAllRootsAndIntermediateParentsWithChild() {
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
    public void givenEmptyPage_whenReloading_thenNoErrors() {
        setupPaginator(Collections.emptyList());

        this.paginator.reload();
    }
}
