package bg.softuni.bookshelf.web.controller;

import bg.softuni.bookshelf.config.SecurityConfig;
import bg.softuni.bookshelf.service.book.dto.BookDetailsDto;
import bg.softuni.bookshelf.shared.exception.BusinessException;
import bg.softuni.bookshelf.shared.exception.ErrorCode;
import bg.softuni.bookshelf.web.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {BookController.class, GlobalExceptionHandler.class})
@ActiveProfiles("test")
@Import(SecurityConfig.class)
class BookControllerTest extends AbstractControllerTestBase {

    private static final String BASE_URL = "/api/books";


    @Nested
    @DisplayName("GET /api/books")
    class GetAllBooksTests {
        @Test
        @DisplayName("Should return 200 OK for anonymous user")
        void shouldReturn200_forAnonymousUser() throws Exception {
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk());
        }
    }

    @Test
    @DisplayName("GET /api/books/{id} - Should return 200 and BookDetailsDto when book exists")
    void shouldGetBookById() throws Exception {
        // Arrange
        UUID id = UUID.randomUUID();
        BookDetailsDto mockDto = new BookDetailsDto(id, "The Hobbit", null, 300, 1937, null, null, null, null, null, null, null);
        given(bookService.getById(id)).willReturn(mockDto);

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.title").value("The Hobbit"));
    }

    @Test
    @DisplayName("GET /api/books/{id} - Should return 404 when book does not exist")
    void shouldReturn404WhenBookNotFound() throws Exception {
        // Arrange
        UUID id = UUID.randomUUID();
        given(bookService.getById(id)).willThrow(new BusinessException(ErrorCode.BOOK_NOT_FOUND));

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Nested
    @DisplayName("GET /api/books/search")
    class SearchBooksTests {
        @Test
        @DisplayName("Should return 200 OK for anonymous user")
        void shouldReturn200_forAnonymousUser() throws Exception {
            mockMvc.perform(get(BASE_URL + "/search").param("query", "test"))
                    .andExpect(status().isOk());
        }
    }
}
