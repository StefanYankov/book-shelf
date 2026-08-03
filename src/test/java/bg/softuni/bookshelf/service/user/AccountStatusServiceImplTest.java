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

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountStatusService Unit Tests")
class AccountStatusServiceImplTest {

    @Mock
    private AccountStatusEventRepository accountStatusEventRepository;

    @InjectMocks
    private AccountStatusServiceImpl accountStatusService;

    private AccountStatusEvent event(StatusEventType type, Instant expiry) {
        AccountStatusEvent e = new AccountStatusEvent();
        e.setEventType(type);
        e.setExpiryDate(expiry);
        return e;
    }

    @Nested
    @DisplayName("isUserActive Tests")
    class IsUserActiveTests {

        @Test
        @DisplayName("Should return true if user has no status events")
        void shouldReturnTrue_WhenNoEvents() {
            // Arrange
            UUID userId = UUID.randomUUID();
            given(accountStatusEventRepository.findMostRecentEventForUser(any(), any())).willReturn(Collections.emptyList());

            // Act & Assert
            assertThat(accountStatusService.isUserActive(userId)).isTrue();
        }

        @Test
        @DisplayName("Should return true if latest event is UNLOCKED")
        void shouldReturnTrue_WhenLatestEventIsUnlocked() {
            // Arrange
            UUID userId = UUID.randomUUID();
            given(accountStatusEventRepository.findMostRecentEventForUser(any(), any()))
                    .willReturn(List.of(event(StatusEventType.ACCOUNT_UNLOCKED, null)));

            // Act & Assert
            assertThat(accountStatusService.isUserActive(userId)).isTrue();
        }

        @Test
        @DisplayName("Should return true if latest event is UNBANNED")
        void shouldReturnTrue_WhenLatestEventIsUnbanned() {
            // Arrange
            UUID userId = UUID.randomUUID();
            given(accountStatusEventRepository.findMostRecentEventForUser(any(), any()))
                    .willReturn(List.of(event(StatusEventType.ACCOUNT_UNBANNED, null)));

            // Act & Assert
            assertThat(accountStatusService.isUserActive(userId)).isTrue();
        }

        @Test
        @DisplayName("Should return false if latest event is a permanent LOCK (null expiry)")
        void shouldReturnFalse_WhenLatestEventIsPermanentLock() {
            // Arrange
            UUID userId = UUID.randomUUID();
            given(accountStatusEventRepository.findMostRecentEventForUser(any(), any()))
                    .willReturn(List.of(event(StatusEventType.ACCOUNT_LOCKED, null)));

            // Act & Assert
            assertThat(accountStatusService.isUserActive(userId)).isFalse();
        }

        @Test
        @DisplayName("Should return false if latest event is BANNED")
        void shouldReturnFalse_WhenLatestEventIsBanned() {
            // Arrange
            UUID userId = UUID.randomUUID();
            given(accountStatusEventRepository.findMostRecentEventForUser(any(), any()))
                    .willReturn(List.of(event(StatusEventType.ACCOUNT_BANNED, null)));

            // Act & Assert
            assertThat(accountStatusService.isUserActive(userId)).isFalse();
        }

        @Test
        @DisplayName("Should return false for a temporary LOCK that has NOT yet expired")
        void shouldReturnFalse_WhenTemporaryLockStillActive() {
            // Arrange
            UUID userId = UUID.randomUUID();
            Instant future = Instant.now().plus(Duration.ofHours(1));
            given(accountStatusEventRepository.findMostRecentEventForUser(any(), any()))
                    .willReturn(List.of(event(StatusEventType.ACCOUNT_LOCKED, future)));

            // Act & Assert
            assertThat(accountStatusService.isUserActive(userId)).isFalse();
        }

        @Test
        @DisplayName("Should return true for a temporary LOCK whose expiry has already passed")
        void shouldReturnTrue_WhenTemporaryLockExpired() {
            // Arrange
            UUID userId = UUID.randomUUID();
            Instant past = Instant.now().minus(Duration.ofHours(1));
            given(accountStatusEventRepository.findMostRecentEventForUser(any(), any()))
                    .willReturn(List.of(event(StatusEventType.ACCOUNT_LOCKED, past)));

            // Act & Assert
            assertThat(accountStatusService.isUserActive(userId)).isTrue();
        }
    }
}