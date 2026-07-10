package bg.softuni.bookshelf.shared.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * A generic, framework-agnostic DTO for representing a paginated API response.
 * This decouples the API contract from the backend's specific pagination implementation (e.g., Spring's Page).
 *
 * @param <T> The type of the content in the page.
 * @param content The list of items on the current page.
 * @param pageNumber The current page number (0-indexed).
 * @param pageSize The number of items requested per page.
 * @param totalElements The total number of items across all pages.
 * @param totalPages The total number of pages available.
 * @param isLast A boolean flag indicating if this is the last page.
 */
public record PagedResponse<T>(
        List<T> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean isLast
) {
    /**
     * Factory method to create a PagedResponse from a Spring Data Page object.
     *
     * @param page The Page object from the service layer.
     * @param <T>  The type of the content.
     * @return A new PagedResponse instance.
     */
    public static <T> PagedResponse<T> from(Page<T> page) {
        return new PagedResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
