package bg.softuni.bookshelf.web.controller;

import bg.softuni.bookshelf.service.book.dto.BookDetailsDto;
import bg.softuni.bookshelf.service.book.dto.BookUpdateDto;
import bg.softuni.bookshelf.service.bookshelf.dto.BookshelfDetailsDto;
import bg.softuni.bookshelf.service.bookshelf.dto.BookshelfUpdateDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminModerationController.class)
@DisplayName("AdminModerationController Access Bounds and Validation Tests")
class AdminModerationControllerTest extends AbstractControllerTestBase {

    private static final String BASE_URL = "/api/admin/moderation";

    @Nested
    @DisplayName("PUT " + BASE_URL + "/books/{bookId}")
    class ModerateBookTests {

        @Test
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Happy Path: Should allow authorized administrator to moderate book metadata")
        void shouldAllowAdminToModerateBook() throws Exception {
            UUID bookId = UUID.randomUUID();
            BookUpdateDto updateDto = BookUpdateDto.builder().title("Sanitized Title").build();
            BookDetailsDto detailsDto = BookDetailsDto.builder().id(bookId).title("Sanitized Title").build();

            given(bookService.moderateBook(any(UUID.class), any(BookUpdateDto.class))).willReturn(detailsDto);

            mockMvc.perform(put(BASE_URL + "/books/{bookId}", bookId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(bookId.toString()))
                    .andExpect(jsonPath("$.title").value("Sanitized Title"));

            verify(bookService).moderateBook(eq(bookId), any(BookUpdateDto.class));
        }

        @Test
        @WithMockApplicationUser(roles = "USER")
        @DisplayName("Security Error: Should block standard users from moderating book details")
        void shouldBlockUserFromBookModeration() throws Exception {
            UUID bookId = UUID.randomUUID();
            BookUpdateDto updateDto = BookUpdateDto.builder().title("Sanitized Title").build();

            mockMvc.perform(put(BASE_URL + "/books/{bookId}", bookId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isForbidden());

            verifyNoInteractions(bookService);
        }
    }

    @Nested
    @DisplayName("PUT " + BASE_URL + "/shelves/{shelfId}")
    class ModerateShelfTests {

        @Test
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Happy Path: Should allow authorized administrator to moderate user bookshelves")
        void shouldAllowAdminToModerateShelf() throws Exception {
            UUID shelfId = UUID.randomUUID();
            BookshelfUpdateDto updateDto = BookshelfUpdateDto.builder().name("Sanitized Title").build();
            BookshelfDetailsDto detailsDto = BookshelfDetailsDto.builder().id(shelfId).name("Sanitized Title").build();

            given(bookshelfService.updateShelf(any(UUID.class), any(BookshelfUpdateDto.class))).willReturn(detailsDto);

            mockMvc.perform(put(BASE_URL + "/shelves/{shelfId}", shelfId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(shelfId.toString()))
                    .andExpect(jsonPath("$.name").value("Sanitized Title"));

            verify(bookshelfService).updateShelf(eq(shelfId), any(BookshelfUpdateDto.class));
        }

        @Test
        @WithMockApplicationUser(roles = "USER")
        @DisplayName("Security Error: Should block standard users from moderating user bookshelves")
        void shouldBlockUserFromShelfModeration() throws Exception {
            UUID shelfId = UUID.randomUUID();
            BookshelfUpdateDto updateDto = BookshelfUpdateDto.builder().name("Sanitized Title").build();

            mockMvc.perform(put(BASE_URL + "/shelves/{shelfId}", shelfId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isForbidden());

            verifyNoInteractions(bookshelfService);
        }
    }

    @Nested
    @DisplayName("DELETE " + BASE_URL + "/shelves/{shelfId}")
    class DeleteShelfTests {

        @Test
        @WithMockApplicationUser(roles = "ADMIN")
        @DisplayName("Happy Path: Should allow authorized administrator to forcibly delete user bookshelves")
        void shouldAllowAdminToDeleteShelf() throws Exception {
            UUID shelfId = UUID.randomUUID();
            doNothing().when(bookshelfService).deleteShelf(shelfId);

            mockMvc.perform(delete(BASE_URL + "/shelves/{shelfId}", shelfId))
                    .andExpect(status().isNoContent());

            verify(bookshelfService).deleteShelf(shelfId);
        }

        @Test
        @WithMockApplicationUser(roles = "USER")
        @DisplayName("Security Error: Should block standard users from executing forced bookshelf deletions")
        void shouldBlockUserFromForcedDeletion() throws Exception {
            UUID shelfId = UUID.randomUUID();

            mockMvc.perform(delete(BASE_URL + "/shelves/{shelfId}", shelfId))
                    .andExpect(status().isForbidden());

            verifyNoInteractions(bookshelfService);
        }
    }
}
