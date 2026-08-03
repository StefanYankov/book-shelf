package bg.softuni.bookshelf.service.user;

import bg.softuni.bookshelf.data.entity.identity.AccountStatusEvent;
import bg.softuni.bookshelf.data.entity.identity.ApplicationUser;
import bg.softuni.bookshelf.data.enums.StatusEventType;
import bg.softuni.bookshelf.data.repository.AccountStatusEventRepository;
import bg.softuni.bookshelf.data.repository.VerificationTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountMaintenanceService Unit Tests")
class AccountMaintenanceServiceTest {

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @Mock
    private AccountStatusEventRepository accountStatusEventRepository;

    @InjectMocks
    private AccountMaintenanceService accountMaintenanceService;

    @Captor
    private ArgumentCaptor<AccountStatusEvent> eventCaptor;

    private ApplicationUser user(UUID id) {
        ApplicationUser u = new ApplicationUser();
        u.setId(id);
        u.setUsername("user-" + id);
        return u;
    }

    private AccountStatusEvent lockEvent(ApplicationUser owner) {
        AccountStatusEvent e = new AccountStatusEvent();
        e.setUser(owner);
        e.setEventType(StatusEventType.ACCOUNT_LOCKED);
        return e;
    }

    @Nested
    @DisplayName("purgeExpiredTokens Tests")
    class PurgeExpiredTokensTests {

        @Test
        @DisplayName("Delegates to the repository and returns the deleted count")
        void shouldDeleteExpiredTokens() {
            // Arrange
            given(verificationTokenRepository.deleteByExpiryDateBefore(any())).willReturn(7);

            // Act
            int deleted = accountMaintenanceService.purgeExpiredTokens();

            // Assert
            assertThat(deleted).isEqualTo(7);
            verify(verificationTokenRepository).deleteByExpiryDateBefore(any());
        }

        @Test
        @DisplayName("Returns zero when there is nothing to purge")
        void shouldReturnZeroWhenNothingExpired() {
            // Arrange
            given(verificationTokenRepository.deleteByExpiryDateBefore(any())).willReturn(0);

            // Act
            int deleted = accountMaintenanceService.purgeExpiredTokens();

            // Assert
            assertThat(deleted).isZero();
        }
    }

    @Nested
    @DisplayName("reconcileExpiredLocks Tests")
    class ReconcileExpiredLocksTests {

        @Test
        @DisplayName("Writes a system unlock event for each expired lock")
        void shouldUnlockEachExpiredLock() {
            // Arrange: two users with expired temporary locks
            ApplicationUser u1 = user(UUID.randomUUID());
            ApplicationUser u2 = user(UUID.randomUUID());
            given(accountStatusEventRepository.findExpiredActiveLocks(any()))
                    .willReturn(List.of(lockEvent(u1), lockEvent(u2)));

            // Act
            int reconciled = accountMaintenanceService.reconcileExpiredLocks();

            // Assert
            assertThat(reconciled).isEqualTo(2);
            verify(accountStatusEventRepository, times(2)).save(eventCaptor.capture());

            List<AccountStatusEvent> saved = eventCaptor.getAllValues();
            assertThat(saved).allSatisfy(e -> {
                assertThat(e.getEventType()).isEqualTo(StatusEventType.ACCOUNT_UNLOCKED);
                assertThat(e.getActor()).isNull();                 // system-generated
                assertThat(e.getExpiryDate()).isNull();            // unlocks never expire
                assertThat(e.getReason()).isEqualTo("Temporary lock expired");
            });
            assertThat(saved).extracting(AccountStatusEvent::getUser).containsExactly(u1, u2);
        }

        @Test
        @DisplayName("Does nothing when there are no expired locks")
        void shouldDoNothingWhenNoExpiredLocks() {
            // Arrange
            given(accountStatusEventRepository.findExpiredActiveLocks(any())).willReturn(List.of());

            // Act
            int reconciled = accountMaintenanceService.reconcileExpiredLocks();

            // Assert
            assertThat(reconciled).isZero();
            verify(accountStatusEventRepository, never()).save(any());
        }
    }
}