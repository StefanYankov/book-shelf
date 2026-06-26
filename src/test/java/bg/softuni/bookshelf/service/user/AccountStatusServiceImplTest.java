package bg.softuni.bookshelf.service.user;

import bg.softuni.bookshelf.data.entity.identity.AccountStatusEvent;
import bg.softuni.bookshelf.data.enums.StatusEventType;
import bg.softuni.bookshelf.data.repository.AccountStatusEventRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AccountStatusServiceImplTest {

    @Mock
    private AccountStatusEventRepository accountStatusEventRepository;

    @InjectMocks
    private AccountStatusServiceImpl accountStatusService;

    @Nested
    @DisplayName("isUserActive Tests")
    class IsUserActiveTests {

        @Test
        @DisplayName("Should return true if user has no status events")
        void shouldReturnTrue_WhenNoEvents() {
            // Arrange
            UUID userId = UUID.randomUUID();
            given(accountStatusEventRepository.findMostRecentEventForUser(any(), any())).willReturn(Collections.emptyList());

            // Act
            boolean isActive = accountStatusService.isUserActive(userId);

            // Assert
            assertThat(isActive).isTrue();
        }

        @Test
        @DisplayName("Should return true if latest event is UNLOCKED")
        void shouldReturnTrue_WhenLatestEventIsUnlocked() {
            // Arrange
            UUID userId = UUID.randomUUID();
            AccountStatusEvent event = new AccountStatusEvent();
            event.setEventType(StatusEventType.ACCOUNT_UNLOCKED);
            given(accountStatusEventRepository.findMostRecentEventForUser(any(), any())).willReturn(List.of(event));

            // Act
            boolean isActive = accountStatusService.isUserActive(userId);

            // Assert
            assertThat(isActive).isTrue();
        }

        @Test
        @DisplayName("Should return true if latest event is UNBANNED")
        void shouldReturnTrue_WhenLatestEventIsUnbanned() {
            // Arrange
            UUID userId = UUID.randomUUID();
            AccountStatusEvent event = new AccountStatusEvent();
            event.setEventType(StatusEventType.ACCOUNT_UNBANNED);
            given(accountStatusEventRepository.findMostRecentEventForUser(any(), any())).willReturn(List.of(event));

            // Act
            boolean isActive = accountStatusService.isUserActive(userId);

            // Assert
            assertThat(isActive).isTrue();
        }

        @Test
        @DisplayName("Should return false if latest event is LOCKED")
        void shouldReturnFalse_WhenLatestEventIsLocked() {
            // Arrange
            UUID userId = UUID.randomUUID();
            AccountStatusEvent event = new AccountStatusEvent();
            event.setEventType(StatusEventType.ACCOUNT_LOCKED);
            given(accountStatusEventRepository.findMostRecentEventForUser(any(), any())).willReturn(List.of(event));

            // Act
            boolean isActive = accountStatusService.isUserActive(userId);

            // Assert
            assertThat(isActive).isFalse();
        }

        @Test
        @DisplayName("Should return false if latest event is BANNED")
        void shouldReturnFalse_WhenLatestEventIsBanned() {
            // Arrange
            UUID userId = UUID.randomUUID();
            AccountStatusEvent event = new AccountStatusEvent();
            event.setEventType(StatusEventType.ACCOUNT_BANNED);
            given(accountStatusEventRepository.findMostRecentEventForUser(any(), any())).willReturn(List.of(event));

            // Act
            boolean isActive = accountStatusService.isUserActive(userId);

            // Assert
            assertThat(isActive).isFalse();
        }
    }
}
