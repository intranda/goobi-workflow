package io.goobi.workflow.api.vocabulary.hateoas;

import io.goobi.workflow.api.vocabulary.APIException;
import org.apache.commons.lang3.NotImplementedException;
import org.goobi.managedbeans.Paginator;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class HATEOASPaginator<T, PageT extends BasePageResult<T>> implements Paginator<T> {
    public static final String NAVIGATE_PREVIOUS = "prev";
    public static final String NAVIGATE_NEXT = "next";
    public static final String NAVIGATE_FIRST = "first";
    public static final String NAVIGATE_LAST = "last";

    private final Client client = ClientBuilder.newClient();

    private Class<PageT> pageClass;
    private PageT currentPage;

    public HATEOASPaginator(Class<PageT> pageClass, PageT initialPage) {
        this.pageClass = pageClass;
        this.currentPage = initialPage;
    }

    private void request(String url) {
        request(url, Optional.empty(), Optional.empty());
    }

    private void request(String url, Optional<Long> pageSize, Optional<Long> pageNumber) {
        url = updatePageAndSizeUrlParameters(url, pageSize, pageNumber);
        try (Response response = client
                .target(url)
                .request(MediaType.APPLICATION_JSON)
                .get()) {
            if (response.getStatus() / 100 != 2) {
                throw new APIException(url, "GET", response.getStatus(), response.readEntity(String.class));
            }
            currentPage = response.readEntity(pageClass);
        }
    }

    private static String updatePageAndSizeUrlParameters(String url, Optional<Long> pageSize, Optional<Long> pageNumber) {
        if (pageSize.isPresent() || pageNumber.isPresent()) {
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
            if (questionMarkIndex < 0) {
                url += "?";
                questionMarkIndex = url.length() - 1;
            }
            url = url.substring(0, questionMarkIndex + 1) + parameters.entrySet().stream()
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .collect(Collectors.joining("&"));
        }
        return url;
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
        return currentPage.getContent();
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
