package com.app.eventnexus.dtos.responses;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Generic pagination wrapper returned by all paginated list endpoints.
 *
 * <p>Fields match the names that the Angular frontend expects:
 * {@code content}, {@code page}, {@code size}, {@code totalElements},
 * {@code totalPages}, {@code last}.
 *
 * @param <T> the type of items in the page
 */
public class PageResponse<T> {

    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;

    /** Required by Jackson for deserialization. */
    public PageResponse() {}

    public PageResponse(List<T> content, int page, int size,
                        long totalElements, int totalPages, boolean last) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.last = last;
    }

    /**
     * Builds a {@code PageResponse} from a Spring Data {@link Page}.
     *
     * @param <T>  item type
     * @param page the Spring Data page
     * @return a populated {@code PageResponse}
     */
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    public List<T> getContent() { return content; }
    public int getPage() { return page; }
    public int getSize() { return size; }
    public long getTotalElements() { return totalElements; }
    public int getTotalPages() { return totalPages; }
    public boolean isLast() { return last; }
}
