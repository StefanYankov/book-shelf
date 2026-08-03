package bg.softuni.bookshelf.service.user;

import bg.softuni.bookshelf.data.entity.identity.AccountStatusEvent;
import bg.softuni.bookshelf.data.entity.identity.ApplicationUser;
import bg.softuni.bookshelf.data.enums.StatusEventType;
import bg.softuni.bookshelf.data.repository.AccountStatusEventRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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

    private AccountStatusEvent event(UUID userId, StatusEventType type, Instant expiry, Instant createdAt) {
        ApplicationUser user = new ApplicationUser();
        user.setId(userId);
        AccountStatusEvent event = new AccountStatusEvent();
        event.setUser(user);
        event.setEventType(type);
        event.setExpiryDate(expiry);
        ReflectionTestUtils.setField(event, "createdAt", createdAt);
        return event;
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

    @Nested
    @DisplayName("getActiveStatus Tests")
    class GetActiveStatusTests {

        @Test
        @DisplayName("Returns an empty map for an empty input without querying")
        void shouldReturnEmptyForEmptyInput() {
            assertThat(accountStatusService.getActiveStatus(List.of())).isEmpty();
        }

        @Test
        @DisplayName("Defaults a user with no events to active")
        void shouldDefaultNoEventsToActive() {
            // Arrange
            UUID userId = UUID.randomUUID();
            given(accountStatusEventRepository.findAllByUserIds(any())).willReturn(List.of());

            // Act
            Map<UUID, Boolean> result = accountStatusService.getActiveStatus(List.of(userId));

            // Assert
            assertThat(result).containsEntry(userId, true);
        }

        @Test
        @DisplayName("Uses only each user's latest event to determine status")
        void shouldUseLatestEventPerUser() {
            // Arrange
            UUID userA = UUID.randomUUID();
            UUID userB = UUID.randomUUID();
            Instant t0 = Instant.now().minus(Duration.ofHours(2));
            Instant t1 = Instant.now().minus(Duration.ofHours(1));

            given(accountStatusEventRepository.findAllByUserIds(any())).willReturn(List.of(
                    event(userA, StatusEventType.ACCOUNT_UNLOCKED, null, t0),
                    event(userA, StatusEventType.ACCOUNT_LOCKED, null, t1),
                    event(userB, StatusEventType.ACCOUNT_LOCKED, null, t0),
                    event(userB, StatusEventType.ACCOUNT_UNLOCKED, null, t1)
            ));

            // Act
            Map<UUID, Boolean> result = accountStatusService.getActiveStatus(List.of(userA, userB));

            // Assert
            assertThat(result).containsEntry(userA, false);
            assertThat(result).containsEntry(userB, true);
        }
    }
}