package bg.softuni.bookshelf.web.controller;

import bg.softuni.bookshelf.service.book.dto.BookSummaryDto;
import bg.softuni.bookshelf.service.bookshelf.dto.BookshelfCreateDto;
import bg.softuni.bookshelf.service.bookshelf.dto.BookshelfDetailsDto;
import bg.softuni.bookshelf.service.bookshelf.dto.BookshelfSummaryDto;
import bg.softuni.bookshelf.service.bookshelf.dto.BookshelfUpdateDto;
import bg.softuni.bookshelf.shared.dto.PagedResponse;
import bg.softuni.bookshelf.shared.exception.BusinessException;
import bg.softuni.bookshelf.shared.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserShelfController.class)
@DisplayName("UserShelfController API Tests")
class UserShelfControllerTest extends AbstractControllerTestBase {

    private static final String BASE_URL = "/api/users/me/shelves";

    @Nested
    @DisplayName("GET " + BASE_URL)
    class GetUserShelvesTests {

        @Test
        @WithMockApplicationUser
        @DisplayName("Happy Path: Should return 200 OK for an authenticated user")
        void shouldReturn200_forAuthenticatedUser() throws Exception {
            // Arrange
            PagedResponse<BookshelfSummaryDto> mockResponse = new PagedResponse<>(
                    Collections.emptyList(), 0, 10, 0L, 0, true
            );
            given(bookshelfService.getShelvesForUser(any(UUID.class), any())).willReturn(mockResponse);

            // Act & Assert
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.totalElements").value(0));

            verify(bookshelfService).getShelvesForUser(any(UUID.class), any());
        }

        @Test
        @DisplayName("Security: Should return 401 Unauthorized for an anonymous user")
        void shouldReturn401_forAnonymousUser() throws Exception {
            // Act & Assert
            mockMvc.perform(get(BASE_URL)).andExpect(status().isUnauthorized());
            verifyNoInteractions(bookshelfService);
        }

        @Test
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Method Security: Should block administrators from retrieving personal bookshelves")
        void shouldReturn403_whenAdminAttempts() throws Exception {
            // Act & Assert
            mockMvc.perform(get(BASE_URL)).andExpect(status().isForbidden());
            verifyNoInteractions(bookshelfService);
        }
    }

    @Nested
    @DisplayName("POST " + BASE_URL)
    class CreateShelfTests {

        @Test
        @WithMockApplicationUser
        @DisplayName("Happy Path: Should return 201 Created with a Location header")
        void shouldReturn201_whenShelfIsCreated() throws Exception {
            // Arrange
            BookshelfCreateDto createDto = BookshelfCreateDto.builder().name("New Shelf").build();
            BookshelfDetailsDto detailsDto = BookshelfDetailsDto.builder().id(UUID.randomUUID()).name("New Shelf").build();
            given(bookshelfService.createShelf(any(BookshelfCreateDto.class), any(UUID.class))).willReturn(detailsDto);

            // Act & Assert
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDto)))
                    .andExpect(status().isCreated())
                    .andExpect(header().exists("Location"))
                    .andExpect(jsonPath("$.id").value(detailsDto.id().toString()));
        }

        @Test
        @WithMockApplicationUser
        @DisplayName("Validation: Should return 400 Bad Request when the name is too short")
        void shouldReturn400_forInvalidInput() throws Exception {
            // Arrange
            BookshelfCreateDto createDto = BookshelfCreateDto.builder().name("S").build();

            // Act & Assert
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.name").exists());

            verifyNoInteractions(bookshelfService);
        }

        @Test
        @DisplayName("Security: Should return 401 Unauthorized for an anonymous user")
        void shouldReturn401_forAnonymousUser() throws Exception {
            // Arrange
            BookshelfCreateDto createDto = BookshelfCreateDto.builder().name("New Shelf").build();

            // Act & Assert
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDto)))
                    .andExpect(status().isUnauthorized());
            verifyNoInteractions(bookshelfService);
        }

        @Test
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Method Security: Should block administrators from creating personal bookshelves")
        void shouldReturn403_whenAdminAttempts() throws Exception {
            // Arrange
            BookshelfCreateDto createDto = BookshelfCreateDto.builder().name("Admin Personal Shelf").build();

            // Act & Assert
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDto)))
                    .andExpect(status().isForbidden());
            verifyNoInteractions(bookshelfService);
        }
    }

    @Nested
    @DisplayName("GET " + BASE_URL + "/{shelfId}")
    class GetShelfByIdTests {

        @Test
        @WithMockApplicationUser
        @DisplayName("Happy Path: Should return 200 OK with shelf details when the shelf exists")
        void shouldReturn200_whenShelfExists() throws Exception {
            // Arrange
            UUID shelfId = UUID.randomUUID();
            BookshelfDetailsDto detailsDto = BookshelfDetailsDto.builder().id(shelfId).name("My Shelf").build();
            given(bookshelfService.getShelfById(shelfId)).willReturn(detailsDto);

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/{shelfId}", shelfId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(shelfId.toString()));
        }

        @Test
        @WithMockApplicationUser
        @DisplayName("Error Path: Should return 404 Not Found when the shelf does not exist")
        void shouldReturn404_whenShelfNotFound() throws Exception {
            // Arrange
            UUID shelfId = UUID.randomUUID();
            given(bookshelfService.getShelfById(shelfId)).willThrow(new BusinessException(ErrorCode.BOOKSHELF_NOT_FOUND));

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/{shelfId}", shelfId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("E1200"));
        }

        @Test
        @DisplayName("Security: Should return 401 Unauthorized for an anonymous user")
        void shouldReturn401_forAnonymousUser() throws Exception {
            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/{shelfId}", UUID.randomUUID()))
                    .andExpect(status().isUnauthorized());
            verifyNoInteractions(bookshelfService);
        }
    }

    @Nested
    @DisplayName("GET " + BASE_URL + "/{shelfId}/books")
    class GetBooksInShelfTests {

        @Test
        @WithMockApplicationUser
        @DisplayName("Happy Path: Should return 200 OK for an authenticated user")
        void shouldReturn200_forAuthenticatedUser() throws Exception {
            // Arrange
            UUID shelfId = UUID.randomUUID();
            PagedResponse<BookSummaryDto> mockResponse = new PagedResponse<>(
                    Collections.emptyList(), 0, 10, 0L, 0, true
            );
            given(bookshelfService.getBooksInShelf(eq(shelfId), any())).willReturn(mockResponse);

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/{shelfId}/books", shelfId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.totalElements").value(0));
        }

        @Test
        @DisplayName("Security: Should return 401 Unauthorized for an anonymous user")
        void shouldReturn401_forAnonymousUser() throws Exception {
            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/{shelfId}/books", UUID.randomUUID()))
                    .andExpect(status().isUnauthorized());
            verifyNoInteractions(bookshelfService);
        }
    }

    @Nested
    @DisplayName("PUT " + BASE_URL + "/{shelfId}")
    class UpdateShelfTests {

        @Test
        @WithMockApplicationUser
        @DisplayName("Happy Path: Should return 200 OK when the shelf is updated")
        void shouldReturn200_whenUpdated() throws Exception {
            // Arrange
            UUID shelfId = UUID.randomUUID();
            BookshelfUpdateDto updateDto = BookshelfUpdateDto.builder().name("Renamed Shelf").build();
            BookshelfDetailsDto detailsDto = BookshelfDetailsDto.builder().id(shelfId).name("Renamed Shelf").build();
            given(bookshelfService.updateShelf(eq(shelfId), any(BookshelfUpdateDto.class))).willReturn(detailsDto);

            // Act & Assert
            mockMvc.perform(put(BASE_URL + "/{shelfId}", shelfId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(shelfId.toString()))
                    .andExpect(jsonPath("$.name").value("Renamed Shelf"));

            verify(bookshelfService).updateShelf(eq(shelfId), any(BookshelfUpdateDto.class));
        }

        @Test
        @WithMockApplicationUser
        @DisplayName("Validation: Should return 400 Bad Request when the name is too short")
        void shouldReturn400_forInvalidInput() throws Exception {
            // Arrange
            UUID shelfId = UUID.randomUUID();
            BookshelfUpdateDto updateDto = BookshelfUpdateDto.builder().name("S").build();

            // Act & Assert
            mockMvc.perform(put(BASE_URL + "/{shelfId}", shelfId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.name").exists());

            verifyNoInteractions(bookshelfService);
        }

        @Test
        @WithMockApplicationUser
        @DisplayName("Error Path: Should return 404 Not Found when the shelf does not exist")
        void shouldReturn404_whenShelfNotFound() throws Exception {
            // Arrange
            UUID shelfId = UUID.randomUUID();
            BookshelfUpdateDto updateDto = BookshelfUpdateDto.builder().name("Renamed Shelf").build();
            given(bookshelfService.updateShelf(eq(shelfId), any(BookshelfUpdateDto.class)))
                    .willThrow(new BusinessException(ErrorCode.BOOKSHELF_NOT_FOUND));

            // Act & Assert
            mockMvc.perform(put(BASE_URL + "/{shelfId}", shelfId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("E1200"));
        }

        @Test
        @DisplayName("Security: Should return 401 Unauthorized for an anonymous user")
        void shouldReturn401_forAnonymousUser() throws Exception {
            // Arrange
            BookshelfUpdateDto updateDto = BookshelfUpdateDto.builder().name("Renamed Shelf").build();

            // Act & Assert
            mockMvc.perform(put(BASE_URL + "/{shelfId}", UUID.randomUUID())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isUnauthorized());
            verifyNoInteractions(bookshelfService);
        }
    }

    @Nested
    @DisplayName("DELETE " + BASE_URL + "/{shelfId}")
    class DeleteShelfTests {

        @Test
        @WithMockApplicationUser
        @DisplayName("Happy Path: Should return 204 No Content when the shelf is deleted")
        void shouldReturn204_whenDeleted() throws Exception {
            // Arrange
            UUID shelfId = UUID.randomUUID();
            doNothing().when(bookshelfService).deleteShelf(shelfId);

            // Act & Assert
            mockMvc.perform(delete(BASE_URL + "/{shelfId}", shelfId))
                    .andExpect(status().isNoContent());

            verify(bookshelfService).deleteShelf(shelfId);
        }

        @Test
        @WithMockApplicationUser
        @DisplayName("Error Path: Should return 404 Not Found when the shelf does not exist")
        void shouldReturn404_whenShelfNotFound() throws Exception {
            // Arrange
            UUID shelfId = UUID.randomUUID();
            doThrow(new BusinessException(ErrorCode.BOOKSHELF_NOT_FOUND)).when(bookshelfService).deleteShelf(shelfId);

            // Act & Assert
            mockMvc.perform(delete(BASE_URL + "/{shelfId}", shelfId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("E1200"));
        }

        @Test
        @DisplayName("Security: Should return 401 Unauthorized for an anonymous user")
        void shouldReturn401_forAnonymousUser() throws Exception {
            // Act & Assert
            mockMvc.perform(delete(BASE_URL + "/{shelfId}", UUID.randomUUID()))
                    .andExpect(status().isUnauthorized());
            verifyNoInteractions(bookshelfService);
        }
    }

    @Nested
    @DisplayName("POST " + BASE_URL + "/{shelfId}/books")
    class AddBookToShelfTests {

        @Test
        @WithMockApplicationUser
        @DisplayName("Happy Path: Should return 204 No Content when a book is added")
        void shouldReturn204_whenBookAdded() throws Exception {
            // Arrange
            UUID shelfId = UUID.randomUUID();
            UUID bookId = UUID.randomUUID();
            doNothing().when(bookshelfService).addBookToShelf(eq(shelfId), any());
            String body = objectMapper.writeValueAsString(Map.of("bookId", bookId.toString()));

            // Act & Assert
            mockMvc.perform(post(BASE_URL + "/{shelfId}/books", shelfId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isNoContent());

            verify(bookshelfService).addBookToShelf(eq(shelfId), any());
        }

        @Test
        @WithMockApplicationUser
        @DisplayName("Validation: Should return 400 Bad Request when the book id is missing")
        void shouldReturn400_whenBookIdMissing() throws Exception {
            // Arrange
            UUID shelfId = UUID.randomUUID();
            String body = objectMapper.writeValueAsString(Map.of());

            // Act & Assert
            mockMvc.perform(post(BASE_URL + "/{shelfId}/books", shelfId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.bookId").exists());

            verifyNoInteractions(bookshelfService);
        }

        @Test
        @WithMockApplicationUser
        @DisplayName("Error Path: Should return 409 Conflict when the book is already on the shelf")
        void shouldReturn409_whenBookAlreadyInShelf() throws Exception {
            // Arrange
            UUID shelfId = UUID.randomUUID();
            UUID bookId = UUID.randomUUID();
            doThrow(new BusinessException(ErrorCode.BOOK_ALREADY_IN_SHELF)).when(bookshelfService).addBookToShelf(eq(shelfId), any());
            String body = objectMapper.writeValueAsString(Map.of("bookId", bookId.toString()));

            // Act & Assert
            mockMvc.perform(post(BASE_URL + "/{shelfId}/books", shelfId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value("E1201"));
        }

        @Test
        @DisplayName("Security: Should return 401 Unauthorized for an anonymous user")
        void shouldReturn401_forAnonymousUser() throws Exception {
            // Arrange
            String body = objectMapper.writeValueAsString(Map.of("bookId", UUID.randomUUID().toString()));

            // Act & Assert
            mockMvc.perform(post(BASE_URL + "/{shelfId}/books", UUID.randomUUID())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isUnauthorized());
            verifyNoInteractions(bookshelfService);
        }
    }

    @Nested
    @DisplayName("DELETE " + BASE_URL + "/{shelfId}/books/{bookId}")
    class RemoveBookFromShelfTests {

        @Test
        @WithMockApplicationUser
        @DisplayName("Happy Path: Should return 204 No Content when a book is removed")
        void shouldReturn204_whenBookRemoved() throws Exception {
            // Arrange
            UUID shelfId = UUID.randomUUID();
            UUID bookId = UUID.randomUUID();
            doNothing().when(bookshelfService).removeBookFromShelf(shelfId, bookId);

            // Act & Assert
            mockMvc.perform(delete(BASE_URL + "/{shelfId}/books/{bookId}", shelfId, bookId))
                    .andExpect(status().isNoContent());

            verify(bookshelfService).removeBookFromShelf(shelfId, bookId);
        }

        @Test
        @WithMockApplicationUser
        @DisplayName("Error Path: Should return 404 Not Found when the book is not on the shelf")
        void shouldReturn404_whenBookNotInShelf() throws Exception {
            // Arrange
            UUID shelfId = UUID.randomUUID();
            UUID bookId = UUID.randomUUID();
            doThrow(new BusinessException(ErrorCode.BOOK_NOT_IN_SHELF)).when(bookshelfService).removeBookFromShelf(shelfId, bookId);

            // Act & Assert
            mockMvc.perform(delete(BASE_URL + "/{shelfId}/books/{bookId}", shelfId, bookId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("E1202"));
        }

        @Test
        @DisplayName("Security: Should return 401 Unauthorized for an anonymous user")
        void shouldReturn401_forAnonymousUser() throws Exception {
            // Act & Assert
            mockMvc.perform(delete(BASE_URL + "/{shelfId}/books/{bookId}", UUID.randomUUID(), UUID.randomUUID()))
                    .andExpect(status().isUnauthorized());
            verifyNoInteractions(bookshelfService);
        }
    }
}