package bg.softuni.bookshelf.web.controller;

import bg.softuni.bookshelf.data.enums.BookFormat;
import bg.softuni.bookshelf.service.book.dto.*;
import bg.softuni.bookshelf.shared.exception.BusinessException;
import bg.softuni.bookshelf.shared.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockApplicationUser
class BookControllerTest extends AbstractControllerTestBase {

    private static final String BASE_URL = "/api/books";

    // --- Object Mother Centralized Factories ---

    private BookDetailsDto createMockBookDetailsDto(UUID id, String title) {
        return new BookDetailsDto(
                id,
                title,
                "9780007525492",
                310,
                1937,
                "Fictional universe text description.",
                BookFormat.HARDCOVER,
                new AuthorDto(UUID.randomUUID(), "J.R.R. Tolkien"),
                new LanguageDto(UUID.randomUUID(), "English"),
                new PublisherDto(UUID.randomUUID(), "George Allen & Unwin"),
                Set.of(new GenreDto(UUID.randomUUID(), "Fantasy")),
                "https://res.cloudinary.com/bookshelf/image/upload/covers/hobbit.jpg"
        );
    }

    private BookSummaryDto createMockBookSummaryDto(UUID id, String title) {
        return new BookSummaryDto(
                id,
                title,
                "J.R.R. Tolkien",
                "https://res.cloudinary.com/bookshelf/image/upload/covers/hobbit.jpg"
        );
    }

    @Nested
    @DisplayName("GET /api/books")
    class GetAllBooksTests {

        @Test
        @DisplayName("Happy Path: Should return 200 OK and PagedResponse format payload")
        void shouldReturn200AndPagedResponse() throws Exception {
            // Arrange
            UUID bookId = UUID.randomUUID();
            List<BookSummaryDto> booksList = List.of(createMockBookSummaryDto(bookId, "The Hobbit"));
            Page<BookSummaryDto> page = new PageImpl<>(booksList);
            given(bookService.getAll(any())).willReturn(page);

            // Act
            ResultActions result = mockMvc.perform(get(BASE_URL));

            // Assert
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].id").value(bookId.toString()))
                    .andExpect(jsonPath("$.content[0].title").value("The Hobbit"))
                    .andExpect(jsonPath("$.totalElements").value(1));

            verify(bookService).getAll(any());
        }
    }

    @Nested
    @DisplayName("GET /api/books/{id}")
    class GetBookByIdTests {

        @Test
        @DisplayName("Happy Path: Should return 200 OK and explicit BookDetailsDto when unique identifier matches")
        void shouldGetBookById() throws Exception {
            // Arrange
            UUID id = UUID.randomUUID();
            BookDetailsDto mockDto = createMockBookDetailsDto(id, "The Hobbit");
            given(bookService.getById(id)).willReturn(mockDto);

            // Act
            ResultActions result = mockMvc.perform(get(BASE_URL + "/{id}", id)
                    .contentType(MediaType.APPLICATION_JSON));

            // Assert
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id.toString()))
                    .andExpect(jsonPath("$.title").value("The Hobbit"))
                    .andExpect(jsonPath("$.isbn").value("9780007525492"))
                    .andExpect(jsonPath("$.pages").value(310))
                    .andExpect(jsonPath("$.format").value("HARDCOVER"))
                    .andExpect(jsonPath("$.author.name").value("J.R.R. Tolkien"))
                    .andExpect(jsonPath("$.language.name").value("English"))
                    .andExpect(jsonPath("$.publisher.name").value("George Allen & Unwin"))
                    .andExpect(jsonPath("$.genres[0].name").value("Fantasy"))
                    .andExpect(jsonPath("$.coverImageUrl").value("https://res.cloudinary.com/bookshelf/image/upload/covers/hobbit.jpg"));

            verify(bookService).getById(id);
        }

        @Test
        @DisplayName("Error Case: Should return 404 Not Found along with comprehensive ProblemDetail mapping structure")
        void shouldReturn404WhenBookNotFound() throws Exception {
            // Arrange
            UUID id = UUID.randomUUID();
            given(bookService.getById(id)).willThrow(new BusinessException(ErrorCode.BOOK_NOT_FOUND));

            // Act
            ResultActions result = mockMvc.perform(get(BASE_URL + "/{id}", id));

            // Assert
            result.andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Business Rule Violation"))
                    .andExpect(jsonPath("$.errorCode").value("E1100"));

            verify(bookService).getById(id);
        }
    }

    @Nested
    @DisplayName("GET /api/books/search")
    class SearchBooksTests {

        @Test
        @DisplayName("Happy Path: Should return 200 OK and populated PagedResponse on valid matching query execution")
        void shouldReturn200AndPagedResponse() throws Exception {
            // Arrange
            UUID bookId = UUID.randomUUID();
            Page<BookSummaryDto> page = new PageImpl<>(List.of(createMockBookSummaryDto(bookId, "The Hobbit")));
            given(bookService.searchBooks(any(), any())).willReturn(page);

            // Act
            ResultActions result = mockMvc.perform(get(BASE_URL + "/search")
                    .param("query", "Hobbit"));

            // Assert
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].title").value("The Hobbit"))
                    .andExpect(jsonPath("$.totalElements").value(1));

            verify(bookService).searchBooks(any(), any());
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"", "   "})
        @DisplayName("Edge Case: Should process empty fallback parsing conditions cleanly without triggering core engine interrupts")
        void shouldHandleNullOrBlankQuery(String query) throws Exception {
            // Arrange
            Page<BookSummaryDto> page = new PageImpl<>(Collections.emptyList());
            given(bookService.searchBooks(any(), any())).willReturn(page);

            // Act
            ResultActions result = mockMvc.perform(get(BASE_URL + "/search")
                    .param("query", query));

            // Assert
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.totalElements").value(0));

            verify(bookService).searchBooks(any(), any());
        }
    }
}