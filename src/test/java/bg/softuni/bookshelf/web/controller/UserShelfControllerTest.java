package bg.softuni.bookshelf.web.controller;

import bg.softuni.bookshelf.config.SecurityConfig;
import bg.softuni.bookshelf.service.bookshelf.dto.BookshelfCreateDto;
import bg.softuni.bookshelf.service.bookshelf.dto.BookshelfDetailsDto;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {UserShelfController.class, GlobalExceptionHandler.class})
@ActiveProfiles("test")
@Import(SecurityConfig.class)
class UserShelfControllerTest extends AbstractControllerTestBase {

    private static final String BASE_URL = "/api/users/me/shelves";

    @Nested
    @DisplayName("GET " + BASE_URL)
    class GetUserShelvesTests {

        @Test
        @WithMockApplicationUser
        @DisplayName("Should return 200 OK for authenticated user")
        void shouldReturn200_forAuthenticatedUser() throws Exception {
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should return 403 Forbidden for anonymous user")
        void shouldReturn403_forAnonymousUser() throws Exception {
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("POST " + BASE_URL)
    class CreateShelfTests {

        @Test
        @WithMockApplicationUser
        @DisplayName("Should return 201 Created when shelf is created successfully")
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
        @DisplayName("Should return 403 Forbidden for anonymous user")
        void shouldReturn403_forAnonymousUser() throws Exception {
            // Arrange
            BookshelfCreateDto createDto = BookshelfCreateDto.builder().name("New Shelf").build();

            // Act & Assert
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDto)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockApplicationUser
        @DisplayName("Should return 400 Bad Request for invalid input")
        void shouldReturn400_forInvalidInput() throws Exception {
            // Arrange
            BookshelfCreateDto createDto = BookshelfCreateDto.builder().name("S").build(); // Name is too short

            // Act & Assert
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createDto)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET " + BASE_URL + "/{shelfId}")
    class GetShelfByIdTests {

        @Test
        @WithMockApplicationUser
        @DisplayName("Should return 200 OK and shelf details when shelf exists")
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
        @DisplayName("Should return 404 Not Found when shelf does not exist")
        void shouldReturn404_whenShelfNotFound() throws Exception {
            // Arrange
            UUID shelfId = UUID.randomUUID();
            given(bookshelfService.getShelfById(shelfId)).willThrow(new BusinessException(ErrorCode.BOOKSHELF_NOT_FOUND));

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/{shelfId}", shelfId))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 403 Forbidden for anonymous user")
        void shouldReturn403_forAnonymousUser() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{shelfId}", UUID.randomUUID()))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET " + BASE_URL + "/{shelfId}/books")
    class GetBooksInShelfTests {

        @Test
        @WithMockApplicationUser
        @DisplayName("Should return 200 OK for authenticated user")
        void shouldReturn200_forAuthenticatedUser() throws Exception {
            // Arrange
            UUID shelfId = UUID.randomUUID();

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/{shelfId}/books", shelfId))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should return 403 Forbidden for anonymous user")
        void shouldReturn403_forAnonymousUser() throws Exception {
            // Arrange
            UUID shelfId = UUID.randomUUID();

            // Act & Assert
            mockMvc.perform(get(BASE_URL + "/{shelfId}/books", shelfId))
                    .andExpect(status().isForbidden());
        }
    }
}
