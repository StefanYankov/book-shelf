package bg.softuni.bookshelf.web.controller;

import bg.softuni.bookshelf.data.enums.BookFormat;
import bg.softuni.bookshelf.service.book.dto.*;
import bg.softuni.bookshelf.shared.exception.BusinessException;
import bg.softuni.bookshelf.shared.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminBookController.class)
@DisplayName("AdminBookController API Tests")
class AdminBookControllerTest extends AbstractControllerTestBase {

    private static final String BASE_URL = "/api/admin/books";

    private static final String VALID_BOOK_JSON = """
            {
              "title": "Dune",
              "isbn": "978-0441013593",
              "pages": 412,
              "yearPublished": 1965,
              "summary": "A desert planet and the struggle for a rare resource.",
              "format": "HARDCOVER",
              "authorId": "55555555-0000-0000-0000-000000000005",
              "languageId": "88888888-0000-0000-0000-000000000001",
              "publisherId": "99999999-0000-0000-0000-000000000001",
              "genreIds": ["77777777-0000-0000-0000-000000000001"]
            }
            """;

    private MockMultipartFile bookPart(String json) {
        return new MockMultipartFile("book", "", MediaType.APPLICATION_JSON_VALUE, json.getBytes());
    }

    private BookDetailsDto createMockBookDetailsDto(UUID id, String title, String coverUrl) {
        return new BookDetailsDto(
                id,
                title,
                "978-0441013593",
                412,
                1965,
                "A desert planet and the struggle for a rare resource.",
                BookFormat.HARDCOVER,
                new BookAuthorDto(UUID.randomUUID(), "Frank Herbert"),
                new BookLanguageDto(UUID.randomUUID(), "English"),
                new BookPublisherDto(UUID.randomUUID(), "Chilton Books"),
                Set.of(new BookGenreDto(UUID.randomUUID(), "Science Fiction")),
                coverUrl
        );
    }

    @Nested
    @DisplayName("POST /api/admin/books")
    class CreateBookTests {

        @Test
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Happy Path: Admin creates a book (multipart, no image) -> 201")
        void adminCreatesWithoutImage() throws Exception {
            // Arrange
            UUID id = UUID.randomUUID();
            given(bookService.createBook(any(), any()))
                    .willReturn(createMockBookDetailsDto(id, "Dune", null));

            // Act & Assert
            mockMvc.perform(multipart(BASE_URL).file(bookPart(VALID_BOOK_JSON)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(id.toString()))
                    .andExpect(jsonPath("$.title").value("Dune"));

            verify(bookService).createBook(any(), any());
        }

        @Test
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Happy Path: Admin creates a book with a cover image part -> 201")
        void adminCreatesWithImage() throws Exception {
            // Arrange
            UUID id = UUID.randomUUID();
            String coverUrl = "https://res.cloudinary.com/demo/image/upload/dune.jpg";
            given(bookService.createBook(any(), any()))
                    .willReturn(createMockBookDetailsDto(id, "Dune", coverUrl));
            MockMultipartFile image = new MockMultipartFile("image", "cover.jpg", "image/jpeg", new byte[]{1, 2, 3});

            // Act & Assert
            mockMvc.perform(multipart(BASE_URL).file(bookPart(VALID_BOOK_JSON)).file(image))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.coverImageUrl").value(coverUrl));

            verify(bookService).createBook(any(), any());
        }

        @Test
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Validation: A blank title is rejected with 400")
        void blankTitleRejected() throws Exception {
            // Arrange
            String invalid = VALID_BOOK_JSON.replace("\"Dune\"", "\"\"");

            // Act & Assert
            mockMvc.perform(multipart(BASE_URL).file(bookPart(invalid)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.title").exists());

            verifyNoInteractions(bookService);
        }

        @Test
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Error Path: A missing referenced author yields 404")
        void referencedAuthorNotFound() throws Exception {
            // Arrange
            given(bookService.createBook(any(), any()))
                    .willThrow(new BusinessException(ErrorCode.AUTHOR_NOT_FOUND));

            // Act & Assert
            mockMvc.perform(multipart(BASE_URL).file(bookPart(VALID_BOOK_JSON)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("E2300"));
        }

        @Test
        @WithMockApplicationUser(roles = "USER")
        @DisplayName("Security: A non-admin cannot create a book")
        void userForbidden() throws Exception {
            // Act & Assert
            mockMvc.perform(multipart(BASE_URL).file(bookPart(VALID_BOOK_JSON)))
                    .andExpect(status().isForbidden());
            verifyNoInteractions(bookService);
        }
    }

    @Nested
    @DisplayName("PUT /api/admin/books/{id}")
    class UpdateBookTests {

        @Test
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Happy Path: Admin updates a book")
        void adminUpdates() throws Exception {
            // Arrange
            UUID id = UUID.randomUUID();
            given(bookService.updateBook(any(), any()))
                    .willReturn(createMockBookDetailsDto(id, "Dune Messiah", null));
            String body = "{\"title\":\"Dune Messiah\"}";

            // Act & Assert
            mockMvc.perform(put(BASE_URL + "/" + id).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Dune Messiah"));

            verify(bookService).updateBook(any(), any());
        }

        @Test
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Error Path: Updating an unknown book yields 404")
        void notFound() throws Exception {
            // Arrange
            UUID id = UUID.randomUUID();
            given(bookService.updateBook(any(), any())).willThrow(new BusinessException(ErrorCode.BOOK_NOT_FOUND));
            String body = "{\"title\":\"Dune Messiah\"}";

            // Act & Assert
            mockMvc.perform(put(BASE_URL + "/" + id).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("E1100"));
        }

        @Test
        @WithMockApplicationUser(roles = "USER")
        @DisplayName("Security: A non-admin cannot update a book")
        void userForbidden() throws Exception {
            // Arrange
            String body = "{\"title\":\"Dune Messiah\"}";

            // Act & Assert
            mockMvc.perform(put(BASE_URL + "/" + UUID.randomUUID()).contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isForbidden());
            verifyNoInteractions(bookService);
        }
    }

    @Nested
    @DisplayName("DELETE /api/admin/books/{id}")
    class DeleteBookTests {

        @Test
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Happy Path: Admin deletes a book -> 204")
        void adminDeletes() throws Exception {
            // Arrange
            UUID id = UUID.randomUUID();
            doNothing().when(bookService).deleteBook(id);

            // Act & Assert
            mockMvc.perform(delete(BASE_URL + "/" + id)).andExpect(status().isNoContent());
            verify(bookService).deleteBook(id);
        }

        @Test
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Error Path: Deleting an unknown book yields 404")
        void notFound() throws Exception {
            // Arrange
            UUID id = UUID.randomUUID();
            doThrow(new BusinessException(ErrorCode.BOOK_NOT_FOUND)).when(bookService).deleteBook(id);

            // Act & Assert
            mockMvc.perform(delete(BASE_URL + "/" + id))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("E1100"));
        }

        @Test
        @WithMockApplicationUser(roles = "USER")
        @DisplayName("Security: A non-admin cannot delete a book")
        void userForbidden() throws Exception {
            // Act & Assert
            mockMvc.perform(delete(BASE_URL + "/" + UUID.randomUUID())).andExpect(status().isForbidden());
            verifyNoInteractions(bookService);
        }
    }
}
